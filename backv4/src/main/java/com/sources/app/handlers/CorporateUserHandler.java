package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler HTTP para la gestión de usuarios empresariales (Corporate Users).
 * Proporciona endpoints para CRUD completo de usuarios empresariales con API Keys.
 * 
 * <p>Endpoints manejados:</p>
 * <ul>
 *   <li>GET /api/corporate-users - Lista todos los usuarios empresariales</li>
 *   <li>GET /api/corporate-users/{id} - Obtiene un usuario empresarial específico</li>
 *   <li>POST /api/corporate-users - Crea un nuevo usuario empresarial</li>
 *   <li>PUT /api/corporate-users/{id} - Actualiza un usuario empresarial</li>
 *   <li>PUT /api/corporate-users/{id}/regenerate-key - Regenera el API Key</li>
 *   <li>PUT /api/corporate-users/{id}/toggle-status - Activa/Desactiva usuario</li>
 * </ul>
 */
public class CorporateUserHandler implements HttpHandler {
    
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;
    private final Gson gson;
    private static final String ENDPOINT = "/api/corporate-users";
    private static final Pattern ID_PATTERN = Pattern.compile(ENDPOINT + "/([0-9]+)$");
    private static final Pattern REGENERATE_KEY_PATTERN = Pattern.compile(ENDPOINT + "/([0-9]+)/regenerate-key");
    private static final Pattern TOGGLE_STATUS_PATTERN = Pattern.compile(ENDPOINT + "/([0-9]+)/toggle-status");

    public CorporateUserHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.objectMapper = new ObjectMapper();
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS headers (ya manejado por CorsFilter, pero por compatibilidad)
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Handle OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        System.out.println("🔍 CorporateUserHandler - Path: " + path + ", Method: " + method);

        try {
            // Verificar si es una operación especial
            Matcher regenerateMatcher = REGENERATE_KEY_PATTERN.matcher(path);
            Matcher toggleMatcher = TOGGLE_STATUS_PATTERN.matcher(path);
            Matcher idMatcher = ID_PATTERN.matcher(path);

            if (regenerateMatcher.matches() && "PUT".equals(method)) {
                // Regenerar API Key
                Long userId = Long.parseLong(regenerateMatcher.group(1));
                handleRegenerateApiKey(exchange, userId);
            } else if (toggleMatcher.matches() && "PUT".equals(method)) {
                // Toggle status
                Long userId = Long.parseLong(toggleMatcher.group(1));
                handleToggleStatus(exchange, userId);
            } else if (idMatcher.matches()) {
                // Operaciones con ID específico
                Long userId = Long.parseLong(idMatcher.group(1));
                if ("GET".equals(method)) {
                    handleGetById(exchange, userId);
                } else if ("PUT".equals(method)) {
                    handleUpdate(exchange, userId);
                } else if ("DELETE".equals(method)) {
                    handleDelete(exchange, userId);
                } else {
                    sendError(exchange, 405, "Método no permitido");
                }
            } else if (path.equals(ENDPOINT)) {
                // Operaciones sin ID
                if ("GET".equals(method)) {
                    handleGetAll(exchange);
                } else if ("POST".equals(method)) {
                    handleCreate(exchange);
                } else {
                    sendError(exchange, 405, "Método no permitido");
                }
            } else {
                sendError(exchange, 404, "Ruta no encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * GET /api/corporate-users - Lista todos los usuarios empresariales
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<User> users = userDAO.findCorporateUsers();
        sendJsonResponse(exchange, 200, users);
    }

    /**
     * GET /api/corporate-users/{id} - Obtiene un usuario empresarial específico
     */
    private void handleGetById(HttpExchange exchange, Long userId) throws IOException {
        User user = userDAO.findById(userId);
        
        if (user == null || user.getIsCorporate() != 1) {
            sendError(exchange, 404, "Usuario empresarial no encontrado");
            return;
        }

        sendJsonResponse(exchange, 200, user);
    }

    /**
     * POST /api/corporate-users - Crea un nuevo usuario empresarial
     */
    private void handleCreate(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("📝 Creando usuario empresarial: " + requestBody);

        try {
            JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

            // Validar campos requeridos
            if (!json.has("companyName") || !json.has("name") || !json.has("email") || 
                !json.has("phone") || !json.has("cui") || !json.has("address") || !json.has("password")) {
                sendError(exchange, 400, "Faltan campos requeridos");
                return;
            }

            String companyName = json.get("companyName").getAsString();
            String name = json.get("name").getAsString();
            String email = json.get("email").getAsString();
            String phone = json.get("phone").getAsString();
            
            // Convertir CUI de forma segura (puede venir como string o número)
            Long cui;
            try {
                if (json.get("cui").isJsonPrimitive() && json.get("cui").getAsJsonPrimitive().isString()) {
                    cui = Long.parseLong(json.get("cui").getAsString());
                } else {
                    cui = json.get("cui").getAsLong();
                }
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "CUI/NIT inválido: debe ser un número");
                return;
            }
            
            String address = json.get("address").getAsString();
            String password = json.get("password").getAsString();
            
            // Fecha de nacimiento opcional
            Date birthDate = null;
            if (json.has("birthDate") && !json.get("birthDate").isJsonNull()) {
                try {
                    String birthDateStr = json.get("birthDate").getAsString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    birthDate = sdf.parse(birthDateStr);
                } catch (Exception e) {
                    System.err.println("⚠️ Error parseando fecha de nacimiento: " + e.getMessage());
                }
            }

            // Generar API Key automáticamente si no se proporciona
            String apiKey;
            if (json.has("apiKey") && !json.get("apiKey").isJsonNull() && !json.get("apiKey").getAsString().isEmpty()) {
                apiKey = json.get("apiKey").getAsString();
            } else {
                apiKey = generateApiKey();
            }

            // Verificar que el API Key no exista
            if (userDAO.existsApiKey(apiKey)) {
                sendError(exchange, 400, "El API Key ya está en uso");
                return;
            }

            // Verificar que el email no exista
            if (userDAO.existsUserWithEmail(email)) {
                sendError(exchange, 400, "El email ya está registrado");
                return;
            }

            // Verificar que el CUI no exista
            if (userDAO.existsUserWithCUI(cui)) {
                sendError(exchange, 400, "El CUI/NIT ya está registrado");
                return;
            }

            // Crear usuario empresarial
            User user = userDAO.createCorporateUser(
                companyName, name, email, phone, cui, address, password, apiKey, birthDate
            );

            if (user != null) {
                sendJsonResponse(exchange, 201, Map.of(
                    "success", true,
                    "message", "Usuario empresarial creado exitosamente",
                    "user", user,
                    "apiKey", apiKey  // Retornar el API Key para que lo guarden
                ));
            } else {
                sendError(exchange, 500, "Error al crear el usuario empresarial");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 400, "Datos inválidos: " + e.getMessage());
        }
    }

    /**
     * PUT /api/corporate-users/{id} - Actualiza un usuario empresarial
     */
    private void handleUpdate(HttpExchange exchange, Long userId) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("✏️ Actualizando usuario empresarial: " + userId);

        try {
            JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

            String companyName = json.get("companyName").getAsString();
            String name = json.get("name").getAsString();
            String email = json.get("email").getAsString();
            String phone = json.get("phone").getAsString();
            
            // Convertir CUI de forma segura
            Long cui;
            try {
                if (json.get("cui").isJsonPrimitive() && json.get("cui").getAsJsonPrimitive().isString()) {
                    cui = Long.parseLong(json.get("cui").getAsString());
                } else {
                    cui = json.get("cui").getAsLong();
                }
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "CUI/NIT inválido: debe ser un número");
                return;
            }
            
            String address = json.get("address").getAsString();
            Integer enabled = json.has("enabled") ? json.get("enabled").getAsInt() : 1;

            // Password opcional
            String password = null;
            if (json.has("password") && !json.get("password").isJsonNull() && !json.get("password").getAsString().isEmpty()) {
                password = json.get("password").getAsString();
            }

            // API Key opcional (normalmente no se cambia aquí, sino con regenerate-key)
            String apiKey = null;
            if (json.has("apiKey") && !json.get("apiKey").isJsonNull() && !json.get("apiKey").getAsString().isEmpty()) {
                apiKey = json.get("apiKey").getAsString();
            }

            // Fecha de nacimiento opcional
            Date birthDate = null;
            if (json.has("birthDate") && !json.get("birthDate").isJsonNull()) {
                try {
                    String birthDateStr = json.get("birthDate").getAsString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    birthDate = sdf.parse(birthDateStr);
                } catch (Exception e) {
                    System.err.println("⚠️ Error parseando fecha de nacimiento: " + e.getMessage());
                }
            }

            User user = userDAO.updateCorporateUser(
                userId, companyName, name, email, phone, cui, address, enabled, password, apiKey, birthDate
            );

            if (user != null) {
                sendJsonResponse(exchange, 200, Map.of(
                    "success", true,
                    "message", "Usuario empresarial actualizado exitosamente",
                    "user", user
                ));
            } else {
                sendError(exchange, 404, "Usuario empresarial no encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 400, "Datos inválidos: " + e.getMessage());
        }
    }

    /**
     * PUT /api/corporate-users/{id}/regenerate-key - Regenera el API Key
     */
    private void handleRegenerateApiKey(HttpExchange exchange, Long userId) throws IOException {
        System.out.println("🔑 Regenerando API Key para usuario: " + userId);

        String newApiKey = generateApiKey();
        boolean success = userDAO.regenerateApiKey(userId, newApiKey);

        if (success) {
            User user = userDAO.findById(userId);
            sendJsonResponse(exchange, 200, Map.of(
                "success", true,
                "message", "API Key regenerado exitosamente",
                "apiKey", newApiKey,
                "user", user
            ));
        } else {
            sendError(exchange, 404, "Usuario empresarial no encontrado");
        }
    }

    /**
     * PUT /api/corporate-users/{id}/toggle-status - Activa/Desactiva usuario
     */
    private void handleToggleStatus(HttpExchange exchange, Long userId) throws IOException {
        System.out.println("🔄 Alternando estado de usuario: " + userId);

        User user = userDAO.toggleCorporateUserStatus(userId);

        if (user != null) {
            String status = user.getEnabled() == 1 ? "activado" : "desactivado";
            sendJsonResponse(exchange, 200, Map.of(
                "success", true,
                "message", "Usuario " + status + " exitosamente",
                "user", user
            ));
        } else {
            sendError(exchange, 404, "Usuario empresarial no encontrado");
        }
    }

    /**
     * DELETE /api/corporate-users/{id} - Elimina (desactiva) un usuario empresarial
     */
    private void handleDelete(HttpExchange exchange, Long userId) throws IOException {
        // Por seguridad, no eliminamos usuarios, solo los desactivamos
        User user = userDAO.toggleCorporateUserStatus(userId);

        if (user != null && user.getEnabled() == 0) {
            sendJsonResponse(exchange, 200, Map.of(
                "success", true,
                "message", "Usuario empresarial desactivado exitosamente"
            ));
        } else {
            sendError(exchange, 404, "Usuario empresarial no encontrado");
        }
    }

    /**
     * Genera un API Key único de 32 caracteres alfanuméricos.
     */
    private String generateApiKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder apiKey = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 32; i++) {
            apiKey.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return apiKey.toString();
    }

    /**
     * Envía una respuesta JSON con un código de estado HTTP.
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Envía un mensaje de error en formato JSON.
     */
    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = Map.of(
            "success", false,
            "error", message
        );
        sendJsonResponse(exchange, statusCode, errorResponse);
    }
}

