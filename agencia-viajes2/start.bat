@echo off
REM Script para iniciar el sistema de agencia de viajes en Windows
REM Inicia tanto el backend (Node.js) como el frontend (React) simultáneamente

echo 🛩️  Iniciando Sistema de Agencia de Viajes
echo ==========================================
echo.

REM Verificar si Node.js está instalado
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js no está instalado. Por favor instala Node.js primero.
    pause
    exit /b 1
)

REM Verificar si npm está instalado
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ npm no está instalado. Por favor instala npm primero.
    pause
    exit /b 1
)

echo ✅ Node.js y npm están instalados
echo.

REM Verificar si las dependencias están instaladas
if not exist "backend\node_modules" (
    echo 📦 Instalando dependencias del backend...
    cd backend
    npm install
    cd ..
)

if not exist "frontend\node_modules" (
    echo 📦 Instalando dependencias del frontend...
    cd frontend
    npm install
    cd ..
)

echo ✅ Dependencias instaladas
echo.

REM Verificar si el archivo .env existe en el backend
if not exist "backend\.env" (
    echo ⚠️  Archivo .env no encontrado en el backend
    echo 📝 Creando archivo .env con configuración por defecto...
    (
        echo PORT=5000
        echo NODE_ENV=development
        echo MONGODB_URI=mongodb+srv://serafinescuscurro00_db_user:pmEvUS47NWr2aCuL@agencias.gultamq.mongodb.net/?retryWrites=true^&w=majority^&appName=agencias
        echo JWT_SECRET=agencia_viajes_jwt_secret_2024_muy_seguro
        echo JWT_EXPIRE=7d
        echo FRONTEND_URL=http://localhost:3000
    ) > backend\.env
    echo ✅ Archivo .env creado
)

echo 🚀 Iniciando servicios...
echo.
echo Backend: http://localhost:5000
echo Frontend: http://localhost:3000
echo.
echo Presiona Ctrl+C para detener ambos servicios
echo.

REM Iniciar ambos servicios
npm start
