#!/bin/bash

echo "🚀 Iniciando Servidor del Panel de Usuario..."

# Compilar el proyecto
echo "📦 Compilando el proyecto..."
mvn clean compile

# Ejecutar el servidor
echo "🔧 Ejecutando el servidor..."
mvn exec:java -Dexec.mainClass="com.sources.app.UserProfileServer" -Dexec.args="8080"

echo "✅ Servidor iniciado en http://localhost:8080"
echo "📋 Rutas disponibles:"
echo "   - /api/user/test (prueba)"
echo "   - /api/user/profile (perfil)"
echo "   - /api/user/settings (configuraciones)"
echo "   - /api/user/security (seguridad)"
echo "   - /api/user/activity (actividad)"
echo "   - /api/user/sessions (sesiones)"
