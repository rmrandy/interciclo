package com.sources.app.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sources.app.util.HibernateUtil;
import com.sources.app.entities.FlightInventory;
import org.hibernate.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Handler HTTP específico para operaciones de asientos.
 * Implementa HttpHandler para compatibilidad con App.java
 */
public class SeatsHandler implements HttpHandler {
    private final Gson gson;
    
    public SeatsHandler() {
        this.gson = new Gson();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        // Manejar preflight OPTIONS
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        System.out.println("DEBUG: 🪑 SeatsHandler - Path recibido: " + path);
        System.out.println("DEBUG: 🪑 SeatsHandler - Método: " + method);
        
        try {
            String response;
            
            if (path.startsWith("/api/airline/seats/") && "GET".equals(method)) {
                System.out.println("DEBUG: ✅ Patrón: /api/airline/seats/{flightId}");
                // Endpoint para obtener asientos disponibles: /api/airline/seats/{flightId}
                String[] pathParts = path.split("/");
                System.out.println("DEBUG: 🔍 Partes del path: " + String.join(", ", pathParts));
                if (pathParts.length >= 5) {
                    String flightId = pathParts[4];
                    System.out.println("DEBUG: 🔍 ID del vuelo extraído: " + flightId);
                    response = getAvailableSeatsJson(flightId);
                } else {
                    System.out.println("DEBUG: ❌ Partes del path insuficientes: " + pathParts.length);
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else {
                System.out.println("DEBUG: ❌ Ningún patrón coincidió");
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
    
    public String getAvailableSeatsJson(String flightId) {
        try {
            System.out.println("DEBUG: 🪑 Obteniendo asientos disponibles para vuelo: " + flightId);
            
            Integer id = Integer.parseInt(flightId);
            
            // Consultar la base de datos para obtener los asientos reales del vuelo
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flightId", id);
            
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Obtener el inventario de asientos del vuelo desde FLIGHT_INVENTORY
                String hql = "FROM FlightInventory fi WHERE fi.flight.idFlight = :flightId AND fi.status = 'ACTIVE'";
                List<FlightInventory> inventory = session.createQuery(hql, FlightInventory.class)
                    .setParameter("flightId", id)
                    .getResultList();
                
                if (inventory.isEmpty()) {
                    System.out.println("DEBUG: ⚠️ No se encontró inventario para el vuelo " + id);
                    // Si no hay inventario, devolver respuesta vacía
                    response.put("seatsByCategory", new HashMap<>());
                    response.put("seatInfo", Map.of(
                        "totalSeats", 0,
                        "firstClassCount", 0,
                        "businessCount", 0,
                        "economyCount", 0
                    ));
                    return gson.toJson(response);
                }
                
                // Generar asientos disponibles según el inventario real
                Map<String, Object> seatsByCategory = new HashMap<>();
                int totalSeats = 0;
                int firstClassCount = 0;
                int businessCount = 0;
                int economyCount = 0;
                
                for (FlightInventory item : inventory) {
                    String category = item.getSeatCategory();
                    int availableSeats = item.getAvailableSeats();
                    
                    if (availableSeats > 0) {
                        List<String> seats = generateSeatsForCategory(category, availableSeats);
                        seatsByCategory.put(category, seats);
                        
                        // Contar asientos por categoría
                        switch (category) {
                            case "FIRST_CLASS":
                                firstClassCount = availableSeats;
                                break;
                            case "BUSINESS":
                                businessCount = availableSeats;
                                break;
                            case "ECONOMY":
                                economyCount = availableSeats;
                                break;
                        }
                        totalSeats += availableSeats;
                    }
                }
                
                response.put("seatsByCategory", seatsByCategory);
                
                // Agregar información adicional basada en el inventario real
                Map<String, Object> seatInfo = new HashMap<>();
                seatInfo.put("totalSeats", totalSeats);
                seatInfo.put("firstClassCount", firstClassCount);
                seatInfo.put("businessCount", businessCount);
                seatInfo.put("economyCount", economyCount);
                response.put("seatInfo", seatInfo);
                
                System.out.println("DEBUG: ✅ Asientos obtenidos del inventario real");
                System.out.println("DEBUG: 📊 Total asientos: " + totalSeats);
                System.out.println("DEBUG: 🪑 Primera Clase: " + firstClassCount);
                System.out.println("DEBUG: 🪑 Business: " + businessCount);
                System.out.println("DEBUG: 🪑 Economy: " + economyCount);
                
                return gson.toJson(response);
            }
            
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de vuelo inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo vuelo: " + e.getMessage()));
        }
    }
    
    /**
     * Genera asientos para una categoría específica basándose en la cantidad disponible
     */
    private List<String> generateSeatsForCategory(String category, int availableSeats) {
        List<String> seats = new ArrayList<>();
        
        // Definir patrones de asientos por categoría
        switch (category) {
            case "FIRST_CLASS":
                // Primera Clase: Filas A-F, 6 asientos por fila
                for (char row = 'A'; row <= 'F' && seats.size() < availableSeats; row++) {
                    for (int seat = 1; seat <= 6 && seats.size() < availableSeats; seat++) {
                        seats.add(row + String.valueOf(seat));
                    }
                }
                break;
                
            case "BUSINESS":
                // Business: Filas G-M, 6 asientos por fila
                for (char row = 'G'; row <= 'M' && seats.size() < availableSeats; row++) {
                    for (int seat = 1; seat <= 6 && seats.size() < availableSeats; seat++) {
                        seats.add(row + String.valueOf(seat));
                    }
                }
                break;
                
            case "ECONOMY":
                // Economy: Filas N-Z, 6 asientos por fila
                for (char row = 'N'; row <= 'Z' && seats.size() < availableSeats; row++) {
                    for (int seat = 1; seat <= 6 && seats.size() < availableSeats; seat++) {
                        seats.add(row + String.valueOf(seat));
                    }
                }
                break;
        }
        
        // Limitar a la cantidad real disponible
        if (seats.size() > availableSeats) {
            seats = seats.subList(0, availableSeats);
        }
        
        return seats;
    }
}
