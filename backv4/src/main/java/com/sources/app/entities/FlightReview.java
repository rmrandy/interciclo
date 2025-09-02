package com.sources.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FLIGHT_REVIEWS")
public class FlightReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REVIEW")
    private Long idReview;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FLIGHT_ID", referencedColumnName = "ID_FLIGHT")
    private Flight flight;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID_USER")
    private User user;

    @Column(name = "RATING")
    private Integer rating; // 1-5

    // En la BD la columna es REVIEW_TEXT
    @Column(name = "REVIEW_TEXT", length = 1000)
    private String comment;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    // Soporte para hilos de comentarios (opcional)
    @Column(name = "PARENT_REVIEW_ID")
    private Long parentReviewId;

    public FlightReview() { this.createdAt = LocalDateTime.now(); }

    public Long getIdReview() { return idReview; }
    public void setIdReview(Long idReview) { this.idReview = idReview; }

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getParentReviewId() { return parentReviewId; }
    public void setParentReviewId(Long parentReviewId) { this.parentReviewId = parentReviewId; }
}


