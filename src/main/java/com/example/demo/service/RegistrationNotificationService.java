package com.example.demo.service;

import com.example.demo.entity.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class RegistrationNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.admin.email:admin@catshelter.local}")
    private String adminEmail;

    @Value("${spring.mail.username:no-reply@catshelter.local}")
    private String fromEmail;

    public RegistrationNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void notifyAdminNewUser(AppUser user) {
        if (mailSender == null) {
            log.info("JavaMailSender not configured, skipping admin notification for user {}", user.getUsername());
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(adminEmail);
            msg.setSubject("Uusi käyttäjä rekisteröityi: " + user.getUsername());
            msg.setText("Uusi käyttäjä on rekisteröitynyt CatShelteriin.\n\n"
                    + "Käyttäjänimi: " + user.getUsername() + "\n"
                    + "Sähköposti: " + user.getEmail() + "\n"
                    + "Nimi: " + user.getFirstName() + " " + user.getLastName());
            mailSender.send(msg);
        } catch (Exception ex) {
            log.warn("Admin-notification email failed: {}", ex.getMessage());
        }
    }
}
