# 🔄 Cómo Reiniciar los Backends para Aplicar los Cambios

## ⚠️ IMPORTANTE: Debes Reiniciar los Servidores

Los cambios que hicimos en el código **NO se aplican automáticamente**.  
Necesitas **detener y reiniciar** cada servidor.

---

## 🛑 Paso 1: Detener los Servidores

### Opción A: Si están corriendo en terminales

Ve a cada terminal donde están corriendo y presiona:

```
Ctrl + C
```

### Opción B: Si los iniciaste desde tu IDE

- En **IntelliJ IDEA**: Haz clic en el botón rojo ⏹️ (Stop)
- En **Eclipse**: Haz clic en el botón rojo ⏹️ (Terminate)
- En **NetBeans**: Haz clic en el botón rojo ⏹️ (Stop Build/Run)

### Opción C: Forzar cierre por puerto

```bash
# backv4 (puerto 8082)
lsof -ti:8082 | xargs kill -9

# backv5 (puerto 8081)
lsof -ti:8081 | xargs kill -9

# backv6 (puerto 8080)
lsof -ti:8080 | xargs kill -9
```

---

## ✅ Paso 2: Recompilar y Reiniciar

### **backv4** (AEROLINEA2 - Puerto 8082)

```bash
# Terminal 1
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4

# Limpiar y compilar
mvn clean compile

# Iniciar servidor
mvn exec:java
```

Deberías ver:
```
[INFO] --- exec:3.1.0:java (default-cli) @ backv4 ---
🚀 AEROLINEA2 Server starting on port 8082...
✅ Servidor iniciado exitosamente
```

### **backv5** (AEROLINEA3 - Puerto 8081)

```bash
# Terminal 2
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv5

# Limpiar y compilar
mvn clean compile

# Iniciar servidor
mvn exec:java
```

Deberías ver:
```
[INFO] --- exec:3.1.0:java (default-cli) @ backv4 ---
🚀 AEROLINEA3 Server starting on port 8081...
✅ Servidor iniciado exitosamente
```

### **backv6** (AEROLINEA4 - Puerto 8080)

```bash
# Terminal 3
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv6

# Limpiar y compilar
mvn clean compile

# Iniciar servidor
mvn exec:java
```

Deberías ver:
```
[INFO] --- exec:3.1.0:java (default-cli) @ backv4 ---
🚀 AEROLINEA4 Server starting on port 8080...
✅ Servidor iniciado exitosamente
```

---

## 🧪 Paso 3: Probar que Funciona

### Cancela un vuelo en cada backend:

```bash
# backv4 (puerto 8082)
curl -X PUT http://localhost:8082/api/airline/flights/ID_VUELO/cancel \
  -H "Content-Type: application/json" \
  -d '{"cancellationReason":"Prueba correos HTML backv4","cancelledBy":1}'

# backv5 (puerto 8081)
curl -X PUT http://localhost:8081/api/airline/flights/ID_VUELO/cancel \
  -H "Content-Type: application/json" \
  -d '{"cancellationReason":"Prueba correos HTML backv5","cancelledBy":1}'

# backv6 (puerto 8080)
curl -X PUT http://localhost:8080/api/airline/flights/ID_VUELO/cancel \
  -H "Content-Type: application/json" \
  -d '{"cancellationReason":"Prueba correos HTML backv6","cancelledBy":1}'
```

Reemplaza `ID_VUELO` con un ID real de tu base de datos.

---

## 📊 Logs que DEBES Ver Ahora

Si todo está bien, verás:

```
=================================================
🚀 FlightDAO.cancelFlight() - INICIANDO CANCELACIÓN [AEROLINEAX]
=================================================
📋 Parámetros recibidos:
   - Flight ID: 123
   - Reason: Prueba correos HTML
   - Cancelled by: 1
=================================================

✅ El vuelo puede ser cancelado, procediendo...

🚀 === INICIANDO ENVÍO DE NOTIFICACIONES DE CANCELACIÓN ===
✅ Configuración de correo cargada desde classpath

📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A PASAJEROS ===
📧 Vuelo: AA-123
📧 Pasajeros a notificar: 2
📧 Preparando correo para: pasajero@email.com
   Asunto: CANCELACIÓN DE VUELO - AA-123
📧 Enviando correo...
📧 ✅ Correo enviado exitosamente a: pasajero@email.com

📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A AGENCIAS ===
📧 Vuelo cancelado: AA-123
📧 Agencias a notificar: 1
📧 Preparando correo para: rr36693904@gmail.com
   Asunto: CANCELACIÓN DE VUELO - AA-123 (NOTIFICACIÓN PARA AGENCIAS)
📧 Enviando correo...
📧 ✅ Correo enviado exitosamente a: rr36693904@gmail.com
✅ Correos enviados a agencias: 1/1

🚀 === FIN DE ENVÍO DE NOTIFICACIONES ===
```

---

## ❌ Si NO Ves Estos Logs Nuevos

**Significa que el servidor sigue usando el código antiguo.**

### Solución:

1. **Detén el servidor** completamente (Ctrl+C o botón Stop)
2. **Espera 3-5 segundos**
3. **Recompila**: `mvn clean compile`
4. **Reinicia**: `mvn exec:java`

---

## 🔍 Verificar que Está Usando el Nuevo Código

En los logs **nuevos** deberías ver:

✅ `=================================================` (líneas de separación)  
✅ `🚀 === INICIANDO ENVÍO DE NOTIFICACIONES ===`  
✅ `📧 Preparando correo para: ...`  
✅ `📧 Enviando correo...`  
✅ `✅ Configuración de correo cargada desde classpath`  

Si ves estos logs, el código nuevo **SÍ está funcionando**.

---

## 📧 Revisar tu Correo

- **Espera 1-2 minutos** (Gmail puede tardar)
- **Revisa la carpeta de SPAM**
- **Busca por asunto**: "CANCELACIÓN DE VUELO"

---

## 🚀 Script Rápido para Reiniciar Todo

```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy

# Detener todo (forzar)
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9
lsof -ti:8082 | xargs kill -9

# Esperar 3 segundos
sleep 3

# Recompilar todo
cd backv4 && mvn clean compile && cd ..
cd backv5 && mvn clean compile && cd ..
cd backv6 && mvn clean compile && cd ..

echo "✅ Todo recompilado. Ahora inicia los servidores en terminales separadas:"
echo "   Terminal 1: cd backv4 && mvn exec:java"
echo "   Terminal 2: cd backv5 && mvn exec:java"
echo "   Terminal 3: cd backv6 && mvn exec:java"
```

---

¿Ya reiniciaste los servidores? Si ya lo hiciste, **cópiame los logs completos** que ves cuando cancelas un vuelo en backv4 o backv5, y te ayudo a ver qué está pasando. 😊

