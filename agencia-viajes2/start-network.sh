#!/bin/bash

# Script para iniciar la aplicación con configuración de red
# Obtener la IP de la red local
NETWORK_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -1)

echo "🌐 Configurando aplicación para IP de red: $NETWORK_IP"

# Configurar variables de entorno para el frontend
echo "REACT_APP_API_URL=http://$NETWORK_IP:5001/api" > frontend/.env
echo "REACT_APP_FRONTEND_URL=http://$NETWORK_IP:3000" >> frontend/.env
echo "GENERATE_SOURCEMAP=false" >> frontend/.env

# Configurar variables de entorno para el backend
echo "PORT=5001" > backend/.env
echo "NODE_ENV=development" >> backend/.env
echo "MONGODB_URI=mongodb://localhost:27017/agencia-viajes" >> backend/.env
echo "JWT_SECRET=tu_jwt_secret_muy_seguro_aqui_para_agencia_viajes_2024" >> backend/.env
echo "JWT_EXPIRE=7d" >> backend/.env
echo "FRONTEND_URL=http://$NETWORK_IP:3000" >> backend/.env
echo "EMAIL_SERVICE=gmail" >> backend/.env
echo "EMAIL_USER=tu_email@gmail.com" >> backend/.env
echo "EMAIL_PASS=tu_password_de_aplicacion" >> backend/.env

echo "✅ Archivos .env configurados correctamente"
echo "🚀 Iniciando backend en puerto 5001..."
echo "🌐 Frontend estará disponible en: http://$NETWORK_IP:3000"
echo "🔗 API estará disponible en: http://$NETWORK_IP:5001/api"

# Iniciar backend en segundo plano
cd backend && npm start &
BACKEND_PID=$!

# Esperar un momento para que el backend se inicie
sleep 3

# Iniciar frontend
cd ../frontend && npm start &
FRONTEND_PID=$!

echo "📊 Procesos iniciados:"
echo "   Backend PID: $BACKEND_PID"
echo "   Frontend PID: $FRONTEND_PID"

# Función para limpiar procesos al salir
cleanup() {
    echo "🛑 Deteniendo procesos..."
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    exit 0
}

# Capturar señales de terminación
trap cleanup SIGINT SIGTERM

# Esperar a que termine cualquiera de los procesos
wait
