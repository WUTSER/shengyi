package com.shengyi.backend.dto;

import java.math.BigDecimal;

public record AllowanceItemResponse(boolean checked, BigDecimal standardAmount, BigDecimal amount) {
}
