# 🚀 **Panel de Usuario - API Documentation**

## 📋 **Resumen**

Este documento describe las APIs del Panel de Usuario Mejorado que permite gestionar perfiles, configuraciones, seguridad y actividad de los usuarios del sistema de aerolíneas.

## 🌐 **Endpoints Base**

- **URL Base**: `http://localhost:6060/api/user`
- **Puerto**: 6060
- **Protocolo**: HTTP/HTTPS

## 🔧 **Endpoints Disponibles**

### **1. Perfil de Usuario**

#### **GET /api/user/profile**
Obtiene el perfil completo del usuario.

**Request Body:**
```json
{
  "userId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Perfil obtenido exitosamente",
  "data": {
    "idUser": 1,
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan@email.com",
    "phone": "+502 1234 5678",
    "birthDate": "1990-05-15",
    "country": "Guatemala",
    "documentType": "CUI",
    "documentNumber": "1234567890123",
    "passportNumber": "A12345678",
    "preferences": {
      "preferredSeat": "WINDOW",
      "preferredMeal": "VEGETARIAN",
      "services": {
        "extraLegroom": false,
        "priorityBoarding": true,
        "luggageInsurance": false
      },
      "payment": {
        "saveCards": true,
        "saveBankAccounts": false
      }
    }
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

#### **POST /api/user/profile**
Actualiza el perfil del usuario.

**Request Body:**
```json
{
  "idUser": 1,
  "firstName": "Juan Carlos",
  "lastName": "Pérez González",
  "phone": "+502 9876 5432",
  "country": "Guatemala"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Perfil actualizado exitosamente",
  "data": {
    "idUser": 1,
    "firstName": "Juan Carlos",
    "lastName": "Pérez González",
    "email": "juan@email.com",
    "phone": "+502 9876 5432",
    "country": "Guatemala"
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

### **2. Configuraciones del Usuario**

#### **POST /api/user/settings**
Obtiene las configuraciones del usuario.

**Request Body:**
```json
{
  "userId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Configuraciones obtenidas exitosamente",
  "data": {
    "notifications": {
      "email": true,
      "push": false,
      "sms": false
    },
    "language": "es",
    "theme": "light",
    "privacy": {
      "shareProfile": false,
      "showActivity": true,
      "analytics": true
    },
    "timezone": "America/Guatemala_City",
    "dateFormat": "DD/MM/YYYY",
    "currency": "GTQ"
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

### **3. Seguridad del Usuario**

#### **POST /api/user/security**
Obtiene las configuraciones de seguridad.

**Request Body:**
```json
{
  "userId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Configuraciones de seguridad obtenidas exitosamente",
  "data": {
    "twoFactorAuth": false,
    "loginNotifications": true,
    "sessionTimeout": true
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

#### **POST /api/user/security (Cambio de Contraseña)**
Cambia la contraseña del usuario.

**Request Body:**
```json
{
  "userId": 1,
  "currentPassword": "password123",
  "newPassword": "newPassword456",
  "action": "changePassword"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Contraseña cambiada exitosamente",
  "data": null,
  "timestamp": "2025-01-27T10:30:00"
}
```

### **4. Historial de Actividad**

#### **POST /api/user/activity**
Obtiene el historial de actividad del usuario.

**Request Body:**
```json
{
  "userId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Historial de actividad obtenido exitosamente",
  "data": {
    "activities": [
      {
        "id": 1,
        "type": "flight_search",
        "description": "Búsqueda de vuelo: Guatemala → Miami",
        "timestamp": "2025-01-27T10:30:00",
        "details": {
          "origin": "Guatemala",
          "destination": "Miami",
          "date": "2025-08-25"
        }
      },
      {
        "id": 2,
        "type": "profile_update",
        "description": "Perfil actualizado",
        "timestamp": "2025-01-26T15:45:00",
        "details": {
          "field": "email",
          "oldValue": "old@email.com",
          "newValue": "new@email.com"
        }
      }
    ],
    "total": 2
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

### **5. Sesiones Activas**

#### **POST /api/user/sessions**
Obtiene las sesiones activas del usuario.

**Request Body:**
```json
{
  "userId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Sesiones activas obtenidas exitosamente",
  "data": {
    "sessions": [
      {
        "id": "1",
        "deviceType": "desktop",
        "deviceName": "Mi PC de Escritorio",
        "browser": "Chrome 120.0",
        "os": "Windows 11",
        "location": "Guatemala City, GT",
        "lastActivity": "2025-01-27T10:30:00"
      },
      {
        "id": "2",
        "deviceType": "mobile",
        "deviceName": "Mi iPhone",
        "browser": "Safari 17.0",
        "os": "iOS 17.0",
        "location": "Guatemala City, GT",
        "lastActivity": "2025-01-27T09:30:00"
      }
    ],
    "total": 2
  },
  "timestamp": "2025-01-27T10:30:00"
}
```

#### **POST /api/user/sessions (Terminar Sesión)**
Termina una sesión específica.

**Request Body:**
```json
{
  "userId": 1,
  "sessionId": "2",
  "action": "terminate"
}
```

#### **POST /api/user/sessions (Terminar Todas las Sesiones)**
Termina todas las sesiones del usuario.

**Request Body:**
```json
{
  "userId": 1,
  "action": "terminateAll"
}
```

### **6. Prueba de Conexión**

#### **GET /api/user/test**
Prueba la conectividad del servidor.

**Response:**
```json
{
  "message": "Panel de Usuario API funcionando correctamente",
  "status": "OK"
}
```

## 🔐 **Autenticación**

Actualmente las APIs no requieren autenticación específica, pero se recomienda implementar:

- **JWT Tokens**
- **API Keys**
- **OAuth 2.0**

## 📊 **Códigos de Estado HTTP**

- **200**: OK - Operación exitosa
- **400**: Bad Request - Datos incorrectos
- **404**: Not Found - Usuario no encontrado
- **405**: Method Not Allowed - Método HTTP no permitido
- **500**: Internal Server Error - Error interno del servidor

## 🚀 **Ejecutar el Servidor**

### **1. Compilar el Proyecto**
```bash
cd backv4
mvn clean compile
```

### **2. Ejecutar el Servidor**
```bash
# Opción 1: Usando el script
./run_user_profile_server.sh

# Opción 2: Manualmente
mvn exec:java -Dexec.mainClass="com.sources.app.UserProfileServer" -Dexec.args="6060"
```

### **3. Verificar que esté Funcionando**
```bash
curl http://localhost:6060/api/user/test
```

## 🔧 **Configuración del Frontend**

### **1. Importar la API**
```typescript
import { userProfileApi, isServerAvailable } from '../utils/userProfileApi';
```

### **2. Usar la API**
```typescript
// Obtener perfil
const profile = await userProfileApi.getUserProfile(userId);

// Actualizar configuraciones
const settings = await userProfileApi.updateUserSettings(newSettings);

// Cambiar contraseña
await userProfileApi.changeUserPassword(userId, currentPass, newPass);
```

## 📱 **Ejemplos de Uso**

### **Frontend Vue.js**
```vue
<template>
  <div>
    <button @click="loadProfile">Cargar Perfil</button>
    <div v-if="profile">
      <h2>{{ profile.firstName }} {{ profile.lastName }}</h2>
      <p>{{ profile.email }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { userProfileApi } from '../utils/userProfileApi'

const profile = ref(null)

const loadProfile = async () => {
  try {
    profile.value = await userProfileApi.getUserProfile(1)
  } catch (error) {
    console.error('Error cargando perfil:', error)
  }
}
</script>
```

### **Postman/curl**
```bash
# Obtener perfil
curl -X POST http://localhost:6060/api/user/profile \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'

# Obtener configuraciones
curl -X POST http://localhost:6060/api/user/settings \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'
```

## 🐛 **Solución de Problemas**

### **1. Servidor No Responde**
- Verificar que el puerto 6060 esté libre
- Revisar logs del servidor
- Verificar que Maven esté instalado

### **2. Errores de CORS**
- El servidor ya incluye headers CORS
- Verificar que el frontend esté en el puerto correcto

### **3. Errores de Base de Datos**
- Verificar conexión a la base de datos
- Revisar configuración de Hibernate
- Verificar que las tablas existan

## 🔮 **Próximas Mejoras**

1. **Autenticación JWT**
2. **Rate Limiting**
3. **Logging Avanzado**
4. **Métricas y Monitoreo**
5. **Cache Redis**
6. **WebSockets para Notificaciones**

## 📞 **Soporte**

Para soporte técnico o reportar bugs, contactar al equipo de desarrollo.

---

**¡El Panel de Usuario está listo para usar! 🎉**
