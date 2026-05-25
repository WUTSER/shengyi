package com.shengyi.backend.dto;

import com.shengyi.backend.model.ReimbursementStatus;

public record ReimbursementQuery(
        String reimbursementNo,
        String title,
        String reason,
        String reimCompanyId,
        String reimDepartmentId,
        String reimburserId,
        String businessTypeId,
        ReimbursementStatus status,
        Integer pageNo,
        Integer pageSize
) {
}
