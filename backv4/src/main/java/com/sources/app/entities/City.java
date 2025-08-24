package com.sources.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "CITIES")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CITY")
    private Long idCity;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "COUNTRY", nullable = false, length = 100)
    private String country;

    public City() { }

    public Long getIdCity() {
        return idCity;
    }

    public void setIdCity(Long idCity) {
        this.idCity = idCity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}


