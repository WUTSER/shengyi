package com.shengyi.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("fk_reim_main")
public class ReimMain {

    @TableId(type = IdType.INPUT)
    private String id;

    private String reimbursementNo;
    private LocalDate billDate;
    private String status;
    private String reimbursementTitle;
    private String businessTripReason;
    private String reimburserId;
    private String reimburserNo;
    private String reimburserName;
    private String reimDepartmentId;
    private String reimDepartmentNo;
    private String reimDepartmentName;
    private String reimCompanyId;
    private String reimCompanyNo;
    private String reimCompanyName;
    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;
    private BigDecimal subsidyTotal;
    private BigDecimal mealAllowance;
    private BigDecimal transportationAllowance;
    private BigDecimal phoneAllowance;
    private String remarks;
    private String paymentNo;
    private LocalDateTime paymentTime;
    private String voidReason;
    private LocalDateTime creationTime;
    private LocalDateTime updateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReimbursementNo() { return reimbursementNo; }
    public void setReimbursementNo(String reimbursementNo) { this.reimbursementNo = reimbursementNo; }

    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReimbursementTitle() { return reimbursementTitle; }
    public void setReimbursementTitle(String reimbursementTitle) { this.reimbursementTitle = reimbursementTitle; }

    public String getBusinessTripReason() { return businessTripReason; }
    public void setBusinessTripReason(String businessTripReason) { this.businessTripReason = businessTripReason; }

    public String getReimburserId() { return reimburserId; }
    public void setReimburserId(String reimburserId) { this.reimburserId = reimburserId; }

    public String getReimburserNo() { return reimburserNo; }
    public void setReimburserNo(String reimburserNo) { this.reimburserNo = reimburserNo; }

    public String getReimburserName() { return reimburserName; }
    public void setReimburserName(String reimburserName) { this.reimburserName = reimburserName; }

    public String getReimDepartmentId() { return reimDepartmentId; }
    public void setReimDepartmentId(String reimDepartmentId) { this.reimDepartmentId = reimDepartmentId; }

    public String getReimDepartmentNo() { return reimDepartmentNo; }
    public void setReimDepartmentNo(String reimDepartmentNo) { this.reimDepartmentNo = reimDepartmentNo; }

    public String getReimDepartmentName() { return reimDepartmentName; }
    public void setReimDepartmentName(String reimDepartmentName) { this.reimDepartmentName = reimDepartmentName; }

    public String getReimCompanyId() { return reimCompanyId; }
    public void setReimCompanyId(String reimCompanyId) { this.reimCompanyId = reimCompanyId; }

    public String getReimCompanyNo() { return reimCompanyNo; }
    public void setReimCompanyNo(String reimCompanyNo) { this.reimCompanyNo = reimCompanyNo; }

    public String getReimCompanyName() { return reimCompanyName; }
    public void setReimCompanyName(String reimCompanyName) { this.reimCompanyName = reimCompanyName; }

    public String getBusinessTypeId() { return businessTypeId; }
    public void setBusinessTypeId(String businessTypeId) { this.businessTypeId = businessTypeId; }

    public String getBusinessTypeNo() { return businessTypeNo; }
    public void setBusinessTypeNo(String businessTypeNo) { this.businessTypeNo = businessTypeNo; }

    public String getBusinessTypeName() { return businessTypeName; }
    public void setBusinessTypeName(String businessTypeName) { this.businessTypeName = businessTypeName; }

    public BigDecimal getSubsidyTotal() { return subsidyTotal; }
    public void setSubsidyTotal(BigDecimal subsidyTotal) { this.subsidyTotal = subsidyTotal; }

    public BigDecimal getMealAllowance() { return mealAllowance; }
    public void setMealAllowance(BigDecimal mealAllowance) { this.mealAllowance = mealAllowance; }

    public BigDecimal getTransportationAllowance() { return transportationAllowance; }
    public void setTransportationAllowance(BigDecimal transportationAllowance) { this.transportationAllowance = transportationAllowance; }

    public BigDecimal getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(BigDecimal phoneAllowance) { this.phoneAllowance = phoneAllowance; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }

    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    public String getVoidReason() { return voidReason; }
    public void setVoidReason(String voidReason) { this.voidReason = voidReason; }

    public LocalDateTime getCreationTime() { return creationTime; }
    public void setCreationTime(LocalDateTime creationTime) { this.creationTime = creationTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
