-- ============================================================================
-- QUERIES PARA IMPLEMENTAR USUARIOS EMPRESARIALES
-- Sistema de Aerolínea - Ensurance Pharmacy
-- Fecha: Octubre 6, 2025
-- 
-- INSTRUCCIONES:
-- 1. Abre DBeaver
-- 2. Conecta a tu base de datos Oracle
-- 3. Ejecuta estos queries EN ORDEN
-- 4. Verifica que no haya errores
-- ============================================================================

-- ============================================================================
-- PASO 1: Agregar campo IS_CORPORATE a tabla USERS
-- Este campo identifica si un usuario es empresarial (agencia de viajes)
-- ============================================================================

-- Verificar si la columna ya existe
SELECT column_name 
FROM user_tab_columns 
WHERE table_name = 'USERS' AND column_name = 'IS_CORPORATE';

-- Si NO existe, agregar la columna
ALTER TABLE USERS 
ADD IS_CORPORATE NUMBER(1,0) DEFAULT 0 CHECK (IS_CORPORATE IN (0,1));

-- Agregar comentario explicativo
COMMENT ON COLUMN USERS.IS_CORPORATE IS 'Indica si el usuario es empresarial (1) o individual (0). Usuarios empresariales pueden comprar en nombre de otros.';


-- ============================================================================
-- PASO 2: Agregar campo COMPANY_NAME para usuarios empresariales
-- Nombre de la empresa/agencia
-- ============================================================================

-- Verificar si existe
SELECT column_name 
FROM user_tab_columns 
WHERE table_name = 'USERS' AND column_name = 'COMPANY_NAME';

-- Agregar columna
ALTER TABLE USERS 
ADD COMPANY_NAME VARCHAR2(200);

-- Agregar comentario
COMMENT ON COLUMN USERS.COMPANY_NAME IS 'Nombre de la empresa/agencia (solo para usuarios empresariales)';


-- ============================================================================
-- PASO 3: Agregar campo API_KEY para autenticación empresarial
-- Token único para que la agencia se autentique
-- ============================================================================

-- Verificar si existe
SELECT column_name 
FROM user_tab_columns 
WHERE table_name = 'USERS' AND column_name = 'API_KEY';

-- Agregar columna
ALTER TABLE USERS 
ADD API_KEY VARCHAR2(100) UNIQUE;

-- Agregar comentario
COMMENT ON COLUMN USERS.API_KEY IS 'API Key única para autenticación de usuarios empresariales';


-- ============================================================================
-- PASO 4: Modificar tabla TICKETS para soportar compras empresariales
-- Agregar campo PURCHASED_BY_USER_ID (quién compró) vs USER_ID (pasajero)
-- ============================================================================

-- Verificar si existe
SELECT column_name 
FROM user_tab_columns 
WHERE table_name = 'TICKETS' AND column_name = 'PURCHASED_BY_USER_ID';

-- Agregar columna (puede ser null para tickets antiguos)
ALTER TABLE TICKETS 
ADD PURCHASED_BY_USER_ID NUMBER(38,0);

-- Agregar foreign key
ALTER TABLE TICKETS 
ADD CONSTRAINT FK_TICKET_PURCHASED_BY 
FOREIGN KEY (PURCHASED_BY_USER_ID) 
REFERENCES USERS(ID_USER);

-- Agregar comentario
COMMENT ON COLUMN TICKETS.PURCHASED_BY_USER_ID IS 'ID del usuario que compró el ticket (puede ser usuario empresarial). Si es NULL, es el mismo que USER_ID';


-- ============================================================================
-- PASO 5: Agregar índices para mejor performance
-- ============================================================================

-- Índice para buscar tickets comprados por un usuario empresarial
CREATE INDEX IDX_TICKETS_PURCHASED_BY ON TICKETS(PURCHASED_BY_USER_ID);

-- Índice para buscar usuarios empresariales
CREATE INDEX IDX_USERS_CORPORATE ON USERS(IS_CORPORATE);

-- Índice para buscar por API_KEY
CREATE INDEX IDX_USERS_API_KEY ON USERS(API_KEY);


-- ============================================================================
-- PASO 6: Crear usuario empresarial de prueba para la agencia
-- ============================================================================

-- Insertar usuario empresarial (AGENCIA DE VIAJES)
INSERT INTO USERS (
    NAME,
    CUI,
    FIRST_NAME,
    LAST_NAME,
    AGE,
    COUNTRY,
    PASSPORT_NUMBER,
    PHONE,
    EMAIL,
    ADDRESS,
    BIRTHDATE,
    ROL,
    ENABLED,
    PASSWORD,
    PAID_SERVICE,
    IS_CORPORATE,
    COMPANY_NAME,
    API_KEY,
    CREATED_AT
) VALUES (
    'Agencia de Viajes El Vuelo',           -- NAME
    1234567890,                              -- CUI (número único)
    'Agencia',                               -- FIRST_NAME
    'El Vuelo',                              -- LAST_NAME
    10,                                      -- AGE (años de la empresa)
    'Guatemala',                             -- COUNTRY
    'CORP-001',                              -- PASSPORT_NUMBER (ID corporativo)
    '555-0100',                              -- PHONE
    'corporate@agencia-elvuelo.com',         -- EMAIL
    'Av. Principal 123, Guatemala',         -- ADDRESS
    TO_DATE('2015-01-01', 'YYYY-MM-DD'),    -- BIRTHDATE (fecha de fundación)
    'CORPORATE',                             -- ROL (nuevo rol empresarial)
    1,                                       -- ENABLED
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeB1fP.M6YxljzTDK',  -- PASSWORD (hash de 'corporate123')
    1,                                       -- PAID_SERVICE
    1,                                       -- IS_CORPORATE ← IMPORTANTE!
    'Agencia de Viajes El Vuelo',          -- COMPANY_NAME
    'AGV-' || DBMS_RANDOM.STRING('X', 32),  -- API_KEY generada automáticamente
    SYSTIMESTAMP                             -- CREATED_AT
);

-- Confirmar inserción
COMMIT;

-- Verificar que se creó correctamente
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


-- ============================================================================
-- PASO 7: Ver estructura actualizada de USERS
-- ============================================================================

SELECT 
    column_name,
    data_type,
    data_length,
    nullable,
    data_default
FROM user_tab_columns 
WHERE table_name = 'USERS'
AND column_name IN ('IS_CORPORATE', 'COMPANY_NAME', 'API_KEY')
ORDER BY column_name;


-- ============================================================================
-- PASO 8: Ver estructura actualizada de TICKETS
-- ============================================================================

SELECT 
    column_name,
    data_type,
    data_length,
    nullable
FROM user_tab_columns 
WHERE table_name = 'TICKETS'
AND column_name = 'PURCHASED_BY_USER_ID';


-- ============================================================================
-- QUERIES ÚTILES PARA ADMINISTRACIÓN
-- ============================================================================

-- Ver todos los usuarios empresariales
SELECT 
    ID_USER,
    NAME,
    COMPANY_NAME,
    EMAIL,
    API_KEY,
    ENABLED,
    CREATED_AT
FROM USERS 
WHERE IS_CORPORATE = 1
ORDER BY CREATED_AT DESC;

-- Ver tickets comprados por usuarios empresariales
SELECT 
    t.ID_TICKET,
    t.BOOKING_DATE,
    purchaser.COMPANY_NAME as COMPRADOR,
    passenger.NAME as PASAJERO,
    t.PASSENGER_FIRST_NAME || ' ' || t.PASSENGER_LAST_NAME as NOMBRE_PASAJERO,
    t.PASSENGER_EMAIL,
    t.TOTAL_AMOUNT,
    t.STATUS
FROM TICKETS t
LEFT JOIN USERS purchaser ON t.PURCHASED_BY_USER_ID = purchaser.ID_USER
LEFT JOIN USERS passenger ON t.USER_ID = passenger.ID_USER
WHERE t.PURCHASED_BY_USER_ID IS NOT NULL
ORDER BY t.BOOKING_DATE DESC;

-- Regenerar API_KEY para un usuario empresarial (si es necesario)
UPDATE USERS 
SET API_KEY = 'AGV-' || DBMS_RANDOM.STRING('X', 32),
    UPDATED_AT = SYSTIMESTAMP
WHERE EMAIL = 'corporate@agencia-elvuelo.com';

COMMIT;

-- Obtener API_KEY de un usuario empresarial
SELECT 
    ID_USER,
    COMPANY_NAME,
    EMAIL,
    API_KEY,
    'Usa esta API_KEY en la configuración de la agencia' as NOTA
FROM USERS 
WHERE EMAIL = 'corporate@agencia-elvuelo.com';


-- ============================================================================
-- PASO 9: (OPCIONAL) Actualizar tickets existentes
-- Si quieres que los tickets antiguos también tengan PURCHASED_BY_USER_ID
-- ============================================================================

-- Actualizar tickets donde el comprador es el mismo que el pasajero
UPDATE TICKETS 
SET PURCHASED_BY_USER_ID = USER_ID
WHERE PURCHASED_BY_USER_ID IS NULL;

COMMIT;


-- ============================================================================
-- PASO 10: Verificación final
-- ============================================================================

-- Verificar cambios en USERS
SELECT 
    'USERS' as TABLA,
    COUNT(*) as TOTAL_USUARIOS,
    SUM(CASE WHEN IS_CORPORATE = 1 THEN 1 ELSE 0 END) as USUARIOS_EMPRESARIALES,
    SUM(CASE WHEN IS_CORPORATE = 0 THEN 1 ELSE 0 END) as USUARIOS_INDIVIDUALES
FROM USERS;

-- Verificar cambios en TICKETS
SELECT 
    'TICKETS' as TABLA,
    COUNT(*) as TOTAL_TICKETS,
    SUM(CASE WHEN PURCHASED_BY_USER_ID IS NOT NULL THEN 1 ELSE 0 END) as TICKETS_EMPRESARIALES,
    SUM(CASE WHEN PURCHASED_BY_USER_ID IS NULL THEN 1 ELSE 0 END) as TICKETS_INDIVIDUALES
FROM TICKETS;


-- ============================================================================
-- ROLLBACK (SOLO SI NECESITAS DESHACER LOS CAMBIOS)
-- ¡NO EJECUTAR SI TODO ESTÁ BIEN!
-- ============================================================================

/*
-- ADVERTENCIA: Esto deshace todos los cambios
-- Solo ejecuta si algo salió mal

-- Eliminar usuario empresarial de prueba
DELETE FROM USERS WHERE IS_CORPORATE = 1;

-- Eliminar foreign key
ALTER TABLE TICKETS DROP CONSTRAINT FK_TICKET_PURCHASED_BY;

-- Eliminar columnas de TICKETS
ALTER TABLE TICKETS DROP COLUMN PURCHASED_BY_USER_ID;

-- Eliminar índices
DROP INDEX IDX_TICKETS_PURCHASED_BY;
DROP INDEX IDX_USERS_CORPORATE;
DROP INDEX IDX_USERS_API_KEY;

-- Eliminar columnas de USERS
ALTER TABLE USERS DROP COLUMN API_KEY;
ALTER TABLE USERS DROP COLUMN COMPANY_NAME;
ALTER TABLE USERS DROP COLUMN IS_CORPORATE;

COMMIT;
*/


-- ============================================================================
-- RESUMEN DE CAMBIOS
-- ============================================================================

/*
TABLA USERS - Nuevas columnas:
  ✅ IS_CORPORATE (NUMBER(1,0)) - Indica si es usuario empresarial
  ✅ COMPANY_NAME (VARCHAR2(200)) - Nombre de la empresa
  ✅ API_KEY (VARCHAR2(100) UNIQUE) - Token de autenticación

TABLA TICKETS - Nueva columna:
  ✅ PURCHASED_BY_USER_ID (NUMBER(38,0)) - Quién compró el ticket

NUEVO ROL:
  ✅ 'CORPORATE' - Para usuarios empresariales

USUARIO CREADO:
  ✅ Agencia de Viajes El Vuelo
  ✅ Email: corporate@agencia-elvuelo.com
  ✅ Password: corporate123
  ✅ API_KEY: AGV-[random]
  ✅ IS_CORPORATE: 1
*/

-- ============================================================================
-- FIN DE LOS QUERIES
-- ============================================================================

