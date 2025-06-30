package com.sources.app.handlers;

import com.sources.app.entities.Medicine;
import com.sources.app.dao.MedicineDAO;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import com.google.gson.Gson;

public class BestSellersHandler implements HttpHandler {
    private final MedicineDAO medicineDAO;
    private final Gson gson;

    public BestSellersHandler(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Medicine> bestSellers = medicineDAO.getAll();
            String response = gson.toJson(bestSellers);
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
} 