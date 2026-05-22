package com.shengyi.backend.dto;

import com.shengyi.backend.model.ReimbursementStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {

    private Dtos() {
    }

    public record ReimbursementQuery(
            String reimbursementNo,
            String title,
            String reason,
            String reimCompanyId,
            String reimDepartmentId,
            String reimburserId,
            String businessTypeId,
            ReimbursementStatus status,
            Integer pageNo,
            Integer pageSize
    ) {
    }

    public record ReimbursementSaveRequest(
            @NotNull(message = "单据日期不能为空")
            LocalDate billDate,

            @NotBlank(message = "报销标题不能为空")
            @Size(max = 500, message = "报销标题不能超过500字")
            String title,

            @NotBlank(message = "出差事由不能为空")
            @Size(max = 500, message = "出差事由不能超过500字")
            String reason,

            @NotBlank(message = "报销人不能为空")
            String reimburserId,

            @NotBlank(message = "报销部门不能为空")
            String reimDepartmentId,

            @NotBlank(message = "费用归属公司不能为空")
            String reimCompanyId,

            @NotBlank(message = "业务类型不能为空")
            String businessTypeId,

            @Size(max = 1000, message = "备注不能超过1000字")
            String remark
    ) {
    }

    public record ReimbursementCreatedResponse(
            String reimbursementId,
            String reimbursementNo,
            ReimbursementStatus status
    ) {
    }

    public record ReimbursementListItem(
            String reimbursementId,
            String reimbursementNo,
            ReimbursementStatus status,
            String statusName,
            String reimburserId,
            String reimburserNo,
            String reimburserName,
            String reimDepartmentId,
            String reimDepartmentNo,
            String reimDepartmentName,
            String reimCompanyId,
            String reimCompanyName,
            String businessTypeId,
            String businessTypeName,
            String title,
            String reason,
            BigDecimal allowanceAmount,
            LocalDateTime createdAt,
            List<String> availableActions
    ) {
    }

    public record ReimbursementDetailResponse(
            ReimbursementHeader header,
            List<TripResponse> trips,
            List<AllowanceResponse> allowances,
            ExpenseTotalsResponse totals,
            List<String> availableActions
    ) {
    }

    public record ReimbursementHeader(
            String reimbursementId,
            String reimbursementNo,
            LocalDate billDate,
            ReimbursementStatus status,
            String statusName,
            String title,
            String reason,
            String reimburserId,
            String reimburserNo,
            String reimburserName,
            String reimDepartmentId,
            String reimDepartmentNo,
            String reimDepartmentName,
            String reimCompanyId,
            String reimCompanyNo,
            String reimCompanyName,
            String businessTypeId,
            String businessTypeNo,
            String businessTypeName,
            String remark,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record TripRequest(
            @NotBlank(message = "出行人不能为空")
            String travelerId,

            @NotBlank(message = "出发城市不能为空")
            String departureCityNo,

            @NotBlank(message = "到达城市不能为空")
            String arrivalCityNo,

            @NotNull(message = "出发日期不能为空")
            LocalDate departureDate,

            @NotNull(message = "到达日期不能为空")
            LocalDate arrivalDate,

            @NotBlank(message = "行程说明不能为空")
            @Size(max = 500, message = "行程说明不能超过500字")
            String description
    ) {
    }

    public record CopyTripRequest(
            @NotNull(message = "出发日期不能为空")
            LocalDate departureDate,

            @NotNull(message = "到达日期不能为空")
            LocalDate arrivalDate
    ) {
    }

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

    public record AllowanceResponse(
            String allowanceId,
            String reimbursementId,
            String tripId,
            String travelerId,
            String travelerName,
            String travelDateRange,
            long allowanceDays,
            String route,
            String allowanceCityNo,
            String allowanceCityName,
            BigDecimal standardAmount,
            BigDecimal applyAmount,
            BigDecimal allowanceAmount
    ) {
    }

    public record CalendarResponse(
            String calendarId,
            LocalDate travelDate,
            String weekDay,
            String allowanceCityNo,
            String allowanceCityName,
            AllowanceItemResponse meal,
            AllowanceItemResponse traffic,
            AllowanceItemResponse communication
    ) {
    }

    public record AllowanceItemResponse(boolean checked, BigDecimal standardAmount, BigDecimal amount) {
    }

    public record CalendarSaveRequest(
            @Valid
            @NotNull(message = "补助日历不能为空")
            List<CalendarSaveItem> items
    ) {
    }

    public record CalendarSaveItem(
            @NotBlank(message = "补助日历ID不能为空")
            String calendarId,

            @Valid
            @NotNull(message = "餐费补助不能为空")
            AllowanceItemSaveRequest meal,

            @Valid
            @NotNull(message = "交通补助不能为空")
            AllowanceItemSaveRequest traffic,

            @Valid
            @NotNull(message = "通讯补助不能为空")
            AllowanceItemSaveRequest communication
    ) {
    }

    public record AllowanceItemSaveRequest(
            boolean checked,

            @NotNull(message = "金额不能为空")
            BigDecimal amount
    ) {
    }

    public record ExpenseTotalsResponse(
            BigDecimal totalAllowanceAmount,
            BigDecimal mealAllowanceAmount,
            BigDecimal trafficAllowanceAmount,
            BigDecimal communicationAllowanceAmount
    ) {
    }

    public record RemarkRequest(
            @Size(max = 1000, message = "备注不能超过1000字")
            String remark
    ) {
    }

    public record VoidRequest(
            @NotBlank(message = "作废原因不能为空")
            String reason
    ) {
    }

    public record PaymentRequest(
            @NotNull(message = "放款时间不能为空")
            LocalDateTime paymentTime,

            @NotBlank(message = "放款流水号不能为空")
            String paymentNo
    ) {
    }
}
