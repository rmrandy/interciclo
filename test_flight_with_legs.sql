-- Script para probar la creación de un vuelo con escala
-- Primero, crear un vuelo base
INSERT INTO "AEROLINEA"."FLIGHTS" (
    "FLIGHT_NUMBER", 
    "ORIGIN_CITY_ID", 
    "DESTINATION_CITY_ID", 
    "DEPARTURE_DATE", 
    "DEPARTURE_TIME", 
    "ARRIVAL_DATE", 
    "ARRIVAL_TIME", 
    "BASE_PRICE", 
    "AVAILABLE_SEATS", 
    "STATUS", 
    "GATE", 
    "TERMINAL", 
    "CHECK_IN_START", 
    "CHECK_IN_END", 
    "BOARDING_TIME", 
    "CREATED_BY", 
    "CREATED_AT", 
    "UPDATED_AT"
) VALUES (
    'AV789', 
    1, -- Ciudad de Guatemala
    3, -- Miami
    '2025-08-25', 
    '08:00', 
    '2025-08-25', 
    '18:30', 
    450.00, 
    180, 
    'SCHEDULED', 
    'B12', 
    '2', 
    '06:00', 
    '07:30', 
    '07:45', 
    1, -- Usuario creador
    '2025-08-17T10:00:00', 
    '2025-08-17T10:00:00'
);

-- Obtener el ID del vuelo creado
-- SELECT "ID_FLIGHT" FROM "AEROLINEA"."FLIGHTS" WHERE "FLIGHT_NUMBER" = 'AV789';

-- Ahora crear las escalas para este vuelo
-- Primera escala: Ciudad de Panamá
INSERT INTO "AEROLINEA"."FLIGHT_LEGS" (
    "FLIGHT_ID", 
    "CITY_ID", 
    "LEG_ORDER", 
    "ARRIVAL_TIME", 
    "DEPARTURE_TIME", 
    "CONNECTION_TIME_MINUTES", 
    "AIRCRAFT_CHANGE", 
    "CREATED_BY", 
    "CREATED_AT", 
    "UPDATED_AT"
) VALUES (
    (SELECT "ID_FLIGHT" FROM "AEROLINEA"."FLIGHTS" WHERE "FLIGHT_NUMBER" = 'AV789'), 
    2, -- Ciudad de Panamá
    1, -- Primera escala
    '10:30', -- Llegada a Panamá
    '11:45', -- Salida de Panamá
    75, -- 75 minutos de conexión
    'N', -- No hay cambio de aeronave
    1, -- Usuario creador
    '2025-08-17T10:00:00', 
    '2025-08-17T10:00:00'
);

-- Segunda escala: Destino final (Miami)
INSERT INTO "AEROLINEA"."FLIGHT_LEGS" (
    "FLIGHT_ID", 
    "CITY_ID", 
    "LEG_ORDER", 
    "ARRIVAL_TIME", 
    "DEPARTURE_TIME", 
    "CONNECTION_TIME_MINUTES", 
    "AIRCRAFT_CHANGE", 
    "CREATED_BY", 
    "CREATED_AT", 
    "UPDATED_AT"
) VALUES (
    (SELECT "ID_FLIGHT" FROM "AEROLINEA"."FLIGHTS" WHERE "FLIGHT_NUMBER" = 'AV789'), 
    3, -- Miami
    2, -- Destino final
    '18:30', -- Llegada a Miami
    NULL, -- No hay salida (es el destino final)
    0, -- No hay conexión
    'N', -- No hay cambio de aeronave
    1, -- Usuario creador
    '2025-08-17T10:00:00', 
    '2025-08-17T10:00:00'
);

-- Verificar que se crearon correctamente
SELECT 
    f."FLIGHT_NUMBER",
    f."GATE",
    f."TERMINAL",
    f."CHECK_IN_START",
    f."CHECK_IN_END",
    f."BOARDING_TIME",
    fl."LEG_ORDER",
    c."NAME" as "ESCALA_CIUDAD",
    fl."ARRIVAL_TIME",
    fl."DEPARTURE_TIME",
    fl."CONNECTION_TIME_MINUTES"
FROM "AEROLINEA"."FLIGHTS" f
LEFT JOIN "AEROLINEA"."FLIGHT_LEGS" fl ON f."ID_FLIGHT" = fl."FLIGHT_ID"
LEFT JOIN "AEROLINEA"."CITIES" c ON fl."CITY_ID" = c."ID_CITY"
WHERE f."FLIGHT_NUMBER" = 'AV789'
ORDER BY fl."LEG_ORDER";
