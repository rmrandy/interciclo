# 👔 Guía de Gestión de Usuarios Empresariales

Esta guía explica cómo gestionar usuarios empresariales (agencias de viaje) con API Keys en el sistema de aerolínea.

## 🎯 ¿Qué es un Usuario Empresarial?

Un usuario empresarial es una **agencia de viaje** o **empresa** que puede:
- Comprar boletos de avión en nombre de sus clientes
- Usar API Keys para autenticación automática
- Acceder al sistema mediante integraciones API
- Gestionar múltiples reservas de diferentes pasajeros

## 🚀 Acceso a la Gestión

### Desde el Frontend (Aerolínea)

1. **Inicia sesión** como administrador en la aerolínea
2. Ve al menú de navegación
3. Busca **"Usuarios Empresariales"** o accede a: `http://localhost:5050/admin/corporate-users`

## ✨ Funcionalidades

### 📋 Listar Usuarios Empresariales

- **Endpoint:** `GET /api/corporate-users`
- **Descripción:** Muestra todos los usuarios empresariales registrados
- **Información mostrada:**
  - Nombre de la empresa
  - Contacto (nombre, email, teléfono)
  - API Key (enmascarado por seguridad)
  - Estado (Activo/Inactivo)
  - Fecha de registro

### ➕ Crear Usuario Empresarial

- **Endpoint:** `POST /api/corporate-users`
- **Descripción:** Crea una nueva agencia de viajes
- **Campos requeridos:**
  - 🏢 Nombre de la empresa
  - 👤 Nombre del contacto
  - 📧 Email
  - 📞 Teléfono
  - 🆔 CUI/NIT
  - 📍 Dirección
  - 🔒 Contraseña

- **Proceso:**
  1. Click en "Nuevo Usuario Empresarial"
  2. Completa el formulario
  3. El sistema **genera automáticamente** un API Key único de 32 caracteres
  4. Al crear, el API Key se copia automáticamente al portapapeles
  5. **IMPORTANTE:** Guarda el API Key, se necesita para las integraciones

### ✏️ Editar Usuario Empresarial

- **Endpoint:** `PUT /api/corporate-users/{id}`
- **Descripción:** Actualiza información de la agencia
- **Campos editables:**
  - Nombre de empresa
  - Datos de contacto
  - Contraseña (opcional)
  - Estado (activo/inactivo)

- **Nota:** El API Key **NO** se modifica aquí, usa la opción "Regenerar API Key"

### 🔑 Regenerar API Key

- **Endpoint:** `PUT /api/corporate-users/{id}/regenerate-key`
- **Descripción:** Genera un nuevo API Key para la agencia
- **Cuándo usar:**
  - El API Key fue comprometido
  - La agencia perdió su API Key
  - Rotación de seguridad periódica

- **⚠️ ADVERTENCIA:** Regenerar el API Key invalidará el anterior. Todas las integraciones deben actualizarse.

- **Proceso:**
  1. Click en el ícono de llave 🔑 junto al usuario
  2. Confirma la acción
  3. El nuevo API Key se muestra y se copia al portapapeles
  4. Envía el nuevo API Key a la agencia

### 🔄 Activar/Desactivar Usuario

- **Endpoint:** `PUT /api/corporate-users/{id}/toggle-status`
- **Descripción:** Habilita o deshabilita el acceso de la agencia
- **Estados:**
  - ✅ **Activo:** La agencia puede usar el sistema
  - ❌ **Inactivo:** La agencia no puede autenticarse ni hacer compras

- **Cuándo usar:**
  - Suspender temporalmente a una agencia
  - Desactivar agencias que ya no trabajan contigo
  - Reactivar agencias suspendidas

## 🔐 Uso del API Key (Para Agencias)

Las agencias usan su API Key para autenticarse en las solicitudes:

### Headers HTTP

```http
X-API-Key: Tu_API_Key_De_32_Caracteres
```

### Ejemplo de Compra de Boleto

```bash
curl -X POST http://localhost:8080/api/airline/tickets \
  -H "Content-Type: application/json" \
  -H "X-API-Key: abc123xyz456..." \
  -d '{
    "flightId": 81,
    "userId": 41,
    "seatNumber": "12A",
    "seatCategory": "ECONOMY",
    "passengerFirstName": "Juan",
    "passengerLastName": "Pérez",
    ...
  }'
```

### Ejemplo con Axios (JavaScript)

```javascript
import axios from 'axios';

const apiKey = 'abc123xyz456...'; // API Key de la agencia

const comprarBoleto = async () => {
  const response = await axios.post(
    'http://localhost:8080/api/airline/tickets',
    {
      flightId: 81,
      userId: 41,
      seatNumber: "12A",
      seatCategory: "ECONOMY",
      // ... más datos
    },
    {
      headers: {
        'X-API-Key': apiKey
      }
    }
  );
  
  return response.data;
};
```

## 📊 Estructura de Datos

### Usuario Empresarial (CorporateUser)

```typescript
interface CorporateUser {
  idUser: number;          // ID único
  companyName: string;     // Nombre de la empresa
  name: string;            // Nombre del contacto
  email: string;           // Email
  phone: string;           // Teléfono
  cui: string;             // CUI/NIT
  address: string;         // Dirección
  birthDate?: string;      // Fecha de nacimiento (opcional)
  apiKey: string;          // API Key único de 32 caracteres
  enabled: number;         // 1 = activo, 0 = inactivo
  isCorporate: number;     // Siempre 1 para usuarios empresariales
  role: string;            // "corporate"
  createdAt?: string;      // Fecha de creación
}
```

## 🔒 Seguridad

### API Key

- **Longitud:** 32 caracteres alfanuméricos
- **Generación:** Aleatoria y única
- **Almacenamiento:** En base de datos (campo `apiKey`)
- **Transmisión:** Siempre en headers HTTPS
- **Validación:** Verificada en cada solicitud empresarial

### Buenas Prácticas

1. **Nunca** compartas API Keys públicamente
2. **Rota** los API Keys periódicamente (cada 6-12 meses)
3. **Desactiva** usuarios que ya no usen el servicio
4. **Monitorea** el uso de API Keys para detectar anomalías
5. **Usa HTTPS** en producción siempre

## 🛡️ Permisos

### Quién Puede Gestionar Usuarios Empresariales

- **Administradores** de la aerolínea (`role = "admin"`)

### Quién NO Puede

- Usuarios regulares
- Empleados sin permisos de administrador
- Las propias agencias (no pueden gestionarse a sí mismas)

## 📖 Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/corporate-users` | Lista todos |
| `GET` | `/api/corporate-users/{id}` | Obtiene uno específico |
| `POST` | `/api/corporate-users` | Crea nuevo |
| `PUT` | `/api/corporate-users/{id}` | Actualiza |
| `PUT` | `/api/corporate-users/{id}/regenerate-key` | Regenera API Key |
| `PUT` | `/api/corporate-users/{id}/toggle-status` | Activa/Desactiva |

## 🎨 Interfaz de Usuario

### Características

- ✅ **Búsqueda en tiempo real** por empresa, email o API Key
- ✅ **Filtros** por estado
- ✅ **Paginación** para listas grandes
- ✅ **Copiar API Key** con un click
- ✅ **Máscaras de seguridad** para API Keys en la lista
- ✅ **Confirmaciones** antes de acciones críticas
- ✅ **Mensajes de éxito/error** claros

### Acciones Rápidas

- **Editar** ✏️ - Modifica información
- **Regenerar** 🔑 - Crea nuevo API Key
- **Toggle** 🔒 - Activa/Desactiva

## 🐛 Solución de Problemas

### Error: "El email ya está registrado"

**Causa:** Ya existe un usuario con ese email  
**Solución:** Usa un email diferente

### Error: "El CUI/NIT ya está registrado"

**Causa:** Ya existe un usuario con ese CUI  
**Solución:** Verifica si el usuario ya existe o usa otro CUI

### Error: "Usuario empresarial no encontrado"

**Causa:** El ID no existe o no es un usuario empresarial  
**Solución:** Verifica el ID y que `isCorporate = 1`

### Error: "Autenticación empresarial requerida"

**Causa:** No se envió el header `X-API-Key` o es inválido  
**Solución:** Verifica que el API Key sea correcto y esté en el header

## 📝 Notas Adicionales

### Base de Datos

Los usuarios empresariales se almacenan en la tabla `USERS` con:
- `isCorporate = 1`
- `role = 'corporate'`
- `apiKey` populated
- `companyName` populated

### Diferencias con Usuarios Normales

| Característica | Usuario Normal | Usuario Empresarial |
|----------------|----------------|---------------------|
| Login | Email + Password | API Key |
| Propósito | Uso personal | Compras para terceros |
| API Key | No tiene | Sí tiene |
| isCorporate | 0 | 1 |
| companyName | NULL | Nombre de empresa |

## 🎯 Casos de Uso

### Caso 1: Nueva Agencia

1. Administrador crea usuario empresarial
2. Sistema genera API Key
3. Administrador envía credenciales a la agencia
4. Agencia integra API Key en su sistema
5. Agencia comienza a hacer compras

### Caso 2: API Key Comprometido

1. Agencia reporta API Key filtrado
2. Administrador regenera API Key
3. Sistema invalida el anterior
4. Administrador envía nuevo API Key
5. Agencia actualiza sus integraciones

### Caso 3: Agencia Suspendida

1. Administrador desactiva usuario
2. Agencia no puede autenticarse
3. Solicitudes con su API Key son rechazadas
4. Cuando se resuelva, administrador reactiva

## 📞 Soporte

Si tienes problemas con usuarios empresariales:

1. Verifica que el backend esté corriendo
2. Revisa los logs del servidor
3. Confirma que el usuario tiene rol `admin`
4. Verifica la conectividad con la base de datos

---

**¡Sistema de usuarios empresariales listo para usar! 🚀**



