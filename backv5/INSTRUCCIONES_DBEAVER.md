# 📊 Instrucciones para DBeaver - Usuarios Empresariales

## 🎯 Objetivo

Modificar la base de datos Oracle para soportar **Usuarios Empresariales** que permitan a la agencia comprar tickets en nombre de sus clientes.

---

## 📋 Pasos en DBeaver

### Paso 1: Abrir DBeaver
1. Abre DBeaver
2. Conecta a tu base de datos Oracle
3. Verifica que estés en el esquema correcto

### Paso 2: Crear Nueva Consulta SQL
1. Clic derecho en tu conexión → "SQL Editor" → "New SQL Script"
2. O usa el atajo: **Cmd+]** (macOS) o **Ctrl+]** (Windows/Linux)

### Paso 3: Copiar y Ejecutar Queries

Abre el archivo: `QUERIES_USUARIOS_EMPRESARIALES.sql`

**Ejecuta los queries en este orden:**

#### 🟢 Query 1: Agregar IS_CORPORATE
```sql
ALTER TABLE USERS 
ADD IS_CORPORATE NUMBER(1,0) DEFAULT 0 CHECK (IS_CORPORATE IN (0,1));

COMMENT ON COLUMN USERS.IS_CORPORATE IS 'Indica si el usuario es empresarial (1) o individual (0)';
```

#### 🟢 Query 2: Agregar COMPANY_NAME
```sql
ALTER TABLE USERS 
ADD COMPANY_NAME VARCHAR2(200);

COMMENT ON COLUMN USERS.COMPANY_NAME IS 'Nombre de la empresa/agencia';
```

#### 🟢 Query 3: Agregar API_KEY
```sql
ALTER TABLE USERS 
ADD API_KEY VARCHAR2(100) UNIQUE;

COMMENT ON COLUMN USERS.API_KEY IS 'API Key única para autenticación empresarial';
```

#### 🟢 Query 4: Modificar Tabla TICKETS
```sql
ALTER TABLE TICKETS 
ADD PURCHASED_BY_USER_ID NUMBER(38,0);

ALTER TABLE TICKETS 
ADD CONSTRAINT FK_TICKET_PURCHASED_BY 
FOREIGN KEY (PURCHASED_BY_USER_ID) 
REFERENCES USERS(ID_USER);

COMMENT ON COLUMN TICKETS.PURCHASED_BY_USER_ID IS 'ID del usuario que compró el ticket';
```

#### 🟢 Query 5: Crear Índices
```sql
CREATE INDEX IDX_TICKETS_PURCHASED_BY ON TICKETS(PURCHASED_BY_USER_ID);
CREATE INDEX IDX_USERS_CORPORATE ON USERS(IS_CORPORATE);
CREATE INDEX IDX_USERS_API_KEY ON USERS(API_KEY);
```

#### 🟢 Query 6: Crear Usuario Empresarial de Prueba
```sql
INSERT INTO USERS (
    NAME, CUI, FIRST_NAME, LAST_NAME, AGE, COUNTRY,
    PASSPORT_NUMBER, PHONE, EMAIL, ADDRESS, BIRTHDATE,
    ROL, ENABLED, PASSWORD, PAID_SERVICE,
    IS_CORPORATE, COMPANY_NAME, API_KEY, CREATED_AT
) VALUES (
    'Agencia de Viajes El Vuelo',
    1234567890,
    'Agencia',
    'El Vuelo',
    10,
    'Guatemala',
    'CORP-001',
    '555-0100',
    'corporate@agencia-elvuelo.com',
    'Av. Principal 123, Guatemala',
    TO_DATE('2015-01-01', 'YYYY-MM-DD'),
    'CORPORATE',
    1,
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeB1fP.M6YxljzTDK',
    1,
    1,
    'Agencia de Viajes El Vuelo',
    'AGV-' || DBMS_RANDOM.STRING('X', 32),
    SYSTIMESTAMP
);

COMMIT;
```

#### 🟢 Query 7: Verificar Usuario Creado
```sql
SELECT 
    ID_USER,
    NAME,
    EMAIL,
    ROL,
    IS_CORPORATE,
    COMPANY_NAME,
    API_KEY,
    CREATED_AT
FROM USERS 
WHERE IS_CORPORATE = 1;
```

**Deberías ver:**
```
ID_USER | NAME                      | EMAIL                          | ROL       | IS_CORPORATE | COMPANY_NAME              | API_KEY
--------|---------------------------|--------------------------------|-----------|--------------|---------------------------|------------------
123     | Agencia de Viajes El Vuelo| corporate@agencia-elvuelo.com  | CORPORATE | 1            | Agencia de Viajes El Vuelo| AGV-XXXXXXXX...
```

---

## ✅ Verificación

### Verificar Cambios en USERS
```sql
-- Ver estructura de USERS
DESC USERS;

-- Ver usuarios empresariales
SELECT ID_USER, NAME, EMAIL, IS_CORPORATE, COMPANY_NAME, API_KEY
FROM USERS 
WHERE IS_CORPORATE = 1;
```

### Verificar Cambios en TICKETS
```sql
-- Ver estructura de TICKETS
DESC TICKETS;

-- Ver columna nueva
SELECT column_name, data_type, nullable
FROM user_tab_columns 
WHERE table_name = 'TICKETS' 
AND column_name = 'PURCHASED_BY_USER_ID';
```

---

## 🔧 En Caso de Errores

### Error: "Column already exists"
**Causa**: Ya ejecutaste este query antes

**Solución**: 
```sql
-- Verifica si ya existe
SELECT column_name FROM user_tab_columns 
WHERE table_name = 'USERS' AND column_name = 'IS_CORPORATE';

-- Si existe, omite ese ALTER TABLE
```

### Error: "Insufficient privileges"
**Causa**: Tu usuario no tiene permisos

**Solución**: Pide al DBA que ejecute los queries o que te dé permisos

### Error: "Unique constraint violated"
**Causa**: Ya existe un usuario con ese email

**Solución**: Cambia el email en el INSERT o elimina el usuario existente:
```sql
DELETE FROM USERS WHERE EMAIL = 'corporate@agencia-elvuelo.com';
COMMIT;
```

---

## 📊 Estructura Final

### Tabla USERS (campos nuevos)
| Columna | Tipo | Nullable | Descripción |
|---------|------|----------|-------------|
| IS_CORPORATE | NUMBER(1,0) | NO | 1 = empresarial, 0 = individual |
| COMPANY_NAME | VARCHAR2(200) | SÍ | Nombre de la empresa |
| API_KEY | VARCHAR2(100) | SÍ | Token único para API |

### Tabla TICKETS (campos nuevos)
| Columna | Tipo | Nullable | Descripción |
|---------|------|----------|-------------|
| PURCHASED_BY_USER_ID | NUMBER(38,0) | SÍ | ID del comprador (FK a USERS) |

---

## 📝 Datos Importantes

### Usuario Empresarial Creado

**Credenciales:**
- Email: `corporate@agencia-elvuelo.com`
- Password: `corporate123`
- Rol: `CORPORATE`

**Para obtener el API_KEY:**
```sql
SELECT API_KEY FROM USERS WHERE EMAIL = 'corporate@agencia-elvuelo.com';
```

**Guarda este API_KEY** - lo necesitarás para configurar la agencia.

---

## 🎯 Concepto

### Antes (Usuario Individual)
```
Usuario → Compra → Ticket
(Comprador = Pasajero)
```

### Ahora (Usuario Empresarial)
```
Agencia (Usuario Empresarial) → Compra → Ticket
                                            ↓
                                    Pasajero (Cliente de la agencia)

PURCHASED_BY_USER_ID = ID de la Agencia
USER_ID = ID del pasajero (o de la agencia si no hay pasajero específico)
PASSENGER_FIRST_NAME = Nombre del cliente
PASSENGER_EMAIL = Email del cliente
```

---

## 💡 Ejemplo de Ticket Empresarial

```sql
INSERT INTO TICKETS (
    FLIGHT_ID,
    USER_ID,                    -- ID del usuario pasajero
    PURCHASED_BY_USER_ID,       -- ID de la agencia (usuario empresarial)
    SEAT_CATEGORY,
    FARE,
    PASSENGER_FIRST_NAME,       -- Nombre del cliente real
    PASSENGER_LAST_NAME,
    PASSENGER_EMAIL,            -- Email del cliente real
    PASSENGER_PHONE,
    PAYMENT_METHOD,
    TOTAL_AMOUNT,
    STATUS
) VALUES (
    1,                          -- Vuelo
    123,                        -- Usuario empresarial (agencia)
    123,                        -- Comprado por la agencia
    'ECONOMY',
    250.00,
    'Juan',                     -- Cliente real
    'Pérez',
    'juan.perez@gmail.com',     -- Email del cliente
    '555-1234',
    'CORPORATE_ACCOUNT',
    250.00,
    'CONFIRMED'
);

COMMIT;
```

---

## ✨ Resumen de Ejecución

### Orden de Ejecución en DBeaver

1. ✅ Ejecutar Query 1 (IS_CORPORATE)
2. ✅ Ejecutar Query 2 (COMPANY_NAME)
3. ✅ Ejecutar Query 3 (API_KEY)
4. ✅ Ejecutar Query 4 (PURCHASED_BY_USER_ID)
5. ✅ Ejecutar Query 5 (Índices)
6. ✅ Ejecutar Query 6 (Crear usuario empresarial)
7. ✅ Ejecutar Query 7 (Verificar)

### Tiempo Estimado
- **5-10 minutos** para ejecutar todos los queries
- **Sin downtime** requerido
- **Compatible** con datos existentes

---

## 🔍 Queries de Verificación Rápida

```sql
-- ¿Se agregaron las columnas?
SELECT COUNT(*) as COLUMNAS_NUEVAS
FROM user_tab_columns 
WHERE table_name = 'USERS' 
AND column_name IN ('IS_CORPORATE', 'COMPANY_NAME', 'API_KEY');
-- Debe retornar: 3

-- ¿Se creó el usuario empresarial?
SELECT COUNT(*) as USUARIOS_EMPRESARIALES
FROM USERS 
WHERE IS_CORPORATE = 1;
-- Debe retornar: al menos 1

-- ¿Se puede hacer JOIN correctamente?
SELECT COUNT(*) 
FROM TICKETS t
LEFT JOIN USERS u ON t.PURCHASED_BY_USER_ID = u.ID_USER;
-- Debe ejecutar sin errores
```

---

## 📞 Soporte

Si encuentras algún error:
1. Lee el mensaje de error
2. Verifica la sintaxis
3. Asegúrate de tener permisos
4. Consulta el archivo `QUERIES_USUARIOS_EMPRESARIALES.sql`

---

**Archivo SQL**: `QUERIES_USUARIOS_EMPRESARIALES.sql`  
**Tiempo estimado**: 5-10 minutos  
**Downtime**: No requerido  
**Reversible**: Sí (ver sección ROLLBACK)

## 🚀 ¡Ejecuta los queries y avísame cuando termines!

