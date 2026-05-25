package com.shengyi.backend.dto;

import java.time.LocalDate;

public record TripResponse(
        String tripId,
        String reimbursementId,
        String travelerId,
        String travelerNo,
        String travelerName,
        String departureCityNo,
        String departureCityName,
        String arrivalCityNo,
        String arrivalCityName,
        LocalDate departureDate,
        LocalDate arrivalDate,
        long travelDays,
        String route,
        String description
) {
}
