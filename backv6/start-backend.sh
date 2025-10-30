#!/bin/bash

# Script para iniciar el backend en un puerto específico
# Uso: ./start-backend.sh [puerto]
# Si no se especifica puerto, se usa 8080 por defecto

# Obtener el puerto del primer argumento o usar 8080 por defecto
PORT=${1:-8080}

echo "=================================================="
echo "🚀 Iniciando Backend Ensurance Pharmacy"
echo "=================================================="
echo "Puerto configurado: $PORT"
echo "=================================================="
echo ""

# Ejecutar Maven con el puerto especificado
mvn exec:java -Dexec.mainClass="com.sources.app.App" -Dport=$PORT

# Nota: El comando anterior bloquea la terminal.
# Si deseas ejecutar en segundo plano, añade & al final


