package com.sources.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "AIRCRAFTS")
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AIRCRAFT")
    private Long idAircraft;

    @Column(name = "REGISTRATION", nullable = false, unique = true, length = 20)
    private String registration; // Matrícula

    @Column(name = "MODEL", nullable = false, length = 100)
    private String model;

    @Column(name = "MANUFACTURER", nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "SEAT_CAPACITY", nullable = false)
    private Integer seatCapacity;

    public Aircraft() { }

    public Long getIdAircraft() {
        return idAircraft;
    }

    public void setIdAircraft(Long idAircraft) {
        this.idAircraft = idAircraft;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(Integer seatCapacity) {
        this.seatCapacity = seatCapacity;
    }
}


