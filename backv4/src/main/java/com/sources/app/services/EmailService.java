package com.sources.app.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sources.app.entities.Flight;
import com.sources.app.entities.Ticket;
import com.sources.app.entities.User;
import com.sources.app.dao.TicketDAO;
import com.sources.app.dao.FlightDAO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio para envío de correos electrónicos
 */
public class EmailService {
    
    private static final String EMAIL_API_URL = "https://api.emailjs.com/api/v1.0/email/send";
    private static final String SERVICE_ID = "service_airline_notifications";
    private static final String TEMPLATE_ID = "template_flight_cancellation";
    private static final String PUBLIC_KEY = "YOUR_EMAILJS_PUBLIC_KEY"; // Reemplazar con tu clave real
    
    private final TicketDAO ticketDAO;
    private final FlightDAO flightDAO;
    private final HttpClient httpClient;
    private final Gson gson;
    
    public EmailService() {
        this.ticketDAO = new TicketDAO();
        this.flightDAO = new FlightDAO();
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
    
    /**
     * Envía notificaciones de cancelación de vuelo a todos los pasajeros
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
            
            System.out.println("📧 Enviando notificaciones a " + activeTickets.size() + " pasajeros...");
            
            int successCount = 0;
            int errorCount = 0;
            
            // Enviar correo a cada pasajero
            for (Ticket ticket : activeTickets) {
                try {
                    boolean sent = sendCancellationEmail(ticket, flight, cancellationReason);
                    if (sent) {
                        successCount++;
                        System.out.println("✅ Correo enviado a: " + ticket.getPassengerEmail());
                    } else {
                        errorCount++;
                        System.err.println("❌ Error enviando correo a: " + ticket.getPassengerEmail());
                    }
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("❌ Excepción enviando correo a " + ticket.getPassengerEmail() + ": " + e.getMessage());
                }
            }
            
            System.out.println("📊 Resumen de envío: " + successCount + " exitosos, " + errorCount + " errores");
            
            // Considerar exitoso si al menos la mayoría de correos se enviaron
            return successCount > errorCount;
            
        } catch (Exception e) {
            System.err.println("❌ Error en sendFlightCancellationNotifications: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envía un correo individual de cancelación
     */
    private boolean sendCancellationEmail(Ticket ticket, Flight flight, String cancellationReason) {
        try {
            // Preparar datos del correo
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("to_email", ticket.getPassengerEmail());
            templateParams.put("passenger_name", ticket.getPassengerFirstName() + " " + ticket.getPassengerLastName());
            templateParams.put("flight_number", flight.getFlightNumber());
            templateParams.put("origin_city", flight.getOriginCity().getName());
            templateParams.put("destination_city", flight.getDestinationCity().getName());
            templateParams.put("departure_date", formatDate(flight.getDepartureDate()));
            templateParams.put("departure_time", flight.getDepartureTime());
            templateParams.put("cancellation_reason", cancellationReason);
            templateParams.put("ticket_number", ticket.getIdTicket().toString());
            templateParams.put("seat_number", ticket.getSeatNumber());
            templateParams.put("seat_category", ticket.getSeatCategory());
            templateParams.put("refund_amount", ticket.getFare().toString());
            templateParams.put("cancellation_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            // Crear el payload para EmailJS
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("service_id", SERVICE_ID);
            emailData.put("template_id", TEMPLATE_ID);
            emailData.put("user_id", PUBLIC_KEY);
            emailData.put("template_params", templateParams);
            
            // Enviar la petición HTTP
            String jsonPayload = gson.toJson(emailData);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EMAIL_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println("✅ Correo enviado exitosamente a " + ticket.getPassengerEmail());
                return true;
            } else {
                System.err.println("❌ Error enviando correo. Status: " + response.statusCode() + ", Body: " + response.body());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Excepción enviando correo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envía correo usando un servicio SMTP local (alternativa a EmailJS)
     */
    public boolean sendCancellationEmailSMTP(Ticket ticket, Flight flight, String cancellationReason) {
        try {
            // Esta es una implementación alternativa usando JavaMail
            // Por ahora, simularemos el envío exitoso
            System.out.println("📧 [SMTP] Enviando correo de cancelación a: " + ticket.getPassengerEmail());
            System.out.println("📧 [SMTP] Asunto: Cancelación de vuelo " + flight.getFlightNumber());
            System.out.println("📧 [SMTP] Pasajero: " + ticket.getPassengerFirstName() + " " + ticket.getPassengerLastName());
            
            // Aquí iría la implementación real con JavaMail
            // Por ahora, simulamos éxito
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error en sendCancellationEmailSMTP: " + e.getMessage());
            return false;
        }
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
     * Crea el contenido HTML del correo de cancelación
     */
    private String createCancellationEmailHTML(Ticket ticket, Flight flight, String cancellationReason) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Cancelación de Vuelo</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #e74c3c; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .flight-info { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #e74c3c; }
                    .refund-info { background: #e8f5e8; padding: 15px; margin: 15px 0; border-radius: 5px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✈️ Cancelación de Vuelo</h1>
                        <p>Estimado/a %s</p>
                    </div>
                    
                    <div class="content">
                        <p>Lamentamos informarle que su vuelo ha sido cancelado debido a circunstancias imprevistas.</p>
                        
                        <div class="flight-info">
                            <h3>Detalles del Vuelo Cancelado:</h3>
                            <p><strong>Número de Vuelo:</strong> %s</p>
                            <p><strong>Ruta:</strong> %s → %s</p>
                            <p><strong>Fecha:</strong> %s</p>
                            <p><strong>Hora:</strong> %s</p>
                            <p><strong>Asiento:</strong> %s (%s)</p>
                            <p><strong>Número de Boleto:</strong> %s</p>
                        </div>
                        
                        <div class="refund-info">
                            <h3>💰 Información de Reembolso:</h3>
                            <p><strong>Monto a Reembolsar:</strong> Q%s</p>
                            <p><strong>Motivo de Cancelación:</strong> %s</p>
                            <p><strong>Fecha de Cancelación:</strong> %s</p>
                        </div>
                        
                        <h3>📞 Próximos Pasos:</h3>
                        <ul>
                            <li>Su reembolso será procesado automáticamente en un plazo de 5-7 días hábiles</li>
                            <li>El monto será devuelto al método de pago original</li>
                            <li>Si necesita ayuda, contacte a nuestro servicio al cliente</li>
                        </ul>
                        
                        <p>Disculpe las molestias causadas y esperamos poder servirle en el futuro.</p>
                    </div>
                    
                    <div class="footer">
                        <p>AeroLinea - Servicio al Cliente</p>
                        <p>Teléfono: +502 1234-5678 | Email: servicio@aerolinea.com</p>
                    </div>
                </div>
            </body>
            </html>
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
}

