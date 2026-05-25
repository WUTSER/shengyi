package com.shengyi.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CopyTripRequest(
        @NotNull(message = "出发日期不能为空") LocalDate departureDate,
        @NotNull(message = "到达日期不能为空") LocalDate arrivalDate
) {
}
