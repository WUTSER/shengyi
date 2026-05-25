package com.shengyi.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CalendarSaveItem(
        @NotBlank(message = "补助日历ID不能为空") String calendarId,
        @Valid @NotNull(message = "餐费补助不能为空") AllowanceItemSaveRequest meal,
        @Valid @NotNull(message = "交通补助不能为空") AllowanceItemSaveRequest traffic,
        @Valid @NotNull(message = "通讯补助不能为空") AllowanceItemSaveRequest communication
) {
}
