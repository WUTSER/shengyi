package com.shengyi.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("fk_reim_itinerary")
public class ReimItinerary {

    @TableId(type = IdType.INPUT)
    private String id;

    private String mainId;
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private String departureCity;
    private String departureCityNo;
    private String arrivingCity;
    private String arrivingCityNo;
    private String itineraryInstructions;
    private LocalDateTime creationTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMainId() { return mainId; }
    public void setMainId(String mainId) { this.mainId = mainId; }

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

    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }

    public String getDepartureCityNo() { return departureCityNo; }
    public void setDepartureCityNo(String departureCityNo) { this.departureCityNo = departureCityNo; }

    public String getArrivingCity() { return arrivingCity; }
    public void setArrivingCity(String arrivingCity) { this.arrivingCity = arrivingCity; }

    public String getArrivingCityNo() { return arrivingCityNo; }
    public void setArrivingCityNo(String arrivingCityNo) { this.arrivingCityNo = arrivingCityNo; }

    public String getItineraryInstructions() { return itineraryInstructions; }
    public void setItineraryInstructions(String itineraryInstructions) { this.itineraryInstructions = itineraryInstructions; }

    public LocalDateTime getCreationTime() { return creationTime; }
    public void setCreationTime(LocalDateTime creationTime) { this.creationTime = creationTime; }
}
