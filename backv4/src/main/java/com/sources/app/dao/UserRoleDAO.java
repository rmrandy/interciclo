package com.sources.app.dao;

import com.sources.app.entities.UserRole;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO para gestionar los roles de usuario en el sistema de aerolíneas.
 */
public class UserRoleDAO {

    /**
     * Obtiene todos los roles activos.
     * @return Lista de roles activos.
     */
    public List<UserRole> getAllActiveRoles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<UserRole> query = session.createQuery(
                "FROM UserRole WHERE enabled = 1 ORDER BY roleName", 
                UserRole.class
            );
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene un rol por su ID.
     * @param idRole ID del rol.
     * @return El rol encontrado o null.
     */
    public UserRole getRoleById(Long idRole) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(UserRole.class, idRole);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene un rol por su nombre.
     * @param roleName Nombre del rol.
     * @return El rol encontrado o null.
     */
    public UserRole getRoleByName(String roleName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<UserRole> query = session.createQuery(
                "FROM UserRole WHERE roleName = :roleName", 
                UserRole.class
            );
            query.setParameter("roleName", roleName);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Guarda un nuevo rol.
     * @param userRole El rol a guardar.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public boolean saveRole(UserRole userRole) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(userRole);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza un rol existente.
     * @param userRole El rol a actualizar.
     * @return true si se actualizó correctamente, false en caso contrario.
     */
    public boolean updateRole(UserRole userRole) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            userRole.setUpdatedAt(new java.util.Date());
            session.update(userRole);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Desactiva un rol (soft delete).
     * @param idRole ID del rol a desactivar.
     * @return true si se desactivó correctamente, false en caso contrario.
     */
    public boolean deactivateRole(Long idRole) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserRole role = session.get(UserRole.class, idRole);
            if (role != null) {
                role.setEnabled(0);
                role.setUpdatedAt(new java.util.Date());
                session.update(role);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si existe un rol con el nombre especificado.
     * @param roleName Nombre del rol a verificar.
     * @return true si existe, false en caso contrario.
     */
    public boolean roleExists(String roleName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(*) FROM UserRole WHERE roleName = :roleName", 
                Long.class
            );
            query.setParameter("roleName", roleName);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inicializa los roles por defecto del sistema.
     * @return true si se inicializaron correctamente, false en caso contrario.
     */
    public boolean initializeDefaultRoles() {
        try {
            // Verificar si ya existen roles
            if (getAllActiveRoles().size() > 0) {
                return true; // Ya están inicializados
            }

            // Crear roles por defecto
            UserRole adminRole = new UserRole(
                UserRole.ROLE_ADMIN,
                "Administrador del sistema con acceso completo",
                "{\"can_manage_users\": true, \"can_manage_roles\": true, \"can_view_reports\": true, \"can_manage_flights\": true}"
            );

            UserRole employeeRole = new UserRole(
                UserRole.ROLE_EMPLOYEE,
                "Empleado con acceso limitado al sistema",
                "{\"can_manage_users\": false, \"can_manage_roles\": false, \"can_view_reports\": true, \"can_manage_flights\": false}"
            );

            UserRole webserviceRole = new UserRole(
                UserRole.ROLE_WEBSERVICE,
                "Servicio web para integraciones externas",
                "{\"can_manage_users\": false, \"can_manage_roles\": false, \"can_view_reports\": false, \"can_manage_flights\": true}"
            );

            UserRole registeredVisitorRole = new UserRole(
                UserRole.ROLE_REGISTERED_VISITOR,
                "Usuario visitante registrado con acceso básico",
                "{\"can_manage_users\": false, \"can_manage_roles\": false, \"can_view_reports\": false, \"can_manage_flights\": false, \"can_make_reservations\": true, \"can_view_comments\": true}"
            );

            // Guardar roles
            saveRole(adminRole);
            saveRole(employeeRole);
            saveRole(webserviceRole);
            saveRole(registeredVisitorRole);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

