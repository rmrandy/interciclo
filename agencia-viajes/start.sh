#!/bin/bash

# Script para iniciar el sistema de agencia de viajes
# Inicia tanto el backend (Node.js) como el frontend (React) simultáneamente

echo "🛩️  Iniciando Sistema de Agencia de Viajes"
echo "=========================================="
echo ""

# Verificar si Node.js está instalado
if ! command -v node &> /dev/null; then
    echo "❌ Node.js no está instalado. Por favor instala Node.js primero."
    exit 1
fi

# Verificar si npm está instalado
if ! command -v npm &> /dev/null; then
    echo "❌ npm no está instalado. Por favor instala npm primero."
    exit 1
fi

echo "✅ Node.js y npm están instalados"
echo ""

# Verificar si las dependencias están instaladas
if [ ! -d "backend/node_modules" ]; then
    echo "📦 Instalando dependencias del backend..."
    cd backend
    npm install
    cd ..
fi

if [ ! -d "frontend/node_modules" ]; then
    echo "📦 Instalando dependencias del frontend..."
    cd frontend
    npm install
    cd ..
fi

echo "✅ Dependencias instaladas"
echo ""

# Verificar si el archivo .env existe en el backend
if [ ! -f "backend/.env" ]; then
    echo "⚠️  Archivo .env no encontrado en el backend"
    echo "📝 Creando archivo .env con configuración por defecto..."
    cat > backend/.env << EOF
PORT=5000
NODE_ENV=development
MONGODB_URI=mongodb+srv://serafinescuscurro00_db_user:pmEvUS47NWr2aCuL@agencias.gultamq.mongodb.net/?retryWrites=true&w=majority&appName=agencias
JWT_SECRET=agencia_viajes_jwt_secret_2024_muy_seguro
JWT_EXPIRE=7d
FRONTEND_URL=http://localhost:3000
EOF
    echo "✅ Archivo .env creado"
fi

echo "🚀 Iniciando servicios..."
echo ""
echo "Backend: http://localhost:5000"
echo "Frontend: http://localhost:3000"
echo ""
echo "Presiona Ctrl+C para detener ambos servicios"
echo ""

# Iniciar ambos servicios
npm start
