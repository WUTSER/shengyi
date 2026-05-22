package com.shengyi.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class Models {

    private Models() {
    }

    public record ReimCompany(String reimCompanyId, String reimCompanyNo, String reimCompanyName) {
    }

    public record ReimDepartment(String reimDepartmentId, String reimDepartmentNo, String reimDepartmentName) {
    }

    public record Employee(String reimburserId, String reimburserNo, String reimburserName) {
    }

    public record BusinessType(
            String businessTypeId,
            String businessTypeNo,
            String businessTypeName,
            String thereSubordinateNode,
            String superiorId
    ) {
    }

    public record City(String cityNo, String cityName, String cityType) {
    }

    public record Project(String projectId, String projectNo, String projectName) {
    }

    public static class TravelReimbursement {
        private String reimbursementId;
        private String reimbursementNo;
        private LocalDate billDate;
        private ReimbursementStatus status = ReimbursementStatus.DRAFT;
        private String title;
        private String reason;
        private String reimburserId;
        private String reimDepartmentId;
        private String reimCompanyId;
        private String businessTypeId;
        private String remark;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime paymentTime;
        private String paymentNo;
        private final List<TravelTrip> trips = new ArrayList<>();
        private final List<TravelAllowance> allowances = new ArrayList<>();
        private final List<ExpenseSplit> expenseSplits = new ArrayList<>();

        public String getReimbursementId() {
            return reimbursementId;
        }

        public void setReimbursementId(String reimbursementId) {
            this.reimbursementId = reimbursementId;
        }

        public String getReimbursementNo() {
            return reimbursementNo;
        }

        public void setReimbursementNo(String reimbursementNo) {
            this.reimbursementNo = reimbursementNo;
        }

        public LocalDate getBillDate() {
            return billDate;
        }

        public void setBillDate(LocalDate billDate) {
            this.billDate = billDate;
        }

        public ReimbursementStatus getStatus() {
            return status;
        }

        public void setStatus(ReimbursementStatus status) {
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getReimburserId() {
            return reimburserId;
        }

        public void setReimburserId(String reimburserId) {
            this.reimburserId = reimburserId;
        }

        public String getReimDepartmentId() {
            return reimDepartmentId;
        }

        public void setReimDepartmentId(String reimDepartmentId) {
            this.reimDepartmentId = reimDepartmentId;
        }

        public String getReimCompanyId() {
            return reimCompanyId;
        }

        public void setReimCompanyId(String reimCompanyId) {
            this.reimCompanyId = reimCompanyId;
        }

        public String getBusinessTypeId() {
            return businessTypeId;
        }

        public void setBusinessTypeId(String businessTypeId) {
            this.businessTypeId = businessTypeId;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public LocalDateTime getPaymentTime() {
            return paymentTime;
        }

        public void setPaymentTime(LocalDateTime paymentTime) {
            this.paymentTime = paymentTime;
        }

        public String getPaymentNo() {
            return paymentNo;
        }

        public void setPaymentNo(String paymentNo) {
            this.paymentNo = paymentNo;
        }

        public List<TravelTrip> getTrips() {
            return trips;
        }

        public List<TravelAllowance> getAllowances() {
            return allowances;
        }

        public List<ExpenseSplit> getExpenseSplits() {
            return expenseSplits;
        }
    }

    public static class ExpenseSplit {
        private String splitId;
        private String reimbursementId;
        private int sequenceNo;
        private String reimCompanyId;
        private String projectId;
        private BigDecimal ratio = BigDecimal.ZERO;
        private BigDecimal amount = BigDecimal.ZERO;

        public String getSplitId() {
            return splitId;
        }

        public void setSplitId(String splitId) {
            this.splitId = splitId;
        }

        public String getReimbursementId() {
            return reimbursementId;
        }

        public void setReimbursementId(String reimbursementId) {
            this.reimbursementId = reimbursementId;
        }

        public int getSequenceNo() {
            return sequenceNo;
        }

        public void setSequenceNo(int sequenceNo) {
            this.sequenceNo = sequenceNo;
        }

        public String getReimCompanyId() {
            return reimCompanyId;
        }

        public void setReimCompanyId(String reimCompanyId) {
            this.reimCompanyId = reimCompanyId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public BigDecimal getRatio() {
            return ratio;
        }

        public void setRatio(BigDecimal ratio) {
            this.ratio = ratio;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public static class TravelTrip {
        private String tripId;
        private String reimbursementId;
        private String travelerId;
        private String departureCityNo;
        private String arrivalCityNo;
        private LocalDate departureDate;
        private LocalDate arrivalDate;
        private String description;

        public String getTripId() {
            return tripId;
        }

        public void setTripId(String tripId) {
            this.tripId = tripId;
        }

        public String getReimbursementId() {
            return reimbursementId;
        }

        public void setReimbursementId(String reimbursementId) {
            this.reimbursementId = reimbursementId;
        }

        public String getTravelerId() {
            return travelerId;
        }

        public void setTravelerId(String travelerId) {
            this.travelerId = travelerId;
        }

        public String getDepartureCityNo() {
            return departureCityNo;
        }

        public void setDepartureCityNo(String departureCityNo) {
            this.departureCityNo = departureCityNo;
        }

        public String getArrivalCityNo() {
            return arrivalCityNo;
        }

        public void setArrivalCityNo(String arrivalCityNo) {
            this.arrivalCityNo = arrivalCityNo;
        }

        public LocalDate getDepartureDate() {
            return departureDate;
        }

        public void setDepartureDate(LocalDate departureDate) {
            this.departureDate = departureDate;
        }

        public LocalDate getArrivalDate() {
            return arrivalDate;
        }

        public void setArrivalDate(LocalDate arrivalDate) {
            this.arrivalDate = arrivalDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class TravelAllowance {
        private String allowanceId;
        private String reimbursementId;
        private String tripId;
        private String travelerId;
        private String allowanceCityNo;
        private BigDecimal standardAmount = BigDecimal.ZERO;
        private BigDecimal applyAmount = BigDecimal.ZERO;
        private BigDecimal allowanceAmount = BigDecimal.ZERO;
        private final List<AllowanceCalendar> calendars = new ArrayList<>();

        public String getAllowanceId() {
            return allowanceId;
        }

        public void setAllowanceId(String allowanceId) {
            this.allowanceId = allowanceId;
        }

        public String getReimbursementId() {
            return reimbursementId;
        }

        public void setReimbursementId(String reimbursementId) {
            this.reimbursementId = reimbursementId;
        }

        public String getTripId() {
            return tripId;
        }

        public void setTripId(String tripId) {
            this.tripId = tripId;
        }

        public String getTravelerId() {
            return travelerId;
        }

        public void setTravelerId(String travelerId) {
            this.travelerId = travelerId;
        }

        public String getAllowanceCityNo() {
            return allowanceCityNo;
        }

        public void setAllowanceCityNo(String allowanceCityNo) {
            this.allowanceCityNo = allowanceCityNo;
        }

        public BigDecimal getStandardAmount() {
            return standardAmount;
        }

        public void setStandardAmount(BigDecimal standardAmount) {
            this.standardAmount = standardAmount;
        }

        public BigDecimal getApplyAmount() {
            return applyAmount;
        }

        public void setApplyAmount(BigDecimal applyAmount) {
            this.applyAmount = applyAmount;
        }

        public BigDecimal getAllowanceAmount() {
            return allowanceAmount;
        }

        public void setAllowanceAmount(BigDecimal allowanceAmount) {
            this.allowanceAmount = allowanceAmount;
        }

        public List<AllowanceCalendar> getCalendars() {
            return calendars;
        }
    }

    public static class AllowanceCalendar {
        private String calendarId;
        private LocalDate travelDate;
        private String allowanceCityNo;
        private AllowanceItem meal = new AllowanceItem();
        private AllowanceItem traffic = new AllowanceItem();
        private AllowanceItem communication = new AllowanceItem();

        public String getCalendarId() {
            return calendarId;
        }

        public void setCalendarId(String calendarId) {
            this.calendarId = calendarId;
        }

        public LocalDate getTravelDate() {
            return travelDate;
        }

        public void setTravelDate(LocalDate travelDate) {
            this.travelDate = travelDate;
        }

        public String getAllowanceCityNo() {
            return allowanceCityNo;
        }

        public void setAllowanceCityNo(String allowanceCityNo) {
            this.allowanceCityNo = allowanceCityNo;
        }

        public AllowanceItem getMeal() {
            return meal;
        }

        public void setMeal(AllowanceItem meal) {
            this.meal = meal;
        }

        public AllowanceItem getTraffic() {
            return traffic;
        }

        public void setTraffic(AllowanceItem traffic) {
            this.traffic = traffic;
        }

        public AllowanceItem getCommunication() {
            return communication;
        }

        public void setCommunication(AllowanceItem communication) {
            this.communication = communication;
        }
    }

    public static class AllowanceItem {
        private boolean checked = true;
        private BigDecimal standardAmount = BigDecimal.ZERO;
        private BigDecimal amount = BigDecimal.ZERO;

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public BigDecimal getStandardAmount() {
            return standardAmount;
        }

        public void setStandardAmount(BigDecimal standardAmount) {
            this.standardAmount = standardAmount;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
