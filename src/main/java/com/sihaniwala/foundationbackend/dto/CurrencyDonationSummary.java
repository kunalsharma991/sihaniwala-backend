package com.sihaniwala.foundationbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Monetary donation totals for a SINGLE currency. Currencies are never summed
 * together and no FX conversion is performed anywhere.
 *
 * <p>{@code successAmount} only counts SUCCESS donations.
 * {@code allAmount} is the total across every status (PENDING/SUCCESS/FAILED/
 * REFUNDED) and is explicitly documented so it is not mistaken for successful
 * revenue.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDonationSummary {
    private String currency;
    private long successCount;
    private BigDecimal successAmount;
    private BigDecimal allAmount;
}
