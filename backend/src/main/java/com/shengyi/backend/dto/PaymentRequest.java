package com.shengyi.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PaymentRequest(
        @NotNull(message = "放款时间不能为空") LocalDateTime paymentTime,
        @NotBlank(message = "放款流水号不能为空") String paymentNo
) {
}
