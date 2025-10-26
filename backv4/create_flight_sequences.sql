-- ============================================
-- Script para crear secuencias de vuelos
-- Ejecutar en DBeaver conectado como AEROLINEA
-- ============================================

-- 1. FLIGHT_INVENTORY_SEQ
CREATE SEQUENCE AEROLINEA.FLIGHT_INVENTORY_SEQ
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;

-- 2. FLIGHT_FARES_SEQ  
CREATE SEQUENCE AEROLINEA.FLIGHT_FARES_SEQ
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;

-- 3. FLIGHT_LEGS_SEQ
CREATE SEQUENCE AEROLINEA.FLIGHT_LEGS_SEQ
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;

-- ============================================
-- Verificar que se crearon correctamente
-- ============================================
SELECT sequence_name, last_number 
FROM user_sequences 
WHERE sequence_name IN ('FLIGHT_INVENTORY_SEQ', 'FLIGHT_FARES_SEQ', 'FLIGHT_LEGS_SEQ', 'FLIGHTS_SEQ')
ORDER BY sequence_name;

