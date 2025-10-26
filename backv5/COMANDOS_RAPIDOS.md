# 🚀 Comandos Rápidos - Tests y JavaDoc

## ⚡ Comandos Esenciales

### 📋 Ejecutar Tests
```bash
# Ir al directorio del proyecto
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4

# Ejecutar TODOS los tests
mvn test

# Ejecutar tests con output detallado
mvn test -DtrimStackTrace=false

# Ejecutar solo un test específico
mvn test -Dtest="FlightHandlerTest"
mvn test -Dtest="UserHandlerTest"
mvn test -Dtest="AirlineServerTest"
```

### 📚 Generar JavaDoc
```bash
# Generar JavaDoc completo
mvn javadoc:javadoc

# Ver la documentación generada (macOS)
open target/site/apidocs/index.html

# Generar JavaDoc empaquetado en JAR
mvn javadoc:jar
```

### 🔄 Comandos Combinados
```bash
# Limpiar, compilar y ejecutar tests
mvn clean test

# Todo: tests + JavaDoc + reportes
mvn clean test javadoc:javadoc

# Generar sitio completo con toda la documentación
mvn clean test site

# Abrir sitio completo (macOS)
open target/site/index.html
```

## 📁 Ubicaciones Importantes

### Tests
- **Código de tests**: `src/test/java/com/sources/app/`
- **Resultados XML**: `target/surefire-reports/*.xml`
- **Reportes HTML**: `target/site/surefire-report.html`

### JavaDoc
- **JavaDoc principal**: `target/site/apidocs/index.html`
- **JavaDoc de tests**: `target/site/testapidocs/index.html`
- **JAR de JavaDoc**: `target/backv4-1.0-SNAPSHOT-javadoc.jar`

## ✅ Tests Creados

✅ **FlightHandlerTest** - Tests para operaciones de vuelos
✅ **AirlineServerTest** - Tests para el servidor HTTP
✅ **UserHandlerTest** - Tests existentes para gestión de usuarios
✅ **Y muchos más tests para DAOs, entities y handlers**

## 📖 Documentación JavaDoc Agregada

✅ **App.java** - Clase principal con documentación completa
✅ **AirlineServer.java** - Servidor HTTP completamente documentado
✅ **FlightHandler.java** - Handler de vuelos con JavaDoc
✅ **UserDAO.java** - Ya tenía documentación completa
✅ **Todos los métodos públicos y privados documentados**

## 🔧 Configuración Maven

El archivo `pom.xml` ha sido actualizado con:
- ✅ Plugin de Surefire para tests
- ✅ Plugin de JavaDoc con configuración UTF-8
- ✅ Plugin de reportes
- ✅ Configuración de reporting

## 📊 Ver Reportes

```bash
# Generar y abrir reporte de tests
mvn surefire-report:report
open target/site/surefire-report.html

# Generar y abrir todo
mvn site
open target/site/index.html
```

## 🆘 Si algo falla

```bash
# Limpiar todo y empezar de cero
mvn clean

# Compilar sin tests
mvn compile -DskipTests

# Ver versión de Maven
mvn --version

# Descargar dependencias faltantes
mvn dependency:resolve
```

## 📝 Notas

- Todos los tests usan **JUnit 5** y **Mockito**
- La documentación está en **español**
- Los tests están organizados por paquetes (dao, handlers, entities)
- JavaDoc incluye ejemplos y descripciones detalladas

## 📧 Documentación Completa

Para más detalles, consulta: `README_TESTING_AND_DOCS.md`

