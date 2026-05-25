package com.shengyi.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TripRequest(
        @Valid @NotNull(message = "出行人不能为空")
        NamedRef traveler,

        @Valid @NotNull(message = "出发城市不能为空")
        CityRef departureCity,

        @Valid @NotNull(message = "到达城市不能为空")
        CityRef arrivalCity,

        @NotNull(message = "出发日期不能为空")
        LocalDate departureDate,

        @NotNull(message = "到达日期不能为空")
        LocalDate arrivalDate,

        @Size(max = 500, message = "行程说明不能超过500字")
        String description
) {
}
