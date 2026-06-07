package com.sihaniwala.foundationbackend.service;

import com.sihaniwala.foundationbackend.entity.InitiativeApplication;
import com.sihaniwala.foundationbackend.entity.InitiativeApplication.*;
import com.sihaniwala.foundationbackend.entity.User;
import com.sihaniwala.foundationbackend.exception.ResourceNotFoundException;
import com.sihaniwala.foundationbackend.repository.InitiativeApplicationRepository;
import com.sihaniwala.foundationbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitiativeService {

    private final InitiativeApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public InitiativeApplication submitApplication(InitiativeType type, String formData, String userEmail) {
        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        InitiativeApplication application = InitiativeApplication.builder()
                .user(user)
                .initiativeType(type)
                .formData(formData)
                .status(ApplicationStatus.PENDING)
                .build();

        application = applicationRepository.save(application);
        log.info("Application submitted: type={}, id={}", type, application.getId());

        if (user != null) {
            try {
                emailService.sendApplicationConfirmation(user.getEmail(), user.getName(), type.name());
            } catch (Exception ignored) {}
        }
        return application;
    }

    public List<InitiativeApplication> getUserApplications(Long userId) {
        return applicationRepository.findByUserId(userId);
    }

    public InitiativeApplication getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    public List<InitiativeApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<InitiativeApplication> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<InitiativeApplication> getApplicationsByType(InitiativeType type) {
        return applicationRepository.findByInitiativeType(type);
    }

    public InitiativeApplication updateApplicationStatus(Long id, ApplicationStatus status, String adminNotes) {
        InitiativeApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        application.setStatus(status);
        if (adminNotes != null) {
            application.setAdminNotes(adminNotes);
        }
        application = applicationRepository.save(application);

        if (application.getUser() != null) {
            try {
                emailService.sendApplicationStatusUpdate(
                        application.getUser().getEmail(),
                        application.getUser().getName(),
                        application.getInitiativeType().name(),
                        status.name()
                );
            } catch (Exception ignored) {}
        }
        return application;
    }
}
