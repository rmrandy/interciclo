package com.sources.app.dao;

import com.sources.app.entities.Flight;
import com.sources.app.entities.FlightLeg;
import com.sources.app.entities.City;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import com.sources.app.entities.FlightFare;
import java.util.HashMap;

public class FlightDAO {

    // Crear vuelo simple (sin inventario ni tarifas)
    public Flight createSimpleFlight(String flightNumber, int originCityId, int destinationCityId,
                                   String departureDate, String departureTime, String arrivalDate, String arrivalTime,
                                   BigDecimal basePrice, int availableSeats, int createdBy) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            
            // Obtener ciudades
            City originCity = session.get(City.class, originCityId);
            City destinationCity = session.get(City.class, destinationCityId);
            
            if (originCity == null || destinationCity == null) {
                throw new RuntimeException("Ciudad de origen o destino no encontrada");
            }
            
            // Crear vuelo
            Flight flight = new Flight();
            flight.setFlightNumber(flightNumber);
            flight.setOriginCity(originCity);
            flight.setDestinationCity(destinationCity);
            flight.setDepartureDate(departureDate);
            flight.setDepartureTime(departureTime);
            flight.setArrivalDate(arrivalDate);
            flight.setArrivalTime(arrivalTime);
            flight.setBasePrice(basePrice); // Este será el precio económico
            flight.setAvailableSeats(availableSeats);
            flight.setStatus("SCHEDULED");
            flight.setCreatedBy(createdBy);
            
            session.persist(flight);
            tx.commit();
            
            return flight;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            throw e;
        }
    }
    
    public void createFlightFares(Flight flight, Map<String, BigDecimal> fares) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            
            for (Map.Entry<String, BigDecimal> entry : fares.entrySet()) {
                String category = entry.getKey();
                BigDecimal price = entry.getValue();
                
                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    FlightFare fare = new FlightFare();
                    fare.setFlight(flight);
                    fare.setSeatCategory(category);
                    fare.setBasePrice(price);
                    fare.setCurrency("USD");
                    fare.setTaxes(BigDecimal.ZERO);
                    fare.setFees(BigDecimal.ZERO);
                    fare.setTotalPrice(price);
                    fare.setStatus("ACTIVE");
                    
                    session.persist(fare);
                }
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            throw e;
        }
    }
    
    public Map<String, BigDecimal> getFlightFares(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM FlightFare ff WHERE ff.flight.idFlight = :flightId AND ff.status = 'ACTIVE'";
            List<FlightFare> fares = session.createQuery(hql, FlightFare.class)
                .setParameter("flightId", flightId)
                .getResultList();
            
            Map<String, BigDecimal> faresMap = new HashMap<>();
            for (FlightFare fare : fares) {
                faresMap.put(fare.getSeatCategory(), fare.getTotalPrice());
            }
            
            return faresMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    // Crear vuelo completo con inventario y tarifas
    public Flight createCompleteFlight(String flightNumber, Integer originCityId, Integer destinationCityId,
                                     String departureDate, String departureTime, String arrivalDate, String arrivalTime,
                                     BigDecimal basePrice, Integer availableSeats, Integer createdBy) {
        Session session = null;
        Transaction tx = null;
        
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            // Verificar si el número de vuelo ya existe
            if (flightNumberExists(flightNumber)) {
                throw new RuntimeException("El número de vuelo " + flightNumber + " ya existe");
            }
            
            // Obtener las ciudades
            City originCity = session.get(City.class, originCityId);
            City destinationCity = session.get(City.class, destinationCityId);
            
            if (originCity == null || destinationCity == null) {
                throw new RuntimeException("Ciudad de origen o destino no encontrada");
            }
            
            // Crear el vuelo
            Flight flight = new Flight();
            flight.setFlightNumber(flightNumber);
            flight.setOriginCity(originCity);
            flight.setDestinationCity(destinationCity);
            flight.setDepartureDate(departureDate);
            flight.setDepartureTime(departureTime);
            flight.setArrivalDate(arrivalDate);
            flight.setArrivalTime(arrivalTime);
            flight.setBasePrice(basePrice);
            flight.setAvailableSeats(availableSeats);
            flight.setStatus("SCHEDULED");
            flight.setCreatedBy(createdBy);
            flight.setCreatedAt(LocalDateTime.now().toString());
            flight.setUpdatedAt(LocalDateTime.now().toString());
            
            session.save(flight);
            tx.commit();
            
            System.out.println("✅ Vuelo completo creado exitosamente: " + flightNumber);
            return flight;
            
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error creando vuelo completo: " + e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Obtener todos los vuelos con información de ciudades y escalas
     */
    public List<Flight> getAllFlights() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Cargar vuelos con ciudades y escalas
            Query<Flight> query = session.createQuery(
                "SELECT DISTINCT f FROM Flight f " +
                "LEFT JOIN FETCH f.originCity " +
                "LEFT JOIN FETCH f.destinationCity " +
                // "LEFT JOIN FETCH f.flightLegs fl " +
                // "LEFT JOIN FETCH fl.city " +
                "ORDER BY f.departureDate, f.departureTime", 
                Flight.class
            );
            
            List<Flight> flights = query.list();
            
            // Cargar escalas para cada vuelo si no se cargaron automáticamente
            for (Flight flight : flights) {
                // if (flight.getFlightLegs() == null || flight.getFlightLegs().isEmpty()) {
                //     FlightLegDAO flightLegDAO = new FlightLegDAO();
                //     List<FlightLeg> legs = flightLegDAO.getFlightLegsByFlightId(flight.getIdFlight());
                //     flight.setFlightLegs(legs);
                // }
            }
            
            return flights;
        } finally {
            session.close();
        }
    }

    // Obtener vuelo por ID
    public Flight getFlightById(Integer flightId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.get(Flight.class, flightId.longValue());
        } finally {
            session.close();
        }
    }

    // Actualizar estado del vuelo
    public boolean updateFlightStatus(Integer flightId, String newStatus, Integer updatedBy) {
        Session session = null;
        Transaction tx = null;
        
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            Flight flight = session.get(Flight.class, flightId);
            if (flight == null) {
                return false;
            }
            
            flight.setStatus(newStatus);
            flight.setUpdatedBy(updatedBy);
            flight.setUpdatedAt(LocalDateTime.now().toString());
            
            if ("CANCELLED".equals(newStatus)) {
                flight.setCancellationDate(LocalDateTime.now().toString());
            }
            
            session.update(flight);
            tx.commit();
            return true;
            
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Agregar una escala a un vuelo existente
     */
    public FlightLeg addFlightLeg(Integer flightId, City city, Integer legOrder, 
                                 String arrivalTime, String departureTime, 
                                 Integer connectionTimeMinutes, String aircraftChange, User createdBy) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            Flight flight = session.get(Flight.class, flightId);
            if (flight == null) {
                throw new IllegalArgumentException("Vuelo no encontrado");
            }
            
            FlightLeg flightLeg = new FlightLeg();
            flightLeg.setFlight(flight);
            flightLeg.setCity(city);
            flightLeg.setLegOrder(legOrder);
            flightLeg.setArrivalTime(arrivalTime);
            flightLeg.setDepartureTime(departureTime);
            flightLeg.setConnectionTimeMinutes(connectionTimeMinutes);
            flightLeg.setAircraftChange(aircraftChange);
            flightLeg.setCreatedBy(createdBy);
            flightLeg.setCreatedAt(java.time.LocalDateTime.now().toString());
            flightLeg.setUpdatedAt(java.time.LocalDateTime.now().toString());
            
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

    // Obtener vuelos por fecha
    public List<Flight> getFlightsByDate(LocalDate date) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<Flight> query = session.createQuery(
                "SELECT DISTINCT f FROM Flight f " +
                "LEFT JOIN FETCH f.originCity " +
                "LEFT JOIN FETCH f.destinationCity " +
                "WHERE f.departureDate = :date " +
                "ORDER BY f.departureTime", Flight.class);
            query.setParameter("date", date);
            return query.list();
        } finally {
            session.close();
        }
    }

    // Obtener vuelos por origen y destino
    public List<Flight> getFlightsByRoute(Integer originCityId, Integer destinationCityId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<Flight> query = session.createQuery(
                "SELECT DISTINCT f FROM Flight f " +
                "LEFT JOIN FETCH f.originCity " +
                "LEFT JOIN FETCH f.destinationCity " +
                "WHERE f.originCity.idCity = :originCityId " +
                "AND f.destinationCity.idCity = :destinationCityId " +
                "ORDER BY f.departureDate, f.departureTime", Flight.class);
            query.setParameter("originCityId", originCityId.longValue());
            query.setParameter("destinationCityId", destinationCityId.longValue());
            return query.list();
        } finally {
            session.close();
        }
    }

    // Verificar si el número de vuelo ya existe
    public boolean flightNumberExists(String flightNumber) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(f) FROM Flight f WHERE f.flightNumber = :flightNumber", Long.class);
            query.setParameter("flightNumber", flightNumber);
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }
}


