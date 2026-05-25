package com.shengyi.backend.dto;

import java.math.BigDecimal;

public record AllowanceResponse(
        String allowanceId,
        String reimbursementId,
        String tripId,
        String travelerId,
        String travelerName,
        String travelDateRange,
        long allowanceDays,
        String route,
        String allowanceCityNo,
        String allowanceCityName,
        BigDecimal standardAmount,
        BigDecimal applyAmount,
        BigDecimal allowanceAmount
) {
}
