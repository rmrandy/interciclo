# 🔧 Solución: API_KEY No Llega al Backend Java

## 🎯 Problema

```
⚠️  No se encontró API Key en los headers
java.lang.RuntimeException: userId es requerido para compras individuales
```

## ✅ Solución

El problema es que **Django NO se ha reiniciado** con el código actualizado.

### DEBES HACER ESTO:

#### 1. Reiniciar Backend Django (OBLIGATORIO)

```bash
# En la terminal de Django:
# 1. Presiona Ctrl+C para detener

# 2. Inicia de nuevo:
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django
./scripts/start-django.sh
```

#### 2. Reiniciar Backend Java (OBLIGATORIO)

```bash
# En la terminal de Java:
# 1. Presiona Ctrl+C para detener

# 2. Inicia de nuevo:
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4
mvn exec:java -Dexec.mainClass="com.sources.app.App"
```

---

## 🔍 Logs de Debug

### Cuando Reinicies Django y Compres

**Deberías ver en la terminal de Django:**
```
🔍 DEBUG: Buscando config empresarial en MongoDB...
✅ DEBUG: Config encontrada: airline_corporate_user
✅ DEBUG: API_KEY encontrado: AGV-WQDJGN...
🔑 USANDO API_KEY EMPRESARIAL: AGV-WQDJGN5KLBM... en headers
```

**Deberías ver en la terminal de Java:**
```
✅ Usuario empresarial encontrado por API Key: Agencia de Viajes El Vuelo
🏢 Compra empresarial detectada: Agencia de Viajes El Vuelo
🏢 Ticket: Usuario=XXX, Comprado por=XXX
✅ Ticket creado exitosamente
```

---

## ❌ Si NO Ves Estos Logs

### Problema: Django no se reinició
**Solución**: 
1. Ctrl+C en Django
2. Ejecuta: `./scripts/start-django.sh`
3. Espera a que diga "Django running..."
4. Prueba de nuevo

### Problema: MongoDB no tiene el API_KEY
**Verificar**:
```bash
mongosh agencia-viajes --eval 'db.corporate_config.findOne({_id: "airline_corporate_user"})'
```

**Debe mostrar**: `apiKey: 'AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L'`

### Problema: Java no reconoce el API_KEY
**Verificar en Oracle**:
```sql
SELECT * FROM USERS WHERE API_KEY = 'AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L';
```

**Debe mostrar**: Usuario con IS_CORPORATE = 1

---

## 📊 Checklist de Verificación

Antes de comprar, verifica:

- [ ] **Django reiniciado** con código nuevo
- [ ] **Java reiniciado** con código nuevo
- [ ] **MongoDB tiene API_KEY** (verificar con mongosh)
- [ ] **Oracle tiene usuario empresarial** (verificar con SQL)
- [ ] **Frontend agencia sin caché** (Cmd+Shift+R)

---

## 🎯 Orden de Acciones

```
1. DETENER Django (Ctrl+C)
   ↓
2. DETENER Java (Ctrl+C)
   ↓
3. INICIAR Java
   cd backv4
   mvn exec:java -Dexec.mainClass="com.sources.app.App"
   ↓
4. Esperar a que Java diga "Servidor iniciado..."
   ↓
5. INICIAR Django
   cd agencia-viajes/backend-django
   ./scripts/start-django.sh
   ↓
6. Esperar a que Django diga "Django running..."
   ↓
7. Refrescar agencia web (Cmd+Shift+R)
   ↓
8. Comprar
```

---

## 🔍 Qué Buscar en los Logs

### Cuando Inicies Django

Deberías ver al hacer una compra:
```
🔍 DEBUG: Buscando config empresarial en MongoDB...
✅ DEBUG: Config encontrada: airline_corporate_user
✅ DEBUG: API_KEY encontrado: AGV-WQDJGN...
🔑 USANDO API_KEY EMPRESARIAL: AGV-WQDJGN5KLBM... en headers
```

### Si NO ves esos logs

Significa que Django está usando código viejo. **Reinícialo**.

---

**Problema**: Django no reiniciado con código nuevo  
**Solución**: Ctrl+C y ./scripts/start-django.sh  
**Resultado**: API_KEY se enviará y compra funcionará  

## 🔄 ¡REINICIA DJANGO AHORA!

