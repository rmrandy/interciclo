package com.sources.app.entities;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "FLIGHT_LEGS", schema = "AEROLINEA")
public class FlightLeg implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_leg_seq")
    @SequenceGenerator(name = "flight_leg_seq", sequenceName = "FLIGHT_LEGS_SEQ", allocationSize = 1)
    @Column(name = "ID_LEG")
    private Long idLeg;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FLIGHT_ID", nullable = false)
    private Flight flight;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITY_ID", nullable = false)
    private City city;
    
    @Column(name = "LEG_ORDER", nullable = false)
    private Integer legOrder; // 1 = primera escala, 2 = destino final
    
    @Column(name = "ARRIVAL_TIME", nullable = false, length = 5)
    private String arrivalTime; // Formato: "HH:MM"
    
    @Column(name = "DEPARTURE_TIME", nullable = false, length = 5)
    private String departureTime; // Formato: "HH:MM"
    
    @Column(name = "CONNECTION_TIME_MINUTES", nullable = false)
    private Integer connectionTimeMinutes;
    
    @Column(name = "AIRCRAFT_CHANGE", length = 1)
    private String aircraftChange = "N"; // "Y" = sí, "N" = no
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY", nullable = false)
    private User createdBy;
    
    @Column(name = "CREATED_AT", nullable = false, length = 30)
    private String createdAt;
    
    @Column(name = "UPDATED_AT", nullable = false, length = 30)
    private String updatedAt;
    
    // Constructores
    public FlightLeg() {}
    
    public FlightLeg(Flight flight, City city, Integer legOrder, String arrivalTime, 
                    String departureTime, Integer connectionTimeMinutes, String aircraftChange, User createdBy) {
        this.flight = flight;
        this.city = city;
        this.legOrder = legOrder;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.connectionTimeMinutes = connectionTimeMinutes;
        this.aircraftChange = aircraftChange;
        this.createdBy = createdBy;
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.updatedAt = java.time.LocalDateTime.now().toString();
    }
    
    // Getters y Setters
    public Long getIdLeg() {
        return idLeg;
    }
    
    public void setIdLeg(Long idLeg) {
        this.idLeg = idLeg;
    }
    
    public Flight getFlight() {
        return flight;
    }
    
    public void setFlight(Flight flight) {
        this.flight = flight;
    }
    
    public City getCity() {
        return city;
    }
    
    public void setCity(City city) {
        this.city = city;
    }
    
    public Integer getLegOrder() {
        return legOrder;
    }
    
    public void setLegOrder(Integer legOrder) {
        this.legOrder = legOrder;
    }
    
    public String getArrivalTime() {
        return arrivalTime;
    }
    
    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public String getDepartureTime() {
        return departureTime;
    }
    
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
    
    public Integer getConnectionTimeMinutes() {
        return connectionTimeMinutes;
    }
    
    public void setConnectionTimeMinutes(Integer connectionTimeMinutes) {
        this.connectionTimeMinutes = connectionTimeMinutes;
    }
    
    public String getAircraftChange() {
        return aircraftChange;
    }
    
    public void setAircraftChange(String aircraftChange) {
        this.aircraftChange = aircraftChange;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Métodos de utilidad
    public boolean hasAircraftChange() {
        return "Y".equals(aircraftChange);
    }
    
    public boolean isFirstLeg() {
        return legOrder != null && legOrder == 1;
    }
    
    public boolean isFinalLeg() {
        return legOrder != null && legOrder == 2;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = java.time.LocalDateTime.now().toString();
    }
}
