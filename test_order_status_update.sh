#!/bin/bash

# Configuración
BASE_URL="http://localhost:8080/api2"

echo "🧪 Probando actualización de estado de pedidos..."
echo ""

# 1. Obtener todos los pedidos
echo "1. Obteniendo lista de pedidos..."
ORDERS_RESPONSE=$(curl -s "${BASE_URL}/orders")
echo "Respuesta: $ORDERS_RESPONSE"
echo ""

# Extraer el primer ID de pedido (asumiendo que hay al menos uno)
ORDER_ID=$(echo "$ORDERS_RESPONSE" | grep -o '"idOrder":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$ORDER_ID" ]; then
    echo "❌ No se encontraron pedidos para probar"
    exit 1
fi

echo "✅ Pedido encontrado: ID=$ORDER_ID"

# 2. Probar actualización de estado
echo ""
echo "2. Probando actualización de estado..."
UPDATE_RESPONSE=$(curl -s -X PUT \
    -H "Content-Type: application/json" \
    -d '{"status":"enviado"}' \
    "${BASE_URL}/orders/${ORDER_ID}/status")

echo "Respuesta de actualización: $UPDATE_RESPONSE"
echo ""

# 3. Verificar que el cambio se aplicó
echo "3. Verificando que el cambio se aplicó..."
VERIFY_RESPONSE=$(curl -s "${BASE_URL}/orders?id=${ORDER_ID}")
echo "Respuesta de verificación: $VERIFY_RESPONSE"

# Verificar si el estado se actualizó
if echo "$VERIFY_RESPONSE" | grep -q '"status":"enviado"'; then
    echo "✅ Verificación exitosa: el estado se actualizó correctamente"
else
    echo "❌ Error: el estado no se actualizó correctamente"
fi 