#!/bin/bash

# Script para ejecutar el esquema de páginas informativas
# Sistema de gestión de contenido para páginas informativas de AeroLinea

echo "🛩️ Ejecutando esquema de páginas informativas para AeroLinea..."

# Configuración de la base de datos
DB_HOST="64.225.58.196"
DB_PORT="1521"
DB_SERVICE="XEPDB1"
DB_USER="AEROLINEA"
DB_PASSWORD="123"

# Archivo SQL
SQL_FILE="informative_pages_schema.sql"

# Verificar que el archivo SQL existe
if [ ! -f "$SQL_FILE" ]; then
    echo "❌ Error: No se encontró el archivo $SQL_FILE"
    exit 1
fi

echo "📋 Ejecutando script SQL: $SQL_FILE"

# Ejecutar el script SQL usando sqlplus
sqlplus -s "$DB_USER/$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_SERVICE" << EOF
@$SQL_FILE
EXIT;
EOF

if [ $? -eq 0 ]; then
    echo "✅ Esquema de páginas informativas creado exitosamente!"
    echo ""
    echo "📊 Tablas creadas:"
    echo "   - INFORMATIVE_PAGES (páginas principales)"
    echo "   - PAGE_SECTIONS (secciones de contenido)"
    echo "   - PAGE_MEDIA (recursos multimedia)"
    echo "   - SITE_CONFIGURATIONS (configuraciones globales)"
    echo "   - NAVIGATION_MENUS (menús de navegación)"
    echo ""
    echo "🌐 Páginas informativas disponibles:"
    echo "   - Tipos de Asientos"
    echo "   - Instrucciones de Abordaje"
    echo "   - Proceso de Check-in"
    echo "   - Información de Equipaje"
    echo "   - Consejos de Viaje"
    echo "   - Contacto"
    echo ""
    echo "🎯 Próximos pasos:"
    echo "   1. Verificar que las páginas se cargan correctamente en el frontend"
    echo "   2. Configurar el sistema de administración para editar contenido"
    echo "   3. Probar la funcionalidad de navegación"
else
    echo "❌ Error al ejecutar el esquema de páginas informativas"
    exit 1
fi

