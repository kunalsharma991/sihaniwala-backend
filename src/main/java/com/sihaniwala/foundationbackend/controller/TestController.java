package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Temporary test controller for debugging email/SMTP configuration.
 * DELETE THIS FILE BEFORE PRODUCTION DEPLOYMENT.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final EmailService emailService;

    /**
     * Test donation receipt email.
     * POST /api/test/email
     * Body: { "email": "test@example.com" }
     */
    @PostMapping("/email")
    public ResponseEntity<?> testDonationEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
        }

        log.info("[TEST ENDPOINT] Testing donation receipt email to: {}", email);
        
        try {
            emailService.sendDonationReceipt(email, "Kunal Sharma", "1000", "INR");
            
            log.info("[TEST ENDPOINT] Donation receipt sent successfully to: {}", email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Donation receipt email sent successfully to " + email,
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("[TEST ENDPOINT] Failed to send email: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            
            String errorDetails = String.format(
                    "Exception: %s\nMessage: %s\nCause: %s",
                    e.getClass().getName(),
                    e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "null"
            );
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to send email",
                    "error", errorDetails,
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    /**
     * Test basic SMTP connectivity.
     * POST /api/test/smtp
     * Body: { "email": "test@example.com" }
     */
    @PostMapping("/smtp")
    public ResponseEntity<?> testSmtpConnection(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
        }

        log.info("[TEST ENDPOINT] Testing SMTP connection with: {}", email);
        
        try {
            emailService.sendTestEmail(email);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "SMTP test email sent successfully to " + email,
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("[TEST ENDPOINT] SMTP test failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            
            String errorType = e.getClass().getSimpleName();
            String suggestion = getSuggestion(e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "SMTP test failed",
                    "errorType", errorType,
                    "errorMessage", e.getMessage(),
                    "suggestion", suggestion,
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    private String getSuggestion(Exception e) {
        String className = e.getClass().getSimpleName();
        return switch (className) {
            case "MailAuthenticationException" -> 
                    "Check spring.mail.username and spring.mail.password in application.properties. " +
                    "For Gmail, use an App Password (not your regular password). " +
                    "Enable 2FA on your Google account and generate an App Password.";
            case "MailSendException" -> 
                    "Check SMTP host/port configuration. " +
                    "For Gmail: host=smtp.gmail.com, port=587 (TLS) or 465 (SSL).";
            case "MailConnectionException" -> 
                    "Cannot connect to mail server. Check if SMTP host is reachable. " +
                    "Verify firewall allows outbound connections on port 587.";
            default -> 
                    "Check application.properties mail configuration and server logs for details.";
        };
    }
}
