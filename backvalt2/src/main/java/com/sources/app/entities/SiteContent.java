package com.sources.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SITE_CONTENT")
public class SiteContent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    @Column(name = "CONTENT_KEY", nullable = false, unique = true)
    private String contentKey;
    
    @Column(name = "CONTENT_VALUE", columnDefinition = "CLOB")
    private String contentValue;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructores
    public SiteContent() {}
    
    public SiteContent(String contentKey, String contentValue, String description) {
        this.contentKey = contentKey;
        this.contentValue = contentValue;
        this.description = description;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContentKey() {
        return contentKey;
    }
    
    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }
    
    public String getContentValue() {
        return contentValue;
    }
    
    public void setContentValue(String contentValue) {
        this.contentValue = contentValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
} 