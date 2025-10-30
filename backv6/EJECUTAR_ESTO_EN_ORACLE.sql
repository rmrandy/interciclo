-- ============================================================================
-- SCRIPT PARA SOLUCIONAR ERROR ORA-01843: not a valid month
-- EJECUTAR ESTE SCRIPT COMPLETO EN DBEAVER O SQL DEVELOPER
-- ============================================================================

-- PASO 1: Crear trigger para CREATED_AT, BOOKING_DATE y BOOKING_TIME
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

-- PASO 2: Crear trigger para UPDATED_AT
CREATE OR REPLACE TRIGGER TRG_TICKETS_UPDATED_AT
BEFORE UPDATE ON TICKETS
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS');
END;
/

-- PASO 3: Verificar que los triggers se crearon correctamente
SELECT trigger_name, status, trigger_type 
FROM user_triggers 
WHERE table_name = 'TICKETS';

-- Deberías ver:
-- TRG_TICKETS_CREATED_AT    | ENABLED | BEFORE EACH ROW
-- TRG_TICKETS_UPDATED_AT    | ENABLED | BEFORE EACH ROW

-- ============================================================================
-- ¡LISTO! Ahora puedes probar la compra desde la aerolínea o la agencia
-- ============================================================================


