package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sources.app.dao.TicketDAO;
import com.sources.app.dao.FlightDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.Ticket;
import com.sources.app.entities.Flight;
import com.sources.app.entities.User;
import com.sources.app.entities.FlightFare;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketHandler implements HttpHandler {
    
    private final TicketDAO ticketDAO;
    private final FlightDAO flightDAO;
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
            } else if (path.startsWith("/api/airline/tickets") && "GET".equals(method)) {
                // Obtener boletos
                response = handleGetTickets(exchange);
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
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
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
            
            // Preparar datos para crear el boleto
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("flightId", jsonRequest.get("flightId").getAsInt());
            ticketData.put("userId", jsonRequest.get("userId").getAsInt());
            ticketData.put("seatNumber", jsonRequest.get("seatNumber").getAsString());
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
            ticketData.put("specialRequests", jsonRequest.has("specialRequests") ? jsonRequest.get("specialRequests").getAsString() : "");
            ticketData.put("paymentMethod", jsonRequest.get("paymentMethod").getAsString());
            
            // Calcular precios
            // Calcular precios - Solo precio base sin impuestos
            BigDecimal baseFare = getBigDecimalFromJson(jsonRequest, "fare", BigDecimal.ZERO);
            BigDecimal totalAmount = getBigDecimalFromJson(jsonRequest, "totalAmount", baseFare);
            BigDecimal discountAmount = getBigDecimalFromJson(jsonRequest, "discountAmount", BigDecimal.ZERO);
            // discountCode es un string, no un número
            String discountCode = jsonRequest.has("discountCode") ? jsonRequest.get("discountCode").getAsString() : "";
            
            ticketData.put("totalAmount", totalAmount);
            ticketData.put("taxes", BigDecimal.ZERO); // Sin impuestos
            ticketData.put("fees", BigDecimal.ZERO);  // Sin cargos
            ticketData.put("discountAmount", discountAmount);
            ticketData.put("discountCode", discountCode);
            
            // Crear el boleto
            Ticket ticket = ticketDAO.createTicketWithValidation(ticketData);
            
            if (ticket != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Boleto creado exitosamente");
                response.put("ticketId", ticket.getIdTicket());
                response.put("totalAmount", ticket.getTotalAmount());
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
                    ticketMap.put("createdAt", ticket.getCreatedAt());
                    
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
    
    private boolean validateCreateTicketRequest(JsonObject request) {
        try {
            // Verificar campos requeridos
            if (!request.has("flightId") || request.get("flightId").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ flightId inválido");
                return false;
            }
            if (!request.has("userId") || request.get("userId").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ userId inválido");
                return false;
            }
            // Validar seatNumber (ahora puede ser un ID generado automáticamente)
            if (!request.has("seatNumber")) {
                System.out.println("DEBUG: ❌ seatNumber faltante");
                return false;
            }
            
            // Validar quantity si está presente
            if (request.has("quantity") && request.get("quantity").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ quantity inválido");
                return false;
            }
            if (!request.has("seatCategory") || request.get("seatCategory").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ seatCategory inválido");
                return false;
            }
            if (!request.has("fare") || getBigDecimalFromJson(request, "fare", BigDecimal.ZERO).compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("DEBUG: ❌ fare inválido");
                return false;
            }
            if (!request.has("passengerFirstName") || request.get("passengerFirstName").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ passengerFirstName inválido");
                return false;
            }
            if (!request.has("passengerLastName") || request.get("passengerLastName").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ passengerLastName inválido");
                return false;
            }
            if (!request.has("passengerDocumentType") || request.get("passengerDocumentType").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ passengerDocumentType inválido");
                return false;
            }
            if (!request.has("passengerDocumentNumber") || request.get("passengerDocumentNumber").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ passengerDocumentNumber inválido");
                return false;
            }
            if (!request.has("passengerEmail") || request.get("passengerEmail").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ passengerEmail inválido");
                return false;
            }
            if (!request.has("passengerPhone") || request.get("passengerPhone").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ passengerPhone inválido");
                return false;
            }
            if (!request.has("paymentMethod") || request.get("paymentMethod").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ paymentMethod inválido");
                return false;
            }
            
            System.out.println("DEBUG: ✅ Validación de boleto exitosa");
            return true;
            
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error en validación de boleto: " + e.getMessage());
            return false;
        }
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
}


