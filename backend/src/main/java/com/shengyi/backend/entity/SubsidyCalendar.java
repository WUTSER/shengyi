package com.shengyi.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("fk_subsidy_calendar")
public class SubsidyCalendar {

    @TableId(type = IdType.INPUT)
    private String id;

    private String mainId;
    private LocalDate travelDate;
    private String travelDateWeek;
    private String subsidizedCities;
    private String subsidizedCityNumber;
    private String remark;
    private BigDecimal standardMealExpensesAmount;
    private BigDecimal standardTrafficAmount;
    private BigDecimal standardCommunicationAmount;
    private BigDecimal mealExpensesAmount;
    private BigDecimal trafficAmount;
    private BigDecimal communicationAmount;
    private String mealChecked;
    private String trafficChecked;
    private String communicationChecked;
    private String isReimbursed;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMainId() { return mainId; }
    public void setMainId(String mainId) { this.mainId = mainId; }

    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }

    public String getTravelDateWeek() { return travelDateWeek; }
    public void setTravelDateWeek(String travelDateWeek) { this.travelDateWeek = travelDateWeek; }

    public String getSubsidizedCities() { return subsidizedCities; }
    public void setSubsidizedCities(String subsidizedCities) { this.subsidizedCities = subsidizedCities; }

    public String getSubsidizedCityNumber() { return subsidizedCityNumber; }
    public void setSubsidizedCityNumber(String subsidizedCityNumber) { this.subsidizedCityNumber = subsidizedCityNumber; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public BigDecimal getStandardMealExpensesAmount() { return standardMealExpensesAmount; }
    public void setStandardMealExpensesAmount(BigDecimal standardMealExpensesAmount) { this.standardMealExpensesAmount = standardMealExpensesAmount; }

    public BigDecimal getStandardTrafficAmount() { return standardTrafficAmount; }
    public void setStandardTrafficAmount(BigDecimal standardTrafficAmount) { this.standardTrafficAmount = standardTrafficAmount; }

    public BigDecimal getStandardCommunicationAmount() { return standardCommunicationAmount; }
    public void setStandardCommunicationAmount(BigDecimal standardCommunicationAmount) { this.standardCommunicationAmount = standardCommunicationAmount; }

    public BigDecimal getMealExpensesAmount() { return mealExpensesAmount; }
    public void setMealExpensesAmount(BigDecimal mealExpensesAmount) { this.mealExpensesAmount = mealExpensesAmount; }

    public BigDecimal getTrafficAmount() { return trafficAmount; }
    public void setTrafficAmount(BigDecimal trafficAmount) { this.trafficAmount = trafficAmount; }

    public BigDecimal getCommunicationAmount() { return communicationAmount; }
    public void setCommunicationAmount(BigDecimal communicationAmount) { this.communicationAmount = communicationAmount; }

    public String getMealChecked() { return mealChecked; }
    public void setMealChecked(String mealChecked) { this.mealChecked = mealChecked; }

    public String getTrafficChecked() { return trafficChecked; }
    public void setTrafficChecked(String trafficChecked) { this.trafficChecked = trafficChecked; }

    public String getCommunicationChecked() { return communicationChecked; }
    public void setCommunicationChecked(String communicationChecked) { this.communicationChecked = communicationChecked; }

    public String getIsReimbursed() { return isReimbursed; }
    public void setIsReimbursed(String isReimbursed) { this.isReimbursed = isReimbursed; }
}
