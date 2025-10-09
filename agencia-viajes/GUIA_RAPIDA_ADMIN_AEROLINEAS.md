# 🚀 Guía Rápida: Configurar Aerolíneas desde el Admin

## ✅ Problema Resuelto

**Antes**: IP hardcodeada `172.16.56.36` que no funcionaba  
**Ahora**: Configuración dinámica desde el panel de administración ✅

---

## 📋 Pasos para Configurar

### 1. Abre el Panel de Admin
```
URL: http://localhost:5001/admin
Pestaña: "Aerolíneas"
```

### 2. Completa el Formulario

**Campos requeridos:**
- **Nombre**: Nombre descriptivo (ej: "Servidor Principal")
- **Código**: 2-3 letras (ej: "SP", "PROD", "DEV")
- **Protocolo**: Selecciona `http` o `https`
- **Host/IP**: La IP correcta de tu servidor backend Java
  - Ejemplo: `192.168.0.2`
  - O: `localhost` (si está en la misma máquina)
- **Puerto**: `8080` (o el que uses para el backend Java)
- **Base path**: `/api` (mantén este valor)

**Endpoints** (generalmente mantén estos valores):
- **Endpoint búsqueda**: `/search`
- **Endpoint compra**: `/book`
- **Endpoint cancelación**: `/cancel`
- **Endpoint health**: `/health`

**Configuración adicional:**
- **Timeout (ms)**: `2000` (2 segundos, aumenta si hay problemas de red)
- **Activo**: ✅ **Sí** (MUY IMPORTANTE - solo la activa se usa)
- **Notas**: Descripción opcional

### 3. Guarda
Clic en "Crear aerolínea"

### 4. ¡Listo!
**La configuración se usa inmediatamente** sin necesidad de reiniciar nada ✅

---

## 💡 Ejemplo de Configuración Correcta

Para un servidor backend Java local:

```
┌─────────────────────────────────────────┐
│ Nombre:         Servidor Local          │
│ Código:         LOC                      │
│ Protocolo:      http                     │
│ Host/IP:        192.168.0.2             │
│ Puerto:         8080                     │
│ Base path:      /api                     │
│ ─────────────────────────────────────── │
│ Endpoint búsqueda:     /search          │
│ Endpoint compra:       /book            │
│ Endpoint cancelación:  /cancel          │
│ Endpoint health:       /health          │
│ ─────────────────────────────────────── │
│ Timeout (ms):   2000                     │
│ Activo:         ✅ Sí                    │
│ Notas:          Backend Java local       │
└─────────────────────────────────────────┘
```

**Resultado**: Todas las llamadas irán a `http://192.168.0.2:8080/api/...`

---

## 🔍 Cómo Saber Si Funciona

### 1. Verifica en Logs del Backend Django

Cuando hagas una búsqueda de vuelos, verás en la consola de Django:

```bash
✅ Usando aerolínea de BD: Servidor Local (http://192.168.0.2:8080/api)
```

### 2. Verifica el Endpoint
```bash
curl http://localhost:5001/api/airlines/active
```

**Debe mostrar**:
```json
{
  "success": true,
  "data": {
    "name": "Servidor Local",
    "host": "192.168.0.2",
    "port": 8080,
    ...
  },
  "fullUrl": "http://192.168.0.2:8080/api"
}
```

### 3. Prueba Buscar Ciudades

En el navegador, ve a la sección de búsqueda. Si ves el error:
```
Connection to 172.16.56.36 timed out
```

Significa que aún estaba usando la IP vieja. **Soluciones**:
1. Verifica que la aerolínea esté marcada como "Activo: Sí"
2. Refresca la página (Cmd+R o F5)
3. Reinicia el backend Django si es necesario

Si ves las ciudades correctamente: **¡Funciona!** ✅

---

## 🔄 Cambiar Entre Servidores

### Tienes Varios Servidores Configurados

```
Aerolínea 1: Desarrollo
- Host: localhost
- Puerto: 8080
- Activo: No

Aerolínea 2: Testing
- Host: 192.168.0.2
- Puerto: 8080
- Activo: Sí ← Esta se está usando

Aerolínea 3: Producción
- Host: 172.16.56.36
- Puerto: 8080
- Activo: No
```

### Para Cambiar a Producción

1. Edita "Aerolínea 2" (Testing) → Cambia "Activo" a "No"
2. Edita "Aerolínea 3" (Producción) → Cambia "Activo" a "Sí"
3. **Listo** - La siguiente llamada usa producción ✅

No necesitas:
- ❌ Reiniciar el servidor
- ❌ Editar código
- ❌ Cambiar variables de entorno
- ❌ Recompilar nada

---

## 🎯 Tips Importantes

### ✅ DO (Haz esto)
1. **Siempre** marca "Activo: Sí" en la aerolínea que quieres usar
2. Verifica el Host/IP antes de guardar
3. Usa puerto 8080 si es el backend Java estándar
4. Mantén "/api" como base path

### ❌ DON'T (No hagas esto)
1. No tengas múltiples aerolíneas con "Activo: Sí" (solo se usa la primera)
2. No uses IPs incorrectas
3. No olvides marcar como "Activo"
4. No uses timeouts muy bajos (mínimo 1000ms)

---

## 🧪 Prueba Rápida

### Paso 1: Configurar
```
Nombre: Prueba Rápida
Código: PR
Host/IP: [LA IP DE TU BACKEND JAVA]
Puerto: 8080
Activo: Sí
```

### Paso 2: Guardar
Clic en "Crear aerolínea"

### Paso 3: Verificar
```bash
curl http://localhost:5001/api/airlines/active
```

### Paso 4: Probar
En el navegador, busca vuelos. Si ves resultados: **¡Funciona!** ✅

---

## ❓ Preguntas Frecuentes

**P: ¿Puedo tener varias aerolíneas configuradas?**  
R: Sí, pero solo una puede estar "Activa" (enabled=true) a la vez.

**P: ¿Necesito reiniciar después de cambiar la configuración?**  
R: No, los cambios se aplican inmediatamente en la siguiente llamada.

**P: ¿Qué pasa si no hay aerolínea activa?**  
R: El sistema usa variables de entorno como fallback (host detectado automáticamente).

**P: ¿Cómo sé qué aerolínea se está usando?**  
R: Revisa los logs del backend Django, mostrará: "✅ Usando aerolínea de BD: [nombre]"

**P: ¿Puedo usar HTTPS?**  
R: Sí, selecciona "https" en el protocolo y asegúrate que el servidor soporte HTTPS.

---

## 📊 Campos del Formulario Explicados

| Campo | Descripción | Ejemplo |
|-------|-------------|---------|
| **Nombre** | Nombre descriptivo | "Servidor Principal" |
| **Código** | Identificador corto | "SP" |
| **Protocolo** | http o https | "http" |
| **Host/IP** | Dirección del servidor | "192.168.0.2" |
| **Puerto** | Puerto del servidor | "8080" |
| **Base path** | Ruta base de la API | "/api" |
| **Endpoint búsqueda** | Ruta para buscar | "/search" |
| **Endpoint compra** | Ruta para comprar | "/book" |
| **Endpoint cancelación** | Ruta para cancelar | "/cancel" |
| **Endpoint health** | Health check | "/health" |
| **Timeout (ms)** | Tiempo de espera | "2000" |
| **Activo** | ¿Usar esta config? | ✅ Sí |
| **Notas** | Comentarios | "Servidor de pruebas" |

---

**Documentación completa**: `CONFIGURACION_AEROLINEAS_DINAMICA.md`  
**Estado**: ✅ Todo funcionando  
**Configuración**: Dinámica desde admin  

## 🎉 ¡Ahora puedes cambiar la IP desde el admin sin tocar código!

