# 📝 Configuración del Sitio y Puertos

## ⚙️ Configuración del Sitio (Header y Footer)

### Acceso a la Configuración

1. Inicia sesión como **administrador** en la agencia
2. Ve a **Admin** > **⚙️ Configuración del Sitio**
3. O accede directamente a: `http://localhost:5173/admin/site-config`

### Campos Editables

#### Nombre de la Agencia
- **Campo**: `agencyName`
- **Aparece en**: Header del sitio
- **Ejemplo**: "Mi Agencia de Viajes Premium"

#### Información del Footer

**Datos de la Empresa**:
- `companyName`: Nombre legal de la empresa
- `address`: Dirección física
- `phone`: Teléfono de contacto
- `email`: Email de contacto

**Redes Sociales**:
- `facebook`: URL completa de Facebook (ej: `https://facebook.com/tu-pagina`)
- `twitter`: URL completa de Twitter/X (ej: `https://twitter.com/tu-usuario`)
- `instagram`: URL completa de Instagram (ej: `https://instagram.com/tu-usuario`)

### Ejemplo de Uso

```javascript
// Los datos se almacenan en MongoDB (colección: site_config)
{
  "agencyName": "Viajes El Mundo",
  "footer": {
    "companyName": "Viajes El Mundo S.A.",
    "address": "Calle Principal 456, Santo Domingo",
    "phone": "+1 809 123 4567",
    "email": "contacto@viajeselmundo.com",
    "socialMedia": {
      "facebook": "https://facebook.com/viajeselmundo",
      "twitter": "https://twitter.com/viajeselmundo",
      "instagram": "https://instagram.com/viajeselmundo"
    }
  }
}
```

---

## 🔌 Configuración de Puertos

### Backend Django

#### Iniciar en Puerto Específico

```bash
cd agencia-viajes/backend-django

# Puerto por defecto (5001)
./scripts/start-django.sh

# Puerto personalizado
./scripts/start-django.sh 5002

# Otro puerto
./scripts/start-django.sh 8000
```

#### Variables de Entorno

El backend Django respeta estas variables:
- `DJANGO_PORT`: Puerto del servidor Django
- `SERVER_IP`: IP específica (opcional, se autodetecta)

### Frontend (Agencia)

#### Configuración Automática

El frontend **detecta automáticamente** el puerto del backend:
1. Prueba puertos candidatos: `5001`, `5002`, `8000`, `8080`
2. Se conecta al primero que responda
3. Usa la misma IP que el navegador

#### Variables de Entorno (Opcional)

Crea un archivo `.env` en `agencia-viajes/agencia/`:

```bash
# Puerto del backend Django
VITE_DJANGO_PORT=5001

# IP del servidor (opcional, se autodetecta)
VITE_SERVER_IP=192.168.0.4

# O URL completa del backend
VITE_API_BASE_URL=http://192.168.0.4:5001/api
```

#### Iniciar Frontend en Puerto Específico

```bash
cd agencia-viajes/agencia

# Puerto por defecto (5173)
npm run dev

# Puerto personalizado
npm run dev -- --port 5174

# Otro puerto
npm run dev -- --port 3000
```

---

## 🚀 Escenarios Comunes

### Desarrollo Local Estándar

```bash
# Terminal 1: Backend Django en puerto 5001
cd agencia-viajes/backend-django
./scripts/start-django.sh

# Terminal 2: Frontend en puerto 5173
cd agencia-viajes/agencia
npm run dev
```

✅ **Resultado**: Frontend en `http://localhost:5173` se conecta a backend en `http://localhost:5001/api`

### Múltiples Instancias de Backend

```bash
# Terminal 1: Backend 1 en puerto 5001
cd agencia-viajes/backend-django
./scripts/start-django.sh 5001

# Terminal 2: Backend 2 en puerto 5002
cd agencia-viajes/backend-django
./scripts/start-django.sh 5002

# Terminal 3: Frontend apuntando al puerto 5002
cd agencia-viajes/agencia
VITE_DJANGO_PORT=5002 npm run dev
```

### Red Local (Varios Dispositivos)

```bash
# En tu computadora (IP: 192.168.0.10)
cd agencia-viajes/backend-django
./scripts/start-django.sh 5001

# El backend se sirve en: http://192.168.0.10:5001
```

Desde otro dispositivo en la misma red:
```
http://192.168.0.10:5173
```

El frontend detectará automáticamente el backend en `http://192.168.0.10:5001`

---

## 🔍 Verificar Configuración

### Ver Configuración Actual

Abre la consola del navegador (F12) y verás:
```
📡 Configuración de puertos: {
  backend: "5001",
  frontend: "5173",
  serverIP: "192.168.0.4",
  backendURL: "http://192.168.0.4:5001"
}
```

### Endpoints de Verificación

- **Backend health**: `http://localhost:5001/api/debug/db-info`
- **Configuración del sitio**: `http://localhost:5001/api/site-config`
- **Aerolíneas activas**: `http://localhost:5001/api/airlines/active`

---

## 📋 Colección MongoDB: `site_config`

La configuración del sitio se almacena en MongoDB:

```javascript
// Documento único en la colección 'site_config'
{
  "_id": ObjectId("..."),
  "agencyName": "Agencia de Viajes",
  "footer": {
    "companyName": "Agencia de Viajes S.A.",
    "address": "Av. Principal 123, Ciudad",
    "phone": "+1 234 567 890",
    "email": "info@agencia.com",
    "socialMedia": {
      "facebook": "",
      "twitter": "",
      "instagram": ""
    }
  },
  "logo": "",
  "primaryColor": "#1e40af",
  "secondaryColor": "#3b82f6"
}
```

---

## 🛠️ Solución de Problemas

### El frontend no se conecta al backend

1. **Verifica que el backend esté corriendo**:
   ```bash
   curl http://localhost:5001/api/debug/db-info
   ```

2. **Revisa la consola del navegador** (F12):
   - Busca mensajes de error de conexión
   - Verifica la URL que está intentando usar

3. **Especifica el puerto manualmente**:
   ```bash
   VITE_DJANGO_PORT=5001 npm run dev
   ```

### Error de CORS

Si ves errores de CORS, verifica que el backend Django tenga configurado `django-cors-headers` correctamente en `settings.py`:

```python
CORS_ALLOW_ALL_ORIGINS = True  # Solo para desarrollo
```

### El nombre de la agencia no cambia

1. Verifica que estés logueado como **admin**
2. Actualiza la configuración en `/admin/site-config`
3. Refresca la página (F5)
4. Revisa la consola del navegador por errores

---

## 📚 Archivos Relevantes

### Backend
- `backend-django/scripts/start-django.sh` - Script de inicio con puerto configurable
- `backend-django/api/views.py` - Vista `site_config_view`
- `backend-django/api/urls.py` - Ruta `/api/site-config`

### Frontend
- `agencia/src/contexts/SiteConfigContext.jsx` - Contexto de configuración
- `agencia/src/pages/SiteConfig.jsx` - Página de edición
- `agencia/src/layouts/MainLayout.jsx` - Header y Footer
- `agencia/src/services/api.js` - Cliente API con detección automática
- `agencia/src/config/ports.js` - Configuración de puertos

---

## ✨ Características

✅ **Editable desde la interfaz** - Sin tocar código  
✅ **Persistencia en MongoDB** - Los cambios se guardan automáticamente  
✅ **Detección automática de puertos** - No requiere configuración manual  
✅ **Soporte multi-dispositivo** - Funciona en red local  
✅ **Solo para administradores** - Rutas protegidas  
✅ **Actualización en tiempo real** - Los cambios se reflejan inmediatamente  

