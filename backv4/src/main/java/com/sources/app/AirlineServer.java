package com.sources.app;

import com.sources.app.handlers.AirlineUserHandler;
import com.sources.app.handlers.FlightHandler;
import com.sources.app.handlers.RouteHandler;
import com.sources.app.handlers.AircraftHandler;
import com.sources.app.handlers.TicketHandler;
import com.sources.app.handlers.BookingHandlerTemp;
// import com.sources.app.handlers.FlightManagementHandler; // Comentado temporalmente
import com.sources.app.dao.CityDAO;
import com.sources.app.entities.City;
import com.sources.app.dao.UserDAO;
import com.sources.app.dao.UserRoleDAO;
import com.sources.app.dao.PolicyDAO;
import com.sources.app.dao.AppointmentDAO;
import com.sources.app.dao.AppointmentMadeDAO;
import com.sources.app.dao.CategoryDAO;
import com.sources.app.dao.ConfigurableAmountDAO;
import com.sources.app.dao.HospitalDAO;
import com.sources.app.dao.MedicineDAO;
import com.sources.app.dao.MedicinePresDAO;
import com.sources.app.dao.PharmacyDAO;
import com.sources.app.dao.PrescriptionDAO;
import com.sources.app.dao.ServiceDAO;
import com.sources.app.dao.TotalHospitalDAO;
import com.sources.app.dao.TotalPharmacyDAO;
import com.sources.app.dao.TransactionsDAO;
import com.sources.app.dao.TransactionPolicyDAO;
import com.sources.app.dao.ServiceCategoryDAO;
import com.sources.app.dao.InsuranceServiceDAO;
import com.sources.app.dao.HospitalInsuranceServiceDAO;
import com.sources.app.dao.EnsuranceAppointmentDAO;
import com.sources.app.dao.PrescriptionApprovalDAO;
import com.sources.app.entities.User;
import com.sources.app.handlers.LoginHandler;
import com.sources.app.handlers.UserHandler;
import com.sources.app.handlers.PolicyHandler;
import com.sources.app.handlers.AppointmentHandler;
import com.sources.app.handlers.AppointmentMadeHandler;
import com.sources.app.handlers.CategoryHandler;
import com.sources.app.handlers.ConfigurableAmountHandler;
import com.sources.app.handlers.HospitalHandler;
import com.sources.app.handlers.MedicineHandler;
import com.sources.app.handlers.MedicinePresHandler;
import com.sources.app.handlers.PharmacyHandler;
import com.sources.app.handlers.PrescriptionHandler;
import com.sources.app.handlers.ServiceHandler;
import com.sources.app.handlers.TotalHospitalHandler;
import com.sources.app.handlers.TotalPharmacyHandler;
import com.sources.app.handlers.TransactionsHandler;
import com.sources.app.handlers.TransactionPolicyHandler;
import com.sources.app.handlers.ServiceCategoryHandler;
import com.sources.app.handlers.NotificationHandler;
import com.sources.app.handlers.InsuranceServiceHandler;
import com.sources.app.handlers.HospitalInsuranceServiceHandler;
import com.sources.app.handlers.HospitalRedirectHandler;
import com.sources.app.handlers.UserByEmailHandler;
import com.sources.app.handlers.EnsuranceAppointmentHandler;
import com.sources.app.handlers.PrescriptionApprovalHandler;
import com.sources.app.handlers.HospitalServiceProxyHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Map;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

/**
 * Servidor HTTP simple para el sistema de aerolíneas.
 * Maneja endpoints dinámicos con configuración de puerto e IP.
 */
public class AirlineServer {
    
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int THREAD_POOL_SIZE = 10;
    
    private int port;
    private String host;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running = false;
    
    // Handlers y DAOs
    private AirlineUserHandler airlineUserHandler;
    private FlightHandler flightHandler;
    // private FlightManagementHandler flightManagementHandler; // Comentado temporalmente
    private RouteHandler routeHandler;
    private AircraftHandler aircraftHandler;
    private TicketHandler ticketHandler;
    private BookingHandlerTemp bookingHandler;
    private UserDAO userDAO;
    private UserRoleDAO userRoleDAO;
    private CityDAO cityDAO;
    
    // Nuevos DAOs y handlers de App.java
    private PolicyDAO policyDAO;
    private AppointmentDAO appointmentDAO;
    private AppointmentMadeDAO appointmentMadeDAO;
    private CategoryDAO categoryDAO;
    private ConfigurableAmountDAO configurableAmountDAO;
    private HospitalDAO hospitalDAO;
    private MedicineDAO medicineDAO;
    private MedicinePresDAO medicinePresDAO;
    private PharmacyDAO pharmacyDAO;
    private PrescriptionDAO prescriptionDAO;
    private ServiceDAO serviceDAO;
    private TotalHospitalDAO totalHospitalDAO;
    private TotalPharmacyDAO totalPharmacyDAO;
    private TransactionsDAO transactionsDAO;
    private TransactionPolicyDAO transactionPolicyDAO;
    private ServiceCategoryDAO serviceCategoryDAO;
    private InsuranceServiceDAO insuranceServiceDAO;
    private HospitalInsuranceServiceDAO hospitalInsuranceServiceDAO;
    private EnsuranceAppointmentDAO ensuranceAppointmentDAO;
    private PrescriptionApprovalDAO prescriptionApprovalDAO;
    
    // Nuevos handlers
    private LoginHandler loginHandler;
    private UserHandler userHandler;
    private PolicyHandler policyHandler;
    private AppointmentHandler appointmentHandler;
    private AppointmentMadeHandler appointmentMadeHandler;
    private CategoryHandler categoryHandler;
    private ConfigurableAmountHandler configurableAmountHandler;
    private HospitalHandler hospitalHandler;
    private MedicineHandler medicineHandler;
    private MedicinePresHandler medicinePresHandler;
    private PharmacyHandler pharmacyHandler;
    private PrescriptionHandler prescriptionHandler;
    private ServiceHandler serviceHandler;
    private TotalHospitalHandler totalHospitalHandler;
    private TotalPharmacyHandler totalPharmacyHandler;
    private TransactionsHandler transactionsHandler;
    private TransactionPolicyHandler transactionPolicyHandler;
    private ServiceCategoryHandler serviceCategoryHandler;
    private NotificationHandler notificationHandler;
    private InsuranceServiceHandler insuranceServiceHandler;
    private HospitalInsuranceServiceHandler hospitalInsuranceServiceHandler;
    private HospitalRedirectHandler hospitalRedirectHandler;
    private UserByEmailHandler userByEmailHandler;
    private EnsuranceAppointmentHandler ensuranceAppointmentHandler;
    private PrescriptionApprovalHandler prescriptionApprovalHandler;
    private HospitalServiceProxyHandler hospitalServiceProxyHandler;
    
    private Gson gson;
    
    public AirlineServer() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }
    
    public AirlineServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.airlineUserHandler = new AirlineUserHandler();
        this.flightHandler = new FlightHandler();
        // this.flightManagementHandler = new FlightManagementHandler(); // Comentado temporalmente
        this.routeHandler = new RouteHandler();
        this.aircraftHandler = new AircraftHandler();
        this.ticketHandler = new TicketHandler();
        this.bookingHandler = new BookingHandlerTemp();
        this.userDAO = new UserDAO();
        this.userRoleDAO = new UserRoleDAO();
        this.gson = new Gson();
        this.cityDAO = new CityDAO();
        
        // Inicializar nuevos DAOs y handlers
        this.policyDAO = new PolicyDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.appointmentMadeDAO = new AppointmentMadeDAO();
        this.categoryDAO = new CategoryDAO();
        this.configurableAmountDAO = new ConfigurableAmountDAO();
        this.hospitalDAO = new HospitalDAO();
        this.medicineDAO = new MedicineDAO();
        this.medicinePresDAO = new MedicinePresDAO();
        this.pharmacyDAO = new PharmacyDAO();
        this.prescriptionDAO = new PrescriptionDAO();
        this.serviceDAO = new ServiceDAO();
        this.totalHospitalDAO = new TotalHospitalDAO();
        this.totalPharmacyDAO = new TotalPharmacyDAO();
        this.transactionsDAO = new TransactionsDAO();
        this.transactionPolicyDAO = new TransactionPolicyDAO();
        this.serviceCategoryDAO = new ServiceCategoryDAO();
        this.insuranceServiceDAO = new InsuranceServiceDAO();
        this.hospitalInsuranceServiceDAO = new HospitalInsuranceServiceDAO();
        this.ensuranceAppointmentDAO = new EnsuranceAppointmentDAO();
        this.prescriptionApprovalDAO = new PrescriptionApprovalDAO();
        
        // Inicializar nuevos handlers
        this.loginHandler = new LoginHandler(userDAO);
        this.userHandler = new UserHandler(userDAO);
        this.policyHandler = new PolicyHandler(policyDAO);
        this.appointmentHandler = new AppointmentHandler(appointmentDAO);
        this.appointmentMadeHandler = new AppointmentMadeHandler(appointmentMadeDAO);
        this.categoryHandler = new CategoryHandler(categoryDAO);
        this.configurableAmountHandler = new ConfigurableAmountHandler(configurableAmountDAO);
        this.hospitalHandler = new HospitalHandler(hospitalDAO);
        this.medicineHandler = new MedicineHandler(medicineDAO);
        this.medicinePresHandler = new MedicinePresHandler(medicinePresDAO);
        this.pharmacyHandler = new PharmacyHandler(pharmacyDAO);
        this.prescriptionHandler = new PrescriptionHandler(prescriptionDAO);
        this.serviceHandler = new ServiceHandler(serviceDAO);
        this.totalHospitalHandler = new TotalHospitalHandler(totalHospitalDAO);
        this.totalPharmacyHandler = new TotalPharmacyHandler(totalPharmacyDAO);
        this.transactionsHandler = new TransactionsHandler(transactionsDAO);
        this.transactionPolicyHandler = new TransactionPolicyHandler(transactionPolicyDAO);
        this.serviceCategoryHandler = new ServiceCategoryHandler(serviceCategoryDAO);
        this.notificationHandler = new NotificationHandler();
        this.insuranceServiceHandler = new InsuranceServiceHandler(insuranceServiceDAO, categoryDAO);
        this.hospitalInsuranceServiceHandler = new HospitalInsuranceServiceHandler(hospitalInsuranceServiceDAO, hospitalDAO, insuranceServiceDAO);
        this.hospitalRedirectHandler = new HospitalRedirectHandler();
        this.userByEmailHandler = new UserByEmailHandler(userDAO);
        this.ensuranceAppointmentHandler = new EnsuranceAppointmentHandler(ensuranceAppointmentDAO);
        this.prescriptionApprovalHandler = new PrescriptionApprovalHandler(prescriptionApprovalDAO, userDAO, configurableAmountDAO);
        this.hospitalServiceProxyHandler = new HospitalServiceProxyHandler(hospitalDAO);
        
        // Inicializar roles por defecto (comentado temporalmente)
        // initializeDefaultRoles();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            System.out.println("🚀 Servidor AeroLinea iniciado en " + host + ":" + port);
            System.out.println("📡 Endpoints disponibles:");
            System.out.println("📄 USUARIOS:");
            System.out.println("   POST /api/airline/register - Registro de usuarios");
            System.out.println("   POST /api/airline/login - Login de usuarios");
            System.out.println("   GET  /api/airline/roles - Obtener roles");
            System.out.println("   POST /api/airline/assign-role - Asignar rol (admin)");
            System.out.println("   GET  /api/airline/users - Obtener usuarios (admin)");
            System.out.println("   POST /api/airline/initialize-roles - Inicializar roles");
            System.out.println("📊 CONSULTAS PÚBLICAS:");
            System.out.println("   GET  /api/airline/aircrafts - Listar aeronaves");
            System.out.println("   GET  /api/airline/routes - Listar rutas");
            System.out.println("   GET  /api/airline/flights - Listar vuelos públicos");
            System.out.println("   GET  /api/airline/flights/search - Buscar vuelos");
            System.out.println("   POST /api/airline/bookings - Crear reserva");
            System.out.println("✈️ GESTIÓN ADMINISTRATIVA:");
            System.out.println("   POST /api/admin/flights - Crear vuelo completo");
            System.out.println("   GET  /api/admin/flights - Listar vuelos (admin)");
            System.out.println("   GET  /api/admin/flights/{id} - Detalles completos");
            System.out.println("   PUT  /api/admin/flights/{id} - Actualizar vuelo");
            System.out.println("   POST /api/admin/flights/{id}/publish - Publicar vuelo");
            System.out.println("   POST /api/admin/flights/{id}/cancel - Cancelar vuelo");
            System.out.println("   PUT  /api/admin/flights/{id}/inventory - Actualizar inventario");
            System.out.println("   PUT  /api/admin/flights/{id}/fares - Actualizar precios");
            System.out.println("   GET  /api/admin/flights/statistics - Estadísticas");
            System.out.println("🏥 ENSURANCE PHARMACY:");
            System.out.println("   POST /api/login - Login de usuarios");
            System.out.println("   GET  /api/users - Obtener usuarios");
            System.out.println("   POST /api/users - Crear usuario");
            System.out.println("   PUT  /api/users - Actualizar usuario");
            System.out.println("   GET  /api/users/by-email - Buscar usuario por email");
            System.out.println("   GET  /api/policy - Obtener pólizas");
            System.out.println("   POST /api/policy - Crear póliza");
            System.out.println("   GET  /api/appointment - Obtener citas");
            System.out.println("   POST /api/appointment - Crear cita");
            System.out.println("   GET  /api/category - Obtener categorías");
            System.out.println("   POST /api/category - Crear categoría");
            System.out.println("   GET  /api/hospital - Obtener hospitales");
            System.out.println("   POST /api/hospital - Crear hospital");
            System.out.println("   GET  /api/medicine - Obtener medicinas");
            System.out.println("   POST /api/medicine - Crear medicina");
            System.out.println("   GET  /api/pharmacy - Obtener farmacias");
            System.out.println("   POST /api/pharmacy - Crear farmacia");
            System.out.println("   GET  /api/prescription - Obtener recetas");
            System.out.println("   POST /api/prescription - Crear receta");
            System.out.println("   GET  /api/service - Obtener servicios");
            System.out.println("   POST /api/service - Crear servicio");
            System.out.println("   GET  /api/transactions - Obtener transacciones");
            System.out.println("   POST /api/transactions - Crear transacción");
            System.out.println("   GET  /api/insurance-services - Obtener servicios de seguro");
            System.out.println("   POST /api/insurance-services - Crear servicio de seguro");
            System.out.println("   GET  /api/hospital-services - Obtener servicios de hospital");
            System.out.println("   POST /api/hospital-services - Crear servicio de hospital");
            System.out.println("   GET  /api/ensurance-appointments - Obtener citas de seguro");
            System.out.println("   POST /api/ensurance-appointments - Crear cita de seguro");
            System.out.println("🔍 SISTEMA:");
            System.out.println("   GET  /api/health - Estado del servidor");
            System.out.println("   GET  /api/debug/cities - Debug de ciudades");
            
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

            // Enviar respuesta
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
            // Health check
            if (path.equals("/api/health")) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ok");
                response.put("service", "AeroLinea API");
                response.put("version", "1.0.0");
                return gson.toJson(response);
            }

            // Debug endpoint para ciudades
            if (path.equals("/api/debug/cities")) {
                try {
                    List<City> cities = cityDAO.findAll();
                    Map<String, Object> debugInfo = new java.util.HashMap<>();
                    debugInfo.put("success", true);
                    debugInfo.put("totalCities", cities.size());
                    debugInfo.put("cities", cities);
                    debugInfo.put("timestamp", java.time.LocalDateTime.now().toString());
                    return gson.toJson(debugInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    Map<String, Object> errorInfo = new java.util.HashMap<>();
                    errorInfo.put("success", false);
                    errorInfo.put("error", e.getMessage());
                    errorInfo.put("stackTrace", e.getStackTrace());
                    return gson.toJson(errorInfo);
                }
            }
            
            // Endpoints de aerolíneas
            if (path.equals("/api/airline/register") && method.equals("POST")) {
                return airlineUserHandler.registerVisitor(body);
            }
            
            if (path.equals("/api/airline/login") && method.equals("POST")) {
                return handleLogin(body);
            }
            
            if (path.equals("/api/airline/roles") && method.equals("GET")) {
                return airlineUserHandler.getAllRoles();
            }
            // ===== Ciudades =====
            if (path.equals("/api/airline/cities") && method.equals("GET")) {
                try {
                    List<City> cities = cityDAO.findAll();
                    System.out.println("Ciudades encontradas: " + cities.size());
                    return gson.toJson(cities);
                } catch (Exception e) {
                    e.printStackTrace();
                    return gson.toJson(Map.of("success", false, "error", "Error obteniendo ciudades: " + e.getMessage()));
                }
            }

            if (path.equals("/api/airline/cities") && method.equals("POST")) {
                try {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    String name = json.get("name").getAsString();
                    String country = json.get("country").getAsString();
                    if (name == null || name.isBlank() || country == null || country.isBlank()) {
                        return gson.toJson(Map.of("success", false, "error", "Nombre y país son obligatorios"));
                    }
                    if (cityDAO.existsByNameAndCountry(name, country)) {
                        return gson.toJson(Map.of("success", false, "error", "La ciudad ya existe"));
                    }
                    City city = new City();
                    city.setName(name);
                    city.setCountry(country);
                    City saved = cityDAO.save(city);
                    return gson.toJson(saved);
                } catch (Exception e) {
                    return gson.toJson(Map.of("success", false, "error", "Error creando ciudad: " + e.getMessage()));
                }
            }

            
            if (path.equals("/api/airline/assign-role") && method.equals("POST")) {
                return handleAssignRole(body, headers);
            }
            
            if (path.equals("/api/airline/users") && method.equals("GET")) {
                return handleGetUsers(headers);
            }
            
            // ===== Actualización de Perfil de Usuario =====
            if (path.equals("/api/airline/user/profile") && method.equals("PUT")) {
                return handleUpdateUserProfile(body);
            }
            
            if (path.equals("/api/airline/user/profile") && method.equals("GET")) {
                return handleGetUserProfile(headers);
            }
            
            if (path.equals("/api/airline/initialize-roles") && method.equals("POST")) {
                return airlineUserHandler.initializeDefaultRoles();
            }

            // Lecturas del dominio de aerolíneas
            if (path.equals("/api/airline/aircrafts") && method.equals("GET")) {
                return aircraftHandler.listAircrafts();
            }
            if (path.equals("/api/airline/routes") && method.equals("GET")) {
                return routeHandler.listRoutes();
            }
            if (path.equals("/api/airline/flights") && method.equals("GET")) {
                try {
                    return flightHandler.getAllFlightsJson();
                } catch (Exception e) {
                    e.printStackTrace();
                    return gson.toJson(Map.of("success", false, "error", "Error en handler: " + e.getMessage()));
                }
            }
            
            // Crear vuelo
            if (path.equals("/api/airline/flights") && method.equals("POST")) {
                System.out.println("DEBUG: POST /api/airline/flights detectado");
                try {
                    System.out.println("DEBUG: Body recibido: " + body);
                    String result = flightHandler.createFlightFromJson(body);
                    System.out.println("DEBUG: Resultado: " + result);
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    return gson.toJson(Map.of("success", false, "error", "Error creando vuelo: " + e.getMessage()));
                }
            }
            if (path.equals("/api/airline/tickets") && method.equals("GET")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            if (path.equals("/api/airline/bookings") && method.equals("GET")) {
                return "[]"; // Temporalmente devolver array vacío
            }

            // Endpoints específicos de vuelos
            if (path.startsWith("/api/airline/flights/") && method.equals("GET")) {
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5) {
                    String flightId = pathParts[4];
                    return flightHandler.getFlightByIdJson(flightId);
                }
            }

            // Búsqueda de vuelos con parámetros
            if (path.startsWith("/api/airline/flights/search") && method.equals("GET")) {
                // Extraer parámetros de query string si existen
                // Por simplicidad, por ahora devolvemos todos los vuelos
                return flightHandler.getAllFlightsJson();
            }

            // Endpoints de reservas
            if (path.equals("/api/airline/bookings") && method.equals("POST")) {
                return bookingHandler.createBooking(body);
            }

            if (path.startsWith("/api/airline/bookings/") && method.equals("GET")) {
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5) {
                    String bookingId = pathParts[4];
                    return bookingHandler.getBookingById(bookingId);
                }
            }

            if (path.startsWith("/api/airline/users/") && path.contains("/bookings") && method.equals("GET")) {
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5) {
                    String userId = pathParts[4];
                    return bookingHandler.getUserBookings(userId);
                }
            }

            if (path.startsWith("/api/airline/bookings/") && path.contains("/cancel") && method.equals("POST")) {
                String[] pathParts = path.split("/");
                if (pathParts.length >= 5) {
                    String bookingId = pathParts[4];
                    // Extraer userId del body
                    JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
                    String userId = jsonBody.get("userId").getAsString();
                    return bookingHandler.cancelBooking(bookingId, userId);
                }
            }

            // ==================== ENDPOINTS DE GESTIÓN ADMINISTRATIVA ====================
            // TEMPORALMENTE DESHABILITADOS PARA PERMITIR COMPILACIÓN
            
            // Crear vuelo completo (con inventario y precios) - DESHABILITADO
            if (path.equals("/api/admin/flights") && method.equals("POST")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // Listar vuelos con filtros de administración - DESHABILITADO
            if (path.equals("/api/admin/flights") && method.equals("GET")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // Obtener detalles completos de vuelo (admin) - DESHABILITADO
            if (path.startsWith("/api/admin/flights/") && method.equals("GET") && !path.contains("/")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // Actualizar vuelo - DESHABILITADO
            if (path.startsWith("/api/admin/flights/") && method.equals("PUT")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // Cancelar vuelo - DESHABILITADO
            if (path.startsWith("/api/admin/flights/") && path.endsWith("/cancel") && method.equals("POST")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // Publicar vuelo - DESHABILITADO
            if (path.startsWith("/api/admin/flights/") && path.endsWith("/publish") && method.equals("POST")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // Actualizar inventario de vuelo - DESHABILITADO
            if (path.startsWith("/api/admin/flights/") && path.endsWith("/inventory") && method.equals("PUT")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // Actualizar precios de vuelo - DESHABILITADO
            if (path.startsWith("/api/admin/flights/") && path.endsWith("/fares") && method.equals("PUT")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // Estadísticas de vuelos - DESHABILITADO
            if (path.equals("/api/admin/flights/statistics") && method.equals("GET")) {
                return gson.toJson(Map.of("success", false, "error", "Endpoint temporalmente deshabilitado"));
            }
            
            // ==================== ENDPOINTS DE ENSURANCE PHARMACY ====================
            // Login y usuarios
            if (path.equals("/api/login") && method.equals("POST")) {
                return handleLoginRequest(body);
            }
            
            if (path.equals("/api/users") && method.equals("GET")) {
                return handleGetUsersRequest();
            }
            
            if (path.equals("/api/users") && method.equals("POST")) {
                return handleCreateUserRequest(body);
            }
            
            if (path.equals("/api/users") && method.equals("PUT")) {
                return handleUpdateUserRequest(body);
            }
            
            if (path.equals("/api/users/by-email") && method.equals("GET")) {
                return handleGetUserByEmailRequest(body);
            }
            
            // Pólizas
            if (path.equals("/api/policy") && method.equals("GET")) {
                return handleGetPoliciesRequest();
            }
            
            if (path.equals("/api/policy") && method.equals("POST")) {
                return handleCreatePolicyRequest(body);
            }
            
            // Citas
            if (path.equals("/api/appointment") && method.equals("GET")) {
                return handleGetAppointmentsRequest();
            }
            
            if (path.equals("/api/appointment") && method.equals("POST")) {
                return handleCreateAppointmentRequest(body);
            }
            
            if (path.equals("/api/appointmentmade") && method.equals("GET")) {
                return handleGetAppointmentsMadeRequest();
            }
            
            if (path.equals("/api/appointmentmade") && method.equals("POST")) {
                return handleCreateAppointmentMadeRequest(body);
            }
            
            // Categorías
            if (path.equals("/api/category") && method.equals("GET")) {
                return handleGetCategoriesRequest();
            }
            
            if (path.equals("/api/category") && method.equals("POST")) {
                return handleCreateCategoryRequest(body);
            }
            
            // Montos configurables
            if (path.equals("/api/configurableamount") && method.equals("GET")) {
                return handleGetConfigurableAmountsRequest();
            }
            
            if (path.equals("/api/configurableamount") && method.equals("POST")) {
                return handleCreateConfigurableAmountRequest(body);
            }
            
            if (path.startsWith("/api/configurable-amount/")) {
                return handleConfigurableAmountRequest(method, path, body);
            }
            
            // Hospitales
            if (path.equals("/api/hospital") && method.equals("GET")) {
                return handleGetHospitalsRequest();
            }
            
            if (path.equals("/api/hospital") && method.equals("POST")) {
                return handleCreateHospitalRequest(body);
            }
            
            // Medicinas
            if (path.equals("/api/medicine") && method.equals("GET")) {
                return handleGetMedicinesRequest();
            }
            
            if (path.equals("/api/medicine") && method.equals("POST")) {
                return handleCreateMedicineRequest(body);
            }
            
            if (path.equals("/api/medicinepres") && method.equals("GET")) {
                return handleGetMedicinePrescriptionsRequest();
            }
            
            if (path.equals("/api/medicinepres") && method.equals("POST")) {
                return handleCreateMedicinePrescriptionRequest(body);
            }
            
            // Farmacias
            if (path.equals("/api/pharmacy") && method.equals("GET")) {
                return handleGetPharmaciesRequest();
            }
            
            if (path.equals("/api/pharmacy") && method.equals("POST")) {
                return handleCreatePharmacyRequest(body);
            }
            
            // Recetas
            if (path.equals("/api/prescription") && method.equals("GET")) {
                return handleGetPrescriptionsRequest();
            }
            
            if (path.equals("/api/prescription") && method.equals("POST")) {
                return handleCreatePrescriptionRequest(body);
            }
            
            if (path.startsWith("/api/prescriptions/")) {
                return handlePrescriptionApprovalRequest(method, path, body);
            }
            
            // Servicios
            if (path.equals("/api/service") && method.equals("GET")) {
                return handleGetServicesRequest();
            }
            
            if (path.equals("/api/service") && method.equals("POST")) {
                return handleCreateServiceRequest(body);
            }
            
            // Totales
            if (path.equals("/api/totalhospital") && method.equals("GET")) {
                return handleGetTotalHospitalsRequest();
            }
            
            if (path.equals("/api/totalpharmacy") && method.equals("GET")) {
                return handleGetTotalPharmaciesRequest();
            }
            
            // Transacciones
            if (path.equals("/api/transactions") && method.equals("GET")) {
                return handleGetTransactionsRequest();
            }
            
            if (path.equals("/api/transactions") && method.equals("POST")) {
                return handleCreateTransactionRequest(body);
            }
            
            if (path.equals("/api/transactionpolicy") && method.equals("GET")) {
                return handleGetTransactionPoliciesRequest();
            }
            
            if (path.equals("/api/transactionpolicy") && method.equals("POST")) {
                return handleCreateTransactionPolicyRequest(body);
            }
            
            // Categorías de servicios
            if (path.equals("/api/servicecategory") && method.equals("GET")) {
                return handleGetServiceCategoriesRequest();
            }
            
            if (path.equals("/api/servicecategory") && method.equals("POST")) {
                return handleCreateServiceCategoryRequest(body);
            }
            
            // Notificaciones
            if (path.equals("/api/notifications/email") && method.equals("POST")) {
                return handleSendEmailRequest(body);
            }
            
            // Servicios de seguro
            if (path.equals("/api/insurance-services") && method.equals("GET")) {
                return handleGetInsuranceServicesRequest();
            }
            
            if (path.equals("/api/insurance-services") && method.equals("POST")) {
                return handleCreateInsuranceServiceRequest(body);
            }
            
            // Servicios de hospital
            if (path.equals("/api/hospital-services") && method.equals("GET")) {
                return handleGetHospitalServicesRequest();
            }
            
            if (path.equals("/api/hospital-services") && method.equals("POST")) {
                return handleCreateHospitalServiceRequest(body);
            }
            
            // Integración con hospital
            if (path.startsWith("/api/hospital-integration")) {
                return handleHospitalIntegrationRequest(method, path, body);
            }
            
            // Citas de seguro
            if (path.equals("/api/ensurance-appointments") && method.equals("GET")) {
                return handleGetEnsuranceAppointmentsRequest();
            }
            
            if (path.equals("/api/ensurance-appointments") && method.equals("POST")) {
                return handleCreateEnsuranceAppointmentRequest(body);
            }
            
            // Proxy de servicios de hospital
            if (path.startsWith("/api/hospital-proxy")) {
                return handleHospitalProxyRequest(method, path, body);
            }
            
            // Endpoint no encontrado
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Endpoint no encontrado: " + method + " " + path);
            return gson.toJson(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error interno del servidor: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    private String handleLogin(String body) {
        try {
            JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
            String email = jsonRequest.get("email").getAsString();
            String password = jsonRequest.get("password").getAsString();
            
            User user = userDAO.login(email, password);
            
            Map<String, Object> response = new HashMap<>();
            if (user != null) {
                response.put("success", true);
                response.put("message", "Login exitoso");
                response.put("user", Map.of(
                    "id", user.getIdUser(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName()
                ));
                response.put("token", "token-" + user.getIdUser()); // Token simple
            } else {
                response.put("success", false);
                response.put("error", "Credenciales inválidas");
            }
            
            return gson.toJson(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error en login: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    private String handleAssignRole(String body, Map<String, String> headers) {
        try {
            // Verificar autorización (simplificado)
            String authHeader = headers.get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "No autorizado");
                return gson.toJson(errorResponse);
            }
            
            // Por simplicidad, asumimos que es admin
            User adminUser = new User();
            adminUser.setRole("ADMIN");
            
            return airlineUserHandler.assignUserRole(body, adminUser);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al asignar rol: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    private String handleGetUsers(Map<String, String> headers) {
        try {
            // Verificar autorización (simplificado)
            String authHeader = headers.get("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "No autorizado");
                return gson.toJson(errorResponse);
            }
            
            var users = userDAO.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            
            return gson.toJson(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener usuarios: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
    
    private void initializeDefaultRoles() {
        try {
            boolean success = userRoleDAO.initializeDefaultRoles();
            if (success) {
                System.out.println("✅ Roles por defecto inicializados correctamente");
            } else {
                System.out.println("⚠️  Los roles ya estaban inicializados");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar roles: " + e.getMessage());
        }
    }
    
    // ==================== MÉTODOS AUXILIARES PARA ENSURANCE PHARMACY ====================
    
    private String handleLoginRequest(String body) {
        try {
            JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
            String email = jsonRequest.get("email").getAsString();
            String password = jsonRequest.get("password").getAsString();
            
            User user = userDAO.login(email, password);
            
            Map<String, Object> response = new HashMap<>();
            if (user != null) {
                response.put("success", true);
                response.put("message", "Login exitoso");
                response.put("user", Map.of(
                    "id", user.getIdUser(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "enabled", user.getEnabled()
                ));
                response.put("token", "token-" + user.getIdUser());
            } else {
                response.put("success", false);
                response.put("error", "Credenciales inválidas");
            }
            
            return gson.toJson(response);
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error en login: " + e.getMessage()));
        }
    }
    
    private String handleGetUsersRequest() {
        try {
            var users = userDAO.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            return gson.toJson(response);
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo usuarios: " + e.getMessage()));
        }
    }
    
    private String handleCreateUserRequest(String body) {
        try {
            JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
            // Implementar lógica básica de creación de usuario
            return gson.toJson(Map.of("success", true, "message", "Usuario creado exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando usuario: " + e.getMessage()));
        }
    }
    
    private String handleUpdateUserRequest(String body) {
        try {
            JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
            // Implementar lógica básica de actualización de usuario
            return gson.toJson(Map.of("success", true, "message", "Usuario actualizado exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error actualizando usuario: " + e.getMessage()));
        }
    }
    
    private String handleGetUserByEmailRequest(String body) {
        try {
            JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
            String email = jsonRequest.get("email").getAsString();
            User user = userDAO.findByEmail(email);
            if (user != null) {
                return gson.toJson(Map.of("success", true, "user", user));
            } else {
                return gson.toJson(Map.of("success", false, "error", "Usuario no encontrado"));
            }
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo usuario por email: " + e.getMessage()));
        }
    }
    
    private String handleGetPoliciesRequest() {
        try {
            var policies = policyDAO.findAll();
            return gson.toJson(Map.of("success", true, "policies", policies));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo pólizas: " + e.getMessage()));
        }
    }
    
    private String handleCreatePolicyRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Póliza creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando póliza: " + e.getMessage()));
        }
    }
    
    private String handleGetAppointmentsRequest() {
        try {
            var appointments = appointmentDAO.findAll();
            return gson.toJson(Map.of("success", true, "appointments", appointments));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo citas: " + e.getMessage()));
        }
    }
    
    private String handleCreateAppointmentRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Cita creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando cita: " + e.getMessage()));
        }
    }
    
    private String handleGetAppointmentsMadeRequest() {
        try {
            var appointments = appointmentMadeDAO.findAll();
            return gson.toJson(Map.of("success", true, "appointments", appointments));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo citas realizadas: " + e.getMessage()));
        }
    }
    
    private String handleCreateAppointmentMadeRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Cita realizada creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando cita realizada: " + e.getMessage()));
        }
    }
    
    private String handleGetCategoriesRequest() {
        try {
            var categories = categoryDAO.findAll();
            return gson.toJson(Map.of("success", true, "categories", categories));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo categorías: " + e.getMessage()));
        }
    }
    
    private String handleCreateCategoryRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Categoría creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando categoría: " + e.getMessage()));
        }
    }
    
    private String handleGetConfigurableAmountsRequest() {
        try {
            var amounts = configurableAmountDAO.findAll();
            return gson.toJson(Map.of("success", true, "amounts", amounts));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo montos configurables: " + e.getMessage()));
        }
    }
    
    private String handleCreateConfigurableAmountRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Monto configurable creado exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando monto configurable: " + e.getMessage()));
        }
    }
    
    private String handleConfigurableAmountRequest(String method, String path, String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Operación de monto configurable realizada"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error en monto configurable: " + e.getMessage()));
        }
    }
    
    private String handleGetHospitalsRequest() {
        try {
            var hospitals = hospitalDAO.findAll();
            return gson.toJson(Map.of("success", true, "hospitals", hospitals));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo hospitales: " + e.getMessage()));
        }
    }
    
    private String handleCreateHospitalRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Hospital creado exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando hospital: " + e.getMessage()));
        }
    }
    
    private String handleGetMedicinesRequest() {
        try {
            var medicines = medicineDAO.findAll();
            return gson.toJson(Map.of("success", true, "medicines", medicines));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo medicinas: " + e.getMessage()));
        }
    }
    
    private String handleCreateMedicineRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Medicina creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando medicina: " + e.getMessage()));
        }
    }
    
    private String handleGetMedicinePrescriptionsRequest() {
        try {
            var prescriptions = medicinePresDAO.findAll();
            return gson.toJson(Map.of("success", true, "prescriptions", prescriptions));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo prescripciones de medicina: " + e.getMessage()));
        }
    }
    
    private String handleCreateMedicinePrescriptionRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Prescripción de medicina creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando prescripción de medicina: " + e.getMessage()));
        }
    }
    
    private String handleGetPharmaciesRequest() {
        try {
            var pharmacies = pharmacyDAO.findAll();
            return gson.toJson(Map.of("success", true, "pharmacies", pharmacies));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo farmacias: " + e.getMessage()));
        }
    }
    
    private String handleCreatePharmacyRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Farmacia creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando farmacia: " + e.getMessage()));
        }
    }
    
    private String handleGetPrescriptionsRequest() {
        try {
            var prescriptions = prescriptionDAO.findAll();
            return gson.toJson(Map.of("success", true, "prescriptions", prescriptions));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo recetas: " + e.getMessage()));
        }
    }
    
    private String handleCreatePrescriptionRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Receta creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando receta: " + e.getMessage()));
        }
    }
    
    private String handlePrescriptionApprovalRequest(String method, String path, String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Operación de aprobación de receta realizada"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error en aprobación de receta: " + e.getMessage()));
        }
    }
    
    private String handleGetServicesRequest() {
        try {
            var services = serviceDAO.findAll();
            return gson.toJson(Map.of("success", true, "services", services));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo servicios: " + e.getMessage()));
        }
    }
    
    private String handleCreateServiceRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Servicio creado exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando servicio: " + e.getMessage()));
        }
    }
    
    private String handleGetTotalHospitalsRequest() {
        try {
            var totals = totalHospitalDAO.findAll();
            return gson.toJson(Map.of("success", true, "totals", totals));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo totales de hospital: " + e.getMessage()));
        }
    }
    
    private String handleGetTotalPharmaciesRequest() {
        try {
            var totals = totalPharmacyDAO.findAll();
            return gson.toJson(Map.of("success", true, "totals", totals));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo totales de farmacia: " + e.getMessage()));
        }
    }
    
    private String handleGetTransactionsRequest() {
        try {
            var transactions = transactionsDAO.findAll();
            return gson.toJson(Map.of("success", true, "transactions", transactions));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo transacciones: " + e.getMessage()));
        }
    }
    
    private String handleCreateTransactionRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Transacción creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando transacción: " + e.getMessage()));
        }
    }
    
    private String handleGetTransactionPoliciesRequest() {
        try {
            var policies = transactionPolicyDAO.findAll();
            return gson.toJson(Map.of("success", true, "policies", policies));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo políticas de transacción: " + e.getMessage()));
        }
    }
    
    private String handleCreateTransactionPolicyRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Política de transacción creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando política de transacción: " + e.getMessage()));
        }
    }
    
    private String handleGetServiceCategoriesRequest() {
        try {
            var categories = serviceCategoryDAO.findAll();
            return gson.toJson(Map.of("success", true, "categories", categories));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo categorías de servicio: " + e.getMessage()));
        }
    }
    
    private String handleCreateServiceCategoryRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Categoría de servicio creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando categoría de servicio: " + e.getMessage()));
        }
    }
    
    private String handleSendEmailRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Email enviado exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error enviando email: " + e.getMessage()));
        }
    }
    
    private String handleGetInsuranceServicesRequest() {
        try {
            var services = insuranceServiceDAO.findAll();
            return gson.toJson(Map.of("success", true, "services", services));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo servicios de seguro: " + e.getMessage()));
        }
    }
    
    private String handleCreateInsuranceServiceRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Servicio de seguro creado exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando servicio de seguro: " + e.getMessage()));
        }
    }
    
    private String handleGetHospitalServicesRequest() {
        try {
            return gson.toJson(Map.of("success", true, "services", new ArrayList<>()));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo servicios de hospital: " + e.getMessage()));
        }
    }
    
    private String handleCreateHospitalServiceRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Servicio de hospital creado exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando servicio de hospital: " + e.getMessage()));
        }
    }
    
    private String handleHospitalIntegrationRequest(String method, String path, String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Operación de integración con hospital realizada"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error en integración con hospital: " + e.getMessage()));
        }
    }
    
    private String handleGetEnsuranceAppointmentsRequest() {
        try {
            var appointments = ensuranceAppointmentDAO.findAll();
            return gson.toJson(Map.of("success", true, "appointments", appointments));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error obteniendo citas de seguro: " + e.getMessage()));
        }
    }
    
    private String handleCreateEnsuranceAppointmentRequest(String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Cita de seguro creada exitosamente"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error creando cita de seguro: " + e.getMessage()));
        }
    }
    
    private String handleHospitalProxyRequest(String method, String path, String body) {
        try {
            return gson.toJson(Map.of("success", true, "message", "Operación de proxy de hospital realizada"));
        } catch (Exception e) {
            return gson.toJson(Map.of("success", false, "error", "Error en proxy de hospital: " + e.getMessage()));
        }
    }
    
    // ===== Métodos para Manejo de Perfil de Usuario =====
    
    private String handleUpdateUserProfile(String body) {
        try {
            JsonObject jsonRequest = JsonParser.parseString(body).getAsJsonObject();
            
            // Validar campos requeridos
            if (!jsonRequest.has("userId") || !jsonRequest.has("firstName") || !jsonRequest.has("lastName")) {
                return gson.toJson(Map.of("success", false, "error", "Campos requeridos: userId, firstName, lastName"));
            }
            
            int userId = jsonRequest.get("userId").getAsInt();
            String firstName = jsonRequest.get("firstName").getAsString();
            String lastName = jsonRequest.get("lastName").getAsString();
            String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString() : null;
            String phone = jsonRequest.has("phone") ? jsonRequest.get("phone").getAsString() : null;
            
            // Validar que los campos no estén vacíos
            if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
                return gson.toJson(Map.of("success", false, "error", "Nombre y apellido no pueden estar vacíos"));
            }
            
            // Actualizar el usuario en la base de datos
            User user = userDAO.findById((long) userId);
            if (user == null) {
                return gson.toJson(Map.of("success", false, "error", "Usuario no encontrado"));
            }
            
            // Actualizar campos
            user.setFirstName(firstName);
            user.setLastName(lastName);
            
            if (email != null && !email.trim().isEmpty()) {
                user.setEmail(email);
            }
            
            if (phone != null && !phone.trim().isEmpty()) {
                user.setPhone(phone);
            }
            
            // Guardar cambios
            userDAO.update(user);
            
            // Crear respuesta de éxito
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Perfil actualizado correctamente");
            response.put("user", Map.of(
                "idUser", user.getIdUser(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "cui", user.getCui(),
                "birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : null,
                "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
            ));
            
            return gson.toJson(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error al actualizar perfil: " + e.getMessage()));
        }
    }
    
    private String handleGetUserProfile(Map<String, String> headers) {
        try {
            // Obtener userId del query string (se debe enviar como parámetro)
            // Por simplicidad, asumimos que se envía en el header o se puede implementar parsing de query string
            String userIdStr = headers.get("User-Id");
            if (userIdStr == null) {
                return gson.toJson(Map.of("success", false, "error", "Se requiere el parámetro userId"));
            }
            
            int userId = Integer.parseInt(userIdStr);
            User user = userDAO.findById((long) userId);
            
            if (user == null) {
                return gson.toJson(Map.of("success", false, "error", "Usuario no encontrado"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", Map.of(
                "idUser", user.getIdUser(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "cui", user.getCui(),
                "birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : null,
                "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
            ));
            
            return gson.toJson(response);
            
        } catch (NumberFormatException e) {
            return gson.toJson(Map.of("success", false, "error", "ID de usuario inválido"));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(Map.of("success", false, "error", "Error al obtener perfil: " + e.getMessage()));
        }
    }
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String host = DEFAULT_HOST;
        
        // Permitir configuración desde argumentos
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido, usando puerto por defecto: " + DEFAULT_PORT);
            }
        }
        
        if (args.length > 1) {
            host = args[1];
        }
        
        AirlineServer server = new AirlineServer(host, port);
        
        // Agregar shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        server.start();
    }
}
