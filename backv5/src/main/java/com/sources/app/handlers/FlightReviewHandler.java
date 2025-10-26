package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sources.app.dao.FlightReviewDAO;
import com.sources.app.entities.FlightReview;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightReviewHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final FlightReviewDAO dao = new FlightReviewDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String response;
        try {
            if (path.matches("/api/airline/flights/\\d+/reviews") && "GET".equals(method)) {
                Integer flightId = Integer.parseInt(path.split("/")[4]);
                List<FlightReview> list = dao.listByFlight(flightId);
                response = gson.toJson(Map.of("success", true, "reviews", list));
            } else if (path.matches("/api/airline/flights/\\d+/reviews") && "POST".equals(method)) {
                Integer flightId = Integer.parseInt(path.split("/")[4]);
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                Integer userId = json.get("userId").getAsInt();
                Integer rating = json.get("rating").getAsInt();
                String comment = json.has("comment") && !json.get("comment").isJsonNull() ? json.get("comment").getAsString() : "";
                if (rating < 1 || rating > 5) {
                    response = gson.toJson(Map.of("success", false, "error", "La calificación debe ser 1-5"));
                } else {
                    // Compatibilidad: este handler no recibe parentReviewId; usar null
                    FlightReview r = dao.create(flightId, userId, rating, comment, null);
                    response = gson.toJson(Map.of("success", true, "review", r));
                }
            } else {
                response = gson.toJson(new HashMap<String, Object>() {{
                    put("success", false); put("error", "Endpoint no encontrado");
                }});
            }
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        } catch (Exception e) {
            e.printStackTrace();
            String err = gson.toJson(Map.of("success", false, "error", e.getMessage()));
            byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(500, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }
}


