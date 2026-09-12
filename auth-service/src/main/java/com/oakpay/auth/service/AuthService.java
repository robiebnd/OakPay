package com.oakpay.auth.service;

import com.oakpay.auth.api.AuthDtos;
import com.oakpay.auth.api.TwoFactorDtos;
import com.oakpay.auth.security.JwtService;
import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.user.PasswordResetToken;
import com.oakpay.auth.user.PasswordResetTokenRepository;
import com.oakpay.auth.user.PendingRegistration;
import com.oakpay.auth.user.PendingRegistrationRepository;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final String REFRESH_KEY_PREFIX = "oakpay:auth:refresh:";
    private static final String VERIFY_RESEND_PREFIX = "oakpay:auth:verify-resend:";
    private static final String RESET_RESEND_PREFIX = "oakpay:auth:reset-resend:";
    private static final Duration VERIFY_TTL = Duration.ofMinutes(15);
    private static final Duration RESET_TTL = Duration.ofMinutes(15);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordResetTokenRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    private final Duration refreshTokenTtl;
    private final VerificationEmailService emailService;
    private final TwoFactorService twoFactorService;

    public AuthService(UserRepository userRepository,
                       PendingRegistrationRepository pendingRegistrationRepository,
                       PasswordResetTokenRepository passwordResetRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       StringRedisTemplate redisTemplate,
                       @Value("${oakpay.jwt.refresh-token-ttl:7d}") Duration refreshTokenTtl,
                       VerificationEmailService emailService,
                       TwoFactorService twoFactorService) {
        this.userRepository = userRepository;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.refreshTokenTtl = refreshTokenTtl;
        this.emailService = emailService;
        this.twoFactorService = twoFactorService;
    }

    @Transactional
    public AuthDtos.RegistrationResponse register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("An account with this email already exists");
        PendingRegistration pending = pendingRegistrationRepository.findByEmailIgnoreCase(email).orElseGet(PendingRegistration::new);
        pending.setEmail(email); pending.setPassword(passwordEncoder.encode(request.password())); pending.setFirstName(request.firstName().trim()); pending.setLastName(request.lastName().trim());
        String code = issueVerification(pending); pendingRegistrationRepository.save(pending);
        return new AuthDtos.RegistrationResponse(new AuthDtos.UserResponse(pending.getId(),pending.getEmail(),pending.getFirstName(),pending.getLastName(),false,null,null,null),developmentCode(code));
    }

    @Transactional
    public AuthDtos.VerificationResponse verifyEmail(AuthDtos.VerifyEmailRequest request) {
        String email=normalizeEmail(request.email()); PendingRegistration pending=pendingRegistrationRepository.findByEmailIgnoreCase(email).orElseThrow(()->new IllegalArgumentException("Invalid or expired verification code"));
        if(pending.getVerificationExpiresAt().isBefore(LocalDateTime.now())||!sha256(request.code().trim()).equals(pending.getVerificationCodeHash())) throw new IllegalArgumentException("Invalid or expired verification code");
        if(userRepository.existsByEmailIgnoreCase(email)){pendingRegistrationRepository.delete(pending);return new AuthDtos.VerificationResponse("Email is already verified.",true,null);}
        User user=new User(); user.setEmail(pending.getEmail()); user.setPassword(pending.getPassword()); user.setFirstName(pending.getFirstName()); user.setLastName(pending.getLastName()); user.setEmailVerified(true); userRepository.save(user); pendingRegistrationRepository.delete(pending);
        return new AuthDtos.VerificationResponse("Email verified successfully. Your account has been created.",true,null);
    }

    @Transactional
    public AuthDtos.VerificationResponse resendVerification(AuthDtos.ResendVerificationRequest request) {
        String email=normalizeEmail(request.email()); PendingRegistration pending=pendingRegistrationRepository.findByEmailIgnoreCase(email).orElse(null);
        if(pending==null||userRepository.existsByEmailIgnoreCase(email)) return new AuthDtos.VerificationResponse("If a pending registration exists for this email, a new verification code has been sent.",false,null);
        enforceCooldown(VERIFY_RESEND_PREFIX+sha256(email)); String code=issueVerification(pending); pendingRegistrationRepository.save(pending);
        return new AuthDtos.VerificationResponse("A new verification code has been sent.",false,developmentCode(code));
    }

    public AuthDtos.PasswordResetResponse forgotPassword(AuthDtos.ForgotPasswordRequest request) {
        String email=normalizeEmail(request.email()); User user=userRepository.findByEmailIgnoreCase(email).orElse(null);
        if(user==null||!user.isEmailVerified()) return new AuthDtos.PasswordResetResponse("If an account exists for this email, a password reset code has been sent.",false,null);
        enforceCooldown(RESET_RESEND_PREFIX+sha256(email)); String code=issuePasswordReset(user);
        return new AuthDtos.PasswordResetResponse("If an account exists for this email, a password reset code has been sent.",false,developmentCode(code));
    }

    @Transactional
    public AuthDtos.PasswordResetResponse resetPassword(AuthDtos.ResetPasswordRequest request) {
        String email=normalizeEmail(request.email()); User user=userRepository.findByEmailIgnoreCase(email).orElseThrow(()->new IllegalArgumentException("Invalid or expired password reset code"));
        if(!user.isEmailVerified()) throw new IllegalArgumentException("Invalid or expired password reset code");
        PasswordResetToken token=passwordResetRepository.findByTokenHash(sha256(request.code().trim())).filter(t->t.getUserId().equals(user.getId())).orElseThrow(()->new IllegalArgumentException("Invalid or expired password reset code"));
        if(token.getUsedAt()!=null||token.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Invalid or expired password reset code");
        user.setPassword(passwordEncoder.encode(request.newPassword())); token.setUsedAt(LocalDateTime.now()); userRepository.save(user); passwordResetRepository.save(token);
        return new AuthDtos.PasswordResetResponse("Password reset successfully. You can now sign in.",true,null);
    }

    @Transactional
    public void changePassword(UserPrincipal principal, AuthDtos.ChangePasswordRequest request) {
        User user=userRepository.findById(principal.getUserId()).orElseThrow(()->new UsernameNotFoundException("User not found"));
        if(!user.isEmailVerified()) throw new IllegalArgumentException("Email verification is required");
        if(!passwordEncoder.matches(request.currentPassword(),user.getPassword())) throw new IllegalArgumentException("Current password is incorrect");
        if(request.currentPassword().equals(request.newPassword())) throw new IllegalArgumentException("New password must be different from your current password");
        user.setPassword(passwordEncoder.encode(request.newPassword())); userRepository.save(user);
    }

    public TwoFactorDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        User user=userRepository.findByEmailIgnoreCase(normalizeEmail(request.email())).orElseThrow(()->new IllegalArgumentException("Invalid email or password"));
        if(!user.isEmailVerified()) throw new IllegalArgumentException("Please verify your email before signing in");
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),request.password()));
        UserPrincipal principal=(UserPrincipal)authentication.getPrincipal();
        if(user.isTwoFactorEnabled()) return new TwoFactorDtos.LoginResponse("Bearer",null,null,0,true,twoFactorService.createChallenge(user.getId()));
        AuthDtos.TokenResponse tokens=issueTokens(principal);
        return new TwoFactorDtos.LoginResponse(tokens.tokenType(),tokens.accessToken(),tokens.refreshToken(),tokens.expiresIn(),false,null);
    }

    public AuthDtos.TokenResponse verifyTwoFactorLogin(TwoFactorDtos.LoginVerifyRequest request) {
        UUID userId=twoFactorService.verifyChallenge(request.challengeToken(),request.code());
        User user=userRepository.findById(userId).orElseThrow(()->new UsernameNotFoundException("User not found"));
        return issueTokens(UserPrincipal.from(user));
    }

    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest request) {
        Claims claims=jwtService.parse(request.refreshToken()); if(!jwtService.isRefreshToken(claims)) throw new IllegalArgumentException("Invalid refresh token");
        String tokenHash=sha256(request.refreshToken()); String userId=redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX+tokenHash); if(userId==null) throw new IllegalArgumentException("Refresh token is expired or revoked");
        User user=userRepository.findById(UUID.fromString(userId)).orElseThrow(()->new UsernameNotFoundException("User not found")); if(!user.isEmailVerified()) throw new IllegalArgumentException("Email verification is required"); UserPrincipal principal=UserPrincipal.from(user); redisTemplate.delete(REFRESH_KEY_PREFIX+tokenHash); return issueTokens(principal);
    }

    public void logout(AuthDtos.RefreshRequest request){redisTemplate.delete(REFRESH_KEY_PREFIX+sha256(request.refreshToken()));}
    private String issueVerification(PendingRegistration pending){String code=generateCode();pending.setVerificationCodeHash(sha256(code));pending.setVerificationExpiresAt(LocalDateTime.now().plus(VERIFY_TTL));pending.setUpdatedAt(LocalDateTime.now());emailService.sendCode(pending,code);return code;}
    private String issuePasswordReset(User user){passwordResetRepository.deleteByUserId(user.getId());String code=generateCode();PasswordResetToken token=new PasswordResetToken();token.setUserId(user.getId());token.setTokenHash(sha256(code));token.setExpiresAt(LocalDateTime.now().plus(RESET_TTL));passwordResetRepository.save(token);emailService.sendPasswordReset(user,code);return code;}
    private void enforceCooldown(String key){Boolean allowed=redisTemplate.opsForValue().setIfAbsent(key,"1",RESEND_COOLDOWN);if(!Boolean.TRUE.equals(allowed))throw new IllegalArgumentException("Please wait before requesting another code");}
    private String developmentCode(String code){return emailService.isDeliveryEnabled()?null:code;}
    private String generateCode(){return String.format("%06d",secureRandom.nextInt(1_000_000));}
    private AuthDtos.TokenResponse issueTokens(UserPrincipal principal){String accessToken=jwtService.generateAccessToken(principal);String refreshToken=jwtService.generateRefreshToken(principal);redisTemplate.opsForValue().set(REFRESH_KEY_PREFIX+sha256(refreshToken),principal.getUserId().toString(),refreshTokenTtl);return new AuthDtos.TokenResponse("Bearer",accessToken,refreshToken,jwtService.getAccessTokenTtlSeconds());}
    private String normalizeEmail(String email){return email.trim().toLowerCase(Locale.ROOT);}
    private String sha256(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException("SHA-256 is not available",e);}}
}
