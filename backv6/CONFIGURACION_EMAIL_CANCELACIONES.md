# 📧 Configuración de Email para Notificaciones de Cancelación

## ✅ Lo que se ha implementado

1. ✅ **Servicio real de email** usando JavaMail (Gmail SMTP)
2. ✅ **Notificaciones a pasajeros** cuando se cancela un vuelo
3. ✅ **Notificaciones a agencias** cuando se cancela un vuelo
4. ✅ **Exclusión automática** de vuelos cancelados en búsquedas
5. ✅ **Logs detallados** en consola para debugging

---

## ⚙️ Configuración Actual

### Archivo: `mail.properties`

```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true

mail.sender.email=rr36693904@gmail.com
mail.sender.password=oyyg qdla yqga bbho
```

✅ **Ya está configurado correctamente**

---

## 📝 Configurar Correos de Agencias

### Edita el archivo:
`backv6/src/main/java/com/sources/app/services/RealEmailService.java`

### Busca el método `getAgencyEmails()` (línea ~194):

```java
private List<String> getAgencyEmails() {
    List<String> emails = new ArrayList<>();
    
    // AGREGAR LOS CORREOS DE TUS AGENCIAS AQUÍ
    emails.add("rr36693904@gmail.com");  // Email de prueba
    
    // Agrega más correos de agencias:
    emails.add("agencia1@example.com");
    emails.add("agencia2@example.com");
    emails.add("tusistema@agenciaviajes.com");
    
    return emails;
}
```

---

## 🧪 Cómo Probar

### 1. **Compila el proyecto**

```bash
cd backv6
mvn clean compile
```

### 2. **Inicia el servidor**

```bash
mvn exec:java
```

O usa tu IDE (IntelliJ, Eclipse) para iniciar `AirlineServer.java`

### 3. **Cancela un vuelo**

```bash
curl -X PUT http://localhost:8080/api/airline/flights/123/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Prueba de notificaciones por email",
    "cancelledBy": 1
  }'
```

Reemplaza `123` con el ID real de un vuelo en tu base de datos.

---

## 📊 Logs que Deberías Ver

Cuando cancelas un vuelo, deberías ver en la consola:

```
=================================================
🚀 FlightDAO.cancelFlight() - INICIANDO CANCELACIÓN
=================================================
📋 Parámetros recibidos:
   - Flight ID: 123
   - Reason: Prueba de notificaciones por email
   - Cancelled by: 1
=================================================

🔍 Verificando si el vuelo puede ser cancelado:
   - ID del vuelo: 123
   - Estado actual: SCHEDULED
   - Fecha de salida: 2024-11-20
   - Fecha actual: 2024-10-31
   - ¿Puede ser cancelado?: true

✅ El vuelo puede ser cancelado, procediendo...

🚀 === INICIANDO ENVÍO DE NOTIFICACIONES DE CANCELACIÓN ===
✅ Configuración de correo cargada desde classpath

📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A PASAJEROS ===
📧 Vuelo: AA-123
📧 Ruta: Guatemala → México
📧 Motivo: Prueba de notificaciones por email
📧 Pasajeros a notificar: 2
📧 ✅ Correo enviado exitosamente a: pasajero1@email.com
📧 ✅ Correo enviado exitosamente a: pasajero2@email.com
✅ Correos enviados a pasajeros: 2/2
📧 ==========================================

📧 === ENVIANDO NOTIFICACIONES DE CANCELACIÓN A AGENCIAS ===
📧 Vuelo cancelado: AA-123
📧 Ruta: Guatemala → México
📧 Fecha: 2024-11-20 14:30
📧 Motivo: Prueba de notificaciones por email
📧 Agencias a notificar: 1
📧 ✅ Correo enviado exitosamente a: rr36693904@gmail.com
✅ Correos enviados a agencias: 1/1
📧 ==========================================

✅ Notificaciones enviadas exitosamente a pasajeros
✅ Notificaciones enviadas exitosamente a agencias
🚀 === FIN DE ENVÍO DE NOTIFICACIONES ===
```

---

## ❌ Solución de Problemas

### Problema 1: No veo NINGÚN log

**Causa**: El método `cancelFlight` no se está ejecutando.

**Solución**:
1. Verifica que el vuelo exista en la base de datos
2. Verifica que la URL del endpoint sea correcta
3. Verifica que el servidor esté corriendo
4. Revisa los logs del servidor para ver si hay errores

### Problema 2: Veo los logs pero NO se envían correos

**Causa**: Error en la configuración de Gmail o credenciales incorrectas.

**Soluciones**:

#### A. Verificar que Gmail permita "aplicaciones menos seguras"

1. Ve a: https://myaccount.google.com/security
2. Activa "Acceso de aplicaciones menos seguras"

#### B. Usar "Contraseña de Aplicación" (Recomendado)

1. Ve a: https://myaccount.google.com/apppasswords
2. Crea una nueva contraseña de aplicación
3. Copia la contraseña generada (ej: `abcd efgh ijkl mnop`)
4. Actualiza `mail.properties`:
   ```properties
   mail.sender.password=abcd efgh ijkl mnop
   ```

#### C. Verificar el error específico

Busca en los logs:
```
❌ Error enviando correo a xxx@email.com: [mensaje de error]
```

Errores comunes:
- `Authentication failed` → Contraseña incorrecta
- `Connection refused` → Firewall bloqueando puerto 587
- `Invalid Addresses` → Email mal formado

### Problema 3: Los correos se envían pero no llegan

**Causa**: Los correos están en spam o el email está mal configurado.

**Solución**:
1. Revisa la carpeta de SPAM
2. Verifica que el email del remitente sea válido
3. Espera unos minutos (puede haber delay)

### Problema 4: Error "mail.properties not found"

**Causa**: El archivo no está en el classpath correcto.

**Solución**:
```bash
# Verifica que el archivo existe
ls -la src/main/resources/mail.properties

# Recompila el proyecto
mvn clean compile
```

---

## 🔒 Seguridad

### ⚠️ IMPORTANTE: NO subas `mail.properties` a GitHub

Agrega al `.gitignore`:

```
# Archivos de configuración sensibles
src/main/resources/mail.properties
*.properties
```

### Usa variables de entorno en producción:

```java
String email = System.getenv("MAIL_SENDER_EMAIL");
String password = System.getenv("MAIL_SENDER_PASSWORD");
```

---

## 📧 Contenido de los Correos

### Para Pasajeros:

```
==========================================
CANCELACIÓN DE VUELO - AEROLINEA4
==========================================

Estimado/a [Nombre] [Apellido],

Lamentamos informarle que su vuelo ha sido CANCELADO.

DETALLES DEL VUELO CANCELADO:
• Número de Vuelo: AA-123
• Ruta: Guatemala → México
• Fecha: 20/11/2024
• Hora: 14:30
• Asiento: 12A (ECONOMY)
• Número de Boleto: #456

INFORMACIÓN DE REEMBOLSO:
• Monto a Reembolsar: Q450.00
• Motivo de Cancelación: Mal clima
• Fecha de Cancelación: 31/10/2024 10:30:00

PRÓXIMOS PASOS:
1. Su reembolso será procesado en 5-7 días hábiles
2. El monto será devuelto al método de pago original
...
```

### Para Agencias:

```
==========================================
NOTIFICACIÓN DE CANCELACIÓN DE VUELO
PARA AGENCIAS DE VIAJES - AEROLINEA4
==========================================

Estimada Agencia de Viajes,

Le informamos que el siguiente vuelo ha sido CANCELADO:

DETALLES DEL VUELO CANCELADO:
• Número de Vuelo: AA-123
• Ruta: Guatemala → México
• Fecha Programada: 20/11/2024
• Hora Programada: 14:30
• Estado: ❌ CANCELADO

MOTIVO DE LA CANCELACIÓN:
Mal clima

⚠️ ACCIONES REQUERIDAS:
• Este vuelo ya NO ESTÁ DISPONIBLE para nuevas reservas
• Contactar a sus clientes con reservas existentes
...
```

---

## 🎯 Verificar que Funciona Todo

### Checklist:

- [ ] ✅ Compilar proyecto sin errores
- [ ] ✅ Iniciar servidor correctamente
- [ ] ✅ Cancelar un vuelo vía API
- [ ] ✅ Ver logs de cancelación en consola
- [ ] ✅ Ver logs de envío de emails
- [ ] ✅ Recibir correo en tu bandeja de entrada
- [ ] ✅ Verificar que el vuelo NO aparece en búsquedas
- [ ] ✅ Configurar correos de agencias

---

## 📞 Ayuda Adicional

Si sigues teniendo problemas:

1. **Revisa los logs completos** del servidor
2. **Verifica la configuración** de Gmail
3. **Prueba con otro email** (si Gmail no funciona)
4. **Contacta al equipo** de desarrollo

---

## 🚀 Próximos Pasos (Opcional)

1. **Templates HTML** para correos más bonitos
2. **Cola de trabajos** (envío asíncrono con RabbitMQ)
3. **Dashboard** para ver historial de cancelaciones
4. **Webhooks** para notificar a agencias vía API
5. **SMS** adicional a correos electrónicos

---

**Última actualización**: 31/10/2024  
**Sistema**: AEROLINEA4 (backv6)  
**Versión**: 1.0

