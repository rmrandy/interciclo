package com.sources.app;

import com.sources.app.dao.*;
import com.sources.app.handlers.*;
import com.sources.app.util.HibernateUtil;
import com.sources.app.util.CorsFilter;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import org.hibernate.Session;
import java.net.InetAddress;
import java.net.*;
import java.util.Enumeration;
import java.util.Collections;

import java.net.InetSocketAddress;

/**
 * Clase principal de la aplicación para el backend de Ensurance Pharmacy.
 * Inicializa la conexión a la base de datos, los DAOs, el servidor HTTP y configura los endpoints de la API.
 * También incluye lógica para verificaciones periódicas como la expiración de servicios.
 */
public class App {
    private static final UserDAO userDAO = new UserDAO();
    private static final PolicyDAO policyDAO = new PolicyDAO();
    private static final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private static final AppointmentMadeDAO appointmentMadeDAO = new AppointmentMadeDAO();
    private static final CategoryDAO categoryDAO = new CategoryDAO();
    private static final ConfigurableAmountDAO configurableAmountDAO = new ConfigurableAmountDAO();
    private static final HospitalDAO hospitalDAO = new HospitalDAO();
    private static final MedicineDAO medicineDAO = new MedicineDAO();
    private static final MedicinePresDAO medicinePresDAO = new MedicinePresDAO();
    private static final PharmacyDAO pharmacyDAO = new PharmacyDAO();
    private static final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private static final ServiceDAO serviceDAO = new ServiceDAO();
    private static final TotalHospitalDAO totalHospitalDAO = new TotalHospitalDAO();
    private static final TotalPharmacyDAO totalPharmacyDAO = new TotalPharmacyDAO();
    private static final TransactionsDAO transactionsDAO = new TransactionsDAO();
    private static final TransactionPolicyDAO transactionPolicyDAO = new TransactionPolicyDAO();
    private static final ServiceCategoryDAO serviceCategoryDAO = new ServiceCategoryDAO();
    private static final InsuranceServiceDAO insuranceServiceDAO = new InsuranceServiceDAO();
    private static final HospitalInsuranceServiceDAO hospitalInsuranceServiceDAO = new HospitalInsuranceServiceDAO();
    private static final EnsuranceAppointmentDAO ensuranceAppointmentDAO = new EnsuranceAppointmentDAO();
    private static final PrescriptionApprovalDAO prescriptionApprovalDAO = new PrescriptionApprovalDAO();

    /**
     * Constructor privado para prevenir la instanciación de la clase de utilidad.
     */
    private App() {
        // Prevent instantiation
    }

    private static String getLocalExternalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Ignora interfaces inactivas o de loopback
                if (!iface.isUp() || iface.isLoopback()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Solo direcciones IPv4 que no sean loopback o de enlace local
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    /**
     * Crea un contexto HTTP con el filtro CORS aplicado.
     * Este método helper facilita la creación de contextos con CORS configurado.
     * 
     * @param server El servidor HTTP donde se creará el contexto
     * @param path La ruta del endpoint
     * @param handler El handler que procesará las solicitudes
     * @param corsFilter El filtro CORS a aplicar
     */
    private static void createContextWithCors(HttpServer server, String path, 
                                               com.sun.net.httpserver.HttpHandler handler, 
                                               CorsFilter corsFilter) {
        HttpContext context = server.createContext(path, handler);
        context.getFilters().add(corsFilter);
    }


    /**
     * El punto de entrada principal de la aplicación.
     * Inicializa el servidor, configura los manejadores de contexto para varios endpoints de la API,
     * realiza comprobaciones iniciales de la base de datos e inicia el servidor HTTP.
     * También programa una tarea diaria para verificar los servicios de usuario expirados.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     * @throws Exception Si hay un error al iniciar el servidor o conectarse a la base de datos.
     */
    public static void main(String[] args) throws Exception {
        // Prueba de conexión a la base de datos
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (session.isConnected()) {
                System.out.println("Conexión exitosa a la base de datos!");
            } else {
                System.err.println("No se pudo establecer conexión a la base de datos.");
            }
        } catch (Exception e) {
            System.err.println("Error al conectar con la base de datos:");
            e.printStackTrace();
        }

        String ip = getLocalExternalIp();
        // Leer puerto desde propiedad del sistema o usar 8080 por defecto
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        
        System.out.println("Iniciando servidor en puerto: " + port);

        // Verificar servicios expirados al iniciar
        System.out.println("Verificando servicios expirados...");
        int updatedUsers = userDAO.checkAllUsersServiceExpiration();
        System.out.println("Se actualizaron " + updatedUsers + " usuarios con servicios expirados.");
        
        // Programar tarea para verificar servicios expirados cada día
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                System.out.println("Ejecutando verificación programada de servicios expirados...");
                int count = userDAO.checkAllUsersServiceExpiration();
                System.out.println("Verificación programada: se actualizaron " + count + " usuarios con servicios expirados.");
            }
        }, 
        // Ejecutar cada 24 horas (en milisegundos)
        86400000, 86400000);

        // Crear y configurar el servidor HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        
        // Crear instancia única del filtro CORS
        CorsFilter corsFilter = new CorsFilter();
        
        // Crear contextos con filtro CORS aplicado
        createContextWithCors(server, "/api/login", new LoginHandler(userDAO), corsFilter);
        createContextWithCors(server, "/api/users", new UserHandler(userDAO), corsFilter);
        // Exponer endpoints de aerolínea bajo el mismo servidor
        createContextWithCors(server, "/api/airline", new AirlineHttpHandler(userDAO), corsFilter);
        createContextWithCors(server, "/api/airline/flights", new FlightHandler(), corsFilter);
        // Aeronaves
        createContextWithCors(server, "/api/airline/aircrafts", new AircraftHttpHandler(), corsFilter);
        createContextWithCors(server, "/api/airline/cities", new AirlineHttpHandler(userDAO), corsFilter);
        createContextWithCors(server, "/api/airline/seats", new SeatsHandler(), corsFilter);
        createContextWithCors(server, "/api/airline/inventory", new InventoryHandler(), corsFilter);
        // Reseñas de vuelos gestionadas dentro de FlightHandler (mismo contexto)
        createContextWithCors(server, "/api/airline/tickets", new TicketHandler(), corsFilter);
        createContextWithCors(server, "/api/policy", new PolicyHandler(policyDAO), corsFilter);
        createContextWithCors(server, "/api/appointment", new AppointmentHandler(appointmentDAO), corsFilter);
        createContextWithCors(server, "/api/appointmentmade", new AppointmentMadeHandler(appointmentMadeDAO), corsFilter);
        createContextWithCors(server, "/api/category", new CategoryHandler(categoryDAO), corsFilter);
        createContextWithCors(server, "/api/configurableamount", new ConfigurableAmountHandler(configurableAmountDAO), corsFilter);
        // Deshabilitado temporalmente por problemas de compilación del handler
        // createContextWithCors(server, "/api/hospital", new HospitalHandler(hospitalDAO), corsFilter);
        createContextWithCors(server, "/api/medicine", new MedicineHandler(medicineDAO), corsFilter);
        createContextWithCors(server, "/api/medicinepres", new MedicinePresHandler(medicinePresDAO), corsFilter);
        createContextWithCors(server, "/api/pharmacy", new PharmacyHandler(pharmacyDAO), corsFilter);
        createContextWithCors(server, "/api/prescription", new PrescriptionHandler(prescriptionDAO), corsFilter);
        createContextWithCors(server, "/api/service", new ServiceHandler(serviceDAO), corsFilter);
        createContextWithCors(server, "/api/totalhospital", new TotalHospitalHandler(totalHospitalDAO), corsFilter);
        createContextWithCors(server, "/api/totalpharmacy", new TotalPharmacyHandler(totalPharmacyDAO), corsFilter);
        createContextWithCors(server, "/api/transactions", new TransactionsHandler(transactionsDAO), corsFilter);
        createContextWithCors(server, "/api/transactionpolicy", new TransactionPolicyHandler(transactionPolicyDAO), corsFilter);
        createContextWithCors(server, "/api/servicecategory", new ServiceCategoryHandler(serviceCategoryDAO), corsFilter);
        createContextWithCors(server, "/api/notifications/email", new NotificationHandler(), corsFilter);
        // Nuevos endpoints para servicios de seguro y relaciones con hospitales
        createContextWithCors(server, "/api/insurance-services", new InsuranceServiceHandler(insuranceServiceDAO, categoryDAO), corsFilter);
        createContextWithCors(server, "/api/hospital-services", new HospitalInsuranceServiceHandler(hospitalInsuranceServiceDAO, hospitalDAO, insuranceServiceDAO), corsFilter);
        // Nuevo handler para la integración con el hospital
        createContextWithCors(server, "/api/hospital-integration", new HospitalRedirectHandler(), corsFilter);
        // Registrar el nuevo handler para buscar usuarios por email
        createContextWithCors(server, "/api/users/by-email", new UserByEmailHandler(userDAO), corsFilter);
        // Nuevo handler para las citas de seguro
        createContextWithCors(server, "/api/ensurance-appointments", new EnsuranceAppointmentHandler(ensuranceAppointmentDAO), corsFilter);
        // Handler para aprobaciones de recetas
        createContextWithCors(server, "/api/prescriptions/", new PrescriptionApprovalHandler(prescriptionApprovalDAO, userDAO, configurableAmountDAO), corsFilter);
        // Actualizar handler para monto configurable
        createContextWithCors(server, "/api/configurable-amount/", new ConfigurableAmountHandler(configurableAmountDAO), corsFilter);
        // Registrar el nuevo handler para proxy de servicios de hospital
        createContextWithCors(server, "/api/hospital-proxy", new HospitalServiceProxyHandler(hospitalDAO), corsFilter);
        // Site settings (branding Header/Footer)
        createContextWithCors(server, "/api/site-settings", new SiteSettingsHandler(new SystemConfigDAO()), corsFilter);
        
        // Endpoints para perfil de usuario
        createContextWithCors(server, "/api/airline/user/profile", new UserProfileHandler(userDAO), corsFilter);
        createContextWithCors(server, "/api/health", new HealthHandler(), corsFilter);
        // Analytics: capturar y consultar clics
        createContextWithCors(server, "/api/analytics", new AnalyticsHandler(), corsFilter);
        // Alias alternativo para evitar bloqueos por adblockers
        createContextWithCors(server, "/api/metrics", new AnalyticsHandler(), corsFilter);
 
        server.setExecutor(null); // Usa el executor por defecto
        server.start();
        System.out.println("Servidor iniciado en http://" + ip + ":" + port + "/api");
    }
}
