#!/bin/bash

# Script para probar la conectividad entre frontend y backend
NETWORK_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -1)

echo "🧪 Probando conectividad de red..."
echo "🌐 IP de red detectada: $NETWORK_IP"
echo ""

# Probar conectividad del backend
echo "🔍 Probando backend en http://$NETWORK_IP:5001/health"
if curl -s -f "http://$NETWORK_IP:5001/health" > /dev/null; then
    echo "✅ Backend responde correctamente"
    curl -s "http://$NETWORK_IP:5001/health" | jq . 2>/dev/null || curl -s "http://$NETWORK_IP:5001/health"
else
    echo "❌ Backend no responde en $NETWORK_IP:5001"
    echo "💡 Asegúrate de que el backend esté ejecutándose"
fi

echo ""

# Probar conectividad del frontend
echo "🔍 Probando frontend en http://$NETWORK_IP:3000"
if curl -s -f "http://$NETWORK_IP:3000" > /dev/null; then
    echo "✅ Frontend responde correctamente"
else
    echo "❌ Frontend no responde en $NETWORK_IP:3000"
    echo "💡 Asegúrate de que el frontend esté ejecutándose"
fi

echo ""
echo "📋 URLs de acceso:"
echo "   Frontend: http://$NETWORK_IP:3000"
echo "   Backend API: http://$NETWORK_IP:5001/api"
echo "   Health Check: http://$NETWORK_IP:5001/health"
