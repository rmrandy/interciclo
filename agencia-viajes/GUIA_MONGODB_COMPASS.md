# 📊 Guía: Configurar API_KEY en MongoDB Compass

## 🎯 Objetivo

Guardar el API_KEY `AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L` en MongoDB para que la agencia lo use automáticamente.

---

## 🚀 OPCIÓN 1: MongoDB Compass (Interfaz Gráfica - Recomendado)

### Paso 1: Abrir MongoDB Compass
1. Abre **MongoDB Compass**
2. Conecta a tu servidor MongoDB (localhost:27017)
3. Selecciona la base de datos: **agencia-viajes**

### Paso 2: Crear Documento
1. En el panel izquierdo, busca la colección `corporate_config`
   - Si NO existe, créala:
     - Clic en "Create Collection"
     - Nombre: `corporate_config`
     - Clic en "Create Collection"

2. Clic en la colección `corporate_config`

3. Clic en el botón **"ADD DATA"** → **"Insert Document"**

4. **BORRA** todo el contenido y pega esto:

```json
{
  "_id": "airline_corporate_user",
  "apiKey": "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  "corporateUserEmail": "corporate@agencia-elvuelo.com",
  "companyName": "Agencia de Viajes El Vuelo",
  "enabled": true,
  "createdAt": {"$date": "2025-10-06T00:00:00.000Z"},
  "updatedAt": {"$date": "2025-10-06T00:00:00.000Z"}
}
```

5. Clic en **"Insert"**

### Paso 3: Verificar
Deberías ver el documento en la colección `corporate_config`:

```
_id: "airline_corporate_user"
apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L"
enabled: true
```

✅ **¡Listo!**

---

## 🚀 OPCIÓN 2: MongoDB Shell (Terminal)

### Paso 1: Abrir Terminal
```bash
# Conectar a MongoDB
mongosh

# O si tienes credenciales:
mongosh "mongodb://localhost:27017/agencia-viajes"
```

### Paso 2: Seleccionar Base de Datos
```javascript
use agencia-viajes
```

### Paso 3: Insertar Configuración
```javascript
db.corporate_config.insertOne({
  _id: "airline_corporate_user",
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  corporateUserEmail: "corporate@agencia-elvuelo.com",
  companyName: "Agencia de Viajes El Vuelo",
  enabled: true,
  createdAt: new Date(),
  updatedAt: new Date()
});
```

### Paso 4: Verificar
```javascript
db.corporate_config.findOne({ _id: "airline_corporate_user" });
```

**Deberías ver:**
```javascript
{
  _id: 'airline_corporate_user',
  apiKey: 'AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L',
  corporateUserEmail: 'corporate@agencia-elvuelo.com',
  companyName: 'Agencia de Viajes El Vuelo',
  enabled: true,
  createdAt: ISODate('2025-10-06T...'),
  updatedAt: ISODate('2025-10-06T...')
}
```

✅ **¡Listo!**

---

## 🚀 OPCIÓN 3: Desde Python (Terminal)

### Ejecutar este comando:
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/agencia-viajes/backend-django

python3 << 'EOF'
from pymongo import MongoClient
from datetime import datetime

client = MongoClient('mongodb://localhost:27017/')
db = client['agencia-viajes']

db.corporate_config.insert_one({
    '_id': 'airline_corporate_user',
    'apiKey': 'AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L',
    'corporateUserEmail': 'corporate@agencia-elvuelo.com',
    'companyName': 'Agencia de Viajes El Vuelo',
    'enabled': True,
    'createdAt': datetime.utcnow(),
    'updatedAt': datetime.utcnow()
})

print('✅ Configuración guardada')

# Verificar
config = db.corporate_config.find_one({'_id': 'airline_corporate_user'})
print('API_KEY guardado:', config.get('apiKey'))
EOF
```

---

## ✅ Verificación

### En MongoDB Compass:
1. Ve a la base de datos `agencia-viajes`
2. Abre la colección `corporate_config`
3. Deberías ver 1 documento con tu API_KEY

### En Terminal:
```bash
mongosh agencia-viajes --eval "db.corporate_config.findOne({_id: 'airline_corporate_user'})"
```

---

## 📋 ¿Qué Prefieres?

- **Opción 1** (MongoDB Compass): Más visual y fácil ⭐⭐⭐⭐⭐
- **Opción 2** (mongo shell): Terminal, rápido ⭐⭐⭐⭐
- **Opción 3** (Python): Un solo comando ⭐⭐⭐

**Recomiendo Opción 1** si tienes MongoDB Compass instalado.

---

¿Cuál opción prefieres usar? O si ya lo hiciste, avísame para continuar con la compilación y pruebas. 😊
