package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.entity.ContactMessage;
import com.sihaniwala.foundationbackend.repository.ContactMessageRepository;
import com.sihaniwala.foundationbackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageRepository contactRepository;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessage>> submitContact(@RequestBody ContactMessage message) {
        message.setRead(false);
        ContactMessage saved = contactRepository.save(message);
        try {
            emailService.sendContactConfirmation(message.getEmail(), message.getName());
        } catch (Exception ignored) {}
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message sent successfully", saved));
    }
}
