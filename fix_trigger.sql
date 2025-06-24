-- Script para deshabilitar el trigger problemático
-- Ejecutar como usuario FARMACIA

-- 1. Deshabilitar el trigger problemático
ALTER TRIGGER FARMACIA.UPDATE_STOCK_ON_ORDER_COMPLETION DISABLE;

-- 2. Verificar que el trigger esté deshabilitado
SELECT TRIGGER_NAME, STATUS 
FROM USER_TRIGGERS 
WHERE TRIGGER_NAME = 'UPDATE_STOCK_ON_ORDER_COMPLETION';

-- 3. Alternativamente, si quieres eliminar el trigger completamente:
-- DROP TRIGGER FARMACIA.UPDATE_STOCK_ON_ORDER_COMPLETION;

-- 4. Verificar que no hay otros triggers en la tabla ORDERS
SELECT TRIGGER_NAME, STATUS 
FROM USER_TRIGGERS 
WHERE TABLE_NAME = 'ORDERS'; 