package com.shengyi.backend.dto;

import java.math.BigDecimal;

public record ExpenseTotalsResponse(
        BigDecimal totalAllowanceAmount,
        BigDecimal mealAllowanceAmount,
        BigDecimal trafficAllowanceAmount,
        BigDecimal communicationAllowanceAmount
) {
}
