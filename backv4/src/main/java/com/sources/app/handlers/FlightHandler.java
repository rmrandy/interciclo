package com.sources.app.handlers;

import com.sources.app.dao.FlightDAO;
import com.sources.app.entities.Flight;
import com.sources.app.entities.City;
// import com.sources.app.dao.CityDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
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
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.sources.app.entities.FlightLeg;
import com.sources.app.dao.FlightLegDAO;
import com.sources.app.entities.User;
// import com.sources.app.dao.UserDAO;
import org.hibernate.Session;
// import org.hibernate.query.Query;
import com.sources.app.util.HibernateUtil;
import com.sources.app.entities.FlightInventory;
import com.sources.app.dao.FlightReviewDAO;
import com.sources.app.entities.FlightReview;

/**
 * Handler HTTP para operaciones de vuelos.
 * Implementa HttpHandler para compatibilidad con App.java
 */
public class FlightHandler implements HttpHandler {
    private final FlightDAO flightDAO;
    // private final CityDAO cityDAO;
    private final Gson gson;
    
    public FlightHandler() {
        this.flightDAO = new FlightDAO();
        // this.cityDAO = new CityDAO();
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
            } else if ((path.equals("/api/airline/flights/bulk") || path.startsWith("/api/airline/flights/bulk")) && "POST".equals(method)) {
                // Creación masiva de vuelos a partir de un JSON con arreglo de vuelos
                response = handleBulkCreateFlights(exchange);
            } else if (path.startsWith("/api/airline/flights/") && path.endsWith("/stopover") && "POST".equals(method)) {
                // Agregar una escala simple ignorando horas/fechas
                String[] parts = path.split("/");
                if (parts.length >= 6) {
                    String flightId = parts[4];
                    response = handleCreateSimpleStopover(exchange, flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
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
            } else if (path.startsWith("/api/airline/flights/") && path.endsWith("/cancel") && "PUT".equals(method)) {
                System.out.println("DEBUG: ✅ Patrón 2.1: /api/airline/flights/{id}/cancel");
                // Endpoint para cancelar vuelos: /api/airline/flights/{id}/cancel
                String[] pathParts = path.split("/");
                if (pathParts.length >= 6) {
                    String flightId = pathParts[4];
                    response = handleCancelFlight(exchange, flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else if (path.startsWith("/api/airline/flights/") && path.endsWith("/status") && "PUT".equals(method)) {
                System.out.println("DEBUG: ✅ Patrón 2.2: /api/airline/flights/{id}/status");
                // Endpoint para actualizar estado: /api/airline/flights/{id}/status
                String[] pathParts = path.split("/");
                if (pathParts.length >= 6) {
                    String flightId = pathParts[4];
                    response = handleUpdateFlightStatus(exchange, flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else if (path.startsWith("/api/airline/flights/") && "PUT".equals(method)) {
                System.out.println("DEBUG: ✅ Patrón 2.3: /api/airline/flights/{id} (PUT)");
                // Endpoint para editar vuelos: PUT /api/airline/flights/{id}
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5) {
                    String flightId = pathParts[4];
                    response = handleUpdateFlight(exchange, flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else if (path.startsWith("/api/airline/flights/") && "DELETE".equals(method)) {
                System.out.println("DEBUG: ✅ Patrón 2.4: /api/airline/flights/{id} (DELETE)");
                // Endpoint para eliminar vuelos: DELETE /api/airline/flights/{id}
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5) {
                    String flightId = pathParts[4];
                    response = handleDeleteFlight(flightId);
                } else {
                    response = gson.toJson(Map.of("success", false, "error", "ID de vuelo no válido"));
                }
            } else if (path.matches("/api/airline/flights/\\d+/reviews") && "GET".equals(method)) {
                Integer flightId = Integer.parseInt(path.split("/")[4]);
                String query = exchange.getRequestURI().getQuery();
                boolean tree = query != null && query.contains("mode=tree");
                response = tree ? getFlightReviewsTreeJson(flightId) : getFlightReviewsJson(flightId);
            } else if (path.matches("/api/airline/flights/\\d+/reviews") && "POST".equals(method)) {
                Integer flightId = Integer.parseInt(path.split("/")[4]);
                response = handleCreateReview(exchange, flightId);
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

    /**
     * Búsqueda de vuelos con una escala como fallback (ignora horas/fechas).
     * Query params: originCityId, destinationCityId
     * Devuelve:
     *  - direct: [] de vuelos directos
     *  - oneStop: [] de combinaciones { first, second, viaCity }
     */
    public String searchFlightsJson(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            java.util.Map<String, String> q = new java.util.HashMap<>();
            if (query != null) {
                for (String p : query.split("&")) {
                    String[] kv = p.split("=");
                    if (kv.length == 2) q.put(kv[0], java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            Integer originId = q.containsKey("originCityId") ? Integer.parseInt(q.get("originCityId")) : null;
            Integer destId   = q.containsKey("destinationCityId") ? Integer.parseInt(q.get("destinationCityId")) : null;
            if (originId == null || destId == null) {
                return gson.toJson(java.util.Map.of("success", false, "error", "Parámetros originCityId y destinationCityId requeridos"));
            }

            java.util.List<com.sources.app.entities.Flight> all = flightDAO.getAllFlights();

            // Directos
            java.util.List<java.util.Map<String, Object>> direct = new java.util.ArrayList<>();
            for (var f : all) {
                if (f.getOriginCity() != null && f.getDestinationCity() != null &&
                    java.util.Objects.equals(f.getOriginCity().getIdCity(), originId.longValue()) &&
                    java.util.Objects.equals(f.getDestinationCity().getIdCity(), destId.longValue())) {
                    direct.add(java.util.Map.of(
                        "idFlight", f.getIdFlight(),
                        "flightNumber", f.getFlightNumber(),
                        "originCityId", f.getOriginCity().getIdCity(),
                        "destinationCityId", f.getDestinationCity().getIdCity()
                    ));
                }
            }

            // Si no hay directos, buscar una escala: origin -> via y via -> dest
            java.util.List<java.util.Map<String, Object>> oneStop = new java.util.ArrayList<>();
            if (direct.isEmpty()) {
                // Armar índices rápidos por origen y por destino
                java.util.Map<Long, java.util.List<com.sources.app.entities.Flight>> byOrigin = new java.util.HashMap<>();
                java.util.Map<Long, java.util.List<com.sources.app.entities.Flight>> byDest = new java.util.HashMap<>();
                for (var f : all) {
                    if (f.getOriginCity() != null && f.getDestinationCity() != null) {
                        byOrigin.computeIfAbsent(f.getOriginCity().getIdCity(), k -> new java.util.ArrayList<>()).add(f);
                        byDest.computeIfAbsent(f.getDestinationCity().getIdCity(), k -> new java.util.ArrayList<>()).add(f);
                    }
                }

                java.util.List<com.sources.app.entities.Flight> fromOrigin = byOrigin.getOrDefault(originId.longValue(), java.util.List.of());
                java.util.List<com.sources.app.entities.Flight> toDest = byDest.getOrDefault(destId.longValue(), java.util.List.of());

                // Buscar ciudad intermedia común: destino del primer tramo == origen del segundo tramo
                for (var f1 : fromOrigin) {
                    Long via = f1.getDestinationCity() != null ? f1.getDestinationCity().getIdCity() : null;
                    if (via == null) continue;
                    for (var f2 : toDest) {
                        Long via2 = f2.getOriginCity() != null ? f2.getOriginCity().getIdCity() : null;
                        if (via2 != null && via.equals(via2)) {
                            oneStop.add(java.util.Map.of(
                                "viaCityId", via.longValue(),
                                "first", java.util.Map.of(
                                    "idFlight", f1.getIdFlight(),
                                    "flightNumber", f1.getFlightNumber(),
                                    "originCityId", f1.getOriginCity().getIdCity().longValue(),
                                    "destinationCityId", f1.getDestinationCity().getIdCity().longValue()
                                ),
                                "second", java.util.Map.of(
                                    "idFlight", f2.getIdFlight(),
                                    "flightNumber", f2.getFlightNumber(),
                                    "originCityId", f2.getOriginCity().getIdCity().longValue(),
                                    "destinationCityId", f2.getDestinationCity().getIdCity().longValue()
                                )
                            ));
                        }
                    }
                }
            }

            return gson.toJson(java.util.Map.of(
                "success", true,
                "direct", direct,
                "oneStop", oneStop
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }
    /**
     * Crea múltiples vuelos con un solo request.
     * Acepta:
     *  - Un arreglo JSON de vuelos
     *  - Un objeto { flights: [...] }
     * Cada elemento debe seguir el mismo formato que el POST /api/airline/flights
     */
    private String handleBulkCreateFlights(HttpExchange exchange) throws IOException {
        try {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (body == null || body.trim().isEmpty()) {
                return gson.toJson(Map.of("success", false, "error", "Body vacío"));
            }

            // Soporte básico de multipart/form-data (archivo con JSON)
            if (contentType != null && contentType.toLowerCase().startsWith("multipart/form-data") && body.contains("Content-Disposition")) {
                // Separar por doble CRLF y tomar el contenido de la última parte
                String[] parts = body.split("\r\n\r\n");
                if (parts.length >= 2) {
                    String candidate = parts[parts.length - 1].trim();
                    if (candidate.endsWith("--")) {
                        candidate = candidate.substring(0, candidate.length() - 2).trim();
                    }
                    body = candidate;
                }
            }

            // Normalizar a JsonArray (parser tolerante)
            JsonArray items;
            JsonElement parsed;
            try {
                JsonReader reader = new JsonReader(new java.io.StringReader(body));
                reader.setLenient(true);
                parsed = JsonParser.parseReader(reader);
            } catch (Exception pe) {
                return gson.toJson(Map.of("success", false, "error", "JSON inválido: " + pe.getMessage()));
            }
            if (parsed.isJsonArray()) {
                items = parsed.getAsJsonArray();
            } else if (parsed.isJsonObject() && parsed.getAsJsonObject().has("flights")) {
                items = parsed.getAsJsonObject().getAsJsonArray("flights");
            } else {
                return gson.toJson(Map.of("success", false, "error", "Formato no soportado. Envíe un arreglo o {flights:[...]}") );
            }

            java.util.List<java.util.Map<String, Object>> created = new java.util.ArrayList<>();
            java.util.List<java.util.Map<String, Object>> errors  = new java.util.ArrayList<>();

            int index = 0;
            for (var el : items) {
                index++;
                try {
                    if (!el.isJsonObject()) {
                        errors.add(Map.of("index", index, "error", "Elemento no es objeto"));
                        continue;
                    }
                    JsonObject obj = el.getAsJsonObject();
                    String result = createFlightFromJson(obj.toString());
                    JsonObject resJson = JsonParser.parseString(result).getAsJsonObject();
                    if (resJson.has("success") && resJson.get("success").getAsBoolean()) {
                        java.util.Map<String, Object> entry = new java.util.HashMap<>();
                        entry.put("index", index);
                        if (resJson.has("flight")) entry.put("flight", resJson.get("flight"));
                        created.add(entry);
                    } else {
                        String err = resJson.has("error") ? resJson.get("error").getAsString() : "Error desconocido";
                        errors.add(Map.of("index", index, "error", err));
                    }
                } catch (Exception e) {
                    errors.add(Map.of("index", index, "error", e.getMessage()));
                }
            }

            java.util.Map<String, Object> out = new java.util.HashMap<>();
            out.put("success", errors.isEmpty());
            out.put("createdCount", created.size());
            out.put("errorCount", errors.size());
            out.put("created", created);
            if (!errors.isEmpty()) out.put("errors", errors);
            return gson.toJson(out);
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Crea una relación de escala (flight -> stopoverCity -> destination) ignorando horas/fechas.
     * Body esperado:
     * {
     *   "stopoverCityId": 10,
     *   "createdBy": 1
     * }
     */
    private String handleCreateSimpleStopover(HttpExchange exchange, String flightId) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            Integer id = Integer.parseInt(flightId);
            Integer stopCity = json.get("stopoverCityId").getAsInt();
            Integer userId = json.has("createdBy") ? json.get("createdBy").getAsInt() : 1;

            // Obtener vuelo y sus ciudades
            com.sources.app.entities.Flight flight = flightDAO.getFlightById(id);
            if (flight == null) return gson.toJson(Map.of("success", false, "error", "Vuelo no encontrado"));
            com.sources.app.entities.City stopCityEnt = getCityById(stopCity);
            if (stopCityEnt == null) return gson.toJson(Map.of("success", false, "error", "Ciudad de escala no encontrada"));
            com.sources.app.entities.User creator = getUserById(userId);

            // Crear dos legs como relación simple: origen -> escala y escala -> destino
            FlightLegDAO legDao = new FlightLegDAO();
            String now = java.time.LocalDateTime.now().toString();

            // leg 1: origen -> escala
            org.hibernate.Session s1 = com.sources.app.util.HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction tx1 = s1.beginTransaction();
            try {
                com.sources.app.entities.FlightLeg leg1 = new com.sources.app.entities.FlightLeg();
                leg1.setFlight(flight);
                leg1.setCity(stopCityEnt);
                leg1.setLegOrder(1);
                leg1.setArrivalTime("00:00");
                leg1.setDepartureTime("00:00");
                leg1.setConnectionTimeMinutes(0);
                leg1.setAircraftChange("N");
                leg1.setCreatedBy(creator);
                leg1.setCreatedAt(now);
                leg1.setUpdatedAt(now);
                s1.persist(leg1);
                tx1.commit();
            } catch (Exception e) {
                if (tx1 != null && tx1.getStatus().canRollback()) tx1.rollback();
                s1.close();
                throw e;
            }
            s1.close();

            // leg 2: escala -> destino (opcional, podemos registrar otra entrada del mismo modo)
            org.hibernate.Session s2 = com.sources.app.util.HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction tx2 = s2.beginTransaction();
            try {
                com.sources.app.entities.FlightLeg leg2 = new com.sources.app.entities.FlightLeg();
                leg2.setFlight(flight);
                // Reusar destino real del vuelo
                leg2.setCity(flight.getDestinationCity());
                leg2.setLegOrder(2);
                leg2.setArrivalTime("00:00");
                leg2.setDepartureTime("00:00");
                leg2.setConnectionTimeMinutes(0);
                leg2.setAircraftChange("N");
                leg2.setCreatedBy(creator);
                leg2.setCreatedAt(now);
                leg2.setUpdatedAt(now);
                s2.persist(leg2);
                tx2.commit();
            } catch (Exception e) {
                if (tx2 != null && tx2.getStatus().canRollback()) tx2.rollback();
                s2.close();
                throw e;
            }
            s2.close();

            return gson.toJson(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String getFlightReviewsJson(Integer flightId) {
        try {
            FlightReviewDAO dao = new FlightReviewDAO();
            java.util.List<FlightReview> list = dao.listByFlight(flightId);
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (FlightReview r : list) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("idReview", r.getIdReview());
                m.put("userId", r.getUser() != null ? r.getUser().getIdUser() : null);
                if (r.getUser() != null) {
                    java.util.Map<String, Object> u = new java.util.HashMap<>();
                    u.put("idUser", r.getUser().getIdUser());
                    try { u.put("firstName", r.getUser().getFirstName()); } catch (Exception ignore) {}
                    try { u.put("lastName", r.getUser().getLastName()); } catch (Exception ignore) {}
                    try { u.put("name", r.getUser().getName()); } catch (Exception ignore) {}
                    try { u.put("email", r.getUser().getEmail()); } catch (Exception ignore) {}
                    m.put("user", u);
                }
                m.put("rating", r.getRating());
                m.put("comment", r.getComment());
                m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
                m.put("parentReviewId", r.getParentReviewId());
                out.add(m);
            }
            return gson.toJson(Map.of("success", true, "reviews", out));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String getFlightReviewsTreeJson(Integer flightId) {
        try {
            FlightReviewDAO dao = new FlightReviewDAO();
            java.util.List<java.util.Map<String, Object>> roots = dao.listByFlightAsTree(flightId);
            return gson.toJson(Map.of("success", true, "reviews", roots));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String handleCreateReview(HttpExchange exchange, Integer flightId) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
            Integer userId = json.get("userId").getAsInt();
            Integer rating = json.get("rating").getAsInt();
            String comment = json.has("comment") && !json.get("comment").isJsonNull() ? json.get("comment").getAsString() : "";
            Long parent = (json.has("parentReviewId") && !json.get("parentReviewId").isJsonNull()) ? json.get("parentReviewId").getAsLong() : null;
            if (rating < 1 || rating > 5) return gson.toJson(Map.of("success", false, "error", "Rating debe ser 1-5"));
            FlightReviewDAO dao = new FlightReviewDAO();
            FlightReview r = dao.create(flightId, userId, rating, comment, parent);
            return gson.toJson(Map.of("success", true, "reviewId", r.getIdReview()));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
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
            Integer aircraftIdParam = null;
            if (jsonRequest.has("aircraftId") && !jsonRequest.get("aircraftId").isJsonNull()) {
                try {
                    aircraftIdParam = jsonRequest.get("aircraftId").getAsInt();
                } catch (Exception ignore) {}
            }
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
                jsonRequest.get("createdBy").getAsInt(),
                aircraftIdParam
            );
            
            if (flight != null) {
                // Si NO se asignó aeronave, crear tarifas desde el payload.
                if (fares != null && (aircraftIdParam == null || aircraftIdParam <= 0)) {
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
    
    // ===== NUEVOS MÉTODOS PARA GESTIÓN COMPLETA DE VUELOS =====
    
    /**
     * Maneja la cancelación de un vuelo
     */
    public String handleCancelFlight(HttpExchange exchange, String flightId) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("DEBUG: 🚫 Cancelando vuelo: " + flightId);
            System.out.println("DEBUG: 📝 Body recibido: " + requestBody);
            
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            // Validar campos requeridos para cancelación
            if (!jsonRequest.has("cancellationReason") || jsonRequest.get("cancellationReason").getAsString().trim().isEmpty()) {
                return gson.toJson(Map.of("success", false, "error", "Motivo de cancelación es requerido"));
            }
            
            if (!jsonRequest.has("cancelledBy") || jsonRequest.get("cancelledBy").getAsInt() <= 0) {
                return gson.toJson(Map.of("success", false, "error", "ID del usuario que cancela es requerido"));
            }
            
            Integer id = Integer.parseInt(flightId);
            String cancellationReason = jsonRequest.get("cancellationReason").getAsString();
            Integer cancelledBy = jsonRequest.get("cancelledBy").getAsInt();
            
            // Cancelar el vuelo usando el DAO
            boolean success = flightDAO.cancelFlight(id, cancellationReason, cancelledBy);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Vuelo cancelado exitosamente");
                response.put("flightId", id);
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "No se pudo cancelar el vuelo"));
            }
            
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de vuelo inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error cancelando vuelo: " + e.getMessage()));
        }
    }
    
    /**
     * Maneja la actualización del estado de un vuelo
     */
    private String handleUpdateFlightStatus(HttpExchange exchange, String flightId) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("DEBUG: 🔄 Actualizando estado del vuelo: " + flightId);
            System.out.println("DEBUG: 📝 Body recibido: " + requestBody);
            
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            // Validar campos requeridos
            if (!jsonRequest.has("status") || jsonRequest.get("status").getAsString().trim().isEmpty()) {
                return gson.toJson(Map.of("success", false, "error", "Nuevo estado es requerido"));
            }
            
            if (!jsonRequest.has("updatedBy") || jsonRequest.get("updatedBy").getAsInt() <= 0) {
                return gson.toJson(Map.of("success", false, "error", "ID del usuario que actualiza es requerido"));
            }
            
            Integer id = Integer.parseInt(flightId);
            String newStatus = jsonRequest.get("status").getAsString();
            Integer updatedBy = jsonRequest.get("updatedBy").getAsInt();
            
            // Validar que el estado sea válido
            if (!isValidFlightStatus(newStatus)) {
                return gson.toJson(Map.of("success", false, "error", "Estado inválido: " + newStatus));
            }
            
            // Actualizar el estado del vuelo usando el DAO
            boolean success = flightDAO.updateFlightStatus(id, newStatus, updatedBy);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Estado del vuelo actualizado exitosamente");
                response.put("flightId", id);
                response.put("newStatus", newStatus);
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "No se pudo actualizar el estado del vuelo"));
            }
            
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de vuelo inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error actualizando estado: " + e.getMessage()));
        }
    }
    
    /**
     * Maneja la actualización completa de un vuelo
     */
    private String handleUpdateFlight(HttpExchange exchange, String flightId) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("DEBUG: ✏️ Actualizando vuelo: " + flightId);
            System.out.println("DEBUG: 📝 Body recibido: " + requestBody);
            
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            // Validar campos requeridos
            if (!validateUpdateFlightRequest(jsonRequest)) {
                return gson.toJson(Map.of("success", false, "error", "Datos de actualización inválidos"));
            }
            
            Integer id = Integer.parseInt(flightId);
            
            // Verificar que el vuelo existe
            Flight existingFlight = flightDAO.getFlightById(id);
            if (existingFlight == null) {
                return gson.toJson(Map.of("success", false, "error", "Vuelo no encontrado"));
            }
            
            // Verificar que el vuelo puede ser modificado
            if (!existingFlight.canBeModified()) {
                return gson.toJson(Map.of("success", false, "error", "El vuelo no puede ser modificado en su estado actual"));
            }
            
            // Actualizar el vuelo usando el DAO
            Flight updatedFlight = flightDAO.updateFlight(id, jsonRequest);
            
            if (updatedFlight != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Vuelo actualizado exitosamente");
                response.put("flight", updatedFlight);
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "No se pudo actualizar el vuelo"));
            }
            
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de vuelo inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error actualizando vuelo: " + e.getMessage()));
        }
    }
    
    /**
     * Maneja la eliminación de un vuelo
     */
    private String handleDeleteFlight(String flightId) {
        try {
            System.out.println("DEBUG: 🗑️ Eliminando vuelo: " + flightId);
            
            Integer id = Integer.parseInt(flightId);
            
            // Verificar que el vuelo existe
            Flight existingFlight = flightDAO.getFlightById(id);
            if (existingFlight == null) {
                return gson.toJson(Map.of("success", false, "error", "Vuelo no encontrado"));
            }
            
            // Verificar que el vuelo puede ser eliminado
            if (!existingFlight.isDraft()) {
                return gson.toJson(Map.of("success", false, "error", "Solo se pueden eliminar vuelos en estado DRAFT"));
            }
            
            // Eliminar el vuelo usando el DAO
            boolean success = flightDAO.deleteFlight(id);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Vuelo eliminado exitosamente");
                response.put("flightId", id);
                return gson.toJson(response);
            } else {
                return gson.toJson(Map.of("success", false, "error", "No se pudo eliminar el vuelo"));
            }
            
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de vuelo inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error eliminando vuelo: " + e.getMessage()));
        }
    }
    
    /**
     * Valida que el estado del vuelo sea válido
     */
    private boolean isValidFlightStatus(String status) {
        return Flight.STATUS_DRAFT.equals(status) ||
               Flight.STATUS_PUBLISHED.equals(status) ||
               Flight.STATUS_CANCELLED.equals(status) ||
               Flight.STATUS_COMPLETED.equals(status);
    }
    
    /**
     * Valida los datos para actualizar un vuelo
     */
    private boolean validateUpdateFlightRequest(JsonObject request) {
        try {
            // Verificar campos requeridos básicos
            if (!request.has("updatedBy") || request.get("updatedBy").getAsInt() <= 0) {
                System.out.println("DEBUG: ❌ updatedBy inválido");
                return false;
            }
            
            // Verificar que al menos un campo sea actualizado
            boolean hasUpdates = false;
            String[] updatableFields = {
                "flightNumber", "originCityId", "destinationCityId", "departureDate", 
                "departureTime", "arrivalDate", "arrivalTime", "basePrice", 
                "availableSeats", "gate", "terminal", "checkInStart", 
                "checkInEnd", "boardingTime"
            };
            
            for (String field : updatableFields) {
                if (request.has(field)) {
                    hasUpdates = true;
                    break;
                }
            }
            
            if (!hasUpdates) {
                System.out.println("DEBUG: ❌ No hay campos para actualizar");
                return false;
            }
            
            // Validar campos específicos si están presentes
            if (request.has("originCityId") && request.has("destinationCityId")) {
                if (request.get("originCityId").getAsInt() == request.get("destinationCityId").getAsInt()) {
                    System.out.println("DEBUG: ❌ Origen y destino no pueden ser iguales");
                    return false;
                }
            }
            
            if (request.has("basePrice") && request.get("basePrice").getAsDouble() <= 0) {
                System.out.println("DEBUG: ❌ Precio base debe ser mayor a 0");
                return false;
            }
            
            if (request.has("availableSeats") && request.get("availableSeats").getAsInt() < 0) {
                System.out.println("DEBUG: ❌ Asientos disponibles no pueden ser negativos");
                return false;
            }
            
            System.out.println("DEBUG: ✅ Validación de actualización exitosa");
            return true;
            
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error en validación de actualización: " + e.getMessage());
            return false;
        }
    }
}
