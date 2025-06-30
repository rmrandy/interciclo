package com.sources.app.handlers;

import com.sources.app.dao.SiteContentDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.SiteContent;
import com.sources.app.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SiteContentHandler implements HttpHandler {
    
    private final SiteContentDAO siteContentDAO;
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;
    
    public SiteContentHandler() {
        this.siteContentDAO = new SiteContentDAO();
        this.userDAO = new UserDAO();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, User-ID");
        
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        
        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange, path);
                    break;
                case "PUT":
                    handlePut(exchange, path);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error interno del servidor: " + e.getMessage());
        }
    }
    
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.endsWith("/api2/site-content")) {
            // Obtener todo el contenido
            List<SiteContent> content = siteContentDAO.getAllContent();
            if (content != null) {
                String json = objectMapper.writeValueAsString(content);
                sendJsonResponse(exchange, 200, json);
            } else {
                sendErrorResponse(exchange, 500, "Error al obtener el contenido del sitio");
            }
        } else if (path.startsWith("/api2/site-content/")) {
            // Obtener contenido específico por clave
            String contentKey = path.substring("/api2/site-content/".length());
            String contentValue = siteContentDAO.getContentByKey(contentKey)
                .map(SiteContent::getContentValue)
                .orElse("");
            
            Map<String, String> response = Map.of("key", contentKey, "value", contentValue);
            String json = objectMapper.writeValueAsString(response);
            sendJsonResponse(exchange, 200, json);
        } else {
            sendErrorResponse(exchange, 404, "Ruta no encontrada");
        }
    }
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.endsWith("/api2/site-content/initialize")) {
            // Inicializar contenido por defecto (solo admin)
            if (!isAdminUser(exchange)) {
                sendErrorResponse(exchange, 403, "Acceso denegado. Solo administradores pueden inicializar el contenido del sitio.");
                return;
            }
            
            siteContentDAO.initializeDefaultContent();
            sendJsonResponse(exchange, 200, "{\"message\": \"Contenido inicializado exitosamente\"}");
        } else {
            sendErrorResponse(exchange, 404, "Ruta no encontrada");
        }
    }
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/api2/site-content/")) {
            // Actualizar contenido (solo admin)
            if (!isAdminUser(exchange)) {
                sendErrorResponse(exchange, 403, "Acceso denegado. Solo administradores pueden modificar el contenido del sitio.");
                return;
            }
            
            String contentKey = path.substring("/api2/site-content/".length());
            
            // Leer el cuerpo de la petición
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> requestData = objectMapper.readValue(requestBody, Map.class);
            String newValue = requestData.get("value");
            
            if (newValue == null) {
                sendErrorResponse(exchange, 400, "Se requiere 'value' en el cuerpo de la petición");
                return;
            }
            
            boolean success = siteContentDAO.updateContentValue(contentKey, newValue);
            if (success) {
                sendJsonResponse(exchange, 200, "{\"message\": \"Contenido actualizado exitosamente\"}");
            } else {
                sendErrorResponse(exchange, 500, "Error al actualizar el contenido");
            }
        } else {
            sendErrorResponse(exchange, 404, "Ruta no encontrada");
        }
    }
    
    private boolean isAdminUser(HttpExchange exchange) {
        try {
            String userIdStr = exchange.getRequestHeaders().getFirst("User-ID");
            if (userIdStr == null) {
                return false;
            }
            
            Long userId = Long.parseLong(userIdStr);
            User user = userDAO.getById(userId);
            
            return user != null && "administrador".equals(user.getRole());
        } catch (Exception e) {
            return false;
        }
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        String errorJson = "{\"error\": \"" + errorMessage + "\"}";
        sendJsonResponse(exchange, statusCode, errorJson);
    }
} 