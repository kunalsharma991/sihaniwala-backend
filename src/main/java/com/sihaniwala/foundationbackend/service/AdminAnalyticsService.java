package com.sihaniwala.foundationbackend.service;

import com.sihaniwala.foundationbackend.dto.ApplicationAnalyticsResponse;
import com.sihaniwala.foundationbackend.dto.ApplicationInitiativeSummary;
import com.sihaniwala.foundationbackend.dto.ApplicationStatusCounts;
import com.sihaniwala.foundationbackend.dto.CurrencyDonationSummary;
import com.sihaniwala.foundationbackend.dto.DonationAnalyticsResponse;
import com.sihaniwala.foundationbackend.dto.DonationStatusCounts;
import com.sihaniwala.foundationbackend.dto.GatewayCounts;
import com.sihaniwala.foundationbackend.dto.InitiativeDonationSummary;
import com.sihaniwala.foundationbackend.dto.MonthlyDonationSummary;
import com.sihaniwala.foundationbackend.entity.Donation;
import com.sihaniwala.foundationbackend.entity.InitiativeApplication;
import com.sihaniwala.foundationbackend.repository.DonationRepository;
import com.sihaniwala.foundationbackend.repository.InitiativeApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only analytics assembled from database-side aggregation queries.
 *
 * <p>This service deliberately contains no business or payment logic: it only
 * turns grouped query results into typed, non-sensitive rollups. Monetary
 * values are always kept separated by currency (no FX conversion), every
 * enum/status key is always present (0 when unused), and list fields are never
 * null (empty when there is no data). No donor identity or payment identifier
 * is ever read or returned.</p>
 */
@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final DonationRepository donationRepository;
    private final InitiativeApplicationRepository applicationRepository;

    public DonationAnalyticsResponse donationAnalytics() {
        DonationStatusCounts statusCounts = DonationStatusCounts.builder()
                .pending(donationRepository.countByStatus(Donation.PaymentStatus.PENDING))
                .success(donationRepository.countByStatus(Donation.PaymentStatus.SUCCESS))
                .failed(donationRepository.countByStatus(Donation.PaymentStatus.FAILED))
                .refunded(donationRepository.countByStatus(Donation.PaymentStatus.REFUNDED))
                .build();

        GatewayCounts gatewayCounts = GatewayCounts.builder()
                .razorpay(donationRepository.countByPaymentGateway(Donation.PaymentGateway.RAZORPAY))
                .paypal(donationRepository.countByPaymentGateway(Donation.PaymentGateway.PAYPAL))
                .build();

        // Success metrics per currency.
        Map<String, Long> successCount = new LinkedHashMap<>();
        Map<String, BigDecimal> successAmount = new LinkedHashMap<>();
        for (Object[] row : donationRepository.successByCurrency(Donation.PaymentStatus.SUCCESS)) {
            String currency = String.valueOf(row[0]);
            successCount.put(currency, toLong(row[1]));
            successAmount.put(currency, toBigDecimal(row[2]));
        }

        // All-status amount per currency (kept separate; documented on the DTO).
        Map<String, BigDecimal> allAmount = new LinkedHashMap<>();
        for (Object[] row : donationRepository.totalByCurrency()) {
            allAmount.put(String.valueOf(row[0]), toBigDecimal(row[1]));
        }

        List<CurrencyDonationSummary> byCurrency = new ArrayList<>();
        java.util.TreeSet<String> currencies = new java.util.TreeSet<>();
        currencies.addAll(successCount.keySet());
        currencies.addAll(allAmount.keySet());
        for (String currency : currencies) {
            byCurrency.add(CurrencyDonationSummary.builder()
                    .currency(currency)
                    .successCount(successCount.getOrDefault(currency, 0L))
                    .successAmount(successAmount.getOrDefault(currency, BigDecimal.ZERO))
                    .allAmount(allAmount.getOrDefault(currency, BigDecimal.ZERO))
                    .build());
        }

        List<MonthlyDonationSummary> byMonth = new ArrayList<>();
        for (Object[] row : donationRepository.successByMonthCurrency()) {
            byMonth.add(MonthlyDonationSummary.builder()
                    .yearMonth(String.valueOf(row[0]))
                    .currency(String.valueOf(row[1]))
                    .count(toLong(row[2]))
                    .successAmount(toBigDecimal(row[3]))
                    .build());
        }

        List<InitiativeDonationSummary> byInitiative = new ArrayList<>();
        for (Object[] row : donationRepository.successByInitiative(Donation.PaymentStatus.SUCCESS)) {
            byInitiative.add(InitiativeDonationSummary.builder()
                    .initiative(String.valueOf(row[0]))
                    .count(toLong(row[1]))
                    .successAmount(toBigDecimal(row[2]))
                    .build());
        }

        return DonationAnalyticsResponse.builder()
                .countByStatus(statusCounts)
                .countByGateway(gatewayCounts)
                .byCurrency(byCurrency)
                .byMonth(byMonth)
                .byInitiative(byInitiative)
                .build();
    }

    public ApplicationAnalyticsResponse applicationAnalytics() {
        ApplicationStatusCounts statusCounts = ApplicationStatusCounts.builder()
                .pending(applicationRepository.countByStatus(InitiativeApplication.ApplicationStatus.PENDING))
                .underReview(applicationRepository.countByStatus(InitiativeApplication.ApplicationStatus.UNDER_REVIEW))
                .approved(applicationRepository.countByStatus(InitiativeApplication.ApplicationStatus.APPROVED))
                .rejected(applicationRepository.countByStatus(InitiativeApplication.ApplicationStatus.REJECTED))
                .build();

        List<ApplicationInitiativeSummary> byInitiativeType = new ArrayList<>();
        for (Object[] row : applicationRepository.countGroupedByInitiativeType()) {
            byInitiativeType.add(ApplicationInitiativeSummary.builder()
                    .type(String.valueOf(row[0]))
                    .count(toLong(row[1]))
                    .build());
        }

        return ApplicationAnalyticsResponse.builder()
                .countByStatus(statusCounts)
                .byInitiativeType(byInitiativeType)
                .build();
    }

    private static long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }
}
