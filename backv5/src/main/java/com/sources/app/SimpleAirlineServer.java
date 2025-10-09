package com.sources.app;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleAirlineServer {
    private static final int DEFAULT_PORT = 8085;
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int THREAD_POOL_SIZE = 10;
    
    private int port;
    private String host;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running = false;
    
    public SimpleAirlineServer() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }
    
    public SimpleAirlineServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            System.out.println("🚀 Servidor Simple AeroLinea iniciado en " + host + ":" + port);
            System.out.println("📡 Endpoints disponibles:");
            System.out.println("   GET  /api/health - Estado del servidor");
            System.out.println("   POST /api/airline/register - Registro de usuarios (mock)");
            System.out.println("   POST /api/airline/login - Login de usuarios (mock)");
            // System.out.println("   OPTIONS /* - CORS preflight");
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }
            
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
    
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el servidor: " + e.getMessage());
            }
        }
        threadPool.shutdown();
        System.out.println("🛑 Servidor detenido");
    }
    
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // Leer la primera línea (método HTTP y ruta)
            String requestLine = in.readLine();
            if (requestLine == null) return;
            
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) return;
            
            String method = requestParts[0];
            String path = requestParts[1];
            
            // Leer headers
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.contains(":")) {
                    String[] headerParts = line.split(":", 2);
                    headers.put(headerParts[0].trim(), headerParts[1].trim());
                }
            }
            
            // Leer body si existe
            StringBuilder body = new StringBuilder();
            if (headers.containsKey("Content-Length")) {
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                char[] buffer = new char[contentLength];
                in.read(buffer, 0, contentLength);
                body.append(buffer);
            }
            
            // Procesar request
            String response = processRequest(method, path, headers, body.toString());
            
            // Enviar respuesta sin CORS
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json; charset=UTF-8");
            out.println("Content-Length: " + response.getBytes("UTF-8").length);
            out.println();
            out.println(response);
            
        } catch (IOException e) {
            System.err.println("Error al manejar cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
    
    private String processRequest(String method, String path, Map<String, String> headers, String body) {
        try {
            // Manejar solicitudes OPTIONS (sin CORS)
            if (method.equals("OPTIONS")) {
                return "{\"status\": \"ok\", \"message\": \"OPTIONS request handled\"}";
            }
            
            // Health check
            if (path.equals("/api/health")) {
                return "{\"status\": \"ok\", \"service\": \"Simple AeroLinea API\", \"version\": \"1.0.0\", \"message\": \"Servidor funcionando correctamente\"}";
            }
            
            // Mock registro
            if (path.equals("/api/airline/register") && method.equals("POST")) {
                return "{\"success\": true, \"message\": \"Usuario registrado exitosamente (mock)\", \"userId\": 12345}";
            }
            
            // Mock login
            if (path.equals("/api/airline/login") && method.equals("POST")) {
                return "{\"success\": true, \"message\": \"Login exitoso (mock)\", \"token\": \"mock-token-12345\"}";
            }
            
            // Endpoint no encontrado
            return "{\"success\": false, \"error\": \"Endpoint no encontrado: " + method + " " + path + "\"}";
            
        } catch (Exception e) {
            return "{\"success\": false, \"error\": \"Error interno del servidor: " + e.getMessage() + "\"}";
        }
    }
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String host = DEFAULT_HOST;
        
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido: " + args[0]);
                return;
            }
        }
        
        if (args.length >= 2) {
            host = args[1];
        }
        
        SimpleAirlineServer server = new SimpleAirlineServer(host, port);
        
        // Agregar shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Cerrando servidor...");
            server.stop();
        }));
        
        server.start();
    }
}

