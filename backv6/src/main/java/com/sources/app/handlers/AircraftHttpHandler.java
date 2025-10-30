package com.sources.app.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * HttpHandler para exponer endpoints de aeronaves bajo /api/airline/aircrafts
 */
public class AircraftHttpHandler implements HttpHandler {

    private final AircraftHandler aircraftHandler;
    private final Gson gson = new Gson();

    public AircraftHttpHandler() {
        this.aircraftHandler = new AircraftHandler();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS básico
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String response;
        try {
            if (path.equals("/api/airline/aircrafts") && "GET".equalsIgnoreCase(method)) {
                response = aircraftHandler.listAircrafts();
                sendJson(exchange, 200, response);
                return;
            }

            if (path.equals("/api/airline/aircrafts") && "POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                response = aircraftHandler.createAircraft(body);
                sendJson(exchange, 200, response);
                return;
            }

            if (path.matches("/api/airline/aircrafts/\\d+") && "PUT".equalsIgnoreCase(method)) {
                String[] parts = path.split("/");
                String id = parts[parts.length - 1];
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                response = aircraftHandler.updateAircraft(id, body);
                sendJson(exchange, 200, response);
                return;
            }

            if (path.matches("/api/airline/aircrafts/\\d+") && "DELETE".equalsIgnoreCase(method)) {
                String[] parts = path.split("/");
                String id = parts[parts.length - 1];
                response = aircraftHandler.deleteAircraft(id);
                sendJson(exchange, 200, response);
                return;
            }

            if (path.matches("/api/airline/aircrafts/\\d+/seat-config") && "GET".equalsIgnoreCase(method)) {
                String[] parts = path.split("/");
                String id = parts[parts.length - 2];
                response = aircraftHandler.getSeatConfig(id);
                sendJson(exchange, 200, response);
                return;
            }

            if (path.matches("/api/airline/aircrafts/\\d+/seat-config") && "PUT".equalsIgnoreCase(method)) {
                String[] parts = path.split("/");
                String id = parts[parts.length - 2];
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                response = aircraftHandler.updateSeatConfig(id, body);
                sendJson(exchange, 200, response);
                return;
            }

            // No encontrado
            sendJson(exchange, 404, gson.toJson(java.util.Map.of(
                "success", false,
                "error", "Endpoint no encontrado: " + method + " " + path
            )));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, gson.toJson(java.util.Map.of(
                "success", false,
                "error", "Error interno del servidor: " + e.getMessage()
            )));
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}


























