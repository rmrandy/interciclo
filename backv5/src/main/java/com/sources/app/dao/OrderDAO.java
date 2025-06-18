package com.sources.app.dao;

import com.sources.app.entities.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class OrderDAO {
    private final EntityManagerFactory emf;
    private final EntityManager em;

    public OrderDAO() {
        this.emf = Persistence.createEntityManagerFactory("default");
        this.em = emf.createEntityManager();
    }

    public Order create(Order order) {
        try {
            em.getTransaction().begin();
            em.persist(order);
            em.getTransaction().commit();
            return order;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    public List<Order> getAll() {
        return em.createQuery("SELECT o FROM Order o", Order.class).getResultList();
    }

    public Order getById(Long id) {
        return em.find(Order.class, id);
    }

    public void close() {
        em.close();
        emf.close();
    }
} 