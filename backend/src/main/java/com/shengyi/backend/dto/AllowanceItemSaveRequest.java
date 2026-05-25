package com.shengyi.backend.dto;

import java.math.BigDecimal;

public record AllowanceItemSaveRequest(
        boolean checked,
        BigDecimal amount
) {
}
