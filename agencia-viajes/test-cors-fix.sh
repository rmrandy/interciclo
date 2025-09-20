#!/bin/bash

echo "🧪 Probando solución de CORS..."
echo ""

# Obtener IP de red
NETWORK_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -1)
echo "🌐 IP de red detectada: $NETWORK_IP"
echo ""

# Probar backend
echo "🔍 Probando backend en http://$NETWORK_IP:5001/health"
if curl -s -f "http://$NETWORK_IP:5001/health" > /dev/null; then
    echo "✅ Backend responde correctamente"
    curl -s "http://$NETWORK_IP:5001/health" | jq . 2>/dev/null || curl -s "http://$NETWORK_IP:5001/health"
else
    echo "❌ Backend no responde"
fi

echo ""

# Probar endpoint de registro con CORS
echo "🔍 Probando endpoint de registro con CORS..."
REGISTER_RESPONSE=$(curl -s -X POST "http://$NETWORK_IP:5001/api/auth/register" \
  -H "Content-Type: application/json" \
  -H "Origin: http://$NETWORK_IP:3000" \
  -d '{"firstName":"Test","lastName":"User","email":"cors-test@test.com","password":"Test123"}')

if echo "$REGISTER_RESPONSE" | grep -q "success"; then
    echo "✅ Endpoint de registro funciona correctamente"
    echo "$REGISTER_RESPONSE" | jq . 2>/dev/null || echo "$REGISTER_RESPONSE"
else
    echo "❌ Error en endpoint de registro"
    echo "$REGISTER_RESPONSE"
fi

echo ""

# Probar frontend
echo "🔍 Probando frontend en http://$NETWORK_IP:3000"
if curl -s -f "http://$NETWORK_IP:3000" > /dev/null; then
    echo "✅ Frontend responde correctamente"
else
    echo "❌ Frontend no responde"
fi

echo ""
echo "📋 URLs de acceso:"
echo "   Frontend: http://$NETWORK_IP:3000"
echo "   Backend API: http://$NETWORK_IP:5001/api"
echo "   Health Check: http://$NETWORK_IP:5001/health"
echo ""
echo "🎉 Si todos los tests pasan, el problema de CORS está resuelto!"

