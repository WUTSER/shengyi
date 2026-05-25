package com.shengyi.backend.dto;

import jakarta.validation.Valid;

import java.math.BigDecimal;

public record AllocationItem(
        Integer rowIndex,
        @Valid NamedRef reimCompany,
        @Valid NamedRef project,
        BigDecimal allocationRatio,
        BigDecimal allocationAmount
) {
}
