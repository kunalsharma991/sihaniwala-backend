package com.sihaniwala.foundationbackend.config;

import com.sihaniwala.foundationbackend.entity.GalleryImage;
import com.sihaniwala.foundationbackend.entity.Project;
import com.sihaniwala.foundationbackend.entity.User;
import com.sihaniwala.foundationbackend.repository.GalleryImageRepository;
import com.sihaniwala.foundationbackend.repository.ProjectRepository;
import com.sihaniwala.foundationbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds essential data on startup.
 *
 * IMPORTANT: schema.sql's old "seed data" INSERT statements never actually ran,
 * because Spring Boot only auto-executes schema.sql/data.sql against EMBEDDED
 * databases (H2 etc.) by default, not against an external PostgreSQL instance.
 * That's why the admin account never existed in the database and login always
 * failed with "Invalid email or password" - it wasn't a wrong-password issue,
 * the account simply didn't exist.
 *
 * This runner replaces that broken approach. It is idempotent (checks before
 * inserting), so it is safe to leave enabled permanently - it will never
 * duplicate or wipe data on restart/redeploy, unlike schema.sql's DROP TABLE
 * statements would have if they had ever been allowed to run.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final GalleryImageRepository galleryImageRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.email:admin@sihaniwala.org}")
    private String adminEmail;

    @Value("${admin.seed.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedSampleProjects();
        seedSampleGallery();
    }

    private void seedAdminUser() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = User.builder()
                .name("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phone("+91-9876543210")
                .role(User.Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        log.warn("==============================================================");
        log.warn(" Seeded default ADMIN account:");
        log.warn("   email:    {}", adminEmail);
        log.warn("   password: {}", adminPassword);
        log.warn(" CHANGE THIS PASSWORD IMMEDIATELY after first login in production,");
        log.warn(" or set ADMIN_SEED_EMAIL / ADMIN_SEED_PASSWORD env vars before");
        log.warn(" the first deploy so this default is never used at all.");
        log.warn("==============================================================");
    }

    private void seedSampleProjects() {
        if (projectRepository.count() > 0) {
            return;
        }
        projectRepository.save(Project.builder()
                .title("Village School Renovation")
                .description("Complete renovation of primary school building with modern facilities")
                .location("Rajasthan, India")
                .status("ACTIVE")
                .beneficiaries("250")
                .build());
        projectRepository.save(Project.builder()
                .title("Clean Water Initiative")
                .description("Installing water purification systems in 10 villages")
                .location("Gujarat, India")
                .status("ACTIVE")
                .beneficiaries("5000")
                .build());
        projectRepository.save(Project.builder()
                .title("Women Empowerment Center")
                .description("Skill development center for underprivileged women")
                .location("Madhya Pradesh, India")
                .status("COMPLETED")
                .beneficiaries("150")
                .build());
        projectRepository.save(Project.builder()
                .title("Mobile Health Clinic")
                .description("Healthcare delivery to remote villages via mobile clinic vans")
                .location("Uttar Pradesh, India")
                .status("ACTIVE")
                .beneficiaries("10000")
                .build());
        log.info("Seeded {} sample projects", 4);
    }

    private void seedSampleGallery() {
        if (galleryImageRepository.count() > 0) {
            return;
        }
        galleryImageRepository.save(GalleryImage.builder()
                .title("School Opening Ceremony")
                .category("education")
                .filePath("gallery/school-opening.jpg")
                .fileName("school-opening.jpg")
                .build());
        galleryImageRepository.save(GalleryImage.builder()
                .title("Water Well Installation")
                .category("water")
                .filePath("gallery/water-well.jpg")
                .fileName("water-well.jpg")
                .build());
        galleryImageRepository.save(GalleryImage.builder()
                .title("Medical Camp")
                .category("health")
                .filePath("gallery/medical-camp.jpg")
                .fileName("medical-camp.jpg")
                .build());
        log.info("Seeded {} sample gallery entries", 3);
    }
}
