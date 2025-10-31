# 🔔 Sistema Completo de Notificaciones de Vuelos Cancelados

## ✨ Cómo Funciona el Sistema Integrado

### Flujo Completo (3 Aerolíneas → 1 Agencia):

```
┌─────────────────────────────────────────────────────────────┐
│                    AEROLÍNEAS                                │
├─────────────────────────────────────────────────────────────┤
│  backv4 (AEROLINEA2)  backv5 (AEROLINEA3)  backv6 (AEROLINEA4) │
│     Puerto 8082          Puerto 8081          Puerto 8080    │
└──────────┬──────────────────┬──────────────────┬────────────┘
           │                  │                  │
           │ Cancelan Vuelo   │                  │
           └──────────────────┴──────────────────┘
                              │
                              ▼
           ┌──────────────────────────────────────┐
           │   1. Guarda en Oracle (CANCELLED)    │
           │   2. Envía CORREO HTML a agencias    │
           │   3. Envía WEBHOOK a la agencia      │
           └──────────────────┬───────────────────┘
                              │
                              ▼
           ┌──────────────────────────────────────┐
           │    AGENCIA DE VIAJES (agencia-viajes)│
           │    Backend Django (Puerto 5001)      │
           ├──────────────────────────────────────┤
           │  Recibe webhook en:                  │
           │  POST /api/flight-cancellations      │
           │                                       │
           │  Guarda en MongoDB:                  │
           │  Colección: flight_cancellations     │
           └──────────────────┬───────────────────┘
                              │
                              ▼
           ┌──────────────────────────────────────┐
           │    FRONTEND (React)                  │
           │    Puerto 5173                       │
           ├──────────────────────────────────────┤
           │  • Campana en header: 🔔 3          │
           │  • Panel desplegable                 │
           │  • Página de historial               │
           │  • Auto-refresh cada 30 seg          │
           └──────────────────────────────────────┘
```

---

## 🚀 Pasos para que Funcione

### 1. **Backend Django debe estar corriendo**

```bash
cd agencia-viajes/backend-django
./scripts/start-django.sh
```

✅ Debe estar en: `http://localhost:5001`

### 2. **Frontend debe estar corriendo**

```bash
cd agencia-viajes/agencia
npm run dev
```

✅ Debe estar en: `http://localhost:5173`

### 3. **Reiniciar las 3 aerolíneas**

**IMPORTANTE**: Reinicia para que usen el código nuevo con webhook

```bash
# Terminal 1 - backv4
cd backv4
mvn clean compile
mvn exec:java

# Terminal 2 - backv5
cd backv5
mvn clean compile
mvn exec:java

# Terminal 3 - backv6
cd backv6
mvn clean compile
mvn exec:java
```

### 4. **Cancelar un vuelo de cualquier aerolínea**

```bash
# Desde backv4 (AEROLINEA2 - Puerto 8082)
curl -X PUT http://localhost:8082/api/airline/flights/ID_VUELO/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Prueba de notificaciones AEROLINEA2",
    "cancelledBy": 1
  }'

# Desde backv5 (AEROLINEA3 - Puerto 8081)
curl -X PUT http://localhost:8081/api/airline/flights/ID_VUELO/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Prueba de notificaciones AEROLINEA3",
    "cancelledBy": 1
  }'

# Desde backv6 (AEROLINEA4 - Puerto 8080)
curl -X PUT http://localhost:8080/api/airline/flights/ID_VUELO/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Prueba de notificaciones AEROLINEA4",
    "cancelledBy": 1
  }'
```

---

## 📊 Logs que Verás en las Aerolíneas

Cuando canceles un vuelo, verás:

```
=================================================
🚀 FlightDAO.cancelFlight() - INICIANDO CANCELACIÓN [AEROLINEAX]
=================================================

🚀 === INICIANDO ENVÍO DE NOTIFICACIONES DE CANCELACIÓN ===
✅ Configuración de correo cargada desde classpath

📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A PASAJEROS ===
📧 Pasajeros a notificar: 2
📧 ✅ Correo enviado exitosamente a: pasajero@email.com

📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A AGENCIAS ===
📧 Vuelo cancelado: AA-123

🔗 Enviando webhook a la agencia...
💡 Usando URL por defecto: http://localhost:5001/api/flight-cancellations
📤 Enviando a: http://localhost:5001/api/flight-cancellations
✅ Webhook enviado exitosamente (HTTP 200)
📥 Respuesta: {"success": true, "message": "Notificación recibida", ...}
✅ Webhook enviado exitosamente - Notificación creada en el dashboard de la agencia

📧 Agencias a notificar por correo: 1
📧 ✅ Correo enviado exitosamente a: rr36693904@gmail.com

🚀 === FIN DE ENVÍO DE NOTIFICACIONES ===
```

---

## 📧 En el Dashboard de la Agencia

### 1. **Header con Campana** 🔔

- Verás la campana en el header (solo si eres admin)
- Badge rojo con número: 🔔**3**
- Click → Abre panel desplegable

### 2. **Panel Desplegable**

- Últimas 10 notificaciones
- Click en notificación → Marca como leída
- Botón "Marcar todas leídas"
- Link "Ver todas"

### 3. **Página de Historial** (`/admin/cancelled-flights`)

- Todas las cancelaciones
- Filtros: Todas / No leídas / Leídas
- Acciones: Marcar leída / Eliminar

---

## 🗄️ Colección MongoDB: `flight_cancellations`

```javascript
{
  "_id": ObjectId("..."),
  "flightId": 123,
  "flightNumber": "AA-123",
  "airlineCode": "AEROLINEA3",      // ← Identifica de qué aerolínea viene
  "airlineName": "Aerolínea Nacional 3",
  "originCity": "Guatemala",
  "destinationCity": "México",
  "departureDate": "2024-11-20",
  "departureTime": "14:30",
  "cancellationReason": "Mal clima",
  "cancelledBy": 41,
  "cancelledAt": "2024-10-31T10:30:00",
  "status": "unread",               // ← Estado: unread, read, archived
  "readAt": null,
  "createdAt": "2024-10-31T10:30:00"
}
```

✅ **Funciona con TODAS las aerolíneas** que tengas registradas

---

## 🧪 Prueba Rápida (Crear Notificación Manual)

Si quieres probar el dashboard SIN cancelar vuelos reales:

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

**Luego**:
1. Ve a: `http://localhost:5173`
2. Inicia sesión como **admin**
3. Verás 🔔**1** en el header
4. Click en la campana → Verás la notificación

---

## ⚙️ Configuración (Opcional)

### Cambiar URL del Webhook

Por defecto usa: `http://localhost:5001/api/flight-cancellations`

Para cambiar (si la agencia está en otro servidor):

```bash
# En cada aerolínea (backv4, backv5, backv6)
export AGENCIA_WEBHOOK_URL=http://192.168.0.10:5001/api/flight-cancellations
```

---

## 📋 Checklist de Verificación

- [ ] ✅ Backend Django corriendo (puerto 5001)
- [ ] ✅ Frontend corriendo (puerto 5173)
- [ ] ✅ backv4/5/6 recompilados y reiniciados
- [ ] ✅ Cancelar un vuelo en cualquier aerolínea
- [ ] ✅ Ver logs del webhook en la consola
- [ ] ✅ Ver campana con badge en la agencia
- [ ] ✅ Click en campana → Ver notificación
- [ ] ✅ Marcar como leída
- [ ] ✅ Ver historial completo

---

## 🎯 ¿Qué Sucede Cuando Cancelas un Vuelo?

### En la Aerolínea (backv4/5/6):
1. ✅ Marca el vuelo como `CANCELLED` en Oracle
2. ✅ Envía **correo HTML** a las agencias
3. ✅ Envía **webhook** a la agencia (crea notificación en dashboard)
4. ✅ El vuelo **NO aparece** más en búsquedas

### En la Agencia:
1. ✅ Recibe el webhook
2. ✅ Guarda la notificación en MongoDB
3. ✅ Badge aparece en el header 🔔**1**
4. ✅ El vuelo **NO aparece** en búsquedas
5. ✅ Admin puede ver y gestionar la notificación

---

## 📝 Endpoints de la Agencia

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/flight-cancellations` | GET | Listar notificaciones |
| `/api/flight-cancellations` | POST | Recibir webhook (desde aerolíneas) |
| `/api/flight-cancellations/{id}/read` | PUT | Marcar como leída |
| `/api/flight-cancellations/mark-all-read` | PUT | Marcar todas como leídas |
| `/api/flight-cancellations/{id}` | DELETE | Eliminar notificación |

---

## ❌ Solución de Problemas

### Problema: No aparecen notificaciones

**Verifica**:
1. ✅ **Backend Django corriendo** en puerto 5001
2. ✅ **Aerolíneas reiniciadas** con código nuevo
3. ✅ **Webhook se está enviando** (ver logs de aerolínea)
4. ✅ **Webhook se recibe** (ver logs de Django)

### Problema: Webhook falla

Verás en la consola de la aerolínea:

```
⚠️ No se pudo conectar con la agencia (¿está el backend corriendo?)
```

**Solución**: Verifica que Django esté corriendo en puerto 5001

### Problema: Badge no actualiza

**Solución**: El badge se actualiza cada 30 segundos. Refresca la página (F5) para ver cambios inmediatos.

---

## 🎨 Personalización

### Cambiar intervalo de actualización:

`agencia/src/components/NotificationBell.jsx` (línea ~71):

```jsx
const interval = setInterval(loadNotifications, 30000); // 30 segundos
```

### Cambiar número de notificaciones en el panel:

`agencia/src/components/NotificationBell.jsx` (línea ~22):

```jsx
const response = await flightCancellationsApi.list({ limit: 10 });
// Cambiar 10 por el número que quieras
```

---

## 📊 Resumen de Archivos

### Backend Django:
- ✅ `api/views.py` - Endpoints de notificaciones
- ✅ `api/urls.py` - Rutas

### Frontend React:
- ✅ `components/NotificationBell.jsx` - Campana con dropdown
- ✅ `pages/CancelledFlights.jsx` - Historial completo
- ✅ `services/api.js` - Cliente API
- ✅ `layouts/MainLayout.jsx` - Integración en header
- ✅ `App.jsx` - Ruta protegida

### Aerolíneas (backv4, backv5, backv6):
- ✅ `services/RealEmailService.java` - Webhook + correos
- ✅ `dao/FlightDAO.java` - Exclusión de cancelados

---

## 🔐 Seguridad

- ✅ Solo **administradores** ven notificaciones
- ✅ Rutas protegidas con `ProtectedRoute`
- ✅ Validación en backend

---

## 🎯 Próximo Paso: PRUEBA AHORA

### Opción A: Cancelar un vuelo real

1. **Reinicia** backv4, backv5 o backv6
2. **Cancela un vuelo**
3. **Ve a la agencia** → Verás 🔔**1**

### Opción B: Crear notificación de prueba

```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes
./test-notification.sh
```

---

## ✅ Ventajas del Sistema

1. ✅ **Integrado con TODAS las aerolíneas** (backv4, backv5, backv6)
2. ✅ **Notificaciones en tiempo real** (webhook)
3. ✅ **Correo HTML** de respaldo
4. ✅ **Dashboard visual** para administradores
5. ✅ **Historial completo** con filtros
6. ✅ **Vuelos excluidos automáticamente** de búsquedas
7. ✅ **Auto-actualización** cada 30 segundos

---

**Estado**: ✅ Completamente funcional  
**Última actualización**: 31/10/2024  
**Versión**: 1.0  
**Funciona con**: backv4, backv5, backv6 → agencia-viajes

