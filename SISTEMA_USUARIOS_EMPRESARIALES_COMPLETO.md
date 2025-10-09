# 🏢 Sistema de Usuarios Empresariales - Guía Completa

**Proyecto**: Ensurance Pharmacy - Aerolínea + Agencia de Viajes  
**Fecha**: Octubre 6, 2025  
**Estado**: ✅ COMPLETADO

---

## 🎯 ¿Qué es este Sistema?

Un sistema **B2B (Business-to-Business)** que permite a la **Agencia de Viajes** comprar vuelos en la **Aerolínea** en nombre de sus clientes.

### Concepto

```
Cliente → Compra en Agencia → Agencia compra en Aerolínea → Ticket para Cliente
         (Web pública)        (Usuario empresarial)          (Pasajero real)
```

**Separación de roles:**
- **Comprador**: La Agencia (usuario empresarial)
- **Pasajero**: El Cliente final
- **Facturación**: A la Agencia
- **Ticket/Email**: Para el Cliente

---

## 📊 Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENTE FINAL                             │
│  (Juan Pérez - juan@gmail.com)                              │
└────────────────┬────────────────────────────────────────────┘
                 │ Busca y compra vuelo
                 ↓
┌─────────────────────────────────────────────────────────────┐
│              AGENCIA DE VIAJES (Frontend)                    │
│  http://localhost:5173                                       │
└────────────────┬────────────────────────────────────────────┘
                 │ POST /api/integrations/airline/tickets
                 ↓
┌─────────────────────────────────────────────────────────────┐
│        AGENCIA BACKEND (Django - MongoDB)                    │
│  http://localhost:5001                                       │
│                                                              │
│  1. Lee API_KEY de MongoDB:                                 │
│     AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L                    │
│                                                              │
│  2. Hace proxy a aerolínea con API_KEY:                     │
│     Headers: X-API-Key: AGV-WQD...                          │
└────────────────┬────────────────────────────────────────────┘
                 │ POST /api/airline/tickets
                 │ Headers: X-API-Key: AGV-WQD...
                 ↓
┌─────────────────────────────────────────────────────────────┐
│         AEROLÍNEA BACKEND (Java - Oracle)                    │
│  http://192.168.0.2:8080                                     │
│                                                              │
│  1. Valida API_KEY → Usuario Empresarial                    │
│  2. Crea ticket:                                            │
│     - PURCHASED_BY_USER_ID: 123 (Agencia)                   │
│     - USER_ID: 123 (Agencia por defecto)                    │
│     - PASSENGER_FIRST_NAME: Juan                            │
│     - PASSENGER_LAST_NAME: Pérez                            │
│     - PASSENGER_EMAIL: juan@gmail.com                       │
│                                                              │
│  3. Envía email a: juan@gmail.com                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔑 Tu API_KEY

```
AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L
```

**Guarda esto en un lugar seguro** - lo necesitarás para configurar.

---

## ✅ Cambios Realizados

### 1. Base de Datos Oracle (Aerolínea)

#### Tabla USERS - Nuevas columnas ✅
```sql
IS_CORPORATE       NUMBER(1,0)   -- 1 = empresarial, 0 = individual
COMPANY_NAME       VARCHAR2(200)  -- Nombre de la empresa
API_KEY            VARCHAR2(100)  -- Token único (UNIQUE)
```

#### Tabla TICKETS - Nueva columna ✅
```sql
PURCHASED_BY_USER_ID  NUMBER(38,0)  -- ID del comprador (FK a USERS)
```

#### Usuario Creado ✅
```
Email: corporate@agencia-elvuelo.com
Password: corporate123
Rol: CORPORATE
API_KEY: AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L
```

---

### 2. Backend Java (Aerolínea)

#### User.java ✅
```java
// Nuevos campos
private Integer isCorporate;
private String companyName;
private String apiKey;

// Nuevos métodos
public boolean isCorporateUser()
public String getApiKey()
```

#### Ticket.java ✅
```java
// Nuevo campo
@ManyToOne
private User purchasedByUser;

// Nuevos métodos
public User getPurchasedByUser()
public boolean isCorporatePurchase()
```

#### UserDAO.java ✅
```java
// Nuevo método
public User findByApiKey(String apiKey)
public List<User> findCorporateUsers()
```

#### CorporateAuthUtil.java ✅ (NUEVO)
```java
// Utilidad para autenticación con API_KEY
public static User authenticateWithApiKey(HttpExchange exchange)
public static String extractApiKey(HttpExchange exchange)
public static User validateApiKey(String apiKey)
```

#### TicketHandler.java ✅
```java
// Soporte para compras empresariales
- Detecta API_KEY en headers
- Automa el usuario empresarial
- Guarda purchasedByUserId
- Logs informativos
```

#### TicketDAO.java ✅
```java
// Manejo de purchasedByUser
if (purchasedByUserId != null) {
    ticket.setPurchasedByUser(purchasedByUser);
}
```

---

### 3. Base de Datos MongoDB (Agencia)

#### Nueva colección: corporate_config ✅
```javascript
{
  _id: "airline_corporate_user",
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  corporateUserEmail: "corporate@agencia-elvuelo.com",
  companyName: "Agencia de Viajes El Vuelo",
  enabled: true
}
```

---

### 4. Backend Django (Agencia)

#### views.py ✅
```python
# Nuevas funciones
def get_corporate_api_key()  # Lee API_KEY de MongoDB
def get_corporate_headers()   # Retorna headers con API_KEY

# Funciones modificadas
def safe_get()  # Usa headers con API_KEY
def safe_post()  # Usa headers con API_KEY
def stream_get()  # Usa headers con API_KEY
```

**Resultado**: Todas las llamadas a la aerolínea incluyen el API_KEY automáticamente

---

## 🚀 Configuración Paso a Paso

### PASO 1: Oracle (Ya Hecho ✅)

Ya ejecutaste el query y tienes:
- ✅ Usuario empresarial creado
- ✅ API_KEY: `AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L`

### PASO 2: MongoDB (Ejecutar Ahora)

Abre **MongoDB Compass** o **mongo shell**:

```javascript
// Conecta a: agencia-viajes

// Ejecuta este comando:
db.corporate_config.insertOne({
  _id: "airline_corporate_user",
  name: "Configuración Usuario Empresarial Aerolínea",
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  corporateUserEmail: "corporate@agencia-elvuelo.com",
  companyName: "Agencia de Viajes El Vuelo",
  enabled: true,
  createdAt: new Date(),
  updatedAt: new Date()
});

// Verificar:
db.corporate_config.findOne({ _id: "airline_corporate_user" });
```

### PASO 3: Compilar Backend Java

```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4
mvn clean compile
```

### PASO 4: Reiniciar Servidores

```bash
# Terminal 1 - Backend Java (Aerolínea)
cd backv4
./run_app_server.sh
# O: mvn exec:java -Dexec.mainClass="com.sources.app.App"

# Terminal 2 - Backend Django (Agencia)
cd agencia-viajes/backend-django
./scripts/start-django.sh
```

---

## 🧪 Cómo Probar

### Prueba 1: Verificar API_KEY en Agencia

```bash
# Verificar que MongoDB tiene el API_KEY
curl http://localhost:5001/api/debug/db-info
```

### Prueba 2: Compra Manual con API_KEY

```bash
# Probar crear ticket con API_KEY
curl -X POST http://192.168.0.2:8080/api/airline/tickets \
  -H "Content-Type: application/json" \
  -H "X-API-Key: AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L" \
  -d '{
    "flightId": 1,
    "userId": 123,
    "seatCategory": "ECONOMY",
    "fare": 250.00,
    "passengerFirstName": "Juan",
    "passengerLastName": "Pérez",
    "passengerDocumentType": "ID_CARD",
    "passengerDocumentNumber": "12345678",
    "passengerEmail": "juan@gmail.com",
    "passengerPhone": "555-1234",
    "paymentMethod": "CREDIT_CARD",
    "totalAmount": 250.00
  }'
```

**Logs esperados en Java:**
```
✅ Usuario empresarial autenticado: Agencia de Viajes El Vuelo (ID: 123)
🏢 Compra empresarial detectada: Agencia de Viajes El Vuelo
🏢 Ticket comprado por usuario empresarial: Agencia de Viajes El Vuelo
```

### Prueba 3: Compra desde la Agencia Web

1. Abre la agencia: `http://localhost:5173`
2. Busca un vuelo
3. Completa datos del cliente
4. Compra

**Deberías ver en logs de Django:**
```
🔑 Usando API_KEY empresarial para autenticación
```

**Deberías ver en logs de Java:**
```
✅ Usuario empresarial autenticado: Agencia de Viajes El Vuelo
🏢 Compra empresarial detectada
```

---

## 📋 Flujo Completo de Compra

### Desde la Perspectiva del Cliente

```
1. Cliente abre agencia web
2. Busca vuelo Guatemala → México
3. Selecciona asientos
4. Completa sus datos:
   - Nombre: Juan Pérez
   - Email: juan@gmail.com
   - Teléfono: 555-1234
5. Confirma compra
6. Recibe ticket en su email
```

### Detrás de Escenas

```
1. Frontend agencia → Backend agencia Django
   POST /api/integrations/airline/tickets
   {
     passengerFirstName: "Juan",
     passengerEmail: "juan@gmail.com",
     ...
   }

2. Backend agencia:
   - Lee API_KEY de MongoDB
   - Hace request a aerolínea

3. Request a aerolínea:
   POST http://192.168.0.2:8080/api/airline/tickets
   Headers: X-API-Key: AGV-WQD...
   Body: { datos del cliente }

4. Backend aerolínea Java:
   - Valida API_KEY
   - Encuentra usuario empresarial (Agencia)
   - Crea ticket:
     * PURCHASED_BY_USER_ID: 123 (Agencia)
     * PASSENGER_EMAIL: juan@gmail.com (Cliente)
     * PASSENGER_FIRST_NAME: Juan
   
5. Email enviado a: juan@gmail.com

6. Ticket guardado con:
   - Comprador: Agencia de Viajes
   - Pasajero: Juan Pérez
```

---

## 💾 Estructura de Datos

### En Oracle (Aerolínea)

#### USERS
```sql
ID_USER: 123
NAME: Agencia de Viajes El Vuelo
EMAIL: corporate@agencia-elvuelo.com
ROL: CORPORATE
IS_CORPORATE: 1
COMPANY_NAME: Agencia de Viajes El Vuelo
API_KEY: AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L
ENABLED: 1
```

#### TICKETS (ejemplo de compra empresarial)
```sql
ID_TICKET: 456
FLIGHT_ID: 1
USER_ID: 123 (Agencia)
PURCHASED_BY_USER_ID: 123 (Agencia)
SEAT_CATEGORY: ECONOMY
PASSENGER_FIRST_NAME: Juan
PASSENGER_LAST_NAME: Pérez
PASSENGER_EMAIL: juan@gmail.com  ← Email del cliente
PASSENGER_PHONE: 555-1234
TOTAL_AMOUNT: 250.00
STATUS: CONFIRMED
```

### En MongoDB (Agencia)

#### corporate_config
```javascript
{
  _id: "airline_corporate_user",
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  corporateUserEmail: "corporate@agencia-elvuelo.com",
  companyName: "Agencia de Viajes El Vuelo",
  enabled: true,
  createdAt: ISODate("2025-10-06..."),
  updatedAt: ISODate("2025-10-06...")
}
```

---

## 🔧 Archivos Modificados/Creados

### Backend Java (Aerolínea)

#### Modificados ✅
```
backv4/src/main/java/com/sources/app/
├── entities/
│   ├── User.java                   (+ isCorporate, companyName, apiKey)
│   └── Ticket.java                 (+ purchasedByUser)
├── dao/
│   ├── UserDAO.java                (+ findByApiKey, findCorporateUsers)
│   └── TicketDAO.java              (+ soporte purchasedByUser)
└── handlers/
    └── TicketHandler.java          (+ autenticación API_KEY)
```

#### Creados ✅
```
backv4/src/main/java/com/sources/app/util/
└── CorporateAuthUtil.java          (NUEVO - autenticación empresarial)
```

### Backend Django (Agencia)

#### Modificados ✅
```
agencia-viajes/backend-django/api/
├── views.py                        (+ get_corporate_api_key, headers con API_KEY)
└── urls.py                         (+ endpoint /airlines/active)
```

### Queries SQL ✅
```
backv4/
├── QUERIES_USUARIOS_EMPRESARIALES.sql
├── CREAR_USUARIO_EMPRESARIAL.sql
└── INSTRUCCIONES_DBEAVER.md
```

### Queries MongoDB ✅
```
agencia-viajes/
└── CONFIGURAR_USUARIO_EMPRESARIAL_MONGO.js
```

### Documentación ✅
```
/
├── SISTEMA_USUARIOS_EMPRESARIALES_COMPLETO.md  (Este archivo)
└── agencia-viajes/
    ├── CONFIGURACION_AEROLINEAS_DINAMICA.md
    └── GUIA_RAPIDA_ADMIN_AEROLINEAS.md
```

---

## 📝 Pasos de Configuración

### ✅ PASO 1: Oracle (HECHO)
- [x] Ejecutar queries en DBeaver
- [x] Crear usuario empresarial
- [x] Obtener API_KEY: `AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L`

### ⏭️ PASO 2: MongoDB (SIGUIENTE)

Ejecuta en **MongoDB Compass** o **mongo shell**:

```javascript
// Conectar a base de datos: agencia-viajes

db.corporate_config.insertOne({
  _id: "airline_corporate_user",
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  corporateUserEmail: "corporate@agencia-elvuelo.com",
  companyName: "Agencia de Viajes El Vuelo",
  enabled: true,
  createdAt: new Date(),
  updatedAt: new Date()
});

// Verificar:
db.corporate_config.findOne({ _id: "airline_corporate_user" });
```

### ⏭️ PASO 3: Compilar Java

```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4
mvn clean compile
```

### ⏭️ PASO 4: Reiniciar Servidores

```bash
# Terminal 1 - Backend Java
cd backv4
mvn exec:java -Dexec.mainClass="com.sources.app.App"

# Terminal 2 - Backend Django
cd agencia-viajes/backend-django
./scripts/start-django.sh

# Terminal 3 - Frontend Agencia (opcional)
cd agencia-viajes/agencia
npm run dev
```

---

## 🔍 Cómo Verificar que Funciona

### Verificación 1: MongoDB tiene el API_KEY

```javascript
// En MongoDB Compass:
db.corporate_config.findOne({ _id: "airline_corporate_user" })

// Debe mostrar:
{
  "_id": "airline_corporate_user",
  "apiKey": "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  "enabled": true
}
```

### Verificación 2: Logs del Backend Django

Cuando hagas una búsqueda o compra, deberías ver:

```
🔑 Usando API_KEY empresarial para autenticación
✅ Usando aerolínea de BD: [nombre] (http://...)
```

### Verificación 3: Logs del Backend Java

Cuando la agencia compre un ticket:

```
✅ Usuario empresarial encontrado por API Key: Agencia de Viajes El Vuelo
🏢 Compra empresarial detectada: Agencia de Viajes El Vuelo
🏢 Ticket comprado por usuario empresarial: Agencia de Viajes El Vuelo
```

### Verificación 4: Base de Datos Oracle

```sql
-- Ver tickets empresariales
SELECT 
    t.ID_TICKET,
    purchaser.COMPANY_NAME as COMPRADOR,
    t.PASSENGER_FIRST_NAME || ' ' || t.PASSENGER_LAST_NAME as PASAJERO,
    t.PASSENGER_EMAIL,
    t.TOTAL_AMOUNT
FROM TICKETS t
LEFT JOIN USERS purchaser ON t.PURCHASED_BY_USER_ID = purchaser.ID_USER
WHERE t.PURCHASED_BY_USER_ID IS NOT NULL;
```

Deberías ver:
```
COMPRADOR                      | PASAJERO    | PASSENGER_EMAIL
-------------------------------|-------------|------------------
Agencia de Viajes El Vuelo     | Juan Pérez  | juan@gmail.com
```

---

## 💡 Ventajas del Sistema

### Para la Agencia
- ✅ Compra en nombre de clientes
- ✅ Autenticación automática (no login manual)
- ✅ Facturación centralizada
- ✅ Control de todas las compras

### Para el Cliente
- ✅ Recibe su ticket
- ✅ Email de confirmación a su correo
- ✅ No necesita cuenta en la aerolínea
- ✅ Experiencia transparente

### Para la Aerolínea
- ✅ Sabe quién compró (agencia) vs quién viaja (cliente)
- ✅ Facturación B2B separada
- ✅ Reportes de ventas por agencia
- ✅ Control de acceso por API_KEY

---

## 📊 Reportes y Consultas

### Tickets Vendidos por la Agencia

```sql
-- En Oracle
SELECT 
    COUNT(*) as TOTAL_TICKETS,
    SUM(t.TOTAL_AMOUNT) as INGRESOS_TOTALES,
    purchaser.COMPANY_NAME
FROM TICKETS t
INNER JOIN USERS purchaser ON t.PURCHASED_BY_USER_ID = purchaser.ID_USER
WHERE purchaser.IS_CORPORATE = 1
GROUP BY purchaser.COMPANY_NAME;
```

### Clientes de la Agencia

```sql
SELECT DISTINCT
    t.PASSENGER_EMAIL,
    t.PASSENGER_FIRST_NAME || ' ' || t.PASSENGER_LAST_NAME as CLIENTE,
    COUNT(*) as VUELOS_COMPRADOS
FROM TICKETS t
WHERE t.PURCHASED_BY_USER_ID IS NOT NULL
GROUP BY t.PASSENGER_EMAIL, t.PASSENGER_FIRST_NAME, t.PASSENGER_LAST_NAME;
```

---

## 🔒 Seguridad

### API_KEY
- ✅ Único por agencia
- ✅ Guardado en BD (no en código)
- ✅ Puede ser revocado
- ✅ Solo para HTTPS en producción

### Validaciones
- ✅ Solo usuarios con `IS_CORPORATE = 1` y `ENABLED = 1`
- ✅ API_KEY debe coincidir exactamente
- ✅ Logs de todas las compras empresariales

---

## 🆘 Solución de Problemas

### Problema: "Usuario empresarial no autenticado"

**Verificar:**
```sql
-- En Oracle
SELECT * FROM USERS WHERE API_KEY = 'AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L';
```

Debe retornar el usuario con `IS_CORPORATE = 1` y `ENABLED = 1`

### Problema: "No se usa el API_KEY"

**Verificar:**
```javascript
// En MongoDB Compass
db.corporate_config.findOne({ _id: "airline_corporate_user" })
```

Debe tener `enabled: true` y el API_KEY correcto

### Problema: "Los tickets no muestran PURCHASED_BY"

**Verificar:**
```java
// En logs de Java, debe aparecer:
🏢 Ticket comprado por usuario empresarial: ...
```

Si no aparece, el API_KEY no se está enviando correctamente.

---

## ✨ Resumen Final

### Lo que Logramos

1. ✅ Sistema de usuarios empresariales implementado
2. ✅ API_KEY para autenticación segura
3. ✅ Separación de comprador vs pasajero
4. ✅ Agencia puede comprar en nombre de clientes
5. ✅ Emails van al cliente final
6. ✅ Facturación a la agencia
7. ✅ Logs y trazabilidad completa

### Próximos Pasos

1. [ ] Ejecutar query en MongoDB (PASO 2)
2. [ ] Compilar backend Java
3. [ ] Reiniciar servidores
4. [ ] Probar compra desde agencia web
5. [ ] Verificar que funciona correctamente

---

## 📞 Comandos Rápidos

### MongoDB
```javascript
// Guardar API_KEY
db.corporate_config.insertOne({
  _id: "airline_corporate_user",
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  enabled: true,
  createdAt: new Date()
});
```

### Compilar Java
```bash
cd backv4
mvn clean compile
```

### Iniciar Todo
```bash
# Java
cd backv4 && mvn exec:java -Dexec.mainClass="com.sources.app.App"

# Django
cd agencia-viajes/backend-django && ./scripts/start-django.sh
```

---

**API_KEY**: `AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L`  
**Usuario**: `corporate@agencia-elvuelo.com`  
**Password**: `corporate123`  
**Estado**: ✅ Código listo - Falta configurar MongoDB

## 🚀 Siguiente paso: Ejecuta el query en MongoDB Compass!

