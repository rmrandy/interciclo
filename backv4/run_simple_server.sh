#!/bin/bash

echo "🚀 Iniciando Servidor Simple AeroLinea..."
echo "📡 Este servidor no requiere base de datos"
echo "🔧 CORS habilitado para desarrollo"

# Compilar
echo "🔨 Compilando..."
mvn clean compile

# Ejecutar servidor simple
echo "📡 Ejecutando servidor en puerto 8080..."
mvn exec:java -Dexec.mainClass="com.sources.app.SimpleAirlineServer" -Dexec.args="8080 0.0.0.0"
