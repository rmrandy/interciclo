-- ============================================================================
-- SCRIPT SIMPLIFICADO: Solo agregar el trigger que falta
-- Este script NO intenta eliminar triggers que no existen
-- ============================================================================

-- PASO 1: Verificar qué triggers existen actualmente
SELECT trigger_name, status 
FROM user_triggers 
WHERE table_name = 'TICKETS'
ORDER BY trigger_name;

-- PASO 2: Crear/Reemplazar el trigger TRG_TICKETS_CREATED_AT (el que está causando el error)
CREATE OR REPLACE TRIGGER AEROLINEA3.TRG_TICKETS_CREATED_AT
BEFORE INSERT ON AEROLINEA3.TICKETS
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

-- PASO 3: Crear/Reemplazar el trigger TRG_TICKETS_UPDATED_AT
CREATE OR REPLACE TRIGGER AEROLINEA3.TRG_TICKETS_UPDATED_AT
BEFORE UPDATE ON AEROLINEA3.TICKETS
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS');
END;
/

-- PASO 4: Verificar que los triggers se crearon correctamente
SELECT 
    trigger_name, 
    status, 
    trigger_type,
    triggering_event
FROM user_triggers 
WHERE table_name = 'TICKETS'
ORDER BY triggering_event, trigger_name;

-- Deberías ver estos triggers con STATUS = ENABLED:
-- TRG_TICKETS_CREATED_AT        | ENABLED | BEFORE EACH ROW | INSERT
-- TRG_TICKETS_AI_DECREMENT      | ENABLED | AFTER EACH ROW  | INSERT
-- TRG_TICKETS_DECREMENT_SEATS   | ENABLED | AFTER EACH ROW  | INSERT (duplicado, ver paso 5)
-- TRG_TICKETS_UPDATED_AT        | ENABLED | BEFORE EACH ROW | UPDATE
-- TRG_TICKETS_AU_STATUS_RESTORE | ENABLED | AFTER EACH ROW  | UPDATE
-- TRG_TICKETS_AD_RESTORE        | ENABLED | AFTER EACH ROW  | DELETE

-- PASO 5 (OPCIONAL): Si ves TRG_TICKETS_DECREMENT_SEATS, elimínalo porque es duplicado
-- Solo ejecuta esto si existe el trigger:
-- DROP TRIGGER AEROLINEA3.TRG_TICKETS_DECREMENT_SEATS;

COMMIT;

-- ============================================================================
-- ¡LISTO! Ahora intenta crear un ticket desde tu aplicación
-- ============================================================================

