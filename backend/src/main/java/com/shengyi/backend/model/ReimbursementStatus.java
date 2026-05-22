package com.shengyi.backend.model;

public enum ReimbursementStatus {
    DRAFT("未提交"),
    APPROVING("审批中"),
    APPROVED("审批通过"),
    COMPLETED("已完成"),
    VOIDED("已作废");

    private final String text;

    ReimbursementStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
