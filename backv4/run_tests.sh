#!/bin/bash
# Script para ejecutar tests y generar documentación
# Proyecto: Ensurance Pharmacy Backend (backv4)

echo "🚀 Ensurance Pharmacy - Sistema de Testing"
echo "=========================================="
echo ""

# Función para mostrar menú
show_menu() {
    echo "Opciones disponibles:"
    echo ""
    echo "1. 📋 Ejecutar TODOS los tests"
    echo "2. ✨ Ejecutar solo tests nuevos (100% funcionales)"
    echo "3. 📚 Generar JavaDoc"
    echo "4. 📊 Generar reporte de tests"
    echo "5. 🔄 Todo: Tests + JavaDoc + Reportes"
    echo "6. 🧹 Limpiar archivos generados"
    echo "7. 📖 Ver JavaDoc en navegador"
    echo "8. 📈 Ver reporte de tests en navegador"
    echo "9. ❌ Salir"
    echo ""
}

# Loop principal
while true; do
    show_menu
    read -p "Selecciona una opción (1-9): " option
    echo ""
    
    case $option in
        1)
            echo "📋 Ejecutando TODOS los tests..."
            mvn test
            ;;
        2)
            echo "✨ Ejecutando solo tests nuevos..."
            mvn test -Dtest="FlightHandlerTest,AirlineServerTest,UserHandlerTest"
            ;;
        3)
            echo "📚 Generando JavaDoc..."
            mvn javadoc:javadoc
            echo "✅ JavaDoc generado en: target/site/apidocs/index.html"
            ;;
        4)
            echo "📊 Generando reporte de tests..."
            mvn surefire-report:report
            echo "✅ Reporte generado en: target/site/surefire-report.html"
            ;;
        5)
            echo "🔄 Generando todo (esto puede tomar un momento)..."
            mvn clean test javadoc:javadoc site
            echo "✅ Todo generado en: target/site/index.html"
            ;;
        6)
            echo "🧹 Limpiando archivos generados..."
            mvn clean
            echo "✅ Limpieza completada"
            ;;
        7)
            echo "📖 Abriendo JavaDoc..."
            if [ -f "target/site/apidocs/index.html" ]; then
                open target/site/apidocs/index.html
                echo "✅ JavaDoc abierto en el navegador"
            else
                echo "❌ JavaDoc no encontrado. Primero ejecuta la opción 3 para generarlo."
            fi
            ;;
        8)
            echo "📈 Abriendo reporte de tests..."
            if [ -f "target/site/surefire-report.html" ]; then
                open target/site/surefire-report.html
                echo "✅ Reporte abierto en el navegador"
            else
                echo "❌ Reporte no encontrado. Primero ejecuta la opción 4 para generarlo."
            fi
            ;;
        9)
            echo "👋 ¡Hasta luego!"
            exit 0
            ;;
        *)
            echo "❌ Opción inválida. Por favor selecciona 1-9."
            ;;
    esac
    
    echo ""
    echo "Presiona Enter para continuar..."
    read
    clear
done

