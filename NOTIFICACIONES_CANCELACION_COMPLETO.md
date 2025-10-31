# 📧 Sistema de Notificaciones de Cancelación de Vuelos

## ✅ Implementado en 3 Backends

### **backv4** (AEROLINEA2 - Puerto 8082)
### **backv5** (AEROLINEA3 - Puerto 8081)
### **backv6** (AEROLINEA4 - Puerto 8080)

---

## 🎯 Funcionalidades Implementadas

Cuando se cancela un vuelo en cualquiera de los 3 backends:

1. ✅ **Envía correos HTML** a los pasajeros con reservas
2. ✅ **Envía correos HTML** a las agencias de viajes registradas
3. ✅ **Oculta el vuelo cancelado** de todas las búsquedas automáticamente
4. ✅ **Registra el motivo** de cancelación en la base de datos
5. ✅ **Logs detallados** en la consola para seguimiento

---

## 📧 Configuración de Correos

### 1. **Configuración de Gmail (Ya está lista)**

Todos los backends usan el mismo archivo `mail.properties`:

```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true

mail.sender.email=rr36693904@gmail.com
mail.sender.password=oyyg qdla yqga bbho
```

✅ **Ya configurado en los 3 proyectos**

### 2. **Agregar Correos de Agencias**

Edita el archivo `RealEmailService.java` en cada proyecto:

**backv4**: `backv4/src/main/java/com/sources/app/services/RealEmailService.java` (línea ~238)  
**backv5**: `backv5/src/main/java/com/sources/app/services/RealEmailService.java` (línea ~238)  
**backv6**: `backv6/src/main/java/com/sources/app/services/RealEmailService.java` (línea ~238)

```java
private List<String> getAgencyEmails() {
    List<String> emails = new ArrayList<>();
    
    // AGREGAR LOS CORREOS DE TUS AGENCIAS AQUÍ
    emails.add("rr36693904@gmail.com");
    emails.add("agencia1@example.com");
    emails.add("agencia2@example.com");
    
    return emails;
}
```

---

## 🚀 Cómo Cancelar un Vuelo

### **backv4** (AEROLINEA2):

```bash
curl -X PUT http://localhost:8082/api/airline/flights/{ID_VUELO}/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Mal clima",
    "cancelledBy": 1
  }'
```

### **backv5** (AEROLINEA3):

```bash
curl -X PUT http://localhost:8081/api/airline/flights/{ID_VUELO}/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Mantenimiento",
    "cancelledBy": 1
  }'
```

### **backv6** (AEROLINEA4):

```bash
curl -X PUT http://localhost:8080/api/airline/flights/{ID_VUELO}/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Problema técnico",
    "cancelledBy": 1
  }'
```

---

## 📊 Logs que Deberías Ver

Cuando canceles un vuelo, verás:

```
=================================================
🚀 FlightDAO.cancelFlight() - INICIANDO CANCELACIÓN [AEROLINEAX]
=================================================
📋 Parámetros recibidos:
   - Flight ID: 123
   - Reason: Mal clima
   - Cancelled by: 1
=================================================

✅ El vuelo puede ser cancelado, procediendo...

🚀 === INICIANDO ENVÍO DE NOTIFICACIONES DE CANCELACIÓN ===
✅ Configuración de correo cargada desde classpath

📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A PASAJEROS ===
📧 Vuelo: AA-123
📧 Pasajeros a notificar: 2
📧 Preparando correo para: pasajero1@email.com
   Asunto: CANCELACIÓN DE VUELO - AA-123
📧 Enviando correo...
📧 ✅ Correo enviado exitosamente a: pasajero1@email.com
📧 Preparando correo para: pasajero2@email.com
   Asunto: CANCELACIÓN DE VUELO - AA-123
📧 Enviando correo...
📧 ✅ Correo enviado exitosamente a: pasajero2@email.com
✅ Correos enviados a pasajeros: 2/2
📧 ==========================================

📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A AGENCIAS ===
📧 Vuelo cancelado: AA-123
📧 Agencias a notificar: 1
📧 Preparando correo para: rr36693904@gmail.com
   Asunto: CANCELACIÓN DE VUELO - AA-123 (NOTIFICACIÓN PARA AGENCIAS)
📧 Enviando correo...
📧 ✅ Correo enviado exitosamente a: rr36693904@gmail.com
✅ Correos enviados a agencias: 1/1
📧 ==========================================

✅ Notificaciones enviadas exitosamente a pasajeros
✅ Notificaciones enviadas exitosamente a agencias
🚀 === FIN DE ENVÍO DE NOTIFICACIONES ===
```

---

## 📨 Formato de los Correos

### **Correo a Pasajeros** (HTML con estilos):

- **Header rojo** con título "CANCELACIÓN DE VUELO"
- **Detalles del vuelo** cancelado
- **Información de reembolso** destacada en verde
- **Próximos pasos** en caja azul
- **Información de contacto**
- **Footer** profesional

### **Correo a Agencias** (HTML con estilos):

- **Header rojo degradado** para agencias
- **Alerta destacada** del vuelo cancelado
- **Detalles completos** del vuelo
- **Acciones requeridas** en caja amarilla
- **Acciones automáticas** completadas en caja verde
- **Información de contacto** para soporte

---

## 🚫 Exclusión de Vuelos Cancelados

Los vuelos cancelados **NO aparecerán** en:

### Todos los Backends (backv4, backv5, backv6):

- ✅ Búsqueda de vuelos (`/airline/flights`)
- ✅ Búsqueda por origen y destino
- ✅ Búsqueda por fecha
- ✅ Listado completo de vuelos
- ✅ Resultados de la agencia de viajes

### Implementación Técnica:

Todos los métodos de búsqueda incluyen el filtro:
```java
WHERE (f.status IS NULL OR f.status != 'CANCELLED')
```

---

## 📋 Archivos Modificados/Creados

### En los 3 backends (backv4, backv5, backv6):

#### **Creados:**
1. ✅ `/src/main/java/com/sources/app/services/RealEmailService.java`
   - Servicio de email con JavaMail
   - Correos HTML profesionales
   - Notificaciones a pasajeros y agencias

#### **Modificados:**
1. ✅ `/src/main/java/com/sources/app/dao/FlightDAO.java`
   - Método `cancelFlight()` - Usa `RealEmailService`
   - Método `searchFlights()` - Excluye cancelados
   - Método `getAllFlights()` - Excluye cancelados
   - Logs mejorados

---

## 🧪 Pruebas

### Paso 1: Compilar el proyecto

```bash
# backv4
cd backv4 && mvn clean compile

# backv5
cd backv5 && mvn clean compile

# backv6
cd backv6 && mvn clean compile
```

### Paso 2: Iniciar el servidor

```bash
# backv4
cd backv4 && mvn exec:java

# backv5
cd backv5 && mvn exec:java

# backv6
cd backv6 && mvn exec:java
```

### Paso 3: Cancelar un vuelo

```bash
# Elige el backend que quieras probar
curl -X PUT http://localhost:8080/api/airline/flights/123/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Prueba de correos HTML",
    "cancelledBy": 1
  }'
```

### Paso 4: Verificar

1. ✅ **Revisa la consola** - Deberías ver logs detallados
2. ✅ **Revisa tu correo** - Deberías recibir el email HTML
3. ✅ **Busca el vuelo** - NO debería aparecer en los resultados
4. ✅ **Revisa la BD** - El vuelo debe tener `STATUS='CANCELLED'`

---

## 🎨 Vista Previa de los Correos

### Para Pasajeros:
- 🔴 Header rojo con "CANCELACIÓN DE VUELO"
- ℹ️ Caja de alerta con información destacada
- 📋 Detalles del vuelo en tabla formateada
- 💰 Reembolso destacado en verde
- 📋 Próximos pasos en caja azul

### Para Agencias:
- 🚨 Header rojo degradado "NOTIFICACIÓN PARA AGENCIAS"
- ⚠️ Alerta de vuelo cancelado
- ✈️ Detalles completos del vuelo
- ⚠️ Acciones requeridas (caja amarilla)
- ✓ Acciones automáticas completadas (caja verde)

---

## ⚙️ Configuración por Backend

| Backend | Aerolínea | Puerto | Base de Datos | Email Remitente |
|---------|-----------|--------|---------------|-----------------|
| backv4  | AEROLINEA2 | 8082   | AEROLINEA2    | rr36693904@gmail.com |
| backv5  | AEROLINEA3 | 8081   | AEROLINEA3    | rr36693904@gmail.com |
| backv6  | AEROLINEA4 | 8080   | AEROLINEA4    | rr36693904@gmail.com |

---

## 🔒 Seguridad

### ⚠️ IMPORTANTE: Protege tus credenciales

Los archivos `mail.properties` contienen información sensible.  
Asegúrate de que están en el `.gitignore`:

```gitignore
# Archivos de configuración sensibles
**/mail.properties
*.properties
```

### Variables de Entorno (Producción)

En producción, usa variables de entorno:

```bash
export MAIL_SENDER_EMAIL=your-email@gmail.com
export MAIL_SENDER_PASSWORD=your-app-password
```

---

## ❌ Solución de Problemas

### Problema: No llegan correos

1. **Revisa la carpeta de SPAM**
2. **Verifica la configuración de Gmail**:
   - Ve a https://myaccount.google.com/apppasswords
   - Crea una nueva "App Password"
   - Actualiza `mail.properties`
3. **Revisa los logs** para errores específicos

### Problema: No veo logs de cancelación

1. **Verifica que el vuelo exista** en la base de datos
2. **Verifica que el servidor esté corriendo**
3. **Revisa que la URL sea correcta**

### Problema: "Authentication failed"

- Tu contraseña de aplicación de Gmail está incorrecta
- Genera una nueva en: https://myaccount.google.com/apppasswords
- Actualiza `mail.properties`

---

## 📞 Comandos Rápidos

### Compilar todos los backends:

```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy

# Compilar backv4
cd backv4 && mvn clean compile && cd ..

# Compilar backv5
cd backv5 && mvn clean compile && cd ..

# Compilar backv6
cd backv6 && mvn clean compile && cd ..
```

### Iniciar todos los backends:

```bash
# Terminal 1 - backv4
cd backv4 && mvn exec:java

# Terminal 2 - backv5
cd backv5 && mvn exec:java

# Terminal 3 - backv6
cd backv6 && mvn exec:java
```

---

## 📖 Documentación Adicional

- **backv6**: Ver `backv6/CONFIGURACION_EMAIL_CANCELACIONES.md`
- **backv6**: Ver `backv6/NOTIFICACIONES_CANCELACION_VUELOS.md`

---

## ✨ Características de los Correos

### Correos HTML Profesionales:
- ✅ Diseño responsive
- ✅ Colores corporativos
- ✅ Información bien organizada
- ✅ Compatible con Gmail, Outlook, Apple Mail
- ✅ Estilos inline (máxima compatibilidad)

### Información Incluida:
- ✅ Detalles completos del vuelo
- ✅ Motivo de cancelación
- ✅ Información de reembolso
- ✅ Próximos pasos
- ✅ Contacto de servicio al cliente

---

## 🔍 Verificar Estado de un Vuelo

```sql
-- backv4 (AEROLINEA2)
SELECT ID_FLIGHT, FLIGHT_NUMBER, STATUS, CANCELLATION_REASON, CANCELLATION_DATE 
FROM AEROLINEA2.FLIGHTS 
WHERE ID_FLIGHT = 123;

-- backv5 (AEROLINEA3)
SELECT ID_FLIGHT, FLIGHT_NUMBER, STATUS, CANCELLATION_REASON, CANCELLATION_DATE 
FROM AEROLINEA3.FLIGHTS 
WHERE ID_FLIGHT = 123;

-- backv6 (AEROLINEA4)
SELECT ID_FLIGHT, FLIGHT_NUMBER, STATUS, CANCELLATION_REASON, CANCELLATION_DATE 
FROM AEROLINEA4.FLIGHTS 
WHERE ID_FLIGHT = 123;
```

---

## 🎯 Checklist de Verificación

Después de implementar, verifica:

- [ ] ✅ Los 3 backends compilan sin errores
- [ ] ✅ Los servidores inician correctamente
- [ ] ✅ Puedes cancelar un vuelo
- [ ] ✅ Ves logs detallados en la consola
- [ ] ✅ Recibes el correo HTML en tu bandeja
- [ ] ✅ El vuelo cancelado NO aparece en búsquedas
- [ ] ✅ Los correos tienen formato HTML profesional

---

## 📧 Correos de Prueba

Actualmente todos los backends envían correos a:
- **Pasajeros**: Sus correos registrados en los tickets
- **Agencias**: `rr36693904@gmail.com` (por defecto)

**IMPORTANTE**: Agrega los correos reales de tus agencias en `RealEmailService.getAgencyEmails()`

---

## 🚀 Próximos Pasos Opcionales

1. **Webhook** para notificar agencias vía API
2. **Dashboard** para ver historial de cancelaciones
3. **SMS** adicional a correos
4. **Cola de trabajos** (RabbitMQ) para envío asíncrono
5. **Templates** personalizables por aerolínea

---

**Última actualización**: 31/10/2024  
**Estado**: ✅ Implementado y probado en los 3 backends  
**Versión**: 1.0

