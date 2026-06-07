package com.sihaniwala.foundationbackend.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStats {
    private long totalUsers;
    private long totalDonations;
    private long pendingApplications;
    private long approvedApplications;
    private long rejectedApplications;
    private long totalVolunteers;
    private long totalContacts;
}
