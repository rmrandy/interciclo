# 👔 Sistema de Usuarios Corporativos

## 📋 Resumen

Sistema completo para gestionar usuarios empresariales (agencias de viaje) con API Keys en MongoDB. Los usuarios corporativos pueden usar las APIs del sistema mediante autenticación con API Key.

## 🏗️ Arquitectura

### Backend: Django + MongoDB
- **Base de datos**: MongoDB (colección `corporate_config`)
- **Puerto**: 5001
- **Endpoints**: `/api/corporate-users`

### Frontend: React
- **Ruta**: `/admin/corporate-users`
- **Protegido**: Solo administradores

## 📡 API Endpoints

### 1. **GET** `/api/corporate-users`
Obtiene lista de todos los usuarios corporativos

**Respuesta**:
```json
[
  {
    "idUser": "507f1f77bcf86cd799439011",
    "companyName": "Agencia XYZ",
    "name": "Juan Pérez",
    "email": "contacto@agencia.com",
    "phone": "12345678",
    "cui": "1234567890",
    "birthDate": "1990-01-01",
    "address": "Dirección completa",
    "apiKey": "ABCxyz123...",
    "enabled": 1,
    "createdAt": "2023-10-20T10:00:00Z",
    "updatedAt": "2023-10-20T10:00:00Z"
  }
]
```

### 2. **POST** `/api/corporate-users`
Crea un nuevo usuario corporativo

**Body**:
```json
{
  "companyName": "Agencia ABC",
  "name": "María García",
  "email": "info@agenciaabc.com",
  "phone": "87654321",
  "cui": "9876543210",
  "birthDate": "1985-05-15",
  "address": "Calle Principal 123",
  "password": "password123"
}
```

**Respuesta**:
```json
{
  "success": true,
  "idUser": "507f1f77bcf86cd799439011",
  "apiKey": "nuevoapikey32caracteres...",
  "message": "Usuario corporativo creado exitosamente"
}
```

**Características**:
- ✅ Genera automáticamente un API Key único de 32 caracteres
- ✅ Hashea la contraseña con bcrypt (12 rounds)
- ✅ Valida email y campos requeridos
- ✅ Verifica que no exista duplicado de email

### 3. **GET** `/api/corporate-users/{id}`
Obtiene un usuario específico

**Respuesta**: Objeto con todos los datos del usuario

### 4. **PUT** `/api/corporate-users/{id}`
Actualiza un usuario corporativo

**Body**:
```json
{
  "companyName": "Nuevo Nombre",
  "name": "Nuevo Contacto",
  "email": "nuevo@email.com",
  "phone": "11112222",
  "cui": "1111222233",
  "address": "Nueva dirección",
  "enabled": 1,
  "password": "nuevacontraseña" // Opcional
}
```

**Características**:
- ✅ Actualiza solo los campos enviados
- ✅ Valida email y duplicados
- ✅ Hashea nueva contraseña si se proporciona

### 5. **PUT** `/api/corporate-users/{id}/regenerate-key`
Regenera el API Key de un usuario

**Respuesta**:
```json
{
  "success": true,
  "apiKey": "nuevoApiKeyRegenerado32chars",
  "message": "API Key regenerado exitosamente"
}
```

**Nota**: ⚠️ El API Key anterior quedará invalidado

### 6. **PUT** `/api/corporate-users/{id}/toggle-status`
Activa/desactiva un usuario corporativo

**Respuesta**:
```json
{
  "success": true,
  "enabled": true,
  "message": "Usuario activado exitosamente"
}
```

## 🎨 Interfaz Frontend

### Características

#### 📊 Dashboard
- Contador de usuarios totales
- Contador de usuarios activos
- Buscador en tiempo real
- Vista de tabla responsive

#### ✨ Funcionalidades
1. **Crear Usuario**
   - Formulario modal completo
   - Validación de campos
   - API Key copiado automáticamente al portapapeles
   
2. **Editar Usuario**
   - Modal con datos precargados
   - Actualización de contraseña opcional
   - Visualización del API Key actual

3. **Regenerar API Key**
   - Confirmación antes de regenerar
   - Nuevo key copiado al portapapeles
   - Actualización automática en la tabla

4. **Activar/Desactivar**
   - Toggle rápido de estado
   - Confirmación de acción
   - Actualización visual inmediata

5. **Copiar API Key**
   - Botón de copia rápida
   - API Keys enmascarados por seguridad
   - Notificación de copia exitosa

#### 🎨 Diseño
- Gradientes modernos
- Íconos intuitivos
- Estados de loading mejorados
- Mensajes de éxito/error claros
- Tabla responsive con scroll horizontal
- Badges de estado coloridos

## 🗄️ Estructura MongoDB

### Colección: `corporate_config`

```javascript
{
  "_id": ObjectId("..."),
  "companyName": "Nombre de la Empresa",
  "name": "Nombre del Contacto",
  "email": "contacto@empresa.com",
  "phone": "12345678",
  "cui": "1234567890",
  "birthDate": "1990-01-01",
  "address": "Dirección completa",
  "password": "$2b$12$hashdelacontraseña...",
  "apiKey": "keyunicode32caracteres",
  "enabled": true,
  "createdAt": ISODate("2023-10-20T10:00:00Z"),
  "updatedAt": ISODate("2023-10-20T10:00:00Z")
}
```

### Índices Recomendados
```javascript
db.corporate_config.createIndex({ email: 1 }, { unique: true });
db.corporate_config.createIndex({ apiKey: 1 }, { unique: true });
db.corporate_config.createIndex({ enabled: 1 });
```

## 🚀 Cómo Usar

### 1. Iniciar Backend Django
```bash
cd agencia-viajes/backend-django
python manage.py runserver 0.0.0.0:5001
```

### 2. Iniciar Frontend React
```bash
cd agencia-viajes/agencia
npm run dev
```

### 3. Acceder a la Gestión
1. Abre el navegador en `http://localhost:5173`
2. Inicia sesión como administrador
3. Ve a **Admin** → **Gestionar Usuarios Empresariales**
4. O accede directamente a: `http://localhost:5173/admin/corporate-users`

## 🔐 Seguridad

### Contraseñas
- ✅ Hasheadas con bcrypt (12 rounds)
- ✅ Validación de longitud mínima (6 caracteres)
- ✅ Nunca se retornan en las respuestas GET

### API Keys
- ✅ Generación aleatoria segura (32 caracteres)
- ✅ Únicos en toda la base de datos
- ✅ Enmascarados en la UI (solo se muestran parcialmente)
- ✅ Regenerables cuando sea necesario

### Validaciones
- ✅ Email formato válido
- ✅ No duplicados de email
- ✅ No duplicados de API Key
- ✅ Campos requeridos validados
- ✅ Solo administradores pueden acceder

## 📝 Notas Importantes

1. **Usuario Existente en MongoDB**: Si ya tienes un documento en `corporate_config` con un API Key (como el que vi en tu captura), el sistema lo mostrará automáticamente en la tabla.

2. **Migración de Datos**: Si tienes usuarios corporativos hardcodeados, puedes migrarlos a MongoDB usando el formulario de creación o insertándolos directamente en la colección.

3. **Autodetección de Backend**: El frontend usa el servicio `api.js` que autodetecta el puerto del backend Django (busca en 5001, 5002, 8000, 8080).

4. **CORS**: El backend Django ya tiene CORS configurado para aceptar peticiones desde cualquier origen.

## 🐛 Troubleshooting

### El frontend no carga usuarios
- ✅ Verifica que el backend Django esté corriendo en el puerto 5001
- ✅ Revisa la consola del navegador para ver qué URL está usando
- ✅ Verifica la conexión a MongoDB

### Error al crear usuario
- ✅ Verifica que todos los campos requeridos estén completos
- ✅ Asegúrate de que el email no esté duplicado
- ✅ La contraseña debe tener al menos 6 caracteres

### API Key no se copia al portapapeles
- ✅ Verifica que el navegador tenga permisos para acceder al portapapeles
- ✅ Debe ser HTTPS o localhost para funcionar
- ✅ Intenta copiar manualmente desde el campo de texto

## 🎯 Próximos Pasos

1. Implementar autenticación con API Key en los endpoints de integración
2. Agregar logs de uso de API por usuario corporativo
3. Implementar límites de rate limiting por API Key
4. Dashboard de estadísticas de uso por empresa
5. Gestión de permisos granulares por API Key

---

**Desarrollado por**: Sistema de Gestión de Aerolíneas
**Fecha**: Octubre 2024
**Stack**: React + Django + MongoDB








