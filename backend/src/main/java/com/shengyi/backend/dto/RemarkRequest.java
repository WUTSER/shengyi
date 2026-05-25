package com.shengyi.backend.dto;

import jakarta.validation.constraints.Size;

public record RemarkRequest(
        @Size(max = 1000, message = "备注不能超过1000字") String remark
) {
}
