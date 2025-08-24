package com.sources.app.handlers;

import com.sources.app.dao.UserDAO;
import com.sources.app.entities.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class UserProfileHandler implements HttpHandler {
    private final UserDAO userDAO;
    private final Gson gson = new Gson();
    
    public UserProfileHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        
        try {
            if ("GET".equals(method)) {
                handleGetProfile(exchange);
            } else if ("PUT".equals(method)) {
                handleUpdateProfile(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor: " + e.getMessage());
        }
    }
    
    private void handleGetProfile(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.contains("userId=")) {
            sendErrorResponse(exchange, 400, "userId es requerido");
            return;
        }
        
        String userIdStr = query.split("userId=")[1];
        try {
            int userId = Integer.parseInt(userIdStr);
            User user = userDAO.findById((long) userId);
            
            if (user == null) {
                sendErrorResponse(exchange, 404, "Usuario no encontrado");
                return;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", createUserJson(user));
            
            sendSuccessResponse(exchange, response);
            
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "userId debe ser un número válido");
        }
    }
    
    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        // Leer el body del request
        String body = new String(exchange.getRequestBody().readAllBytes());
        JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
        
        try {
            int userId = jsonRequest.get("userId").getAsInt();
            User user = userDAO.findById((long) userId);
            
            if (user == null) {
                sendErrorResponse(exchange, 404, "Usuario no encontrado");
                return;
            }
            
            // Actualizar campos del usuario
            if (jsonRequest.has("firstName")) {
                user.setFirstName(jsonRequest.get("firstName").getAsString());
            }
            if (jsonRequest.has("lastName")) {
                user.setLastName(jsonRequest.get("lastName").getAsString());
            }
            if (jsonRequest.has("phone")) {
                user.setPhone(jsonRequest.get("phone").getAsString());
            }
            
            // Guardar cambios
            userDAO.update(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Perfil actualizado correctamente");
            response.put("user", createUserJson(user));
            
            sendSuccessResponse(exchange, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error actualizando perfil: " + e.getMessage());
        }
    }
    
    private Map<String, Object> createUserJson(User user) {
        Map<String, Object> userJson = new HashMap<>();
        userJson.put("idUser", user.getIdUser());
        userJson.put("firstName", user.getFirstName());
        userJson.put("lastName", user.getLastName());
        userJson.put("email", user.getEmail());
        userJson.put("phone", user.getPhone());
        userJson.put("cui", user.getCui());
        userJson.put("birthDate", user.getBirthDate());
        userJson.put("createdAt", user.getCreatedAt());
        return userJson;
    }
    
    private void sendSuccessResponse(HttpExchange exchange, Map<String, Object> response) throws IOException {
        String jsonResponse = gson.toJson(response);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        
        String jsonResponse = gson.toJson(errorResponse);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
}
