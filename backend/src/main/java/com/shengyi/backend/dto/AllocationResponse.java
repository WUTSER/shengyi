package com.shengyi.backend.dto;

import java.math.BigDecimal;

public record AllocationResponse(
        String allocationId,
        Integer rowIndex,
        String reimCompanyId,
        String reimCompanyNo,
        String reimCompanyName,
        String projectId,
        String projectNo,
        String projectName,
        BigDecimal allocationRatio,
        BigDecimal allocationAmount
) {
}
