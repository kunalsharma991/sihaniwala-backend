package com.sihaniwala.foundationbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Donation counts grouped by payment gateway. JSON keys mirror the
 * {@code PaymentGateway} enum names; both keys are always present.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayCounts {
    @JsonProperty("RAZORPAY")
    private long razorpay;

    @JsonProperty("PAYPAL")
    private long paypal;
}
