# 🔐 Configuración CORS - BackendV5

Este documento explica la configuración de CORS (Cross-Origin Resource Sharing) implementada en el backend.

## 🎯 Objetivo

Permitir que **múltiples frontends** corriendo en **diferentes puertos** puedan comunicarse con el backend sin problemas de CORS.

## 🏗️ Arquitectura

### Componentes

1. **CorsFilter.java** - Filtro centralizado que maneja CORS
2. **App.java** - Configuración del servidor con el filtro aplicado

### Flujo de Solicitudes

```
Cliente (Frontend)
    │
    ├─ Preflight (OPTIONS) ──────────┐
    │                                 │
    ├─ GET/POST/PUT/DELETE ──────────┤
    │                                 │
    ▼                                 ▼
┌──────────────────────────────────────┐
│         CorsFilter (Filtro)          │
│   ✓ Valida origen                    │
│   ✓ Añade headers CORS              │
│   ✓ Maneja preflight                │
└──────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────┐
│    Handler específico (Controller)   │
│   • FlightHandler                    │
│   • UserHandler                      │
│   • etc.                             │
└──────────────────────────────────────┘
```

## 📝 Implementación

### 1. CorsFilter.java

Ubicación: `src/main/java/com/sources/app/util/CorsFilter.java`

**Headers configurados:**

- `Access-Control-Allow-Origin`: Origen específico o `*`
- `Access-Control-Allow-Credentials`: `true`
- `Access-Control-Allow-Methods`: `GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD`
- `Access-Control-Allow-Headers`: Lista completa de headers permitidos
- `Access-Control-Expose-Headers`: Headers que el cliente puede leer
- `Access-Control-Max-Age`: `3600` (cachea preflight por 1 hora)

**Características:**

✅ Detecta el origen de la solicitud dinámicamente  
✅ Permite credenciales (cookies, headers de autorización)  
✅ Maneja preflight automáticamente  
✅ Optimiza con cache de 1 hora  
✅ Permite headers personalizados  

### 2. App.java

Ubicación: `src/main/java/com/sources/app/App.java`

**Método helper:**

```java
private static void createContextWithCors(HttpServer server, String path, 
                                           HttpHandler handler, 
                                           CorsFilter corsFilter) {
    HttpContext context = server.createContext(path, handler);
    context.getFilters().add(corsFilter);
}
```

Este método crea contextos HTTP con el filtro CORS aplicado automáticamente.

**Aplicación:**

Todos los endpoints del servidor tienen el filtro CORS aplicado:

```java
CorsFilter corsFilter = new CorsFilter();
createContextWithCors(server, "/api/login", new LoginHandler(userDAO), corsFilter);
createContextWithCors(server, "/api/airline/flights", new FlightHandler(), corsFilter);
// ... todos los demás endpoints
```

## 🔧 Configuración de Puertos

### Backend

```bash
# Iniciar en puerto específico
mvn exec:java -Dexec.mainClass="com.sources.app.App" -Dport=8080

# O usar el script
./start-backend.sh 8080
```

### Frontends

Los frontends pueden correr en **cualquier puerto** y el CORS funcionará:

```bash
# Aerolínea en puerto 5050
PORT=5050 npm run dev

# Agencia en puerto 3000
PORT=3000 npm run dev
```

## 🧪 Pruebas de CORS

### Prueba básica

```bash
curl -H "Origin: http://localhost:5050" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     -v \
     http://localhost:8080/api/airline/flights
```

**Respuesta esperada:**

```
< HTTP/1.1 204 No Content
< Access-Control-Allow-Origin: http://localhost:5050
< Access-Control-Allow-Credentials: true
< Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
< Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, X-API-Key, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers
< Access-Control-Max-Age: 3600
```

### Prueba con múltiples orígenes

```bash
# Origen 1
curl -H "Origin: http://localhost:5050" -v http://localhost:8080/api/health

# Origen 2
curl -H "Origin: http://localhost:5051" -v http://localhost:8080/api/health

# Origen 3
curl -H "Origin: http://localhost:3000" -v http://localhost:8080/api/health
```

Todos deberían recibir el header `Access-Control-Allow-Origin` con su respectivo origen.

## 📊 Ventajas de Esta Implementación

| Característica | Beneficio |
|----------------|-----------|
| **Centralizado** | Un solo lugar para configurar CORS |
| **Consistente** | Todos los endpoints tienen la misma configuración |
| **Flexible** | Detecta el origen dinámicamente |
| **Seguro** | Permite credenciales de forma segura |
| **Eficiente** | Cache de preflight reduce solicitudes |
| **Mantenible** | Fácil de modificar en el futuro |

## 🔒 Seguridad

### Configuración Actual (Desarrollo)

```java
Access-Control-Allow-Origin: [origen-del-cliente]
Access-Control-Allow-Credentials: true
```

✅ **Ventaja:** Funciona con cualquier frontend  
⚠️ **Consideración:** En producción, considera restringir los orígenes permitidos

### Configuración para Producción (Recomendada)

Si necesitas restringir orígenes en producción, modifica el `CorsFilter.java`:

```java
// Lista de orígenes permitidos
private static final Set<String> ALLOWED_ORIGINS = Set.of(
    "https://aerolinea1.com",
    "https://aerolinea2.com",
    "https://agencia1.com",
    "https://agencia2.com",
    "https://agencia3.com"
);

@Override
public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    String origin = exchange.getRequestHeaders().getFirst("Origin");
    
    if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }
    
    // ... resto del código
}
```

## 🐛 Debugging

### Verificar que el filtro está activo

Añade logs en el `CorsFilter.java`:

```java
@Override
public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    String origin = exchange.getRequestHeaders().getFirst("Origin");
    System.out.println("🔍 CORS Filter - Origen: " + origin);
    
    // ... resto del código
}
```

### Ver headers en el navegador

1. Abre DevTools (F12)
2. Ve a la pestaña **Network**
3. Selecciona una solicitud
4. Ve a la sección **Response Headers**
5. Busca los headers `Access-Control-*`

## 📚 Referencias

- [MDN - CORS](https://developer.mozilla.org/es/docs/Web/HTTP/CORS)
- [MDN - Preflight Request](https://developer.mozilla.org/es/docs/Glossary/Preflight_request)
- [Oracle - HttpServer Filter](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/Filter.html)

## 🔄 Changelog

### 2024-10-22
- ✅ Implementado `CorsFilter.java`
- ✅ Actualizado `App.java` con método helper
- ✅ Aplicado filtro a todos los endpoints
- ✅ Soporte para múltiples puertos
- ✅ Documentación completa


