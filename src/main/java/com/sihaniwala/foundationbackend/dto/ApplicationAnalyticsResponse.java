package com.sihaniwala.foundationbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated, non-sensitive initiative-application analytics for the admin
 * dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationAnalyticsResponse {
    private ApplicationStatusCounts countByStatus;
    private List<ApplicationInitiativeSummary> byInitiativeType;
}
