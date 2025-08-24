package com.sources.app.dao;

import com.sources.app.entities.FlightLeg;
import com.sources.app.entities.Flight;
import com.sources.app.entities.City;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class FlightLegDAO {
    
    /**
     * Crear una nueva escala para un vuelo
     */
    public FlightLeg createFlightLeg(FlightLeg flightLeg) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            session.persist(flightLeg);
            transaction.commit();
            return flightLeg;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }
    
    /**
     * Obtener todas las escalas de un vuelo específico
     */
    public List<FlightLeg> getFlightLegsByFlightId(Integer flightId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<FlightLeg> query = session.createQuery(
                "FROM FlightLeg fl WHERE fl.flight.idFlight = :flightId ORDER BY fl.legOrder", 
                FlightLeg.class
            );
            query.setParameter("flightId", flightId);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    /**
     * Obtener una escala específica por ID
     */
    public FlightLeg getFlightLegById(Long legId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.get(FlightLeg.class, legId);
        } finally {
            session.close();
        }
    }
    
    /**
     * Actualizar una escala existente
     */
    public FlightLeg updateFlightLeg(FlightLeg flightLeg) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            session.merge(flightLeg);
            transaction.commit();
            return flightLeg;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }
    
    /**
     * Eliminar una escala
     */
    public boolean deleteFlightLeg(Long legId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            FlightLeg flightLeg = session.get(FlightLeg.class, legId);
            if (flightLeg != null) {
                session.remove(flightLeg);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }
    
    /**
     * Verificar si un vuelo ya tiene escalas
     */
    public boolean flightHasLegs(Integer flightId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(fl) FROM FlightLeg fl WHERE fl.flight.idFlight = :flightId", 
                Long.class
            );
            query.setParameter("flightId", flightId);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }
    
    /**
     * Obtener el número máximo de escalas para un vuelo
     */
    public Integer getMaxLegOrderForFlight(Integer flightId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<Integer> query = session.createQuery(
                "SELECT MAX(fl.legOrder) FROM FlightLeg fl WHERE fl.flight.idFlight = :flightId", 
                Integer.class
            );
            query.setParameter("flightId", flightId);
            Integer maxOrder = query.uniqueResult();
            return maxOrder != null ? maxOrder : 0;
        } finally {
            session.close();
        }
    }
    
    /**
     * Crear escalas para un vuelo (máximo 1 escala)
     */
    public boolean createFlightWithLegs(Flight flight, City stopoverCity, String stopoverArrivalTime, 
                                      String stopoverDepartureTime, Integer connectionTimeMinutes, 
                                      String aircraftChange, User createdBy) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // Verificar que el vuelo no tenga escalas ya
            if (flightHasLegs(flight.getIdFlight())) {
                throw new IllegalStateException("El vuelo ya tiene escalas configuradas");
            }
            
            // Crear primera escala (ciudad intermedia)
            FlightLeg firstLeg = new FlightLeg(
                flight, stopoverCity, 1, 
                stopoverArrivalTime, stopoverDepartureTime, 
                connectionTimeMinutes, aircraftChange, createdBy
            );
            session.persist(firstLeg);
            
            // Crear segunda escala (destino final)
            FlightLeg finalLeg = new FlightLeg(
                flight, flight.getDestinationCity(), 2, 
                stopoverDepartureTime, flight.getArrivalTime(), 
                0, "N", createdBy
            );
            session.persist(finalLeg);
            
            transaction.commit();
            return true;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }
}
