/*
// TEMPORALMENTE COMENTADO PARA PERMITIR COMPILACIÓN
// package com.sources.app.handlers;

// import com.google.gson.Gson;
// import com.google.gson.GsonBuilder;
// import com.google.gson.JsonSyntaxException;
// import com.sources.app.dao.FlightManagementDAO;
// import com.sources.app.entities.*;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.time.format.DateTimeFormatter;
// import java.util.List;
// import java.util.Map;
// import java.util.HashMap;

/**
 * Handler para gestión completa de vuelos - CRUD, inventario, precios, escalas
 * TEMPORALMENTE COMENTADO PARA PERMITIR COMPILACIÓN
 */
/*
public class FlightManagementHandler {
    
    private final FlightManagementDAO flightDAO;
    private final Gson gson;
    
    public FlightManagementHandler() {
        this.flightDAO = new FlightManagementDAO();
        this.gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();
    }
    
    // ==================== CREAR VUELO COMPLETO ====================
    
    public String createCompleteFlight(String requestBody) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> request = gson.fromJson(requestBody, Map.class);
            
            // Datos básicos del vuelo
            String flightNumber = (String) request.get("flightNumber");
            Integer routeId = ((Double) request.get("routeId")).intValue();
            Integer aircraftId = ((Double) request.get("aircraftId")).intValue();
            Integer createdBy = ((Double) request.get("createdBy")).intValue();
            
            // Fechas y horarios
            LocalDate departureDate = LocalDate.parse((String) request.get("departureDate"));
            LocalTime departureTime = LocalTime.parse((String) request.get("departureTime"));
            LocalDate arrivalDate = LocalDate.parse((String) request.get("arrivalDate"));
            LocalTime arrivalTime = LocalTime.parse((String) request.get("arrivalTime"));
            
            // Inventario por categoría
            @SuppressWarnings("unchecked")
            Map<String, Object> inventoryMap = (Map<String, Object>) request.get("inventory");
            Map<String, Integer> seatsByCategory = new HashMap<>();
            for (Map.Entry<String, Object> entry : inventoryMap.entrySet()) {
                seatsByCategory.put(entry.getKey(), ((Double) entry.getValue()).intValue());
            }
            
            // Precios por categoría
            @SuppressWarnings("unchecked")
            Map<String, Object> faresMap = (Map<String, Object>) request.get("fares");
            Map<String, BigDecimal> pricesByCategory = new HashMap<>();
            for (Map.Entry<String, Object> entry : faresMap.entrySet()) {
                pricesByCategory.put(entry.getKey(), BigDecimal.valueOf((Double) entry.getValue()));
            }
            
            // Crear vuelo completo
            Flight flight = flightDAO.createCompleteFlightWithInventoryAndFares(
                flightNumber, routeId, aircraftId,
                departureDate, departureTime, arrivalDate, arrivalTime,
                createdBy, seatsByCategory, pricesByCategory
            );
            
            // Agregar escalas si existen
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> legs = (List<Map<String, Object>>) request.get("legs");
            if (legs != null && !legs.isEmpty()) {
                for (Map<String, Object> legData : legs) {
                    Integer airportId = ((Double) legData.get("airportId")).intValue();
                    Integer legOrder = ((Double) legData.get("legOrder")).intValue();
                    LocalDateTime arrivalTimeLeg = LocalDateTime.parse((String) legData.get("arrivalTime"));
                    LocalDateTime departureTimeLeg = LocalDateTime.parse((String) legData.get("departureTime"));
                    Boolean aircraftChange = (Boolean) legData.get("aircraftChange");
                    
                    flightDAO.addFlightLeg(flight.getIdFlight(), airportId, legOrder, 
                        arrivalTimeLeg, departureTimeLeg, aircraftChange);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flightId", flight.getIdFlight());
            response.put("flightNumber", flight.getFlightNumber());
            response.put("message", "Vuelo creado exitosamente");
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al crear vuelo: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== LISTAR VUELOS ====================
    
    public String listFlights(String status, LocalDate fromDate, LocalDate toDate) {
        try {
            List<Flight> flights = flightDAO.listFlights(status, fromDate, toDate);
            
            // Convertir a formato de respuesta
            List<Map<String, Object>> flightResponses = new ArrayList<>();
            for (Flight flight : flights) {
                Map<String, Object> flightData = new HashMap<>();
                flightData.put("id", flight.getIdFlight());
                flightData.put("flightNumber", flight.getFlightNumber());
                flightData.put("status", flight.getStatus());
                flightData.put("departureDate", flight.getDepartureDate());
                flightData.put("departureTime", flight.getDepartureTime());
                flightData.put("arrivalDate", flight.getArrivalDate());
                flightData.put("arrivalTime", flight.getArrivalTime());
                flightData.put("basePrice", flight.getBasePrice());
                flightData.put("availableSeats", flight.getAvailableSeats());
                
                // Información de ruta
                Route route = flight.getRoute();
                if (route != null) {
                    Aeropuerto originAirport = route.getOriginAirport();
                    Aeropuerto destAirport = route.getDestinationAirport();
                    if (originAirport != null && destAirport != null) {
                        flightData.put("origin", originAirport.getCity());
                        flightData.put("destination", destAirport.getCity());
                    }
                }
                
                // Información de aeronave
                Aircraft aircraft = flight.getAircraft();
                if (aircraft != null) {
                    flightData.put("aircraftModel", aircraft.getModel());
                    flightData.put("aircraftRegistration", aircraft.getRegistration());
                    flightData.put("totalSeats", aircraft.getSeatCapacity());
                }
                
                flightResponses.add(flightData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flights", flightResponses);
            response.put("total", flightResponses.size());
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al listar vuelos: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== OBTENER DETALLES DE VUELO ====================
    
    public String getFlightDetails(String flightId) {
        try {
            Integer id = Integer.parseInt(flightId);
            Flight flight = flightDAO.getFlightById(id);
            
            if (flight == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Vuelo no encontrado");
                return gson.toJson(errorResponse);
            }
            
            // Información básica del vuelo
            Map<String, Object> flightData = new HashMap<>();
            flightData.put("id", flight.getIdFlight());
            flightData.put("flightNumber", flight.getFlightNumber());
            flightData.put("status", flight.getStatus());
            flightData.put("departureDate", flight.getDepartureDate());
            flightData.put("departureTime", flight.getDepartureTime());
            flightData.put("arrivalDate", flight.getArrivalDate());
            flightData.put("arrivalTime", flight.getArrivalTime());
            flightData.put("basePrice", flight.getBasePrice());
            flightData.put("availableSeats", flight.getAvailableSeats());
            flightData.put("createdAt", flight.getCreatedAt());
            flightData.put("updatedAt", flight.getUpdatedAt());
            
            // Información de ruta
            Route route = flight.getRoute();
            if (route != null) {
                Aeropuerto originAirport = route.getOriginAirport();
                Aeropuerto destAirport = route.getDestinationAirport();
                if (originAirport != null && destAirport != null) {
                    flightData.put("originAirport", originAirport.getNombre());
                    flightData.put("originCity", originAirport.getCity());
                    flightData.put("destinationAirport", destAirport.getNombre());
                    flightData.put("destinationCity", destAirport.getCity());
                }
            }
            
            // Información de aeronave
            Aircraft aircraft = flight.getAircraft();
            if (aircraft != null) {
                flightData.put("aircraftModel", aircraft.getModel());
                flightData.put("aircraftRegistration", aircraft.getRegistration());
                flightData.put("aircraftManufacturer", aircraft.getManufacturer());
                flightData.put("totalSeats", aircraft.getSeatCapacity());
            }
            
            // Escalas del vuelo
            List<FlightLeg> legs = flightDAO.getFlightLegs(flight.getIdFlight());
            List<Map<String, Object>> legResponses = new ArrayList<>();
            for (FlightLeg leg : legs) {
                Map<String, Object> legData = new HashMap<>();
                legData.put("id", leg.getIdLeg());
                legData.put("legOrder", leg.getLegOrder());
                legData.put("arrivalTime", leg.getArrivalTime());
                legData.put("departureTime", leg.getDepartureTime());
                legData.put("connectionTimeMinutes", leg.getConnectionTimeMinutes());
                legData.put("aircraftChange", leg.getAircraftChange());
                
                Aeropuerto airport = leg.getAirport();
                if (airport != null) {
                    legData.put("airportName", airport.getNombre());
                    legData.put("airportCode", airport.getCodigoIata());
                    legData.put("city", airport.getCity());
                }
                
                legResponses.add(legData);
            }
            flightData.put("legs", legResponses);
            
            // Inventario del vuelo
            List<FlightInventory> inventory = flightDAO.getFlightInventory(flight.getIdFlight());
            List<Map<String, Object>> inventoryResponses = new ArrayList<>();
            for (FlightInventory inv : inventory) {
                Map<String, Object> invData = new HashMap<>();
                invData.put("category", inv.getSeatCategory());
                invData.put("totalSeats", inv.getTotalSeats());
                invData.put("availableSeats", inv.getAvailableSeats());
                invData.put("reservedSeats", inv.getReservedSeats());
                invData.put("soldSeats", inv.getSoldSeats());
                invData.put("status", inv.getStatus());
                inventoryResponses.add(invData);
            }
            flightData.put("inventory", inventoryResponses);
            
            // Precios del vuelo
            List<FlightFare> fares = flightDAO.getFlightFares(flight.getIdFlight());
            List<Map<String, Object>> fareResponses = new ArrayList<>();
            for (FlightFare fare : fares) {
                Map<String, Object> fareData = new HashMap<>();
                fareData.put("category", fare.getSeatCategory());
                fareData.put("basePrice", fare.getBasePrice());
                fareData.put("currency", fare.getCurrency());
                fareData.put("taxes", fare.getTaxes());
                fareData.put("fees", fare.getFees());
                fareData.put("totalPrice", fare.getTotalPrice());
                fareData.put("promotionalPrice", fare.getPromotionalPrice());
                fareData.put("promotionValidUntil", fare.getPromotionValidUntil());
                fareData.put("refundable", fare.getRefundable());
                fareData.put("changeable", fare.getChangeable());
                fareData.put("changeFee", fare.getChangeFee());
                fareData.put("status", fare.getStatus());
                fareData.put("effectivePrice", fare.getEffectivePrice());
                fareResponses.add(fareData);
            }
            flightData.put("fares", fareResponses);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("flight", flightData);
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener detalles del vuelo: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== ACTUALIZAR VUELO ====================
    
    public String updateFlight(String flightId, String requestBody) {
        try {
            Integer id = Integer.parseInt(flightId);
            Flight flight = flightDAO.getFlightById(id);
            
            if (flight == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Vuelo no encontrado");
                return gson.toJson(errorResponse);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> request = gson.fromJson(requestBody, Map.class);
            
            // Actualizar campos básicos si se proporcionan
            if (request.containsKey("status")) {
                flight.setStatus((String) request.get("status"));
            }
            if (request.containsKey("departureDate")) {
                flight.setDepartureDate(LocalDate.parse((String) request.get("departureDate")));
            }
            if (request.containsKey("departureTime")) {
                flight.setDepartureTime(LocalTime.parse((String) request.get("departureTime")));
            }
            if (request.containsKey("arrivalDate")) {
                flight.setArrivalDate(LocalDate.parse((String) request.get("arrivalDate")));
            }
            if (request.containsKey("arrivalTime")) {
                flight.setArrivalTime(LocalTime.parse((String) request.get("arrivalTime")));
            }
            if (request.containsKey("basePrice")) {
                flight.setBasePrice(BigDecimal.valueOf((Double) request.get("basePrice")));
            }
            
            // Guardar cambios
            Flight updatedFlight = flightDAO.updateFlight(flight, null, "Actualización manual");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vuelo actualizado exitosamente");
            response.put("flightId", updatedFlight.getIdFlight());
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al actualizar vuelo: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== CANCELAR VUELO ====================
    
    public String cancelFlight(String flightId, String requestBody) {
        try {
            Integer id = Integer.parseInt(flightId);
            Flight flight = flightDAO.getFlightById(id);
            
            if (flight == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Vuelo no encontrado");
                return gson.toJson(errorResponse);
            }
            
            // Cambiar estado a cancelado
            flight.setStatus("CANCELLED");
            flight.setUpdatedAt(LocalDateTime.now());
            
            // Guardar cambios
            Flight updatedFlight = flightDAO.updateFlight(flight, null, "Vuelo cancelado");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vuelo cancelado exitosamente");
            response.put("flightId", updatedFlight.getIdFlight());
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al cancelar vuelo: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== PUBLICAR VUELO ====================
    
    public String publishFlight(String flightId, String requestBody) {
        try {
            Integer id = Integer.parseInt(flightId);
            Flight flight = flightDAO.getFlightById(id);
            
            if (flight == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Vuelo no encontrado");
                return gson.toJson(errorResponse);
            }
            
            // Cambiar estado a publicado
            flight.setStatus("PUBLISHED");
            flight.setUpdatedAt(LocalDateTime.now());
            
            // Guardar cambios
            Flight updatedFlight = flightDAO.updateFlight(flight, null, "Vuelo publicado");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vuelo publicado exitosamente");
            response.put("flightId", updatedFlight.getIdFlight());
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al publicar vuelo: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== COMPLETAR VUELO ====================
    
    public String completeFlight(String flightId, String requestBody) {
        try {
            Integer id = Integer.parseInt(flightId);
            Flight flight = flightDAO.getFlightById(id);
            
            if (flight == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Vuelo no encontrado");
                return gson.toJson(errorResponse);
            }
            
            // Cambiar estado a completado
            flight.setStatus("COMPLETED");
            flight.setUpdatedAt(LocalDateTime.now());
            
            // Guardar cambios
            Flight updatedFlight = flightDAO.updateFlight(flight, null, "Vuelo completado");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vuelo completado exitosamente");
            response.put("flightId", updatedFlight.getIdFlight());
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al completar vuelo: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== ACTUALIZAR INVENTARIO ====================
    
    public String updateFlightInventory(String flightId, String requestBody) {
        try {
            Integer id = Integer.parseInt(flightId);
            @SuppressWarnings("unchecked")
            Map<String, Object> request = gson.fromJson(requestBody, Map.class);
            
            String category = (String) request.get("category");
            Integer totalSeats = ((Double) request.get("totalSeats")).intValue();
            Integer reservedSeats = request.containsKey("reservedSeats") ? 
                ((Double) request.get("reservedSeats")).intValue() : null;
            Integer soldSeats = request.containsKey("soldSeats") ? 
                ((Double) request.get("soldSeats")).intValue() : null;
            
            FlightInventory inventory = flightDAO.updateInventory(id, category, totalSeats, reservedSeats, soldSeats);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inventario actualizado exitosamente");
            response.put("inventory", inventory);
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al actualizar inventario: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== ACTUALIZAR PRECIOS ====================
    
    public String updateFlightFares(String flightId, String requestBody) {
        try {
            Integer id = Integer.parseInt(flightId);
            @SuppressWarnings("unchecked")
            Map<String, Object> request = gson.fromJson(requestBody, Map.class);
            
            String category = (String) request.get("category");
            BigDecimal basePrice = BigDecimal.valueOf((Double) request.get("basePrice"));
            BigDecimal taxes = request.containsKey("taxes") ? 
                BigDecimal.valueOf((Double) request.get("taxes")) : BigDecimal.ZERO;
            BigDecimal fees = request.containsKey("fees") ? 
                BigDecimal.valueOf((Double) request.get("fees")) : BigDecimal.ZERO;
            
            FlightFare fare = flightDAO.updateFare(id, category, basePrice, taxes, fees);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Precios actualizados exitosamente");
            response.put("fare", fare);
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al actualizar precios: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    // ==================== ESTADÍSTICAS ====================
    
    public String getFlightStatistics() {
        try {
            // Obtener estadísticas básicas
            List<Flight> allFlights = flightDAO.listFlights(null, null, null);
            
            long totalFlights = allFlights.size();
            long publishedFlights = allFlights.stream()
                .filter(f -> "PUBLISHED".equals(f.getStatus()))
                .count();
            long cancelledFlights = allFlights.stream()
                .filter(f -> "CANCELLED".equals(f.getStatus()))
                .count();
            long completedFlights = allFlights.stream()
                .filter(f -> "COMPLETED".equals(f.getStatus()))
                .count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalFlights", totalFlights);
            stats.put("publishedFlights", publishedFlights);
            stats.put("cancelledFlights", cancelledFlights);
            stats.put("completedFlights", completedFlights);
            stats.put("activeFlights", publishedFlights);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener estadísticas: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
}
*/
