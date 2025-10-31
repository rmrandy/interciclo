# ✅ Configuración Dinámica de Aerolíneas - Agencia de Viajes

## 🎯 Problema Solucionado

**Antes**: 
- ❌ IP hardcodeada: `172.16.56.36:8080`
- ❌ Configuración en variables de entorno
- ❌ No se usaba la configuración del panel de admin

**Después**:
- ✅ Lee configuración desde la base de datos
- ✅ Usa la aerolínea marcada como "Activa" en el admin
- ✅ IP y puerto dinámicos según lo que configures
- ✅ Actualización en tiempo real

---

## 🔧 Cambios Realizados

### 1. Backend Django - views.py ✅

#### Función `airline_origin_base()` Mejorada
```python
def airline_origin_base(request=None):
    """
    Obtiene la URL base de la aerolínea desde la base de datos.
    Busca la primera aerolínea activa (enabled=True) en la colección airlines.
    Si no encuentra ninguna, usa variables de entorno como fallback.
    """
    try:
        # Leer de la BD
        db = get_db()
        airline = db.airlines.find_one({'enabled': True})
        
        if airline:
            # Usar configuración de la BD ✅
            protocol = airline.get('protocol', 'http')
            host = airline.get('host', 'localhost')
            port = str(airline.get('port', 8080))
            base = airline.get('basePath', '/api')
            
            print(f"✅ Usando aerolínea de BD: {airline.get('name')} ({protocol}://{host}:{port}{base})")
            return f"{protocol}://{host}:{port}{base}"
        else:
            # Fallback a variables de entorno
            ...
    except Exception as e:
        # Fallback en caso de error
        ...
```

**Beneficios:**
- ✅ Lee de la BD primero
- ✅ Usa aerolíneas con `enabled: true`
- ✅ Fallback a variables de entorno si no hay BD
- ✅ Logs informativos en consola

#### Función `airline_timeout_seconds()` Mejorada
```python
def airline_timeout_seconds():
    """
    Obtiene el timeout desde la BD.
    Fallback a variables de entorno.
    """
    try:
        db = get_db()
        airline = db.airlines.find_one({'enabled': True})
        
        if airline and 'timeoutMs' in airline:
            ms = int(airline.get('timeoutMs', 20000))
            return max(3, ms // 1000)
    except Exception:
        pass
    
    # Fallback
    ...
```

#### Nuevo Endpoint: `airlines_active_view()` ✅
```python
@csrf_exempt
def airlines_active_view(request):
    """
    GET /api/airlines/active
    Retorna la configuración de la aerolínea activa.
    """
    db = get_db()
    airline = db.airlines.find_one({'enabled': True})
    
    if airline:
        return JsonResponse({
            'success': True,
            'data': airline,
            'fullUrl': f"{protocol}://{host}:{port}{base}"
        })
```

### 2. URLs Actualizadas ✅

```python
# api/urls.py
urlpatterns = [
    path('airlines', airlines_list_view),
    path('airlines/active', airlines_active_view),  # NUEVO ✅
    path('airlines/<str:airline_id>', airlines_detail_view),
]
```

### 3. Script start-django.sh Actualizado ✅

**Antes**:
```bash
export AIRLINE_HOST="172.16.56.36"  # ❌ IP hardcodeada
```

**Después**:
```bash
# Usa la IP detectada automáticamente
export AIRLINE_HOST="${IP}"  # ✅ IP dinámica

# NOTA: Estos valores solo se usan como fallback
# si NO hay aerolíneas activas en la BD
```

---

## 📋 Cómo Funciona Ahora

### Flujo de Configuración

```
1. Admin crea/edita aerolínea en panel
   ↓
2. Se guarda en MongoDB (colección airlines)
   ↓
3. Backend Django lee de la BD cuando hace proxy
   ↓
4. Usa: protocol://host:port/basePath de la BD
   ↓
5. Si actualizas el admin, se usa inmediatamente
```

### Prioridad de Configuración

```
1º - Base de Datos (airlines collection, enabled=true)  ✅ PRIORIDAD ALTA
2º - Variables de entorno (AIRLINE_HOST, etc.)           FALLBACK
3º - Valores por defecto (http://localhost:8080/api)     ÚLTIMO RECURSO
```

---

## 🚀 Cómo Usar

### Paso 1: Configurar Aerolínea en el Admin

En el panel de administración (la interfaz que muestras):

1. **Nombre**: "Mi Aerolínea"
2. **Código**: "MA"
3. **Protocolo**: `http`
4. **Host/IP**: `192.168.0.2` (o la IP correcta)
5. **Puerto**: `8080`
6. **Base path**: `/api`
7. **Endpoint búsqueda**: `/search`
8. **Endpoint compra**: `/book`
9. **Endpoint cancelación**: `/cancel`
10. **Endpoint health**: `/health`
11. **Timeout (ms)**: `2000`
12. **Activo**: ✅ **Sí** (Importante!)

Clic en "Crear aerolínea"

### Paso 2: Verificar Configuración Activa

```bash
# Ver qué aerolínea está activa
curl http://localhost:5001/api/airlines/active
```

**Respuesta esperada:**
```json
{
  "success": true,
  "data": {
    "name": "Mi Aerolínea",
    "code": "MA",
    "protocol": "http",
    "host": "192.168.0.2",
    "port": 8080,
    "basePath": "/api",
    "enabled": true
  },
  "fullUrl": "http://192.168.0.2:8080/api",
  "message": "Usando: Mi Aerolínea"
}
```

### Paso 3: El Backend Usa Esta Configuración Automáticamente

Cuando el backend hace proxy a la aerolínea:
```python
# En proxy_airline_cities(), proxy_airline_flights(), etc.
base = airline_origin_base(request)  # ← Lee de la BD automáticamente
data = safe_get(f"{base}/airline/cities")
```

**Resultado**: Usa `http://192.168.0.2:8080/api/airline/cities` ✅

---

## 📊 Endpoints Disponibles

### Administración de Aerolíneas

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/airlines` | GET | Listar todas las aerolíneas |
| `/api/airlines` | POST | Crear nueva aerolínea |
| `/api/airlines/active` | GET | **Obtener aerolínea activa** ✅ NUEVO |
| `/api/airlines/{id}` | GET | Ver detalles de una aerolínea |
| `/api/airlines/{id}` | PUT/PATCH | Actualizar aerolínea |
| `/api/airlines/{id}` | DELETE | Eliminar aerolínea |

### Proxy a Aerolínea (Usan configuración de BD)

| Endpoint | Usa BD |
|----------|--------|
| `/api/integrations/airline/cities` | ✅ |
| `/api/integrations/airline/flights` | ✅ |
| `/api/integrations/airline/seats` | ✅ |
| `/api/integrations/airline/tickets` | ✅ |
| `/api/integrations/airline/login` | ✅ |
| `/api/integrations/airline/register` | ✅ |

---

## 🔍 Logs y Debugging

### Ver en Consola Qué Configuración Se Usa

Cuando el backend hace una llamada a la aerolínea, verás en la consola:

```bash
✅ Usando aerolínea de BD: Prueba (http://192.168.0.2:8080/api)
```

O si no hay configuración:

```bash
⚠️  No se encontró aerolínea activa en BD, usando variables de entorno
```

### Verificar Desde Terminal

```bash
# Ver aerolíneas configuradas
curl http://localhost:5001/api/airlines

# Ver cuál está activa
curl http://localhost:5001/api/airlines/active

# Ver info de la BD
curl http://localhost:5001/api/debug/db-info
```

---

## 🛠️ Actualizar Configuración

### Desde el Admin (Interfaz Web)
1. Ve a "Admin" → "Aerolíneas"
2. Haz clic en "Editar" en la aerolínea
3. Cambia Host/IP, Puerto, etc.
4. Guarda
5. **La próxima llamada usará la nueva configuración** ✅

### Desde API Directamente
```bash
# Actualizar aerolínea (ejemplo con curl)
curl -X PATCH http://localhost:5001/api/airlines/[ID] \
  -H "Content-Type: application/json" \
  -d '{
    "host": "192.168.1.100",
    "port": 8080,
    "enabled": true
  }'
```

### Activar/Desactivar Aerolínea
```bash
# Desactivar
curl -X PATCH http://localhost:5001/api/airlines/[ID] \
  -H "Content-Type: application/json" \
  -d '{"enabled": false}'

# Activar
curl -X PATCH http://localhost:5001/api/airlines/[ID] \
  -H "Content-Type: application/json" \
  -d '{"enabled": true}'
```

---

## 💡 Casos de Uso

### Caso 1: Cambiar de Servidor de Pruebas a Producción

**Servidor de Pruebas:**
```
Host: 192.168.0.2
Puerto: 8080
Activo: Sí
```

**Servidor de Producción:**
```
Host: 172.16.56.36
Puerto: 8080
Activo: No (por ahora)
```

**Para cambiar**:
1. Desactiva el servidor de pruebas (enabled: false)
2. Activa el servidor de producción (enabled: true)
3. Listo! Automáticamente usa el nuevo servidor

### Caso 2: Múltiples Aerolíneas

Puedes tener varias aerolíneas configuradas:
- Aerolínea A (enabled: false)
- Aerolínea B (enabled: true) ← Esta se usa
- Aerolínea C (enabled: false)

Solo la marcada como "Activo: Sí" se utiliza.

### Caso 3: Desarrollo vs Producción

```
Desarrollo:
- Nombre: "AeroLinea Dev"
- Host: localhost
- Puerto: 8080
- Activo: Sí (durante desarrollo)

Producción:
- Nombre: "AeroLinea Prod"
- Host: 172.16.56.36
- Puerto: 8080
- Activo: No (cambiar a Sí cuando deploys)
```

---

## 🎯 Ventajas de Este Sistema

### ✅ Sin Código
- No necesitas editar código para cambiar IPs
- Todo se maneja desde el admin
- No hay que recompilar ni reiniciar

### ✅ Dinámico
- Cambios en tiempo real
- Solo actualiza en el admin
- La siguiente llamada usa la nueva config

### ✅ Flexible
- Múltiples aerolíneas configuradas
- Activar/desactivar fácilmente
- Cambiar entre entornos rápidamente

### ✅ Auditable
- Historial de cambios en la BD
- Saber qué aerolínea está activa
- Logs en consola del backend

---

## 📝 Estructura en MongoDB

### Colección: `airlines`

```javascript
{
  "_id": ObjectId("..."),
  "name": "Prueba",
  "code": "PP",
  "protocol": "http",
  "host": "192.168.0.2",
  "port": 8080,
  "basePath": "/api",
  "endpoints": {
    "search": "/search",
    "book": "/book",
    "cancel": "/cancel",
    "health": "/health"
  },
  "enabled": true,  // ← Solo la que tiene true se usa
  "timeoutMs": 2000,
  "notes": "Servidor de pruebas",
  "createdAt": ISODate("..."),
  "updatedAt": ISODate("...")
}
```

---

## 🔄 Migración de Configuración Existente

Si ya tienes variables de entorno configuradas:

### Opción 1: Crear Aerolínea en Admin Manualmente
1. Ve al admin
2. Crea una nueva aerolínea con tus valores actuales
3. Márcala como "Activo: Sí"
4. Listo

### Opción 2: Insertar Directamente en MongoDB
```javascript
// En MongoDB shell o Compass
db.airlines.insertOne({
  name: "Mi Aerolínea",
  code: "MA",
  protocol: "http",
  host: "192.168.0.2",  // Tu IP
  port: 8080,
  basePath: "/api",
  endpoints: {
    search: "/search",
    book: "/book",
    cancel: "/cancel",
    health: "/health"
  },
  enabled: true,
  timeoutMs: 2000,
  notes: "",
  createdAt: new Date(),
  updatedAt: new Date()
})
```

---

## 🧪 Cómo Probar

### 1. Verifica que no haya aerolínea activa
```bash
curl http://localhost:5001/api/airlines/active
```

**Resultado esperado**:
```json
{
  "success": false,
  "message": "No hay aerolínea activa configurada"
}
```

### 2. Crea una aerolínea en el admin
- Rellena el formulario
- **Importante**: Marca "Activo: Sí"
- Guarda

### 3. Verifica nuevamente
```bash
curl http://localhost:5001/api/airlines/active
```

**Resultado esperado**:
```json
{
  "success": true,
  "data": {...},
  "fullUrl": "http://192.168.0.2:8080/api",
  "message": "Usando: Mi Aerolínea"
}
```

### 4. Prueba una llamada proxy
```bash
# Probar obtener ciudades (usará la config de BD)
curl http://localhost:5001/api/integrations/airline/cities
```

**Logs en backend Django**:
```
✅ Usando aerolínea de BD: Mi Aerolínea (http://192.168.0.2:8080/api)
```

---

## 📊 Comparación: Antes vs Después

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Configuración** | Variables de entorno | ✅ Base de datos |
| **IP** | 172.16.56.36 (hardcoded) | ✅ Configurable en admin |
| **Puerto** | 8080 (hardcoded) | ✅ Configurable en admin |
| **Cambios** | Editar código/script | ✅ Editar en admin UI |
| **Reinicio** | Necesario | ✅ No necesario |
| **Múltiples aerolíneas** | No soportado | ✅ Soportado |
| **Activar/Desactivar** | No soportado | ✅ Toggle en admin |

---

## 🎓 Mejores Prácticas

### 1. Siempre Ten Una Aerolínea Activa
```
enabled: true  ✅ Bueno
enabled: false ❌ No se usa
```

### 2. Solo Una Aerolínea Activa a la Vez
Si tienes varias marcadas como activas, se usará la primera encontrada.

### 3. Campos Requeridos
- ✅ name (obligatorio)
- ✅ code (obligatorio)
- ✅ protocol (obligatorio)
- ✅ host (obligatorio)
- ✅ port (obligatorio)
- ✅ basePath (obligatorio)

### 4. Testing
Antes de activar en producción:
1. Crea la aerolínea con enabled: false
2. Verifica los endpoints manualmente
3. Cuando funcione, cambia a enabled: true

---

## 🆘 Solución de Problemas

### Problema: "No hay aerolínea activa"
**Solución**: Verifica que al menos una aerolínea tenga `enabled: true`

```bash
# Ver todas las aerolíneas
curl http://localhost:5001/api/airlines
```

### Problema: "Sigo viendo la IP antigua"
**Causas posibles**:
1. La aerolínea no está marcada como "Activa"
2. Hay caché en el navegador
3. El backend Django no se reinició después de cambios en código

**Soluciones**:
```bash
# 1. Verificar aerolínea activa
curl http://localhost:5001/api/airlines/active

# 2. Limpiar caché del navegador
# Cmd+Shift+R (macOS) o Ctrl+Shift+R (Windows/Linux)

# 3. Reiniciar Django
# Ctrl+C y luego ./scripts/start-django.sh
```

### Problema: "Connection timeout"
**Solución**: Ajusta el timeout en el admin

- Aumenta "Timeout (ms)" a 5000 o más
- Guarda
- Intenta nuevamente

---

## ✨ Beneficios

### Para Desarrollo
- ✅ Cambio rápido entre entornos
- ✅ No editar código
- ✅ Testing fácil de diferentes servidores

### Para Producción
- ✅ Configuración centralizada
- ✅ Cambios sin downtime
- ✅ Rollback rápido si hay problemas

### Para el Equipo
- ✅ No necesitan acceso al código
- ✅ Admin UI intuitivo
- ✅ Cambios auditables en BD

---

## 🔄 Próximos Pasos

### Opcionales (Mejoras Futuras)
1. [ ] Agregar health check automático de aerolíneas
2. [ ] Dashboard con estado de conectividad
3. [ ] Múltiples aerolíneas activas (balanceo)
4. [ ] Caché de configuración en memoria
5. [ ] Notificaciones cuando cambia configuración

---

## 📞 Resumen

### Cambios Realizados
1. ✅ `airline_origin_base()` lee de BD
2. ✅ `airline_timeout_seconds()` lee de BD
3. ✅ Nuevo endpoint `/api/airlines/active`
4. ✅ Script sin IP hardcodeada
5. ✅ Logs informativos en consola

### Cómo Usar
1. Configura aerolínea en admin
2. Marca como "Activo: Sí"
3. Guarda
4. **Automáticamente se usa** ✅

### Verificar
```bash
curl http://localhost:5001/api/airlines/active
```

---

**Estado**: ✅ Completado  
**Configuración**: Dinámica desde BD  
**IP Hardcodeada**: ✅ Eliminada  
**Admin**: ✅ Funcional  

## 🎉 ¡Configuración Dinámica Lista!

