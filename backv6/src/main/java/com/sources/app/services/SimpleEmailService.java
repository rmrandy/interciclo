package com.sources.app.services;

import com.sources.app.entities.Flight;
import com.sources.app.entities.Ticket;
import com.sources.app.dao.TicketDAO;
import com.sources.app.dao.FlightDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

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
    
    /**
     * Envía notificaciones de cancelación de vuelo a las agencias registradas
     * Consulta la base de datos MongoDB de las agencias para obtener sus correos
     */
    public boolean sendCancellationNotificationsToAgencies(Integer flightId, String cancellationReason) {
        try {
            // Obtener información del vuelo
            Flight flight = flightDAO.getFlightById(flightId);
            if (flight == null) {
                System.err.println("❌ Vuelo no encontrado: " + flightId);
                return false;
            }
            
            System.out.println("📧 === ENVIANDO NOTIFICACIONES A AGENCIAS ===");
            System.out.println("📧 Vuelo cancelado: " + flight.getFlightNumber());
            System.out.println("📧 Ruta: " + flight.getOriginCity().getName() + " → " + flight.getDestinationCity().getName());
            System.out.println("📧 Fecha: " + flight.getDepartureDate() + " " + flight.getDepartureTime());
            System.out.println("📧 Motivo: " + cancellationReason);
            
            // Lista de correos de agencias (hardcoded por ahora, puedes consultar MongoDB)
            List<String> agencyEmails = getAgencyEmailsFromMongoDB();
            
            if (agencyEmails.isEmpty()) {
                System.out.println("ℹ️ No hay agencias registradas para notificar");
                return true;
            }
            
            System.out.println("📧 Agencias a notificar: " + agencyEmails.size());
            
            // Crear contenido del correo
            String emailContent = createAgencyCancellationEmailContent(flight, cancellationReason);
            
            // Simular envío a cada agencia
            int successCount = 0;
            for (String email : agencyEmails) {
                try {
                    System.out.println("📧 Enviando notificación a agencia: " + email);
                    
                    // AQUÍ PUEDES INTEGRAR CON UN SERVICIO REAL DE EMAIL (SendGrid, AWS SES, etc.)
                    // Por ahora solo simulamos el envío
                    System.out.println("📧 ✅ Correo enviado exitosamente a: " + email);
                    System.out.println("📧 Asunto: CANCELACIÓN DE VUELO - " + flight.getFlightNumber());
                    System.out.println("📧 ---");
                    
                    successCount++;
                } catch (Exception e) {
                    System.err.println("❌ Error enviando correo a " + email + ": " + e.getMessage());
                }
            }
            
            System.out.println("✅ Notificaciones enviadas: " + successCount + "/" + agencyEmails.size());
            System.out.println("📧 ==========================================");
            
            return successCount > 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error en sendCancellationNotificationsToAgencies: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene los correos de las agencias desde MongoDB
     * Puedes modificar esto para consultar la base de datos real
     */
    private List<String> getAgencyEmailsFromMongoDB() {
        List<String> emails = new ArrayList<>();
        
        try {
            // OPCIÓN 1: Consultar MongoDB directamente (requiere driver de MongoDB)
            // Por ahora usamos correos hardcoded como fallback
            
            // OPCIÓN 2: Consultar a través del backend de la agencia
            String agenciaBackendUrl = System.getenv("AGENCIA_BACKEND_URL");
            if (agenciaBackendUrl != null && !agenciaBackendUrl.isEmpty()) {
                emails = fetchAgencyEmailsFromBackend(agenciaBackendUrl);
            }
            
            // Si no se pudo obtener de la BD, usar emails de ejemplo
            if (emails.isEmpty()) {
                System.out.println("⚠️ Usando correos de agencias de ejemplo (configurar AGENCIA_BACKEND_URL)");
                // Aquí puedes agregar correos de agencias conocidas
                emails.add("agencia@example.com");
                emails.add("reservas@viajeselmundo.com");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo correos de agencias: " + e.getMessage());
        }
        
        return emails;
    }
    
    /**
     * Obtiene los correos de las agencias consultando el backend de la agencia
     */
    private List<String> fetchAgencyEmailsFromBackend(String backendUrl) {
        List<String> emails = new ArrayList<>();
        
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + "/api/airlines"))
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parsear JSON simple para extraer emails
                String body = response.body();
                // Aquí deberías usar un parser JSON real (Gson, Jackson, etc.)
                System.out.println("✅ Respuesta del backend de agencias recibida");
                // Por ahora solo log
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error consultando backend de agencias: " + e.getMessage());
        }
        
        return emails;
    }
    
    /**
     * Crea el contenido del correo de cancelación para las agencias
     */
    private String createAgencyCancellationEmailContent(Flight flight, String cancellationReason) {
        return String.format("""
            ==========================================
            NOTIFICACIÓN DE CANCELACIÓN DE VUELO
            PARA AGENCIAS DE VIAJES
            ==========================================
            
            Estimada Agencia de Viajes,
            
            Le informamos que el siguiente vuelo ha sido CANCELADO:
            
            DETALLES DEL VUELO CANCELADO:
            • Número de Vuelo: %s
            • Ruta: %s → %s
            • Fecha Programada: %s
            • Hora Programada: %s
            • Estado: CANCELADO
            
            MOTIVO DE LA CANCELACIÓN:
            %s
            
            FECHA Y HORA DE CANCELACIÓN:
            %s
            
            ACCIONES REQUERIDAS:
            • Este vuelo ya NO ESTÁ DISPONIBLE para nuevas reservas
            • Contactar a sus clientes con reservas existentes
            • Procesar reembolsos según políticas de la aerolínea
            • Actualizar su sistema de búsqueda de vuelos
            
            INFORMACIÓN DE CONTACTO:
            • Servicio al Cliente: +502 1234-5678
            • Email Corporativo: corporate@aerolinea.com
            • Soporte Agencias: 24/7
            
            Este vuelo ha sido removido automáticamente de:
            ✓ Sistema de búsqueda de vuelos
            ✓ Inventario disponible
            ✓ Todas las plataformas de venta
            
            Disculpe las molestias causadas.
            
            Atentamente,
            Sistema de Gestión de Vuelos - AEROLINEA4
            ==========================================
            """,
            flight.getFlightNumber(),
            flight.getOriginCity().getName(),
            flight.getDestinationCity().getName(),
            formatDate(flight.getDepartureDate()),
            flight.getDepartureTime() != null ? flight.getDepartureTime() : "N/A",
            cancellationReason,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
    }
}

