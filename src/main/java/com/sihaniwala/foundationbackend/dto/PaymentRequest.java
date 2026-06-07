package com.sihaniwala.foundationbackend.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentRequest {
    private BigDecimal amount;
    private String currency;
    private String initiative;
    private String donorName;
    private String donorEmail;
    private boolean anonymous;
    private boolean recurring;
}
