# Guía de Testing y Documentación - Backend Ensurance Pharmacy (backv4)

## 📋 Índice
- [Tests Unitarios](#tests-unitarios)
- [Documentación JavaDoc](#documentación-javadoc)
- [Reportes](#reportes)
- [Estructura del Proyecto](#estructura-del-proyecto)

---

## 🧪 Tests Unitarios

### Prerrequisitos
- Java 23 o superior
- Maven 3.6 o superior
- Todas las dependencias instaladas

### Comandos para Ejecutar Tests

#### 1. Ejecutar TODOS los tests
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4
mvn test
```

#### 2. Ejecutar tests de un paquete específico
```bash
# Solo tests de handlers
mvn test -Dtest="com.sources.app.handlers.**"

# Solo tests de DAOs
mvn test -Dtest="com.sources.app.dao.**"

# Solo tests de entities
mvn test -Dtest="com.sources.app.entities.**"
```

#### 3. Ejecutar un test específico
```bash
# Ejecutar solo FlightHandlerTest
mvn test -Dtest="FlightHandlerTest"

# Ejecutar solo UserHandlerTest
mvn test -Dtest="UserHandlerTest"

# Ejecutar solo AirlineServerTest
mvn test -Dtest="AirlineServerTest"
```

#### 4. Ejecutar tests con reporte detallado
```bash
mvn test -Dtest="**/*Test" -DtrimStackTrace=false
```

#### 5. Ejecutar tests sin compilar (solo si ya compilaste antes)
```bash
mvn surefire:test
```

#### 6. Saltar tests (cuando solo quieres compilar)
```bash
mvn clean install -DskipTests
```

### Salida de Tests

Los resultados de los tests se guardan en:
- **Reportes XML**: `target/surefire-reports/*.xml`
- **Reportes de texto**: `target/surefire-reports/*.txt`

### Ejemplo de salida exitosa:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.sources.app.handlers.FlightHandlerTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.5 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 📚 Documentación JavaDoc

### Generar JavaDoc

#### 1. Generar JavaDoc para todo el proyecto
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4
mvn javadoc:javadoc
```

La documentación se generará en: `target/site/apidocs/index.html`

#### 2. Generar JavaDoc y empaquetarlo en un JAR
```bash
mvn javadoc:jar
```

Esto crea un archivo JAR con la documentación en: `target/backv4-1.0-SNAPSHOT-javadoc.jar`

#### 3. Generar JavaDoc solo para código de producción (sin tests)
```bash
mvn javadoc:javadoc
```

#### 4. Generar JavaDoc incluyendo tests
```bash
mvn javadoc:test-javadoc
```

La documentación de tests se genera en: `target/site/testapidocs/index.html`

#### 5. Generar JavaDoc con todas las opciones de reporting
```bash
mvn site
```

Esto genera un sitio completo con JavaDoc, reportes de tests y más en: `target/site/index.html`

### Ver la Documentación Generada

#### En macOS:
```bash
# Abrir JavaDoc principal
open target/site/apidocs/index.html

# Abrir JavaDoc de tests
open target/site/testapidocs/index.html

# Abrir sitio completo
open target/site/index.html
```

#### En Linux:
```bash
xdg-open target/site/apidocs/index.html
```

#### En Windows:
```bash
start target/site/apidocs/index.html
```

### Configuración de JavaDoc

El proyecto está configurado para:
- ✅ Mostrar todos los miembros (incluyendo privados)
- ✅ Encoding UTF-8
- ✅ Desactivar linting estricto (permite documentación flexible)
- ✅ Generar JAR de JavaDoc automáticamente

---

## 📊 Reportes

### Generar Reporte de Cobertura de Tests

#### 1. Generar reporte de tests con Surefire
```bash
mvn surefire-report:report
```

El reporte se genera en: `target/site/surefire-report.html`

#### 2. Ver el reporte
```bash
open target/site/surefire-report.html  # macOS
```

### Limpiar reportes anteriores
```bash
mvn clean
```

---

## 📁 Estructura del Proyecto

```
backv4/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sources/app/
│   │   │       ├── App.java                  ✅ Punto de entrada principal
│   │   │       ├── AirlineServer.java        ✅ Servidor HTTP
│   │   │       ├── dao/                      📦 Data Access Objects
│   │   │       │   ├── UserDAO.java          ✅ Gestión de usuarios
│   │   │       │   ├── FlightDAO.java        ✅ Gestión de vuelos
│   │   │       │   ├── PolicyDAO.java        ✅ Gestión de pólizas
│   │   │       │   └── ...
│   │   │       ├── entities/                 📦 Entidades JPA
│   │   │       │   ├── User.java
│   │   │       │   ├── Flight.java
│   │   │       │   └── ...
│   │   │       ├── handlers/                 📦 HTTP Handlers
│   │   │       │   ├── FlightHandler.java    ✅ Manejo de vuelos
│   │   │       │   ├── UserHandler.java      ✅ Manejo de usuarios
│   │   │       │   └── ...
│   │   │       ├── services/                 📦 Servicios
│   │   │       │   ├── EmailService.java
│   │   │       │   └── ...
│   │   │       └── util/                     📦 Utilidades
│   │   │           ├── HibernateUtil.java
│   │   │           └── ...
│   │   └── resources/
│   │       ├── hibernate.cfg.xml
│   │       └── ...
│   └── test/
│       └── java/
│           └── com/sources/app/
│               ├── AirlineServerTest.java    ✅ Tests del servidor
│               ├── dao/                      📦 Tests de DAOs
│               │   ├── UserDAOTest.java
│               │   ├── FlightDAOTest.java
│               │   └── ...
│               ├── entities/                 📦 Tests de entidades
│               │   ├── UserTest.java
│               │   └── ...
│               └── handlers/                 📦 Tests de handlers
│                   ├── FlightHandlerTest.java ✅ Tests de vuelos
│                   ├── UserHandlerTest.java
│                   └── ...
├── target/                                   📂 Archivos generados
│   ├── classes/                             📦 Clases compiladas
│   ├── test-classes/                        📦 Tests compilados
│   ├── site/                                📂 Documentación y reportes
│   │   ├── apidocs/                         📚 JavaDoc principal
│   │   ├── testapidocs/                     📚 JavaDoc de tests
│   │   └── surefire-report.html            📊 Reporte de tests
│   └── surefire-reports/                    📊 Resultados de tests XML
├── pom.xml                                   ⚙️ Configuración Maven
└── README_TESTING_AND_DOCS.md               📖 Este archivo
```

---

## 🔧 Comandos Comunes Combinados

### Limpiar, compilar y ejecutar tests
```bash
mvn clean test
```

### Compilar, ejecutar tests y generar JavaDoc
```bash
mvn clean test javadoc:javadoc
```

### Generar todo (tests, JavaDoc, reportes, sitio completo)
```bash
mvn clean test site
```

### Empaquetar con tests y JavaDoc
```bash
mvn clean package javadoc:jar
```

### Solo compilar sin tests ni JavaDoc (más rápido)
```bash
mvn clean compile -DskipTests
```

---

## 📝 Notas Importantes

### Tests
- ✅ Los tests utilizan **JUnit 5** (Jupiter)
- ✅ Se utiliza **Mockito** para mocking
- ✅ Los tests están organizados por paquetes (dao, entities, handlers)
- ✅ Cada clase de producción tiene su correspondiente clase de test

### JavaDoc
- ✅ Todas las clases principales tienen documentación completa
- ✅ Los métodos públicos y privados están documentados
- ✅ Se incluyen ejemplos de uso cuando es apropiado
- ✅ La documentación está en español
- ✅ Se utilizan tags estándar: `@param`, `@return`, `@throws`, `@author`, etc.

### Dependencias de Testing
```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>

<!-- EqualsVerifier -->
<dependency>
    <groupId>nl.jqno.equalsverifier</groupId>
    <artifactId>equalsverifier</artifactId>
    <version>3.16.1</version>
    <scope>test</scope>
</dependency>
```

---

## 🚀 Quick Start

### Para ejecutar tests rápidamente:
```bash
cd backv4
mvn test
```

### Para generar documentación rápidamente:
```bash
cd backv4
mvn javadoc:javadoc
open target/site/apidocs/index.html
```

### Para ver todo (tests + docs):
```bash
cd backv4
mvn clean test site
open target/site/index.html
```

---

## 🆘 Solución de Problemas

### "mvn: command not found"
```bash
# Instalar Maven con Homebrew (macOS)
brew install maven

# Verificar instalación
mvn --version
```

### "JAVA_HOME not set"
```bash
# En macOS/Linux, agregar a ~/.zshrc o ~/.bash_profile
export JAVA_HOME=$(/usr/libexec/java_home -v 23)
export PATH=$JAVA_HOME/bin:$PATH

# Recargar configuración
source ~/.zshrc
```

### Tests fallan por conexión a base de datos
- Verificar que Oracle DB esté corriendo
- Revisar `hibernate.cfg.xml` para credenciales correctas
- Algunos tests pueden requerir datos de prueba en la BD

### JavaDoc falla por errores de sintaxis
- La configuración actual usa `-Xdoclint:none` para ser permisiva
- Si quieres ver warnings, elimina esa opción del `pom.xml`

---

## 📧 Contacto y Soporte

Para preguntas o problemas:
- Revisar la documentación JavaDoc generada
- Consultar los tests existentes como ejemplos
- Verificar logs en `server.log`

---

## 📜 Licencia

Este proyecto es parte de Ensurance Pharmacy - Sistema de Gestión Integral

---

**Última actualización**: Octubre 2024
**Versión**: 1.0
**Equipo**: Desarrollo Backend Ensurance Pharmacy

