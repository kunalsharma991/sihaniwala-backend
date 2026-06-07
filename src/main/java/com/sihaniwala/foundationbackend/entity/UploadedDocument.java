package com.sihaniwala.foundationbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_documents")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UploadedDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String originalName;
    @Column(nullable = false)
    private String storedName;
    @Column(nullable = false)
    private String filePath;
    private String contentType;
    private Long fileSize;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private InitiativeApplication application;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
