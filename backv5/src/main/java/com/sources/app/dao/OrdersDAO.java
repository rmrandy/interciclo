package com.sources.app.dao;

import com.sources.app.entities.Orders;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Orders}.
 * Esta clase proporciona métodos para realizar operaciones CRUD en Pedidos (Orders),
 * que representan pedidos de clientes en el sistema. Utiliza Hibernate para interacciones con la base de datos.
 */
public class OrdersDAO {

    /**
     * Crea un nuevo Pedido (Order) en la base de datos, asociándolo a un Usuario (User) existente.
     *
     * @param status El estado inicial del pedido (p. ej., "PENDIENTE", "PROCESANDO").
     * @param idUser El ID del {@link User} que realiza el pedido.
     * @return La entidad {@link Orders} recién creada, o null si el usuario no existe o ocurre un error.
     */
    public Orders create(String status, Long idUser) {
        Transaction tx = null;
        Orders order = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            order = new Orders();
            order.setStatus(status);
            Date now = new Date();
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            User user = session.get(User.class, idUser);
            order.setUser(user);

            session.save(order);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return order;
    }

    /**
     * Recupera todos los registros de Pedido (Order) de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Orders}, o null si ocurrió un error.
     */
    public List<Orders> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Orders> query = session.createQuery("FROM Orders", Orders.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera un registro de Pedido (Order) específico por su identificador único.
     *
     * @param id El ID del Pedido (Order) a recuperar.
     * @return La entidad {@link Orders} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Orders getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Orders.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro de Pedido (Order) existente en la base de datos.
     *
     * @param order La entidad {@link Orders} con información actualizada. El ID debe coincidir con un registro existente.
     * @return La entidad {@link Orders} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Orders update(Orders order) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            order.setUpdatedAt(new Date());
            session.update(order);
            tx.commit();
            return order;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza el estado de un pedido específico.
     *
     * @param orderId El ID del pedido a actualizar.
     * @param newStatus El nuevo estado del pedido.
     * @return La entidad {@link Orders} actualizada, o null si la actualización falló.
     */
    public Orders updateStatus(Long orderId, String newStatus) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            
            // Deshabilitar el trigger problemático temporalmente
            session.createNativeQuery("ALTER TRIGGER FARMACIA.UPDATE_STOCK_ON_ORDER_COMPLETION DISABLE").executeUpdate();
            
            // Usar consulta SQL directa para actualizar solo el estado
            String updateQuery = "UPDATE ORDERS SET STATUS = :status, UPDATED_AT = :updatedAt WHERE ID_ORDER = :orderId";
            Query<?> query = session.createNativeQuery(updateQuery);
            query.setParameter("status", newStatus);
            query.setParameter("updatedAt", new Date());
            query.setParameter("orderId", orderId);
            
            int updatedRows = query.executeUpdate();
            
            if (updatedRows == 0) {
                System.out.println("Order not found with ID: " + orderId);
                if (tx != null) tx.rollback();
                return null;
            }
            
            // Rehabilitar el trigger
            session.createNativeQuery("ALTER TRIGGER FARMACIA.UPDATE_STOCK_ON_ORDER_COMPLETION ENABLE").executeUpdate();
            
            tx.commit();
            System.out.println("Order status updated successfully to: " + newStatus);
            
            // Retornar el pedido actualizado
            return session.get(Orders.class, orderId);
            
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error updating order status: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera pedidos filtrados por estado.
     *
     * @param status El estado por el cual filtrar los pedidos.
     * @return Una lista de entidades {@link Orders} con el estado especificado, o null si ocurrió un error.
     */
    public List<Orders> getByStatus(String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Orders> query = session.createQuery("FROM Orders WHERE status = :status", Orders.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera pedidos filtrados por usuario.
     *
     * @param userId El ID del usuario por el cual filtrar los pedidos.
     * @return Una lista de entidades {@link Orders} del usuario especificado, o null si ocurrió un error.
     */
    public List<Orders> getByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Orders> query = session.createQuery("FROM Orders WHERE user.idUser = :userId", Orders.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera pedidos filtrados por usuario y estado.
     *
     * @param userId El ID del usuario por el cual filtrar los pedidos.
     * @param status El estado por el cual filtrar los pedidos.
     * @return Una lista de entidades {@link Orders} del usuario y estado especificados, o null si ocurrió un error.
     */
    public List<Orders> getByUserIdAndStatus(Long userId, String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Orders> query = session.createQuery("FROM Orders WHERE user.idUser = :userId AND status = :status", Orders.class);
            query.setParameter("userId", userId);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
