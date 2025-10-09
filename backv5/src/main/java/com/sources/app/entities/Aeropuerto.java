package com.sources.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa una entidad de aeropuerto mapeada a la tabla AEROPUERTOS.
 * Contiene información sobre aeropuertos, incluyendo códigos IATA/ICAO, capacidad y ubicación.
 */
@Entity
@Table(name = "AEROPUERTOS")
public class Aeropuerto {

    /**
     * Identificador único del aeropuerto.
     * Clave primaria manual (no auto-generada). NUMBER(5,0).
     */
    @Id
    @Column(name = "ID_AEROPUERTO", precision = 5, scale = 0)
    private Long idAeropuerto;

    /**
     * Identificador de la ciudad donde se encuentra el aeropuerto.
     * No puede ser nulo. NUMBER(5,0).
     */
    @Column(name = "ID_CIUDAD", nullable = false, precision = 5, scale = 0)
    private Long idCiudad;

    /**
     * Código IATA del aeropuerto (3 caracteres).
     * No puede ser nulo y debe ser único. VARCHAR2(3).
     */
    @Column(name = "CODIGO_IATA", nullable = false, length = 3, unique = true)
    private String codigoIata;

    /**
     * Código ICAO del aeropuerto (4 caracteres).
     * Puede ser nulo. VARCHAR2(4).
     */
    @Column(name = "CODIGO_ICAO", length = 4)
    private String codigoIcao;

    /**
     * Nombre del aeropuerto.
     * No puede ser nulo. VARCHAR2(200).
     */
    @Column(name = "NOMBRE", nullable = false, length = 200)
    private String nombre;

    /**
     * Tipo de aeropuerto (INTERNACIONAL, NACIONAL, etc.).
     * Valor por defecto: 'INTERNACIONAL'. VARCHAR2(20).
     */
    @Column(name = "TIPO_AEROPUERTO", length = 20, columnDefinition = "VARCHAR2(20) DEFAULT 'INTERNACIONAL'")
    private String tipoAeropuerto = "INTERNACIONAL";

    /**
     * Capacidad anual de pasajeros del aeropuerto.
     * Puede ser nulo. NUMBER(10,0).
     */
    @Column(name = "CAPACIDAD_ANUAL", precision = 10, scale = 0)
    private Long capacidadAnual;

    /**
     * Fecha y hora de creación del registro.
     * Valor por defecto: SYSTIMESTAMP. TIMESTAMP(6).
     */
    @Column(name = "FECHA_CREACION", columnDefinition = "TIMESTAMP(6) DEFAULT SYSTIMESTAMP")
    private LocalDateTime fechaCreacion;

    /**
     * Indica si el aeropuerto está activo.
     * Valor por defecto: 'S'. Solo acepta 'S' o 'N'. CHAR(1).
     */
    @Column(name = "ACTIVO", columnDefinition = "CHAR(1) DEFAULT 'S'")
    private Character activo = 'S';

    /**
     * Constructor por defecto para la entidad Aeropuerto.
     * Requerido por JPA.
     */
    public Aeropuerto() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = 'S';
    }

    /**
     * Constructor con parámetros principales.
     */
    public Aeropuerto(Long idCiudad, String codigoIata, String nombre) {
        this();
        this.idCiudad = idCiudad;
        this.codigoIata = codigoIata;
        this.nombre = nombre;
    }

    // Getters y Setters
    public Long getIdAeropuerto() {
        return idAeropuerto;
    }

    public void setIdAeropuerto(Long idAeropuerto) {
        this.idAeropuerto = idAeropuerto;
    }

    public Long getIdCiudad() {
        return idCiudad;
    }

    public void setIdCiudad(Long idCiudad) {
        this.idCiudad = idCiudad;
    }

    public String getCodigoIata() {
        return codigoIata;
    }

    public void setCodigoIata(String codigoIata) {
        this.codigoIata = codigoIata;
    }

    public String getCodigoIcao() {
        return codigoIcao;
    }

    public void setCodigoIcao(String codigoIcao) {
        this.codigoIcao = codigoIcao;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoAeropuerto() {
        return tipoAeropuerto;
    }

    public void setTipoAeropuerto(String tipoAeropuerto) {
        this.tipoAeropuerto = tipoAeropuerto;
    }

    public Long getCapacidadAnual() {
        return capacidadAnual;
    }

    public void setCapacidadAnual(Long capacidadAnual) {
        this.capacidadAnual = capacidadAnual;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Character getActivo() {
        return activo;
    }

    public void setActivo(Character activo) {
        this.activo = activo;
    }
}
