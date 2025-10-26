# 🛫 Implementación de Vuelos con Escala y Round-Trip

## 🎯 Requisitos

### 1. Vuelos con Escala (1 parada)
- Buscar vuelos con UNA escala intermedia
- Generar 2 tickets: Origen → Escala, Escala → Destino
- Si no hay ruta con 1 escala, no mostrar

### 2. Vuelos Redondos (Round-trip)
- Buscar vuelo de ida + vuelo de vuelta
- Generar 2 tickets: Ida, Vuelta
- Misma información de pago para ambos

---

## 📋 Plan de Implementación

### Paso 1: Lógica de Búsqueda con Escalas
```sql
-- Buscar vuelos directos
SELECT * FROM FLIGHTS 
WHERE ORIGIN_CITY_ID = ? AND DESTINATION_CITY_ID = ?

-- Buscar vuelos con 1 escala
-- Opción A: Vuelos con FlightLegs
SELECT f.* FROM FLIGHTS f
INNER JOIN FLIGHT_LEGS fl ON f.ID_FLIGHT = fl.FLIGHT_ID
WHERE f.ORIGIN_CITY_ID = ? 
  AND fl.LEG_ORDER = 1  -- Primera escala
  AND EXISTS (
    SELECT 1 FROM FLIGHT_LEGS fl2 
    WHERE fl2.FLIGHT_ID = f.ID_FLIGHT 
      AND fl2.LEG_ORDER = 2
      AND fl2.CITY_ID = ?  -- Destino final
  )

-- Opción B: Combinación de 2 vuelos
-- Vuelo 1: Origen → Ciudad X
-- Vuelo 2: Ciudad X → Destino
```

### Paso 2: Lógica de Compra con 2 Tickets
```java
if (esVueloConEscala) {
    // Crear ticket 1: Origen → Escala
    Ticket ticket1 = crearTicket(vuelo1, pasajero, pago);
    
    // Crear ticket 2: Escala → Destino  
    Ticket ticket2 = crearTicket(vuelo2, pasajero, pago);
    
    // Retornar ambos tickets
    return {ticket1, ticket2};
}

if (esRoundTrip) {
    // Crear ticket ida
    Ticket ticketIda = crearTicket(vueloIda, pasajero, pago);
    
    // Crear ticket vuelta
    Ticket ticketVuelta = crearTicket(vueloVuelta, pasajero, pago);
    
    return {ticketIda, ticketVuelta};
}
```

---

## 🚀 Estrategia Simplificada

Dado que es un proyecto complejo y ya hay mucho código, te recomiendo:

**Para MVP rápido**:
1. ✅ Vuelos directos (ya funcionan)
2. ✅ Round-trip: Buscar 2 vuelos separados y generar 2 tickets
3. ⏭️ Vuelos con escala: Fase 2 (más complejo)

**¿Empezamos con Round-Trip primero?** Es más simple y útil.

---

**Tiempo estimado**:
- Round-trip: 30-40 minutos
- Con escala: 1-2 horas (más complejo)

¿Procedo con Round-Trip primero?

