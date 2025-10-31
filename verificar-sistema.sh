#!/bin/bash

echo "🔍 Verificando Estado del Sistema de Notificaciones"
echo "======================================================"
echo ""

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar Django backend
echo "📡 Verificando Backend Django (puerto 5001)..."
if curl -s http://localhost:5001/api/debug/db-info > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Backend Django está corriendo${NC}"
    
    # Verificar endpoint de notificaciones
    RESPONSE=$(curl -s http://localhost:5001/api/flight-cancellations)
    if echo "$RESPONSE" | grep -q "success"; then
        echo -e "${GREEN}✅ Endpoint de notificaciones funciona${NC}"
        
        # Contar notificaciones
        COUNT=$(echo "$RESPONSE" | grep -o '"total":[0-9]*' | grep -o '[0-9]*')
        UNREAD=$(echo "$RESPONSE" | grep -o '"unreadCount":[0-9]*' | grep -o '[0-9]*')
        echo "   📊 Notificaciones totales: $COUNT"
        echo "   📊 No leídas: $UNREAD"
    else
        echo -e "${RED}❌ Endpoint de notificaciones NO funciona${NC}"
    fi
else
    echo -e "${RED}❌ Backend Django NO está corriendo${NC}"
    echo -e "${YELLOW}   Inicia con: cd agencia-viajes/backend-django && ./scripts/start-django.sh${NC}"
    echo ""
fi

# Verificar Frontend
echo ""
echo "🌐 Verificando Frontend React (puerto 5173)..."
if curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend está corriendo${NC}"
else
    echo -e "${RED}❌ Frontend NO está corriendo${NC}"
    echo -e "${YELLOW}   Inicia con: cd agencia-viajes/agencia && npm run dev${NC}"
fi

# Verificar aerolíneas
echo ""
echo "✈️  Verificando Aerolíneas..."

# backv4
echo ""
echo "   backv4 (AEROLINEA2 - Puerto 8082):"
if curl -s http://localhost:8082/api/airline/health > /dev/null 2>&1; then
    echo -e "   ${GREEN}✅ Corriendo${NC}"
else
    echo -e "   ${RED}❌ NO corriendo${NC}"
fi

# backv5
echo "   backv5 (AEROLINEA3 - Puerto 8081):"
if curl -s http://localhost:8081/api/airline/health > /dev/null 2>&1; then
    echo -e "   ${GREEN}✅ Corriendo${NC}"
else
    echo -e "   ${RED}❌ NO corriendo${NC}"
fi

# backv6
echo "   backv6 (AEROLINEA4 - Puerto 8080):"
if curl -s http://localhost:8080/api/airline/health > /dev/null 2>&1; then
    echo -e "   ${GREEN}✅ Corriendo${NC}"
else
    echo -e "   ${RED}❌ NO corriendo${NC}"
fi

echo ""
echo "======================================================"
echo ""
echo "📝 Resumen:"
echo ""
echo "Para que el sistema funcione NECESITAS:"
echo "1. ✅ Backend Django corriendo (puerto 5001) - OBLIGATORIO"
echo "2. ✅ Frontend React corriendo (puerto 5173) - OBLIGATORIO"
echo "3. ✅ Al menos 1 aerolínea corriendo - OBLIGATORIO"
echo ""
echo "Puertos obligatorios: 5001 (Django) y 5173 (Frontend)"
echo ""

