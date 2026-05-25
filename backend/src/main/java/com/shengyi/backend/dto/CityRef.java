package com.shengyi.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CityRef(
        @NotBlank(message = "城市编号不能为空") String cityNo,
        String cityName,
        String cityType
) {
}
