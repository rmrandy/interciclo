-- ============================================================================
-- VERIFICAR Y CORREGIR COLUMNA CREATED_AT EN TABLA TICKETS
-- Error: ORA-01843: not a valid month
-- Solución: Asegurar que CREATED_AT sea VARCHAR2 o agregar trigger
-- ============================================================================

-- PASO 1: Verificar el tipo de dato actual de CREATED_AT
SELECT 
    column_name, 
    data_type, 
    data_length, 
    nullable
FROM user_tab_columns 
WHERE table_name = 'TICKETS' 
AND column_name IN ('CREATED_AT', 'UPDATED_AT', 'BOOKING_DATE', 'BOOKING_TIME', 'CANCELLATION_DATE');

-- PASO 2: Si CREATED_AT es DATE o TIMESTAMP, convertirla a VARCHAR2
-- Primero respaldar los datos existentes (si los hay)
-- Luego modificar la columna

-- Opción A: Modificar columna a VARCHAR2 (si está como DATE/TIMESTAMP)
-- NOTA: Ejecutar solo si el tipo actual NO es VARCHAR2
-- ALTER TABLE TICKETS MODIFY CREATED_AT VARCHAR2(30);
-- ALTER TABLE TICKETS MODIFY UPDATED_AT VARCHAR2(30);
-- ALTER TABLE TICKETS MODIFY BOOKING_DATE VARCHAR2(30);
-- ALTER TABLE TICKETS MODIFY BOOKING_TIME VARCHAR2(30);
-- ALTER TABLE TICKETS MODIFY CANCELLATION_DATE VARCHAR2(30);

-- PASO 3: Crear trigger para establecer automáticamente CREATED_AT
-- Este trigger se ejecuta antes de insertar un nuevo registro
CREATE OR REPLACE TRIGGER TRG_TICKETS_CREATED_AT
BEFORE INSERT ON TICKETS
FOR EACH ROW
BEGIN
    -- Establecer CREATED_AT si es NULL o vacío
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

-- PASO 4: Crear trigger para establecer automáticamente UPDATED_AT
CREATE OR REPLACE TRIGGER TRG_TICKETS_UPDATED_AT
BEFORE UPDATE ON TICKETS
FOR EACH ROW
BEGIN
    -- Actualizar UPDATED_AT automáticamente
    :NEW.UPDATED_AT := TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS');
END;
/

-- PASO 5: Verificar que los triggers se crearon correctamente
SELECT trigger_name, status, trigger_type 
FROM user_triggers 
WHERE table_name = 'TICKETS';

-- PASO 6: Probar inserción (comentar para producción)
-- INSERT INTO TICKETS (
--     FLIGHT_ID, USER_ID, SEAT_NUMBER, SEAT_CATEGORY, 
--     FARE, STATUS, PAYMENT_STATUS, PAYMENT_METHOD, 
--     TOTAL_AMOUNT, PASSENGER_FIRST_NAME, PASSENGER_LAST_NAME,
--     PASSENGER_DOCUMENT_TYPE, PASSENGER_DOCUMENT_NUMBER,
--     PASSENGER_EMAIL, PASSENGER_PHONE, QUANTITY
-- ) VALUES (
--     201, 1, 'TEST123', 'ECONOMY',
--     100, 'RESERVED', 'PENDING', 'CREDIT_CARD',
--     100, 'Test', 'User',
--     'PASSPORT', '123456',
--     'test@test.com', '12345678', 1
-- );

-- PASO 7: Ver el registro insertado
-- SELECT * FROM TICKETS WHERE SEAT_NUMBER = 'TEST123';

-- PASO 8: Limpiar el registro de prueba
-- DELETE FROM TICKETS WHERE SEAT_NUMBER = 'TEST123';
-- COMMIT;


