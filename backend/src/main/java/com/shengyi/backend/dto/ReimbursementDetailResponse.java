package com.shengyi.backend.dto;

import java.util.List;

public record ReimbursementDetailResponse(
        ReimbursementHeader header,
        List<TripResponse> trips,
        List<AllowanceResponse> allowances,
        List<AllocationResponse> allocations,
        ExpenseTotalsResponse totals,
        List<String> availableActions
) {
}
