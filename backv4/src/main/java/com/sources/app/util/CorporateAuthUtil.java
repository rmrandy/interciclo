package com.sources.app.util;

import com.sources.app.dao.UserDAO;
import com.sources.app.entities.User;
import com.sun.net.httpserver.HttpExchange;

/**
 * Utilidad para autenticación de usuarios empresariales mediante API Key.
 * Permite a las agencias autenticarse sin necesidad de login tradicional.
 * 
 * @author Equipo de Desarrollo Ensurance Pharmacy
 * @version 1.0
 * @since 2024
 */
public class CorporateAuthUtil {
    
    private static final UserDAO userDAO = new UserDAO();
    
    /**
     * Extrae el API Key del header X-API-Key o X-Api-Key de la solicitud HTTP.
     * 
     * @param exchange El objeto HttpExchange con la solicitud HTTP
     * @return El API Key si existe en los headers, null si no
     */
    public static String extractApiKey(HttpExchange exchange) {
        // Intentar varios formatos de header
        String apiKey = exchange.getRequestHeaders().getFirst("X-API-Key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = exchange.getRequestHeaders().getFirst("X-Api-Key");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = exchange.getRequestHeaders().getFirst("apikey");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = exchange.getRequestHeaders().getFirst("Authorization");
            if (apiKey != null && apiKey.startsWith("Bearer ")) {
                apiKey = apiKey.substring(7);
            }
        }
        return apiKey != null ? apiKey.trim() : null;
    }
    
    /**
     * Valida un API Key y retorna el usuario empresarial correspondiente.
     * 
     * @param apiKey El API Key a validar
     * @return El usuario empresarial si el API Key es válido, null si no
     */
    public static User validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }
        
        try {
            User user = userDAO.findByApiKey(apiKey.trim());
            if (user != null && user.isCorporateUser() && user.getEnabled() == 1) {
                System.out.println("✅ Usuario empresarial autenticado: " + user.getCompanyName() + " (ID: " + user.getIdUser() + ")");
                return user;
            }
        } catch (Exception e) {
            System.err.println("❌ Error validando API Key: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Autentica una solicitud HTTP usando API Key.
     * 
     * @param exchange El objeto HttpExchange con la solicitud HTTP
     * @return El usuario empresarial autenticado o null si falla la autenticación
     */
    public static User authenticateWithApiKey(HttpExchange exchange) {
        String apiKey = extractApiKey(exchange);
        if (apiKey == null) {
            System.out.println("⚠️  No se encontró API Key en los headers");
            return null;
        }
        
        return validateApiKey(apiKey);
    }
    
    /**
     * Verifica si una solicitud HTTP viene de un usuario empresarial autenticado.
     * 
     * @param exchange El objeto HttpExchange con la solicitud HTTP
     * @return true si está autenticado como empresarial, false si no
     */
    public static boolean isCorporateAuthenticated(HttpExchange exchange) {
        return authenticateWithApiKey(exchange) != null;
    }
}

