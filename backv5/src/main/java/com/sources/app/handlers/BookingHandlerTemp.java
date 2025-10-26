package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handler temporal para bookings mientras se resuelven los errores de compatibilidad
 */
public class BookingHandlerTemp {
    
    private final Gson gson;
    
    public BookingHandlerTemp() {
        this.gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();
    }
    
    public String getUserBookings(String userId) {
        return "[]"; // Devolver array vacío temporalmente
    }
    
    public String getBookingById(String bookingId) {
        return gson.toJson(java.util.Map.of("error", "Método en desarrollo"));
    }
    
    public String createBooking(String requestBody) {
        return gson.toJson(java.util.Map.of("error", "Método en desarrollo"));
    }
    
    public String cancelBooking(String bookingId, String userId) {
        return gson.toJson(java.util.Map.of("error", "Método en desarrollo"));
    }
}
