package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.InitiativeApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InitiativeApplicationRepository extends JpaRepository<InitiativeApplication, Long> {
    List<InitiativeApplication> findByUserId(Long userId);
    List<InitiativeApplication> findByStatus(InitiativeApplication.ApplicationStatus status);
    List<InitiativeApplication> findByInitiativeType(InitiativeApplication.InitiativeType type);
    long countByStatus(InitiativeApplication.ApplicationStatus status);
}
