package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sources.app.dao.TicketDAO;
import com.sources.app.dao.FlightDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.Ticket;
import com.sources.app.entities.User;
import com.sources.app.util.CorporateAuthUtil;
// removed unused imports
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
// PDFBox removido temporalmente para compilar sin dependencia

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketHandler implements HttpHandler {
    
    private final TicketDAO ticketDAO;
    // DAOs adicionales reservados para futuras validaciones; evitar warnings
    @SuppressWarnings("unused")
    private final FlightDAO flightDAO;
    @SuppressWarnings("unused")
    private final UserDAO userDAO;
    private final Gson gson;
    
    public TicketHandler() {
        this.ticketDAO = new TicketDAO();
        this.flightDAO = new FlightDAO();
        this.userDAO = new UserDAO();
        this.gson = new Gson();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configuración de CORS para permitir solicitudes desde cualquier origen
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-API-Key");
        
        // Manejo de solicitudes OPTIONS (preflight de CORS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No Content
            return;
        }
        
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        System.out.println("DEBUG: 🎫 TicketHandler - Path: " + path + ", Método: " + method);
        System.out.println("DEBUG: 🎫 TicketHandler - Path startsWith /api/airline/tickets: " + path.startsWith("/api/airline/tickets"));
        System.out.println("DEBUG: 🎫 TicketHandler - Method equals POST: " + "POST".equals(method));
        
        String response = "";
        
        try {
            if (path.startsWith("/api/airline/tickets/round-trip") && "POST".equals(method)) {
                // 🔄 Crear tickets para vuelo redondo (ida + vuelta)
                response = handleCreateRoundTripTickets(exchange);
            } else if (path.startsWith("/api/airline/tickets/with-stopover") && "POST".equals(method)) {
                // ✈️ Crear tickets para vuelo con escala (2 segmentos)
                response = handleCreateStopoverTickets(exchange);
            } else if (path.startsWith("/api/airline/tickets") && "POST".equals(method)) {
                // Crear nuevo boleto simple
                response = handleCreateTicket(exchange);
            } else if (path.matches("/api/airline/tickets/\\d+") && "GET".equals(method)) {
                // Obtener ticket por ID
                String[] pathParts = path.split("/");
                String ticketId = pathParts[4];
                response = handleGetTicketById(ticketId);
            } else if (path.startsWith("/api/airline/tickets/corporate") && "GET".equals(method)) {
                // 🏢 Obtener tickets empresariales (comprados por usuario empresarial)
                response = handleGetCorporateTickets(exchange);
            } else if (path.startsWith("/api/airline/tickets") && "GET".equals(method)) {
                // Obtener boletos
                response = handleGetTickets(exchange);
            } else if (path.startsWith("/api/airline/tickets/code/") && "GET".equals(method)) {
                // Consultar por código de reservación público
                String code = path.substring("/api/airline/tickets/code/".length());
                response = handleGetByReservationCode(code);
            } else if (path.startsWith("/api/airline/tickets/") && path.endsWith("/status") && "PUT".equals(method)) {
                // Actualizar estado del boleto
                String[] pathParts = path.split("/");
                String ticketId = pathParts[4];
                response = handleUpdateTicketStatus(exchange, ticketId);
            } else if (path.startsWith("/api/airline/tickets/") && path.endsWith("/cancel") && "POST".equals(method)) {
                // Cancelar boleto
                String[] pathParts = path.split("/");
                String ticketId = pathParts[4];
                response = handleCancelTicket(exchange, ticketId);
            } else if (path.startsWith("/api/airline/tickets/") && path.endsWith("/payment") && "PUT".equals(method)) {
                // Actualizar estado de pago
                String[] pathParts = path.split("/");
                String ticketId = pathParts[4];
                response = handleUpdatePaymentStatus(exchange, ticketId);
            } else if (path.startsWith("/api/airline/tickets/") && path.endsWith("/pdf") && "GET".equals(method)) {
                // Descargar PDF del ticket
                String[] pathParts = path.split("/");
                String ticketId = pathParts[4];
                handleDownloadTicketPdf(exchange, ticketId);
                return;
            } else if (path.startsWith("/api/airline/flights/") && path.endsWith("/seats") && "GET".equals(method)) {
                // Obtener asientos disponibles de un vuelo
                String[] pathParts = path.split("/");
                String flightId = pathParts[4];
                response = handleGetAvailableSeats(flightId);
            } else if (path.startsWith("/api/airline/tickets/statistics") && "GET".equals(method)) {
                // Obtener estadísticas de boletos
                response = handleGetTicketStatistics();
            } else {
                response = gson.toJson(Map.of("success", false, "error", "Endpoint no encontrado: " + method + " " + path));
            }
            
            // Enviar respuesta
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            
            exchange.sendResponseHeaders(200, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            String errorResponse = gson.toJson(Map.of("success", false, "error", "Error interno: " + e.getMessage()));
            byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(500, errorBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
        }
    }
    
    // Método auxiliar para manejar valores BigDecimal de JSON de manera segura
    private BigDecimal getBigDecimalFromJson(JsonObject json, String key, BigDecimal defaultValue) {
        try {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return BigDecimal.valueOf(json.get(key).getAsDouble());
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error convirtiendo " + key + " a BigDecimal: " + e.getMessage());
        }
        return defaultValue;
    }
    
    private String handleCreateTicket(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("DEBUG: 🎫 Creando boleto - Body: " + requestBody);
        
        try {
            // 🔐 AUTENTICACIÓN EMPRESARIAL: Verificar si viene con API_KEY
            User corporateUser = CorporateAuthUtil.authenticateWithApiKey(exchange);
            
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            if (!validateCreateTicketRequest(jsonRequest)) {
                return gson.toJson(Map.of("success", false, "error", "Datos de boleto inválidos"));
            }
            
            // No establecer bookingDate/bookingTime para evitar conversiones implícitas de Oracle
            
            // Preparar datos para crear el boleto
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("flightId", jsonRequest.get("flightId").getAsInt());
            
            // Determinar quién compra y quién viaja
            Integer userId;
            Integer purchasedByUserId = null;
            
            if (corporateUser != null) {
                // 🏢 COMPRA EMPRESARIAL (desde agencia)
                System.out.println("🏢 Compra empresarial detectada: " + corporateUser.getCompanyName());
                userId = corporateUser.getIdUser().intValue(); // La agencia es el "usuario" del ticket
                purchasedByUserId = corporateUser.getIdUser().intValue(); // La agencia compró
                
                // Si viene un userId específico del pasajero en el JSON, usarlo
                if (jsonRequest.has("passengerUserId") && !jsonRequest.get("passengerUserId").isJsonNull()) {
                    userId = jsonRequest.get("passengerUserId").getAsInt();
                    System.out.println("👤 Pasajero específico ID: " + userId);
                } else if (jsonRequest.has("userId") && !jsonRequest.get("userId").isJsonNull()) {
                    // Si viene userId pero es compra empresarial, usar ese userId como pasajero
                    Integer providedUserId = jsonRequest.get("userId").getAsInt();
                    if (providedUserId != null && !providedUserId.equals(corporateUser.getIdUser().intValue())) {
                        userId = providedUserId;
                        System.out.println("👤 Usuario pasajero del payload: " + userId);
                    }
                }
                System.out.println("🏢 Ticket: Usuario=" + userId + ", Comprado por=" + purchasedByUserId);
            } else {
                // 👤 COMPRA INDIVIDUAL (directo en aerolínea, sin API_KEY)
                if (!jsonRequest.has("userId") || jsonRequest.get("userId").isJsonNull()) {
                    throw new RuntimeException("userId es requerido para compras individuales");
                }
                userId = jsonRequest.get("userId").getAsInt();
                System.out.println("👤 Compra individual - Usuario ID: " + userId);
            }
            
            ticketData.put("userId", userId);
            ticketData.put("purchasedByUserId", purchasedByUserId);
            
            // Si viene seatNumber del cliente, usarlo; si no, se deja null (asignación posterior opcional)
            if (jsonRequest.has("seatNumber") && !jsonRequest.get("seatNumber").isJsonNull()) {
                ticketData.put("seatNumber", jsonRequest.get("seatNumber").getAsString());
            }
            ticketData.put("seatCategory", jsonRequest.get("seatCategory").getAsString());
            
            // Agregar cantidad de asientos si está presente
            if (jsonRequest.has("quantity")) {
                ticketData.put("quantity", jsonRequest.get("quantity").getAsInt());
            } else {
                ticketData.put("quantity", 1); // Por defecto 1 asiento
            }
            
            ticketData.put("fare", getBigDecimalFromJson(jsonRequest, "fare", BigDecimal.ZERO));
            ticketData.put("passengerFirstName", jsonRequest.get("passengerFirstName").getAsString());
            ticketData.put("passengerLastName", jsonRequest.get("passengerLastName").getAsString());
            ticketData.put("passengerDocumentType", jsonRequest.get("passengerDocumentType").getAsString());
            ticketData.put("passengerDocumentNumber", jsonRequest.get("passengerDocumentNumber").getAsString());
            ticketData.put("passengerEmail", jsonRequest.get("passengerEmail").getAsString());
            ticketData.put("passengerPhone", jsonRequest.get("passengerPhone").getAsString());
            // Manejo seguro para specialRequests
            String specialRequests = "";
            if (jsonRequest.has("specialRequests") && !jsonRequest.get("specialRequests").isJsonNull()) {
                specialRequests = jsonRequest.get("specialRequests").getAsString();
            }
            ticketData.put("specialRequests", specialRequests);
            ticketData.put("paymentMethod", jsonRequest.get("paymentMethod").getAsString());
            
            // No enviar bookingDate/bookingTime; que la BD maneje defaults o queden null
            
            // Calcular precios
            // Calcular precios - Solo precio base sin impuestos
            BigDecimal baseFare = getBigDecimalFromJson(jsonRequest, "fare", BigDecimal.ZERO);
            BigDecimal totalAmount = getBigDecimalFromJson(jsonRequest, "totalAmount", baseFare);
            BigDecimal discountAmount = getBigDecimalFromJson(jsonRequest, "discountAmount", BigDecimal.ZERO);
            // discountCode es un string, no un número - manejo seguro para valores null
            String discountCode = "";
            if (jsonRequest.has("discountCode") && !jsonRequest.get("discountCode").isJsonNull()) {
                discountCode = jsonRequest.get("discountCode").getAsString();
            }
            
            ticketData.put("totalAmount", totalAmount);
            ticketData.put("taxes", BigDecimal.ZERO); // Sin impuestos
            ticketData.put("fees", BigDecimal.ZERO);  // Sin cargos
            ticketData.put("discountAmount", discountAmount);
            ticketData.put("discountCode", discountCode);
            
            // Crear el boleto
            Ticket ticket = ticketDAO.createTicketWithValidation(ticketData);
            
            if (ticket != null) {
                // Generar código de boleto simple si no existe
                String reservationCode = "BK" + System.currentTimeMillis();
                // Enviar correo de confirmación (no bloqueante para el flujo principal)
                try {
                    Map<String, Object> emailPayload = new HashMap<>();
                    emailPayload.put("to", ticket.getPassengerEmail());
                    emailPayload.put("subject", "Confirmación de compra - Ticket " + ticket.getIdTicket());
                    String emailBody = "" +
                        "<div style=\"font-family:Inter,Arial,sans-serif;color:#0f172a\">" +
                        "<h2 style=\"margin:0 0 12px\">\u2708\ufe0f Confirmación de compra</h2>" +
                        "<p>Hola <strong>" + ticket.getPassengerFirstName() + "</strong>,</p>" +
                        "<p>Tu compra fue exitosa. Estos son los detalles de tu boleto:</p>" +
                        "<table cellpadding=\"6\" style=\"border-collapse:collapse;background:#f8fafc;border-radius:8px\">" +
                        row("Ticket ID", String.valueOf(ticket.getIdTicket())) +
                        row("Código", reservationCode) +
                        row("Vuelo", String.valueOf(ticket.getFlight().getIdFlight())) +
                        row("Categoría", ticket.getSeatCategory()) +
                        row("Asiento", ticket.getSeatNumber()) +
                        row("Total", String.valueOf(ticket.getTotalAmount())) +
                        row("Reserva", (ticket.getBookingDate()!=null?ticket.getBookingDate():"") + " " + (ticket.getBookingTime()!=null?ticket.getBookingTime():"")) +
                        "</table>" +
                        "<p style=\"margin-top:12px\">Gracias por volar con nosotros.</p>" +
                        "</div>";
                    emailPayload.put("body", emailBody);
                    emailPayload.put("isHtml", true);
                    emailPayload.put("contentType", "text/html");
                    String emailJson = gson.toJson(emailPayload);
                    com.sources.app.util.HttpClientUtil.post("http://127.0.0.1:8085/api/notifications/email", emailJson);
                } catch (Exception ignored) { }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Boleto creado exitosamente");
                response.put("ticketId", ticket.getIdTicket());
                response.put("reservationCode", reservationCode);
                response.put("totalAmount", ticket.getTotalAmount());
                response.put("seatNumber", ticket.getSeatNumber());
                response.put("bookingDate", ticket.getBookingDate());
                response.put("messageForUser", "Compra registrada");
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "Error al crear el boleto"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error procesando datos: " + e.getMessage()));
        }
    }
    
    private String handleGetTickets(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryString(query);
            
            List<Ticket> tickets = null;
            
            if (queryParams.containsKey("userId")) {
                // Obtener boletos de un usuario específico
                Integer userId = Integer.parseInt(queryParams.get("userId"));
                tickets = ticketDAO.getTicketsByUserId(userId);
            } else if (queryParams.containsKey("flightId")) {
                // Obtener boletos de un vuelo específico
                Integer flightId = Integer.parseInt(queryParams.get("flightId"));
                tickets = ticketDAO.getTicketsByFlightId(flightId);
            } else if (queryParams.containsKey("status")) {
                // Obtener boletos por estado
                String status = queryParams.get("status");
                tickets = ticketDAO.getTicketsByStatus(status);
            } else {
                // Obtener todos los boletos (para admin)
                tickets = ticketDAO.findAll();
            }
            
            if (tickets != null) {
                List<Map<String, Object>> ticketMaps = new java.util.ArrayList<>();
                
                for (Ticket ticket : tickets) {
                    Map<String, Object> ticketMap = new HashMap<>();
                    ticketMap.put("idTicket", ticket.getIdTicket());
                    ticketMap.put("flightNumber", ticket.getFlight().getFlightNumber());
                    ticketMap.put("originCity", ticket.getFlight().getOriginCity().getName());
                    ticketMap.put("destinationCity", ticket.getFlight().getDestinationCity().getName());
                    ticketMap.put("departureDate", ticket.getFlight().getDepartureDate());
                    ticketMap.put("departureTime", ticket.getFlight().getDepartureTime());
                    ticketMap.put("seatNumber", ticket.getSeatNumber());
                    ticketMap.put("seatCategory", ticket.getSeatCategory());
                    ticketMap.put("passengerName", ticket.getPassengerFirstName() + " " + ticket.getPassengerLastName());
                    ticketMap.put("status", ticket.getStatus());
                    ticketMap.put("paymentStatus", ticket.getPaymentStatus());
                    ticketMap.put("totalAmount", ticket.getTotalAmount());
                    ticketMap.put("bookingDate", ticket.getBookingDate());
                    ticketMap.put("createdAt", ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : null);
                    ticketMap.put("updatedAt", ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().toString() : null);
                    
                    ticketMaps.add(ticketMap);
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("tickets", ticketMaps);
                response.put("count", ticketMaps.size());
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "Error obteniendo boletos"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo boletos: " + e.getMessage()));
        }
    }
    
    private String handleUpdateTicketStatus(HttpExchange exchange, String ticketId) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("DEBUG: 🎫 Actualizando estado del boleto " + ticketId + " - Body: " + requestBody);
        
        try {
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            String newStatus = jsonRequest.get("status").getAsString();
            
            Long ticketIdLong = Long.parseLong(ticketId);
            boolean success = ticketDAO.updateTicketStatus(ticketIdLong, newStatus);
            
            if (success) {
                return gson.toJson(Map.of("success", true, "message", "Estado del boleto actualizado"));
            } else {
                return gson.toJson(Map.of("success", false, "error", "Boleto no encontrado"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error actualizando estado: " + e.getMessage()));
        }
    }
    
    private String handleCancelTicket(HttpExchange exchange, String ticketId) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("DEBUG: 🎫 Cancelando boleto " + ticketId + " - Body: " + requestBody);
        
        try {
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            String reason = jsonRequest.get("reason").getAsString();
            
            Long ticketIdLong = Long.parseLong(ticketId);
            boolean success = ticketDAO.cancelTicket(ticketIdLong, reason);
            
            if (success) {
                return gson.toJson(Map.of("success", true, "message", "Boleto cancelado exitosamente"));
            } else {
                return gson.toJson(Map.of("success", false, "error", "Boleto no encontrado"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error cancelando boleto: " + e.getMessage()));
        }
    }
    
    private String handleUpdatePaymentStatus(HttpExchange exchange, String ticketId) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("DEBUG: 🎫 Actualizando estado de pago del boleto " + ticketId + " - Body: " + requestBody);
        
        try {
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            String paymentStatus = jsonRequest.get("paymentStatus").getAsString();
            
            Long ticketIdLong = Long.parseLong(ticketId);
            boolean success = ticketDAO.updatePaymentStatus(ticketIdLong, paymentStatus);
            
            if (success) {
                return gson.toJson(Map.of("success", true, "message", "Estado de pago actualizado"));
            } else {
                return gson.toJson(Map.of("success", false, "error", "Boleto no encontrado"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error actualizando estado de pago: " + e.getMessage()));
        }
    }
    
    private String handleGetAvailableSeats(String flightId) {
        try {
            Integer flightIdInt = Integer.parseInt(flightId);
            List<String> availableSeats = ticketDAO.getAvailableSeats(flightIdInt);
            
            if (availableSeats != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("availableSeats", availableSeats);
                response.put("count", availableSeats.size());
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "Error obteniendo asientos disponibles"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo asientos: " + e.getMessage()));
        }
    }
    
    private String handleGetTicketStatistics() {
        try {
            Map<String, Object> stats = ticketDAO.getTicketStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            return gson.toJson(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo estadísticas: " + e.getMessage()));
        }
    }

    private String handleGetByReservationCode(String code) {
        // Método de búsqueda por código no disponible en DAO actual
        return gson.toJson(Map.of("success", false, "error", "Código no encontrado"));
    }

    private String handleGetTicketById(String ticketId) {
        try {
            Long id = Long.parseLong(ticketId);
            Ticket t = ticketDAO.getTicketById(id);
            if (t == null) {
                return gson.toJson(Map.of("success", false, "error", "Boleto no encontrado"));
            }
            Map<String, Object> ticket = new HashMap<>();
            ticket.put("idTicket", t.getIdTicket());
            ticket.put("reservationCode", "N/A");
            ticket.put("flightId", t.getFlight() != null ? t.getFlight().getIdFlight() : null);
            ticket.put("flightNumber", t.getFlight().getFlightNumber());
            ticket.put("originCity", t.getFlight().getOriginCity().getName());
            ticket.put("destinationCity", t.getFlight().getDestinationCity().getName());
            ticket.put("departureDate", t.getFlight().getDepartureDate());
            ticket.put("departureTime", t.getFlight().getDepartureTime());
            ticket.put("passengerFirstName", t.getPassengerFirstName());
            ticket.put("passengerLastName", t.getPassengerLastName());
            ticket.put("seatNumber", t.getSeatNumber());
            ticket.put("seatCategory", t.getSeatCategory());
            ticket.put("status", t.getStatus());
            ticket.put("paymentStatus", t.getPaymentStatus());
            ticket.put("totalAmount", t.getTotalAmount());
            return gson.toJson(Map.of("success", true, "ticket", ticket));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo ticket: " + e.getMessage()));
        }
    }

    private void handleDownloadTicketPdf(HttpExchange exchange, String ticketId) throws IOException {
        org.apache.pdfbox.pdmodel.PDDocument doc = null;
        try {
            Long id = Long.parseLong(ticketId);
            Ticket t = ticketDAO.getTicketById(id);
            if (t == null) {
                String err = gson.toJson(Map.of("success", false, "error", "Boleto no encontrado"));
                byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
                return;
            }

            System.out.println("📄 Generando PDF para ticket #" + t.getIdTicket());

            // Generar PDF mejorado con PDFBox
            doc = new org.apache.pdfbox.pdmodel.PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
            doc.addPage(page);

            org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);
            
            try {
                // Título
                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 24);
                cs.newLineAtOffset(50, 750);
                cs.showText("TICKET DE VUELO");
                cs.endText();

                // Línea separadora
                cs.setLineWidth(1f);
                cs.moveTo(50, 740);
                cs.lineTo(550, 740);
                cs.stroke();

                float y = 710;
                
                // Información del ticket
                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(50, y);
                cs.showText("Ticket #" + t.getIdTicket());
                cs.endText();
                y -= 30;

                // Detalles del vuelo
                y = writeLineSafe(cs, 12, 50, y, "Vuelo: " + safeString(t.getFlight().getFlightNumber()));
                
                String route = safeString(t.getFlight().getOriginCity() != null ? t.getFlight().getOriginCity().getName() : "N/A") 
                             + " -> " 
                             + safeString(t.getFlight().getDestinationCity() != null ? t.getFlight().getDestinationCity().getName() : "N/A");
                y = writeLineSafe(cs, 12, 50, y, "Ruta: " + route);
                
                String departure = safeString(t.getFlight().getDepartureDate()) + " " + safeString(t.getFlight().getDepartureTime());
                y = writeLineSafe(cs, 12, 50, y, "Salida: " + departure);
                
                y -= 10; // Espacio extra

                // Información del pasajero
                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(50, y);
                cs.showText("Informacion del Pasajero");
                cs.endText();
                y -= 25;

                y = writeLineSafe(cs, 12, 50, y, "Nombre: " + safeString(t.getPassengerFirstName()) + " " + safeString(t.getPassengerLastName()));
                y = writeLineSafe(cs, 12, 50, y, "Email: " + safeString(t.getPassengerEmail()));
                y = writeLineSafe(cs, 12, 50, y, "Telefono: " + safeString(t.getPassengerPhone()));
                y = writeLineSafe(cs, 12, 50, y, "Documento: " + safeString(t.getPassengerDocumentNumber()));
                
                y -= 10;

                // Información del asiento
                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(50, y);
                cs.showText("Informacion del Asiento");
                cs.endText();
                y -= 25;

                y = writeLineSafe(cs, 12, 50, y, "Categoria: " + safeString(t.getSeatCategory()));
                y = writeLineSafe(cs, 12, 50, y, "Asiento: " + (t.getSeatNumber() != null ? t.getSeatNumber() : "Por asignar"));
                y = writeLineSafe(cs, 12, 50, y, "Cantidad: " + (t.getQuantity() != null ? t.getQuantity() : 1));
                
                y -= 10;

                // Información de pago
                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(50, y);
                cs.showText("Informacion de Pago");
                cs.endText();
                y -= 25;

                y = writeLineSafe(cs, 12, 50, y, "Tarifa base: $" + safeString(t.getFare()));
                y = writeLineSafe(cs, 12, 50, y, "Total: $" + safeString(t.getTotalAmount()));
                y = writeLineSafe(cs, 12, 50, y, "Estado: " + safeString(t.getStatus()));
                y = writeLineSafe(cs, 12, 50, y, "Metodo de pago: " + safeString(t.getPaymentMethod()));

                // Nota al pie
                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_OBLIQUE, 10);
                cs.newLineAtOffset(50, 50);
                cs.showText("Gracias por volar con nosotros - AeroLinea");
                cs.endText();

                cs.close();

            } catch (Exception e) {
                System.err.println("❌ Error al escribir contenido del PDF: " + e.getMessage());
                e.printStackTrace();
                if (cs != null) {
                    try { cs.close(); } catch (Exception ignored) {}
                }
                throw e;
            }

            // Guardar PDF en ByteArray
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            doc.save(baos);
            doc.close();
            doc = null; // Marcar como cerrado
            
            byte[] pdfBytes = baos.toByteArray();
            
            System.out.println("✅ PDF generado exitosamente: " + pdfBytes.length + " bytes");

            // Enviar headers correctos
            exchange.getResponseHeaders().set("Content-Type", "application/pdf");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"Ticket-" + t.getIdTicket() + ".pdf\"");
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(pdfBytes.length));
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            
            exchange.sendResponseHeaders(200, pdfBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) { 
                os.write(pdfBytes);
                os.flush();
            }
            
            System.out.println("✅ PDF enviado correctamente para ticket #" + t.getIdTicket());
            
        } catch (NumberFormatException e) {
            System.err.println("❌ ID de ticket inválido: " + ticketId);
            sendErrorResponse(exchange, 400, "ID de ticket inválido");
        } catch (Exception e) {
            System.err.println("❌ Error generando PDF: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error generando PDF: " + e.getMessage());
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (Exception e) {
                    System.err.println("⚠️ Error cerrando documento PDF: " + e.getMessage());
                }
            }
        }
    }

    private String safeString(Object obj) {
        if (obj == null) return "N/A";
        String str = obj.toString().trim();
        return str.isEmpty() ? "N/A" : str;
    }

    private float writeLineSafe(org.apache.pdfbox.pdmodel.PDPageContentStream cs, int fontSize, float x, float y, String text) throws IOException {
        y -= 20;
        if (text != null && !text.trim().isEmpty()) {
            cs.beginText();
            cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, fontSize);
            cs.newLineAtOffset(x, y);
            // Limpiar texto de caracteres especiales que pueden causar problemas
            String cleanText = text.replaceAll("[^\\x20-\\x7E]", "?");
            cs.showText(cleanText);
            cs.endText();
        }
        return y;
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String err = gson.toJson(Map.of("success", false, "error", message));
        byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    /**
     * Crea 2 tickets para un vuelo redondo (ida y vuelta).
     * Usa la misma información de pago para ambos tickets.
     */
    private String handleCreateRoundTripTickets(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("🔄 Creando tickets round-trip - Body: " + requestBody);
        
        try {
            User corporateUser = CorporateAuthUtil.authenticateWithApiKey(exchange);
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            if (!jsonRequest.has("outboundFlightId") || !jsonRequest.has("returnFlightId")) {
                return gson.toJson(Map.of("success", false, "error", "Se requieren outboundFlightId y returnFlightId"));
            }
            
            Integer outboundFlightId = jsonRequest.get("outboundFlightId").getAsInt();
            Integer returnFlightId = jsonRequest.get("returnFlightId").getAsInt();
            
            Integer userId;
            Integer purchasedByUserId = null;
            
            if (corporateUser != null) {
                userId = corporateUser.getIdUser().intValue();
                purchasedByUserId = userId;
                System.out.println("🏢 Round-trip empresarial: " + corporateUser.getCompanyName());
            } else {
                userId = jsonRequest.has("userId") ? jsonRequest.get("userId").getAsInt() : null;
                if (userId == null) {
                    return gson.toJson(Map.of("success", false, "error", "userId requerido"));
                }
            }
            
            // Datos comunes del pasajero
            Map<String, Object> commonData = extractPassengerData(jsonRequest);
            
            // Crear ticket de IDA
            Map<String, Object> ticketData1 = new HashMap<>(commonData);
            ticketData1.put("flightId", outboundFlightId);
            ticketData1.put("userId", userId);
            ticketData1.put("purchasedByUserId", purchasedByUserId);
            ticketData1.put("specialRequests", "Vuelo de IDA - Round Trip");
            
            Ticket ticket1 = ticketDAO.createTicketWithValidation(ticketData1);
            
            // Crear ticket de VUELTA
            Map<String, Object> ticketData2 = new HashMap<>(commonData);
            ticketData2.put("flightId", returnFlightId);
            ticketData2.put("userId", userId);
            ticketData2.put("purchasedByUserId", purchasedByUserId);
            ticketData2.put("specialRequests", "Vuelo de VUELTA - Round Trip");
            
            Ticket ticket2 = ticketDAO.createTicketWithValidation(ticketData2);
            
            if (ticket1 != null && ticket2 != null) {
                List<Map<String, Object>> tickets = new ArrayList<>();
                tickets.add(Map.of("ticketId", ticket1.getIdTicket(), "type", "outbound", "flightNumber", ticket1.getFlight().getFlightNumber()));
                tickets.add(Map.of("ticketId", ticket2.getIdTicket(), "type", "return", "flightNumber", ticket2.getFlight().getFlightNumber()));
                
                System.out.println("✅ Round-trip: Tickets #" + ticket1.getIdTicket() + " y #" + ticket2.getIdTicket());
                
                return gson.toJson(Map.of(
                    "success", true,
                    "message", "Vuelo redondo confirmado",
                    "tickets", tickets,
                    "totalTickets", 2
                ));
            } else {
                return gson.toJson(Map.of("success", false, "error", "Error creando tickets"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error round-trip: " + e.getMessage()));
        }
    }

    /**
     * Crea 2 tickets para un vuelo con escala (2 segmentos).
     */
    private String handleCreateStopoverTickets(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("✈️ Creando tickets con escala - Body: " + requestBody);
        
        try {
            User corporateUser = CorporateAuthUtil.authenticateWithApiKey(exchange);
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            if (!jsonRequest.has("firstSegmentFlightId") || !jsonRequest.has("secondSegmentFlightId")) {
                return gson.toJson(Map.of("success", false, "error", "Se requieren firstSegmentFlightId y secondSegmentFlightId"));
            }
            
            Integer flight1Id = jsonRequest.get("firstSegmentFlightId").getAsInt();
            Integer flight2Id = jsonRequest.get("secondSegmentFlightId").getAsInt();
            
            Integer userId;
            Integer purchasedByUserId = null;
            
            if (corporateUser != null) {
                userId = corporateUser.getIdUser().intValue();
                purchasedByUserId = userId;
            } else {
                userId = jsonRequest.has("userId") ? jsonRequest.get("userId").getAsInt() : null;
                if (userId == null) {
                    return gson.toJson(Map.of("success", false, "error", "userId requerido"));
                }
            }
            
            Map<String, Object> commonData = extractPassengerData(jsonRequest);
            
            // Ticket segmento 1: Origen → Escala
            Map<String, Object> ticketData1 = new HashMap<>(commonData);
            ticketData1.put("flightId", flight1Id);
            ticketData1.put("userId", userId);
            ticketData1.put("purchasedByUserId", purchasedByUserId);
            ticketData1.put("specialRequests", "Segmento 1 - Con Escala");
            
            Ticket ticket1 = ticketDAO.createTicketWithValidation(ticketData1);
            
            // Ticket segmento 2: Escala → Destino
            Map<String, Object> ticketData2 = new HashMap<>(commonData);
            ticketData2.put("flightId", flight2Id);
            ticketData2.put("userId", userId);
            ticketData2.put("purchasedByUserId", purchasedByUserId);
            ticketData2.put("specialRequests", "Segmento 2 - Con Escala");
            
            Ticket ticket2 = ticketDAO.createTicketWithValidation(ticketData2);
            
            if (ticket1 != null && ticket2 != null) {
                List<Map<String, Object>> tickets = new ArrayList<>();
                tickets.add(Map.of("ticketId", ticket1.getIdTicket(), "segment", 1, "flightNumber", ticket1.getFlight().getFlightNumber()));
                tickets.add(Map.of("ticketId", ticket2.getIdTicket(), "segment", 2, "flightNumber", ticket2.getFlight().getFlightNumber()));
                
                System.out.println("✅ Vuelo con escala: Tickets #" + ticket1.getIdTicket() + " y #" + ticket2.getIdTicket());
                
                return gson.toJson(Map.of(
                    "success", true,
                    "message", "Vuelo con escala confirmado",
                    "tickets", tickets,
                    "totalTickets", 2
                ));
            } else {
                return gson.toJson(Map.of("success", false, "error", "Error creando tickets"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error con escala: " + e.getMessage()));
        }
    }

    /**
     * Extrae datos comunes del pasajero del JSON request.
     */
    private Map<String, Object> extractPassengerData(JsonObject jsonRequest) {
        Map<String, Object> data = new HashMap<>();
        data.put("seatCategory", jsonRequest.get("seatCategory").getAsString());
        data.put("fare", getBigDecimalFromJson(jsonRequest, "fare", BigDecimal.ZERO));
        data.put("passengerFirstName", jsonRequest.get("passengerFirstName").getAsString());
        data.put("passengerLastName", jsonRequest.get("passengerLastName").getAsString());
        data.put("passengerEmail", jsonRequest.get("passengerEmail").getAsString());
        data.put("passengerPhone", jsonRequest.get("passengerPhone").getAsString());
        data.put("passengerDocumentType", jsonRequest.get("passengerDocumentType").getAsString());
        data.put("passengerDocumentNumber", jsonRequest.get("passengerDocumentNumber").getAsString());
        data.put("paymentMethod", jsonRequest.get("paymentMethod").getAsString());
        data.put("totalAmount", getBigDecimalFromJson(jsonRequest, "fare", BigDecimal.ZERO));
        data.put("taxes", BigDecimal.ZERO);
        data.put("fees", BigDecimal.ZERO);
        data.put("discountAmount", BigDecimal.ZERO);
        data.put("discountCode", "");
        data.put("quantity", jsonRequest.has("quantity") ? jsonRequest.get("quantity").getAsInt() : 1);
        return data;
    }

    /**
     * Obtiene los tickets comprados por el usuario empresarial autenticado.
     * Este endpoint es para que la agencia vea todas sus compras.
     */
    private String handleGetCorporateTickets(HttpExchange exchange) {
        try {
            // 🔐 Autenticar con API_KEY
            User corporateUser = CorporateAuthUtil.authenticateWithApiKey(exchange);
            
            if (corporateUser == null) {
                return gson.toJson(Map.of(
                    "success", false, 
                    "error", "Autenticación empresarial requerida. Usa header X-API-Key"
                ));
            }
            
            System.out.println("🏢 Obteniendo tickets empresariales para: " + corporateUser.getCompanyName());
            
            // Obtener tickets comprados por este usuario empresarial
            List<Ticket> tickets = ticketDAO.getTicketsByPurchasedByUserId(corporateUser.getIdUser().intValue());
            
            // Convertir a formato JSON
            List<Map<String, Object>> ticketsList = new ArrayList<>();
            for (Ticket t : tickets) {
                Map<String, Object> ticketMap = new HashMap<>();
                ticketMap.put("ticketId", t.getIdTicket());
                ticketMap.put("flightId", t.getFlight().getIdFlight());
                ticketMap.put("flightNumber", t.getFlight().getFlightNumber());
                ticketMap.put("origin", t.getFlight().getOriginCity() != null ? t.getFlight().getOriginCity().getName() : "N/A");
                ticketMap.put("destination", t.getFlight().getDestinationCity() != null ? t.getFlight().getDestinationCity().getName() : "N/A");
                ticketMap.put("departureDate", t.getFlight().getDepartureDate());
                ticketMap.put("departureTime", t.getFlight().getDepartureTime());
                ticketMap.put("passengerFirstName", t.getPassengerFirstName());
                ticketMap.put("passengerLastName", t.getPassengerLastName());
                ticketMap.put("passengerEmail", t.getPassengerEmail());
                ticketMap.put("passengerPhone", t.getPassengerPhone());
                ticketMap.put("seatCategory", t.getSeatCategory());
                ticketMap.put("seatNumber", t.getSeatNumber());
                ticketMap.put("quantity", t.getQuantity());
                ticketMap.put("totalAmount", t.getTotalAmount());
                ticketMap.put("status", t.getStatus());
                ticketMap.put("paymentStatus", t.getPaymentStatus());
                ticketMap.put("bookingDate", t.getBookingDate());
                ticketMap.put("bookingTime", t.getBookingTime());
                ticketMap.put("purchasedBy", corporateUser.getCompanyName());
                ticketsList.add(ticketMap);
            }
            
            return gson.toJson(Map.of(
                "success", true,
                "tickets", ticketsList,
                "total", tickets.size(),
                "companyName", corporateUser.getCompanyName()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of(
                "success", false, 
                "error", "Error obteniendo tickets empresariales: " + e.getMessage()
            ));
        }
    }

    
    private boolean validateCreateTicketRequest(JsonObject jsonRequest) {
        // Campos requeridos para crear un boleto
        // userId NO es requerido si viene con API_KEY (compra empresarial)
        String[] requiredFields = {
            "flightId", "seatCategory", "fare", 
            "passengerFirstName", "passengerLastName", "passengerDocumentType",
            "passengerDocumentNumber", "passengerEmail", "passengerPhone", "paymentMethod"
        };
        
        for (String field : requiredFields) {
            if (!jsonRequest.has(field) || jsonRequest.get(field).isJsonNull()) {
                System.out.println("DEBUG: ❌ Campo requerido faltante: " + field);
                return false;
            }
        }
        
        System.out.println("DEBUG: ✅ Validación de boleto exitosa");
        return true;
    }
    
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    // Método legacy no utilizado actualmente (se deja por compatibilidad futura)
    private String generateSeatNumber(int flightId) {
        long timestamp = System.currentTimeMillis();
        return String.format("%d%c%03d", flightId, 'E', timestamp % 1000);
    }

    private static String row(String label, String value) {
        return "<tr><td style=\"padding:6px 12px;color:#334155\"><strong>" + label +
               ":</strong></td><td style=\"padding:6px 12px;color:#0f172a\">" + value + "</td></tr>";
    }
}


