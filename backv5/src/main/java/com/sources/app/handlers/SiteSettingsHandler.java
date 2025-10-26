package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.SystemConfigDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler para exponer y actualizar configuración de branding del sitio
 * (header y footer) mediante claves en SYSTEM_CONFIG.
 *
 * GET  /api/site-settings              -> devuelve todas las claves relevantes
 * PUT  /api/site-settings              -> actualiza una o varias claves
 */
public class SiteSettingsHandler implements HttpHandler {

    private final SystemConfigDAO configDAO;
    private final ObjectMapper mapper = new ObjectMapper();

    public SiteSettingsHandler(SystemConfigDAO configDAO) {
        this.configDAO = configDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "PUT":
                    handlePut(exchange);
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        Map<String, String> payload = new HashMap<>();
        payload.put("brand.title", configDAO.getConfigValue("SITE_BRAND_TITLE", "AeroLinea"));
        payload.put("brand.subtitle", configDAO.getConfigValue("SITE_BRAND_SUBTITLE", "Tu compañía de seguros aeronáuticos"));
        payload.put("footer.text", configDAO.getConfigValue("SITE_FOOTER_TEXT", "© 2025 AeroLinea. Todos los derechos reservados."));
        payload.put("brand.logoUrl", configDAO.getConfigValue("SITE_BRAND_LOGO_URL", ""));

        byte[] bytes = mapper.writeValueAsBytes(payload);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> req = mapper.readValue(body, Map.class);

        String title = str(req.get("brand.title"));
        String subtitle = str(req.get("brand.subtitle"));
        String footer = str(req.get("footer.text"));
        String logoUrl = str(req.get("brand.logoUrl"));

        if (title != null) configDAO.saveOrUpdate("SITE_BRAND_TITLE", title, "Título de marca mostrado en header");
        if (subtitle != null) configDAO.saveOrUpdate("SITE_BRAND_SUBTITLE", subtitle, "Subtítulo mostrado en header");
        if (footer != null) configDAO.saveOrUpdate("SITE_FOOTER_TEXT", footer, "Texto del footer");
        if (logoUrl != null) configDAO.saveOrUpdate("SITE_BRAND_LOGO_URL", logoUrl, "URL del logo en header");

        handleGet(exchange); // devolver valores actuales
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}


