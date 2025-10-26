package com.sources.app.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "FLIGHT_FARES")
public class FlightFare {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_fare_seq")
    @SequenceGenerator(name = "flight_fare_seq", sequenceName = "FLIGHT_FARES_SEQ", allocationSize = 1)
    @Column(name = "ID_FARE")
    private Long idFare;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FLIGHT_ID", referencedColumnName = "ID_FLIGHT")
    private Flight flight;

    @Column(name = "SEAT_CATEGORY", nullable = false, length = 20)
    private String seatCategory;

    @Column(name = "BASE_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency = "GTQ";

    @Column(name = "TAXES", precision = 10, scale = 2)
    private BigDecimal taxes = BigDecimal.ZERO;

    @Column(name = "FEES", precision = 10, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;

    @Column(name = "TOTAL_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "PROMOTIONAL_PRICE", precision = 10, scale = 2)
    private BigDecimal promotionalPrice;

    @Column(name = "PROMOTION_VALID_UNTIL")
    private LocalDateTime promotionValidUntil;

    @Column(name = "REFUNDABLE")
    private Boolean refundable = true;

    @Column(name = "CHANGEABLE")
    private Boolean changeable = true;

    @Column(name = "CHANGE_FEE", precision = 10, scale = 2)
    private BigDecimal changeFee = BigDecimal.ZERO;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    public FlightFare() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public FlightFare(Flight flight, String seatCategory, BigDecimal basePrice) {
        this();
        this.flight = flight;
        this.seatCategory = seatCategory;
        this.basePrice = basePrice;
        calculateTotalPrice();
    }

    // Getters y Setters
    public Long getIdFare() { return idFare; }
    public void setIdFare(Long idFare) { this.idFare = idFare; }

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }

    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { 
        this.basePrice = basePrice; 
        calculateTotalPrice();
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getTaxes() { return taxes; }
    public void setTaxes(BigDecimal taxes) { 
        this.taxes = taxes; 
        calculateTotalPrice();
    }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { 
        this.fees = fees; 
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getPromotionalPrice() { return promotionalPrice; }
    public void setPromotionalPrice(BigDecimal promotionalPrice) { this.promotionalPrice = promotionalPrice; }

    public LocalDateTime getPromotionValidUntil() { return promotionValidUntil; }
    public void setPromotionValidUntil(LocalDateTime promotionValidUntil) { this.promotionValidUntil = promotionValidUntil; }

    public Boolean getRefundable() { return refundable; }
    public void setRefundable(Boolean refundable) { this.refundable = refundable; }

    public Boolean getChangeable() { return changeable; }
    public void setChangeable(Boolean changeable) { this.changeable = changeable; }

    public BigDecimal getChangeFee() { return changeFee; }
    public void setChangeFee(BigDecimal changeFee) { this.changeFee = changeFee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Métodos de negocio
    public void calculateTotalPrice() {
        if (this.basePrice != null) {
            this.totalPrice = this.basePrice
                .add(this.taxes != null ? this.taxes : BigDecimal.ZERO)
                .add(this.fees != null ? this.fees : BigDecimal.ZERO);
        }
    }

    public BigDecimal getCurrentPrice() {
        if (this.promotionalPrice != null && this.promotionValidUntil != null) {
            if (LocalDateTime.now().isBefore(this.promotionValidUntil)) {
                return this.promotionalPrice;
            }
        }
        return this.totalPrice;
    }

    public boolean isPromotionActive() {
        return this.promotionalPrice != null && this.promotionValidUntil != null 
               && LocalDateTime.now().isBefore(this.promotionValidUntil);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        calculateTotalPrice();
    }
}
