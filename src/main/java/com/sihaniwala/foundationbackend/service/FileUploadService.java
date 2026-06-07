package com.sihaniwala.foundationbackend.service;

import com.sihaniwala.foundationbackend.entity.InitiativeApplication;
import com.sihaniwala.foundationbackend.entity.UploadedDocument;
import com.sihaniwala.foundationbackend.entity.User;
import com.sihaniwala.foundationbackend.exception.BadRequestException;
import com.sihaniwala.foundationbackend.exception.ResourceNotFoundException;
import com.sihaniwala.foundationbackend.repository.UploadedDocumentRepository;
import com.sihaniwala.foundationbackend.repository.InitiativeApplicationRepository;
import com.sihaniwala.foundationbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final UploadedDocumentRepository documentRepository;
    private final InitiativeApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    private Path rootPath;

    @PostConstruct
    public void init() {
        rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath.resolve("documents"));
            Files.createDirectories(rootPath.resolve("gallery"));
            Files.createDirectories(rootPath.resolve("profile"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    public UploadedDocument uploadFile(MultipartFile file, String subDir, String userEmail, Long applicationId) {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + extension;

        String dir = subDir != null ? subDir : "documents";
        Path targetDir = rootPath.resolve(dir).normalize();
        Path targetPath = targetDir.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }

        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        InitiativeApplication application = null;
        if (applicationId != null) {
            application = applicationRepository.findById(applicationId).orElse(null);
        }

        UploadedDocument doc = UploadedDocument.builder()
                .originalName(originalName)
                .storedName(storedName)
                .filePath(dir + "/" + storedName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .user(user)
                .application(application)
                .build();

        doc = documentRepository.save(doc);
        log.info("File uploaded: {} -> {}", originalName, storedName);
        return doc;
    }

    public Resource downloadFile(String filePath) {
        try {
            Path file = rootPath.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new ResourceNotFoundException("File not found: " + filePath);
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + filePath);
        }
    }

    public void deleteFile(Long id) {
        UploadedDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File record not found"));
        try {
            Path file = rootPath.resolve(doc.getFilePath()).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Failed to delete physical file: {}", e.getMessage());
        }
        documentRepository.delete(doc);
    }

    public List<UploadedDocument> getAllFiles() {
        return documentRepository.findAll();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf")
                && !contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("image/jpg"))) {
            throw new BadRequestException("Only PDF, JPG, and PNG files are allowed");
        }
    }
}
