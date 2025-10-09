package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sources.app.dao.UserDAO;
import com.sources.app.dao.CityDAO;
import com.sources.app.entities.City;
import com.sources.app.entities.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpHandler para exponer los endpoints de aerolínea bajo el servidor principal basado en HttpServer.
 * Actualmente implementa:
 * - POST /api/airline/register
 * - POST /api/airline/login
 * - GET  /api/airline/roles
 * - POST /api/airline/initialize-roles
 */
public class AirlineHttpHandler implements HttpHandler {

    private final AirlineUserHandler airlineUserHandler;
    private final UserDAO userDAO;
    private final Gson gson = new Gson();
    private final CityDAO cityDAO = new CityDAO();

    public AirlineHttpHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.airlineUserHandler = new AirlineUserHandler();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/airline/register") && "POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String json = airlineUserHandler.registerVisitor(body);
                sendJson(exchange, 200, json);
                return;
            }

            if (path.equals("/api/airline/login") && "POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
                String email = jsonBody.get("email").getAsString();
                String password = jsonBody.get("password").getAsString();

                User user = userDAO.login(email, password);
                Map<String, Object> response = new HashMap<>();
                if (user != null) {
                    response.put("success", true);
                    response.put("message", "Login exitoso");
                    response.put("user", Map.of(
                            "id", user.getIdUser(),
                            "email", user.getEmail(),
                            "role", user.getRole(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName()
                    ));
                    response.put("token", "token-" + user.getIdUser());
                    sendJson(exchange, 200, gson.toJson(response));
                } else {
                    response.put("success", false);
                    response.put("error", "Credenciales inválidas");
                    sendJson(exchange, 401, gson.toJson(response));
                }
                return;
            }

            if (path.equals("/api/airline/roles") && "GET".equalsIgnoreCase(method)) {
                String json = airlineUserHandler.getAllRoles();
                sendJson(exchange, 200, json);
                return;
            }

            if (path.equals("/api/airline/initialize-roles") && "POST".equalsIgnoreCase(method)) {
                String json = airlineUserHandler.initializeDefaultRoles();
                sendJson(exchange, 200, json);
                return;
            }

            // ===== Ciudades =====
            if (path.equals("/api/airline/cities") && "GET".equalsIgnoreCase(method)) {
                sendJson(exchange, 200, gson.toJson(cityDAO.findAll()));
                return;
            }

            if (path.equals("/api/airline/cities") && "POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String name = json.get("name").getAsString();
                String country = json.get("country").getAsString();

                if (name == null || name.isBlank() || country == null || country.isBlank()) {
                    sendJson(exchange, 400, gson.toJson(Map.of(
                            "success", false,
                            "error", "Nombre y país son obligatorios"
                    )));
                    return;
                }

                if (cityDAO.existsByNameAndCountry(name, country)) {
                    sendJson(exchange, 409, gson.toJson(Map.of(
                            "success", false,
                            "error", "La ciudad ya existe"
                    )));
                    return;
                }

                City city = new City();
                city.setName(name);
                city.setCountry(country);
                City saved = cityDAO.save(city);
                sendJson(exchange, 201, gson.toJson(saved));
                return;
            }

            // No encontrado
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Endpoint no encontrado: " + method + " " + path);
            sendJson(exchange, 404, gson.toJson(error));

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error interno del servidor: " + e.getMessage());
            sendJson(exchange, 500, gson.toJson(error));
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}


