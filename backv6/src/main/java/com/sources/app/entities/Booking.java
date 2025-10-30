package com.sources.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_BOOKING")
    private Long idBooking;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID_USER")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "TICKET_ID", referencedColumnName = "ID_TICKET")
    private Ticket ticket;

    @Column(name = "BOOKED_AT", nullable = false)
    private LocalDateTime bookedAt;

    @Column(name = "STATUS", length = 20)
    private String status; // CONFIRMED, CANCELLED

    public Booking() { this.bookedAt = LocalDateTime.now(); }

    public Long getIdBooking() { return idBooking; }
    public void setIdBooking(Long idBooking) { this.idBooking = idBooking; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}


