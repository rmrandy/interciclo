-- ============================================================================
-- SOLUCIÓN RÁPIDA: Reparar trigger TRG_TICKETS_CREATED_AT inválido
-- Error: ORA-04098: trigger 'AEROLINEA3.TRG_TICKETS_CREATED_AT' is invalid
-- ============================================================================

-- PASO 1: Eliminar el trigger inválido
DROP TRIGGER TRG_TICKETS_CREATED_AT;

-- PASO 2: Recrear el trigger correctamente
CREATE OR REPLACE TRIGGER TRG_TICKETS_CREATED_AT
BEFORE INSERT ON TICKETS
FOR EACH ROW
BEGIN
    -- Establecer CREATED_AT si es NULL
    IF :NEW.CREATED_AT IS NULL THEN
        :NEW.CREATED_AT := TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS');
    END IF;
    
    -- Establecer BOOKING_DATE si es NULL
    IF :NEW.BOOKING_DATE IS NULL THEN
        :NEW.BOOKING_DATE := TO_CHAR(SYSDATE, 'YYYY-MM-DD');
    END IF;
    
    -- Establecer BOOKING_TIME si es NULL
    IF :NEW.BOOKING_TIME IS NULL THEN
        :NEW.BOOKING_TIME := TO_CHAR(SYSTIMESTAMP, 'HH24:MI:SS');
    END IF;
END;
/

-- PASO 3: Verificar el estado del trigger
SELECT 
    trigger_name, 
    status, 
    trigger_type,
    triggering_event
FROM user_triggers 
WHERE trigger_name = 'TRG_TICKETS_CREATED_AT';

-- Deberías ver:
-- TRG_TICKETS_CREATED_AT | ENABLED | BEFORE EACH ROW | INSERT

-- PASO 4: Ver todos los triggers de la tabla TICKETS
SELECT 
    trigger_name, 
    status, 
    trigger_type,
    triggering_event
FROM user_triggers 
WHERE table_name = 'TICKETS'
ORDER BY trigger_name;

-- ============================================================================
-- Si el trigger se crea correctamente, deberías poder crear tickets sin error
-- ============================================================================

