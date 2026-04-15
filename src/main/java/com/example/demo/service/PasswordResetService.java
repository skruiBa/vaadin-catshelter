package com.example.demo.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.AppUser;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@catshelter.local}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<AppUser> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            return;
        }

        AppUser user = userOpt.get();
        tokenRepository.deleteByUser_Id(user.getId());
        tokenRepository.deleteByExpiresAtBefore(Instant.now());

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        tokenRepository.save(token);

        sendResetEmail(user, token.getToken());
    }

    @Transactional(readOnly = true)
    public boolean isResetTokenValid(String tokenValue) {
        return tokenRepository.findByToken(tokenValue)
                .map(PasswordResetToken::isUsableNow)
                .orElse(false);
    }

    @Transactional
    public boolean resetPassword(String tokenValue, String newPassword) {
        if (tokenValue == null || tokenValue.isBlank() || newPassword == null || newPassword.length() < 6) {
            return false;
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenValue);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOpt.get();
        if (!token.isUsableNow()) {
            return false;
        }

        AppUser user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
        return true;
    }

    private void sendResetEmail(AppUser user, String token) {
        if (mailSender == null) {
            log.info("JavaMailSender not configured, skipping password reset email for {}", user.getEmail());
            log.info("Manual reset link for development: {}/reset-password?token={}", appBaseUrl, token);
            return;
        }

        try {
            String link = appBaseUrl + "/reset-password?token=" + token;
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(user.getEmail());
            msg.setSubject("CatShelter - Salasanan vaihto");
            msg.setText("Pyysit salasanan vaihtoa.\n\nAvaa linkki (voimassa 30 min):\n" + link
                    + "\n\nJos et pyytänyt vaihtoa, voit jättää viestin huomiotta.");
            mailSender.send(msg);
        } catch (Exception ex) {
            log.warn("Password reset email failed for {}: {}", user.getEmail(), ex.getMessage());
        }
    }
}
