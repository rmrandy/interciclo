package com.sources.app.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para manejar agencias y clientes empresariales
 */
@Entity
@Table(name = "AGENCIES")
public class Agency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AGENCY")
    private Integer idAgency;
    
    @Column(name = "AGENCY_CODE", nullable = false, unique = true, length = 20)
    private String agencyCode; // Código único de la agencia
    
    @Column(name = "COMPANY_NAME", nullable = false, length = 200)
    private String companyName; // Nombre de la empresa
    
    @Column(name = "CONTACT_NAME", nullable = false, length = 100)
    private String contactName; // Nombre del contacto principal
    
    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email; // Email para notificaciones
    
    @Column(name = "PHONE", length = 20)
    private String phone; // Teléfono de contacto
    
    @Column(name = "ADDRESS", length = 500)
    private String address; // Dirección de la agencia
    
    @Column(name = "COUNTRY", length = 50)
    private String country; // País
    
    @Column(name = "CITY", length = 100)
    private String city; // Ciudad
    
    @Column(name = "DISCOUNT_PERCENTAGE", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO; // % de descuento (ej: 5.00 = 5%)
    
    @Column(name = "CREDIT_LIMIT", precision = 15, scale = 2)
    private BigDecimal creditLimit; // Límite de crédito si aplica
    
    @Column(name = "CURRENT_BALANCE", precision = 15, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO; // Balance actual
    
    @Column(name = "COMMISSION_PERCENTAGE", precision = 5, scale = 2)
    private BigDecimal commissionPercentage = BigDecimal.ZERO; // % de comisión para la agencia
    
    @Column(name = "PAYMENT_TERMS", length = 50)
    private String paymentTerms = "IMMEDIATE"; // IMMEDIATE, NET_30, NET_60
    
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, INACTIVE
    
    @Column(name = "NOTIFICATION_PREFERENCES", length = 100)
    private String notificationPreferences = "EMAIL"; // EMAIL, WS, BOTH
    
    @Column(name = "WS_ENDPOINT_URL", length = 500)
    private String wsEndpointUrl; // URL del WS de la agencia para notificaciones
    
    @Column(name = "WS_AUTH_TOKEN", length = 200)
    private String wsAuthToken; // Token de autenticación para WS
    
    @Column(name = "CONTRACT_START_DATE")
    @Temporal(TemporalType.DATE)
    private java.time.LocalDate contractStartDate; // Inicio del contrato
    
    @Column(name = "CONTRACT_END_DATE")
    @Temporal(TemporalType.DATE)
    private java.time.LocalDate contractEndDate; // Fin del contrato
    
    @Column(name = "LAST_ACTIVITY")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastActivity; // Última actividad/transacción
    
    @Column(name = "NOTES", length = 1000)
    private String notes; // Notas adicionales
    
    @Column(name = "CREATED_AT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
    
    // Constructores
    public Agency() {}
    
    public Agency(String agencyCode, String companyName, String contactName, String email) {
        this.agencyCode = agencyCode;
        this.companyName = companyName;
        this.contactName = contactName;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Integer getIdAgency() {
        return idAgency;
    }
    
    public void setIdAgency(Integer idAgency) {
        this.idAgency = idAgency;
    }
    
    public String getAgencyCode() {
        return agencyCode;
    }
    
    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getContactName() {
        return contactName;
    }
    
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }
    
    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }
    
    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }
    
    public String getPaymentTerms() {
        return paymentTerms;
    }
    
    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotificationPreferences() {
        return notificationPreferences;
    }
    
    public void setNotificationPreferences(String notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }
    
    public String getWsEndpointUrl() {
        return wsEndpointUrl;
    }
    
    public void setWsEndpointUrl(String wsEndpointUrl) {
        this.wsEndpointUrl = wsEndpointUrl;
    }
    
    public String getWsAuthToken() {
        return wsAuthToken;
    }
    
    public void setWsAuthToken(String wsAuthToken) {
        this.wsAuthToken = wsAuthToken;
    }
    
    public java.time.LocalDate getContractStartDate() {
        return contractStartDate;
    }
    
    public void setContractStartDate(java.time.LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }
    
    public java.time.LocalDate getContractEndDate() {
        return contractEndDate;
    }
    
    public void setContractEndDate(java.time.LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
    
    // Métodos de negocio
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    public boolean hasValidContract() {
        LocalDateTime now = LocalDateTime.now();
        return contractStartDate != null && 
               contractEndDate != null &&
               !now.toLocalDate().isBefore(contractStartDate) &&
               !now.toLocalDate().isAfter(contractEndDate);
    }
    
    public boolean canReceiveNotifications() {
        return isActive() && email != null && !email.isEmpty();
    }
    
    public boolean supportsWebServiceNotifications() {
        return "WS".equals(notificationPreferences) || 
               "BOTH".equals(notificationPreferences) &&
               wsEndpointUrl != null && !wsEndpointUrl.isEmpty();
    }
    
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice) {
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return originalPrice;
        }
        
        BigDecimal discount = originalPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        return originalPrice.subtract(discount);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    // Constantes para estados
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_INACTIVE = "INACTIVE";
    
    // Constantes para notificaciones
    public static final String NOTIFICATION_EMAIL = "EMAIL";
    public static final String NOTIFICATION_WS = "WS";
    public static final String NOTIFICATION_BOTH = "BOTH";
    
    // Constantes para términos de pago
    public static final String PAYMENT_IMMEDIATE = "IMMEDIATE";
    public static final String PAYMENT_NET_30 = "NET_30";
    public static final String PAYMENT_NET_60 = "NET_60";
}
