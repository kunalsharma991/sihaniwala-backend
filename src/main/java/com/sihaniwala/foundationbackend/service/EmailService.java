package com.sihaniwala.foundationbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendRegistrationEmail(String to, String name) {
        sendEmail(to, "Welcome to Sihaniwala Foundation",
                "Dear " + name + ",\n\nThank you for registering with Sihaniwala Foundation. " +
                "Your account has been created successfully.\n\nTogether, we can make a difference!\n\n" +
                "Warm regards,\nSihaniwala Foundation Team");
    }

    public void sendDonationReceipt(String to, String name, String amount, String currency) {
        sendEmail(to, "Donation Receipt - Sihaniwala Foundation",
                "Dear " + name + ",\n\nThank you for your generous donation of " + currency + " " + amount + ". " +
                "Your contribution helps us make a real difference in people's lives.\n\n" +
                "This email serves as your donation receipt.\n\n" +
                "With gratitude,\nSihaniwala Foundation Team");
    }

    public void sendApplicationConfirmation(String to, String name, String initiative) {
        sendEmail(to, "Application Received - " + initiative,
                "Dear " + name + ",\n\nWe have received your application for " + initiative + ". " +
                "Our team will review it and get back to you soon.\n\n" +
                "Thank you for trusting Sihaniwala Foundation.\n\n" +
                "Regards,\nSihaniwala Foundation Team");
    }

    public void sendApplicationStatusUpdate(String to, String name, String initiative, String status) {
        sendEmail(to, "Application Update - " + initiative,
                "Dear " + name + ",\n\nYour application for " + initiative +
                " has been updated to: " + status + ".\n\n" +
                "Please login to your dashboard for more details.\n\n" +
                "Regards,\nSihaniwala Foundation Team");
    }

    public void sendPasswordResetEmail(String to, String name, String resetToken) {
        sendEmail(to, "Password Reset - Sihaniwala Foundation",
                "Dear " + name + ",\n\nYou requested a password reset. " +
                "Use the following token to reset your password: " + resetToken + "\n\n" +
                "This token will expire in 24 hours.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Regards,\nSihaniwala Foundation Team");
    }

    public void sendContactConfirmation(String to, String name) {
        sendEmail(to, "We received your message - Sihaniwala Foundation",
                "Dear " + name + ",\n\nThank you for reaching out to us. " +
                "We have received your message and will respond within 24-48 hours.\n\n" +
                "Regards,\nSihaniwala Foundation Team");
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
