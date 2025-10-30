package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sources.app.dao.TicketDAO;
import com.sources.app.dao.FlightDAO;
import com.sources.app.entities.Ticket;
import com.sources.app.entities.User;
import com.sources.app.util.CorporateAuthUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler para crear tickets de vuelos redondos (round-trip).
 * Crea 2 tickets: uno de ida y uno de vuelta.
 * 
 * @author Equipo de Desarrollo Ensurance Pharmacy
 * @version 1.0
 */
public class RoundTripTicketHandler {
    
    private final TicketDAO ticketDAO;
    private final FlightDAO flightDAO;
    private final Gson gson;
    
    public RoundTripTicketHandler() {
        this.ticketDAO = new TicketDAO();
        this.flightDAO = new FlightDAO();
        this.gson = new Gson();
    }
    
    /**
     * Crea 2 tickets para un vuelo redondo (ida y vuelta).
     * Usa la misma información de pago para ambos tickets.
     * 
     * @param exchange HttpExchange con la solicitud
     * @return JSON con los 2 tickets creados
     */
    public String handleCreateRoundTripTickets(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("🔄 Creando tickets round-trip - Body: " + requestBody);
        
        try {
            // Autenticación empresarial si aplica
            User corporateUser = CorporateAuthUtil.authenticateWithApiKey(exchange);
            
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            // Validar campos requeridos para round-trip
            if (!jsonRequest.has("outboundFlightId") || !jsonRequest.has("returnFlightId")) {
                return gson.toJson(Map.of(
                    "success", false,
                    "error", "Se requieren outboundFlightId (ida) y returnFlightId (vuelta)"
                ));
            }
            
            Integer outboundFlightId = jsonRequest.get("outboundFlightId").getAsInt();
            Integer returnFlightId = jsonRequest.get("returnFlightId").getAsInt();
            
            // Determinar userId según autenticación
            Integer userId;
            Integer purchasedByUserId = null;
            
            if (corporateUser != null) {
                userId = corporateUser.getIdUser().intValue();
                purchasedByUserId = corporateUser.getIdUser().intValue();
                System.out.println("🏢 Round-trip empresarial: " + corporateUser.getCompanyName());
            } else {
                if (!jsonRequest.has("userId")) {
                    return gson.toJson(Map.of("success", false, "error", "userId requerido"));
                }
                userId = jsonRequest.get("userId").getAsInt();
                System.out.println("👤 Round-trip individual: Usuario " + userId);
            }
            
            // Preparar datos comunes
            String seatCategory = jsonRequest.get("seatCategory").getAsString();
            BigDecimal farePerFlight = new BigDecimal(jsonRequest.get("fare").getAsString());
            String passengerFirstName = jsonRequest.get("passengerFirstName").getAsString();
            String passengerLastName = jsonRequest.get("passengerLastName").getAsString();
            String passengerEmail = jsonRequest.get("passengerEmail").getAsString();
            String passengerPhone = jsonRequest.get("passengerPhone").getAsString();
            String passengerDocumentType = jsonRequest.get("passengerDocumentType").getAsString();
            String passengerDocumentNumber = jsonRequest.get("passengerDocumentNumber").getAsString();
            String paymentMethod = jsonRequest.get("paymentMethod").getAsString();
            
            List<Map<String, Object>> createdTickets = new ArrayList<>();
            
            // TICKET 1: Vuelo de IDA
            Map<String, Object> ticketData1 = new HashMap<>();
            ticketData1.put("flightId", outboundFlightId);
            ticketData1.put("userId", userId);
            ticketData1.put("purchasedByUserId", purchasedByUserId);
            ticketData1.put("seatCategory", seatCategory);
            ticketData1.put("fare", farePerFlight);
            ticketData1.put("passengerFirstName", passengerFirstName);
            ticketData1.put("passengerLastName", passengerLastName);
            ticketData1.put("passengerEmail", passengerEmail);
            ticketData1.put("passengerPhone", passengerPhone);
            ticketData1.put("passengerDocumentType", passengerDocumentType);
            ticketData1.put("passengerDocumentNumber", passengerDocumentNumber);
            ticketData1.put("paymentMethod", paymentMethod);
            ticketData1.put("totalAmount", farePerFlight);
            ticketData1.put("taxes", BigDecimal.ZERO);
            ticketData1.put("fees", BigDecimal.ZERO);
            ticketData1.put("discountAmount", BigDecimal.ZERO);
            ticketData1.put("discountCode", "");
            ticketData1.put("specialRequests", "Vuelo de IDA - Round Trip");
            
            Ticket ticket1 = ticketDAO.createTicketWithValidation(ticketData1);
            
            // TICKET 2: Vuelo de VUELTA
            Map<String, Object> ticketData2 = new HashMap<>();
            ticketData2.put("flightId", returnFlightId);
            ticketData2.put("userId", userId);
            ticketData2.put("purchasedByUserId", purchasedByUserId);
            ticketData2.put("seatCategory", seatCategory);
            ticketData2.put("fare", farePerFlight);
            ticketData2.put("passengerFirstName", passengerFirstName);
            ticketData2.put("passengerLastName", passengerLastName);
            ticketData2.put("passengerEmail", passengerEmail);
            ticketData2.put("passengerPhone", passengerPhone);
            ticketData2.put("passengerDocumentType", passengerDocumentType);
            ticketData2.put("passengerDocumentNumber", passengerDocumentNumber);
            ticketData2.put("paymentMethod", paymentMethod);
            ticketData2.put("totalAmount", farePerFlight);
            ticketData2.put("taxes", BigDecimal.ZERO);
            ticketData2.put("fees", BigDecimal.ZERO);
            ticketData2.put("discountAmount", BigDecimal.ZERO);
            ticketData2.put("discountCode", "");
            ticketData2.put("specialRequests", "Vuelo de VUELTA - Round Trip");
            
            Ticket ticket2 = ticketDAO.createTicketWithValidation(ticketData2);
            
            if (ticket1 != null && ticket2 != null) {
                // Ticket de ida
                Map<String, Object> t1Map = new HashMap<>();
                t1Map.put("ticketId", ticket1.getIdTicket());
                t1Map.put("type", "outbound");
                t1Map.put("flightNumber", ticket1.getFlight().getFlightNumber());
                createdTickets.add(t1Map);
                
                // Ticket de vuelta
                Map<String, Object> t2Map = new HashMap<>();
                t2Map.put("ticketId", ticket2.getIdTicket());
                t2Map.put("type", "return");
                t2Map.put("flightNumber", ticket2.getFlight().getFlightNumber());
                createdTickets.add(t2Map);
                
                System.out.println("✅ Round-trip creado: Tickets #" + ticket1.getIdTicket() + " y #" + ticket2.getIdTicket());
                
                return gson.toJson(Map.of(
                    "success", true,
                    "message", "Vuelo redondo confirmado",
                    "tickets", createdTickets,
                    "totalAmount", farePerFlight.multiply(new BigDecimal("2"))
                ));
            } else {
                return gson.toJson(Map.of(
                    "success", false,
                    "error", "Error creando uno de los tickets"
                ));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of(
                "success", false,
                "error", "Error procesando round-trip: " + e.getMessage()
            ));
        }
    }
}

