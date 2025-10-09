-- ============================================================================
-- CREAR USUARIO EMPRESARIAL PARA AGENCIA DE VIAJES
-- Ejecutar en DBeaver en tu base de datos Oracle
-- ============================================================================

-- PASO 1: Crear el usuario empresarial
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
    'Agencia de Viajes El Vuelo',                                           -- NAME
    1234567890,                                                              -- CUI
    'Agencia',                                                               -- FIRST_NAME
    'El Vuelo',                                                              -- LAST_NAME
    10,                                                                      -- AGE
    'Guatemala',                                                             -- COUNTRY
    'CORP-001',                                                              -- PASSPORT_NUMBER
    '555-0100',                                                              -- PHONE
    'corporate@agencia-elvuelo.com',                                        -- EMAIL
    'Av. Principal 123, Guatemala',                                         -- ADDRESS
    TO_DATE('2015-01-01', 'YYYY-MM-DD'),                                   -- BIRTHDATE
    'CORPORATE',                                                             -- ROL
    1,                                                                       -- ENABLED
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeB1fP.M6YxljzTDK',      -- PASSWORD (hash de 'corporate123')
    1,                                                                       -- PAID_SERVICE
    1,                                                                       -- IS_CORPORATE
    'Agencia de Viajes El Vuelo',                                          -- COMPANY_NAME
    'AGV-' || DBMS_RANDOM.STRING('X', 32),                                 -- API_KEY (generada automáticamente)
    SYSTIMESTAMP                                                            -- CREATED_AT
);

-- PASO 2: Confirmar los cambios
COMMIT;

-- PASO 3: Verificar que se creó correctamente
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
WHERE EMAIL = 'corporate@agencia-elvuelo.com';

-- PASO 4: (IMPORTANTE) Guardar el API_KEY para configurar la agencia
SELECT 
    '🔑 GUARDA ESTE API_KEY:' as NOTA,
    API_KEY,
    'Lo necesitarás para configurar la agencia' as INSTRUCCION
FROM USERS 
WHERE EMAIL = 'corporate@agencia-elvuelo.com';


-- ============================================================================
-- CREDENCIALES DEL USUARIO EMPRESARIAL
-- ============================================================================
/*
Email:    corporate@agencia-elvuelo.com
Password: corporate123
Rol:      CORPORATE
Tipo:     Usuario Empresarial (IS_CORPORATE = 1)
API_KEY:  Se genera automáticamente al insertar
*/


-- ============================================================================
-- VERIFICACIÓN ADICIONAL
-- ============================================================================

-- Ver todos los usuarios empresariales
SELECT COUNT(*) as TOTAL_USUARIOS_EMPRESARIALES
FROM USERS 
WHERE IS_CORPORATE = 1;

-- Ver detalles completos
SELECT 
    ID_USER as "ID",
    NAME as "Nombre",
    COMPANY_NAME as "Empresa",
    EMAIL as "Email",
    ROL as "Rol",
    IS_CORPORATE as "Es Empresarial",
    ENABLED as "Activo",
    API_KEY as "API Key"
FROM USERS 
WHERE IS_CORPORATE = 1;


-- ============================================================================
-- SI NECESITAS REGENERAR EL API_KEY
-- ============================================================================

/*
-- Ejecuta esto solo si necesitas un nuevo API_KEY

UPDATE USERS 
SET API_KEY = 'AGV-' || DBMS_RANDOM.STRING('X', 32)
WHERE EMAIL = 'corporate@agencia-elvuelo.com';

COMMIT;

-- Ver el nuevo API_KEY
SELECT API_KEY FROM USERS WHERE EMAIL = 'corporate@agencia-elvuelo.com';
*/


-- ============================================================================
-- SI NECESITAS ELIMINAR Y RECREAR
-- ============================================================================

/*
-- Solo ejecuta si algo salió mal y quieres empezar de cero

DELETE FROM USERS WHERE EMAIL = 'corporate@agencia-elvuelo.com';
COMMIT;

-- Luego vuelve a ejecutar el INSERT de arriba
*/


-- ============================================================================
-- FIN
-- ============================================================================

