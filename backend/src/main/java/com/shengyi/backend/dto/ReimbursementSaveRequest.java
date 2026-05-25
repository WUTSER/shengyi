package com.shengyi.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ReimbursementSaveRequest(
        LocalDate billDate,

        @NotBlank(message = "报销标题不能为空")
        @Size(max = 500, message = "报销标题不能超过500字")
        String title,

        @NotBlank(message = "出差事由不能为空")
        @Size(max = 500, message = "出差事由不能超过500字")
        String reason,

        @Valid @NotNull(message = "报销人不能为空")
        NamedRef reimburser,

        @Valid @NotNull(message = "报销部门不能为空")
        NamedRef reimDepartment,

        @Valid @NotNull(message = "费用归属公司不能为空")
        NamedRef reimCompany,

        @Valid @NotNull(message = "业务类型不能为空")
        NamedRef businessType,

        @Size(max = 1000, message = "备注不能超过1000字")
        String remark,

        @Valid
        List<AllocationItem> allocations
) {
}
