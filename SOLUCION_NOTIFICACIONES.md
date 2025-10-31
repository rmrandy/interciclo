# 🔧 Solución: Notificaciones No Funcionan

## ❌ Problema Detectado

**El backend Django de agencia-viajes NO está corriendo**

Por eso:
- ❌ Las aerolíneas no pueden enviar webhooks
- ❌ No se crean notificaciones en MongoDB
- ❌ No ves la campana funcionando

---

## ✅ Solución (Sigue estos pasos EN ORDEN)

### **Paso 1: Inicia el Backend Django** ⚠️ MUY IMPORTANTE

```bash
# Abre una NUEVA terminal
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django

# Inicia Django
./scripts/start-django.sh
```

**ESPERA** a ver este mensaje:
```
🌐 Django servirá API en http://192.168.0.X:5001
System check identified no issues (0 silenced).
Starting development server at http://0.0.0.0:5001/
```

✅ **NO CIERRES ESTA TERMINAL** - Django debe quedar corriendo

---

### **Paso 2: Verifica que Django está respondiendo**

En otra terminal:

```bash
curl http://localhost:5001/api/flight-cancellations
```

Deberías ver:
```json
{"success": true, "notifications": [], "unreadCount": 0, "total": 0}
```

✅ Si ves esto → Django funciona correctamente

---

### **Paso 3: Inicia el Frontend**

```bash
# Otra terminal
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/agencia

npm run dev
```

Deberías ver:
```
➜  Local:   http://localhost:5173/
```

---

### **Paso 4: Prueba el Sistema**

#### **Opción A: Crear notificación de prueba (Más Rápido)**

```bash
curl -X POST http://localhost:5001/api/flight-cancellations \
  -H "Content-Type: application/json" \
  -d '{
    "flightId": 9999,
    "flightNumber": "TEST-001",
    "airlineCode": "AEROLINEA3",
    "airlineName": "Aerolínea de Prueba",
    "originCity": "Guatemala",
    "destinationCity": "México",
    "departureDate": "2024-12-25",
    "departureTime": "10:00",
    "cancellationReason": "Prueba del dashboard de notificaciones"
  }'
```

Deberías ver:
```json
{"success": true, "message": "Notificación recibida", "notificationId": "..."}
```

#### **Opción B: Cancelar un vuelo real**

1. **Reinicia UNA aerolínea** (ej: backv6):

```bash
cd backv6
# Ctrl+C para detener
mvn exec:java
```

2. **Cancela un vuelo**:

```bash
curl -X PUT http://localhost:8080/api/airline/flights/ID_VUELO/cancel \
  -H "Content-Type: application/json" \
  -d '{"cancellationReason":"Prueba notificaciones","cancelledBy":1}'
```

3. **Verifica logs** de backv6 - Deberías ver:

```
🔗 Enviando webhook a la agencia...
✅ Webhook enviado exitosamente (HTTP 200)
```

---

### **Paso 5: Ve a la Agencia**

1. Abre: `http://localhost:5173`
2. Inicia sesión como **admin**
3. **Deberías ver**: 🔔**1** en el header (arriba a la derecha)
4. Haz **click en la campana** 🔔
5. Verás el panel desplegable con la notificación

---

## 🔍 Verificación de que TODO está corriendo

Ejecuta esto para verificar:

```bash
# Verifica Django (puerto 5001)
curl -s http://localhost:5001/api/debug/db-info | grep -q "success" && echo "✅ Django OK" || echo "❌ Django NO corriendo"

# Verifica Frontend (puerto 5173)
curl -s http://localhost:5173 | grep -q "html" && echo "✅ Frontend OK" || echo "❌ Frontend NO corriendo"

# Verifica backv4 (puerto 8082)
curl -s http://localhost:8082/api/airline/health | grep -q "status" && echo "✅ backv4 OK" || echo "❌ backv4 NO corriendo"

# Verifica backv5 (puerto 8081)
curl -s http://localhost:8081/api/airline/health | grep -q "status" && echo "✅ backv5 OK" || echo "❌ backv5 NO corriendo"

# Verifica backv6 (puerto 8080)
curl -s http://localhost:8080/api/airline/health | grep -q "status" && echo "✅ backv6 OK" || echo "❌ backv6 NO corriendo"
```

---

## 📋 Estado Actual de tus Terminales

Deberías tener **5 terminales abiertas**:

| Terminal | Servicio | Puerto | Comando |
|----------|----------|--------|---------|
| 1 | Django backend | 5001 | `./scripts/start-django.sh` |
| 2 | Frontend React | 5173 | `npm run dev` |
| 3 | backv4 (opcional) | 8082 | `mvn exec:java` |
| 4 | backv5 (opcional) | 8081 | `mvn exec:java` |
| 5 | backv6 (opcional) | 8080 | `mvn exec:java` |

---

## 🚨 LO MÁS IMPORTANTE

**El backend Django DEBE estar corriendo** para recibir webhooks.

Si no ves:
```
Starting development server at http://0.0.0.0:5001/
```

Es porque **Django NO está corriendo** y por eso NO funciona.

---

## 🧪 Prueba Rápida (Una vez Django esté corriendo)

```bash
# 1. Verifica que Django responde
curl http://localhost:5001/api/flight-cancellations

# 2. Crea notificación de prueba
curl -X POST http://localhost:5001/api/flight-cancellations \
  -H "Content-Type: application/json" \
  -d '{"flightId":999,"flightNumber":"TEST-001","airlineCode":"TEST","airlineName":"Prueba","originCity":"A","destinationCity":"B","departureDate":"2024-12-25","departureTime":"10:00","cancellationReason":"Prueba"}'

# 3. Verifica que se creó
curl http://localhost:5001/api/flight-cancellations
```

Si ves `"notifications": [...]` con contenido → ✅ **Funciona**

Luego refresca `http://localhost:5173` y deberías ver 🔔**1**

---

**¿Está Django corriendo ahora?** Dime qué ves cuando ejecutas:

```bash
cd agencia-viajes/backend-django
./scripts/start-django.sh
```

Una vez que Django esté corriendo, te ayudo a copiar todo a `agencia-viajes2`. 😊
