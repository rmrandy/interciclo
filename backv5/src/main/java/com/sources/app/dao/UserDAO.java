package com.sources.app.dao;

import com.sources.app.entities.User;

import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Date;

/**
 * Data Access Object (DAO) para gestionar las entidades de Usuario (User).
 * Proporciona métodos para operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
 * y otras operaciones relacionadas con los usuarios, como inicio de sesión y validaciones.
 */
public class UserDAO {

    /**
     * Autentica a un usuario basado en su correo electrónico y contraseña.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return El objeto User si la autenticación es exitosa, null en caso contrario o si ocurre un error.
     */
    public User login(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email AND password = :password", User.class);
            query.setParameter("email", email);
            query.setParameter("password", password);
            User user = query.uniqueResult();
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifica si ya existe un usuario con el correo electrónico proporcionado.
     *
     * @param email El correo electrónico a verificar.
     * @return true si existe un usuario con ese email, false en caso contrario.
     */
    public boolean existsUserWithEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si ya existe un usuario con el CUI (Código Único de Identificación) proporcionado.
     *
     * @param cui El CUI a verificar.
     * @return true si existe un usuario con ese CUI, false en caso contrario.
     */
    public boolean existsUserWithCUI(Long cui) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.cui = :cui", Long.class);
            query.setParameter("cui", cui);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si ya existe un usuario con el número de pasaporte proporcionado.
     *
     * @param passportNumber El número de pasaporte a verificar.
     * @return true si existe un usuario con ese número de pasaporte, false en caso contrario.
     */
    public boolean existsUserWithPassport(String passportNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.passportNumber = :passportNumber", Long.class);
            query.setParameter("passportNumber", passportNumber);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crea un nuevo usuario con todos los campos requeridos para el sistema de aerolíneas.
     *
     * @param name Nombre completo del usuario.
     * @param cui CUI del usuario.
     * @param firstName Nombres del usuario.
     * @param lastName Apellidos del usuario.
     * @param age Edad del usuario.
     * @param country País de origen del usuario.
     * @param passportNumber Número de pasaporte del usuario.
     * @param phone Teléfono del usuario.
     * @param email Correo electrónico del usuario.
     * @param address Dirección del usuario.
     * @param birthDate Fecha de nacimiento del usuario.
     * @param password Contraseña del usuario.
     * @param role Rol del usuario (por defecto REGISTERED_VISITOR).
     * @return El objeto User creado, o null si ya existe un usuario con el mismo email/CUI/pasaporte o si ocurre un error.
     */
    /**
     * Verifica si es el primer usuario en el sistema
     * @return true si es el primer usuario, false en caso contrario
     */
    public boolean isFirstUser() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM User", Long.class);
            Long count = query.getSingleResult();
            return count == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User createAirlineUser(String name, Long cui, String firstName, String lastName, Integer age, 
                                 String country, String passportNumber, String phone, String email, 
                                 String address, Date birthDate, String password, String role) {
        
        if (existsUserWithEmail(email)) {
            System.out.println("ERROR: Ya existe un usuario con el email: " + email);
            return null;
        }
        
        if (existsUserWithCUI(cui)) {
            System.out.println("ERROR: Ya existe un usuario con el CUI: " + cui);
            return null;
        }

        if (existsUserWithPassport(passportNumber)) {
            System.out.println("ERROR: Ya existe un usuario con el número de pasaporte: " + passportNumber);
            return null;
        }
        
        Transaction tx = null;
        Session session = null;
        User user = null;
        
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            // Verificar si es el primer usuario
            boolean isFirstUser = isFirstUser();
            String finalRole = role;
            
            if (isFirstUser) {
                finalRole = "ADMIN";
                System.out.println("🎉 ¡PRIMER USUARIO REGISTRADO! Se le asigna rol de ADMIN");
            } else if (role == null || role.isEmpty()) {
                finalRole = "REGISTERED_VISITOR";
            }
            
            user = new User();
            user.setName(name);
            user.setCui(cui);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setAge(age);
            user.setCountry(country);
            user.setPassportNumber(passportNumber);
            user.setPhone(phone);
            user.setEmail(email);
            user.setAddress(address);
            user.setBirthDate(birthDate);
            user.setPassword(password);
            user.setRole(finalRole);
            user.setEnabled(1); // Usuario siempre activo por defecto
            user.setPaidService(0); // No ha pagado servicio por defecto
            
            session.save(user);
            tx.commit();
            
            if (isFirstUser) {
                System.out.println("👑 ADMIN creado exitosamente: " + user.getEmail() + " (Primer usuario del sistema)");
            } else {
                System.out.println("Usuario creado exitosamente: " + user.getEmail() + " (Rol: " + finalRole + ")");
            }
            return user;
            
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     * Verifica previamente si ya existe un usuario con el mismo email o CUI.
     *
     * @param name Nombre del usuario.
     * @param cui CUI del usuario.
     * @param phone Teléfono del usuario.
     * @param email Correo electrónico del usuario.
     * @param birthdate Fecha de nacimiento del usuario.
     * @param address Dirección del usuario.
     * @param password Contraseña del usuario.

     * @return El objeto User creado, o null si ya existe un usuario con el mismo email/CUI o si ocurre un error.
     */
    public User create(String name, Long cui, String phone, String email, Date birthdate, String address, String password) {
        if (existsUserWithEmail(email)) {
            System.out.println("ERROR: Ya existe un usuario con el email: " + email);
            return null;
        }
        
        if (existsUserWithCUI(cui)) {
            System.out.println("ERROR: Ya existe un usuario con el CUI: " + cui);
            return null;
        }
        
        Transaction tx = null;
        Session session = null;
        User user = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            user = new User();
            user.setName(name);
            user.setCui(cui);
            user.setPhone(phone);
            user.setEmail(email);
            user.setBirthDate(birthdate);
            user.setAddress(address);
            user.setPassword(password);
            user.setRole(" ");
            user.setEnabled(0);
            // Ya no usamos Policy
            
            // Valores por defecto para los nuevos campos
            user.setPaidService(null); // Inicialmente nulo, sin valor definido
            user.setExpirationDate(null); // Sin fecha de expiración

            session.save(user);

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return user;
    }

    /**
     * Recupera todos los usuarios de la base de datos.
     *
     * @return Una lista de todos los objetos User, o null si ocurre un error.
     */
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca un usuario por su ID único.
     *
     * @param idUser El ID del usuario a buscar.
     * @return El objeto User encontrado, o null si no se encuentra o si ocurre un error.
     */
    public User findById(Long idUser) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, idUser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza la información de un usuario existente en la base de datos.
     * También verifica y actualiza el estado de expiración del servicio pagado.
     *
     * @param user El objeto User con la información actualizada.
     * @return El objeto User actualizado, o null si el usuario no existe o si ocurre un error.
     */
    public User update(User user) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            User existingUser = session.get(User.class, user.getIdUser());
            if (existingUser == null) {
                return null;
            }

            existingUser.setName(user.getName());
            existingUser.setCui(user.getCui());
            existingUser.setPhone(user.getPhone());
            existingUser.setEmail(user.getEmail());
            existingUser.setAddress(user.getAddress());
            existingUser.setBirthDate(user.getBirthDate());
            existingUser.setRole(user.getRole());
            existingUser.setEnabled(user.getEnabled());
            existingUser.setPassword(user.getPassword());
            
            // Actualizar campos de servicio
            existingUser.setPaidService(user.getPaidService());
            existingUser.setExpirationDate(user.getExpirationDate());

            // Verificar si hay que limpiar la fecha de expiración
            if (user.getPaidService() != null && user.getPaidService().equals(0)) {
                existingUser.setExpirationDate(null); // Si no tiene servicio pagado, no tiene fecha de expiración
            }

            // Verificar expiración del servicio
            checkServiceExpiration(existingUser);

            // Ya no usamos Policy en el sistema de aerolínea

            session.update(existingUser);
            tx.commit();
            
            return existingUser;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * Verifica si el servicio del usuario ha expirado y actualiza su estado
     * si paidService es true y la expirationDate es anterior a la fecha actual.
     * Si expira, establece paidService a false y policy a null.
     * 
     * @param user Usuario a verificar
     */
    private void checkServiceExpiration(User user) {
        // Ignorar usuarios sin servicio definido o sin fecha de expiración
        if (user.getPaidService() == null || user.getExpirationDate() == null) {
            return;
        }
        
        // Solo verificar expiración si el servicio está pagado
        if (user.getPaidService() != null && user.getPaidService().equals(1)) {
            Date today = new Date();
            if (user.getExpirationDate().before(today)) {
                // El servicio ha expirado
                user.setPaidService(0);
                // Ya no usamos Policy
                System.out.println("Servicio expirado para el usuario: " + user.getEmail());
            }
        }
    }
    
    /**
     * Verifica y actualiza el estado de expiración del servicio para todos los usuarios.
     * Busca usuarios con `paidService = true` y `expirationDate` anterior a hoy,
     * y para ellos, establece `paidService = false` y `policy = null`.
     *
     * @return El número de usuarios cuyo estado de servicio fue actualizado a expirado.
     */
    public int checkAllUsersServiceExpiration() {
        int updatedCount = 0;
        Transaction tx = null;
        Session session = null;
        
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            // Buscar usuarios con servicio pagado y fecha de expiración anterior a hoy
            Date today = new Date();
            Query<User> query = session.createQuery(
                "FROM User WHERE paidService = 1 AND expirationDate < :today", 
                User.class
            );
            query.setParameter("today", today);
            List<User> expiredUsers = query.getResultList();
            
            for (User user : expiredUsers) {
                user.setPaidService(0);
                // Ya no usamos Policy
                session.update(user);
                updatedCount++;
            }
            
            tx.commit();
            System.out.println("Servicios expirados actualizados: " + updatedCount);
            return updatedCount;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return 0;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email El correo electrónico del usuario a buscar.
     * @return El objeto User encontrado, o null si no se encuentra o si ocurre un error.
     */
    public User findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca un usuario por su API Key (para usuarios empresariales).
     *
     * @param apiKey El API Key único del usuario empresarial.
     * @return El objeto User si se encuentra y es válido, null en caso contrario.
     */
    public User findByApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User WHERE apiKey = :apiKey AND isCorporate = 1 AND enabled = 1", 
                User.class
            );
            query.setParameter("apiKey", apiKey.trim());
            User user = query.uniqueResult();
            
            if (user != null) {
                System.out.println("✅ Usuario empresarial encontrado por API Key: " + user.getCompanyName());
            }
            
            return user;
        } catch (Exception e) {
            System.err.println("❌ Error buscando usuario por API Key: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca todos los usuarios empresariales activos.
     *
     * @return Lista de usuarios empresariales, lista vacía si no hay.
     */
    public List<User> findCorporateUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User WHERE isCorporate = 1 ORDER BY companyName", 
                User.class
            );
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Verifica si ya existe un usuario con el API Key proporcionado.
     */
    public boolean existsApiKey(String apiKey) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.apiKey = :apiKey", 
                Long.class
            );
            query.setParameter("apiKey", apiKey);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crea un nuevo usuario empresarial con API Key.
     */
    public User createCorporateUser(String companyName, String name, String email, 
                                    String phone, Long cui, String address, 
                                    String password, String apiKey, Date birthDate) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            User user = new User();
            user.setCompanyName(companyName);
            user.setName(name);
            
            // Dividir el nombre en firstName y lastName
            String[] nameParts = name.trim().split("\\s+", 2);
            if (nameParts.length >= 2) {
                user.setFirstName(nameParts[0]);
                user.setLastName(nameParts[1]);
            } else {
                user.setFirstName(name);
                user.setLastName(companyName);
            }
            
            user.setEmail(email);
            user.setPhone(phone);
            user.setCui(cui);
            user.setAddress(address);
            user.setPassword(password);
            user.setApiKey(apiKey);
            user.setBirthDate(birthDate);
            user.setIsCorporate(1);
            user.setRole("corporate");
            user.setEnabled(1);
            
            // Campos adicionales requeridos
            user.setAge(0);
            user.setCountry("N/A");
            user.setPassportNumber(String.valueOf(cui));
            user.setPaidService(null);
            user.setExpirationDate(null);

            session.persist(user);
            tx.commit();
            
            System.out.println("✅ Usuario empresarial creado: " + companyName + " (API Key: " + apiKey + ")");
            return user;
        } catch (Exception e) {
            if (tx != null) {
                try {
                    if (tx.getStatus().canRollback()) {
                        tx.rollback();
                    }
                } catch (Exception rollbackEx) {
                    System.err.println("⚠️ Error en rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("❌ Error creando usuario empresarial: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Actualiza un usuario empresarial existente.
     */
    public User updateCorporateUser(Long userId, String companyName, String name, 
                                    String email, String phone, Long cui, String address, 
                                    Integer enabled, String password, String apiKey, Date birthDate) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            User user = session.get(User.class, userId);
            if (user == null || user.getIsCorporate() != 1) {
                System.err.println("❌ Usuario empresarial no encontrado: " + userId);
                return null;
            }

            user.setCompanyName(companyName);
            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            user.setCui(cui);
            user.setAddress(address);
            user.setEnabled(enabled);
            user.setBirthDate(birthDate);

            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            }

            if (apiKey != null && !apiKey.trim().isEmpty()) {
                user.setApiKey(apiKey);
            }

            session.merge(user);
            tx.commit();
            
            System.out.println("✅ Usuario empresarial actualizado: " + companyName);
            return user;
        } catch (Exception e) {
            if (tx != null) {
                try {
                    if (tx.getStatus().canRollback()) {
                        tx.rollback();
                    }
                } catch (Exception rollbackEx) {
                    System.err.println("⚠️ Error en rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("❌ Error actualizando usuario empresarial: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Regenera el API Key de un usuario empresarial.
     */
    public boolean regenerateApiKey(Long userId, String newApiKey) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, userId);
            if (user == null || user.getIsCorporate() != 1) {
                System.err.println("❌ Usuario empresarial no encontrado: " + userId);
                return false;
            }

            user.setApiKey(newApiKey);
            session.merge(user);
            tx.commit();
            
            System.out.println("✅ API Key regenerado para: " + user.getCompanyName());
            return true;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            System.err.println("❌ Error regenerando API Key: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Alterna el estado (activo/inactivo) de un usuario empresarial.
     */
    public User toggleCorporateUserStatus(Long userId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, userId);
            if (user == null || user.getIsCorporate() != 1) {
                System.err.println("❌ Usuario empresarial no encontrado: " + userId);
                return null;
            }

            user.setEnabled(user.getEnabled() == 1 ? 0 : 1);
            session.merge(user);
            tx.commit();
            
            String status = user.getEnabled() == 1 ? "activado" : "desactivado";
            System.out.println("✅ Usuario empresarial " + status + ": " + user.getCompanyName());
            return user;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            System.err.println("❌ Error alternando estado: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

