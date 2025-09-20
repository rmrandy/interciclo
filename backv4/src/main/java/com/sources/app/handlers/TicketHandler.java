package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sources.app.dao.TicketDAO;
import com.sources.app.dao.FlightDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.Ticket;
// removed unused imports
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
// PDFBox removido temporalmente para compilar sin dependencia

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
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
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
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
            if (path.startsWith("/api/airline/tickets") && "POST".equals(method)) {
                // Crear nuevo boleto
                response = handleCreateTicket(exchange);
            } else if (path.matches("/api/airline/tickets/\\d+") && "GET".equals(method)) {
                // Obtener ticket por ID
                String[] pathParts = path.split("/");
                String ticketId = pathParts[4];
                response = handleGetTicketById(ticketId);
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
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            if (!validateCreateTicketRequest(jsonRequest)) {
                return gson.toJson(Map.of("success", false, "error", "Datos de boleto inválidos"));
            }
            
            // No establecer bookingDate/bookingTime para evitar conversiones implícitas de Oracle
            
            // Preparar datos para crear el boleto
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("flightId", jsonRequest.get("flightId").getAsInt());
            ticketData.put("userId", jsonRequest.get("userId").getAsInt());
            
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
                    com.sources.app.util.HttpClientUtil.post("http://127.0.0.1:8080/api/notifications/email", emailJson);
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
        try {
            Long id = Long.parseLong(ticketId);
            Ticket t = ticketDAO.getTicketById(id);
            if (t == null) {
                String err = gson.toJson(Map.of("success", false, "error", "Boleto no encontrado"));
                byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
                return;
            }

            // Generar PDF sencillo con PDFBox
            org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument();
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);

            org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);
            cs.beginText();
            cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 18);
            cs.newLineAtOffset(72, 720);
            cs.showText("Ticket de vuelo #" + t.getIdTicket());
            cs.endText();

            float y = 700;
            y = writeLine(cs, 12, 72, y, "Vuelo: " + t.getFlight().getFlightNumber());
            y = writeLine(cs, 12, 72, y, "Ruta: " + t.getFlight().getOriginCity().getName() + " → " + t.getFlight().getDestinationCity().getName());
            y = writeLine(cs, 12, 72, y, "Salida: " + t.getFlight().getDepartureDate() + " " + t.getFlight().getDepartureTime());
            y = writeLine(cs, 12, 72, y, "Pasajero: " + t.getPassengerFirstName() + " " + t.getPassengerLastName());
            y = writeLine(cs, 12, 72, y, "Categoría: " + t.getSeatCategory() + "  Asiento: " + (t.getSeatNumber() != null ? t.getSeatNumber() : "AUTO"));
            y = writeLine(cs, 12, 72, y, "Total: $" + t.getTotalAmount());
            cs.close();

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            doc.save(baos);
            doc.close();
            byte[] pdf = baos.toByteArray();

            exchange.getResponseHeaders().add("Content-Type", "application/pdf");
            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=Ticket-" + t.getIdTicket() + ".pdf");
            exchange.sendResponseHeaders(200, pdf.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(pdf); }
        } catch (Exception e) {
            e.printStackTrace();
            String err = gson.toJson(Map.of("success", false, "error", "Error generando PDF: " + e.getMessage()));
            byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(500, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }

    private float writeLine(org.apache.pdfbox.pdmodel.PDPageContentStream cs, int fontSize, float x, float y, String text) throws IOException {
        y -= 18;
        cs.beginText();
        cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y;
    }
    
    private boolean validateCreateTicketRequest(JsonObject jsonRequest) {
        // Campos requeridos para crear un boleto
        String[] requiredFields = {
            "flightId", "userId", "seatCategory", "fare", 
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


