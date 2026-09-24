package com.sihaniwala.foundationbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Initiative application count for a single {@code InitiativeType}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationInitiativeSummary {
    private String type;
    private long count;
}
