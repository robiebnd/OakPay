package com.oakpay.auth.service;

import com.oakpay.auth.user.PendingRegistration;
import com.oakpay.auth.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VerificationEmailService {
    private static final Logger log = LoggerFactory.getLogger(VerificationEmailService.class);
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public VerificationEmailService(JavaMailSender mailSender,
                                    @Value("${oakpay.mail.enabled:false}") boolean enabled,
                                    @Value("${oakpay.mail.from:noreply@oakpay.local}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public boolean isDeliveryEnabled() { return enabled; }

    public void send(User user, String code) {
        sendCode(user.getEmail(), user.getFirstName(), code);
    }

    public void sendCode(String email, String firstName, String code) {
        if (!enabled) {
            log.info("OakPay email verification code for {}: {} (mail delivery disabled)", email, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Verify your OakPay email");
        message.setText("Hello " + firstName + ",\n\n"
                + "Your OakPay verification code is: " + code + "\n\n"
                + "This code expires in 15 minutes and can only be used once.\n\n"
                + "If you did not start an OakPay registration, you can ignore this email.\n\n"
                + "OakPay");
        mailSender.send(message);
    }

    public void sendCode(PendingRegistration registration, String code) {
        sendCode(registration.getEmail(), registration.getFirstName(), code);
    }

    public void sendPasswordReset(User user, String code) {
        if (!enabled) {
            log.info("OakPay password reset code for {}: {} (mail delivery disabled)", user.getEmail(), code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Reset your OakPay password");
        message.setText("Hello " + user.getFirstName() + ",\n\n"
                + "Your OakPay password reset code is: " + code + "\n\n"
                + "This code expires in 15 minutes and can only be used once.\n\n"
                + "If you did not request a password reset, you can ignore this email.\n\n"
                + "OakPay");
        mailSender.send(message);
    }
}
