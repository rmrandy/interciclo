# 🔧 Solución: Error al Insertar Tickets en Oracle

## ❌ Problema Original

```
ERROR: ORA-01400: cannot insert NULL into ("AEROLINEA3"."TICKETS"."ID_TICKET")
```

### ¿Por qué ocurrió?

El error ocurría porque:

1. **Oracle no soporta `GenerationType.IDENTITY`** de la misma manera que MySQL o PostgreSQL
2. Las entidades estaban configuradas con `@GeneratedValue(strategy = GenerationType.IDENTITY)`
3. Cuando Hibernate intentaba insertar un registro, Oracle no podía generar el ID automáticamente
4. Resultado: Intentaba insertar `NULL` en el campo `ID_TICKET`, violando la restricción `NOT NULL`

### ¿Por qué otras operaciones funcionaban?

- **Crear ciudades funcionó inicialmente** porque probablemente ya existía un trigger o secuencia previa
- **Luego falló** cuando intentó crear una ciudad duplicada (otro error diferente)
- **Analytics falló** por el mismo problema de secuencias

## ✅ Solución Implementada

### 1. Cambios en las Entidades

He corregido las entidades críticas para usar **secuencias de Oracle**:

#### Ticket.java
```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_seq")
@SequenceGenerator(name = "ticket_seq", sequenceName = "SEQ_TICKETS", allocationSize = 1)
@Column(name = "ID_TICKET")
private Long idTicket;
```

#### ClickEvent.java
```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "click_event_seq")
@SequenceGenerator(name = "click_event_seq", sequenceName = "SEQ_CLICK_EVENTS", allocationSize = 1)
@Column(name = "ID_CLICK")
private Long idClick;
```

#### City.java
```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "city_seq")
@SequenceGenerator(name = "city_seq", sequenceName = "SEQ_CITIES", allocationSize = 1)
@Column(name = "ID_CITY")
private Long idCity;
```

### 2. Script SQL para Crear Secuencias

He creado el archivo `fix_sequences_oracle.sql` con las secuencias necesarias.

## 🚀 Pasos para Aplicar la Solución

### 1️⃣ Ejecutar el Script SQL

**Abre SQL Developer, DBeaver o SQL*Plus** y ejecuta:

```bash
cd backv5
# Copia el contenido de fix_sequences_oracle.sql
# O ejecuta desde la línea de comandos:
sqlplus usuario/contraseña@basedatos @fix_sequences_oracle.sql
```

O manualmente, ejecuta estos comandos en tu cliente SQL:

```sql
-- Crear secuencias
CREATE SEQUENCE SEQ_TICKETS START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_CLICK_EVENTS START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_CITIES START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;

-- Verificar
SELECT sequence_name, last_number FROM user_sequences
WHERE sequence_name IN ('SEQ_TICKETS', 'SEQ_CLICK_EVENTS', 'SEQ_CITIES');
```

### 2️⃣ Ajustar Secuencias si Ya Tienes Datos

Si ya tienes registros en las tablas, ajusta las secuencias:

```sql
-- Para TICKETS
DECLARE
  v_max_id NUMBER;
BEGIN
  SELECT NVL(MAX(ID_TICKET), 0) + 1 INTO v_max_id FROM TICKETS;
  EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_TICKETS';
  EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_TICKETS START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
END;
/

-- Para CITIES
DECLARE
  v_max_id NUMBER;
BEGIN
  SELECT NVL(MAX(ID_CITY), 0) + 1 INTO v_max_id FROM CITIES;
  EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_CITIES';
  EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_CITIES START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
END;
/

-- Para CLICK_EVENTS
DECLARE
  v_max_id NUMBER;
BEGIN
  SELECT NVL(MAX(ID_CLICK), 0) + 1 INTO v_max_id FROM CLICK_EVENTS;
  EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_CLICK_EVENTS';
  EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_CLICK_EVENTS START WITH ' || v_max_id || ' INCREMENT BY 1 NOCACHE NOCYCLE';
END;
/
```

### 3️⃣ Reiniciar el Backend

```bash
# Detener el backend actual (Ctrl+C)
# Reiniciar
cd backv5
./start-backend.sh 8080
```

El código ya está compilado, solo necesitas reiniciar.

## ✅ Verificar que Funciona

### Prueba 1: Crear un Ticket

Desde el frontend de la agencia, intenta comprar un boleto.

**Deberías ver en los logs del backend:**

```
Hibernate: select next value for SEQ_TICKETS
Hibernate: insert into TICKETS (...) values (...)
```

**Sin errores ORA-01400.**

### Prueba 2: Verificar la Secuencia

En SQL:

```sql
-- Ver el valor actual de la secuencia
SELECT SEQ_TICKETS.CURRVAL FROM DUAL;

-- Ver el siguiente valor
SELECT SEQ_TICKETS.NEXTVAL FROM DUAL;
```

### Prueba 3: Insertar un Ticket Manualmente

```sql
-- Insertar un ticket de prueba
INSERT INTO TICKETS (
  ID_TICKET, FLIGHT_ID, USER_ID, SEAT_NUMBER, SEAT_CATEGORY, 
  FARE, STATUS, PAYMENT_STATUS, PAYMENT_METHOD, TOTAL_AMOUNT
) VALUES (
  SEQ_TICKETS.NEXTVAL, 1, 1, '12A', 'ECONOMY', 
  100.00, 'CONFIRMED', 'PAID', 'CREDIT_CARD', 100.00
);

COMMIT;
```

## 📊 Diferencias entre IDENTITY y SEQUENCE

| Característica | IDENTITY (MySQL/PostgreSQL) | SEQUENCE (Oracle) |
|----------------|----------------------------|-------------------|
| **Soporte en Oracle** | ❌ Limitado/No funciona | ✅ Nativo |
| **Configuración** | Automática | Requiere secuencia |
| **Rendimiento** | Bueno | Excelente |
| **Flexibilidad** | Limitada | Alta |

## 🔍 Entidades Corregidas

Las siguientes entidades fueron corregidas:

1. ✅ **Ticket** - Ahora usa `SEQ_TICKETS`
2. ✅ **ClickEvent** - Ahora usa `SEQ_CLICK_EVENTS`
3. ✅ **City** - Ahora usa `SEQ_CITIES`

## ⚠️ Nota sobre Otras Entidades

Hay **28 entidades más** que aún usan `GenerationType.IDENTITY`. Si tienes problemas similares con otras tablas, aplica el mismo patrón:

1. Cambia `GenerationType.IDENTITY` a `GenerationType.SEQUENCE`
2. Añade `@SequenceGenerator` con el nombre de la secuencia
3. Crea la secuencia en Oracle

**Ejemplo genérico:**

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nombre_entidad_seq")
@SequenceGenerator(name = "nombre_entidad_seq", sequenceName = "SEQ_NOMBRE_TABLA", allocationSize = 1)
@Column(name = "ID_CAMPO")
private Long id;
```

```sql
CREATE SEQUENCE SEQ_NOMBRE_TABLA START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
```

## 🐛 Errores Comunes

### Error: "sequence SEQ_TICKETS does not exist"

**Solución:** Ejecuta el script SQL para crear la secuencia.

### Error: "cannot insert duplicate key"

**Solución:** La secuencia está generando IDs que ya existen. Ajusta el valor inicial de la secuencia.

### Error: "ORA-01400" persiste

**Solución:** Verifica que:
1. La secuencia existe: `SELECT * FROM user_sequences WHERE sequence_name = 'SEQ_TICKETS'`
2. El backend está reiniciado
3. La entidad tiene la anotación `@SequenceGenerator` correcta

## 📚 Referencias

- [Oracle Sequences](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-SEQUENCE.html)
- [JPA GenerationType](https://docs.oracle.com/javaee/7/api/javax/persistence/GenerationType.html)
- [Hibernate @SequenceGenerator](https://docs.jboss.org/hibernate/orm/6.2/javadocs/org/hibernate/annotations/SequenceGenerator.html)

## ✅ Resultado Esperado

Después de aplicar esta solución:

✅ Las compras de boletos funcionan correctamente  
✅ No más errores ORA-01400  
✅ Los IDs se generan automáticamente  
✅ El sistema funciona con múltiples agencias y aerolíneas  
✅ Analytics funciona sin errores  
✅ Ciudades se crean sin problemas de ID  


