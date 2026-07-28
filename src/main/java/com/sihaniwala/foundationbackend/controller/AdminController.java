package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.dto.DashboardStats;
import com.sihaniwala.foundationbackend.entity.*;
import com.sihaniwala.foundationbackend.entity.InitiativeApplication.ApplicationStatus;
import com.sihaniwala.foundationbackend.service.AdminService;
import com.sihaniwala.foundationbackend.service.InitiativeService;
import com.sihaniwala.foundationbackend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final InitiativeService initiativeService;
    private final FileUploadService fileUploadService;

    // Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    // Applications
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<InitiativeApplication>>> getApplications(
            @RequestParam(required = false) String status) {
        List<InitiativeApplication> apps;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            try {
                apps = initiativeService.getApplicationsByStatus(ApplicationStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                apps = adminService.getAllApplications();
            }
        } else {
            apps = adminService.getAllApplications();
        }
        return ResponseEntity.ok(ApiResponse.ok(apps));
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ApiResponse<InitiativeApplication>> updateApplicationStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        ApplicationStatus status = ApplicationStatus.valueOf(body.get("status"));
        String notes = body.get("adminNotes");
        InitiativeApplication app = initiativeService.updateApplicationStatus(id, status, notes);
        return ResponseEntity.ok(ApiResponse.ok("Application updated", app));
    }

    // Users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<ApiResponse<User>> toggleUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User status updated", adminService.toggleUserStatus(id)));
    }

    // Donations
    @GetMapping("/donations")
    public ResponseEntity<ApiResponse<List<Donation>>> getDonations() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllDonations()));
    }

    // Volunteers
    @GetMapping("/volunteers")
    public ResponseEntity<ApiResponse<List<Volunteer>>> getVolunteers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllVolunteers()));
    }

    @DeleteMapping("/volunteers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVolunteer(@PathVariable Long id) {
        adminService.deleteVolunteer(id);
        return ResponseEntity.ok(ApiResponse.ok("Volunteer removed", null));
    }

    // Contacts
    @GetMapping("/contacts")
    public ResponseEntity<ApiResponse<List<ContactMessage>>> getContacts() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllContacts()));
    }

    @PutMapping("/contacts/{id}/read")
    public ResponseEntity<ApiResponse<ContactMessage>> markContactRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.markContactRead(id)));
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id) {
        adminService.deleteContact(id);
        return ResponseEntity.ok(ApiResponse.ok("Contact deleted", null));
    }

    // Gallery
    @GetMapping("/gallery")
    public ResponseEntity<ApiResponse<List<GalleryImage>>> getGallery() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllGalleryImages()));
    }

    @PostMapping("/gallery")
    public ResponseEntity<ApiResponse<GalleryImage>> addGalleryImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category) {
        var doc = fileUploadService.uploadFile(file, "gallery", null, null);
        GalleryImage image = GalleryImage.builder()
                .title(title != null ? title : doc.getOriginalName())
                .category(category)
                .filePath(doc.getFilePath())
                .fileName(doc.getStoredName())
                .build();
        image = adminService.saveGalleryImage(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Image uploaded", image));
    }

    @DeleteMapping("/gallery/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGalleryImage(@PathVariable Long id) {
        adminService.deleteGalleryImage(id);
        return ResponseEntity.ok(ApiResponse.ok("Image deleted", null));
    }

    // Projects
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<Project>>> getProjects() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllProjects()));
    }

    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<Project>> createProject(@RequestBody Project project) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project created", adminService.saveProject(project)));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<Project>> updateProject(@PathVariable Long id, @RequestBody Project project) {
        Project existing = adminService.getProjectById(id);
        existing.setTitle(project.getTitle());
        existing.setDescription(project.getDescription());
        existing.setLocation(project.getLocation());
        existing.setStatus(project.getStatus());
        existing.setBeneficiaries(project.getBeneficiaries());
        return ResponseEntity.ok(ApiResponse.ok("Project updated", adminService.saveProject(existing)));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        adminService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.ok("Project deleted", null));
    }
}
