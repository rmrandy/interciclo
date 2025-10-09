# ✅ Checklist - Sistema de Usuarios Empresariales

## 🎯 Estado Actual

### ✅ COMPLETADO
- [x] **MongoDB**: API_KEY guardado (`AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L`)
- [x] **Oracle**: Usuario empresarial creado
- [x] **Código Java**: Actualizado y compilado (BUILD SUCCESS)
- [x] **Código Django**: Actualizado con headers API_KEY
- [x] **Estilos**: Mejorados en aerolínea

---

## ⏭️ FALTA HACER (Para que Funcione)

### 1. Reiniciar Backend Java ⚠️
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4

# Si está corriendo, detenerlo (Ctrl+C)
# Luego iniciar con el código nuevo:
mvn exec:java -Dexec.mainClass="com.sources.app.App"
```

**Por qué**: El servidor necesita cargar el nuevo código con soporte de API_KEY

### 2. Reiniciar Backend Django (si está corriendo) ⚠️
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django

# Detener (Ctrl+C) y reiniciar:
./scripts/start-django.sh
```

**Por qué**: Necesita leer el API_KEY de MongoDB

### 3. (Opcional) Crear Índices en Oracle 📊
```bash
# Ejecutar en DBeaver:
# Archivo: INDICES_VUELOS_PERFORMANCE.sql
```

**Por qué**: Para que sea más rápido (20-30x)

---

## 🧪 Prueba Rápida

### Después de Reiniciar los Servidores:

#### Prueba 1: Ver API_KEY desde Django
```bash
# En una terminal:
curl http://localhost:5001/api/airlines/active
```

**Deberías ver**: La configuración de la aerolínea

#### Prueba 2: Comprar desde la Agencia

1. Abre: `http://localhost:5173` (agencia)
2. Busca vuelos
3. Selecciona uno
4. Completa datos del cliente
5. Compra

**Logs esperados en Django**:
```
🔑 Usando API_KEY empresarial para autenticación
```

**Logs esperados en Java**:
```
✅ Usuario empresarial autenticado: Agencia de Viajes El Vuelo (ID: ...)
🏢 Compra empresarial detectada: Agencia de Viajes El Vuelo
🏢 Ticket comprado por usuario empresarial: Agencia de Viajes El Vuelo
```

---

## 📊 Checklist Visual

```
┌─────────────────────────────────────────────────┐
│ CONFIGURACIÓN                                   │
├─────────────────────────────────────────────────┤
│ ✅ MongoDB: API_KEY guardado                    │
│ ✅ Oracle: Usuario empresarial creado           │
│ ✅ Código Java: Compilado                       │
│ ✅ Código Django: Actualizado                   │
│ ✅ Estilos: Mejorados                           │
├─────────────────────────────────────────────────┤
│ PENDIENTE                                       │
├─────────────────────────────────────────────────┤
│ ⏭️ Reiniciar Backend Java                       │
│ ⏭️ Reiniciar Backend Django                     │
│ 📊 Crear índices (opcional pero recomendado)   │
└─────────────────────────────────────────────────┘
```

---

## 🚀 Comandos para Iniciar Todo

### Terminal 1 - Backend Java
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/backv4
mvn exec:java -Dexec.mainClass="com.sources.app.App"
```

### Terminal 2 - Backend Django
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django
./scripts/start-django.sh
```

### Terminal 3 - Frontend Agencia (si no está corriendo)
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/agencia
npm run dev
```

---

## 💡 Respuesta a tu Pregunta

**"¿Ya puedo comprar desde la agencia?"**

**Casi, necesitas:**
1. ✅ Código compilado (YA ESTÁ)
2. ⏭️ **Reiniciar backend Java** (falta)
3. ⏭️ **Reiniciar backend Django** (falta)

**Después de eso**: SÍ, podrás comprar desde la agencia y usará el API_KEY automáticamente ✅

---

## 🔍 Cómo Saber si Funciona

### Cuando Compres desde la Agencia

**Verás en logs de Django:**
```
🔑 Usando API_KEY empresarial para autenticación
✅ Usando aerolínea de BD: [nombre] (http://...)
```

**Verás en logs de Java:**
```
✅ Usuario empresarial encontrado por API Key: Agencia de Viajes El Vuelo
🏢 Compra empresarial detectada: Agencia de Viajes El Vuelo
DEBUG: 🎫 Creando boleto...
🏢 Ticket comprado por usuario empresarial: Agencia de Viajes El Vuelo
```

**Si NO ves estos logs**: El API_KEY no se está usando (verifica MongoDB y que reiniciaste Django)

---

**Estado**: ✅ Código listo y compilado  
**Falta**: Reiniciar servidores  
**Después**: ¡Listo para comprar desde la agencia!  

## 🚀 ¿Reinicio los servidores por ti o lo haces tú?
