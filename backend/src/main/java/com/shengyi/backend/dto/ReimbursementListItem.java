package com.shengyi.backend.dto;

import com.shengyi.backend.model.ReimbursementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReimbursementListItem(
        String reimbursementId,
        String reimbursementNo,
        ReimbursementStatus status,
        String statusName,
        String reimburserId,
        String reimburserNo,
        String reimburserName,
        String reimDepartmentId,
        String reimDepartmentNo,
        String reimDepartmentName,
        String reimCompanyId,
        String reimCompanyName,
        String businessTypeId,
        String businessTypeName,
        String title,
        String reason,
        BigDecimal allowanceAmount,
        LocalDateTime createdAt,
        List<String> availableActions
) {
}
