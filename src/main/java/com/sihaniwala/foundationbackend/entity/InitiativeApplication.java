package com.sihaniwala.foundationbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "initiative_applications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InitiativeApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitiativeType initiativeType;

    @Column(columnDefinition = "TEXT")
    private String formData;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public enum InitiativeType { HOSPITAL, MARRIAGE_SUPPORT, WATER_SPRAY, EDUCATION_BPL, FINANCIAL_HELP, SCHOOL_ADOPTION }
    public enum ApplicationStatus { PENDING, UNDER_REVIEW, APPROVED, REJECTED }
}
