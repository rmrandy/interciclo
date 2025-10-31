-- ============================================================================
-- AJUSTAR SECUENCIAS DE AEROLINEA4
-- 
-- ESQUEMA: AEROLINEA4
-- EJECUTAR: En DBeaver conectado al esquema AEROLINEA4
-- 
-- Este script ajusta las secuencias para evitar duplicados de IDs
-- ============================================================================

-- Conectarse a: AEROLINEA4
-- Tablas afectadas: CITIES, TICKETS, FLIGHTS, USERS

DECLARE
    v_max_id NUMBER;
BEGIN
    -- ========================================
    -- TABLA: AEROLINEA4.CITIES
    -- SECUENCIA: SEQ_CITIES
    -- ========================================
    SELECT NVL(MAX(ID_CITY), 0) + 1 INTO v_max_id FROM CITIES;
    EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_CITIES';
    EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_CITIES START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
    DBMS_OUTPUT.PUT_LINE('✅ SEQ_CITIES ajustada a: ' || v_max_id);
    
    -- ========================================
    -- TABLA: AEROLINEA4.TICKETS
    -- SECUENCIA: SEQ_TICKETS
    -- ========================================
    SELECT NVL(MAX(ID_TICKET), 0) + 1 INTO v_max_id FROM TICKETS;
    EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_TICKETS';
    EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_TICKETS START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
    DBMS_OUTPUT.PUT_LINE('✅ SEQ_TICKETS ajustada a: ' || v_max_id);
    
    -- ========================================
    -- TABLA: AEROLINEA4.FLIGHTS
    -- SECUENCIA: SEQ_FLIGHTS
    -- ========================================
    SELECT NVL(MAX(ID_FLIGHT), 0) + 1 INTO v_max_id FROM FLIGHTS;
    EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_FLIGHTS';
    EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_FLIGHTS START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
    DBMS_OUTPUT.PUT_LINE('✅ SEQ_FLIGHTS ajustada a: ' || v_max_id);
    
    -- ========================================
    -- TABLA: AEROLINEA4.USERS
    -- SECUENCIA: SEQ_USERS
    -- ========================================
    SELECT NVL(MAX(ID_USER), 0) + 1 INTO v_max_id FROM USERS;
    EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_USERS';
    EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_USERS START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
    DBMS_OUTPUT.PUT_LINE('✅ SEQ_USERS ajustada a: ' || v_max_id);
END;
/

-- Verificar que las secuencias se ajustaron correctamente
SELECT sequence_name, last_number
FROM user_sequences
WHERE sequence_name IN ('SEQ_CITIES', 'SEQ_TICKETS', 'SEQ_FLIGHTS', 'SEQ_USERS')
ORDER BY sequence_name;

COMMIT;

-- ============================================================================
-- ✅ SECUENCIAS AJUSTADAS EN AEROLINEA4
-- ============================================================================

