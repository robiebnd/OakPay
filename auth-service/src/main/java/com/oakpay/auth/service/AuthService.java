package com.oakpay.auth.service;

import com.oakpay.auth.api.AuthDtos;
import com.oakpay.auth.security.JwtService;
import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.user.EmailVerificationToken;
import com.oakpay.auth.user.EmailVerificationTokenRepository;
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
    private static final String REFRESH_KEY_PREFIX="oakpay:auth:refresh:";
    private static final String VERIFY_RESEND_PREFIX="oakpay:auth:verify-resend:";
    private static final Duration VERIFY_TTL=Duration.ofMinutes(15);
    private static final Duration RESEND_COOLDOWN=Duration.ofSeconds(60);
    private final SecureRandom secureRandom=new SecureRandom();
    private final UserRepository userRepository; private final EmailVerificationTokenRepository verificationRepository;
    private final PasswordEncoder passwordEncoder; private final AuthenticationManager authenticationManager; private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate; private final Duration refreshTokenTtl; private final VerificationEmailService emailService;
    public AuthService(UserRepository userRepository,EmailVerificationTokenRepository verificationRepository,PasswordEncoder passwordEncoder,AuthenticationManager authenticationManager,JwtService jwtService,StringRedisTemplate redisTemplate,@Value("${oakpay.jwt.refresh-token-ttl:7d}") Duration refreshTokenTtl,VerificationEmailService emailService){this.userRepository=userRepository;this.verificationRepository=verificationRepository;this.passwordEncoder=passwordEncoder;this.authenticationManager=authenticationManager;this.jwtService=jwtService;this.redisTemplate=redisTemplate;this.refreshTokenTtl=refreshTokenTtl;this.emailService=emailService;}
    @Transactional public AuthDtos.UserResponse register(AuthDtos.RegisterRequest request){String email=request.email().trim().toLowerCase();if(userRepository.existsByEmailIgnoreCase(email))throw new IllegalArgumentException("An account with this email already exists");User user=new User();user.setEmail(email);user.setPassword(passwordEncoder.encode(request.password()));user.setFirstName(request.firstName().trim());user.setLastName(request.lastName().trim());user=userRepository.save(user);issueVerification(user);return toResponse(user);}
    @Transactional public AuthDtos.VerificationResponse verifyEmail(AuthDtos.VerifyEmailRequest request){String email=request.email().trim().toLowerCase();User user=userRepository.findByEmailIgnoreCase(email).orElseThrow(()->new IllegalArgumentException("Invalid verification details"));if(user.isEmailVerified())return new AuthDtos.VerificationResponse("Email is already verified.",true);EmailVerificationToken token=verificationRepository.findByTokenHash(sha256(request.code().trim())).filter(t->t.getUserId().equals(user.getId())).orElseThrow(()->new IllegalArgumentException("Invalid or expired verification code"));if(token.getUsedAt()!=null||token.getExpiresAt().isBefore(LocalDateTime.now()))throw new IllegalArgumentException("Invalid or expired verification code");user.setEmailVerified(true);token.setUsedAt(LocalDateTime.now());userRepository.save(user);verificationRepository.save(token);return new AuthDtos.VerificationResponse("Email verified successfully.",true);}
    @Transactional public AuthDtos.VerificationResponse resendVerification(AuthDtos.ResendVerificationRequest request){String email=request.email().trim().toLowerCase();User user=userRepository.findByEmailIgnoreCase(email).orElse(null);if(user==null||user.isEmailVerified())return new AuthDtos.VerificationResponse("If the account exists and is not verified, a new verification code has been sent.",false);String key=VERIFY_RESEND_PREFIX+sha256(email);Boolean allowed=redisTemplate.opsForValue().setIfAbsent(key,"1",RESEND_COOLDOWN);if(!Boolean.TRUE.equals(allowed))throw new IllegalArgumentException("Please wait before requesting another verification code");issueVerification(user);return new AuthDtos.VerificationResponse("A new verification code has been sent.",false);}
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request){Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().trim(),request.password()));User user=userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase()).orElseThrow(()->new UsernameNotFoundException("User not found"));if(!user.isEmailVerified())throw new IllegalArgumentException("Please verify your email before signing in");UserPrincipal principal=(UserPrincipal)authentication.getPrincipal();return issueTokens(principal);}
    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest request){Claims claims=jwtService.parse(request.refreshToken());if(!jwtService.isRefreshToken(claims))throw new IllegalArgumentException("Invalid refresh token");String tokenHash=sha256(request.refreshToken());String userId=redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX+tokenHash);if(userId==null)throw new IllegalArgumentException("Refresh token is expired or revoked");User user=userRepository.findById(UUID.fromString(userId)).orElseThrow(()->new UsernameNotFoundException("User not found"));UserPrincipal principal=UserPrincipal.from(user);redisTemplate.delete(REFRESH_KEY_PREFIX+tokenHash);return issueTokens(principal);}
    public void logout(AuthDtos.RefreshRequest request){redisTemplate.delete(REFRESH_KEY_PREFIX+sha256(request.refreshToken()));}
    private void issueVerification(User user){verificationRepository.deleteByUserId(user.getId());String code=String.format("%06d",secureRandom.nextInt(1_000_000));EmailVerificationToken token=new EmailVerificationToken();token.setUserId(user.getId());token.setTokenHash(sha256(code));token.setExpiresAt(LocalDateTime.now().plus(VERIFY_TTL));verificationRepository.save(token);emailService.send(user,code);}
    private AuthDtos.TokenResponse issueTokens(UserPrincipal principal){String accessToken=jwtService.generateAccessToken(principal);String refreshToken=jwtService.generateRefreshToken(principal);redisTemplate.opsForValue().set(REFRESH_KEY_PREFIX+sha256(refreshToken),principal.getUserId().toString(),refreshTokenTtl);return new AuthDtos.TokenResponse("Bearer",accessToken,refreshToken,jwtService.getAccessTokenTtlSeconds());}
    private AuthDtos.UserResponse toResponse(User user){return new AuthDtos.UserResponse(user.getId(),user.getEmail(),user.getFirstName(),user.getLastName(),user.isEmailVerified());}
    private String sha256(String value){try{byte[] digest=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(digest);}catch(NoSuchAlgorithmException e){throw new IllegalStateException("SHA-256 is unavailable",e);}}
}
