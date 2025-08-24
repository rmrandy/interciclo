package com.sources.app.handlers;

import com.sources.app.dao.UserDAO;
import com.sources.app.dao.UserRoleDAO;
import com.sources.app.entities.User;
import com.sources.app.entities.UserRole;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler para gestionar usuarios del sistema de aerolíneas.
 * Incluye funcionalidades de registro con captcha y gestión de roles.
 */
public class AirlineUserHandler {

    private UserDAO userDAO;
    private UserRoleDAO userRoleDAO;
    private Gson gson;

    public AirlineUserHandler() {
        this.userDAO = new UserDAO();
        this.userRoleDAO = new UserRoleDAO();
        this.gson = new Gson();
    }

    /**
     * Registra un nuevo usuario visitante con validación de captcha.
     * 
     * @param requestBody JSON con los datos del usuario y captcha
     * @return Respuesta JSON con el resultado del registro
     */
    public String registerVisitor(String requestBody) {
        try {
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            
            // Validar captcha
            String captchaToken = jsonRequest.get("captchaToken").getAsString();
            if (!validateCaptcha(captchaToken)) {
                return createErrorResponse("Captcha inválido. Por favor, inténtelo de nuevo.");
            }

            // Extraer datos del usuario
            String firstName = jsonRequest.get("firstName").getAsString();
            String lastName = jsonRequest.get("lastName").getAsString();
            String email = jsonRequest.get("email").getAsString();
            String password = jsonRequest.get("password").getAsString();
            Integer age = jsonRequest.get("age").getAsInt();
            String country = jsonRequest.get("country").getAsString();
            String passportNumber = jsonRequest.get("passportNumber").getAsString();
            String phone = jsonRequest.has("phone") ? jsonRequest.get("phone").getAsString() : "";
            String address = jsonRequest.has("address") ? jsonRequest.get("address").getAsString() : "";

            // Validaciones básicas
            if (!isValidEmail(email)) {
                return createErrorResponse("Formato de email inválido.");
            }

            if (password.length() < 8) {
                return createErrorResponse("La contraseña debe tener al menos 8 caracteres.");
            }

            if (age < 18) {
                return createErrorResponse("Debe ser mayor de 18 años para registrarse.");
            }

            if (passportNumber.length() < 5) {
                return createErrorResponse("Número de pasaporte inválido.");
            }

            // Generar CUI único (simulado)
            Long cui = generateUniqueCUI();
            
            // Crear nombre completo
            String fullName = firstName + " " + lastName;
            
            // Crear usuario
            User newUser = userDAO.createAirlineUser(
                fullName, cui, firstName, lastName, age, country, passportNumber,
                phone, email, address, new Date(), password, "REGISTERED_VISITOR"
            );

            if (newUser != null) {
                Map<String, Object> response = new HashMap<>();
                
                // Verificar si es el primer usuario (ADMIN)
                boolean isFirstUser = "ADMIN".equals(newUser.getRole());
                
                if (isFirstUser) {
                    response.put("success", true);
                    response.put("message", "🎉 ¡FELICITACIONES! Eres el primer usuario registrado y has sido nombrado ADMIN del sistema");
                    response.put("isFirstUser", true);
                    response.put("adminMessage", "Como administrador, tendrás acceso completo a todas las funcionalidades del sistema");
                } else {
                    response.put("success", true);
                    response.put("message", "Usuario registrado exitosamente");
                    response.put("isFirstUser", false);
                }
                
                response.put("userId", newUser.getIdUser());
                response.put("email", newUser.getEmail());
                response.put("role", newUser.getRole());
                
                return gson.toJson(response);
            } else {
                return createErrorResponse("Error al crear el usuario. Verifique que el email y pasaporte no estén en uso.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Asigna un rol a un usuario (solo administradores).
     * 
     * @param requestBody JSON con userId y nuevo rol
     * @param adminUser Usuario administrador que realiza la acción
     * @return Respuesta JSON con el resultado
     */
    public String assignUserRole(String requestBody, User adminUser) {
        try {
            // Verificar que el usuario sea administrador
            if (!"ADMIN".equals(adminUser.getRole())) {
                return createErrorResponse("No tiene permisos para realizar esta acción.");
            }

            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();
            Long userId = jsonRequest.get("userId").getAsLong();
            String newRole = jsonRequest.get("role").getAsString();

            // Validar que el rol existe
            UserRole role = userRoleDAO.getRoleByName(newRole);
            if (role == null) {
                return createErrorResponse("Rol no válido: " + newRole);
            }

            // Buscar y actualizar usuario
            User user = userDAO.findById(userId);
            if (user == null) {
                return createErrorResponse("Usuario no encontrado.");
            }

            user.setRole(newRole);
            User updatedUser = userDAO.update(user);

            if (updatedUser != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Rol asignado exitosamente");
                response.put("userId", updatedUser.getIdUser());
                response.put("newRole", updatedUser.getRole());
                
                return gson.toJson(response);
            } else {
                return createErrorResponse("Error al actualizar el rol del usuario.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los roles disponibles.
     * 
     * @return Lista de roles en formato JSON
     */
    public String getAllRoles() {
        try {
            var roles = userRoleDAO.getAllActiveRoles();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("roles", roles);
            
            return gson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Error al obtener los roles: " + e.getMessage());
        }
    }

    /**
     * Inicializa los roles por defecto del sistema.
     * 
     * @return Respuesta JSON con el resultado
     */
    public String initializeDefaultRoles() {
        try {
            boolean success = userRoleDAO.initializeDefaultRoles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Roles inicializados correctamente" : "Error al inicializar roles");
            
            return gson.toJson(response);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Error al inicializar roles: " + e.getMessage());
        }
    }

    /**
     * Valida el captcha usando Google reCAPTCHA.
     * 
     * @param captchaToken Token del captcha
     * @return true si el captcha es válido, false en caso contrario
     */
    private boolean validateCaptcha(String captchaToken) {
        try {
            // URL de verificación de Google reCAPTCHA
            String url = "https://www.google.com/recaptcha/api/siteverify";
            String secretKey = System.getenv("RECAPTCHA_SECRET_KEY"); // Configurar en variables de entorno
            
            if (secretKey == null) {
                // Para desarrollo, aceptar cualquier token
                return true;
            }

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            // Parámetros de la petición
            String postData = "secret=" + secretKey + "&response=" + captchaToken;
            
            PrintWriter out = new PrintWriter(con.getOutputStream());
            out.print(postData);
            out.flush();

            // Leer respuesta
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parsear respuesta JSON
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            return jsonResponse.get("success").getAsBoolean();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Valida formato de email.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Genera un CUI único (simulado).
     */
    private Long generateUniqueCUI() {
        return System.currentTimeMillis() % 1000000000L;
    }

    /**
     * Crea una respuesta de error en formato JSON.
     */
    private String createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return gson.toJson(response);
    }
}
