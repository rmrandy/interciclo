# 📧 Notificaciones de Cancelación de Vuelos

## ✨ Funcionalidades Implementadas

### 1. **Envío de Notificaciones a Agencias**

Cuando se cancela un vuelo en `backv6` (AEROLINEA4), el sistema **automáticamente**:

✅ Envía notificaciones por correo a las **agencias de viajes registradas**  
✅ Notifica a los **pasajeros con reservas** en ese vuelo  
✅ **Oculta el vuelo cancelado** de todas las búsquedas  
✅ Registra el **motivo de cancelación** y quién lo realizó  

---

## 🔧 Configuración

### Opción 1: Correos Hardcoded (Por Defecto)

Los correos de las agencias están configurados directamente en el código:

**Archivo**: `backv6/src/main/java/com/sources/app/services/SimpleEmailService.java`

```java
// Línea 228-230 (aproximadamente)
if (emails.isEmpty()) {
    System.out.println("⚠️ Usando correos de agencias de ejemplo");
    emails.add("agencia@example.com");
    emails.add("reservas@viajeselmundo.com");
    // Agregar más correos aquí:
    emails.add("tu-agencia@correo.com");
}
```

### Opción 2: Desde Variable de Entorno (Recomendado para Producción)

Configura la URL del backend de la agencia para obtener los correos dinámicamente:

```bash
# En Windows
set AGENCIA_BACKEND_URL=http://localhost:5001

# En Linux/Mac
export AGENCIA_BACKEND_URL=http://localhost:5001
```

El sistema intentará obtener los correos de las agencias consultando:
```
GET http://localhost:5001/api/airlines
```

---

## 📨 Contenido del Correo a Agencias

Cuando se cancela un vuelo, las agencias reciben un correo con:

```
==========================================
NOTIFICACIÓN DE CANCELACIÓN DE VUELO
PARA AGENCIAS DE VIAJES
==========================================

Estimada Agencia de Viajes,

Le informamos que el siguiente vuelo ha sido CANCELADO:

DETALLES DEL VUELO CANCELADO:
• Número de Vuelo: AA-1234
• Ruta: Ciudad A → Ciudad B
• Fecha Programada: 15/11/2024
• Hora Programada: 14:30
• Estado: CANCELADO

MOTIVO DE LA CANCELACIÓN:
Condiciones meteorológicas adversas

FECHA Y HORA DE CANCELACIÓN:
31/10/2024 10:45:30

ACCIONES REQUERIDAS:
• Este vuelo ya NO ESTÁ DISPONIBLE para nuevas reservas
• Contactar a sus clientes con reservas existentes
• Procesar reembolsos según políticas de la aerolínea
• Actualizar su sistema de búsqueda de vuelos

INFORMACIÓN DE CONTACTO:
• Servicio al Cliente: +502 1234-5678
• Email Corporativo: corporate@aerolinea.com
• Soporte Agencias: 24/7

Este vuelo ha sido removido automáticamente de:
✓ Sistema de búsqueda de vuelos
✓ Inventario disponible
✓ Todas las plataformas de venta

Disculpe las molestias causadas.

Atentamente,
Sistema de Gestión de Vuelos - AEROLINEA4
==========================================
```

---

## 🚫 Exclusión Automática de Vuelos Cancelados

### Búsquedas Afectadas

Los vuelos cancelados **NO aparecerán** en:

1. **Búsqueda de vuelos** (`/airline/flights`)
   - Búsqueda por origen y destino
   - Búsqueda por fecha
   - Búsqueda por ruta

2. **Listado de todos los vuelos** (`GET /airline/flights`)
   - Solo muestra vuelos activos

3. **Búsquedas de la agencia**
   - Las agencias NO verán vuelos cancelados
   - Se evitan reservas en vuelos cancelados

### Implementación Técnica

Los métodos modificados en `FlightDAO.java`:

```java
// searchFlights()
WHERE 1=1 
AND (f.status IS NULL OR f.status != 'CANCELLED')

// getAllFlights()
WHERE (f.status IS NULL OR f.status != 'CANCELLED')

// getFlightsByDate()
WHERE f.departureDate = :date 
AND (f.status IS NULL OR f.status != 'CANCELLED')

// getFlightsByRoute()
WHERE f.originCity.idCity = :originCityId 
AND (f.status IS NULL OR f.status != 'CANCELLED')
```

---

## 🎯 Cómo Cancelar un Vuelo

### Desde la API

```bash
PUT http://localhost:8080/api/airline/flights/{flightId}/cancel
Content-Type: application/json

{
  "cancellationReason": "Condiciones meteorológicas adversas",
  "cancelledBy": 1
}
```

### Ejemplo con cURL

```bash
curl -X PUT http://localhost:8080/api/airline/flights/123/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Mantenimiento de emergencia en la aeronave",
    "cancelledBy": 1
  }'
```

### Respuesta Exitosa

```json
{
  "success": true,
  "message": "Vuelo cancelado exitosamente",
  "flightId": 123
}
```

---

## 🔍 Logs del Sistema

Cuando se cancela un vuelo, verás en la consola:

```
🚀 FlightDAO.cancelFlight() - Iniciando cancelación
   - Flight ID: 123
   - Reason: Condiciones meteorológicas adversas
   - Cancelled by: 1

🔍 Verificando si el vuelo puede ser cancelado:
   - ID del vuelo: 123
   - Estado actual: SCHEDULED
   - Fecha de salida: 2024-11-15
   - Fecha actual: 2024-10-31
   - ¿Puede ser cancelado?: true

✅ El vuelo puede ser cancelado, procediendo...

📧 Iniciando envío de notificaciones de cancelación...
📧 Enviando correo a: passenger1@email.com
📧 Enviando correo a: passenger2@email.com
✅ Notificaciones de cancelación enviadas exitosamente a pasajeros

📧 Iniciando envío de notificaciones a agencias...
📧 === ENVIANDO NOTIFICACIONES A AGENCIAS ===
📧 Vuelo cancelado: AA-1234
📧 Ruta: Ciudad A → Ciudad B
📧 Fecha: 2024-11-15 14:30
📧 Motivo: Condiciones meteorológicas adversas
📧 Agencias a notificar: 2
📧 Enviando notificación a agencia: agencia@example.com
📧 ✅ Correo enviado exitosamente a: agencia@example.com
📧 Enviando notificación a agencia: reservas@viajeselmundo.com
📧 ✅ Correo enviado exitosamente a: reservas@viajeselmundo.com
✅ Notificaciones enviadas: 2/2
📧 ==========================================
✅ Notificaciones enviadas exitosamente a agencias
```

---

## 📋 Base de Datos

### Campo `status` en la Tabla `FLIGHTS`

```sql
-- Estados posibles
'SCHEDULED'    -- Vuelo programado (por defecto)
'CANCELLED'    -- Vuelo cancelado
'COMPLETED'    -- Vuelo completado
'DELAYED'      -- Vuelo retrasado
```

### Campos de Cancelación

```sql
-- Campos agregados para cancelación
CANCELLATION_REASON  VARCHAR2(500)  -- Motivo de la cancelación
CANCELLED_BY         NUMBER(10)     -- ID del usuario que canceló
CANCELLATION_DATE    VARCHAR2(50)   -- Fecha de cancelación
```

---

## 🛠️ Integración con Servicio de Email Real

Para usar un servicio de correo **real** (SendGrid, AWS SES, Gmail SMTP):

### 1. **Agregar dependencias Maven** (opcional)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.sendgrid</groupId>
    <artifactId>sendgrid-java</artifactId>
    <version>4.9.3</version>
</dependency>
```

### 2. **Modificar el código**

En `SimpleEmailService.java`, línea ~180:

```java
// Reemplazar esta línea:
System.out.println("📧 ✅ Correo enviado exitosamente a: " + email);

// Por integración real, ejemplo con SendGrid:
SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
Email from = new Email("noreply@aerolinea.com");
Email to = new Email(email);
String subject = "CANCELACIÓN DE VUELO - " + flight.getFlightNumber();
Content content = new Content("text/plain", emailContent);
Mail mail = new Mail(from, subject, to, content);
Request request = new Request();
request.setMethod(Method.POST);
request.setEndpoint("mail/send");
request.setBody(mail.build());
Response response = sg.api(request);
```

### 3. **Configurar variable de entorno**

```bash
# SendGrid
export SENDGRID_API_KEY=your-api-key-here

# AWS SES
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
```

---

## ✅ Checklist de Verificación

- [✅] **Notificaciones a pasajeros** - Implementado
- [✅] **Notificaciones a agencias** - Implementado
- [✅] **Exclusión de vuelos cancelados** - Implementado en todas las búsquedas
- [✅] **Registro de motivo de cancelación** - Implementado
- [✅] **Registro de quién canceló** - Implementado
- [✅] **Fecha de cancelación** - Implementado
- [⚠️] **Integración con servicio de email real** - Pendiente (actualmente simula envío)

---

## 🚀 Próximos Pasos (Opcional)

1. **Integrar con servicio de email real** (SendGrid, AWS SES, Gmail SMTP)
2. **Agregar templates HTML** para correos más profesionales
3. **Consultar MongoDB** directamente para obtener correos de agencias
4. **Agregar cola de trabajos** (RabbitMQ, AWS SQS) para envío asíncrono
5. **Dashboard de cancelaciones** para administradores

---

## 📞 Soporte

Para más información o ayuda, contacta al equipo de desarrollo.

**Sistema**: AEROLINEA4 (backv6)  
**Última actualización**: 31/10/2024  

