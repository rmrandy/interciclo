package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Order;
import com.sources.app.dao.OrderDAO;
import com.sources.app.dao.MedicineDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OrderHandler implements HttpHandler {
    private final OrderDAO orderDAO;
    private final MedicineDAO medicineDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/orders";

    public OrderHandler(OrderDAO orderDAO, MedicineDAO medicineDAO) {
        this.orderDAO = orderDAO;
        this.medicineDAO = medicineDAO;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (!path.startsWith(ENDPOINT)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Order> orders = orderDAO.getAll();
        String response = objectMapper.writeValueAsString(orders);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Order order = objectMapper.readValue(requestBody, Order.class);
        // Validar y procesar la orden
        Order createdOrder = orderDAO.create(order);
        if (createdOrder != null) {
            // Actualizar el inventario de productos
            for (Order.OrderItem item : order.getItems()) {
                medicineDAO.updateStock(item.getProductId(), item.getQuantity());
            }
            String response = objectMapper.writeValueAsString(createdOrder);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create order\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
} 