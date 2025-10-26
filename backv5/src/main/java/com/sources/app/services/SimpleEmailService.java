package com.sources.app.services;

import com.sources.app.entities.Flight;
import com.sources.app.entities.Ticket;
import com.sources.app.dao.TicketDAO;
import com.sources.app.dao.FlightDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio simplificado para notificaciones de correo (solo para pruebas)
 */
public class SimpleEmailService {
    
    private final TicketDAO ticketDAO;
    private final FlightDAO flightDAO;
    
    public SimpleEmailService() {
        this.ticketDAO = new TicketDAO();
        this.flightDAO = new FlightDAO();
    }
    
    /**
     * Simula el envío de notificaciones de cancelación de vuelo
     */
    public boolean sendFlightCancellationNotifications(Integer flightId, String cancellationReason) {
        try {
            // Obtener información del vuelo
            Flight flight = flightDAO.getFlightById(flightId);
            if (flight == null) {
                System.err.println("❌ Vuelo no encontrado: " + flightId);
                return false;
            }
            
            // Obtener todos los tickets activos para este vuelo
            List<Ticket> activeTickets = ticketDAO.getActiveTicketsByFlightId(flightId);
            
            if (activeTickets.isEmpty()) {
                System.out.println("ℹ️ No hay tickets activos para el vuelo " + flightId);
                return true; // No hay pasajeros, pero no es un error
            }
            
            System.out.println("📧 === SIMULANDO ENVÍO DE NOTIFICACIONES ===");
            System.out.println("📧 Vuelo: " + flight.getFlightNumber());
            System.out.println("📧 Motivo: " + cancellationReason);
            System.out.println("📧 Pasajeros afectados: " + activeTickets.size());
            System.out.println("📧 Fecha de cancelación: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            System.out.println("📧 ==========================================");
            
            // Simular envío a cada pasajero
            for (Ticket ticket : activeTickets) {
                System.out.println("📧 Enviando correo a: " + ticket.getPassengerEmail());
                System.out.println("📧 Pasajero: " + ticket.getPassengerFirstName() + " " + ticket.getPassengerLastName());
                System.out.println("📧 Asiento: " + ticket.getSeatNumber() + " (" + ticket.getSeatCategory() + ")");
                System.out.println("📧 Monto a reembolsar: Q" + ticket.getFare());
                System.out.println("📧 ---");
            }
            
            System.out.println("✅ Simulación de envío completada para " + activeTickets.size() + " pasajeros");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error en sendFlightCancellationNotifications: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crea el contenido del correo de cancelación (para referencia)
     */
    public String createCancellationEmailContent(Ticket ticket, Flight flight, String cancellationReason) {
        return String.format("""
            ==========================================
            CANCELACIÓN DE VUELO - AEROLINEA
            ==========================================
            
            Estimado/a %s,
            
            Lamentamos informarle que su vuelo ha sido cancelado.
            
            DETALLES DEL VUELO CANCELADO:
            • Número de Vuelo: %s
            • Ruta: %s → %s
            • Fecha: %s
            • Hora: %s
            • Asiento: %s (%s)
            • Número de Boleto: %s
            
            INFORMACIÓN DE REEMBOLSO:
            • Monto a Reembolsar: Q%s
            • Motivo de Cancelación: %s
            • Fecha de Cancelación: %s
            
            PRÓXIMOS PASOS:
            • Su reembolso será procesado en 5-7 días hábiles
            • El monto será devuelto al método de pago original
            • Contacte servicio al cliente si necesita ayuda
            
            Disculpe las molestias causadas.
            
            AeroLinea - Servicio al Cliente
            Teléfono: +502 1234-5678
            Email: servicio@aerolinea.com
            ==========================================
            """,
            ticket.getPassengerFirstName() + " " + ticket.getPassengerLastName(),
            flight.getFlightNumber(),
            flight.getOriginCity().getName(),
            flight.getDestinationCity().getName(),
            formatDate(flight.getDepartureDate()),
            flight.getDepartureTime(),
            ticket.getSeatNumber(),
            ticket.getSeatCategory(),
            ticket.getIdTicket(),
            ticket.getFare(),
            cancellationReason,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }
    
    /**
     * Formatea una fecha para mostrar
     */
    private String formatDate(String dateString) {
        try {
            if (dateString == null || dateString.isEmpty()) {
                return "N/A";
            }
            
            // Asumiendo formato YYYY-MM-DD
            LocalDateTime date = LocalDateTime.parse(dateString + "T00:00:00");
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dateString; // Devolver original si hay error
        }
    }
}

