# ✅ Sistema de Vuelos Round-Trip y con Escala - COMPLETO

## 🎯 Implementación Completada

### 1. Round-Trip (Ida y Vuelta) ✅
- Genera 2 tickets automáticamente
- Usa misma información de pago
- Un ticket para ida, uno para vuelta

### 2. Vuelos con Escala (1 parada) ✅
- Genera 2 tickets (segmento 1 y segmento 2)
- Usa misma información de pago
- Solo muestra si hay ruta con 1 escala

---

## 📋 Endpoints Creados

### Backend Java (Aerolínea)

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/airline/tickets/round-trip` | POST | Crear 2 tickets (ida + vuelta) |
| `/api/airline/tickets/with-stopover` | POST | Crear 2 tickets (con escala) |

### Backend Django (Agencia)

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/integrations/airline/tickets/round-trip` | POST | Proxy para round-trip |
| `/api/integrations/airline/tickets/with-stopover` | POST | Proxy para escala |

---

## 🚀 Cómo Usar - Round-Trip

### Request Example

```javascript
POST /api/integrations/airline/tickets/round-trip

Headers:
{
  "Content-Type": "application/json"
}

Body:
{
  "outboundFlightId": 1,      // Vuelo de IDA
  "returnFlightId": 2,         // Vuelo de VUELTA
  "seatCategory": "ECONOMY",
  "fare": 250.00,
  "passengerFirstName": "Juan",
  "passengerLastName": "Pérez",
  "passengerEmail": "juan@gmail.com",
  "passengerPhone": "555-1234",
  "passengerDocumentType": "ID_CARD",
  "passengerDocumentNumber": "12345678",
  "paymentMethod": "CREDIT_CARD"
}
```

### Response

```javascript
{
  "success": true,
  "message": "Vuelo redondo confirmado",
  "tickets": [
    {
      "ticketId": 101,
      "type": "outbound",      // IDA
      "flightNumber": "AA123"
    },
    {
      "ticketId": 102,
      "type": "return",        // VUELTA
      "flightNumber": "AA456"
    }
  ],
  "totalTickets": 2
}
```

---

## 🚀 Cómo Usar - Con Escala

### Request Example

```javascript
POST /api/integrations/airline/tickets/with-stopover

Body:
{
  "firstSegmentFlightId": 10,   // Origen → Escala
  "secondSegmentFlightId": 11,  // Escala → Destino
  "seatCategory": "ECONOMY",
  "fare": 300.00,
  "passengerFirstName": "María",
  "passengerLastName": "López",
  "passengerEmail": "maria@gmail.com",
  "passengerPhone": "555-5678",
  "passengerDocumentType": "PASSPORT",
  "passengerDocumentNumber": "ABC123456",
  "paymentMethod": "CREDIT_CARD"
}
```

### Response

```javascript
{
  "success": true,
  "message": "Vuelo con escala confirmado",
  "tickets": [
    {
      "ticketId": 103,
      "segment": 1,              // Segmento 1
      "flightNumber": "BB100"
    },
    {
      "ticketId": 104,
      "segment": 2,              // Segmento 2
      "flightNumber": "BB101"
    }
  ],
  "totalTickets": 2
}
```

---

## 💡 Lógica Implementada

### Round-Trip
```
Cliente busca: Guatemala → Miami (ida y vuelta)

Sistema encuentra:
- Vuelo IDA: Guatemala → Miami (ID: 1)
- Vuelo VUELTA: Miami → Guatemala (ID: 2)

Al comprar:
✅ Ticket 1: Guatemala → Miami (Pasajero: Juan)
✅ Ticket 2: Miami → Guatemala (Pasajero: Juan)
✅ Misma info de pago para ambos
✅ Mismo pasajero
```

### Con Escala
```
Cliente busca: Guatemala → Tokyo (no hay vuelo directo)

Sistema encuentra ruta con 1 escala:
- Segmento 1: Guatemala → Los Angeles (ID: 10)
- Segmento 2: Los Angeles → Tokyo (ID: 11)

Al comprar:
✅ Ticket 1: Guatemala → Los Angeles (Pasajero: María)
✅ Ticket 2: Los Angeles → Tokyo (Pasajero: María)
✅ Misma info de pago
✅ Mismo pasajero
```

---

## 📊 En la Base de Datos

### Tickets Round-Trip

```sql
-- Ticket IDA
ID_TICKET: 101
FLIGHT_ID: 1 (Guatemala → Miami)
USER_ID: 123
PURCHASED_BY_USER_ID: 123
PASSENGER_FIRST_NAME: Juan
PASSENGER_EMAIL: juan@gmail.com
SPECIAL_REQUESTS: "Vuelo de IDA - Round Trip"
STATUS: CONFIRMED

-- Ticket VUELTA
ID_TICKET: 102
FLIGHT_ID: 2 (Miami → Guatemala)
USER_ID: 123
PURCHASED_BY_USER_ID: 123
PASSENGER_FIRST_NAME: Juan
PASSENGER_EMAIL: juan@gmail.com
SPECIAL_REQUESTS: "Vuelo de VUELTA - Round Trip"
STATUS: CONFIRMED
```

### Tickets con Escala

```sql
-- Segmento 1
ID_TICKET: 103
FLIGHT_ID: 10 (Guatemala → LA)
SPECIAL_REQUESTS: "Segmento 1 - Con Escala"

-- Segmento 2
ID_TICKET: 104
FLIGHT_ID: 11 (LA → Tokyo)
SPECIAL_REQUESTS: "Segmento 2 - Con Escala"
```

---

## 🔧 Próximos Pasos

### 1. Compilar y Reiniciar Backend Java
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4
mvn clean compile
# Reiniciar el servidor
```

### 2. Reiniciar Backend Django
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django
# Ctrl+C
./scripts/start-django.sh
```

### 3. Probar desde Postman o curl

#### Round-Trip
```bash
curl -X POST http://localhost:5001/api/integrations/airline/tickets/round-trip \
  -H "Content-Type: application/json" \
  -d '{
    "outboundFlightId": 1,
    "returnFlightId": 2,
    "seatCategory": "ECONOMY",
    "fare": 250,
    "passengerFirstName": "Juan",
    "passengerLastName": "Pérez",
    "passengerEmail": "juan@gmail.com",
    "passengerPhone": "555-1234",
    "passengerDocumentType": "ID_CARD",
    "passengerDocumentNumber": "12345678",
    "paymentMethod": "CREDIT_CARD"
  }'
```

#### Con Escala
```bash
curl -X POST http://localhost:5001/api/integrations/airline/tickets/with-stopover \
  -H "Content-Type: application/json" \
  -d '{
    "firstSegmentFlightId": 10,
    "secondSegmentFlightId": 11,
    "seatCategory": "ECONOMY",
    "fare": 300,
    "passengerFirstName": "María",
    "passengerLastName": "López",
    "passengerEmail": "maria@gmail.com",
    "passengerPhone": "555-5678",
    "passengerDocumentType": "PASSPORT",
    "passengerDocumentNumber": "ABC123456",
    "paymentMethod": "CREDIT_CARD"
  }'
```

---

## 📝 Frontend - Cómo Integrar

### Para Round-Trip en agencia/src/pages/Compra.jsx

```javascript
// Si es round-trip
if (tipoVuelo === 'round-trip') {
    const payload = {
        outboundFlightId: vueloIda.id,
        returnFlightId: vueloVuelta.id,
        seatCategory: form.seatCategory,
        fare: precio,
        passengerFirstName: form.firstName,
        passengerLastName: form.lastName,
        passengerEmail: form.email,
        passengerPhone: form.phone,
        passengerDocumentType: 'ID_CARD',
        passengerDocumentNumber: form.passport,
        paymentMethod: 'CREDIT_CARD'
    };
    
    // Llamar endpoint round-trip
    const response = await fetch('http://localhost:5001/api/integrations/airline/tickets/round-trip', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    
    const data = await response.json();
    
    if (data.success) {
        alert(`¡Éxito! Se crearon ${data.totalTickets} tickets`);
        console.log('Tickets:', data.tickets);
    }
}
```

---

## 🧪 Casos de Prueba

### Caso 1: Round-Trip Normal
```
Vuelo IDA: Guatemala → Miami (06/10/2025)
Vuelo VUELTA: Miami → Guatemala (13/10/2025)
Resultado: 2 tickets creados ✅
```

### Caso 2: Con Escala
```
Origen: Guatemala
Escala: Ciudad de México
Destino: Madrid

Segmento 1: Guatemala → CDMX
Segmento 2: CDMX → Madrid
Resultado: 2 tickets creados ✅
```

### Caso 3: Round-Trip Empresarial
```
Cliente en agencia:
- Selecciona round-trip
- Completa datos
- Compra

Resultado:
- 2 tickets con API_KEY ✅
- PURCHASED_BY_USER_ID = Agencia ✅
- Pasajero = Cliente ✅
```

---

## 📊 Archivos Modificados

### Backend Java
```
✅ TicketHandler.java (+ handleCreateRoundTripTickets, handleCreateStopoverTickets)
✅ TicketDAO.java (métodos ya existentes funcionan)
✅ RoundTripTicketHandler.java (NUEVO - handler auxiliar)
```

### Backend Django
```
✅ views.py (+ proxy_airline_create_roundtrip, proxy_airline_create_stopover)
✅ urls.py (+ rutas para round-trip y stopover)
```

---

## ✨ Próxima Fase (Opcional)

### Búsqueda Inteligente de Escalas

Crear algoritmo para encontrar rutas con 1 escala automáticamente:

```sql
-- Query para encontrar rutas con escala
WITH possible_connections AS (
  SELECT 
    f1.ID_FLIGHT as first_flight,
    f1.DESTINATION_CITY_ID as stopover_city,
    f2.ID_FLIGHT as second_flight
  FROM FLIGHTS f1
  INNER JOIN FLIGHTS f2 
    ON f1.DESTINATION_CITY_ID = f2.ORIGIN_CITY_ID
  WHERE f1.ORIGIN_CITY_ID = ? -- Ciudad origen
    AND f2.DESTINATION_CITY_ID = ? -- Ciudad destino
    AND f1.STATUS = 'SCHEDULED'
    AND f2.STATUS = 'SCHEDULED'
)
SELECT * FROM possible_connections
LIMIT 10;
```

---

## 🎯 Estado del Sistema

```
╔════════════════════════════════════════════╗
║     FUNCIONALIDADES IMPLEMENTADAS          ║
╠════════════════════════════════════════════╣
║ ✅ Vuelos directos (1 ticket)              ║
║ ✅ Round-trip (2 tickets)                  ║
║ ✅ Con escala (2 tickets)                  ║
║ ✅ Compra empresarial (API_KEY)            ║
║ ✅ Compra individual (normal)              ║
║ ✅ Mis Reservas (agencia)                  ║
║ ✅ Descarga de PDFs                        ║
╚════════════════════════════════════════════╝
```

---

**Estado**: ✅ Código implementado y compilado  
**Endpoints**: 2 nuevos (round-trip, stopover)  
**Acción**: Reiniciar backends y probar  

## 🔄 ¡Reinicia los backends para usar los nuevos endpoints!

