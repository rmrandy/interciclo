package com.sources.app.util;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 * Filtro CORS (Cross-Origin Resource Sharing) para permitir solicitudes desde cualquier origen.
 * Este filtro se aplica globalmente a todos los endpoints del servidor HTTP.
 * 
 * <p>Características:</p>
 * <ul>
 *   <li>Permite solicitudes desde cualquier origen (*)</li>
 *   <li>Soporta credenciales (cookies, autorización HTTP)</li>
 *   <li>Permite todos los métodos HTTP comunes</li>
 *   <li>Permite headers personalizados</li>
 *   <li>Maneja solicitudes preflight (OPTIONS)</li>
 * </ul>
 * 
 * @author Equipo de Desarrollo Ensurance Pharmacy
 * @version 1.0
 * @since 2024
 */
public class CorsFilter extends Filter {
    
    /**
     * Constructor por defecto del filtro CORS.
     */
    public CorsFilter() {
        super();
    }
    
    /**
     * Retorna la descripción del filtro.
     * 
     * @return Descripción del filtro CORS
     */
    @Override
    public String description() {
        return "CORS Filter - Permite solicitudes desde cualquier origen";
    }
    
    /**
     * Aplica los headers CORS a todas las solicitudes HTTP.
     * 
     * <p>Headers configurados:</p>
     * <ul>
     *   <li>Access-Control-Allow-Origin: * (permite cualquier origen)</li>
     *   <li>Access-Control-Allow-Credentials: true (permite cookies)</li>
     *   <li>Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH</li>
     *   <li>Access-Control-Allow-Headers: * (permite cualquier header)</li>
     *   <li>Access-Control-Expose-Headers: * (expone todos los headers)</li>
     *   <li>Access-Control-Max-Age: 3600 (cachea preflight por 1 hora)</li>
     * </ul>
     * 
     * @param exchange El objeto HttpExchange que contiene request y response
     * @param chain La cadena de filtros a ejecutar
     * @throws IOException Si ocurre un error al procesar la solicitud
     */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // Manejo especial de solicitudes OPTIONS (preflight) - debe ser ANTES de todo
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            setCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        // Continuar con la cadena de filtros (ejecuta los handlers)
        chain.doFilter(exchange);
        
        // DESPUÉS de que los handlers se ejecuten, limpiar y establecer headers CORS correctos
        // Esto previene duplicación de headers
        cleanAndSetCorsHeaders(exchange);
    }
    
    /**
     * Limpia cualquier header CORS duplicado y establece los correctos.
     * 
     * @param exchange El objeto HttpExchange
     */
    private void cleanAndSetCorsHeaders(HttpExchange exchange) {
        // Remover cualquier header CORS que los handlers hayan añadido
        exchange.getResponseHeaders().remove("Access-Control-Allow-Origin");
        exchange.getResponseHeaders().remove("Access-Control-Allow-Credentials");
        exchange.getResponseHeaders().remove("Access-Control-Allow-Methods");
        exchange.getResponseHeaders().remove("Access-Control-Allow-Headers");
        exchange.getResponseHeaders().remove("Access-Control-Expose-Headers");
        exchange.getResponseHeaders().remove("Access-Control-Max-Age");
        
        // Establecer headers CORS limpios
        setCorsHeaders(exchange);
    }
    
    /**
     * Establece los headers CORS en la respuesta.
     * 
     * @param exchange El objeto HttpExchange
     */
    private void setCorsHeaders(HttpExchange exchange) {
        // Obtener el origen de la solicitud
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        
        // Configurar headers CORS de manera robusta
        if (origin != null && !origin.isEmpty()) {
            // Permitir el origen específico de la solicitud
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            // Solo permitir credenciales cuando el origen es específico
            exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        } else {
            // Si no hay origen, permitir cualquiera (para peticiones directas)
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        }
        
        // Métodos HTTP permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        
        // Headers permitidos (acepta cualquier header personalizado)
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Requested-With, X-API-Key, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
        
        // Headers expuestos (permite que el cliente acceda a estos headers)
        exchange.getResponseHeaders().set("Access-Control-Expose-Headers", 
            "Content-Type, Authorization, X-API-Key, Content-Length, X-Request-Id");
        
        // Tiempo de cache para preflight (1 hora)
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }
}

