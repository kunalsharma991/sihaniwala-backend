package com.sihaniwala.foundationbackend.service;

import com.sihaniwala.foundationbackend.dto.DashboardStats;
import com.sihaniwala.foundationbackend.entity.*;
import com.sihaniwala.foundationbackend.exception.BadRequestException;
import com.sihaniwala.foundationbackend.exception.ResourceNotFoundException;
import com.sihaniwala.foundationbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final InitiativeApplicationRepository applicationRepository;
    private final VolunteerRepository volunteerRepository;
    private final ContactMessageRepository contactRepository;
    private final GalleryImageRepository galleryRepository;
    private final ProjectRepository projectRepository;

    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalUsers(userRepository.count())
                .totalDonations(donationRepository.findByStatus(Donation.PaymentStatus.SUCCESS).size())
                .pendingApplications(applicationRepository.countByStatus(InitiativeApplication.ApplicationStatus.PENDING))
                .approvedApplications(applicationRepository.countByStatus(InitiativeApplication.ApplicationStatus.APPROVED))
                .rejectedApplications(applicationRepository.countByStatus(InitiativeApplication.ApplicationStatus.REJECTED))
                .totalVolunteers(volunteerRepository.count())
                .totalContacts(contactRepository.count())
                .build();
    }

    // User Management
    public List<User> getAllUsers() { return userRepository.findAll(); }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    public void deleteUser(Long id, String authenticatedEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new BadRequestException("You cannot delete your own admin account");
        }
        if (user.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.findAll().stream()
                    .filter(existing -> existing.getRole() == User.Role.ADMIN)
                    .count();
            if (adminCount <= 1) {
                throw new BadRequestException("The last admin account cannot be deleted");
            }
        }
        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("User cannot be deleted because related records prevent deletion");
        }
    }

    // Donation Management
    public List<Donation> getAllDonations() { return donationRepository.findAll(); }

    public void deleteDonation(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
        donationRepository.delete(donation);
    }

    // Application Management
    public List<InitiativeApplication> getAllApplications() { return applicationRepository.findAll(); }

    // Volunteer Management
    public List<Volunteer> getAllVolunteers() { return volunteerRepository.findAll(); }

    public void deleteVolunteer(Long id) {
        if (!volunteerRepository.existsById(id))
            throw new ResourceNotFoundException("Volunteer not found");
        volunteerRepository.deleteById(id);
    }

    // Contact Management
    public List<ContactMessage> getAllContacts() { return contactRepository.findAll(); }

    public ContactMessage markContactRead(Long id) {
        ContactMessage msg = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        msg.setRead(true);
        return contactRepository.save(msg);
    }

    public void deleteContact(Long id) {
        if (!contactRepository.existsById(id))
            throw new ResourceNotFoundException("Contact message not found");
        contactRepository.deleteById(id);
    }

    // Gallery Management
    public List<GalleryImage> getAllGalleryImages() { return galleryRepository.findAll(); }

    public GalleryImage getGalleryImageById(Long id) {
        return galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery image not found"));
    }

    public GalleryImage saveGalleryImage(GalleryImage image) { return galleryRepository.save(image); }

    public void deleteGalleryImage(Long id) {
        if (!galleryRepository.existsById(id))
            throw new ResourceNotFoundException("Gallery image not found");
        galleryRepository.deleteById(id);
    }

    // Project Management
    public List<Project> getAllProjects() { return projectRepository.findAll(); }

    public Project saveProject(Project project) { return projectRepository.save(project); }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id))
            throw new ResourceNotFoundException("Project not found");
        projectRepository.deleteById(id);
    }
}
