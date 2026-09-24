package com.sihaniwala.foundationbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Successful donation totals grouped by the donation's {@code initiative}
 * label. Monetary values are aggregated per initiative regardless of currency,
 * so a caller that needs currency-exact amounts should use the currency
 * breakdown; this grouping is intended for relative initiative distribution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiativeDonationSummary {
    private String initiative;
    private BigDecimal successAmount;
    private long count;
}
