package com.sihaniwala.foundationbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "volunteers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    private String phone;
    private String city;
    private String interest;
    @Column(columnDefinition = "TEXT")
    private String message;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
