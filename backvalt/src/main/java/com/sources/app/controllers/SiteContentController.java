package com.sources.app.controllers;

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

/**
 * Controlador para manejar el contenido dinámico del sitio web
 * Permite a los administradores editar headers, footers y otros textos del sitio
 */
public class SiteContentController implements HttpHandler {
    
    private final SiteContentDAO siteContentDAO;
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;
    
    public SiteContentController() {
        this.siteContentDAO = new SiteContentDAO();
        this.userDAO = new UserDAO();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Configurar CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, User-ID");
        
        // Manejar preflight requests
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
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
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Error interno del servidor: " + e.getMessage());
        }
    }
    
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.endsWith("/api2/site-content-v2")) {
            // Obtener todo el contenido (público)
            List<SiteContent> content = siteContentDAO.getAllContent();
            if (content != null) {
                // Mapear a objetos simples
                List<Map<String, Object>> result = new java.util.ArrayList<>();
                for (SiteContent sc : content) {
                    result.add(Map.of(
                        "key", sc.getContentKey(),
                        "value", sc.getContentValue(),
                        "description", sc.getDescription()
                    ));
                }
                String json = objectMapper.writeValueAsString(result);
                sendJsonResponse(exchange, 200, json);
            } else {
                sendErrorResponse(exchange, 500, "Error al obtener el contenido del sitio");
            }
        } else if (path.startsWith("/api2/site-content/")) {
            // Obtener contenido específico por clave
            String contentKey = path.substring("/api2/site-content/".length());
            Optional<SiteContent> content = siteContentDAO.getContentByKey(contentKey);
            
            if (content.isPresent()) {
                Map<String, Object> response = Map.of(
                    "key", content.get().getContentKey(),
                    "value", content.get().getContentValue(),
                    "description", content.get().getDescription()
                );
                String json = objectMapper.writeValueAsString(response);
                sendJsonResponse(exchange, 200, json);
            } else {
                sendErrorResponse(exchange, 404, "Contenido no encontrado");
            }
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
            Map<String, String> response = Map.of("message", "Contenido inicializado exitosamente");
            String json = objectMapper.writeValueAsString(response);
            sendJsonResponse(exchange, 200, json);
        } else if (path.endsWith("/api2/site-content")) {
            // Crear nuevo contenido (solo admin)
            if (!isAdminUser(exchange)) {
                sendErrorResponse(exchange, 403, "Acceso denegado. Solo administradores pueden crear contenido del sitio.");
                return;
            }
            
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> requestData = objectMapper.readValue(requestBody, Map.class);
            
            String contentKey = requestData.get("key");
            String contentValue = requestData.get("value");
            String description = requestData.get("description");
            
            if (contentKey == null || contentValue == null) {
                sendErrorResponse(exchange, 400, "Se requieren 'key' y 'value' en el cuerpo de la petición");
                return;
            }
            
            SiteContent newContent = new SiteContent(contentKey, contentValue, description);
            boolean success = siteContentDAO.saveOrUpdateContent(newContent);
            
            if (success) {
                Map<String, String> response = Map.of("message", "Contenido creado exitosamente");
                String json = objectMapper.writeValueAsString(response);
                sendJsonResponse(exchange, 201, json);
            } else {
                sendErrorResponse(exchange, 500, "Error al crear el contenido");
            }
        } else {
            sendErrorResponse(exchange, 404, "Ruta no encontrada");
        }
    }
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/api2/site-content-v2/")) {
            // Actualizar contenido (solo admin)
            if (!isAdminUser(exchange)) {
                sendErrorResponse(exchange, 403, "Acceso denegado. Solo administradores pueden modificar el contenido del sitio.");
                return;
            }
            String contentKey = path.substring("/api2/site-content-v2/".length());
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
                sendErrorResponse(exchange, 404, "No existe un registro con la clave: " + contentKey);
            }
        } else {
            sendErrorResponse(exchange, 404, "Ruta no encontrada");
        }
    }
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/api2/site-content/")) {
            // Eliminar contenido (solo admin)
            if (!isAdminUser(exchange)) {
                sendErrorResponse(exchange, 403, "Acceso denegado. Solo administradores pueden eliminar contenido del sitio.");
                return;
            }
            
            String contentKey = path.substring("/api2/site-content/".length());
            
            // Por ahora, no implementamos eliminación física, solo marcamos como vacío
            boolean success = siteContentDAO.updateContentValue(contentKey, "");
            if (success) {
                Map<String, String> response = Map.of("message", "Contenido eliminado exitosamente");
                String json = objectMapper.writeValueAsString(response);
                sendJsonResponse(exchange, 200, json);
            } else {
                sendErrorResponse(exchange, 500, "Error al eliminar el contenido");
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
            
            return user != null && ("administrador".equals(user.getRole()) || "admin".equals(user.getRole()));
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
        Map<String, String> errorResponse = Map.of("error", errorMessage);
        String errorJson = objectMapper.writeValueAsString(errorResponse);
        sendJsonResponse(exchange, statusCode, errorJson);
    }
} 