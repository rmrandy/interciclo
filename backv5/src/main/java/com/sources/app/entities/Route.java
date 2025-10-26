package com.sources.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "ROUTES")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROUTE")
    private Long idRoute;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ORIGIN_AIRPORT_ID", referencedColumnName = "ID_AEROPUERTO")
    private Aeropuerto originAirport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "DESTINATION_AIRPORT_ID", referencedColumnName = "ID_AEROPUERTO")
    private Aeropuerto destinationAirport;

    @Column(name = "DISTANCE_KM")
    private Integer distanceKm;

    public Route() { }

    public Long getIdRoute() {
        return idRoute;
    }

    public void setIdRoute(Long idRoute) {
        this.idRoute = idRoute;
    }

    public Aeropuerto getOriginAirport() {
        return originAirport;
    }

    public void setOriginAirport(Aeropuerto originAirport) {
        this.originAirport = originAirport;
    }

    public Aeropuerto getDestinationAirport() {
        return destinationAirport;
    }

    public void setDestinationAirport(Aeropuerto destinationAirport) {
        this.destinationAirport = destinationAirport;
    }

    public Integer getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Integer distanceKm) {
        this.distanceKm = distanceKm;
    }
}


