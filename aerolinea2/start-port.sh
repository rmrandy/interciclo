#!/bin/bash
# Script para iniciar el frontend de aerolínea en un puerto específico
# Uso: ./start-port.sh [puerto]
# Ejemplo: ./start-port.sh 5050

# Colores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sin color

echo -e "${BLUE}🚀 Ensurance Pharmacy - Frontend Aerolínea${NC}"
echo "==========================================="
echo ""

# Puerto por defecto
DEFAULT_PORT=5050

# Si se proporciona un puerto como argumento, usarlo
if [ -n "$1" ]; then
    PORT=$1
    echo -e "${GREEN}✅ Usando puerto personalizado: ${PORT}${NC}"
else
    PORT=$DEFAULT_PORT
    echo -e "${GREEN}✅ Usando puerto por defecto: ${PORT}${NC}"
fi

echo ""
echo -e "${YELLOW}📡 Obteniendo dirección IP...${NC}"
python3 getip.py

echo ""
echo -e "${BLUE}🌐 Iniciando servidor de desarrollo en puerto ${PORT}...${NC}"
echo -e "${YELLOW}Presiona Ctrl+C para detener el servidor${NC}"
echo ""

# Iniciar Vite con el puerto especificado
PORT=$PORT npm run dev

echo ""
echo -e "${GREEN}✅ Servidor detenido${NC}"

