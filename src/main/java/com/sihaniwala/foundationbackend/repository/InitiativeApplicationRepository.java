package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.InitiativeApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InitiativeApplicationRepository extends JpaRepository<InitiativeApplication, Long> {
    List<InitiativeApplication> findByUserId(Long userId);
    List<InitiativeApplication> findByStatus(InitiativeApplication.ApplicationStatus status);
    List<InitiativeApplication> findByInitiativeType(InitiativeApplication.InitiativeType type);
    long countByStatus(InitiativeApplication.ApplicationStatus status);

    /** Applications per initiative type. Row: [initiativeType, count]. */
    @Query("SELECT a.initiativeType, COUNT(a) FROM InitiativeApplication a "
            + "GROUP BY a.initiativeType ORDER BY a.initiativeType")
    List<Object[]> countGroupedByInitiativeType();
}
