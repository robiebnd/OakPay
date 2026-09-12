package com.oakpay.auth.service;

import com.oakpay.auth.api.TwoFactorDtos;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TwoFactorService {
    private static final String CHALLENGE_PREFIX = "oakpay:auth:2fa:";
    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);
    private static final String ISSUER = "OakPay";
    private static final int SECRET_BYTES = 20;
    private static final int DIGITS = 6;
    private static final int PERIOD_SECONDS = 30;
    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final byte[] encryptionKey;

    public TwoFactorService(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            StringRedisTemplate redisTemplate,
                            @Value("${oakpay.two-factor.encryption-key:oakpay-development-2fa-key-change-before-production-2026}") String encryptionKey) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.encryptionKey = sha256Bytes(encryptionKey);
    }

    public TwoFactorDtos.StatusResponse status(UUID userId) {
        User user=getUser(userId);
        return new TwoFactorDtos.StatusResponse(user.isTwoFactorEnabled());
    }

    @Transactional
    public TwoFactorDtos.SetupResponse setup(UUID userId) {
        User user=getUser(userId);
        if(user.isTwoFactorEnabled()) throw new IllegalArgumentException("Two-factor authentication is already enabled");
        String secret=base32Encode(randomBytes(SECRET_BYTES));
        user.setTwoFactorSecretEncrypted(encrypt(secret));
        userRepository.save(user);
        String label=ISSUER+":"+user.getEmail();
        String uri="otpauth://totp/"+urlEncode(label)+"?secret="+secret+"&issuer="+urlEncode(ISSUER)+"&algorithm=SHA1&digits="+DIGITS+"&period="+PERIOD_SECONDS;
        return new TwoFactorDtos.SetupResponse(secret,uri);
    }

    @Transactional
    public TwoFactorDtos.EnabledResponse enable(UUID userId,String code) {
        User user=getUser(userId);
        if(user.isTwoFactorEnabled()) return new TwoFactorDtos.EnabledResponse(true,"Two-factor authentication is already enabled.");
        if(user.getTwoFactorSecretEncrypted()==null) throw new IllegalArgumentException("Start two-factor setup first");
        if(!verifyCode(decrypt(user.getTwoFactorSecretEncrypted()),code)) throw new IllegalArgumentException("Invalid authenticator code");
        user.setTwoFactorEnabled(true); userRepository.save(user);
        return new TwoFactorDtos.EnabledResponse(true,"Two-factor authentication enabled successfully.");
    }

    @Transactional
    public TwoFactorDtos.EnabledResponse disable(UUID userId,String password,String code) {
        User user=getUser(userId);
        if(!user.isTwoFactorEnabled()) return new TwoFactorDtos.EnabledResponse(false,"Two-factor authentication is already disabled.");
        if(!passwordEncoder.matches(password,user.getPassword())) throw new IllegalArgumentException("Current password is incorrect");
        if(user.getTwoFactorSecretEncrypted()==null || !verifyCode(decrypt(user.getTwoFactorSecretEncrypted()),code)) throw new IllegalArgumentException("Invalid authenticator code");
        user.setTwoFactorEnabled(false); user.setTwoFactorSecretEncrypted(null); userRepository.save(user);
        return new TwoFactorDtos.EnabledResponse(false,"Two-factor authentication disabled successfully.");
    }

    public String createChallenge(UUID userId) {
        String challenge=UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(CHALLENGE_PREFIX+challenge,userId.toString(),CHALLENGE_TTL);
        return challenge;
    }

    public UUID verifyChallenge(String challenge,String code) {
        String userId=redisTemplate.opsForValue().get(CHALLENGE_PREFIX+challenge);
        if(userId==null) throw new IllegalArgumentException("Two-factor challenge has expired. Please sign in again.");
        User user=getUser(UUID.fromString(userId));
        if(!user.isTwoFactorEnabled()||user.getTwoFactorSecretEncrypted()==null) throw new IllegalArgumentException("Two-factor authentication is not enabled");
        if(!verifyCode(decrypt(user.getTwoFactorSecretEncrypted()),code)) throw new IllegalArgumentException("Invalid authenticator code");
        redisTemplate.delete(CHALLENGE_PREFIX+challenge);
        return user.getId();
    }

    private User getUser(UUID id){return userRepository.findById(id).orElseThrow(()->new IllegalArgumentException("User not found"));}
    private byte[] randomBytes(int length){byte[] value=new byte[length];random.nextBytes(value);return value;}
    private boolean verifyCode(String secret,String code){if(code==null||!code.matches("\\d{6}"))return false;long counter=System.currentTimeMillis()/1000/PERIOD_SECONDS;for(long offset=-1;offset<=1;offset++)if(generateCode(secret,counter+offset).equals(code))return true;return false;}
    private String generateCode(String secret,long counter){try{byte[] key=base32Decode(secret);byte[] data=ByteBuffer.allocate(8).putLong(counter).array();Mac mac=Mac.getInstance("HmacSHA1");mac.init(new SecretKeySpec(key,"HmacSHA1"));byte[] hash=mac.doFinal(data);int offset=hash[hash.length-1]&0x0f;int binary=((hash[offset]&0x7f)<<24)|((hash[offset+1]&0xff)<<16)|((hash[offset+2]&0xff)<<8)|(hash[offset+3]&0xff);return String.format("%06d",binary%1_000_000);}catch(GeneralSecurityException e){throw new IllegalStateException("Unable to verify authenticator code",e);}}
    private String encrypt(String plaintext){try{byte[] iv=randomBytes(12);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(encryptionKey,"AES"),new GCMParameterSpec(128,iv));byte[] encrypted=cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));byte[] combined=ByteBuffer.allocate(iv.length+encrypted.length).put(iv).put(encrypted).array();return Base64.getEncoder().encodeToString(combined);}catch(GeneralSecurityException e){throw new IllegalStateException("Unable to protect two-factor secret",e);}}
    private String decrypt(String value){try{byte[] combined=Base64.getDecoder().decode(value);byte[] iv=new byte[12];byte[] encrypted=new byte[combined.length-12];System.arraycopy(combined,0,iv,0,12);System.arraycopy(combined,12,encrypted,0,encrypted.length);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(encryptionKey,"AES"),new GCMParameterSpec(128,iv));return new String(cipher.doFinal(encrypted),StandardCharsets.UTF_8);}catch(GeneralSecurityException|IllegalArgumentException e){throw new IllegalStateException("Unable to read two-factor secret",e);}}
    private byte[] sha256Bytes(String value){try{return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException("SHA-256 is not available",e);}}
    private String urlEncode(String value){return value.replace("%","%25").replace(" ","%20").replace(":","%3A").replace("@","%40");}
    private static final char[] BASE32="ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private String base32Encode(byte[] data){StringBuilder out=new StringBuilder((data.length*8+4)/5);int buffer=0,bits=0;for(byte b:data){buffer=(buffer<<8)|(b&255);bits+=8;while(bits>=5){out.append(BASE32[(buffer>>(bits-5))&31]);bits-=5;}}if(bits>0)out.append(BASE32[(buffer<<(5-bits))&31]);return out.toString();}
    private byte[] base32Decode(String value){String normalized=value.replace("=","").replace(" ","").toUpperCase();byte[] out=new byte[normalized.length()*5/8+1];int buffer=0,bits=0,index=0;for(char c:normalized.toCharArray()){int digit=base32Digit(c);if(digit<0)throw new IllegalArgumentException("Invalid two-factor secret");buffer=(buffer<<5)|digit;bits+=5;if(bits>=8){out[index++]=(byte)((buffer>>(bits-8))&255);bits-=8;}}byte[] exact=new byte[index];System.arraycopy(out,0,exact,0,index);return exact;}
    private int base32Digit(char c){if(c>='A'&&c<='Z')return c-'A';if(c>='2'&&c<='7')return c-'2'+26;return -1;}
}
