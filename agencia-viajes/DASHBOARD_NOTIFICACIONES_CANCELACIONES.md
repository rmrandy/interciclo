# 🔔 Dashboard de Notificaciones de Vuelos Cancelados

## ✨ Sistema Completo Implementado

### Para Administradores de Agencia

Ahora los administradores de la agencia pueden:

1. ✅ **Ver notificaciones en tiempo real** con badge en el header (🔔)
2. ✅ **Panel desplegable** con las últimas notificaciones
3. ✅ **Página de historial completo** con filtros y búsqueda
4. ✅ **Marcar como leídas** individual o todas a la vez
5. ✅ **Eliminar notificaciones** que ya no sean relevantes
6. ✅ **Actualización automática** cada 30 segundos

---

## 📊 Características del Sistema

### 1. **Campana de Notificaciones en el Header** 🔔

- **Ubicación**: Header, solo visible para administradores
- **Badge rojo**: Muestra el número de notificaciones sin leer (ej: 🔔**3**)
- **Click**: Despliega panel con las últimas 10 notificaciones
- **Auto-refresh**: Se actualiza cada 30 segundos automáticamente

### 2. **Panel Desplegable**

```
┌──────────────────────────────────────┐
│ 🚨 Vuelos Cancelados    [Marcar todas]│
├──────────────────────────────────────┤
│ ⚠️  AA-123 | AEROLINEA3              │
│     Guatemala → México                │
│     Motivo: Mal clima                 │
│     Hace 15 min              [Eliminar]│
├──────────────────────────────────────┤
│ ⚠️  BB-456 | AEROLINEA2              │
│     Santo Domingo → Panamá            │
│     Motivo: Mantenimiento             │
│     Hace 2 h                 [Eliminar]│
├──────────────────────────────────────┤
│      Ver todas las notificaciones →  │
└──────────────────────────────────────┘
```

**Características**:
- Color de fondo rojo claro para no leídas
- Click en notificación → Marcar como leída
- Botón "Marcar todas leídas" (aparece si hay no leídas)
- Link a página completa

### 3. **Página de Historial Completo** (`/admin/cancelled-flights`)

**Filtros disponibles**:
- **Todas**: Muestra todas las notificaciones
- **No leídas**: Solo las que no has marcado como leídas
- **Leídas**: Solo las que ya revisaste

**Información mostrada**:
- Número de vuelo
- Aerolínea
- Ruta (origen → destino)
- Fecha programada
- Fecha de cancelación
- Motivo detallado
- Acciones: Marcar leída, Eliminar

---

## 🗄️ Base de Datos MongoDB

### Colección: `flight_cancellations`

```javascript
{
  "_id": ObjectId("..."),
  "flightId": 123,
  "flightNumber": "AA-123",
  "airlineId": "672...",
  "airlineCode": "AEROLINEA3",
  "airlineName": "Aerolínea Nacional",
  "originCity": "Guatemala",
  "destinationCity": "México",
  "departureDate": "2024-11-20",
  "departureTime": "14:30",
  "cancellationReason": "Condiciones meteorológicas adversas",
  "cancelledBy": 41,
  "cancelledAt": "2024-10-31T10:30:00",
  "status": "unread",  // unread, read, archived
  "readAt": null,      // Se llena cuando se marca como leída
  "createdAt": "2024-10-31T10:30:00"
}
```

---

## 🔌 Endpoints API

### Backend Django (`http://localhost:5001/api/`)

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/flight-cancellations` | GET | Obtener notificaciones (con filtros) |
| `/flight-cancellations` | POST | Recibir notificación (webhook) |
| `/flight-cancellations/{id}/read` | PUT | Marcar como leída |
| `/flight-cancellations/mark-all-read` | PUT | Marcar todas como leídas |
| `/flight-cancellations/{id}` | DELETE | Eliminar notificación |

### Ejemplos de Uso:

#### Obtener notificaciones no leídas:
```bash
curl http://localhost:5001/api/flight-cancellations?status=unread
```

#### Marcar como leída:
```bash
curl -X PUT http://localhost:5001/api/flight-cancellations/672.../read
```

#### Recibir notificación (webhook):
```bash
curl -X POST http://localhost:5001/api/flight-cancellations \
  -H "Content-Type: application/json" \
  -d '{
    "flightId": 123,
    "flightNumber": "AA-123",
    "airlineCode": "AEROLINEA3",
    "airlineName": "Aerolínea Nacional",
    "originCity": "Guatemala",
    "destinationCity": "México",
    "departureDate": "2024-11-20",
    "departureTime": "14:30",
    "cancellationReason": "Mal clima"
  }'
```

---

## 🚀 Cómo Usar (Para Administradores)

### 1. **Ver Notificaciones en el Header**

1. Inicia sesión como **administrador**
2. Verás la campana 🔔 en el header
3. Si hay notificaciones sin leer, verás un badge rojo con el número

### 2. **Panel Desplegable**

1. Haz **click en la campana** 🔔
2. Se abre el panel con las últimas 10 notificaciones
3. **Click en una notificación** → Se marca como leída
4. **Marcar todas leídas** → Limpia el badge

### 3. **Página de Historial Completo**

1. En el panel, click en **"Ver todas las notificaciones →"**
2. O ve directamente a: **Admin** > **🚨 Vuelos Cancelados**
3. Usa los filtros para organizar las notificaciones
4. Marca como leídas o elimina las que ya no necesites

---

## 🔗 Integración con Aerolíneas (Webhook)

### Configurar Webhook en las Aerolíneas

Para que las aerolíneas envíen notificaciones automáticamente a la agencia:

#### En `backv4/backv5/backv6`:

Modifica `RealEmailService.java` para agregar un método que llame al webhook:

```java
private void sendWebhookToAgency(Flight flight, String cancellationReason) {
    try {
        String agenciaWebhookUrl = "http://localhost:5001/api/flight-cancellations";
        
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = String.format("""
            {
                "flightId": %d,
                "flightNumber": "%s",
                "airlineCode": "AEROLINEA3",
                "airlineName": "Aerolínea Nacional",
                "originCity": "%s",
                "destinationCity": "%s",
                "departureDate": "%s",
                "departureTime": "%s",
                "cancellationReason": "%s"
            }
            """,
            flight.getIdFlight(),
            flight.getFlightNumber(),
            flight.getOriginCity().getName(),
            flight.getDestinationCity().getName(),
            flight.getDepartureDate(),
            flight.getDepartureTime(),
            cancellationReason
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(agenciaWebhookUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        
        client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("✅ Webhook enviado a la agencia");
        
    } catch (Exception e) {
        System.err.println("⚠️ Error enviando webhook: " + e.getMessage());
    }
}
```

Y llámalo desde `sendCancellationNotificationsToAgencies()`:

```java
// Después de enviar correos
sendWebhookToAgency(flight, cancellationReason);
```

---

## 📝 Prueba Manual (Sin Aerolíneas)

Para probar el sistema sin esperar a que cancelen vuelos:

### 1. Crear notificación de prueba:

```bash
curl -X POST http://localhost:5001/api/flight-cancellations \
  -H "Content-Type: application/json" \
  -d '{
    "flightId": 999,
    "flightNumber": "TEST-001",
    "airlineCode": "AEROLINEA3",
    "airlineName": "Aerolínea de Prueba",
    "originCity": "Ciudad A",
    "destinationCity": "Ciudad B",
    "departureDate": "2024-12-25",
    "departureTime": "10:00",
    "cancellationReason": "Prueba del sistema de notificaciones"
  }'
```

### 2. Recarga la página de la agencia

- Verás la campana con badge 🔔**1**
- Click en la campana → Verás la notificación
- Ve a `/admin/cancelled-flights` → Verás el historial

---

## 🎨 Interfaz de Usuario

### Colores y Estados:

| Estado | Color de Fondo | Border | Badge |
|--------|----------------|--------|-------|
| No leída | #fef2f2 (rojo claro) | #dc2626 (rojo) | NUEVO (rojo) |
| Leída | white | #e5e7eb (gris) | - |

### Iconos:

- 🔔 Campana de notificaciones
- ⚠️ Vuelo cancelado
- ✓ Marcar como leída
- 🗑️ Eliminar
- 🚨 Vuelos cancelados (página)

---

## 📋 Archivos Creados/Modificados

### Backend:
1. ✅ `/backend-django/api/views.py`
   - Agregados endpoints de notificaciones
2. ✅ `/backend-django/api/urls.py`
   - Rutas de notificaciones

### Frontend:
1. ✅ `/agencia/src/components/NotificationBell.jsx`
   - Componente de campana con dropdown
2. ✅ `/agencia/src/pages/CancelledFlights.jsx`
   - Página de historial completo
3. ✅ `/agencia/src/services/api.js`
   - Cliente API para notificaciones
4. ✅ `/agencia/src/layouts/MainLayout.jsx`
   - Integración de campana en header
5. ✅ `/agencia/src/App.jsx`
   - Ruta protegida para admin
6. ✅ `/agencia/src/pages/Admin.jsx`
   - Link al dashboard de cancelaciones

---

## 🎯 Flujo Completo

### Cuando se cancela un vuelo:

1. 🚫 **Aerolínea cancela** el vuelo (backv4/5/6)
2. 📧 **Envía correo** a las agencias
3. 🔗 **Envía webhook** a la agencia (opcional)
4. 💾 **Se guarda** en MongoDB (`flight_cancellations`)
5. 🔔 **Badge aparece** en el header de la agencia
6. 👨‍💼 **Admin ve** la notificación
7. ✓ **Admin marca** como leída
8. ✅ **Sistema actualizado**

---

## 🧪 Cómo Probar

### 1. Inicia backend Django:
```bash
cd agencia-viajes/backend-django
./scripts/start-django.sh
```

### 2. Inicia frontend:
```bash
cd agencia-viajes/agencia
npm run dev
```

### 3. Crea notificación de prueba:
```bash
curl -X POST http://localhost:5001/api/flight-cancellations \
  -H "Content-Type: application/json" \
  -d '{
    "flightId": 999,
    "flightNumber": "PRUEBA-123",
    "airlineCode": "TEST",
    "airlineName": "Aerolínea de Prueba",
    "originCity": "Guatemala",
    "destinationCity": "México",
    "departureDate": "2024-12-25",
    "departureTime": "14:30",
    "cancellationReason": "Prueba del sistema de notificaciones"
  }'
```

### 4. Ve a la agencia:
- Inicia sesión como **admin**
- Verás 🔔**1** en el header
- Click en la campana → Verás la notificación
- Click en "Ver todas" → Historial completo

---

## 🔒 Seguridad

- ✅ **Solo administradores** pueden ver notificaciones
- ✅ **Rutas protegidas** con `ProtectedRoute`
- ✅ **Validación en backend** de campos requeridos
- ✅ **Sanitización** de datos en MongoDB

---

## 📱 Responsive

El sistema es **completamente responsive**:
- Panel se ajusta en móviles
- Badge visible en todas las resoluciones
- Tabla responsiva en historial

---

## 🎨 Personalización

### Cambiar colores:

En `NotificationBell.jsx` y `CancelledFlights.jsx`:

```jsx
// Rojo de alerta
'#dc2626' → '#tu-color'

// Fondo de no leídas
'#fef2f2' → '#tu-color'
```

### Cambiar intervalo de actualización:

En `NotificationBell.jsx`, línea ~71:

```jsx
const interval = setInterval(loadNotifications, 30000); // 30 segundos
// Cambiar a 60000 para 1 minuto
// Cambiar a 120000 para 2 minutos
```

---

## 📊 Estadísticas (Futuras)

Puedes agregar:
- Contador de cancelaciones por aerolínea
- Gráfico de cancelaciones por mes
- Motivos más comunes de cancelación
- Aerolíneas con más cancelaciones

---

## 🚀 Próximos Pasos

### Ya Implementado:
- [x] Endpoints backend
- [x] Campana en header
- [x] Panel desplegable
- [x] Página de historial
- [x] Marcar como leída
- [x] Eliminar notificaciones
- [x] Auto-refresh

### Pendiente (Opcional):
- [ ] Webhook automático desde aerolíneas
- [ ] Notificaciones push del navegador
- [ ] Sonido al recibir nueva notificación
- [ ] Dashboard con estadísticas
- [ ] Exportar reporte PDF

---

## 📞 Rutas de Acceso

| Ruta | Descripción | Acceso |
|------|-------------|--------|
| `/` | Header con campana 🔔 | Solo admins |
| `/admin/cancelled-flights` | Historial completo | Solo admins |
| `/admin` | Panel de admin con link | Solo admins |

---

## ✅ Verificación

Para verificar que todo funciona:

1. ✅ Backend Django corriendo
2. ✅ Frontend corriendo
3. ✅ Logueado como admin
4. ✅ Campana visible en header
5. ✅ Crear notificación de prueba
6. ✅ Ver badge con número
7. ✅ Click en campana → Ver panel
8. ✅ Click en notificación → Se marca leída
9. ✅ Ver historial completo

---

**Estado**: ✅ Completamente funcional  
**Última actualización**: 31/10/2024  
**Versión**: 1.0

