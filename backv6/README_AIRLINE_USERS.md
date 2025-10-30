# 🛩️ Sistema de Usuarios AeroLinea

## 📋 Descripción General

Sistema completo de gestión de usuarios para una aerolínea con 4 categorías de usuarios, registro con captcha, y gestión de roles dinámica.

## 👥 Categorías de Usuarios

### 1. **Usuario Administrador/Empleado**
- Acceso completo al sistema
- Gestión de usuarios y roles
- Configuración del sistema

### 2. **Webservice**
- Integración con sistemas externos
- APIs para reservas y consultas

### 3. **Usuario Visitante Anónimo**
- Consultar vuelos disponibles
- Ver comentarios (solo hoteles y aerolíneas)
- Registrarse para crear cuenta

### 4. **Usuario Visitante Registrado**
- Todas las funcionalidades del anónimo
- Hacer reservaciones
- Dejar comentarios
- Acceso a servicios personalizados

## 🏗️ Arquitectura del Sistema

### Backend (Java + Hibernate)
```
backv4/
├── src/main/java/com/sources/app/
│   ├── entities/
│   │   ├── User.java (actualizado con nuevos campos)
│   │   └── UserRole.java (nueva entidad)
│   ├── dao/
│   │   ├── UserDAO.java (actualizado)
│   │   └── UserRoleDAO.java (nuevo)
│   ├── handlers/
│   │   └── AirlineUserHandler.java (nuevo)
│   └── AirlineServer.java (servidor HTTP)
├── src/main/resources/
│   └── hibernate.cfg.xml (actualizado)
├── db_update_airline_users.sql
└── run_airline_server.sh
```

### Frontend (Vue 3 + TypeScript)
```
ensurance/
├── src/
│   ├── pages/
│   │   └── register.vue (actualizado)
│   ├── utils/
│   │   └── airlineApi.ts (nuevo)
│   └── stores/
│       └── authStore.ts
```

## 🚀 Instalación y Configuración

### 1. Base de Datos
```sql
-- Ejecutar el script de actualización
@db_update_airline_users.sql
```

### 2. Backend
```bash
# Navegar al directorio del backend
cd backv4

# Compilar y ejecutar el servidor
./run_airline_server.sh [puerto] [host]

# Ejemplo:
./run_airline_server.sh 8080 0.0.0.0
```

### 3. Frontend
```bash
# Navegar al directorio del frontend
cd ensurance

# Instalar dependencias
npm install

# Ejecutar en modo desarrollo
npm run dev
```

## 📡 APIs Disponibles

### Endpoints del Sistema

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| `POST` | `/api/airline/register` | Registro de usuarios visitantes | No |
| `POST` | `/api/airline/login` | Login de usuarios | No |
| `GET` | `/api/airline/roles` | Obtener roles disponibles | No |
| `POST` | `/api/airline/assign-role` | Asignar rol a usuario | Admin |
| `GET` | `/api/airline/users` | Obtener lista de usuarios | Admin |
| `POST` | `/api/airline/initialize-roles` | Inicializar roles por defecto | No |
| `GET` | `/api/health` | Estado del servidor | No |

### Ejemplos de Uso

#### Registro de Usuario
```javascript
const response = await airlineApi.registerVisitor({
  firstName: "Juan",
  lastName: "Pérez",
  email: "juan@email.com",
  password: "password123",
  age: 25,
  country: "Guatemala",
  passportNumber: "A12345678",
  phone: "+502 1234-5678",
  address: "Ciudad de Guatemala",
  captchaToken: "token-del-captcha"
});
```

#### Login
```javascript
const response = await airlineApi.login({
  email: "juan@email.com",
  password: "password123"
});
```

#### Asignar Rol (Admin)
```javascript
const response = await airlineApi.assignUserRole(
  userId, 
  "EMPLOYEE", 
  adminToken
);
```

## 🔐 Sistema de Autenticación

### Captcha
- Implementado con Google reCAPTCHA
- En desarrollo: captcha simple interno
- Configuración: Variable de entorno `RECAPTCHA_SECRET_KEY`

### Roles y Permisos
```json
{
  "ADMIN": {
    "can_manage_users": true,
    "can_manage_roles": true,
    "can_view_reports": true,
    "can_manage_flights": true
  },
  "EMPLOYEE": {
    "can_manage_users": false,
    "can_manage_roles": false,
    "can_view_reports": true,
    "can_manage_flights": false
  },
  "WEBSERVICE": {
    "can_manage_users": false,
    "can_manage_roles": false,
    "can_view_reports": false,
    "can_manage_flights": true
  },
  "REGISTERED_VISITOR": {
    "can_manage_users": false,
    "can_manage_roles": false,
    "can_view_reports": false,
    "can_manage_flights": false,
    "can_make_reservations": true,
    "can_view_comments": true
  }
}
```

## 🎨 Frontend - Tema de Aerolínea

### Características del Diseño
- ✈️ Tema aeronáutico completo
- 🎨 Colores azules y dorados
- 📱 Diseño responsive
- 🔄 Animaciones suaves
- 🎯 UX optimizada

### Componentes Principales
- **Formulario de Registro**: Con captcha y validaciones
- **Sistema de Navegación**: Roles dinámicos
- **Gestión de Usuarios**: Panel administrativo
- **Configuración de API**: IP y puerto dinámicos

## 🔧 Configuración Dinámica

### API Configuration
```typescript
// Configurar IP y puerto
AirlineApiConfig.getInstance().updateConfig('192.168.1.100', '8080');

// Obtener configuración actual
const baseUrl = AirlineApiConfig.getInstance().getBaseUrl();
```

### Variables de Entorno
```bash
# Backend
RECAPTCHA_SECRET_KEY=your_recaptcha_secret

# Frontend
VITE_API_URL=http://localhost:8080
```

## 📊 Base de Datos

### Tabla USERS (Actualizada)
```sql
ALTER TABLE USERS ADD (
    FIRST_NAME VARCHAR2(100) NOT NULL,
    LAST_NAME VARCHAR2(100) NOT NULL,
    AGE NUMBER(3,0) NOT NULL,
    COUNTRY VARCHAR2(100) NOT NULL,
    PASSPORT_NUMBER VARCHAR2(50) NOT NULL
);
```

### Tabla USER_ROLES (Nueva)
```sql
CREATE TABLE USER_ROLES (
    ID_ROLE NUMBER(19,0) GENERATED BY DEFAULT AS IDENTITY,
    ROLE_NAME VARCHAR2(50) NOT NULL UNIQUE,
    DESCRIPTION VARCHAR2(255),
    PERMISSIONS VARCHAR2(1000),
    ENABLED NUMBER(10,0) NOT NULL,
    CREATED_AT TIMESTAMP(6) DEFAULT SYSTIMESTAMP,
    UPDATED_AT TIMESTAMP(6) DEFAULT SYSTIMESTAMP
);
```

## 🧪 Testing

### Pruebas del Backend
```bash
# Ejecutar tests
mvn test

# Tests específicos
mvn test -Dtest=AirlineUserHandlerTest
```

### Pruebas del Frontend
```bash
# Ejecutar tests
npm run test

# Tests de componentes
npm run test:unit
```

## 🚨 Validaciones

### Registro de Usuario
- ✅ Email único
- ✅ Pasaporte único
- ✅ Edad mínima 18 años
- ✅ Contraseña mínima 8 caracteres
- ✅ Captcha válido
- ✅ Términos y condiciones aceptados

### Login
- ✅ Credenciales válidas
- ✅ Usuario activo
- ✅ Rol asignado

## 🔄 Flujo de Usuario

### Usuario Anónimo
1. Visita el portal
2. Consulta vuelos disponibles
3. Ve comentarios
4. Se registra

### Usuario Registrado
1. Inicia sesión
2. Hace reservaciones
3. Deja comentarios
4. Accede a servicios personalizados

### Administrador
1. Inicia sesión como admin
2. Gestiona usuarios
3. Asigna roles
4. Configura el sistema

## 📝 Logs y Monitoreo

### Logs del Servidor
```
🚀 Servidor AeroLinea iniciado en 0.0.0.0:8080
📡 Endpoints disponibles:
   POST /api/airline/register - Registro de usuarios
   POST /api/airline/login - Login de usuarios
   GET  /api/airline/roles - Obtener roles
   POST /api/airline/assign-role - Asignar rol (admin)
   GET  /api/airline/users - Obtener usuarios (admin)
   POST /api/airline/initialize-roles - Inicializar roles
   GET  /api/health - Estado del servidor
✅ Roles por defecto inicializados correctamente
```

## 🔮 Próximas Funcionalidades

- [ ] Sistema de notificaciones por email
- [ ] Autenticación JWT completa
- [ ] Auditoría de acciones
- [ ] Dashboard de analytics
- [ ] Integración con sistemas de pago
- [ ] App móvil

## 🆘 Soporte

Para problemas o consultas:
1. Revisar logs del servidor
2. Verificar configuración de base de datos
3. Comprobar conectividad de red
4. Validar permisos de archivos

---

**Desarrollado para AeroLinea** ✈️
