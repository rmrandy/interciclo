#!/bin/bash

# Script para probar el sistema de notificaciones de vuelos cancelados

echo "🧪 Probando Sistema de Notificaciones de Vuelos Cancelados"
echo "=========================================================="
echo ""

# URL del backend (ajusta si usas otro puerto)
BACKEND_URL="http://localhost:5001"

echo "📧 Creando notificación de prueba..."
echo ""

# Crear notificación de prueba
curl -X POST ${BACKEND_URL}/api/flight-cancellations \
  -H "Content-Type: application/json" \
  -d '{
    "flightId": 9999,
    "flightNumber": "TEST-'$(date +%H%M)'",
    "airlineCode": "AEROLINEA3",
    "airlineName": "Aerolínea de Prueba",
    "originCity": "Guatemala",
    "destinationCity": "México",
    "departureDate": "2024-12-25",
    "departureTime": "10:00",
    "cancellationReason": "Prueba del sistema de notificaciones - '$(date +%Y-%m-%d\ %H:%M:%S)'"
  }' \
  | python3 -m json.tool

echo ""
echo "=========================================================="
echo "✅ Notificación creada exitosamente"
echo ""
echo "📋 Próximos pasos:"
echo "   1. Ve a la agencia: http://localhost:5173"
echo "   2. Inicia sesión como admin"
echo "   3. Verás la campana 🔔 con badge en el header"
echo "   4. Haz click en la campana para ver la notificación"
echo "   5. O ve a: http://localhost:5173/admin/cancelled-flights"
echo ""

