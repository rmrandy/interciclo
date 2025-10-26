package com.sources.app.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sources.app.util.HibernateUtil;
import com.sources.app.entities.FlightInventory;
import com.sources.app.entities.Flight;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Handler HTTP específico para operaciones de inventario de vuelos.
 * Implementa HttpHandler para compatibilidad con App.java
 */
public class InventoryHandler implements HttpHandler {
    private final Gson gson;
    
    public InventoryHandler() {
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
        
        System.out.println("DEBUG: 🔍 InventoryHandler - Path recibido: " + path);
        System.out.println("DEBUG: 🔍 InventoryHandler - Método: " + method);
        
        try {
            String response;
            
            if (path.startsWith("/api/airline/inventory/") && "GET".equals(method)) {
                // Endpoint para obtener inventario completo: /api/airline/inventory/{flightId}
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5) {
                    String flightId = pathParts[4];
                    response = getFlightInventoryJson(flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else if (path.startsWith("/api/airline/inventory/") && path.endsWith("/summary") && "GET".equals(method)) {
                // Endpoint para obtener resumen de inventario: /api/airline/inventory/{flightId}/summary
                String[] pathParts = path.split("/");
                if (pathParts.length >= 6) {
                    String flightId = pathParts[4];
                    response = getFlightInventorySummaryJson(flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
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
            String errorResponse = gson.toJson(Map.of("success", false, "error", "Error interno del servidor: " + e.getMessage()));
            byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
            
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(500, errorBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
        }
    }
    
    /**
     * Obtiene el inventario completo de un vuelo con información detallada por categoría
     */
    public String getFlightInventoryJson(String flightId) {
        try {
            System.out.println("DEBUG: 🪑 Obteniendo inventario completo para vuelo: " + flightId);
            
            Integer id = Integer.parseInt(flightId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flightId", id);
            
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Obtener el vuelo para información adicional
                Flight flight = session.get(Flight.class, id);
                if (flight == null) {
                    return gson.toJson(Map.of("success", false, "error", "Vuelo no encontrado"));
                }
                
                // Obtener el inventario de asientos del vuelo desde FLIGHT_INVENTORY
                String hql = "FROM FlightInventory fi WHERE fi.flight.idFlight = :flightId AND fi.status = 'ACTIVE' ORDER BY fi.seatCategory";
                List<FlightInventory> inventory = session.createQuery(hql, FlightInventory.class)
                    .setParameter("flightId", id)
                    .getResultList();
                
                if (inventory.isEmpty()) {
                    System.out.println("DEBUG: ⚠️ No se encontró inventario para el vuelo " + id);
                    // Si no hay inventario, devolver estructura vacía
                    response.put("inventory", new HashMap<>());
                    response.put("summary", Map.of(
                        "totalSeats", 0,
                        "availableSeats", 0,
                        "reservedSeats", 0,
                        "soldSeats", 0,
                        "categories", new ArrayList<>()
                    ));
                    return gson.toJson(response);
                }
                
                // Construir inventario por categoría
                Map<String, Object> inventoryByCategory = new HashMap<>();
                int totalSeats = 0;
                int totalAvailable = 0;
                int totalReserved = 0;
                int totalSold = 0;
                List<String> categories = new ArrayList<>();
                
                for (FlightInventory item : inventory) {
                    String category = item.getSeatCategory();
                    categories.add(category);
                    
                    Map<String, Object> categoryInfo = new HashMap<>();
                    categoryInfo.put("totalSeats", item.getTotalSeats());
                    categoryInfo.put("availableSeats", item.getAvailableSeats());
                    categoryInfo.put("reservedSeats", item.getReservedSeats());
                    categoryInfo.put("soldSeats", item.getSoldSeats());
                    categoryInfo.put("status", item.getStatus());
                    
                    inventoryByCategory.put(category, categoryInfo);
                    
                    // Acumular totales
                    totalSeats += item.getTotalSeats();
                    totalAvailable += item.getAvailableSeats();
                    totalReserved += item.getReservedSeats();
                    totalSold += item.getSoldSeats();
                }
                
                response.put("inventory", inventoryByCategory);
                
                // Agregar resumen general
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalSeats", totalSeats);
                summary.put("availableSeats", totalAvailable);
                summary.put("reservedSeats", totalReserved);
                summary.put("soldSeats", totalSold);
                summary.put("categories", categories);
                summary.put("occupancyRate", totalSeats > 0 ? Math.round((double)(totalSold + totalReserved) / totalSeats * 100.0) / 100.0 : 0.0);
                
                response.put("summary", summary);
                
                System.out.println("DEBUG: ✅ Inventario obtenido exitosamente");
                System.out.println("DEBUG: 📊 Total asientos: " + totalSeats);
                System.out.println("DEBUG: 🪑 Disponibles: " + totalAvailable);
                System.out.println("DEBUG: 🔒 Reservados: " + totalReserved);
                System.out.println("DEBUG: 💰 Vendidos: " + totalSold);
                
                return gson.toJson(response);
            }
            
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de vuelo inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo inventario: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene un resumen del inventario de un vuelo
     */
    public String getFlightInventorySummaryJson(String flightId) {
        try {
            System.out.println("DEBUG: 📊 Obteniendo resumen de inventario para vuelo: " + flightId);
            
            Integer id = Integer.parseInt(flightId);
            
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Obtener solo el resumen del inventario
                String hql = "SELECT fi.seatCategory, " +
                           "SUM(fi.totalSeats), " +
                           "SUM(fi.availableSeats), " +
                           "SUM(fi.reservedSeats), " +
                           "SUM(fi.soldSeats) " +
                           "FROM FlightInventory fi " +
                           "WHERE fi.flight.idFlight = :flightId AND fi.status = 'ACTIVE' " +
                           "GROUP BY fi.seatCategory";
                
                List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("flightId", id)
                    .getResultList();
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("flightId", id);
                
                if (results.isEmpty()) {
                    response.put("summary", Map.of(
                        "totalSeats", 0,
                        "availableSeats", 0,
                        "reservedSeats", 0,
                        "soldSeats", 0
                    ));
                    return gson.toJson(response);
                }
                
                int totalSeats = 0;
                int totalAvailable = 0;
                int totalReserved = 0;
                int totalSold = 0;
                
                for (Object[] result : results) {
                    totalSeats += (Integer) result[1];
                    totalAvailable += (Integer) result[2];
                    totalReserved += (Integer) result[3];
                    totalSold += (Integer) result[4];
                }
                
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalSeats", totalSeats);
                summary.put("availableSeats", totalAvailable);
                summary.put("reservedSeats", totalReserved);
                summary.put("soldSeats", totalSold);
                summary.put("occupancyRate", totalSeats > 0 ? Math.round((double)(totalSold + totalReserved) / totalSeats * 100.0) / 100.0 : 0.0);
                
                response.put("summary", summary);
                
                return gson.toJson(response);
            }
            
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de vuelo inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo resumen: " + e.getMessage()));
        }
    }
}

