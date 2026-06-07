package com.sihaniwala.foundationbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gallery")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GalleryImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String category;
    @Column(nullable = false)
    private String filePath;
    private String fileName;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
