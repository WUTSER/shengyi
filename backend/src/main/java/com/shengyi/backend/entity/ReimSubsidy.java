package com.shengyi.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("fk_reim_subsidy")
public class ReimSubsidy {

    @TableId(type = IdType.INPUT)
    private String id;

    private String mainId;
    private String itineraryId;
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private Integer subsidyDays;
    private String departureCity;
    private String departureCityNo;
    private String arrivingCity;
    private String arrivingCityNo;
    private BigDecimal applicationAmount;
    private BigDecimal subsidyAmount;
    private BigDecimal mealAllowance;
    private BigDecimal transportationAllowance;
    private BigDecimal phoneAllowance;
    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMainId() { return mainId; }
    public void setMainId(String mainId) { this.mainId = mainId; }

    public String getItineraryId() { return itineraryId; }
    public void setItineraryId(String itineraryId) { this.itineraryId = itineraryId; }

    public String getTravelerId() { return travelerId; }
    public void setTravelerId(String travelerId) { this.travelerId = travelerId; }

    public String getTravelerNo() { return travelerNo; }
    public void setTravelerNo(String travelerNo) { this.travelerNo = travelerNo; }

    public String getTravelerName() { return travelerName; }
    public void setTravelerName(String travelerName) { this.travelerName = travelerName; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }

    public Integer getSubsidyDays() { return subsidyDays; }
    public void setSubsidyDays(Integer subsidyDays) { this.subsidyDays = subsidyDays; }

    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }

    public String getDepartureCityNo() { return departureCityNo; }
    public void setDepartureCityNo(String departureCityNo) { this.departureCityNo = departureCityNo; }

    public String getArrivingCity() { return arrivingCity; }
    public void setArrivingCity(String arrivingCity) { this.arrivingCity = arrivingCity; }

    public String getArrivingCityNo() { return arrivingCityNo; }
    public void setArrivingCityNo(String arrivingCityNo) { this.arrivingCityNo = arrivingCityNo; }

    public BigDecimal getApplicationAmount() { return applicationAmount; }
    public void setApplicationAmount(BigDecimal applicationAmount) { this.applicationAmount = applicationAmount; }

    public BigDecimal getSubsidyAmount() { return subsidyAmount; }
    public void setSubsidyAmount(BigDecimal subsidyAmount) { this.subsidyAmount = subsidyAmount; }

    public BigDecimal getMealAllowance() { return mealAllowance; }
    public void setMealAllowance(BigDecimal mealAllowance) { this.mealAllowance = mealAllowance; }

    public BigDecimal getTransportationAllowance() { return transportationAllowance; }
    public void setTransportationAllowance(BigDecimal transportationAllowance) { this.transportationAllowance = transportationAllowance; }

    public BigDecimal getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(BigDecimal phoneAllowance) { this.phoneAllowance = phoneAllowance; }

    public String getBusinessTypeId() { return businessTypeId; }
    public void setBusinessTypeId(String businessTypeId) { this.businessTypeId = businessTypeId; }

    public String getBusinessTypeNo() { return businessTypeNo; }
    public void setBusinessTypeNo(String businessTypeNo) { this.businessTypeNo = businessTypeNo; }

    public String getBusinessTypeName() { return businessTypeName; }
    public void setBusinessTypeName(String businessTypeName) { this.businessTypeName = businessTypeName; }
}
