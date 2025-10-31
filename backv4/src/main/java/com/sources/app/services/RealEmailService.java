package com.sources.app.services;

import com.sources.app.entities.Flight;
import com.sources.app.entities.Ticket;
import com.sources.app.dao.TicketDAO;
import com.sources.app.dao.FlightDAO;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Servicio REAL para envío de correos usando JavaMail y configuración de mail.properties
 */
public class RealEmailService {
    
    private final TicketDAO ticketDAO;
    private final FlightDAO flightDAO;
    private final Properties mailConfig;
    
    public RealEmailService() {
        this.ticketDAO = new TicketDAO();
        this.flightDAO = new FlightDAO();
        this.mailConfig = loadMailProperties();
    }
    
    /**
     * Carga la configuración de correo desde mail.properties
     */
    private Properties loadMailProperties() {
        Properties props = new Properties();
        try {
            // Intentar cargar desde el classpath
            InputStream input = getClass().getClassLoader().getResourceAsStream("mail.properties");
            if (input != null) {
                props.load(input);
                System.out.println("✅ Configuración de correo cargada desde classpath");
            } else {
                // Fallback: intentar cargar desde archivo directo
                try (FileInputStream fileInput = new FileInputStream("src/main/resources/mail.properties")) {
                    props.load(fileInput);
                    System.out.println("✅ Configuración de correo cargada desde archivo");
                } catch (Exception e) {
                    System.err.println("⚠️ No se pudo cargar mail.properties, usando valores por defecto");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando mail.properties: " + e.getMessage());
        }
        return props;
    }
    
    /**
     * Crea una sesión de correo con autenticación
     */
    private Session createMailSession() {
        String host = mailConfig.getProperty("mail.smtp.host", "smtp.gmail.com");
        String port = mailConfig.getProperty("mail.smtp.port", "587");
        String auth = mailConfig.getProperty("mail.smtp.auth", "true");
        String starttls = mailConfig.getProperty("mail.smtp.starttls.enable", "true");
        
        String username = mailConfig.getProperty("mail.sender.email");
        String password = mailConfig.getProperty("mail.sender.password");
        
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", host);
        
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
    
    /**
     * Envía un correo electrónico en formato HTML
     */
    private boolean sendEmail(String to, String subject, String htmlContent) {
        try {
            String fromEmail = mailConfig.getProperty("mail.sender.email");
            if (fromEmail == null || fromEmail.isEmpty()) {
                System.err.println("❌ Email del remitente no configurado en mail.properties");
                return false;
            }
            
            System.out.println("📧 Preparando correo para: " + to);
            System.out.println("   Asunto: " + subject);
            
            Session session = createMailSession();
            Message message = new MimeMessage(session);
            
            message.setFrom(new InternetAddress(fromEmail, "AEROLINEA2 - Sistema de Notificaciones"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            
            // Enviar como HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            System.out.println("📧 Enviando correo...");
            Transport.send(message);
            
            System.out.println("📧 ✅ Correo enviado exitosamente a: " + to);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error enviando correo a " + to + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envía notificaciones de cancelación de vuelo a los pasajeros
     */
    public boolean sendFlightCancellationNotifications(Integer flightId, String cancellationReason) {
        try {
            System.out.println("\n📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A PASAJEROS ===");
            
            // Obtener información del vuelo
            Flight flight = flightDAO.getFlightById(flightId);
            if (flight == null) {
                System.err.println("❌ Vuelo no encontrado: " + flightId);
                return false;
            }
            
            // Obtener todos los tickets activos para este vuelo
            List<Ticket> activeTickets = ticketDAO.getActiveTicketsByFlightId(flightId);
            
            if (activeTickets.isEmpty()) {
                System.out.println("ℹ️ No hay pasajeros con reservas en el vuelo " + flight.getFlightNumber());
                return true;
            }
            
            System.out.println("📧 Vuelo: " + flight.getFlightNumber());
            System.out.println("📧 Ruta: " + flight.getOriginCity().getName() + " → " + flight.getDestinationCity().getName());
            System.out.println("📧 Motivo: " + cancellationReason);
            System.out.println("📧 Pasajeros a notificar: " + activeTickets.size());
            
            int successCount = 0;
            for (Ticket ticket : activeTickets) {
                String email = ticket.getPassengerEmail();
                if (email == null || email.isEmpty()) {
                    System.out.println("⚠️ Pasajero sin email: " + ticket.getPassengerFirstName() + " " + ticket.getPassengerLastName());
                    continue;
                }
                
                String subject = "CANCELACIÓN DE VUELO - " + flight.getFlightNumber();
                String content = createPassengerCancellationEmailContent(ticket, flight, cancellationReason);
                
                if (sendEmail(email, subject, content)) {
                    successCount++;
                }
            }
            
            System.out.println("✅ Correos enviados a pasajeros: " + successCount + "/" + activeTickets.size());
            System.out.println("📧 ==========================================\n");
            
            return successCount > 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error en sendFlightCancellationNotifications: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envía notificaciones de cancelación de vuelo a las agencias
     */
    public boolean sendCancellationNotificationsToAgencies(Integer flightId, String cancellationReason) {
        try {
            System.out.println("\n📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A AGENCIAS ===");
            
            // Obtener información del vuelo
            Flight flight = flightDAO.getFlightById(flightId);
            if (flight == null) {
                System.err.println("❌ Vuelo no encontrado: " + flightId);
                return false;
            }
            
            System.out.println("📧 Vuelo cancelado: " + flight.getFlightNumber());
            System.out.println("📧 Ruta: " + flight.getOriginCity().getName() + " → " + flight.getDestinationCity().getName());
            System.out.println("📧 Fecha: " + flight.getDepartureDate() + " " + flight.getDepartureTime());
            System.out.println("📧 Motivo: " + cancellationReason);
            
            // 1. ENVIAR WEBHOOK A LA AGENCIA (para crear notificación en dashboard)
            try {
                System.out.println("\n🔗 Enviando webhook a la agencia...");
                boolean webhookSent = sendWebhookToAgency(flight, cancellationReason);
                if (webhookSent) {
                    System.out.println("✅ Webhook enviado exitosamente - Notificación creada en el dashboard de la agencia");
                } else {
                    System.out.println("⚠️ No se pudo enviar webhook a la agencia");
                }
            } catch (Exception webhookException) {
                System.err.println("⚠️ Error enviando webhook: " + webhookException.getMessage());
            }
            
            // 2. ENVIAR CORREOS ELECTRÓNICOS
            List<String> agencyEmails = getAgencyEmails();
            
            if (agencyEmails.isEmpty()) {
                System.out.println("⚠️ No hay correos de agencias configurados");
                System.out.println("💡 Configura los correos en RealEmailService.getAgencyEmails()");
                return true;
            }
            
            System.out.println("\n📧 Agencias a notificar por correo: " + agencyEmails.size());
            
            String subject = "CANCELACIÓN DE VUELO - " + flight.getFlightNumber() + " (NOTIFICACIÓN PARA AGENCIAS)";
            String content = createAgencyCancellationEmailContent(flight, cancellationReason);
            
            int successCount = 0;
            for (String email : agencyEmails) {
                if (sendEmail(email, subject, content)) {
                    successCount++;
                }
            }
            
            System.out.println("✅ Correos enviados a agencias: " + successCount + "/" + agencyEmails.size());
            System.out.println("📧 ==========================================\n");
            
            return successCount > 0;
            
        } catch (Exception e) {
            System.err.println("❌ Error en sendCancellationNotificationsToAgencies: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envía webhook a la agencia para crear notificación en el dashboard
     */
    private boolean sendWebhookToAgency(Flight flight, String cancellationReason) {
        try {
            // URL del webhook de la agencia (configurable por variable de entorno)
            String agenciaWebhookUrl = System.getenv("AGENCIA_WEBHOOK_URL");
            if (agenciaWebhookUrl == null || agenciaWebhookUrl.isEmpty()) {
                agenciaWebhookUrl = "http://localhost:5001/api/flight-cancellations";
                System.out.println("💡 Usando URL por defecto: " + agenciaWebhookUrl);
                System.out.println("   (Configura AGENCIA_WEBHOOK_URL para cambiar)");
            }
            
            // Construir el payload JSON
            String jsonPayload = String.format("""
                {
                    "flightId": %d,
                    "flightNumber": "%s",
                    "airlineCode": "AEROLINEA2",
                    "airlineName": "Aerolínea Nacional 2",
                    "originCity": "%s",
                    "destinationCity": "%s",
                    "departureDate": "%s",
                    "departureTime": "%s",
                    "cancellationReason": "%s",
                    "cancelledBy": %d,
                    "cancelledAt": "%s"
                }
                """,
                flight.getIdFlight(),
                flight.getFlightNumber(),
                flight.getOriginCity().getName(),
                flight.getDestinationCity().getName(),
                flight.getDepartureDate() != null ? flight.getDepartureDate() : "",
                flight.getDepartureTime() != null ? flight.getDepartureTime() : "",
                cancellationReason.replace("\"", "\\\""),
                flight.getCancelledBy() != null ? flight.getCancelledBy() : 0,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            System.out.println("📤 Enviando a: " + agenciaWebhookUrl);
            
            // Crear cliente HTTP
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(agenciaWebhookUrl))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(java.time.Duration.ofSeconds(5))
                .build();
            
            java.net.http.HttpResponse<String> response = client.send(
                request, 
                java.net.http.HttpResponse.BodyHandlers.ofString()
            );
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✅ Webhook enviado exitosamente (HTTP " + response.statusCode() + ")");
                return true;
            } else {
                System.err.println("⚠️ Webhook falló (HTTP " + response.statusCode() + ")");
                return false;
            }
            
        } catch (java.net.ConnectException e) {
            System.err.println("⚠️ No se pudo conectar con la agencia (¿está el backend corriendo?)");
            return false;
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando webhook a la agencia: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene los correos de las agencias
     * CONFIGURA AQUÍ LOS CORREOS DE TUS AGENCIAS
     */
    private List<String> getAgencyEmails() {
        List<String> emails = new ArrayList<>();
        
        // TODO: AGREGAR LOS CORREOS DE TUS AGENCIAS AQUÍ
        emails.add("rr36693904@gmail.com");  // Email de prueba (el mismo configurado)
        
        // Puedes agregar más correos:
        // emails.add("agencia1@example.com");
        // emails.add("agencia2@example.com");
        
        return emails;
    }
    
    /**
     * Crea el contenido del correo de cancelación para pasajeros en formato HTML
     */
    private String createPassengerCancellationEmailContent(Ticket ticket, Flight flight, String cancellationReason) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
                    .container { background: #f9fafb; padding: 20px; border-radius: 8px; }
                    .header { background: #dc2626; color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }
                    .content { background: white; padding: 30px; border-radius: 0 0 8px 8px; }
                    .info-box { background: #fef2f2; border-left: 4px solid #dc2626; padding: 15px; margin: 20px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e5e7eb; }
                    .label { font-weight: bold; color: #666; }
                    .value { color: #111; }
                    .steps { background: #f0f9ff; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                    h1 { margin: 0; font-size: 24px; }
                    h2 { color: #dc2626; font-size: 18px; margin-top: 0; }
                    ul { margin: 10px 0; padding-left: 20px; }
                    li { margin: 8px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✈️ CANCELACIÓN DE VUELO</h1>
                        <p style="margin: 5px 0 0 0; font-size: 14px;">AEROLINEA2</p>
                    </div>
                    <div class="content">
                        <p>Estimado/a <strong>%s %s</strong>,</p>
                        
                        <div class="info-box">
                            <h2>⚠️ Su vuelo ha sido CANCELADO</h2>
                            <p style="margin: 5px 0 0 0;">Lamentamos informarle que su vuelo no podrá realizarse como estaba programado.</p>
                        </div>
                        
                        <h3 style="color: #dc2626; margin-top: 25px;">Detalles del Vuelo Cancelado</h3>
                        <div class="detail-row">
                            <span class="label">Número de Vuelo:</span>
                            <span class="value"><strong>%s</strong></span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Ruta:</span>
                            <span class="value">%s → %s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Fecha:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Hora:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Asiento:</span>
                            <span class="value">%s (%s)</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Número de Boleto:</span>
                            <span class="value">#%s</span>
                        </div>
                        
                        <h3 style="color: #059669; margin-top: 25px;">💰 Información de Reembolso</h3>
                        <div class="detail-row">
                            <span class="label">Monto a Reembolsar:</span>
                            <span class="value"><strong style="color: #059669; font-size: 18px;">Q%.2f</strong></span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Motivo de Cancelación:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row" style="border-bottom: none;">
                            <span class="label">Fecha de Cancelación:</span>
                            <span class="value">%s</span>
                        </div>
                        
                        <div class="steps">
                            <h3 style="margin-top: 0; color: #0369a1;">📋 Próximos Pasos</h3>
                            <ul>
                                <li>Su reembolso será procesado en <strong>5-7 días hábiles</strong></li>
                                <li>El monto será devuelto al método de pago original</li>
                                <li>Recibirá un correo de confirmación del reembolso</li>
                                <li>Puede contactar servicio al cliente si necesita ayuda</li>
                            </ul>
                        </div>
                        
                        <h3 style="color: #0369a1; margin-top: 25px;">📞 Información de Contacto</h3>
                        <p style="margin: 10px 0;">
                            <strong>Servicio al Cliente:</strong> +502 1234-5678<br>
                            <strong>Email:</strong> servicio@aerolinea.com<br>
                            <strong>Horario:</strong> 24/7
                        </p>
                        
                        <p style="margin-top: 25px; font-style: italic; color: #666;">
                            Disculpe las molestias causadas.
                        </p>
                        
                        <p style="margin-top: 15px;">
                            Atentamente,<br>
                            <strong>AEROLINEA2</strong><br>
                            Sistema de Gestión de Vuelos
                        </p>
                        
                        <div class="footer">
                            <p>Este es un correo automático, por favor no responda.</p>
                            <p style="margin: 5px 0;">&copy; 2024 AEROLINEA2. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """,
            ticket.getPassengerFirstName(),
            ticket.getPassengerLastName(),
            flight.getFlightNumber(),
            flight.getOriginCity().getName(),
            flight.getDestinationCity().getName(),
            formatDate(flight.getDepartureDate()),
            flight.getDepartureTime() != null ? flight.getDepartureTime() : "N/A",
            ticket.getSeatNumber(),
            ticket.getSeatCategory(),
            ticket.getIdTicket(),
            ticket.getFare() != null ? ticket.getFare().doubleValue() : 0.0,
            cancellationReason,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
    }
    
    /**
     * Crea el contenido del correo de cancelación para agencias en formato HTML
     */
    private String createAgencyCancellationEmailContent(Flight flight, String cancellationReason) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
                    .container { background: #f9fafb; padding: 20px; border-radius: 8px; }
                    .header { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 25px; border-radius: 8px 8px 0 0; text-align: center; }
                    .content { background: white; padding: 30px; border-radius: 0 0 8px 8px; }
                    .alert-box { background: #fef2f2; border: 2px solid #dc2626; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #e5e7eb; }
                    .label { font-weight: bold; color: #666; }
                    .value { color: #111; }
                    .action-box { background: #fffbeb; border-left: 4px solid #f59e0b; padding: 20px; margin: 20px 0; border-radius: 4px; }
                    .success-box { background: #ecfdf5; border-left: 4px solid #059669; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; padding-top: 20px; border-top: 2px solid #e5e7eb; }
                    h1 { margin: 0; font-size: 26px; }
                    h2 { color: #dc2626; font-size: 18px; margin-top: 0; }
                    h3 { color: #0369a1; margin-top: 20px; margin-bottom: 10px; }
                    ul { margin: 10px 0; padding-left: 20px; }
                    li { margin: 10px 0; }
                    .badge { background: #dc2626; color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; display: inline-block; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚨 NOTIFICACIÓN DE CANCELACIÓN DE VUELO</h1>
                        <p style="margin: 10px 0 0 0; font-size: 16px; font-weight: bold;">PARA AGENCIAS DE VIAJES</p>
                        <p style="margin: 5px 0 0 0; font-size: 14px;">AEROLINEA2</p>
                    </div>
                    <div class="content">
                        <p style="font-size: 16px;">Estimada <strong>Agencia de Viajes</strong>,</p>
                        
                        <div class="alert-box">
                            <h2 style="margin-top: 0;">⚠️ VUELO CANCELADO</h2>
                            <p style="margin: 0; font-size: 15px;">
                                Le informamos que el siguiente vuelo ha sido <strong>CANCELADO</strong> 
                                y ya <strong>NO ESTÁ DISPONIBLE</strong> para nuevas reservas.
                            </p>
                        </div>
                        
                        <h3>✈️ Detalles del Vuelo Cancelado</h3>
                        <div class="detail-row">
                            <span class="label">Número de Vuelo:</span>
                            <span class="value"><strong style="font-size: 18px; color: #dc2626;">%s</strong></span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Ruta:</span>
                            <span class="value"><strong>%s → %s</strong></span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Fecha Programada:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Hora Programada:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Estado:</span>
                            <span class="value"><span class="badge">CANCELADO</span></span>
                        </div>
                        
                        <h3 style="color: #dc2626;">📝 Motivo de la Cancelación</h3>
                        <div style="background: #fef2f2; padding: 15px; border-radius: 8px; margin: 10px 0;">
                            <p style="margin: 0; font-size: 15px; color: #991b1b;"><strong>%s</strong></p>
                        </div>
                        
                        <div class="detail-row" style="border-bottom: none; margin-top: 15px;">
                            <span class="label">Fecha y Hora de Cancelación:</span>
                            <span class="value">%s</span>
                        </div>
                        
                        <div class="action-box">
                            <h3 style="margin-top: 0; color: #f59e0b;">⚠️ ACCIONES REQUERIDAS</h3>
                            <ul style="margin: 10px 0;">
                                <li>Este vuelo ya <strong>NO ESTÁ DISPONIBLE</strong> para nuevas reservas</li>
                                <li>Contactar a sus clientes con reservas existentes</li>
                                <li>Procesar reembolsos según políticas de la aerolínea</li>
                                <li>Actualizar su sistema de búsqueda de vuelos</li>
                            </ul>
                        </div>
                        
                        <div class="success-box">
                            <h3 style="margin-top: 0; color: #059669;">✓ Acciones Automáticas Completadas</h3>
                            <p style="margin: 5px 0;">Este vuelo ha sido removido automáticamente de:</p>
                            <ul style="margin: 10px 0;">
                                <li>Sistema de búsqueda de vuelos</li>
                                <li>Inventario disponible</li>
                                <li>Todas las plataformas de venta</li>
                            </ul>
                        </div>
                        
                        <h3>📞 Información de Contacto - Soporte Agencias</h3>
                        <p style="margin: 10px 0; background: #f0f9ff; padding: 15px; border-radius: 8px;">
                            <strong>Servicio al Cliente:</strong> +502 1234-5678<br>
                            <strong>Email Corporativo:</strong> corporate@aerolinea.com<br>
                            <strong>Soporte Técnico:</strong> soporte@aerolinea.com<br>
                            <strong>Horario:</strong> 24/7
                        </p>
                        
                        <p style="margin-top: 25px; font-style: italic; color: #666; text-align: center;">
                            Disculpe las molestias causadas.
                        </p>
                        
                        <p style="margin-top: 15px; text-align: center;">
                            Atentamente,<br>
                            <strong>AEROLINEA2</strong><br>
                            Sistema de Gestión de Vuelos<br>
                            Departamento de Operaciones
                        </p>
                        
                        <div class="footer">
                            <p>Este es un correo automático, por favor no responda.</p>
                            <p style="margin: 5px 0;">Para soporte técnico: soporte@aerolinea.com</p>
                            <p style="margin: 5px 0;">&copy; 2024 AEROLINEA2. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
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
            return dateString;
        }
    }
}

