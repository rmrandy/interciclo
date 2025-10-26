package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Representa los roles de usuario en el sistema de aerolíneas.
 * Define las 4 categorías: ADMIN, EMPLOYEE, WEBSERVICE, REGISTERED_VISITOR
 */
@Entity
@Table(name = "USER_ROLES")
public class UserRole {

    /**
     * Identificador único del rol (Clave primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROLE")
    private Long idRole;

    /**
     * Nombre del rol.
     * No puede ser nulo y debe ser único.
     */
    @Column(name = "ROLE_NAME", nullable = false, unique = true, length = 50)
    private String roleName;

    /**
     * Descripción del rol.
     */
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    /**
     * Permisos asociados al rol (JSON string).
     */
    @Column(name = "PERMISSIONS", length = 1000)
    private String permissions;

    /**
     * Estado del rol (1 = activo, 0 = inactivo).
     */
    @Column(name = "ENABLED", nullable = false)
    private Integer enabled;

    /**
     * Fecha de creación del rol.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT")
    private Date createdAt;

    /**
     * Fecha de última modificación del rol.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    /**
     * Constructor por defecto.
     */
    public UserRole() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.enabled = 1;
    }

    /**
     * Constructor con parámetros.
     */
    public UserRole(String roleName, String description, String permissions) {
        this();
        this.roleName = roleName;
        this.description = description;
        this.permissions = permissions;
    }

    // Getters y Setters
    public Long getIdRole() { return idRole; }
    public void setIdRole(Long idRole) { this.idRole = idRole; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Constantes para los roles del sistema.
     */
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";
    public static final String ROLE_WEBSERVICE = "WEBSERVICE";
    public static final String ROLE_REGISTERED_VISITOR = "REGISTERED_VISITOR";
    public static final String ROLE_ANONYMOUS = "ANONYMOUS";
}

