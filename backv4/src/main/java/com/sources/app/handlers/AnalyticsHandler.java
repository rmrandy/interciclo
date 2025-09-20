package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sources.app.dao.AnalyticsDAO;
import com.sources.app.entities.ClickEvent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AnalyticsHandler implements HttpHandler {
    private final AnalyticsDAO dao = new AnalyticsDAO();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String origin = getHeader(exchange, "Origin");
        if (origin == null || origin.isBlank()) origin = "*";
        // CORS dinámico: reflejar Origin cuando existe y permitir credenciales
        exchange.getResponseHeaders().set("Vary", "Origin");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method) && path.endsWith("/click")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                ClickEvent ev = new ClickEvent();
                ev.setUserId(getAsInteger(json, "userId"));
                ev.setSessionId(truncate(getAsString(json, "sessionId"), 100));
                java.time.OffsetDateTime now = java.time.OffsetDateTime.now().withNano(0);
                String iso = now.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                // Por seguridad, recortar a 30 caracteres si excede
                ev.setEventTime(iso.length() > 30 ? iso.substring(0, 30) : iso);
                ev.setUrlPath(truncate(getAsString(json, "urlPath"), 500));
                ev.setFullUrl(truncate(getAsString(json, "fullUrl"), 1000));
                ev.setPageTitle(truncate(getAsString(json, "pageTitle"), 500));
                ev.setElementTag(truncate(getAsString(json, "elementTag"), 50));
                ev.setElementIdAttr(truncate(getAsString(json, "elementId"), 200));
                ev.setElementClasses(truncate(getAsString(json, "elementClasses"), 500));
                ev.setTextSnippet(truncate(getAsString(json, "textSnippet"), 500));
                ev.setCssSelector(truncate(getAsString(json, "cssSelector"), 1000));
                ev.setxPos(getAsInteger(json, "x"));
                ev.setyPos(getAsInteger(json, "y"));
                ev.setViewportWidth(getAsInteger(json, "vpW"));
                ev.setViewportHeight(getAsInteger(json, "vpH"));
                ev.setUserAgent(getHeader(exchange, "User-Agent"));
                ev.setIpAddress(exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : null);

                Long id = dao.saveClick(ev);
                write(exchange, 200, gson.toJson(java.util.Map.of("success", true, "id", id)));
                return;
            }

            if ("GET".equalsIgnoreCase(method) && path.endsWith("/events")) {
                var q = exchange.getRequestURI().getQuery();
                java.util.Map<String, String> params = new java.util.HashMap<>();
                if (q != null) {
                    for (String p : q.split("&")) {
                        String[] kv = p.split("=");
                        if (kv.length == 2) params.put(kv[0], java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8));
                    }
                }
                String from = params.getOrDefault("from", null);
                String to = params.getOrDefault("to", null);
                Integer uid = params.containsKey("userId") ? Integer.valueOf(params.get("userId")) : null;
                String pathLike = params.containsKey("pathLike") ? params.get("pathLike") : null;
                int limit = params.containsKey("limit") ? Integer.parseInt(params.get("limit")) : 1000;

                var list = dao.listClicks(from, to, uid, pathLike, limit);
                write(exchange, 200, gson.toJson(java.util.Map.of("success", true, "events", list)));
                return;
            }

            if ("GET".equalsIgnoreCase(method) && path.endsWith("/events.csv")) {
                var q = exchange.getRequestURI().getQuery();
                java.util.Map<String, String> params = new java.util.HashMap<>();
                if (q != null) {
                    for (String p : q.split("&")) {
                        String[] kv = p.split("=");
                        if (kv.length == 2) params.put(kv[0], java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8));
                    }
                }
                String from = params.getOrDefault("from", null);
                String to = params.getOrDefault("to", null);
                Integer uid = params.containsKey("userId") ? Integer.valueOf(params.get("userId")) : null;
                String pathLike = params.containsKey("pathLike") ? params.get("pathLike") : null;
                int limit = params.containsKey("limit") ? Integer.parseInt(params.get("limit")) : 10000;

                var list = dao.listClicks(from, to, uid, pathLike, limit);
                StringBuilder csv = new StringBuilder();
                csv.append("idClick,eventTime,userId,sessionId,urlPath,elementTag,elementId,classes,textSnippet,x,y\n");
                for (var ev : list) {
                    csv.append(nullSafe(ev.getIdClick())).append(',')
                       .append(quote(ev.getEventTime())).append(',')
                       .append(nullSafe(ev.getUserId())).append(',')
                       .append(quote(ev.getSessionId())).append(',')
                       .append(quote(ev.getUrlPath())).append(',')
                       .append(quote(ev.getElementTag())).append(',')
                       .append(quote(ev.getElementIdAttr())).append(',')
                       .append(quote(ev.getElementClasses())).append(',')
                       .append(quote(ev.getTextSnippet())).append(',')
                       .append(nullSafe(ev.getxPos())).append(',')
                       .append(nullSafe(ev.getyPos())).append('\n');
                }
                byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/csv; charset=UTF-8");
                exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=events.csv");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
                return;
            }

            write(exchange, 404, gson.toJson(java.util.Map.of("success", false, "error", "Not found")));
        } catch (Exception e) {
            e.printStackTrace();
            write(exchange, 500, gson.toJson(java.util.Map.of("success", false, "error", e.getMessage())));
        }
    }

    private static String getHeader(HttpExchange ex, String name) {
        try { return ex.getRequestHeaders().getFirst(name); } catch (Exception ignore) { return null; }
    }

    private static String getAsString(JsonObject json, String key) {
        try {
            if (!json.has(key)) return null;
            var el = json.get(key);
            if (el == null || el.isJsonNull()) return null;
            return el.getAsString();
        } catch (Exception e) { return null; }
    }

    private static Integer getAsInteger(JsonObject json, String key) {
        try {
            if (!json.has(key)) return null;
            var el = json.get(key);
            if (el == null || el.isJsonNull()) return null;
            return el.getAsInt();
        } catch (Exception e) { return null; }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static void write(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static String quote(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
        return '"' + v + '"';
    }
    private static String nullSafe(Object o) { return o == null ? "" : String.valueOf(o); }
}


