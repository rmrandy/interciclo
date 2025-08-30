package com.sources.app.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
// Mantener tipos String para compatibilidad con columnas VARCHAR en Oracle

@Entity
@Table(name = "TICKETS")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TICKET")
    private Long idTicket;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FLIGHT_ID", referencedColumnName = "ID_FLIGHT")
    private Flight flight;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID_USER")
    private User user;

    @Column(name = "SEAT_NUMBER", length = 6)
    private String seatNumber;

    @Column(name = "SEAT_CATEGORY", length = 20)
    private String seatCategory; // ECONOMY, BUSINESS, FIRST_CLASS

    @Column(name = "QUANTITY")
    private Integer quantity = 1; // Cantidad de asientos reservados

    @Column(name = "FARE", precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(name = "STATUS", length = 20)
    private String status; // RESERVED, CONFIRMED, CANCELLED, USED

    @Column(name = "BOOKING_DATE", insertable = false, updatable = false)
    private String bookingDate; // Fecha de reserva

    @Column(name = "BOOKING_TIME", insertable = false, updatable = false)
    private String bookingTime; // Hora de reserva

    @Column(name = "PASSENGER_FIRST_NAME", length = 100)
    private String passengerFirstName;

    @Column(name = "PASSENGER_LAST_NAME", length = 100)
    private String passengerLastName;

    @Column(name = "PASSENGER_DOCUMENT_TYPE", length = 20)
    private String passengerDocumentType; // PASSPORT, ID_CARD, DRIVER_LICENSE

    @Column(name = "PASSENGER_DOCUMENT_NUMBER", length = 50)
    private String passengerDocumentNumber;

    @Column(name = "PASSENGER_EMAIL", length = 100)
    private String passengerEmail;

    @Column(name = "PASSENGER_PHONE", length = 20)
    private String passengerPhone;

    @Column(name = "SPECIAL_REQUESTS", length = 500)
    private String specialRequests; // Comidas especiales, asistencia, etc.

    @Column(name = "PAYMENT_STATUS", length = 20)
    private String paymentStatus; // PENDING, PAID, FAILED, REFUNDED

    @Column(name = "PAYMENT_METHOD", length = 30)
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER

    @Column(name = "TOTAL_AMOUNT", precision = 10, scale = 2)
    private BigDecimal totalAmount; // Precio total con impuestos y cargos

    @Column(name = "TAXES", precision = 10, scale = 2)
    private BigDecimal taxes;

    @Column(name = "FEES", precision = 10, scale = 2)
    private BigDecimal fees;

    @Column(name = "DISCOUNT_AMOUNT", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "DISCOUNT_CODE", length = 50)
    private String discountCode;

    @Column(name = "CANCELLATION_DATE")
    private String cancellationDate;

    @Column(name = "CANCELLATION_REASON", length = 200)
    private String cancellationReason;

    @Column(name = "REFUND_AMOUNT", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private String createdAt;

    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private String updatedAt;

    public Ticket() { }

    // Getters y Setters
    public Long getIdTicket() { return idTicket; }
    public void setIdTicket(Long idTicket) { this.idTicket = idTicket; }

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getFare() { return fare; }
    public void setFare(BigDecimal fare) { this.fare = fare; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public String getPassengerFirstName() { return passengerFirstName; }
    public void setPassengerFirstName(String passengerFirstName) { this.passengerFirstName = passengerFirstName; }

    public String getPassengerLastName() { return passengerLastName; }
    public void setPassengerLastName(String passengerLastName) { this.passengerLastName = passengerLastName; }

    public String getPassengerDocumentType() { return passengerDocumentType; }
    public void setPassengerDocumentType(String passengerDocumentType) { this.passengerDocumentType = passengerDocumentType; }

    public String getPassengerDocumentNumber() { return passengerDocumentNumber; }
    public void setPassengerDocumentNumber(String passengerDocumentNumber) { this.passengerDocumentNumber = passengerDocumentNumber; }

    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }

    public String getPassengerPhone() { return passengerPhone; }
    public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTaxes() { return taxes; }
    public void setTaxes(BigDecimal taxes) { this.taxes = taxes; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }

    public String getCancellationDate() { return cancellationDate; }
    public void setCancellationDate(String cancellationDate) { this.cancellationDate = cancellationDate; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = "RESERVED";
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // timestamps manejados por BD
    }
}


