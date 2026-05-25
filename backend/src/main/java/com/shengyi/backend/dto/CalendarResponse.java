package com.shengyi.backend.dto;

import java.time.LocalDate;

public record CalendarResponse(
        String calendarId,
        String allowanceId,
        LocalDate travelDate,
        String weekDay,
        String allowanceCityNo,
        String allowanceCityName,
        AllowanceItemResponse meal,
        AllowanceItemResponse traffic,
        AllowanceItemResponse communication
) {
}
