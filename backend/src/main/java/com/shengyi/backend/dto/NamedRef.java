package com.shengyi.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record NamedRef(
        @NotBlank(message = "ID不能为空") String id,
        String code,
        String name
) {
}
