package com.sources.app.dao;

import com.sources.app.entities.Ticket;
import com.sources.app.entities.Flight;
import com.sources.app.entities.User;
// import com.sources.app.entities.FlightFare; // no usado aquí
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
// import org.hibernate.query.Query; // no usado aquí

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;

public class TicketDAO {

    // Crear un nuevo boleto
    public Ticket createTicket(Ticket ticket) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(ticket);
            tx.commit();
            return ticket;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            throw e;
        }
    }

    // Obtener boleto por ID
    public Ticket getTicketById(Long ticketId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Ticket.class, ticketId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Obtener todos los boletos de un usuario
    public List<Ticket> getTicketsByUserId(Integer userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Ticket t WHERE t.user.idUser = :userId ORDER BY t.createdAt DESC";
            return session.createQuery(hql, Ticket.class)
                .setParameter("userId", userId)
                .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Obtener todos los boletos de un vuelo
    public List<Ticket> getTicketsByFlightId(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Ticket t WHERE t.flight.idFlight = :flightId ORDER BY t.seatNumber";
            return session.createQuery(hql, Ticket.class)
                .setParameter("flightId", flightId)
                .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Obtener boletos por estado
    public List<Ticket> getTicketsByStatus(String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt DESC";
            return session.createQuery(hql, Ticket.class)
                .setParameter("status", status)
                .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Actualizar estado del boleto
    public boolean updateTicketStatus(Long ticketId, String newStatus) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Ticket ticket = session.get(Ticket.class, ticketId);
            if (ticket != null) {
                String oldStatus = ticket.getStatus();
                ticket.setStatus(newStatus);
                session.merge(ticket);

                // Ajustar inventario por transición de estado
                if (oldStatus != null && !newStatus.equals(oldStatus)) {
                    Integer flightId = ticket.getFlight().getIdFlight();
                    String seatCategory = ticket.getSeatCategory();
                    Integer quantity = ticket.getQuantity() != null ? ticket.getQuantity() : 1;

                    if ("RESERVED".equals(oldStatus) && "CONFIRMED".equals(newStatus)) {
                        moveReservedToSold(session, flightId, seatCategory, quantity);
                        recalcFlightAvailableSeats(session, flightId);
                    } else if (("RESERVED".equals(oldStatus) || "CONFIRMED".equals(oldStatus)) && "CANCELLED".equals(newStatus)) {
                        releaseInventory(session, flightId, seatCategory, quantity, "RESERVED".equals(oldStatus), "CONFIRMED".equals(oldStatus));
                        recalcFlightAvailableSeats(session, flightId);
                    }
                }
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    // Cancelar boleto
    public boolean cancelTicket(Long ticketId, String reason) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Ticket ticket = session.get(Ticket.class, ticketId);
            if (ticket != null) {
                Integer flightId = ticket.getFlight().getIdFlight();
                String seatCategory = ticket.getSeatCategory();
                Integer quantity = ticket.getQuantity() != null ? ticket.getQuantity() : 1;
                boolean wasReserved = "RESERVED".equals(ticket.getStatus());
                boolean wasConfirmed = "CONFIRMED".equals(ticket.getStatus());

                // Liberar inventario según estado actual
                releaseInventory(session, flightId, seatCategory, quantity, wasReserved, wasConfirmed);

                ticket.setStatus("CANCELLED");
                ticket.setCancellationDate(java.time.LocalDateTime.now().toString());
                ticket.setCancellationReason(reason);
                session.merge(ticket);

                recalcFlightAvailableSeats(session, flightId);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar estado de pago
    public boolean updatePaymentStatus(Long ticketId, String paymentStatus) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Ticket ticket = session.get(Ticket.class, ticketId);
            if (ticket != null) {
                ticket.setPaymentStatus(paymentStatus);
                if ("PAID".equals(paymentStatus)) {
                    Integer flightId = ticket.getFlight().getIdFlight();
                    String seatCategory = ticket.getSeatCategory();
                    Integer quantity = ticket.getQuantity() != null ? ticket.getQuantity() : 1;
                    // Confirmar inventario: pasar de reservado a vendido
                    moveReservedToSold(session, flightId, seatCategory, quantity);
                    ticket.setStatus("CONFIRMED");
                    session.merge(ticket);
                    recalcFlightAvailableSeats(session, flightId);
                } else {
                    session.merge(ticket);
                }
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    // Verificar disponibilidad de asiento
    public boolean isSeatAvailable(Integer flightId, String seatNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(t) FROM Ticket t WHERE t.flight.idFlight = :flightId AND t.seatNumber = :seatNumber AND t.status IN ('RESERVED', 'CONFIRMED')";
            Long count = session.createQuery(hql, Long.class)
                .setParameter("flightId", flightId)
                .setParameter("seatNumber", seatNumber)
                .getSingleResult();
            return count == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener asientos ocupados de un vuelo
    public List<String> getOccupiedSeats(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t.seatNumber FROM Ticket t WHERE t.flight.idFlight = :flightId AND t.status IN ('RESERVED', 'CONFIRMED')";
            return session.createQuery(hql, String.class)
                .setParameter("flightId", flightId)
                .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Obtener asientos disponibles de un vuelo
    public List<String> getAvailableSeats(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Obtener asientos ocupados
            List<String> occupiedSeats = getOccupiedSeats(flightId);
            
            // Obtener total de asientos del vuelo
            Flight flight = session.get(Flight.class, flightId);
            if (flight == null) return null;
            
            int totalSeats = flight.getAvailableSeats();
            List<String> availableSeats = new java.util.ArrayList<>();
            
            // Generar lista de asientos disponibles (A1, A2, B1, B2, etc.)
            for (char row = 'A'; row <= 'Z' && availableSeats.size() < totalSeats; row++) {
                for (int col = 1; col <= 30 && availableSeats.size() < totalSeats; col++) {
                    String seatNumber = row + String.valueOf(col);
                    if (!occupiedSeats.contains(seatNumber)) {
                        availableSeats.add(seatNumber);
                    }
                }
            }
            
            return availableSeats;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Crear boleto con validaciones e integración con inventario por categoría
    public Ticket createTicketWithValidation(Map<String, Object> ticketData) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Integer flightId = (Integer) ticketData.get("flightId");
            String seatCategory = (String) ticketData.get("seatCategory");
            Integer quantity = ticketData.get("quantity") instanceof Integer ? (Integer) ticketData.get("quantity") : 1;
            if (quantity == null || quantity < 1) quantity = 1;

            // Reservar inventario por categoría
            reserveInventorySeats(session, flightId, seatCategory, quantity);

            // Crear el boleto
            Ticket ticket = new Ticket();
            
            // Obtener entidades relacionadas
            Flight flight = session.get(Flight.class, flightId);
            User user = session.get(User.class, (Integer) ticketData.get("userId"));
            
            if (flight == null) throw new RuntimeException("Vuelo no encontrado");
            if (user == null) throw new RuntimeException("Usuario no encontrado");
            
            ticket.setFlight(flight);
            ticket.setUser(user);
            if (ticketData.get("seatNumber") != null) {
                ticket.setSeatNumber((String) ticketData.get("seatNumber"));
            }
            ticket.setSeatCategory(seatCategory);
            ticket.setQuantity(quantity);
            ticket.setFare((BigDecimal) ticketData.get("fare"));
            ticket.setPassengerFirstName((String) ticketData.get("passengerFirstName"));
            ticket.setPassengerLastName((String) ticketData.get("passengerLastName"));
            ticket.setPassengerDocumentType((String) ticketData.get("passengerDocumentType"));
            ticket.setPassengerDocumentNumber((String) ticketData.get("passengerDocumentNumber"));
            ticket.setPassengerEmail((String) ticketData.get("passengerEmail"));
            ticket.setPassengerPhone((String) ticketData.get("passengerPhone"));
            ticket.setSpecialRequests((String) ticketData.get("specialRequests"));
            ticket.setPaymentMethod((String) ticketData.get("paymentMethod"));
            ticket.setTotalAmount((BigDecimal) ticketData.get("totalAmount"));
            ticket.setTaxes((BigDecimal) ticketData.get("taxes"));
            ticket.setFees((BigDecimal) ticketData.get("fees"));
            ticket.setDiscountAmount((BigDecimal) ticketData.get("discountAmount"));
            ticket.setDiscountCode((String) ticketData.get("discountCode"));

            // No enviar timestamps desde Java: que los defina la BD
            ticket.setBookingDate(null);
            ticket.setBookingTime(null);

            // Persistir el ticket en la misma sesión
            session.persist(ticket);
            // Recalcular asientos disponibles del vuelo
            recalcFlightAvailableSeats(session, flightId);
            tx.commit();
            
            return ticket;
            
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            throw new RuntimeException("Error creando boleto: " + e.getMessage());
        }
    }

    /**
     * Reserva asientos en inventario por categoría (incrementa RESERVED_SEATS).
     */
    private void reserveInventorySeats(Session session, Integer flightId, String seatCategory, Integer quantity) {
        Object[] row = (Object[]) session.createNativeQuery(
            "SELECT AVAILABLE_SEATS, RESERVED_SEATS, SOLD_SEATS FROM FLIGHT_INVENTORY WHERE FLIGHT_ID = :fid AND SEAT_CATEGORY = :cat FOR UPDATE"
        ).setParameter("fid", flightId)
         .setParameter("cat", seatCategory)
         .uniqueResult();

        if (row == null) {
            throw new RuntimeException("Inventario no encontrado para categoría " + seatCategory);
        }

        Number available = (Number) row[0];
        Number reserved = (Number) row[1];
        if (available == null || available.intValue() < quantity) {
            throw new RuntimeException("No hay asientos disponibles en " + seatCategory);
        }

        int newReserved = (reserved != null ? reserved.intValue() : 0) + quantity;

        session.createNativeQuery(
            "UPDATE FLIGHT_INVENTORY SET RESERVED_SEATS = :newReserved WHERE FLIGHT_ID = :fid AND SEAT_CATEGORY = :cat"
        ).setParameter("newReserved", newReserved)
         .setParameter("fid", flightId)
         .setParameter("cat", seatCategory)
         .executeUpdate();
    }

    /**
     * Mueve asientos de reservado a vendido.
     */
    private void moveReservedToSold(Session session, Integer flightId, String seatCategory, Integer quantity) {
        Object[] row = (Object[]) session.createNativeQuery(
            "SELECT RESERVED_SEATS, SOLD_SEATS FROM FLIGHT_INVENTORY WHERE FLIGHT_ID = :fid AND SEAT_CATEGORY = :cat FOR UPDATE"
        ).setParameter("fid", flightId)
         .setParameter("cat", seatCategory)
         .uniqueResult();

        if (row == null) return;
        int reserved = ((Number) (row[0] != null ? row[0] : 0)).intValue();
        int sold = ((Number) (row[1] != null ? row[1] : 0)).intValue();

        int toMove = Math.min(quantity, reserved);
        session.createNativeQuery(
            "UPDATE FLIGHT_INVENTORY SET RESERVED_SEATS = :newReserved, SOLD_SEATS = :newSold WHERE FLIGHT_ID = :fid AND SEAT_CATEGORY = :cat"
        ).setParameter("newReserved", Math.max(0, reserved - toMove))
         .setParameter("newSold", sold + toMove)
         .setParameter("fid", flightId)
         .setParameter("cat", seatCategory)
         .executeUpdate();
    }

    /**
     * Libera asientos del inventario, desde reservado y/o vendido.
     */
    private void releaseInventory(Session session, Integer flightId, String seatCategory, Integer quantity, boolean fromReserved, boolean fromSold) {
        Object[] row = (Object[]) session.createNativeQuery(
            "SELECT RESERVED_SEATS, SOLD_SEATS FROM FLIGHT_INVENTORY WHERE FLIGHT_ID = :fid AND SEAT_CATEGORY = :cat FOR UPDATE"
        ).setParameter("fid", flightId)
         .setParameter("cat", seatCategory)
         .uniqueResult();

        if (row == null) return;
        int reserved = ((Number) (row[0] != null ? row[0] : 0)).intValue();
        int sold = ((Number) (row[1] != null ? row[1] : 0)).intValue();

        int newReserved = reserved;
        int newSold = sold;
        if (fromReserved) newReserved = Math.max(0, reserved - quantity);
        if (fromSold) newSold = Math.max(0, sold - quantity);

        session.createNativeQuery(
            "UPDATE FLIGHT_INVENTORY SET RESERVED_SEATS = :newReserved, SOLD_SEATS = :newSold WHERE FLIGHT_ID = :fid AND SEAT_CATEGORY = :cat"
        ).setParameter("newReserved", newReserved)
         .setParameter("newSold", newSold)
         .setParameter("fid", flightId)
         .setParameter("cat", seatCategory)
         .executeUpdate();
    }

    /**
     * Recalcula y sincroniza FLIGHTS.AVAILABLE_SEATS desde FLIGHT_INVENTORY.
     */
    private void recalcFlightAvailableSeats(Session session, Integer flightId) {
        session.createNativeQuery(
            "UPDATE FLIGHTS f SET f.AVAILABLE_SEATS = (SELECT NVL(SUM(AVAILABLE_SEATS),0) FROM FLIGHT_INVENTORY WHERE FLIGHT_ID = :fid), " +
            "f.UPDATED_AT = TO_CHAR(SYSTIMESTAMP,'YYYY-MM-DD HH24:MI:SS') WHERE f.ID_FLIGHT = :fid"
        ).setParameter("fid", flightId).executeUpdate();
    }

    // Obtener estadísticas de boletos
    public Map<String, Object> getTicketStatistics() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Map<String, Object> stats = new HashMap<>();
            
            // Total de boletos
            String totalHql = "SELECT COUNT(t) FROM Ticket t";
            Long totalTickets = session.createQuery(totalHql, Long.class).getSingleResult();
            stats.put("totalTickets", totalTickets);
            
            // Boletos por estado
            String statusHql = "SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status";
            List<Object[]> statusCounts = session.createQuery(statusHql, Object[].class).getResultList();
            Map<String, Long> statusStats = new HashMap<>();
            for (Object[] result : statusCounts) {
                statusStats.put((String) result[0], (Long) result[1]);
            }
            stats.put("statusStats", statusStats);
            
            // Ingresos totales
            String revenueHql = "SELECT SUM(t.totalAmount) FROM Ticket t WHERE t.paymentStatus = 'PAID'";
            BigDecimal totalRevenue = session.createQuery(revenueHql, BigDecimal.class).getSingleResult();
            stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            
            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    // Obtener todos los boletos
    public List<Ticket> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Ticket t ORDER BY t.createdAt DESC";
            return session.createQuery(hql, Ticket.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}


