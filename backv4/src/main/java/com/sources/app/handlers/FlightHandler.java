package com.sources.app.handlers;

import com.sources.app.dao.FlightDAO;
import com.sources.app.entities.Flight;
import com.sources.app.entities.City;
import com.sources.app.dao.CityDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.FlightLeg;
import com.sources.app.dao.FlightLegDAO;
import com.sources.app.entities.User;
import com.sources.app.dao.UserDAO;
import org.hibernate.Session;
import org.hibernate.query.Query;
import com.sources.app.util.HibernateUtil;
import com.sources.app.entities.FlightInventory;

/**
 * Handler HTTP para operaciones de vuelos.
 * Implementa HttpHandler para compatibilidad con App.java
 */
public class FlightHandler implements HttpHandler {
    private final FlightDAO flightDAO;
    private final CityDAO cityDAO;
    private final Gson gson;
    
    public FlightHandler() {
        this.flightDAO = new FlightDAO();
        this.cityDAO = new CityDAO();
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
        
        System.out.println("DEBUG: 🔍 Path recibido: " + path);
        System.out.println("DEBUG: 🔍 Método: " + method);
        
        try {
            String response;
            
            if (path.equals("/api/airline/flights")) {
                System.out.println("DEBUG: ✅ Patrón 1: /api/airline/flights");
                if ("GET".equals(method)) {
                    response = getAllFlightsJson();
                } else if ("POST".equals(method)) {
                    response = handleCreateFlight(exchange);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "Método no permitido: " + method));
                }
            } else if (path.startsWith("/api/airline/flights/") && path.endsWith("/seats") && "GET".equals(method)) {
                System.out.println("DEBUG: ✅ Patrón 2: /api/airline/flights/{id}/seats");
                // Endpoint para obtener asientos disponibles: /api/airline/flights/{id}/seats
                String[] pathParts = path.split("/");
                System.out.println("DEBUG: 🔍 Partes del path: " + String.join(", ", pathParts));
                if (pathParts.length >= 6) {
                    String flightId = pathParts[4];
                    System.out.println("DEBUG: 🔍 ID del vuelo extraído: " + flightId);
                    response = getAvailableSeatsJson(flightId);
                } else {
                    System.out.println("DEBUG: ❌ Partes del path insuficientes: " + pathParts.length);
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else if (path.startsWith("/api/airline/flights/") && "GET".equals(method)) {
                System.out.println("DEBUG: ✅ Patrón 3: /api/airline/flights/{id}");
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5) {
                    String flightId = pathParts[4];
                    response = getFlightByIdJson(flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else if (path.startsWith("/api/admin/flights/") && path.endsWith("/legs") && "POST".equals(method)) {
                System.out.println("DEBUG: ✅ Patrón 4: /api/admin/flights/{id}/legs");
                // Endpoint para crear escalas: /api/admin/flights/{id}/legs
                String[] pathParts = path.split("/");
                if (pathParts.length >= 6) {
                    String flightId = pathParts[4];
                    response = handleCreateFlightLeg(exchange, flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else if (path.startsWith("/api/admin/flights")) {
                System.out.println("DEBUG: ✅ Patrón 5: /api/admin/flights");
                if ("GET".equals(method)) {
                    response = getAllFlightsJson();
                } else if ("POST".equals(method)) {
                    response = handleCreateFlight(exchange);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "Método no permitido: " + method));
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
    
    private String handleCreateFlight(HttpExchange exchange) throws IOException {
        // Leer el body de la petición
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("DEBUG: POST /api/airline/flights detectado");
        System.out.println("DEBUG: Body recibido: " + requestBody);
        
        try {
            String result = createFlightFromJson(requestBody);
            System.out.println("DEBUG: Resultado: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error creando vuelo: " + e.getMessage()));
        }
    }
    
    private String handleCreateFlightLeg(HttpExchange exchange, String flightId) throws IOException {
        // Leer el body de la petición
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("DEBUG: POST /api/admin/flights/" + flightId + "/legs detectado");
        System.out.println("DEBUG: Body recibido: " + requestBody);
        
        try {
            String result = createFlightLegFromJson(requestBody, flightId);
            System.out.println("DEBUG: Resultado: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error creando escala: " + e.getMessage()));
        }
    }
    
    public String getAllFlightsJson() {
        try {
            List<Flight> flights = flightDAO.getAllFlights();
            List<Map<String, Object>> flightMaps = new ArrayList<>();
            
            for (Flight flight : flights) {
                Map<String, Object> flightMap = new HashMap<>();
                flightMap.put("idFlight", flight.getIdFlight());
                flightMap.put("flightNumber", flight.getFlightNumber());
                flightMap.put("originCity", flight.getOriginCity() != null ? flight.getOriginCity().getName() : "N/A");
                flightMap.put("destinationCity", flight.getDestinationCity() != null ? flight.getDestinationCity().getName() : "N/A");
                flightMap.put("departureDate", flight.getDepartureDate());
                flightMap.put("departureTime", flight.getDepartureTime());
                flightMap.put("arrivalDate", flight.getArrivalDate());
                flightMap.put("arrivalTime", flight.getArrivalTime());
                flightMap.put("basePrice", flight.getBasePrice());
                flightMap.put("availableSeats", flight.getAvailableSeats());
                flightMap.put("status", flight.getStatus());
                flightMap.put("gate", flight.getGate());
                flightMap.put("terminal", flight.getTerminal());
                flightMap.put("checkInStart", flight.getCheckInStart());
                flightMap.put("checkInEnd", flight.getCheckInEnd());
                flightMap.put("boardingTime", flight.getBoardingTime());
                
                // Agregar tarifas del vuelo
                Map<String, BigDecimal> fares = flightDAO.getFlightFares(flight.getIdFlight());
                flightMap.put("fares", fares);
                
                // Agregar información de escalas
                List<Map<String, Object>> legsList = new ArrayList<>();
                // if (flight.getFlightLegs() != null) {
                //     for (FlightLeg leg : flight.getFlightLegs()) {
                //         Map<String, Object> legMap = new HashMap<>();
                //         legMap.put("cityId", leg.getCity() != null ? leg.getCity().getIdCity() : null);
                //         legMap.put("arrivalTime", leg.getArrivalTime());
                //         legMap.put("departureTime", leg.getDepartureTime());
                //         legMap.put("connectionTimeMinutes", leg.getConnectionTimeMinutes());
                //         legMap.put("aircraftChange", leg.getAircraftChange());
                //         legsList.add(legMap);
                //     }
                // }
                flightMap.put("flightLegs", legsList);
                flightMap.put("hasStops", false); // Temporalmente deshabilitado
                
                flightMaps.add(flightMap);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flights", flightMaps);
            return gson.toJson(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error obteniendo vuelos: " + e.getMessage());
            return gson.toJson(response);
        }
    }
    
    public String getFlightByIdJson(String flightId) {
        try {
            Integer id = Integer.parseInt(flightId);
            Flight flight = flightDAO.getFlightById(id);
            if (flight != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("flight", flight);
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "Vuelo no encontrado"));
            }
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de vuelo inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo vuelo: " + e.getMessage()));
        }
    }
    
    public String createFlightFromJson(String jsonBody) {
        try {
            JsonObject jsonRequest = JsonParser.parseString(jsonBody).getAsJsonObject();
            
            // Validar campos requeridos
            if (!validateCreateFlightRequest(jsonRequest)) {
                return gson.toJson(Map.of("success", false, "error", "Datos de vuelo inválidos"));
            }
            
            // Obtener el precio económico como precio base
            JsonObject fares = jsonRequest.getAsJsonObject("fares");
            BigDecimal economyPrice = BigDecimal.ZERO;
            
            if (fares != null && fares.has("ECONOMY")) {
                economyPrice = BigDecimal.valueOf(fares.get("ECONOMY").getAsDouble());
            }
            
            if (economyPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return gson.toJson(Map.of("success", false, "error", "El precio económico debe ser mayor a 0"));
            }
            
            // Crear vuelo usando el DAO con el precio económico como base
            Flight flight = flightDAO.createSimpleFlight(
                jsonRequest.get("flightNumber").getAsString(),
                jsonRequest.get("originCityId").getAsInt(),
                jsonRequest.get("destinationCityId").getAsInt(),
                jsonRequest.get("departureDate").getAsString(),
                jsonRequest.get("departureTime").getAsString(),
                jsonRequest.get("arrivalDate").getAsString(),
                jsonRequest.get("arrivalTime").getAsString(),
                economyPrice, // Usar precio económico como base
                jsonRequest.get("availableSeats").getAsInt(),
                jsonRequest.get("createdBy").getAsInt()
            );
            
            if (flight != null) {
                // Crear tarifas para todas las categorías
                if (fares != null) {
                    Map<String, BigDecimal> faresMap = new HashMap<>();
                    fares.entrySet().forEach(entry -> {
                        if (entry.getValue().getAsDouble() > 0) {
                            faresMap.put(entry.getKey(), BigDecimal.valueOf(entry.getValue().getAsDouble()));
                        }
                    });
                    
                    if (!faresMap.isEmpty()) {
                        flightDAO.createFlightFares(flight, faresMap);
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Vuelo creado exitosamente");
                response.put("flight", flight);
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "Error al crear el vuelo"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error procesando datos: " + e.getMessage()));
        }
    }
    
    public String createFlightLegFromJson(String jsonBody, String flightId) {
        try {
            System.out.println("DEBUG: 🛫 Iniciando creación de escala para vuelo: " + flightId);
            System.out.println("DEBUG: 📝 JSON recibido: " + jsonBody);
            
            JsonObject jsonRequest = JsonParser.parseString(jsonBody).getAsJsonObject();
            
            // Validar campos requeridos para la escala
            if (!validateCreateFlightLegRequest(jsonRequest)) {
                System.out.println("DEBUG: ❌ Validación de escala falló");
                return gson.toJson(Map.of("success", false, "error", "Datos de escala inválidos"));
            }
            
            System.out.println("DEBUG: ✅ Validación de escala exitosa, procediendo...");
            
            // Obtener el vuelo
            Flight flight = flightDAO.getFlightById(Integer.parseInt(flightId));
            if (flight == null) {
                System.out.println("DEBUG: ❌ Vuelo no encontrado con ID: " + flightId);
                return gson.toJson(Map.of("success", false, "error", "Vuelo no encontrado"));
            }
            
            System.out.println("DEBUG: ✅ Vuelo encontrado: " + flight.getFlightNumber());
            
            // Obtener la ciudad de escala
            City stopoverCity = getCityById(jsonRequest.get("stopoverCityId").getAsInt());
            if (stopoverCity == null) {
                System.out.println("DEBUG: ❌ Ciudad de escala no encontrada con ID: " + jsonRequest.get("stopoverCityId").getAsInt());
                return gson.toJson(Map.of("success", false, "error", "Ciudad de escala no encontrada"));
            }
            
            System.out.println("DEBUG: ✅ Ciudad de escala encontrada: " + stopoverCity.getName());
            
            // Obtener el usuario creador (por defecto ID 1)
            User createdBy = getUserById(1);
            if (createdBy == null) {
                System.out.println("DEBUG: ❌ Usuario creador no encontrado con ID: 1");
                return gson.toJson(Map.of("success", false, "error", "Usuario creador no encontrado"));
            }
            
            System.out.println("DEBUG: ✅ Usuario creador encontrado: " + createdBy.getFirstName());
            
            // Crear la escala usando el DAO
            FlightLegDAO flightLegDAO = new FlightLegDAO();
            System.out.println("DEBUG: 🚀 Llamando a createFlightWithLegs...");
            
            boolean success = flightLegDAO.createFlightWithLegs(
                flight,
                stopoverCity,
                jsonRequest.get("stopoverArrivalTime").getAsString(),
                jsonRequest.get("stopoverDepartureTime").getAsString(),
                jsonRequest.get("connectionTimeMinutes").getAsInt(),
                jsonRequest.get("aircraftChange").getAsString(),
                createdBy
            );
            
            if (success) {
                System.out.println("DEBUG: ✅ Escalas creadas exitosamente");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Escalas creadas exitosamente para el vuelo " + flight.getFlightNumber());
                return gson.toJson(response);
            } else {
                System.out.println("DEBUG: ❌ Error al crear las escalas");
                return gson.toJson(Map.of("success", false, "error", "Error al crear las escalas"));
            }
            
        } catch (Exception e) {
            System.out.println("DEBUG: 💥 Error general en createFlightLegFromJson: " + e.getMessage());
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error procesando datos de escala: " + e.getMessage()));
        }
    }
    
    private boolean validateCreateFlightRequest(JsonObject request) {
        try {
            // Verificar campos requeridos
            if (!request.has("flightNumber") || request.get("flightNumber").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ flightNumber inválido");
                return false;
            }
            if (!request.has("originCityId") || request.get("originCityId").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ originCityId inválido");
                return false;
            }
            if (!request.has("destinationCityId") || request.get("destinationCityId").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ destinationCityId inválido");
                return false;
            }
            if (!request.has("departureDate") || request.get("departureDate").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ departureDate inválido");
                return false;
            }
            if (!request.has("departureTime") || request.get("departureTime").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ departureTime inválido");
                return false;
            }
            if (!request.has("arrivalDate") || request.get("arrivalDate").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ arrivalDate inválido");
                return false;
            }
            if (!request.has("arrivalTime") || request.get("arrivalTime").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ arrivalTime inválido");
                return false;
            }
            if (!request.has("availableSeats") || request.get("availableSeats").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ availableSeats inválido");
                return false;
            }
            if (!request.has("createdBy") || request.get("createdBy").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ createdBy inválido");
                return false;
            }
            
            // Verificar que tenga tarifas y al menos el precio económico
            if (!request.has("fares")) {
                System.out.println("DEBUG: ❌ No se encontraron tarifas");
                return false;
            }
            
            JsonObject fares = request.getAsJsonObject("fares");
            if (!fares.has("ECONOMY") || fares.get("ECONOMY").getAsDouble() <= 0) {
                System.out.println("DEBUG: ❌ Precio económico inválido o faltante");
                return false;
            }
            
            // Verificar que origen y destino sean diferentes
            if (request.get("originCityId").getAsInt() == request.get("destinationCityId").getAsInt()) {
                System.out.println("DEBUG: ❌ Origen y destino no pueden ser iguales");
                return false;
            }
            
            System.out.println("DEBUG: ✅ Validación de vuelo exitosa");
            return true;
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error en validación de vuelo: " + e.getMessage());
            return false;
        }
    }

    private boolean validateCreateFlightLegRequest(JsonObject request) {
        try {
            System.out.println("DEBUG: Validando datos de escala: " + request.toString());
            
            if (!request.has("stopoverCityId") || request.get("stopoverCityId").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ stopoverCityId inválido");
                return false;
            }
            if (!request.has("stopoverArrivalTime") || request.get("stopoverArrivalTime").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ stopoverArrivalTime inválido");
                return false;
            }
            if (!request.has("stopoverDepartureTime") || request.get("stopoverDepartureTime").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ stopoverDepartureTime inválido");
                return false;
            }
            if (!request.has("connectionTimeMinutes") || request.get("connectionTimeMinutes").getAsInt() < 0) {
                System.out.println("DEBUG: ❌ connectionTimeMinutes inválido");
                return false;
            }
            if (!request.has("aircraftChange") || request.get("aircraftChange").getAsString().trim().isEmpty()) {
                System.out.println("DEBUG: ❌ aircraftChange inválido");
                return false;
            }
            
            System.out.println("DEBUG: ✅ Validación de escala exitosa");
            return true;
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error en validación de escala: " + e.getMessage());
            return false;
        }
    }
    
    // Métodos auxiliares
    private City getCityById(int cityId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(City.class, cityId);
        } catch (Exception e) {
            return null;
        }
    }
    
    private User getUserById(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, userId);
        } catch (Exception e) {
            return null;
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
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo asientos: " + e.getMessage()));
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
