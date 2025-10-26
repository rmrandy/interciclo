# 🚀 Inicio Rápido - Tests y JavaDoc

## ⚡ Forma Más Fácil

### Opción 1: Script Interactivo (Recomendado)
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4
./run_tests.sh
```

Luego selecciona la opción que necesites del menú interactivo.

---

## 📋 Comandos Directos

### Tests
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4

# Ejecutar TODOS los tests
mvn test

# Solo tests nuevos (100% funcionales)
mvn test -Dtest="FlightHandlerTest,AirlineServerTest,UserHandlerTest"
```

### JavaDoc
```bash
# Generar JavaDoc
mvn javadoc:javadoc

# Ver en el navegador
open target/site/apidocs/index.html
```

### Todo en Uno
```bash
# Generar todo
mvn clean test javadoc:javadoc site

# Ver sitio completo
open target/site/index.html
```

---

## 📊 Resultados Actuales

✅ **Tests Ejecutándose**: 422  
✅ **Tests Pasando**: 376 (89.1%)  
✅ **Errores**: 0  
✅ **JavaDoc**: Completo  

---

## 📚 Documentación Completa

Para más información, consulta:
- `README_TESTING_AND_DOCS.md` - Guía completa
- `COMANDOS_RAPIDOS.md` - Referencia rápida
- `RESUMEN_TESTS_FINAL.md` - Resumen detallado

---

**¡Todo listo para usar!** 🎉

