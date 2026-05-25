package com.shengyi.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record VoidRequest(
        @NotBlank(message = "作废原因不能为空") String reason
) {
}
