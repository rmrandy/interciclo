# 🚀 API de Actualización de Perfil de Usuario

## 📋 Descripción
Sistema integrado en el servidor principal de aerolíneas para actualizar perfiles de usuario en tiempo real.

## 🌐 Configuración de Puertos e IPs

### **Puertos del Sistema:**
- **8080** - Sistema principal de aerolíneas (incluye actualización de perfil)
- **8082** - Sistema de farmacia

### **IP del Servidor:**
- **IP por defecto:** `192.168.0.26`
- **Configurable:** Usar variable de entorno `VITE_IP`

## 🚀 Cómo Ejecutar

### **1. Iniciar el Servidor Principal:**
```bash
cd backv4
mvn exec:java -Dexec.mainClass="com.sources.app.AirlineServer"
```

### **2. El Servidor se Iniciará en:**
- **URL:** `http://192.168.0.26:8080` (o tu IP configurada)
- **Puerto:** 8080
- **Endpoints:**
  - `PUT /api/airline/user/profile` - Actualizar perfil
  - `GET /api/airline/user/profile?userId=X` - Obtener perfil

## 📱 Funcionalidades del Frontend

### **Panel de Usuario:**
- ✅ **Editar Perfil** - Modificar nombre, apellido, email, teléfono
- ✅ **Configuraciones** - Notificaciones y tema
- ✅ **Seguridad** - Cambio de contraseña
- ✅ **Historial** - Actividad reciente

### **Integración con Backend:**
- 🔄 **Verificación automática** del estado del servidor
- 💾 **Persistencia en base de datos** cuando el servidor está disponible
- 📱 **Fallback a localStorage** cuando el servidor no está disponible
- 🎯 **Indicador visual** del estado de conexión

## 🔧 Configuración

### **Cambiar IP del Servidor:**
```bash
# Opción 1: Variable de entorno
export VITE_IP=192.168.0.100
./run_user_profile_update_server.sh

# Opción 2: Parámetro Java
mvn exec:java -Dexec.mainClass="com.sources.app.UserProfileUpdateServer" -Dserver.ip=192.168.0.100
```

### **Cambiar Puerto:**
El puerto se configura en el servidor principal `AirlineServer.java`:
```java
private static final int DEFAULT_PORT = 8080; // Cambiar aquí
```

## 📊 Estructura de Datos

### **Request de Actualización:**
```json
{
  "userId": 1,
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan@example.com",
  "phone": "12345678"
}
```

### **Response de Éxito:**
```json
{
  "success": true,
  "message": "Perfil actualizado correctamente",
  "user": {
    "idUser": 1,
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "phone": "12345678",
    "cui": 12345678,
    "birthDate": "1990-01-01",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

## 🚨 Solución de Problemas

### **Error de CORS:**
- ✅ **CORS no es necesario** - Todo está en el mismo sistema
- ✅ **Mismo puerto e IP** - Frontend y backend comparten origen
- ✅ **Sin preflight requests** - Comunicación directa

### **Servidor No Disponible:**
- 🔄 El frontend verifica automáticamente la conexión
- 📱 Los cambios se guardan localmente como fallback
- ✅ Se sincroniza cuando el servidor vuelve a estar disponible

### **Puerto en Uso:**
- 🔍 Verificar que el puerto 8083 esté libre
- 🚀 Usar `netstat -an | grep 8083` para verificar
- 🔄 Cambiar puerto en `ports.ts` si es necesario

## 📁 Archivos Principales

### **Backend:**
- `AirlineServer.java` - Servidor principal con endpoints de perfil integrados
- `UserDAO.java` - Acceso a datos de usuario

### **Frontend:**
- `userProfileUpdateApi.ts` - Cliente API
- `user-services.vue` - Panel de usuario
- `ports.ts` - Configuración de puertos

## 🎯 Estado del Proyecto
- ✅ **Backend implementado** integrado en el sistema principal
- ✅ **Frontend integrado** con fallback automático
- ✅ **Sin CORS** - Comunicación directa en el mismo sistema
- ✅ **Configuración centralizada** de puertos e IPs
- ✅ **Manejo de errores** robusto
- ✅ **Documentación completa** de uso
