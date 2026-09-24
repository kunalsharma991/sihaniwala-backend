package com.sihaniwala.foundationbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Initiative application counts grouped by status. All four keys are always
 * present, including UNDER_REVIEW (which the legacy dashboard omitted).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusCounts {
    @JsonProperty("PENDING")
    private long pending;

    @JsonProperty("UNDER_REVIEW")
    private long underReview;

    @JsonProperty("APPROVED")
    private long approved;

    @JsonProperty("REJECTED")
    private long rejected;
}
