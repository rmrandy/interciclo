package com.sources.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FLIGHT_INVENTORY")
public class FlightInventory {

    @Id
    @Column(name = "ID_INVENTORY")
    private Long idInventory;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FLIGHT_ID", referencedColumnName = "ID_FLIGHT")
    private Flight flight;

    @Column(name = "SEAT_CATEGORY", nullable = false, length = 20)
    private String seatCategory;

    @Column(name = "TOTAL_SEATS", nullable = false)
    private Integer totalSeats;

    @Column(name = "AVAILABLE_SEATS", nullable = false)
    private Integer availableSeats;

    @Column(name = "RESERVED_SEATS", nullable = false)
    private Integer reservedSeats = 0;

    @Column(name = "SOLD_SEATS", nullable = false)
    private Integer soldSeats = 0;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    public FlightInventory() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public FlightInventory(Flight flight, String seatCategory, Integer totalSeats) {
        this();
        this.flight = flight;
        this.seatCategory = seatCategory;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    // Getters y Setters
    public Long getIdInventory() { return idInventory; }
    public void setIdInventory(Long idInventory) { this.idInventory = idInventory; }

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }

    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public Integer getReservedSeats() { return reservedSeats; }
    public void setReservedSeats(Integer reservedSeats) { this.reservedSeats = reservedSeats; }

    public Integer getSoldSeats() { return soldSeats; }
    public void setSoldSeats(Integer soldSeats) { this.soldSeats = soldSeats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Métodos de negocio
    public void updateAvailableSeats() {
        this.availableSeats = this.totalSeats - this.reservedSeats - this.soldSeats;
        if (this.availableSeats < 0) {
            this.availableSeats = 0;
        }
    }

    public boolean hasAvailableSeats() {
        return this.availableSeats > 0;
    }

    public boolean canReserve(Integer seats) {
        return this.availableSeats >= seats;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateAvailableSeats();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        updateAvailableSeats();
    }
}
