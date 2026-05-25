package com.shengyi.backend.dto;

import com.shengyi.backend.model.ReimbursementStatus;

public record ReimbursementCreatedResponse(
        String reimbursementId,
        String reimbursementNo,
        ReimbursementStatus status
) {
}
