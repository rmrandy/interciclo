# 🚀 Cómo Iniciar el Frontend de Aerolínea

## ⚡ Formas Rápidas de Iniciar

### 1️⃣ Puerto por Defecto (5050) - Más Fácil ✅
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/aerolinea

# Opción A: Script personalizado
./start-port.sh

# Opción B: npm script
npm run dev:5050
```

**Resultado**: `http://localhost:5050`

---

### 2️⃣ Puerto Personalizado - Más Flexible ✅
```bash
# Cualquier puerto que quieras
./start-port.sh 3000     # Puerto 3000
./start-port.sh 8081     # Puerto 8081
./start-port.sh 4200     # Puerto 4200
./start-port.sh 9090     # Puerto 9090
```

**Resultado**: `http://localhost:[PUERTO]`

---

### 3️⃣ Variable de Entorno - Más Control ✅
```bash
# Especificar puerto con variable de entorno
PORT=5050 npm run dev
PORT=3000 npm run dev
PORT=7777 npm run dev
```

---

### 4️⃣ Scripts npm Predefinidos - Más Rápido ✅
```bash
npm run dev:5050    # Puerto 5050
npm run dev:3000    # Puerto 3000
npm run dev:8081    # Puerto 8081
```

---

## 🎯 Ejemplos Prácticos

### Desarrollo Normal
```bash
cd aerolinea
./start-port.sh
# Servidor en http://localhost:5050
```

### Testing en Otro Puerto
```bash
cd aerolinea
./start-port.sh 8888
# Servidor en http://localhost:8888
```

### Múltiples Instancias (Avanzado)
```bash
# Terminal 1
PORT=5050 npm run dev

# Terminal 2
PORT=5051 npm run dev

# Ahora tienes dos instancias corriendo
```

---

## 📊 Configuración Actual

### vite.config.ts
```typescript
server: {
  port: parseInt(process.env.PORT || '5050'), // Puerto por defecto
  host: true, // Accesible desde red local
  strictPort: false, // Busca otro si está ocupado
  open: false, // No abre navegador automáticamente
}
```

**Características:**
- ✅ Puerto por defecto: 5050
- ✅ Configurable con variable `PORT`
- ✅ Si el puerto está ocupado, busca otro
- ✅ Accesible desde otros dispositivos en tu red
- ✅ No molesta abriendo el navegador

---

## 🔍 Ver en Qué Puerto Está Corriendo

Cuando inicies el servidor, verás:

```bash
🚀 Ensurance Pharmacy - Frontend Aerolínea
===========================================

✅ Usando puerto personalizado: 5050

📡 Obteniendo dirección IP...
[IP de tu máquina]

🌐 Iniciando servidor de desarrollo en puerto 5050...

  VITE v4.5.14  ready in 523 ms

  ➜  Local:   http://localhost:5050/      👈 Usar este
  ➜  Network: http://192.168.1.100:5050/  👈 O este desde otros dispositivos
```

---

## 🛠️ Cambiar Puerto Si Está Ocupado

### Si ves este error:
```
Port 5050 is in use, trying another one...
```

**Solución 1**: El sistema automáticamente usará otro puerto (5051, 5052, etc.)

**Solución 2**: Especifica un puerto diferente:
```bash
./start-port.sh 6000
```

**Solución 3**: Mata el proceso que usa el puerto:
```bash
# Ver qué está usando el puerto 5050
lsof -i :5050

# Matar el proceso
kill -9 [PID]
```

---

## 🌐 Acceder Desde Otros Dispositivos

Si quieres acceder desde tu teléfono o tablet en la misma red:

1. Inicia el servidor normalmente
2. Busca la línea "Network: http://192.168.x.x:5050/"
3. Usa esa URL en tu otro dispositivo

**Ejemplo**:
```
➜  Network: http://192.168.1.100:5050/
```
Abre `http://192.168.1.100:5050` en tu teléfono/tablet.

---

## 📋 Resumen de Scripts Disponibles

| Script | Puerto | Comando |
|--------|--------|---------|
| **dev** | Variable/5050 | `npm run dev` |
| **dev:5050** | 5050 | `npm run dev:5050` |
| **dev:3000** | 3000 | `npm run dev:3000` |
| **dev:8081** | 8081 | `npm run dev:8081` |
| **dev:custom** | Argumento | `npm run dev:custom 9999` |
| **start-port.sh** | Argumento/5050 | `./start-port.sh [puerto]` |

---

## ⚙️ Configuración Avanzada

### Forzar Puerto Específico (No Buscar Alternativa)
Si quieres que falle si el puerto está ocupado:

```typescript
// En vite.config.ts cambiar:
strictPort: true, // En vez de false
```

### Abrir Navegador Automáticamente
```typescript
// En vite.config.ts cambiar:
open: true, // En vez de false
```

### Solo Localhost (No Red Local)
```typescript
// En vite.config.ts cambiar:
host: 'localhost', // En vez de true
```

---

## 🎯 Recomendaciones

### Para Desarrollo Normal
```bash
./start-port.sh
# Puerto 5050, fácil de recordar
```

### Para Testing
```bash
./start-port.sh 8081
# Puerto diferente para no interferir
```

### Para Producción Local
```bash
PORT=4200 npm run dev
# Puerto más "profesional"
```

### Para Múltiples Versiones
```bash
# Versión estable en 5050
PORT=5050 npm run dev

# Versión de desarrollo en 5051
PORT=5051 npm run dev
```

---

## 📞 Ayuda Rápida

```bash
# ¿Qué puerto usar?
# → 5050 es una buena opción (ya configurado)

# ¿Cómo iniciar?
# → ./start-port.sh

# ¿Cómo cambiar puerto?
# → ./start-port.sh [numero]

# ¿Está ocupado el puerto?
# → lsof -i :5050
```

---

**Puerto por defecto**: 5050  
**Configurable**: ✅ Sí  
**Scripts disponibles**: 6 opciones  
**Estado**: ✅ Listo para usar  
**Documentación**: Este archivo

