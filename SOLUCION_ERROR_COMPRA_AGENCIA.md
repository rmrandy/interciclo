# ✅ Solución: Error de Compra en Agencia

## 🎯 Problema

```
Error: socket hang up
HTTPConnectionPool(host='192.168.0.2', port=8080): Read timed out
```

## ✅ Solución Aplicada

### 1. Configuración MongoDB Actualizada
```javascript
{
  name: "AeroLinea Principal",
  host: "localhost",      // ← Cambio de IP a localhost
  port: 8080,
  timeoutMs: 40000,       // ← Timeout aumentado a 40s
  enabled: true,
  corporateApiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L"
}
```

### 2. Código Django Actualizado
- ✅ Timeout por defecto: 40 segundos
- ✅ Lee configuración de MongoDB
- ✅ Usa API_KEY automáticamente

---

## 🔄 IMPORTANTE: Reiniciar Django

**Para que funcione, DEBES reiniciar Django:**

```bash
# En la terminal donde está Django:
# 1. Detener con Ctrl+C

# 2. Iniciar de nuevo:
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django
./scripts/start-django.sh
```

**Verás en los logs al iniciar:**
```
✅ Usando aerolínea de BD: AeroLinea Principal (http://localhost:8080/api)
Timeout configurado: 40 segundos
```

---

## 🧪 Después de Reiniciar

### 1. Verifica la Configuración
```bash
curl http://localhost:5001/api/airlines/active
```

**Deberías ver:**
```json
{
  "success": true,
  "data": {
    "name": "AeroLinea Principal",
    "host": "localhost",
    "timeoutMs": 40000
  },
  "fullUrl": "http://localhost:8080/api"
}
```

### 2. Prueba Buscar Vuelos

En la agencia (`http://localhost:5173`):
- Busca vuelos
- **Debería funcionar sin error 502**

### 3. Prueba Comprar

- Completa el formulario de compra
- **Debería funcionar sin timeout**

**Logs esperados en Django:**
```
🔑 Usando API_KEY empresarial para autenticación
✅ Usando aerolínea de BD: AeroLinea Principal (http://localhost:8080/api)
```

**Logs esperados en Java:**
```
✅ Usuario empresarial autenticado: Agencia de Viajes El Vuelo
🏢 Compra empresarial detectada
```

---

## 📊 Configuración Final

| Componente | Configuración | Estado |
|------------|---------------|--------|
| **Host** | localhost | ✅ |
| **Puerto** | 8080 | ✅ |
| **Timeout** | 40 segundos | ✅ |
| **API_KEY** | AGV-WQD... | ✅ |
| **Backend Java** | Corriendo | ✅ |
| **Backend Django** | Necesita reinicio | ⏭️ |

---

## 🎯 Checklist

- [x] MongoDB configurado con localhost
- [x] Timeout aumentado a 40s
- [x] API_KEY guardado
- [x] Código actualizado
- [ ] **Reiniciar Django** ← FALTA ESTO

---

## 🚀 Comando para Reiniciar

```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django

# Detener (Ctrl+C) y luego:
./scripts/start-django.sh
```

---

**Estado**: ✅ Configuración lista  
**Falta**: Reiniciar Django  
**Después**: Todo funcionará ✅  

## 🔄 ¡Reinicia Django y prueba de nuevo!

