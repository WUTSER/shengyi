package com.shengyi.backend.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReimbursementStatus {
    DRAFT("0", "草稿"),
    COMPLETED("1", "已完成"),
    VOIDED("2", "已作废");

    private final String code;
    private final String text;

    ReimbursementStatus(String code, String text) {
        this.code = code;
        this.text = text;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static ReimbursementStatus fromStorage(String value) {
        if (value == null || value.isBlank()) {
            return DRAFT;
        }
        return switch (value) {
            case "0", "DRAFT" -> DRAFT;
            case "1", "COMPLETED", "APPROVING", "APPROVED" -> COMPLETED;
            case "2", "VOIDED" -> VOIDED;
            default -> throw new IllegalArgumentException("Unknown reimbursement status: " + value);
        };
    }
}
