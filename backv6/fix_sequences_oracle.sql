-- Script para crear secuencias en Oracle para auto-generación de IDs
-- Ejecuta este script en tu base de datos Oracle antes de usar el backend

-- =====================================================
-- SECUENCIAS PARA TABLAS CRÍTICAS
-- =====================================================

-- Secuencia para TICKETS
CREATE SEQUENCE SEQ_TICKETS
START WITH 1000
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Secuencia para CLICK_EVENTS
CREATE SEQUENCE SEQ_CLICK_EVENTS
START WITH 1000
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- Secuencia para CITIES
CREATE SEQUENCE SEQ_CITIES
START WITH 1000
INCREMENT BY 1
NOCACHE
NOCYCLE;

-- =====================================================
-- VERIFICAR QUE LAS SECUENCIAS SE CREARON CORRECTAMENTE
-- =====================================================

-- Ver todas las secuencias creadas
SELECT sequence_name, last_number, increment_by
FROM user_sequences
WHERE sequence_name IN ('SEQ_TICKETS', 'SEQ_CLICK_EVENTS', 'SEQ_CITIES');

-- =====================================================
-- OPCIONAL: Ajustar el valor inicial si ya tienes datos
-- =====================================================

-- Si ya tienes datos en la tabla TICKETS, ajusta la secuencia:
-- DECLARE
--   v_max_id NUMBER;
-- BEGIN
--   SELECT NVL(MAX(ID_TICKET), 0) + 1 INTO v_max_id FROM TICKETS;
--   EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_TICKETS';
--   EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_TICKETS START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
-- END;
-- /

-- Si ya tienes datos en CLICK_EVENTS:
-- DECLARE
--   v_max_id NUMBER;
-- BEGIN
--   SELECT NVL(MAX(ID_CLICK), 0) + 1 INTO v_max_id FROM CLICK_EVENTS;
--   EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_CLICK_EVENTS';
--   EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_CLICK_EVENTS START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
-- END;
-- /

-- Si ya tienes datos en CITIES:
-- DECLARE
--   v_max_id NUMBER;
-- BEGIN
--   SELECT NVL(MAX(ID_CITY), 0) + 1 INTO v_max_id FROM CITIES;
--   EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_CITIES';
--   EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_CITIES START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
-- END;
-- /

COMMIT;








