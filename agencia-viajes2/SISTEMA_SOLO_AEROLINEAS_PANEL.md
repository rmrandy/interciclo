# ✅ Sistema Actualizado: SOLO Aerolíneas del Panel Admin

## 🎯 Cambio Principal

**ANTES**: El sistema usaba variables de entorno como fallback si no había aerolínea configurada.

**AHORA**: El sistema **SOLO** usa aerolíneas configuradas en el panel de administración con su API Key. **No hay fallback** a variables de entorno.

---

## 🔧 Cambios Realizados

### 1. Nueva Función: `get_active_airline()`
Obtiene la aerolínea activa completa desde MongoDB:
```python
def get_active_airline():
    """
    Obtiene la aerolínea activa desde la base de datos.
    Retorna el documento completo o None si no hay aerolínea activa.
    """
    try:
        db = get_db()
        airline = db.airlines.find_one({'enabled': True})
        return airline
    except Exception as e:
        print(f"❌ Error obteniendo aerolínea activa: {e}")
        return None
```

### 2. Función Actualizada: `airline_origin_base()`
Ahora **NO usa fallback**:
```python
def airline_origin_base(request=None):
    """
    Obtiene la URL base de la aerolínea SOLO desde la base de datos.
    Ya NO usa fallback a variables de entorno.
    Retorna None si no hay aerolínea activa configurada.
    """
    airline = get_active_airline()
    
    if not airline:
        print("❌ No se encontró aerolínea activa en BD. Debes configurar una aerolínea en el panel de Admin.")
        return None
    
    # Usar configuración de la BD
    protocol = airline.get('protocol', 'http').strip()
    host = airline.get('host', 'localhost').strip()
    port = str(airline.get('port', 8080)).strip()
    base = airline.get('basePath', '/api').strip()
    
    url = f"{protocol}://{host}:{port}{base}"
    api_key_info = f" con API Key: {airline.get('apiKey', 'N/A')[:10]}..." if airline.get('apiKey') else " sin API Key"
    print(f"✅ Usando aerolínea: {airline.get('name')} ({url}){api_key_info}")
    
    return url
```

### 3. Nueva Función: `get_airline_headers()`
Reemplaza `get_corporate_headers()` para usar el API Key de la aerolínea activa:
```python
def get_airline_headers():
    """
    Retorna headers con el API Key de la aerolínea activa.
    SOLO usa aerolíneas configuradas en el panel, no sistema antiguo.
    """
    headers = {'Content-Type': 'application/json'}
    
    airline = get_active_airline()
    if airline and airline.get('apiKey'):
        api_key = airline.get('apiKey').strip()
        if api_key:
            headers['X-API-Key'] = api_key
            print(f"🔑 Usando API Key de aerolínea '{airline.get('name')}': {api_key[:10]}...")
    else:
        print(f"⚠️  Aerolínea sin API Key configurado")
    
    return headers
```

### 4. Funciones de Request Actualizadas
Todas las funciones `safe_get`, `safe_post`, `stream_get` ahora usan `get_airline_headers()`:
```python
def safe_get(url, timeout=None, params=None):
    headers = get_airline_headers()  # ← Usa API Key de aerolínea activa
    r = requests.get(url, params=params or {}, headers=headers, timeout=(3.0, read_timeout))
    # ...
```

### 5. Validación en Todas las Funciones de Proxy
Todas las funciones proxy ahora validan que exista aerolínea activa:
```python
@csrf_exempt
def proxy_airline_cities(request):
    base = airline_origin_base(request)
    if not base:
        return JsonResponse({ 
            'success': False, 
            'message': 'No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea.' 
        }, status=503)
    # ... resto del código
```

Funciones actualizadas:
- ✅ `proxy_airline_cities`
- ✅ `proxy_airline_flights`
- ✅ `proxy_airline_seats`
- ✅ `proxy_airline_create_ticket`
- ✅ `proxy_airline_create_roundtrip`
- ✅ `proxy_airline_create_stopover`
- ✅ `proxy_airline_login`
- ✅ `proxy_airline_register`
- ✅ `proxy_airline_tickets`
- ✅ `proxy_airline_ticket_pdf`
- ✅ `proxy_airline_ticket_by_id`
- ✅ `proxy_airline_corporate_tickets`
- ✅ `proxy_airline_flight_reviews`

### 6. Campo API Key en Normalización
```python
def normalize_airline_payload(data):
    return {
        # ... otros campos ...
        'apiKey': (data.get('apiKey') or '').strip(),  # ← Ahora se guarda el API Key
        # ...
    }
```

---

## 🚀 Cómo Usar

### Paso 1: Configurar Aerolínea en el Panel Admin

Ve a `http://localhost:5001/admin` → Pestaña "Aerolíneas"

Completa el formulario:

| Campo | Valor | Ejemplo |
|-------|-------|---------|
| **Nombre** | Nombre descriptivo | "Backend Principal" |
| **Código** | 2-3 letras | "BP" |
| **Protocolo** | http o https | `http` |
| **Host/IP** | IP del backend Java | `192.168.0.11` |
| **Puerto** | Puerto del servidor | `8080` |
| **Base path** | Ruta base | `/api` |
| **🔑 API Key** | **IMPORTANTE** | `abc123xyz456...` |
| **Timeout (ms)** | Tiempo de espera | `2000` |
| **Activo** | **Debe ser Sí** | ✅ **Sí** |

**Guarda la aerolínea**

### Paso 2: Verificar en Logs

Cuando el backend Django haga una petición a la aerolínea, verás:

```bash
✅ Usando aerolínea: Backend Principal (http://192.168.0.11:8080/api) con API Key: abc123xyz4...
🔑 Usando API Key de aerolínea 'Backend Principal': abc123xyz4...
```

**SI NO HAY AEROLÍNEA ACTIVA**, verás:
```bash
❌ No se encontró aerolínea activa en BD. Debes configurar una aerolínea en el panel de Admin.
```

Y el frontend recibirá:
```json
{
  "success": false,
  "message": "No hay aerolínea activa configurada. Ve al panel de Admin para configurar una aerolínea."
}
```

### Paso 3: Múltiples Aerolíneas

Puedes tener varias aerolíneas configuradas:

```
Aerolínea 1: "Desarrollo Local"
- Host: localhost
- Puerto: 8080
- API Key: dev-key-123
- Activo: No

Aerolínea 2: "Servidor de Pruebas"
- Host: 192.168.0.11
- Puerto: 8080
- API Key: test-key-456
- Activo: Sí ← Esta se usa

Aerolínea 3: "Producción"
- Host: 172.16.56.36
- Puerto: 8080
- API Key: prod-key-789
- Activo: No
```

**Solo la marcada como "Activo: Sí" se usará.**

---

## 📊 Comparación: Antes vs Después

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Fuente de configuración** | Variables de entorno + BD | ✅ **SOLO BD** |
| **Fallback** | Sí (a variables de entorno) | ❌ **No fallback** |
| **API Key** | Sistema antiguo (corporate_config) | ✅ **Por aerolínea** |
| **Validación** | Permitía continuar sin aerolínea | ✅ **Error claro si no hay aerolínea** |
| **Logs** | Confusos (usaba variables) | ✅ **Claros** (indica aerolínea y API Key) |
| **Múltiples aerolíneas** | No soportado | ✅ **Soportado** |

---

## 🔍 Estructura en MongoDB

### Colección: `airlines`

```javascript
{
  "_id": ObjectId("..."),
  "name": "Backend Principal",
  "code": "BP",
  "protocol": "http",
  "host": "192.168.0.11",
  "port": 8080,
  "basePath": "/api",
  "apiKey": "abc123xyz456...",  // ← API Key específico de esta aerolínea
  "endpoints": {
    "search": "/search",
    "book": "/book",
    "cancel": "/cancel",
    "health": "/health"
  },
  "enabled": true,  // ← SOLO las activas se usan
  "timeoutMs": 2000,
  "notes": "Backend Java principal",
  "createdAt": ISODate("..."),
  "updatedAt": ISODate("...")
}
```

---

## ⚡ Flujo de Peticiones

```
1. Usuario hace búsqueda de vuelos en frontend
   ↓
2. Frontend llama a: GET /api/integrations/airline/flights
   ↓
3. Backend Django ejecuta: proxy_airline_flights()
   ↓
4. Llama a: airline_origin_base()
   ↓
5. Busca en MongoDB: db.airlines.find_one({'enabled': True})
   ↓
6. Si NO encuentra aerolínea activa:
   ❌ Retorna error 503: "No hay aerolínea activa configurada"
   ↓
7. Si SÍ encuentra aerolínea:
   ✅ Obtiene: protocol, host, port, basePath, apiKey
   ↓
8. Construye headers con API Key:
   headers = {
     'Content-Type': 'application/json',
     'X-API-Key': 'abc123xyz456...'
   }
   ↓
9. Hace petición a backend Java:
   GET http://192.168.0.11:8080/api/airline/flights
   Headers: X-API-Key: abc123xyz456...
   ↓
10. Backend Java valida API Key y responde
   ↓
11. Django retorna respuesta al frontend
```

---

## 🛠️ Solución de Problemas

### Problema: "No hay aerolínea activa configurada"

**Causa**: No hay ninguna aerolínea con `enabled: true` en MongoDB.

**Solución**:
1. Ve al panel de Admin
2. Crea una nueva aerolínea o edita una existente
3. Asegúrate de marcar **"Activo: Sí"**
4. Guarda

### Problema: "API Key no llega al backend Java"

**Causa**: El campo `apiKey` está vacío en la aerolínea.

**Solución**:
1. Ve al panel de Admin
2. Edita la aerolínea activa
3. Ingresa el API Key en el campo "🔑 API Key (Usuario Empresarial)"
4. Guarda

**Verificar en logs**:
```bash
# Deberías ver:
🔑 Usando API Key de aerolínea 'Backend Principal': abc123xyz4...

# En lugar de:
⚠️  Aerolínea sin API Key configurado
```

### Problema: "Sigue usando IP antigua"

**Causa**: La aerolínea con IP antigua sigue marcada como activa.

**Solución**:
1. Desactiva la aerolínea antigua (Activo: No)
2. Activa la aerolínea correcta (Activo: Sí)
3. Guarda ambas

### Problema: "Connection timeout"

**Causa**: El backend Java no está accesible en la IP/Puerto configurado.

**Solución**:
1. Verifica que el backend Java esté ejecutándose
2. Verifica la IP y puerto en el panel de Admin
3. Prueba hacer ping a la IP:
   ```bash
   ping 192.168.0.11
   ```
4. Prueba acceder al endpoint directamente:
   ```bash
   curl http://192.168.0.11:8080/api/health
   ```

---

## 📝 Endpoints Afectados

Todos estos endpoints ahora requieren aerolínea activa configurada:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/integrations/airline/cities` | GET | Obtener ciudades |
| `/api/integrations/airline/flights` | GET | Buscar vuelos |
| `/api/integrations/airline/seats` | GET | Obtener asientos |
| `/api/integrations/airline/tickets` | POST | Comprar ticket |
| `/api/integrations/airline/tickets/round-trip` | POST | Vuelo redondo |
| `/api/integrations/airline/tickets/with-stopover` | POST | Vuelo con escala |
| `/api/integrations/airline/login` | POST | Login aerolínea |
| `/api/integrations/airline/register` | POST | Registro aerolínea |
| `/api/integrations/airline/tickets` | GET | Listar tickets |
| `/api/integrations/airline/tickets/{id}/pdf` | GET | Descargar PDF |
| `/api/integrations/airline/tickets/{id}` | GET | Ver ticket |
| `/api/integrations/airline/tickets/corporate` | GET | Tickets corporativos |
| `/api/integrations/airline/flights/{id}/reviews` | GET | Reviews de vuelo |

---

## ✅ Beneficios del Cambio

### 1. **Claridad Total**
- Ya no hay confusión entre variables de entorno y configuración de BD
- Logs claros que muestran exactamente qué aerolínea se está usando

### 2. **API Keys por Aerolínea**
- Cada aerolínea puede tener su propio API Key
- Fácil de rotar o revocar API Keys

### 3. **Error Temprano**
- Si no hay aerolínea configurada, se detecta inmediatamente
- No intenta usar configuraciones incorrectas

### 4. **Gestión Centralizada**
- TODO desde el panel de Admin
- No hay que editar archivos .env ni reiniciar servicios

### 5. **Múltiples Aerolíneas**
- Soporte completo para múltiples backends
- Fácil cambio entre ellos (solo cambiar "Activo")

---

## 🎯 Resumen Ejecutivo

### Antes
```
Si no hay aerolínea en BD → Usa variables de entorno
API Key → Busca en corporate_config (sistema antiguo)
Error → Confuso, seguía intentando
```

### Ahora
```
Si no hay aerolínea en BD → ❌ Error claro
API Key → De la aerolínea activa
Error → Mensaje claro: "Configura aerolínea en Admin"
```

---

## 📞 Estado del Sistema

**Cambios aplicados**: ✅ Completados  
**Testing requerido**: Configurar una aerolínea en Admin  
**Breaking change**: Sí, ahora REQUIERE aerolínea configurada  
**Rollback**: Revertir cambios en views.py  

---

## 🚀 Próximos Pasos

1. **Configura una aerolínea** en el panel de Admin
2. **Ingresa el API Key** generado desde el backend Java
3. **Marca como Activo: Sí**
4. **Prueba una búsqueda** de vuelos
5. **Verifica los logs** para confirmar que se usa el API Key correcto

**¡El sistema ahora es más robusto y claro!** 🎉

