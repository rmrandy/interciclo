# ⏱️ Timeout Aumentado a 40 Segundos

## ✅ Cambios Realizados

### 1. Backend Django - views.py
```python
# Timeout por defecto cambiado de 20s a 40s
def airline_timeout_seconds():
    return 40  # 40 segundos
```

### 2. Script start-django.sh
```bash
export AIRLINE_TIMEOUT_MS="40000"  # 40 segundos
```

### 3. MongoDB - Aerolínea Creada
```javascript
{
  name: "Servidor Principal",
  host: "192.168.0.2",
  port: 8080,
  timeoutMs: 40000,  // 40 segundos
  enabled: true
}
```

---

## 🎯 Configuración Actual

**Timeout**: 40 segundos (40,000 ms)

**Esto significa**:
- ✅ La agencia esperará hasta 40 segundos para respuesta de la aerolínea
- ✅ No más errores de "Read timed out" en operaciones lentas
- ✅ Suficiente tiempo para operaciones complejas

---

## 🔄 Para Aplicar los Cambios

### Reiniciar Backend Django
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django

# Detener el servidor (Ctrl+C)
# Iniciar de nuevo:
./scripts/start-django.sh
```

**Verás en los logs**:
```
Timeout configurado: 40 segundos
```

---

## 🧪 Verificar Configuración

### En MongoDB
```bash
mongosh agencia-viajes --eval 'db.airlines.findOne({enabled: true}, {name: 1, host: 1, timeoutMs: 1})'
```

**Deberías ver**:
```javascript
{
  name: 'Servidor Principal',
  host: '192.168.0.2',
  timeoutMs: 40000
}
```

---

## 💡 Ajustar Timeout si es Necesario

### Desde el Panel de Admin

1. Ve a: **Admin** → **Aerolíneas**
2. Edita la aerolínea "Servidor Principal"
3. Cambia **Timeout (ms)** a: `40000` (o el valor que necesites)
4. Guarda

**Se aplicará inmediatamente en la siguiente llamada**

### Desde MongoDB Directamente

```javascript
// Aumentar a 60 segundos (si 40 no es suficiente)
db.airlines.updateOne(
  {enabled: true},
  {$set: {timeoutMs: 60000, updatedAt: new Date()}}
);
```

---

## ⏱️ Timeouts Recomendados

| Escenario | Timeout | Comando |
|-----------|---------|---------|
| **Desarrollo local** | 40s | `{timeoutMs: 40000}` |
| **Red lenta** | 60s | `{timeoutMs: 60000}` |
| **Producción rápida** | 20s | `{timeoutMs: 20000}` |
| **Testing** | 10s | `{timeoutMs: 10000}` |

**Configurado actualmente**: 40 segundos ✅

---

**Timeout**: 40 segundos  
**Estado**: ✅ Configurado  
**Próximo paso**: Reiniciar Django  

## 🔄 Reinicia Django para que aplique!

