package com.shengyi.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CalendarSaveRequest(
        @Valid @NotNull(message = "补助日历不能为空") List<CalendarSaveItem> items
) {
}
