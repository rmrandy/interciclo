package com.sources.app.dao;

import com.sources.app.entities.Flight;
import com.sources.app.entities.FlightLeg;
import com.sources.app.entities.City;
import com.sources.app.entities.User;
import com.sources.app.entities.Aircraft;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.sources.app.entities.FlightFare;
import java.util.HashMap;
import java.util.ArrayList;

public class FlightDAO {

    // Crear vuelo simple y opcionalmente asignar aeronave con autogeneración de inventario/tarifas
    public Flight createSimpleFlight(String flightNumber, int originCityId, int destinationCityId,
                                   String departureDate, String departureTime, String arrivalDate, String arrivalTime,
                                   BigDecimal basePrice, int availableSeats, int createdBy,
                                   Integer aircraftId) {
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

            // Asignar ID manualmente desde la secuencia
            Integer nextId = ((Number) session.createNativeQuery("SELECT SEQ_FLIGHTS.NEXTVAL FROM DUAL")
                    .getSingleResult()).intValue();
            flight.setIdFlight(nextId);

            session.persist(flight);
            session.flush();

            // Asignar aeronave y generar inventario/tarifas si se envió aircraftId
            if (aircraftId != null && aircraftId > 0) {
                Aircraft aircraft = session.get(Aircraft.class, aircraftId.longValue());
                if (aircraft == null) {
                    throw new RuntimeException("Aeronave no encontrada: " + aircraftId);
                }
                flight.setAircraft(aircraft);
                session.merge(flight);
                try {
                    generateInventoryAndFares(session, flight.getIdFlight(), aircraftId);
                } catch (Exception ex) {
                    if (isOracleTriggerInvalid(ex)) {
                        System.err.println("WARN: Inventario/tarifas no generados por trigger inválido. Continuando sin actualizar FLIGHTS. Detalle: " + ex.getMessage());
                    } else {
                        throw ex;
                    }
                }
            }
            tx.commit();
            
            return flight;
        } catch (Exception e) {
            try {
                if (tx != null && tx.getStatus() != null && tx.getStatus().canRollback()) {
                    tx.rollback();
                }
            } catch (Exception rollbackEx) {
                System.err.println("WARN: Rollback falló (posible conexión cerrada): " + rollbackEx.getMessage());
            }
            throw e;
        }
    }

    private void generateInventoryAndFares(Session session, Integer flightId, Integer aircraftId) {
        // Limpiar inventario previo
        session.createNativeQuery(
            "DELETE FROM FLIGHT_INVENTORY WHERE FLIGHT_ID = :fid"
        ).setParameter("fid", flightId).executeUpdate();

        // Insertar inventario desde configuración del avión
        session.createNativeQuery(
            "INSERT INTO FLIGHT_INVENTORY (ID_INVENTORY, FLIGHT_ID, SEAT_CATEGORY, TOTAL_SEATS, AVAILABLE_SEATS, RESERVED_SEATS, SOLD_SEATS, STATUS, CREATED_AT, UPDATED_AT) " +
            "SELECT SEQ_FLIGHT_INVENTORY.NEXTVAL, :fid, c.SEAT_CATEGORY, c.SEATS, c.SEATS, 0, 0, 'ACTIVE', SYSTIMESTAMP, SYSTIMESTAMP " +
            "FROM AIRCRAFT_SEAT_CONFIG c WHERE c.AIRCRAFT_ID = :aid AND c.SEATS > 0"
        ).setParameter("fid", flightId).setParameter("aid", aircraftId).executeUpdate();

        // Actualizar asientos disponibles globales del vuelo
        try {
            session.createNativeQuery(
                "UPDATE FLIGHTS f SET f.AVAILABLE_SEATS = (SELECT NVL(SUM(TOTAL_SEATS),0) FROM FLIGHT_INVENTORY WHERE FLIGHT_ID = :fid), " +
                "f.UPDATED_AT = TO_CHAR(SYSTIMESTAMP,'YYYY-MM-DD HH24:MI:SS') WHERE f.ID_FLIGHT = :fid"
            ).setParameter("fid", flightId).executeUpdate();
        } catch (Exception ex) {
            if (isOracleTriggerInvalid(ex)) {
                // Si existe un trigger inválido en FLIGHTS (ej. ORA-04098), continuar sin bloquear la transacción
                System.err.println("WARN: UPDATE de FLIGHTS omitido por trigger inválido. Detalle: " + ex.getMessage());
            } else {
                throw ex;
            }
        }

        // MERGE de tarifas por categoría usando multiplicadores
        session.createNativeQuery(
            "MERGE INTO FLIGHT_FARES ff " +
            "USING ( " +
            "  SELECT :fid AS FLIGHT_ID, c.SEAT_CATEGORY AS SEAT_CATEGORY, " +
            "         ROUND(f.BASE_PRICE * c.PRICE_MULTIPLIER, 2) AS BASE_PRICE " +
            "  FROM FLIGHTS f, AIRCRAFT_SEAT_CONFIG c " +
            "  WHERE f.ID_FLIGHT = :fid AND c.AIRCRAFT_ID = :aid AND c.SEATS > 0 " +
            ") s ON (ff.FLIGHT_ID = s.FLIGHT_ID AND ff.SEAT_CATEGORY = s.SEAT_CATEGORY) " +
            "WHEN MATCHED THEN UPDATE SET ff.BASE_PRICE = s.BASE_PRICE, ff.TOTAL_PRICE = s.BASE_PRICE, ff.UPDATED_AT = SYSTIMESTAMP, ff.STATUS = 'ACTIVE' " +
            "WHEN NOT MATCHED THEN INSERT (ID_FARE, FLIGHT_ID, SEAT_CATEGORY, BASE_PRICE, CURRENCY, TAXES, FEES, TOTAL_PRICE, STATUS, CREATED_AT, UPDATED_AT) " +
            "VALUES (SEQ_FLIGHT_FARES.NEXTVAL, s.FLIGHT_ID, s.SEAT_CATEGORY, s.BASE_PRICE, 'GTQ', 0, 0, s.BASE_PRICE, 'ACTIVE', SYSTIMESTAMP, SYSTIMESTAMP)"
        ).setParameter("fid", flightId).setParameter("aid", aircraftId).executeUpdate();
    }

    /**
     * Detecta si el error proviene de un trigger inválido de Oracle (ORA-04098)
     */
    private boolean isOracleTriggerInvalid(Exception ex) {
        Throwable t = ex;
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null && (msg.contains("ORA-04098") || msg.toUpperCase().contains("TRIGGER") && msg.toUpperCase().contains("INVALID"))) {
                return true;
            }
            t = t.getCause();
        }
        return false;
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

                    // Asignar ID manualmente desde la secuencia
                    Long nextFareId = ((Number) session.createNativeQuery("SELECT SEQ_FLIGHT_FARES.NEXTVAL FROM DUAL")
                            .getSingleResult()).longValue();
                    fare.setIdFare(nextFareId);

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

    /**
     * Búsqueda dinámica de vuelos con filtros opcionales.
     * - Si seatCategory está presente, los filtros de precio aplican sobre FlightFare.totalPrice
     * - Si no, los filtros de precio aplican sobre Flight.basePrice
     */
    public List<Flight> searchFlights(
            Integer originCityId,
            Integer destinationCityId,
            String departureDate,
            String returnDate,
            Integer passengers,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String seatCategory,
            Boolean nonstop
    ) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            StringBuilder hql = new StringBuilder(
                "SELECT DISTINCT f FROM Flight f " +
                "LEFT JOIN FETCH f.originCity " +
                "LEFT JOIN FETCH f.destinationCity " +
                "WHERE 1=1 " +
                "AND (f.status IS NULL OR f.status != 'CANCELLED') " // Excluir vuelos cancelados
            );

            java.util.Map<String, Object> params = new java.util.HashMap<>();

            if (originCityId != null && originCityId > 0) {
                hql.append(" AND f.originCity.idCity = :originId");
                params.put("originId", originCityId.longValue());
            }
            if (destinationCityId != null && destinationCityId > 0) {
                hql.append(" AND f.destinationCity.idCity = :destId");
                params.put("destId", destinationCityId.longValue());
            }
            if (departureDate != null && !departureDate.isBlank()) {
                hql.append(" AND f.departureDate = :depDate");
                params.put("depDate", departureDate);
            }
            // returnDate se usa en round-trip en el front; aquí no es vinculante para un único vuelo

            if (seatCategory != null && !seatCategory.isBlank()) {
                hql.append(" AND EXISTS (SELECT 1 FROM FlightFare ff WHERE ff.flight.idFlight = f.idFlight AND ff.seatCategory = :seatCat");
                params.put("seatCat", seatCategory);
                if (minPrice != null) {
                    hql.append(" AND ff.totalPrice >= :minFare");
                    params.put("minFare", minPrice);
                }
                if (maxPrice != null) {
                    hql.append(" AND ff.totalPrice <= :maxFare");
                    params.put("maxFare", maxPrice);
                }
                hql.append(")");
            } else {
                if (minPrice != null) {
                    hql.append(" AND f.basePrice >= :minPrice");
                    params.put("minPrice", minPrice);
                }
                if (maxPrice != null) {
                    hql.append(" AND f.basePrice <= :maxPrice");
                    params.put("maxPrice", maxPrice);
                }
            }

            // nonstop: en este modelo los legs no están activos; por ahora ignoramos o dejamos como futuro

            hql.append(" ORDER BY f.departureDate, f.departureTime");

            Query<Flight> query = session.createQuery(hql.toString(), Flight.class);
            for (var e : params.entrySet()) {
                query.setParameter(e.getKey(), e.getValue());
            }

            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Calcula el promedio de rating para un vuelo. Si no hay reseñas, devuelve null.
     */
    public Double getAverageRating(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT AVG(fr.rating) FROM FlightReview fr WHERE fr.flight.idFlight = :fid";
            Double avg = session.createQuery(hql, Double.class)
                    .setParameter("fid", flightId)
                    .uniqueResult();
            return avg;
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

            // Asignar ID manualmente desde la secuencia
            Integer nextId = ((Number) session.createNativeQuery("SELECT SEQ_FLIGHTS.NEXTVAL FROM DUAL")
                    .getSingleResult()).intValue();
            flight.setIdFlight(nextId);

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
            // Cargar vuelos con ciudades y escalas (excluyendo cancelados)
            Query<Flight> query = session.createQuery(
                "SELECT DISTINCT f FROM Flight f " +
                "LEFT JOIN FETCH f.originCity " +
                "LEFT JOIN FETCH f.destinationCity " +
                "WHERE (f.status IS NULL OR f.status != 'CANCELLED') " +
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
            
            // Verificar que el nuevo estado sea válido
            if (!isValidFlightStatus(newStatus)) {
                return false;
            }
            
            // Actualizar estado
            String oldStatus = flight.getStatus();
            flight.setStatus(newStatus);
            flight.setUpdatedBy(updatedBy);
            flight.setUpdatedAt(LocalDateTime.now().toString());
            
            if ("CANCELLED".equals(newStatus)) {
                flight.setCancellationDate(LocalDateTime.now().toString());
            }
            
            // Agregar entrada al log de cambios
            flight.addChangeLogEntry("Estado cambiado de " + oldStatus + " a " + newStatus, updatedBy);
            
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
                "AND (f.status IS NULL OR f.status != 'CANCELLED') " +
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
                "AND (f.status IS NULL OR f.status != 'CANCELLED') " +
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
    
    // ===== NUEVOS MÉTODOS PARA GESTIÓN COMPLETA DE VUELOS =====
    
    /**
     * Cancela un vuelo
     */
    public boolean cancelFlight(Integer flightId, String cancellationReason, Integer cancelledBy) {
        System.out.println("\n=================================================");
        System.out.println("🚀 FlightDAO.cancelFlight() - INICIANDO CANCELACIÓN");
        System.out.println("=================================================");
        System.out.println("📋 Parámetros recibidos:");
        System.out.println("   - Flight ID: " + flightId);
        System.out.println("   - Reason: " + cancellationReason);
        System.out.println("   - Cancelled by: " + cancelledBy);
        System.out.println("=================================================\n");
        
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            
            Flight flight = session.get(Flight.class, flightId);
            if (flight == null) {
                System.out.println("❌ Vuelo no encontrado con ID: " + flightId);
                return false;
            }
            
            // Verificar que el vuelo puede ser cancelado
            System.out.println("🔍 Verificando si el vuelo puede ser cancelado:");
            System.out.println("   - ID del vuelo: " + flight.getIdFlight());
            System.out.println("   - Estado actual: " + flight.getStatus());
            System.out.println("   - Fecha de salida: " + flight.getDepartureDate());
            System.out.println("   - Fecha actual: " + LocalDate.now());
            
            boolean canCancel = flight.canBeCancelled();
            System.out.println("   - ¿Puede ser cancelado?: " + canCancel);
            
            if (!canCancel) {
                System.out.println("❌ El vuelo no puede ser cancelado");
                return false;
            }
            
            System.out.println("✅ El vuelo puede ser cancelado, procediendo...");
            
            // Actualizar estado del vuelo
            flight.setStatus(Flight.STATUS_CANCELLED);
            flight.setCancellationReason(cancellationReason);
            flight.setCancelledBy(cancelledBy);
            flight.setCancellationDate(LocalDateTime.now().toString());
            
            // Agregar entrada al log de cambios
            flight.addChangeLogEntry("Vuelo cancelado: " + cancellationReason, cancelledBy);
            
            session.merge(flight);
            tx.commit();
            
            // Enviar notificaciones por correo REALES usando JavaMail
            try {
                System.out.println("\n🚀 === INICIANDO ENVÍO DE NOTIFICACIONES DE CANCELACIÓN ===");
                com.sources.app.services.RealEmailService emailService = new com.sources.app.services.RealEmailService();
                
                // Enviar a pasajeros
                boolean emailsSent = emailService.sendFlightCancellationNotifications(flightId, cancellationReason);
                
                if (emailsSent) {
                    System.out.println("✅ Notificaciones enviadas exitosamente a pasajeros");
                } else {
                    System.out.println("⚠️ Algunas notificaciones a pasajeros no se pudieron enviar");
                }
                
                // Enviar a agencias
                boolean agencyEmailsSent = emailService.sendCancellationNotificationsToAgencies(flightId, cancellationReason);
                
                if (agencyEmailsSent) {
                    System.out.println("✅ Notificaciones enviadas exitosamente a agencias");
                } else {
                    System.out.println("⚠️ Algunas notificaciones a agencias no se pudieron enviar");
                }
                
                System.out.println("🚀 === FIN DE ENVÍO DE NOTIFICACIONES ===\n");
                
            } catch (Exception emailException) {
                System.err.println("❌ Error enviando notificaciones: " + emailException.getMessage());
                emailException.printStackTrace();
                // No fallar la cancelación si hay error en el correo
            }
            
            return true;
            
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    

    
    /**
     * Actualiza un vuelo completo
     */
    public Flight updateFlight(Integer flightId, com.google.gson.JsonObject updateData) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            
            Flight flight = session.get(Flight.class, flightId);
            if (flight == null) {
                return null;
            }
            
            // Permitir modificación independientemente del estado
            
            // Actualizar campos si están presentes en updateData
            if (updateData.has("flightNumber")) {
                flight.setFlightNumber(updateData.get("flightNumber").getAsString());
            }
            
            if (updateData.has("originCityId")) {
                City originCity = session.get(City.class, updateData.get("originCityId").getAsInt());
                if (originCity != null) {
                    flight.setOriginCity(originCity);
                }
            }
            
            if (updateData.has("destinationCityId")) {
                City destinationCity = session.get(City.class, updateData.get("destinationCityId").getAsInt());
                if (destinationCity != null) {
                    flight.setDestinationCity(destinationCity);
                }
            }
            
            if (updateData.has("departureDate")) {
                flight.setDepartureDate(updateData.get("departureDate").getAsString());
            }
            
            if (updateData.has("departureTime")) {
                flight.setDepartureTime(updateData.get("departureTime").getAsString());
            }
            
            if (updateData.has("arrivalDate")) {
                flight.setArrivalDate(updateData.get("arrivalDate").getAsString());
            }
            
            if (updateData.has("arrivalTime")) {
                flight.setArrivalTime(updateData.get("arrivalTime").getAsString());
            }
            
            if (updateData.has("basePrice")) {
                flight.setBasePrice(BigDecimal.valueOf(updateData.get("basePrice").getAsDouble()));
            }
            
            if (updateData.has("availableSeats")) {
                flight.setAvailableSeats(updateData.get("availableSeats").getAsInt());
            }
            
            if (updateData.has("gate")) {
                flight.setGate(updateData.get("gate").getAsString());
            }
            
            if (updateData.has("terminal")) {
                flight.setTerminal(updateData.get("terminal").getAsString());
            }
            
            if (updateData.has("checkInStart")) {
                flight.setCheckInStart(updateData.get("checkInStart").getAsString());
            }
            
            if (updateData.has("checkInEnd")) {
                flight.setCheckInEnd(updateData.get("checkInEnd").getAsString());
            }
            
            if (updateData.has("boardingTime")) {
                flight.setBoardingTime(updateData.get("boardingTime").getAsString());
            }

            // Asignación de aeronave (y regeneración de inventario/tarifas)
            if (updateData.has("aircraftId")) {
                Integer aircraftId = updateData.get("aircraftId").isJsonNull() ? null : updateData.get("aircraftId").getAsInt();
                if (aircraftId != null && aircraftId > 0) {
                    Aircraft aircraft = session.get(Aircraft.class, aircraftId.longValue());
                    if (aircraft == null) {
                        throw new RuntimeException("Aeronave no encontrada: " + aircraftId);
                    }
                    flight.setAircraft(aircraft);
                    session.merge(flight);
                    // Generar inventario y tarifas basadas en la configuración del avión
                    generateInventoryAndFares(session, flightId, aircraftId);
                } else {
                    flight.setAircraft(null);
                }
            }
            
            // Actualizar campos de auditoría
            if (updateData.has("updatedBy")) {
                flight.setUpdatedBy(updateData.get("updatedBy").getAsInt());
            }
            
            // Agregar entrada al log de cambios
            if (updateData.has("updatedBy")) {
                flight.addChangeLogEntry("Vuelo actualizado", updateData.get("updatedBy").getAsInt());
            }
            
            session.merge(flight);
            tx.commit();
            
            return flight;
            
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Elimina un vuelo
     */
    public boolean deleteFlight(Integer flightId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            
            Flight flight = session.get(Flight.class, flightId);
            if (flight == null) {
                return false;
            }
            
            // Verificar que el vuelo puede ser eliminado
            if (!flight.isDraft()) {
                return false;
            }
            
            // Eliminar el vuelo
            session.remove(flight);
            tx.commit();
            
            return true;
            
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Valida que el estado del vuelo sea válido
     */
    private boolean isValidFlightStatus(String status) {
        return Flight.STATUS_DRAFT.equals(status) ||
               Flight.STATUS_PUBLISHED.equals(status) ||
               Flight.STATUS_CANCELLED.equals(status) ||
               Flight.STATUS_COMPLETED.equals(status);
    }
}


