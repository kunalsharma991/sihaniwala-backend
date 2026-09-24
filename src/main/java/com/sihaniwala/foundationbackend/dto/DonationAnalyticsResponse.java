package com.sihaniwala.foundationbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated, non-sensitive donation analytics for the admin dashboard.
 * Contains only counts, per-currency monetary totals, month buckets, gateway
 * counts and initiative labels. It never includes donor identity or any payment
 * identifier (paymentId / orderId / paymentSignature).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationAnalyticsResponse {
    private DonationStatusCounts countByStatus;
    private GatewayCounts countByGateway;
    private List<CurrencyDonationSummary> byCurrency;
    private List<MonthlyDonationSummary> byMonth;
    private List<InitiativeDonationSummary> byInitiative;
}
