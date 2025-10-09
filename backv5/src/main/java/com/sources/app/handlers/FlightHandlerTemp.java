package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handler temporal para vuelos mientras se resuelven los errores de compatibilidad
 */
public class FlightHandlerTemp {
    
    private final Gson gson;
    
    public FlightHandlerTemp() {
        this.gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();
    }
    
    public String listFlights() {
        return "[]"; // Devolver array vacío temporalmente
    }
    
    public String getFlightById(String flightId) {
        return gson.toJson(java.util.Map.of("error", "Método en desarrollo"));
    }
    
    public String searchFlights(String origin, String destination, String date) {
        return "[]"; // Devolver array vacío temporalmente
    }
}
