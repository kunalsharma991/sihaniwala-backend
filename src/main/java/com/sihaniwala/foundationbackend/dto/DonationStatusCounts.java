package com.sihaniwala.foundationbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Donation counts grouped by payment status. Field names are lower-case for
 * Java, but the JSON keys are the upper-case enum names so consumers can index
 * by status directly. Every key is always present (0 when no rows match).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationStatusCounts {
    @JsonProperty("PENDING")
    private long pending;

    @JsonProperty("SUCCESS")
    private long success;

    @JsonProperty("FAILED")
    private long failed;

    @JsonProperty("REFUNDED")
    private long refunded;
}
