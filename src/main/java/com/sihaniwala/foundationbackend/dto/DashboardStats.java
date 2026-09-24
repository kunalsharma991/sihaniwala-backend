package com.sihaniwala.foundationbackend.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStats {
    private long totalUsers;
    /**
     * Count of SUCCESS donations only (NOT every donation ever created). The
     * frontend labels this "Successful Donations". Kept under the same field
     * name for backward compatibility with the existing dashboard.
     */
    private long totalDonations;
    private long pendingApplications;
    private long underReviewApplications;
    private long approvedApplications;
    private long rejectedApplications;
    private long totalVolunteers;
    private long totalContacts;
    private long totalProjects;
}
