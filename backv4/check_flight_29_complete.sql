-- Script para verificar la relación completa entre inventario y precios del vuelo 29
-- Muestra cómo se conectan ambas tablas para el proceso de compra

-- Verificar que ambas tablas tengan datos para el vuelo 29
SELECT '=== VERIFICACIÓN DE DATOS COMPLETOS ===' as INFO FROM DUAL;

-- 1. Inventario de asientos
SELECT '📋 INVENTARIO DE ASIENTOS:' as SECCIÓN FROM DUAL;
SELECT 
    FLIGHT_ID,
    SEAT_CATEGORY,
    TOTAL_SEATS,
    AVAILABLE_SEATS,
    RESERVED_SEATS,
    SOLD_SEATS,
    STATUS
FROM FLIGHT_INVENTORY 
WHERE FLIGHT_ID = 29
ORDER BY SEAT_CATEGORY;

-- 2. Precios configurados
SELECT '💰 PRECIOS CONFIGURADOS:' as SECCIÓN FROM DUAL;
SELECT 
    FLIGHT_ID,
    SEAT_CATEGORY,
    BASE_PRICE,
    TAXES,
    FEES,
    TOTAL_PRICE,
    STATUS
FROM FLIGHT_FARES 
WHERE FLIGHT_ID = 29
ORDER BY SEAT_CATEGORY;

-- 3. JOIN para mostrar la información completa (como la vería el sistema de compra)
SELECT '🔗 INFORMACIÓN COMPLETA PARA COMPRA:' as SECCIÓN FROM DUAL;
SELECT 
    i.FLIGHT_ID,
    i.SEAT_CATEGORY,
    i.TOTAL_SEATS,
    i.AVAILABLE_SEATS,
    f.BASE_PRICE,
    f.TAXES,
    f.FEES,
    f.TOTAL_PRICE as PRECIO_FINAL,
    CASE 
        WHEN i.AVAILABLE_SEATS > 0 THEN 'DISPONIBLE'
        ELSE 'NO DISPONIBLE'
    END as ESTADO_COMPRA
FROM FLIGHT_INVENTORY i
LEFT JOIN FLIGHT_FARES f ON i.FLIGHT_ID = f.FLIGHT_ID 
    AND i.SEAT_CATEGORY = f.SEAT_CATEGORY
WHERE i.FLIGHT_ID = 29
ORDER BY i.SEAT_CATEGORY;

-- 4. Verificar que todas las categorías tengan tanto inventario como precios
SELECT '✅ VERIFICACIÓN DE INTEGRIDAD:' as SECCIÓN FROM DUAL;
SELECT 
    'Categorías con inventario: ' || COUNT(DISTINCT SEAT_CATEGORY) as INVENTARIO
FROM FLIGHT_INVENTORY 
WHERE FLIGHT_ID = 29;

SELECT 
    'Categorías con precios: ' || COUNT(DISTINCT SEAT_CATEGORY) as PRECIOS
FROM FLIGHT_FARES 
WHERE FLIGHT_ID = 29;

-- 5. Mostrar ejemplo de cómo se usaría en el proceso de compra
SELECT '🎫 EJEMPLO DE PROCESO DE COMPRA:' as SECCIÓN FROM DUAL;
SELECT 
    'Para comprar un asiento ' || i.SEAT_CATEGORY || 
    ' del vuelo ' || i.FLIGHT_ID || 
    ': Precio $' || f.TOTAL_PRICE || 
    ' (Disponibles: ' || i.AVAILABLE_SEATS || ')' as PROCESO_COMPRA
FROM FLIGHT_INVENTORY i
JOIN FLIGHT_FARES f ON i.FLIGHT_ID = f.FLIGHT_ID 
    AND i.SEAT_CATEGORY = f.SEAT_CATEGORY
WHERE i.FLIGHT_ID = 29 AND i.AVAILABLE_SEATS > 0
ORDER BY i.SEAT_CATEGORY;
