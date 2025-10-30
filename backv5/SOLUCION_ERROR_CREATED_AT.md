# Solución: Error ORA-01843 - not a valid month

## 🔴 Problema
Al intentar crear un ticket (compra de boleto), se produce el error:
```
ORA-01843: not a valid month
```

Este error ocurre porque Oracle está intentando interpretar el campo `CREATED_AT` como una fecha, pero el formato no coincide con la configuración de la base de datos.

## ✅ Solución
Crear triggers en Oracle que establezcan automáticamente los campos de fecha (`CREATED_AT`, `UPDATED_AT`, `BOOKING_DATE`, `BOOKING_TIME`).

---

## 📋 PASOS PARA SOLUCIONAR

### Paso 1: Abrir DBeaver (o tu cliente SQL de Oracle)

1. Conectarte a tu base de datos Oracle
2. Seleccionar tu esquema (usuario) donde está la tabla `TICKETS`

### Paso 2: Verificar la estructura actual

Ejecuta este query para ver el tipo de dato de las columnas:

```sql
SELECT 
    column_name, 
    data_type, 
    data_length, 
    nullable
FROM user_tab_columns 
WHERE table_name = 'TICKETS' 
AND column_name IN ('CREATED_AT', 'UPDATED_AT', 'BOOKING_DATE', 'BOOKING_TIME', 'CANCELLATION_DATE');
```

### Paso 3: Crear trigger para CREATED_AT

Copia y ejecuta este script completo en DBeaver:

```sql
-- Crear trigger para establecer automáticamente CREATED_AT, BOOKING_DATE y BOOKING_TIME
CREATE OR REPLACE TRIGGER TRG_TICKETS_CREATED_AT
BEFORE INSERT ON TICKETS
FOR EACH ROW
BEGIN
    -- Establecer CREATED_AT si es NULL o vacío
    IF :NEW.CREATED_AT IS NULL THEN
        :NEW.CREATED_AT := TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS');
    END IF;
    
    -- Establecer BOOKING_DATE si es NULL
    IF :NEW.BOOKING_DATE IS NULL THEN
        :NEW.BOOKING_DATE := TO_CHAR(SYSDATE, 'YYYY-MM-DD');
    END IF;
    
    -- Establecer BOOKING_TIME si es NULL
    IF :NEW.BOOKING_TIME IS NULL THEN
        :NEW.BOOKING_TIME := TO_CHAR(SYSTIMESTAMP, 'HH24:MI:SS');
    END IF;
END;
/
```

### Paso 4: Crear trigger para UPDATED_AT

```sql
-- Crear trigger para establecer automáticamente UPDATED_AT
CREATE OR REPLACE TRIGGER TRG_TICKETS_UPDATED_AT
BEFORE UPDATE ON TICKETS
FOR EACH ROW
BEGIN
    -- Actualizar UPDATED_AT automáticamente
    :NEW.UPDATED_AT := TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS');
END;
/
```

### Paso 5: Verificar que los triggers se crearon correctamente

```sql
SELECT trigger_name, status, trigger_type 
FROM user_triggers 
WHERE table_name = 'TICKETS';
```

Deberías ver:
```
TRIGGER_NAME              | STATUS  | TRIGGER_TYPE
--------------------------|---------|-------------
TRG_TICKETS_CREATED_AT    | ENABLED | BEFORE EACH ROW
TRG_TICKETS_UPDATED_AT    | ENABLED | BEFORE EACH ROW
```

### Paso 6: Compilar el backend (en tu terminal)

```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv5
mvn clean compile
```

### Paso 7: Reiniciar el servidor backend

```bash
# Detener el servidor actual
pkill -f "java.*App"

# Iniciar el servidor
mvn exec:java -Dexec.mainClass="com.sources.app.App"
```

### Paso 8: Probar la compra

Ahora intenta realizar una compra desde:
- La aerolínea (frontend)
- La agencia de viajes

La compra debería funcionar correctamente sin errores.

---

## 🧪 PRUEBA OPCIONAL (Solo para verificar)

Si quieres probar que el trigger funciona correctamente, puedes ejecutar este INSERT de prueba:

```sql
-- Insertar un ticket de prueba
INSERT INTO TICKETS (
    FLIGHT_ID, USER_ID, SEAT_NUMBER, SEAT_CATEGORY, 
    FARE, STATUS, PAYMENT_STATUS, PAYMENT_METHOD, 
    TOTAL_AMOUNT, PASSENGER_FIRST_NAME, PASSENGER_LAST_NAME,
    PASSENGER_DOCUMENT_TYPE, PASSENGER_DOCUMENT_NUMBER,
    PASSENGER_EMAIL, PASSENGER_PHONE, QUANTITY
) VALUES (
    201, 1, 'TEST999', 'ECONOMY',
    100, 'RESERVED', 'PENDING', 'CREDIT_CARD',
    100, 'Test', 'User',
    'PASSPORT', '123456',
    'test@test.com', '12345678', 1
);

-- Ver el ticket creado (debería tener CREATED_AT, BOOKING_DATE y BOOKING_TIME automáticamente)
SELECT 
    ID_TICKET, SEAT_NUMBER, CREATED_AT, BOOKING_DATE, BOOKING_TIME
FROM TICKETS 
WHERE SEAT_NUMBER = 'TEST999';

-- Limpiar el registro de prueba
DELETE FROM TICKETS WHERE SEAT_NUMBER = 'TEST999';
COMMIT;
```

---

## 📚 Explicación Técnica

### ¿Por qué ocurría el error?

1. La columna `CREATED_AT` en Oracle puede ser de tipo `DATE`, `TIMESTAMP` o `VARCHAR2`
2. Hibernate intentaba insertar un valor en este campo
3. Oracle intentaba interpretar el valor como fecha con su formato predeterminado
4. El formato enviado no coincidía con el formato esperado → Error ORA-01843

### ¿Cómo lo resolvemos?

1. Marcamos `CREATED_AT` como `insertable = false, updatable = false` en Java
2. Hibernate ya no intentará insertar valores en este campo
3. El trigger de Oracle se encarga automáticamente de establecer el valor correcto
4. El formato siempre será correcto porque lo genera Oracle directamente

### Ventajas de usar triggers

✅ El formato de fecha siempre es consistente  
✅ No depende de la configuración regional de Java  
✅ Oracle maneja nativamente sus propios tipos de datos  
✅ Más robusto y menos propenso a errores  
✅ Los timestamps son automáticos, no se pueden olvidar

---

## 🔍 Troubleshooting

### Si el trigger no se crea

**Error:** `ORA-00942: table or view does not exist`
- **Solución:** Asegúrate de estar conectado con el usuario correcto (el dueño de la tabla TICKETS)

**Error:** `ORA-04098: trigger is invalid and failed re-validation`
- **Solución:** Revisa la sintaxis del trigger, puede haber un error de tipeo

### Si sigue dando error después de crear el trigger

1. Verifica que el trigger esté ENABLED:
```sql
SELECT trigger_name, status FROM user_triggers WHERE table_name = 'TICKETS';
```

2. Si está DISABLED, habilitalo:
```sql
ALTER TRIGGER TRG_TICKETS_CREATED_AT ENABLE;
ALTER TRIGGER TRG_TICKETS_UPDATED_AT ENABLE;
```

3. Reinicia el backend completamente

---

## ✅ Confirmación de Éxito

Después de aplicar estos cambios, deberías poder:

1. ✅ Comprar boletos desde la aerolínea sin errores
2. ✅ Comprar boletos desde la agencia sin errores
3. ✅ Ver los tickets creados con `CREATED_AT` correctamente establecido
4. ✅ Ver que `BOOKING_DATE` y `BOOKING_TIME` se establecen automáticamente

---

## 📞 Si necesitas ayuda

Si después de seguir estos pasos sigues teniendo problemas:

1. Copia el mensaje de error completo
2. Ejecuta este query y comparte el resultado:
```sql
SELECT trigger_name, status, trigger_type 
FROM user_triggers 
WHERE table_name = 'TICKETS';
```
3. Comparte los logs del backend Java

---

**Fecha de creación:** 28 de octubre de 2025  
**Última actualización:** 28 de octubre de 2025


