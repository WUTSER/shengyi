package com.shengyi.backend.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReimbursementStatus {
    DRAFT("0", "草稿"),
    APPROVING("1", "审批中"),
    COMPLETED("2", "已完成"),
    VOIDED("3", "已作废");

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
            case "1", "APPROVING" -> APPROVING;
            case "2", "COMPLETED", "APPROVED" -> COMPLETED;
            case "3", "VOIDED" -> VOIDED;
            default -> throw new IllegalArgumentException("Unknown reimbursement status: " + value);
        };
    }
}
