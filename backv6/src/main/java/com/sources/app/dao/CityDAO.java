package com.sources.app.dao;

import com.sources.app.entities.City;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class CityDAO {

    public List<City> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM City", City.class).getResultList();
        }
    }

    public City save(City city) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(city);
            tx.commit();
            return city;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public boolean existsByNameAndCountry(String name, String country) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery(
                    "SELECT COUNT(c) FROM City c WHERE UPPER(c.name) = :name AND UPPER(c.country) = :country",
                    Long.class);
            q.setParameter("name", name.toUpperCase());
            q.setParameter("country", country.toUpperCase());
            Long count = q.uniqueResult();
            return count != null && count > 0;
        }
    }
}



