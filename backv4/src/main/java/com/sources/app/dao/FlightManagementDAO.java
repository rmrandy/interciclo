package com.sources.app.dao;

import com.sources.app.entities.*;
import com.sources.app.util.HibernateUtil;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * DAO para gestión completa de vuelos con inventario, precios y escalas
 */
public class FlightManagementDAO {

    // ==================== CRUD DE VUELOS ====================
    
    public Flight createFlight(Flight flight) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(flight);
            transaction.commit();
            return flight;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al crear vuelo: " + e.getMessage());
        }
    }
    
    public Flight createCompleteFlightWithInventoryAndFares(
            String flightNumber, Integer originCityId, Integer destinationCityId,
            LocalDate departureDate, LocalTime departureTime,
            LocalDate arrivalDate, LocalTime arrivalTime,
            Integer createdBy,
            Map<String, Integer> seatsByCategory, // Ej: {"ECONOMY": 150, "BUSINESS": 30}
            Map<String, BigDecimal> pricesByCategory // Ej: {"ECONOMY": 850.00, "BUSINESS": 1500.00}
    ) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            // Buscar ciudades
            City originCity = session.get(City.class, originCityId.longValue());
            City destinationCity = session.get(City.class, destinationCityId.longValue());
            
            if (originCity == null || destinationCity == null) {
                throw new RuntimeException("Ciudad de origen o destino no encontrada");
            }
            
            // Calcular precio base (menor precio)
            BigDecimal basePrice = pricesByCategory.values().stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.valueOf(500));
            
            // Calcular total de asientos
            Integer totalSeats = seatsByCategory.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
            
            // Crear vuelo
            Flight flight = new Flight(flightNumber, originCity, destinationCity, 
                departureDate.toString(), departureTime.toString(), arrivalDate.toString(), arrivalTime.toString(),
                basePrice, totalSeats, createdBy);
            
            session.persist(flight);
            session.flush(); // Para obtener el ID
            
            // Crear inventario por categoría
            for (Map.Entry<String, Integer> entry : seatsByCategory.entrySet()) {
                FlightInventory inventory = new FlightInventory(flight, entry.getKey(), entry.getValue());
                session.persist(inventory);
            }
            
            // Crear precios por categoría
            for (Map.Entry<String, BigDecimal> entry : pricesByCategory.entrySet()) {
                FlightFare fare = new FlightFare(flight, entry.getKey(), entry.getValue());
                session.persist(fare);
            }
            
            transaction.commit();
            return flight;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al crear vuelo completo: " + e.getMessage());
        }
    }
    
    public Flight getFlightById(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Flight.class, flightId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Flight getFlightByNumber(String flightNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            TypedQuery<Flight> query = session.createQuery(
                "FROM Flight f WHERE f.flightNumber = :flightNumber", Flight.class);
            query.setParameter("flightNumber", flightNumber);
            List<Flight> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Flight> listFlights(String status, LocalDate fromDate, LocalDate toDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Flight f WHERE 1=1");
            
            if (status != null && !status.isEmpty()) {
                hql.append(" AND f.status = :status");
            }
            
            if (fromDate != null) {
                hql.append(" AND f.departureDate >= :fromDate");
            }
            
            if (toDate != null) {
                hql.append(" AND f.departureDate <= :toDate");
            }
            
            hql.append(" ORDER BY f.departureDate, f.departureTime");
            
            TypedQuery<Flight> query = session.createQuery(hql.toString(), Flight.class);
            
            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }
            if (fromDate != null) {
                query.setParameter("fromDate", fromDate);
            }
            if (toDate != null) {
                query.setParameter("toDate", toDate);
            }
            
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public Flight updateFlight(Flight flight, Integer updatedBy, String changeDescription) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            // Agregar entrada al log de cambios
            flight.addChangeLogEntry(changeDescription, updatedBy);
            
            session.merge(flight);
            transaction.commit();
            return flight;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar vuelo: " + e.getMessage());
        }
    }
    
    public boolean cancelFlight(Integer flightId, Integer cancelledBy, String reason) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            Flight flight = session.get(Flight.class, flightId);
            if (flight == null || !flight.canBeCancelled()) {
                return false;
            }
            
            flight.setStatus(Flight.STATUS_CANCELLED);
            flight.setCancelledBy(cancelledBy);
            flight.setCancellationReason(reason);
            flight.setCancellationDate(LocalDateTime.now().toString());
            flight.addChangeLogEntry("Vuelo cancelado: " + reason, cancelledBy);
            
            session.merge(flight);
            transaction.commit();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean publishFlight(Integer flightId, Integer publishedBy) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            Flight flight = session.get(Flight.class, flightId);
            if (flight == null || !flight.isDraft()) {
                return false;
            }
            
            flight.setStatus(Flight.STATUS_PUBLISHED);
            flight.addChangeLogEntry("Vuelo publicado", publishedBy);
            
            session.merge(flight);
            transaction.commit();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== GESTIÓN DE ESCALAS ====================
    
    public FlightLeg addFlightLeg(Integer flightId, Integer airportId, Integer legOrder,
                                  LocalDateTime arrivalTime, LocalDateTime departureTime, 
                                  Boolean aircraftChange) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            Flight flight = session.get(Flight.class, flightId);
            Aeropuerto airport = session.get(Aeropuerto.class, airportId);
            
            if (flight == null || airport == null) {
                throw new RuntimeException("Flight o Airport no encontrado");
            }
            
            // Obtener la ciudad del aeropuerto
            City city = session.get(City.class, airport.getIdCiudad());
            FlightLeg leg = new FlightLeg(flight, city, legOrder, arrivalTime.toString(), departureTime.toString(), 0, "N", null);
            leg.setAircraftChange(aircraftChange != null ? (aircraftChange ? "Y" : "N") : "N");
            
            session.persist(leg);
            transaction.commit();
            return leg;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al agregar escala: " + e.getMessage());
        }
    }
    
    public List<FlightLeg> getFlightLegs(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            TypedQuery<FlightLeg> query = session.createQuery(
                "FROM FlightLeg fl WHERE fl.flight.idFlight = :flightId ORDER BY fl.legOrder", 
                FlightLeg.class);
            query.setParameter("flightId", flightId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    // ==================== GESTIÓN DE INVENTARIO ====================
    
    public FlightInventory updateInventory(Integer flightId, String category, 
                                          Integer totalSeats, Integer reservedSeats, Integer soldSeats) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            TypedQuery<FlightInventory> query = session.createQuery(
                "FROM FlightInventory fi WHERE fi.flight.idFlight = :flightId AND fi.seatCategory = :category", 
                FlightInventory.class);
            query.setParameter("flightId", flightId);
            query.setParameter("category", category);
            
            List<FlightInventory> results = query.getResultList();
            FlightInventory inventory;
            
            if (results.isEmpty()) {
                // Crear nuevo inventario
                Flight flight = session.get(Flight.class, flightId);
                inventory = new FlightInventory(flight, category, totalSeats);
            } else {
                inventory = results.get(0);
                inventory.setTotalSeats(totalSeats);
            }
            
            if (reservedSeats != null) inventory.setReservedSeats(reservedSeats);
            if (soldSeats != null) inventory.setSoldSeats(soldSeats);
            
            session.merge(inventory);
            transaction.commit();
            return inventory;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar inventario: " + e.getMessage());
        }
    }
    
    public List<FlightInventory> getFlightInventory(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            TypedQuery<FlightInventory> query = session.createQuery(
                "FROM FlightInventory fi WHERE fi.flight.idFlight = :flightId ORDER BY fi.seatCategory", 
                FlightInventory.class);
            query.setParameter("flightId", flightId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    // ==================== GESTIÓN DE PRECIOS ====================
    
    public FlightFare updateFare(Integer flightId, String category, BigDecimal basePrice,
                                BigDecimal taxes, BigDecimal fees) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            TypedQuery<FlightFare> query = session.createQuery(
                "FROM FlightFare ff WHERE ff.flight.idFlight = :flightId AND ff.seatCategory = :category", 
                FlightFare.class);
            query.setParameter("flightId", flightId);
            query.setParameter("category", category);
            
            List<FlightFare> results = query.getResultList();
            FlightFare fare;
            
            if (results.isEmpty()) {
                // Crear nueva tarifa
                Flight flight = session.get(Flight.class, flightId);
                fare = new FlightFare(flight, category, basePrice);
            } else {
                fare = results.get(0);
                fare.setBasePrice(basePrice);
                if (taxes != null) fare.setTaxes(taxes);
                if (fees != null) fare.setFees(fees);
            }
            
            session.merge(fare);
            transaction.commit();
            return fare;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar tarifa: " + e.getMessage());
        }
    }
    
    public List<FlightFare> getFlightFares(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            TypedQuery<FlightFare> query = session.createQuery(
                "FROM FlightFare ff WHERE ff.flight.idFlight = :flightId ORDER BY ff.seatCategory", 
                FlightFare.class);
            query.setParameter("flightId", flightId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    // ==================== BÚSQUEDAS Y REPORTES ====================
    
    public List<Object[]> searchFlightsWithDetails(String originCity, String destinationCity, 
                                                   LocalDate departureDate, String seatCategory) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT f, r, a, orig, dest, inv, fare
                FROM Flight f
                JOIN f.route r
                JOIN f.aircraft a
                JOIN r.originAirport orig
                JOIN r.destinationAirport dest
                LEFT JOIN f.inventory inv ON inv.seatCategory = :seatCategory
                LEFT JOIN f.fares fare ON fare.seatCategory = :seatCategory
                WHERE f.status = 'PUBLISHED'
                AND f.departureDate = :departureDate
                AND (:originCity IS NULL OR orig.city = :originCity)
                AND (:destinationCity IS NULL OR dest.city = :destinationCity)
                AND (inv IS NULL OR inv.availableSeats > 0)
                ORDER BY f.departureTime
                """;
            
            Query query = session.createQuery(hql);
            query.setParameter("seatCategory", seatCategory != null ? seatCategory : "ECONOMY");
            query.setParameter("departureDate", departureDate);
            query.setParameter("originCity", originCity);
            query.setParameter("destinationCity", destinationCity);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            return results;
            
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public Map<String, Object> getFlightStatistics() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Map<String, Object> stats = new HashMap<>();
            
            // Total de vuelos por estado
            TypedQuery<Object[]> statusQuery = session.createQuery(
                "SELECT f.status, COUNT(f) FROM Flight f GROUP BY f.status", Object[].class);
            List<Object[]> statusResults = statusQuery.getResultList();
            
            Map<String, Long> statusCounts = new HashMap<>();
            for (Object[] result : statusResults) {
                statusCounts.put((String) result[0], (Long) result[1]);
            }
            stats.put("flightsByStatus", statusCounts);
            
            // Vuelos de hoy
            TypedQuery<Long> todayQuery = session.createQuery(
                "SELECT COUNT(f) FROM Flight f WHERE f.departureDate = :today", Long.class);
            todayQuery.setParameter("today", LocalDate.now());
            Long todayFlights = todayQuery.getSingleResult();
            stats.put("todayFlights", todayFlights);
            
            // Vuelos próximos (próximos 7 días)
            TypedQuery<Long> upcomingQuery = session.createQuery(
                "SELECT COUNT(f) FROM Flight f WHERE f.departureDate BETWEEN :today AND :nextWeek AND f.status = 'PUBLISHED'", Long.class);
            upcomingQuery.setParameter("today", LocalDate.now());
            upcomingQuery.setParameter("nextWeek", LocalDate.now().plusDays(7));
            Long upcomingFlights = upcomingQuery.getSingleResult();
            stats.put("upcomingFlights", upcomingFlights);
            
            return stats;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
