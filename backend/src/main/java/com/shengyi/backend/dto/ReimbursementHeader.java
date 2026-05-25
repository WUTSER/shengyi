package com.shengyi.backend.dto;

import com.shengyi.backend.model.ReimbursementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReimbursementHeader(
        String reimbursementId,
        String reimbursementNo,
        LocalDate billDate,
        ReimbursementStatus status,
        String statusName,
        String title,
        String reason,
        String reimburserId,
        String reimburserNo,
        String reimburserName,
        String reimDepartmentId,
        String reimDepartmentNo,
        String reimDepartmentName,
        String reimCompanyId,
        String reimCompanyNo,
        String reimCompanyName,
        String businessTypeId,
        String businessTypeNo,
        String businessTypeName,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
