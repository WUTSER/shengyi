package com.shengyi.backend.controller;

import com.shengyi.backend.common.ApiResponse;
import com.shengyi.backend.common.PageResponse;
import com.shengyi.backend.dto.Dtos.AllowanceResponse;
import com.shengyi.backend.dto.Dtos.CalendarResponse;
import com.shengyi.backend.dto.Dtos.CalendarSaveRequest;
import com.shengyi.backend.dto.Dtos.CopyTripRequest;
import com.shengyi.backend.dto.Dtos.ExpenseTotalsResponse;
import com.shengyi.backend.dto.Dtos.PaymentRequest;
import com.shengyi.backend.dto.Dtos.ReimbursementCreatedResponse;
import com.shengyi.backend.dto.Dtos.ReimbursementDetailResponse;
import com.shengyi.backend.dto.Dtos.ReimbursementListItem;
import com.shengyi.backend.dto.Dtos.ReimbursementQuery;
import com.shengyi.backend.dto.Dtos.ReimbursementSaveRequest;
import com.shengyi.backend.dto.Dtos.RemarkRequest;
import com.shengyi.backend.dto.Dtos.TripRequest;
import com.shengyi.backend.dto.Dtos.TripResponse;
import com.shengyi.backend.dto.Dtos.VoidRequest;
import com.shengyi.backend.model.ReimbursementStatus;
import com.shengyi.backend.service.ReimbursementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/travel-reimbursements")
public class ReimbursementController {

    private final ReimbursementService reimbursementService;

    public ReimbursementController(ReimbursementService reimbursementService) {
        this.reimbursementService = reimbursementService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ReimbursementListItem>> list(
            @RequestParam(required = false) String reimbursementNo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String reimCompanyId,
            @RequestParam(required = false) String reimDepartmentId,
            @RequestParam(required = false) String reimburserId,
            @RequestParam(required = false) String businessTypeId,
            @RequestParam(required = false) ReimbursementStatus status,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        ReimbursementQuery query = new ReimbursementQuery(
                reimbursementNo,
                title,
                reason,
                reimCompanyId,
                reimDepartmentId,
                reimburserId,
                businessTypeId,
                status,
                pageNo,
                pageSize
        );
        return ApiResponse.success(reimbursementService.list(query));
    }

    @PostMapping
    public ApiResponse<ReimbursementCreatedResponse> create(
            @Valid @RequestBody ReimbursementSaveRequest request
    ) {
        return ApiResponse.success(reimbursementService.create(request));
    }

    @GetMapping("/{reimbursementId}")
    public ApiResponse<ReimbursementDetailResponse> detail(@PathVariable String reimbursementId) {
        return ApiResponse.success(reimbursementService.detail(reimbursementId));
    }

    @PutMapping("/{reimbursementId}")
    public ApiResponse<ReimbursementDetailResponse> update(
            @PathVariable String reimbursementId,
            @Valid @RequestBody ReimbursementSaveRequest request
    ) {
        return ApiResponse.success(reimbursementService.update(reimbursementId, request));
    }

    @DeleteMapping("/{reimbursementId}")
    public ApiResponse<Void> delete(@PathVariable String reimbursementId) {
        reimbursementService.delete(reimbursementId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{reimbursementId}/submit")
    public ApiResponse<ReimbursementDetailResponse> submit(@PathVariable String reimbursementId) {
        return ApiResponse.success("提交成功", reimbursementService.submit(reimbursementId));
    }

    @PostMapping("/{reimbursementId}/withdraw")
    public ApiResponse<ReimbursementDetailResponse> withdraw(@PathVariable String reimbursementId) {
        return ApiResponse.success(reimbursementService.withdraw(reimbursementId));
    }

    @PostMapping("/{reimbursementId}/void")
    public ApiResponse<ReimbursementDetailResponse> voidBill(
            @PathVariable String reimbursementId,
            @Valid @RequestBody VoidRequest request
    ) {
        return ApiResponse.success(reimbursementService.voidBill(reimbursementId, request));
    }

    @PostMapping("/{reimbursementId}/approve")
    public ApiResponse<ReimbursementDetailResponse> approve(@PathVariable String reimbursementId) {
        return ApiResponse.success(reimbursementService.approve(reimbursementId));
    }

    @PostMapping("/{reimbursementId}/disburse")
    public ApiResponse<ReimbursementDetailResponse> disburse(
            @PathVariable String reimbursementId,
            @Valid @RequestBody PaymentRequest request
    ) {
        return ApiResponse.success(reimbursementService.disburse(reimbursementId, request));
    }

    @PostMapping("/{reimbursementId}/trips")
    public ApiResponse<TripResponse> addTrip(
            @PathVariable String reimbursementId,
            @Valid @RequestBody TripRequest request
    ) {
        return ApiResponse.success(reimbursementService.addTrip(reimbursementId, request));
    }

    @PutMapping("/{reimbursementId}/trips/{tripId}")
    public ApiResponse<TripResponse> updateTrip(
            @PathVariable String reimbursementId,
            @PathVariable String tripId,
            @Valid @RequestBody TripRequest request
    ) {
        return ApiResponse.success(reimbursementService.updateTrip(reimbursementId, tripId, request));
    }

    @DeleteMapping("/{reimbursementId}/trips/{tripId}")
    public ApiResponse<Void> deleteTrip(
            @PathVariable String reimbursementId,
            @PathVariable String tripId
    ) {
        reimbursementService.deleteTrip(reimbursementId, tripId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{reimbursementId}/trips/{tripId}/copy")
    public ApiResponse<TripResponse> copyTrip(
            @PathVariable String reimbursementId,
            @PathVariable String tripId,
            @Valid @RequestBody CopyTripRequest request
    ) {
        return ApiResponse.success(reimbursementService.copyTrip(reimbursementId, tripId, request));
    }

    @GetMapping("/{reimbursementId}/allowances")
    public ApiResponse<List<AllowanceResponse>> allowances(@PathVariable String reimbursementId) {
        return ApiResponse.success(reimbursementService.allowances(reimbursementId));
    }

    @GetMapping("/{reimbursementId}/allowances/{allowanceId}/calendar")
    public ApiResponse<List<CalendarResponse>> calendars(
            @PathVariable String reimbursementId,
            @PathVariable String allowanceId
    ) {
        return ApiResponse.success(reimbursementService.calendars(reimbursementId, allowanceId));
    }

    @PutMapping("/{reimbursementId}/allowances/{allowanceId}/calendar")
    public ApiResponse<List<CalendarResponse>> saveCalendar(
            @PathVariable String reimbursementId,
            @PathVariable String allowanceId,
            @Valid @RequestBody CalendarSaveRequest request
    ) {
        return ApiResponse.success(reimbursementService.saveCalendar(reimbursementId, allowanceId, request));
    }

    @GetMapping("/{reimbursementId}/expense-totals")
    public ApiResponse<ExpenseTotalsResponse> totals(@PathVariable String reimbursementId) {
        return ApiResponse.success(reimbursementService.totals(reimbursementId));
    }

    @PutMapping("/{reimbursementId}/remark")
    public ApiResponse<ReimbursementDetailResponse> updateRemark(
            @PathVariable String reimbursementId,
            @Valid @RequestBody RemarkRequest request
    ) {
        return ApiResponse.success(reimbursementService.updateRemark(reimbursementId, request));
    }

    @DeleteMapping("/{reimbursementId}/remark")
    public ApiResponse<ReimbursementDetailResponse> deleteRemark(@PathVariable String reimbursementId) {
        return ApiResponse.success(reimbursementService.deleteRemark(reimbursementId));
    }
}
