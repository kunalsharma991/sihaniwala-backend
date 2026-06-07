package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.entity.UploadedDocument;
import com.sihaniwala.foundationbackend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<UploadedDocument>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subDir", required = false, defaultValue = "documents") String subDir,
            @RequestParam(value = "applicationId", required = false) Long applicationId,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        UploadedDocument doc = fileUploadService.uploadFile(file, subDir, email, applicationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("File uploaded", doc));
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("path") String filePath) {
        Resource resource = fileUploadService.downloadFile(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        fileUploadService.deleteFile(id);
        return ResponseEntity.ok(ApiResponse.ok("File deleted", null));
    }
}
