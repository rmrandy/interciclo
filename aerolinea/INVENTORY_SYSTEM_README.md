# Sistema de Inventario de Asientos - Carrito de Vuelos

## Descripción General

Se ha implementado un nuevo sistema de inventario de asientos que reemplaza la selección manual de asientos específicos por un sistema de gestión de inventario por categoría. Este sistema permite a los usuarios reservar cantidades de asientos sin necesidad de seleccionar asientos específicos.

## Cambios Implementados

### 1. Eliminación de Selección Manual de Asientos
- ❌ **Antes**: Los usuarios seleccionaban asientos específicos (ej: "A1", "B3", etc.)
- ✅ **Ahora**: Los usuarios seleccionan solo la categoría y cantidad de asientos

### 2. Sistema de Inventario por Categoría
- **ECONOMY**: Asientos económicos
- **BUSINESS**: Asientos ejecutivos  
- **FIRST_CLASS**: Asientos de primera clase

### 3. Gestión de Cantidades
- Controles de incremento/decremento para seleccionar cantidad
- Validación automática de disponibilidad
- Límites basados en asientos disponibles por categoría

## Estructura de Datos

### Inventario del Vuelo
```typescript
inventoryByCategory: {
  'ECONOMY': {
    availableSeats: 150,
    totalSeats: 150,
    reservedSeats: 0,
    soldSeats: 0
  },
  'BUSINESS': {
    availableSeats: 30,
    totalSeats: 30,
    reservedSeats: 0,
    soldSeats: 0
  },
  'FIRST_CLASS': {
    availableSeats: 20,
    totalSeats: 20,
    reservedSeats: 0,
    soldSeats: 0
  }
}
```

### Item del Carrito
```typescript
cartItem: {
  flight: Flight,
  selectedCategory: 'ECONOMY' | 'BUSINESS' | 'FIRST_CLASS',
  quantity: number, // Cantidad de asientos (no asientos específicos)
  inventoryByCategory: InventoryData,
  processing: boolean
}
```

## Flujo de Usuario

### 1. Selección de Categoría
- Usuario selecciona la categoría de asiento deseada
- Sistema muestra la cantidad disponible para esa categoría
- Se resetea la cantidad a 1

### 2. Selección de Cantidad
- Usuario puede aumentar/disminuir la cantidad con botones +/-
- Sistema valida que no exceda la disponibilidad
- Se muestra información de asientos disponibles

### 3. Cálculo de Precios
- Precio base × cantidad de asientos
- Impuestos y cargos calculados sobre el total
- Resumen actualizado en tiempo real

### 4. Confirmación
- Usuario confirma la selección
- Sistema valida disponibilidad final
- Item se marca como confirmado

## Ventajas del Nuevo Sistema

### Para el Usuario
- ✅ **Más simple**: No necesita seleccionar asientos específicos
- ✅ **Más rápido**: Proceso de reserva simplificado
- ✅ **Flexible**: Puede reservar múltiples asientos fácilmente
- ✅ **Claro**: Ve exactamente cuántos asientos están disponibles

### Para el Sistema
- ✅ **Mejor gestión**: Control centralizado del inventario
- ✅ **Escalable**: Fácil de expandir a más categorías
- ✅ **Consistente**: Misma lógica para todos los vuelos
- ✅ **Auditable**: Rastreo completo de reservas y ventas

## API Endpoints

### 🆕 Nuevo Endpoint: Inventario Completo
```typescript
GET /api/airline/inventory/{flightId}
```

**Respuesta:**
```json
{
  "success": true,
  "flightId": 1,
  "inventory": {
    "ECONOMY": {
      "totalSeats": 150,
      "availableSeats": 135,
      "reservedSeats": 5,
      "soldSeats": 10,
      "status": "ACTIVE"
    },
    "BUSINESS": {
      "totalSeats": 30,
      "availableSeats": 25,
      "reservedSeats": 2,
      "soldSeats": 3,
      "status": "ACTIVE"
    }
  },
  "summary": {
    "totalSeats": 180,
    "availableSeats": 160,
    "reservedSeats": 7,
    "soldSeats": 13,
    "categories": ["ECONOMY", "BUSINESS"],
    "occupancyRate": 0.11
  }
}
```

### 🆕 Nuevo Endpoint: Resumen de Inventario
```typescript
GET /api/airline/inventory/{flightId}/summary
```

**Respuesta:**
```json
{
  "success": true,
  "flightId": 1,
  "summary": {
    "totalSeats": 180,
    "availableSeats": 160,
    "reservedSeats": 7,
    "soldSeats": 13,
    "occupancyRate": 0.11
  }
}
```

### 🔄 Endpoint Legacy: Asientos Disponibles
```typescript
GET /api/airline/seats/{flightId}
```

**Nota**: Este endpoint se mantiene por compatibilidad pero se recomienda usar el nuevo `/inventory/{flightId}`.

## Base de Datos

### Tabla FLIGHT_INVENTORY
```sql
CREATE TABLE FLIGHT_INVENTORY (
    ID_INVENTORY NUMBER(10) PRIMARY KEY,
    FLIGHT_ID NUMBER(10) NOT NULL,
    SEAT_CATEGORY VARCHAR2(20) NOT NULL,
    TOTAL_SEATS NUMBER(6) NOT NULL,
    AVAILABLE_SEATS NUMBER(6) NOT NULL,
    RESERVED_SEATS NUMBER(6) DEFAULT 0,
    SOLD_SEATS NUMBER(6) DEFAULT 0,
    STATUS VARCHAR2(20) DEFAULT 'ACTIVE',
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

## Validaciones Implementadas

### 1. Disponibilidad de Asientos
- Verificación de que la cantidad solicitada no exceda los disponibles
- Validación en tiempo real durante la selección

### 2. Categoría Válida
- Solo categorías predefinidas (ECONOMY, BUSINESS, FIRST_CLASS)
- Deshabilitación de categorías sin asientos disponibles

### 3. Cantidad Mínima
- Mínimo 1 asiento por reserva
- Máximo limitado por disponibilidad real

## Implementación Backend

### Nuevo Handler: InventoryHandler
- **Clase**: `com.sources.app.handlers.InventoryHandler`
- **Endpoints**:
  - `GET /api/airline/inventory/{flightId}` - Inventario completo
  - `GET /api/airline/inventory/{flightId}/summary` - Resumen del inventario

### Características del Handler
- ✅ **CORS habilitado** para integración frontend
- ✅ **Manejo de errores** robusto
- ✅ **Logging detallado** para debugging
- ✅ **Validación de datos** de entrada
- ✅ **Respuestas estructuradas** y consistentes

### Registro en el Servidor
```java
// En App.java
server.createContext("/api/airline/inventory", new InventoryHandler());
```

## Implementación Frontend

### API Client Actualizado
```typescript
// Nuevo método para inventario completo
async getFlightInventory(flightId: number): Promise<any>

// Nuevo método para resumen
async getFlightInventorySummary(flightId: number): Promise<any>
```

### Carrito Rediseñado
- 🎨 **Interfaz elegante** con gradientes y efectos visuales
- 🚀 **Animaciones suaves** y transiciones
- 📱 **Diseño responsive** para móviles y desktop
- ✨ **Efectos hover** y estados interactivos

## Próximos Pasos

### 1. Integración con Checkout
- Implementar descuento automático del inventario al confirmar reserva
- Sincronización con sistema de pagos

### 2. Gestión de Reservas Temporales
- Sistema de bloqueo temporal de asientos durante el proceso de reserva
- Timeout automático para reservas no confirmadas

### 3. Reportes de Inventario
- Dashboard para administradores
- Estadísticas de ocupación por categoría

## Testing

### Archivo de Prueba
Se incluye `test_inventory_api.html` para probar los nuevos endpoints:

1. **Probar Inventario Completo**: `/api/airline/inventory/{flightId}`
2. **Probar Resumen**: `/api/airline/inventory/{flightId}/summary`
3. **Configurar URL de API** para diferentes entornos

### Cómo Usar
1. Abrir `test_inventory_api.html` en un navegador
2. Configurar la URL base de la API
3. Ingresar ID de vuelo para probar
4. Verificar respuestas y estructura de datos

## Compatibilidad

### Frontend
- ✅ Vue.js 3 con Composition API
- ✅ TypeScript para tipado
- ✅ Responsive design para móviles
- ✅ Estilos modernos con CSS3 avanzado

### Backend
- ✅ Java con Hibernate
- ✅ Base de datos Oracle/PostgreSQL
- ✅ API REST estándar
- ✅ Nuevo handler dedicado para inventario

## Notas de Implementación

- El sistema mantiene compatibilidad con el endpoint existente `/api/airline/seats/{flightId}`
- Se agregó el método `getFlightInventory()` en la API del frontend
- La transformación de datos se hace en el cliente para mantener compatibilidad
- El fallback de inventario asegura que el sistema funcione incluso si hay errores de API
- **Nuevo**: Handler dedicado `InventoryHandler` para mejor organización del código

## Soporte

Para reportar problemas o solicitar mejoras, contactar al equipo de desarrollo.
