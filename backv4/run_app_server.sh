#!/bin/bash

set -e

echo "🚀 Iniciando App Server (HttpServer con contextos /api y /api/airline)"

PORT=${1:-8080}

echo "🔨 Compilando..."
mvn -q -e -DskipTests clean compile

echo "📡 Ejecutando en puerto ${PORT} (IP autodetectada)"
printf "%s\n" "$PORT" | mvn -q exec:java -Dexec.mainClass="com.sources.app.App"



