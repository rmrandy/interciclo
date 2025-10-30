# 🚀 Guía del Agregador de Vuelos Multi-Aerolíneas

## 📋 ¿Qué es?

El agregador de vuelos permite que tu agencia busque y compre vuelos en **múltiples aerolíneas simultáneamente**, mostrando todos los resultados juntos y ordenados por precio.

---

## 🔧 Configuración Inicial

### **Paso 1: Crear Usuarios Empresariales en cada Aerolínea**

1. Entra a cada aerolínea como **Admin**:
   - Aerolínea 1: `http://192.168.0.33:2020`
   - Aerolínea 2: `http://192.168.0.33:3020`

2. Ve a **Administración** → **🏢 Usuarios Empresariales**

3. Crea un nuevo usuario empresarial:
   - **Nombre de la Empresa**: "Agencia Viajes XYZ"
   - **Nombre del Contacto**: Tu nombre
   - **Email**: El que uses para la agencia
   - **CUI/NIT**: Cualquier número
   - **Teléfono**: Tu teléfono
   - **Dirección**: Dirección de la agencia
   - **Contraseña**: (cualquiera, no la necesitas)
   - **Usuario activo**: ✅

4. **¡IMPORTANTE!** Al crear el usuario, se generará un **API Key**. Cópialo inmediatamente (algo como: `abc123xyz456...`)

5. Repite esto en **cada aerolínea** (tendrás 2 API Keys diferentes)

---

### **Paso 2: Configurar las Aerolíneas en la Agencia**

1. Entra a tu **Agencia**: `http://localhost:3000` (o tu puerto)

2. Login como **Admin**

3. Ve a **Administración** → Tab **Aerolíneas**

4. **Añade la primera aerolínea:**
   ```
   Nombre: AeroLinea Principal
   Código: AER1
   Protocolo: http
   Host/IP: 192.168.0.33
   Puerto: 2020
   Base path: /api
   🔑 API Key: [PEGA EL API KEY GENERADO EN PASO 1]
   Endpoint búsqueda: airline/flights/search
   Endpoint compra: airline/tickets
   Endpoint cancelación: airline/tickets/cancel
   Endpoint health: health
   Timeout (ms): 5000
   Activo: Sí
   ```

5. Click en **Crear aerolínea**

6. **Añade la segunda aerolínea:**
   ```
   Nombre: AeroLinea Express
   Código: AER2
   Protocolo: http
   Host/IP: 192.168.0.33
   Puerto: 3020
   Base path: /api
   🔑 API Key: [PEGA EL API KEY DE LA SEGUNDA AEROLÍNEA]
   (Los demás campos iguales)
   ```

---

## 🎯 Cómo Usar el Agregador

### **Búsqueda Agregada** 

La búsqueda ahora es **automática**. Cuando uses el buscador normal de la agencia, automáticamente buscará en todas las aerolíneas configuradas.

**Lo que verás:**
- Vuelos de **todas las aerolíneas** juntos
- Ordenados por **precio** (menor a mayor)
- Cada vuelo muestra de qué aerolínea es
- Si una aerolínea falla, las demás siguen funcionando

---

## 📊 Endpoints del API

### **1. Búsqueda Agregada**
```http
GET /api/flights/aggregated-search?origin=CIUDAD1&destination=CIUDAD2&departureDate=2025-10-27
```

**Respuesta:**
```json
{
  "success": true,
  "totalFlights": 15,
  "airlinesQueried": 2,
  "airlinesWithResults": 2,
  "flights": [
    {
      "idFlight": 123,
      "flightNumber": "AE001",
      "basePrice": 250.00,
      "airlineId": "abc123",
      "airlineName": "AeroLinea Principal",
      "airlineCode": "AER1",
      "uniqueId": "AER1-123"
    },
    ...
  ]
}
```

### **2. Compra en Aerolínea Específica**
```http
POST /api/flights/aggregated-purchase
Content-Type: application/json

{
  "airlineId": "abc123",
  "flightId": 123,
  "userId": 456,
  "passengers": [...]
}
```

---

## 🧪 Cómo Probar

1. **Crea vuelos en ambas aerolíneas:**
   - Entra a cada aerolínea como admin
   - Crea vuelos con diferentes precios
   - Mismo origen y destino para poder compararlos

2. **Busca desde la agencia:**
   - Ve a la página de búsqueda
   - Busca vuelos
   - Deberías ver vuelos de **ambas aerolíneas** juntos

3. **Compra un vuelo:**
   - Selecciona cualquier vuelo (de cualquier aerolínea)
   - La compra se procesará en la aerolínea correcta automáticamente

---

## ⚡ Características Avanzadas

### **Tolerancia a Fallos**
- Si una aerolínea no responde, las demás siguen funcionando
- Timeout configurable por aerolínea (5 segundos por defecto)
- Los errores se reportan pero no detienen la búsqueda

### **Seguridad**
- API Keys almacenados en el backend (no se exponen al frontend)
- Autenticación automática en cada petición
- Logs de todas las peticiones

### **Performance**
- Peticiones en **paralelo** (no espera una para hacer la otra)
- Caché opcional (se puede implementar)
- Timeout para evitar esperas largas

---

## 🔍 Verificación

Para verificar que todo funciona:

1. **Check de aerolíneas configuradas:**
   ```bash
   # En la consola del backend Django verás:
   🔍 Buscando en AeroLinea Principal: http://192.168.0.33:2020/api/airline/flights/search
   ✅ AeroLinea Principal: 5 vuelos encontrados
   🔍 Buscando en AeroLinea Express: http://192.168.0.33:3020/api/airline/flights/search
   ✅ AeroLinea Express: 3 vuelos encontrados
   ```

2. **En la tabla de aerolíneas** deberías ver:
   - ✅ Ambas aerolíneas activas
   - 🔑 API Keys mostrados parcialmente (primeros 8 y últimos 4 caracteres)

---

## 🐛 Resolución de Problemas

### **Error: "Sin API Key"**
- Verifica que copiaste el API Key completo
- El API Key debe tener ~32 caracteres
- Edita la aerolínea y pega el API Key de nuevo

### **Error: "Timeout"**
- Verifica que las aerolíneas estén corriendo
- Aumenta el timeout (10000 ms = 10 segundos)
- Verifica que el host/puerto sean correctos

### **No aparecen vuelos**
- Verifica que haya vuelos creados en las aerolíneas
- Verifica las fechas de búsqueda
- Revisa los logs del backend Django

### **Error: "Aerolínea deshabilitada"**
- Ve a Admin → Aerolíneas
- Edita la aerolínea y marca "Activo: Sí"

---

## 📝 Notas Importantes

1. **Los API Keys son permanentes** hasta que los regeneres desde la aerolínea
2. **Cada aerolínea necesita su propio API Key**
3. **Puedes añadir más aerolíneas** cuando quieras (3, 4, 10...)
4. **Los vuelos se identifican por** `uniqueId = CODIGO-ID_FLIGHT`
5. **La compra siempre se hace en la aerolínea correcta** automáticamente

---

## 🎓 Ejemplo Completo

```javascript
// Desde el frontend de la agencia
const results = await fetch('http://localhost:8000/api/flights/aggregated-search?origin=1&destination=2&departureDate=2025-10-27');

// Respuesta combinada de ambas aerolíneas
{
  "totalFlights": 8,
  "flights": [
    { "price": 200, "airlineName": "AeroLinea Principal", "airlineCode": "AER1" },
    { "price": 220, "airlineName": "AeroLinea Express", "airlineCode": "AER2" },
    { "price": 250, "airlineName": "AeroLinea Principal", "airlineCode": "AER1" },
    ...
  ]
}
```

---

¡Listo! Ahora tu agencia funciona como un **verdadero agregador de vuelos** como Kayak, Skyscanner, etc. 🎉







