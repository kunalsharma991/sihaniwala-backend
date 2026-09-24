package com.sihaniwala.foundationbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Successful donation totals for one calendar month within a single currency
 * (yearMonth is formatted as {@code YYYY-MM}). Different currencies for the
 * same month are returned as separate rows.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyDonationSummary {
    private String yearMonth;
    private String currency;
    private BigDecimal successAmount;
    private long count;
}
