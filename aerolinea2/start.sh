#!/bin/bash
# Script simple para iniciar el frontend en puerto 5050
# Uso: ./start.sh

echo "🚀 Iniciando Frontend Aerolínea en puerto 5050..."
echo ""

# Obtener IP
python3 getip.py

# Iniciar en puerto 5050
PORT=5050 npm run dev

