package com.sources.app.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "FLIGHTS")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_seq")
    @SequenceGenerator(name = "flight_seq", sequenceName = "FLIGHTS_SEQ", allocationSize = 1)
    @Column(name = "ID_FLIGHT")
    private Integer idFlight;

    @Column(name = "FLIGHT_NUMBER", nullable = false, unique = true, length = 10)
    private String flightNumber; // Ej: AE001

    @ManyToOne(optional = false)
    @JoinColumn(name = "ORIGIN_CITY_ID", referencedColumnName = "ID_CITY")
    private City originCity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "DESTINATION_CITY_ID", referencedColumnName = "ID_CITY")
    private City destinationCity;

    @ManyToOne(optional = true)
    @JoinColumn(name = "AIRCRAFT_ID", referencedColumnName = "ID_AIRCRAFT")
    private Aircraft aircraft;

    @Column(name = "DEPARTURE_DATE")
    private String departureDate; // Cambiado de LocalDate a String
    
    @Column(name = "DEPARTURE_TIME")
    private String departureTime; // Cambiado de LocalTime a String
    
    @Column(name = "ARRIVAL_DATE")
    private String arrivalDate; // Cambiado de LocalDate a String
    
    @Column(name = "ARRIVAL_TIME")
    private String arrivalTime; // Cambiado de LocalTime a String

    @Column(name = "BASE_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice; // Precio base para economía

    @Column(name = "AVAILABLE_SEATS", nullable = false)
    private Integer availableSeats;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, CANCELLED, COMPLETED

    @Column(name = "GATE", length = 10)
    private String gate; // Puerta de embarque

    @Column(name = "TERMINAL", length = 20)
    private String terminal; // Terminal

    @Column(name = "CHECK_IN_START")
    private String checkInStart; // Cambiado de LocalDateTime a String

    @Column(name = "CHECK_IN_END")
    private String checkInEnd; // Cambiado de LocalDateTime a String

    @Column(name = "BOARDING_TIME")
    private String boardingTime; // Cambiado de LocalDateTime a String

    // Campos de auditoría
    @Column(name = "CREATED_BY", nullable = false)
    private Integer createdBy; // ID del usuario que creó

    @Column(name = "UPDATED_BY")
    private Integer updatedBy; // ID del usuario que modificó

    @Column(name = "CANCELLED_BY")
    private Integer cancelledBy; // ID del usuario que canceló

    @Column(name = "CANCELLATION_REASON", length = 500)
    private String cancellationReason; // Motivo de cancelación (obligatorio al cancelar)

    @Column(name = "CANCELLATION_DATE")
    private String cancellationDate; // Cambiado de LocalDateTime a String

    @Column(name = "CHANGE_LOG", length = 2000)
    private String changeLog; // Log de cambios importantes

    @Column(name = "NOTIFICATIONS_SENT")
    private Boolean notificationsSent = false; // Si se enviaron notificaciones

    @Column(name = "CREATED_AT")
    private String createdAt; // Cambiado de LocalDateTime a String

    @Column(name = "UPDATED_AT")
    private String updatedAt; // Cambiado de LocalDateTime a String

    // Relaciones con las nuevas entidades
    // @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<FlightLeg> flightLegs; // Escalas (máximo 1 escala)

    // @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<FlightInventory> inventory; // Inventario por categoría

    // @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<FlightFare> fares; // Precios por categoría

    // Constructores
    public Flight() { 
        this.createdAt = LocalDateTime.now().toString();
        this.updatedAt = LocalDateTime.now().toString();
    }

    public Flight(String flightNumber, City originCity, City destinationCity, 
                  String departureDate, String departureTime,
                  String arrivalDate, String arrivalTime, 
                  BigDecimal basePrice, Integer availableSeats, Integer createdBy) {
        this.flightNumber = flightNumber;
        this.originCity = originCity;
        this.destinationCity = destinationCity;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.basePrice = basePrice;
        this.availableSeats = availableSeats;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now().toString();
        this.updatedAt = LocalDateTime.now().toString();
    }

    // Getters y Setters
    public Integer getIdFlight() { return idFlight; }
    public void setIdFlight(Integer idFlight) { this.idFlight = idFlight; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public City getOriginCity() { return originCity; }
    public void setOriginCity(City originCity) { this.originCity = originCity; }

    public City getDestinationCity() { return destinationCity; }
    public void setDestinationCity(City destinationCity) { this.destinationCity = destinationCity; }

    public Aircraft getAircraft() { return aircraft; }
    public void setAircraft(Aircraft aircraft) { this.aircraft = aircraft; }

    public String getDepartureDate() { return departureDate; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(String arrivalDate) { this.arrivalDate = arrivalDate; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public String getCheckInStart() { return checkInStart; }
    public void setCheckInStart(String checkInStart) { this.checkInStart = checkInStart; }

    public String getCheckInEnd() { return checkInEnd; }
    public void setCheckInEnd(String checkInEnd) { this.checkInEnd = checkInEnd; }

    public String getBoardingTime() { return boardingTime; }
    public void setBoardingTime(String boardingTime) { this.boardingTime = boardingTime; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }

    public Integer getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(Integer cancelledBy) { this.cancelledBy = cancelledBy; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getCancellationDate() { return cancellationDate; }
    public void setCancellationDate(String cancellationDate) { this.cancellationDate = cancellationDate; }

    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }

    public Boolean getNotificationsSent() { return notificationsSent; }
    public void setNotificationsSent(Boolean notificationsSent) { this.notificationsSent = notificationsSent; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // public List<FlightLeg> getFlightLegs() { return flightLegs; }
    // public void setFlightLegs(List<FlightLeg> flightLegs) { this.flightLegs = flightLegs; }

    // public List<FlightInventory> getInventory() { return inventory; }
    // public void setInventory(List<FlightInventory> inventory) { this.inventory = inventory; }

    // public List<FlightFare> getFares() { return fares; }
    // public void setFares(List<FlightFare> fares) { this.fares = fares; }

    // Métodos de negocio
    public boolean isPublished() {
        return STATUS_PUBLISHED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public boolean isDraft() {
        return STATUS_DRAFT.equals(status);
    }

    public boolean canBeModified() {
        return isDraft() || isPublished();
    }

    public boolean canBeCancelled() {
        // TEMPORAL: Permitir cancelar cualquier vuelo para debugging
        System.out.println("🔍 canBeCancelled() - Estado: " + status + ", Fecha: " + departureDate);
        return true; // Temporalmente siempre true para debugging
    }

    // public boolean hasStops() {
    //     return flightLegs != null && !flightLegs.isEmpty();
    // }

    // public int getStopCount() {
    //     return flightLegs != null ? flightLegs.size() : 0;
    // }

    // public boolean isDirectFlight() {
    //     return !hasStops();
    // }

    public void addChangeLogEntry(String change, Integer userId) {
        String timestamp = LocalDateTime.now().toString();
        String entry = String.format("[%s] Usuario %d: %s", timestamp, userId, change);
        
        if (changeLog == null || changeLog.isEmpty()) {
            changeLog = entry;
        } else {
            changeLog = changeLog + "\n" + entry;
        }
        
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now().toString();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now().toString();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now.toString();
        this.updatedAt = now.toString();
    }

    // Constantes para estados
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";
}


