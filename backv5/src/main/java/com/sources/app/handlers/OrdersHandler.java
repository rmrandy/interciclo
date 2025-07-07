package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.dao.OrdersDAO;
import com.sources.app.entities.Orders;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.sources.app.entities.User; // Import para validación
import com.sources.app.dao.OrderMedicineDAO;
import com.sources.app.dao.MedicineDAO;
import com.sources.app.entities.Medicine;

/**
 * Manejador HTTP para gestionar las operaciones CRUD de los Pedidos (Orders).
 * Responde a solicitudes en el endpoint "/api2/orders".
 * Soporta los métodos GET, POST, PUT y OPTIONS.
 */
public class OrdersHandler implements HttpHandler {
    private final OrdersDAO ordersDAO;
    private final OrderMedicineDAO orderMedicineDAO;
    private final MedicineDAO medicineDAO;
    private final ObjectMapper objectMapper;
    private static final String ENDPOINT = "/api2/orders";

    /**
     * Constructor para OrdersHandler.
     *
     * @param ordersDAO El DAO para acceder a los datos de los pedidos.
     * @param orderMedicineDAO El DAO para acceder a los datos de los registros de medicamentos en pedidos.
     * @param medicineDAO El DAO para acceder a los datos de los medicamentos.
     */
    public OrdersHandler(OrdersDAO ordersDAO, OrderMedicineDAO orderMedicineDAO, MedicineDAO medicineDAO) {
        this.ordersDAO = ordersDAO;
        this.orderMedicineDAO = orderMedicineDAO;
        this.medicineDAO = medicineDAO;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Maneja las solicitudes HTTP entrantes para el endpoint de pedidos.
     * Configura las cabeceras CORS y delega a los métodos de manejo apropiados
     * según el método HTTP (handlePost, handleGet, handlePut).
     *
     * @param exchange El objeto HttpExchange que representa la solicitud y respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Handle CORS preflight requests
        if("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())){
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            if ("POST".equalsIgnoreCase(method) && path.endsWith("/checkout")) {
                handleCheckout(exchange);
            } else if("POST".equalsIgnoreCase(method)){
                handlePost(exchange);
            } else if("GET".equalsIgnoreCase(method)){
                handleGet(exchange);
            } else if("PUT".equalsIgnoreCase(method)){
                handlePut(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    /**
     * Maneja las solicitudes POST para crear un nuevo pedido.
     * Espera un cuerpo JSON con el estado del pedido y el ID del usuario asociado.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Orders createOrder = objectMapper.readValue(requestBody, Orders.class);
        // Validar datos requeridos
        User user = createOrder.getUser();
        if (user == null || user.getIdUser() == null) {
            sendResponse(exchange, 400, "{\"error\": \"User ID is required\"}");
            return;
        }
        String status = createOrder.getStatus();
        if (status == null || status.trim().isEmpty()) {
            status = "recibido";
        }
        // Llamar al DAO con status y userId
        Orders order = ordersDAO.create(status, user.getIdUser()); 
        if(order != null) {
            sendResponse(exchange, 201, objectMapper.writeValueAsString(order));
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Failed to create order\"}");
        }
    }

    /**
     * Maneja las solicitudes GET para obtener pedidos.
     * Si se proporciona un parámetro de consulta 'id', devuelve el pedido específico.
     * Si se proporciona 'status', filtra por estado.
     * Si se proporciona 'userId', filtra por usuario.
     * De lo contrario, devuelve todos los pedidos.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if(query != null && query.startsWith("id=")) {
            handleGetById(exchange, query);
        } else if(query != null && query.startsWith("status=")) {
            handleGetByStatus(exchange, query);
        } else if(query != null && query.startsWith("userId=")) {
            handleGetByUserId(exchange, query);
        } else if(query != null && query.contains("userId=") && query.contains("status=")) {
            handleGetByUserIdAndStatus(exchange, query);
        } else {
            handleGetAll(exchange);
        }
    }

    /**
     * Maneja la obtención de un pedido específico por su ID.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID (formato: id=orderId).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetById(HttpExchange exchange, String query) throws IOException {
        try {
            Long id = Long.parseLong(query.substring(3));
            Orders order = ordersDAO.getById(id);
            if(order != null) {
                sendResponse(exchange, 200, objectMapper.writeValueAsString(order));
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * Maneja la obtención de pedidos filtrados por estado.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el estado (formato: status=estado).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetByStatus(HttpExchange exchange, String query) throws IOException {
        try {
            String status = query.substring(7); // "status=".length() == 7
            List<Orders> list = ordersDAO.getByStatus(status);
            if (list != null) {
                sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Error retrieving orders by status\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid status parameter\"}");
        }
    }

    /**
     * Maneja la obtención de pedidos filtrados por usuario.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID del usuario (formato: userId=123).
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetByUserId(HttpExchange exchange, String query) throws IOException {
        try {
            Long userId = Long.parseLong(query.substring(7)); // "userId=".length() == 7
            List<Orders> list = ordersDAO.getByUserId(userId);
            if (list != null) {
                sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Error retrieving orders by user\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid userId format\"}");
        }
    }

    /**
     * Maneja la obtención de pedidos filtrados por usuario y estado.
     *
     * @param exchange El objeto HttpExchange.
     * @param query La cadena de consulta que contiene el ID del usuario y estado.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetByUserIdAndStatus(HttpExchange exchange, String query) throws IOException {
        try {
            // Parsear userId y status de la query string
            String[] params = query.split("&");
            Long userId = null;
            String status = null;
            
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    userId = Long.parseLong(param.substring(7));
                } else if (param.startsWith("status=")) {
                    status = param.substring(7);
                }
            }
            
            if (userId != null && status != null) {
                List<Orders> list = ordersDAO.getByUserIdAndStatus(userId, status);
                if (list != null) {
                    sendResponse(exchange, 200, objectMapper.writeValueAsString(list));
                } else {
                    sendResponse(exchange, 500, "{\"error\": \"Error retrieving orders by user and status\"}");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Both userId and status parameters are required\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid userId format\"}");
        }
    }

    /**
     * Maneja la obtención de todos los pedidos.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Orders> list = ordersDAO.getAll();
        // Crear una lista de mapas para incluir el total calculado
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (Orders order : list) {
            java.util.Map<String, Object> map = objectMapper.convertValue(order, java.util.Map.class);
            // Calcular el total sumando los OrderMedicine
            double total = 0.0;
            try {
                List<com.sources.app.entities.OrderMedicine> items = orderMedicineDAO.getByOrderId(order.getIdOrder());
                if (items != null) {
                    for (com.sources.app.entities.OrderMedicine om : items) {
                        try {
                            total += Double.parseDouble(om.getTotal());
                        } catch (Exception e) {
                            // Si hay error en el parseo, ignorar ese item
                        }
                    }
                }
            } catch (Exception e) {
                // Si hay error, dejar total en 0
            }
            map.put("total", total);
            result.add(map);
        }
        sendResponse(exchange, 200, objectMapper.writeValueAsString(result));
    }

    /**
     * Maneja las solicitudes PUT para actualizar un pedido existente.
     * Si la ruta termina en /status, actualiza solo el estado del pedido.
     * De lo contrario, actualiza el pedido completo.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handlePut(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        if (path.endsWith("/status")) {
            handleUpdateStatus(exchange);
        } else {
            handleUpdateOrder(exchange);
        }
    }

    /**
     * Maneja la actualización del estado de un pedido específico.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleUpdateStatus(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Verificar que la ruta tenga el formato correcto: /api2/orders/{id}/status
        if (!path.endsWith("/status")) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid endpoint for status update. Expected: /api2/orders/{id}/status\"}");
            return;
        }
        
        String[] pathParts = path.split("/");
        
        // La ruta debe tener al menos 5 partes: ["", "api2", "orders", "{id}", "status"]
        if (pathParts.length < 5) {
            sendResponse(exchange, 400, "{\"error\": \"Order ID is required for status update\"}");
            return;
        }
        
        try {
            // El ID está en la posición 3 (índice 3) cuando la ruta es /api2/orders/{id}/status
            Long orderId = Long.parseLong(pathParts[3]);
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            
            // Esperar un JSON simple con el nuevo estado
            String newStatus = objectMapper.readTree(requestBody).get("status").asText();
            
            if (newStatus == null || newStatus.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Status is required\"}");
                return;
            }
            
            Orders updatedOrder = ordersDAO.updateStatus(orderId, newStatus);
            if (updatedOrder != null) {
                sendResponse(exchange, 200, objectMapper.writeValueAsString(updatedOrder));
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Order not found or update failed\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid order ID format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid request body format\"}");
        }
    }

    /**
     * Maneja la actualización completa de un pedido existente.
     *
     * @param exchange El objeto HttpExchange.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void handleUpdateOrder(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Orders updateOrder = objectMapper.readValue(requestBody, Orders.class);
        
        if (updateOrder.getIdOrder() == null) {
             sendResponse(exchange, 400, "{\"error\": \"Order ID is required for update\"}");
             return;
        }
        // Podrían ir más validaciones (e.g., status no vacío)
        
        Orders order = ordersDAO.update(updateOrder);
        if(order != null){
            sendResponse(exchange, 200, objectMapper.writeValueAsString(order));
        } else {
             sendResponse(exchange, 400, "{\"error\": \"Failed to update order or order not found\"}");
        }
    }
    
    /**
     * Envía una respuesta HTTP con un código de estado y cuerpo específicos.
     *
     * @param exchange El objeto HttpExchange.
     * @param statusCode El código de estado HTTP.
     * @param responseBody El cuerpo de la respuesta como String.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Maneja el checkout de una orden: crea la orden, los productos y descuenta inventario.
     */
    private void handleCheckout(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            ObjectMapper mapper = new ObjectMapper();
            CheckoutRequest checkout = mapper.readValue(requestBody, CheckoutRequest.class);
            if (checkout.userId == null || checkout.productos == null || checkout.productos.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"userId y productos son requeridos\"}");
                return;
            }
            // 1. Crear la orden
            Orders order = ordersDAO.create("Pagado", checkout.userId);
            if (order == null) {
                sendResponse(exchange, 500, "{\"error\": \"No se pudo crear la orden\"}");
                return;
            }
            // 2. Crear los registros en OrderMedicine y descontar inventario
            for (ProductoCompra prod : checkout.productos) {
                Medicine med = medicineDAO.getById(prod.idMedicine);
                if (med == null) {
                    sendResponse(exchange, 400, "{\"error\": \"Producto no encontrado: " + prod.idMedicine + "\"}");
                    return;
                }
                if (med.getStock() < prod.quantity) {
                    sendResponse(exchange, 400, "{\"error\": \"Stock insuficiente para: " + med.getName() + "\"}");
                    return;
                }
                // Descontar inventario
                med.setStock(med.getStock() - prod.quantity);
                medicineDAO.update(med);
                // Crear OrderMedicine
                orderMedicineDAO.create(order, med, prod.quantity, med.getPrice(), String.valueOf(med.getPrice() * prod.quantity));
            }
            sendResponse(exchange, 201, "{\"success\": true, \"orderId\": " + order.getIdOrder() + "}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Error procesando el checkout\"}");
        }
    }

    private static class CheckoutRequest {
        public Long userId;
        public java.util.List<ProductoCompra> productos;
        public Pago pago;
    }
    private static class ProductoCompra {
        public Long idMedicine;
        public Integer quantity;
    }
    private static class Pago {
        public String cardName;
        public String cardNumber;
        public String expDate;
        public String cvc;
    }
}
