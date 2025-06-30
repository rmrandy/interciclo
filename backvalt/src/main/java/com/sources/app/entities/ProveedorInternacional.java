package com.sources.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "proveedores_internacionales")
public class ProveedorInternacional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private Double porcentajeGanancia;

    @Column(nullable = false)
    private Integer tiempoEntregaDias;

    @Column(nullable = false)
    private Boolean activo = true;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public Double getPorcentajeGanancia() { return porcentajeGanancia; }
    public void setPorcentajeGanancia(Double porcentajeGanancia) { this.porcentajeGanancia = porcentajeGanancia; }

    public Integer getTiempoEntregaDias() { return tiempoEntregaDias; }
    public void setTiempoEntregaDias(Integer tiempoEntregaDias) { this.tiempoEntregaDias = tiempoEntregaDias; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
} 