# ⚙️ Configuración de Puertos - Frontend Aerolínea

## ✅ Cambios Realizados

### 1. vite.config.ts - Actualizado ✅
```typescript
server: {
  port: parseInt(process.env.PORT || '5050'), // Puerto por defecto 5050
  host: true,
  strictPort: false,
  open: false,
}
```

### 2. package.json - Scripts Nuevos ✅
```json
"scripts": {
  "dev": "python3 getip.py && vite --host",
  "dev:5050": "PORT=5050 python3 getip.py && vite --host",  // NUEVO
  "dev:3000": "PORT=3000 python3 getip.py && vite --host",  // NUEVO
  "dev:8081": "PORT=8081 python3 getip.py && vite --host",  // NUEVO
  "dev:custom": "python3 getip.py && vite --host --port",   // NUEVO
}
```

### 3. Scripts Creados ✅
- **start-port.sh** - Script con parámetro de puerto
- **start.sh** - Script simple para puerto 5050
- **README_PUERTOS.md** - Documentación completa
- **COMO_INICIAR.md** - Guía de uso
- **CONFIGURACION_PUERTO.md** - Este archivo

---

## 🚀 Formas de Iniciar (Elige la que Prefieras)

### Opción 1: Script Simple (Más Fácil)
```bash
cd aerolinea
./start.sh
```
**Puerto**: 5050 (fijo)

### Opción 2: Script con Parámetro (Más Flexible)
```bash
cd aerolinea
./start-port.sh          # Puerto 5050 por defecto
./start-port.sh 3000     # Puerto 3000
./start-port.sh 8081     # Puerto 8081
./start-port.sh 4200     # Cualquier puerto
```
**Puerto**: El que tú elijas

### Opción 3: npm Scripts (Más Estándar)
```bash
cd aerolinea
npm run dev:5050    # Puerto 5050
npm run dev:3000    # Puerto 3000
npm run dev:8081    # Puerto 8081
```
**Puerto**: Predefinido

### Opción 4: Variable de Entorno (Más Control)
```bash
cd aerolinea
PORT=5050 npm run dev    # Puerto 5050
PORT=7777 npm run dev    # Puerto 7777
```
**Puerto**: Variable PORT

### Opción 5: Vite Directo (Más Avanzado)
```bash
cd aerolinea
npx vite --host --port 5050
npx vite --host --port 3000
```
**Puerto**: Argumento --port

---

## 📝 Ejemplos de Uso Real

### Caso 1: Desarrollo Normal
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/aerolinea
./start.sh
```
Abre: `http://localhost:5050`

### Caso 2: Testing en Puerto Diferente
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/aerolinea
./start-port.sh 8888
```
Abre: `http://localhost:8888`

### Caso 3: Desarrollo con Backend
```bash
# Terminal 1 - Backend
cd backv4
./run_app_server.sh    # Puerto 2020

# Terminal 2 - Frontend
cd aerolinea
./start.sh             # Puerto 5050
```
- Backend: `http://localhost:2020/api`
- Frontend: `http://localhost:5050`

---

## 🎯 Puerto Recomendado

### ✅ Puerto 5050
**Ventajas:**
- No conflictúa con servicios comunes
- Fácil de recordar
- Ya configurado como defecto
- Diferente del backend (2020)

**Usar:**
```bash
./start.sh
```

### Otros Puertos Comunes

| Puerto | Uso Típico | Comando |
|--------|------------|---------|
| **3000** | React/Vue/Node | `./start-port.sh 3000` |
| **4200** | Angular | `./start-port.sh 4200` |
| **5050** | Este proyecto ✅ | `./start.sh` |
| **2020** | Backend (ocupado) | ❌ No usar |
| **8081** | Testing | `./start-port.sh 8081` |

---

## 🔧 Configuración Detallada

### Estructura de Configuración

```
aerolinea/
├── vite.config.ts              ← Configuración de puerto aquí
├── package.json                ← Scripts npm aquí
├── start.sh                    ← Script simple (puerto 5050)
├── start-port.sh               ← Script con parámetro
├── README_PUERTOS.md           ← Guía completa de puertos
├── COMO_INICIAR.md             ← Guía de inicio
└── CONFIGURACION_PUERTO.md     ← Este archivo
```

### vite.config.ts Explicado
```typescript
export default defineConfig({
  plugins: [vue()],
  server: {
    // Puerto: Lee variable PORT, si no existe usa 5050
    port: parseInt(process.env.PORT || '5050'),
    
    // Host true = accesible desde red local (0.0.0.0)
    host: true,
    
    // strictPort false = si está ocupado, busca otro
    strictPort: false,
    
    // open false = no abre navegador automáticamente
    open: false,
  },
});
```

---

## 💡 Tips y Trucos

### Tip 1: Ver Puerto Actual
```bash
# Al iniciar, verás:
➜  Local:   http://localhost:5050/
➜  Network: http://192.168.1.100:5050/
```

### Tip 2: Puerto Ocupado
Si el puerto está ocupado:
```bash
# Ver qué proceso lo usa
lsof -i :5050

# Matar el proceso
kill -9 [PID]

# O simplemente usa otro puerto
./start-port.sh 5051
```

### Tip 3: Guardar Configuración Personalizada
```bash
# Crear archivo .env en la raíz del proyecto
echo "PORT=5050" > .env

# Luego solo ejecuta
npm run dev
```

### Tip 4: Múltiples Desarrolladores
```bash
# Cada desarrollador puede usar su puerto
# Desarrollador 1
PORT=5050 npm run dev

# Desarrollador 2
PORT=5051 npm run dev
```

---

## 🔒 Puertos Reservados/Bloqueados

Evita estos puertos (ya en uso común):

| Puerto | Servicio | Estado |
|--------|----------|--------|
| 80 | HTTP | ❌ Requiere sudo |
| 443 | HTTPS | ❌ Requiere sudo |
| 3306 | MySQL | ❌ Probablemente ocupado |
| 5432 | PostgreSQL | ❌ Probablemente ocupado |
| 2020 | Backend Java | ❌ Ocupado por backv4 |

---

## ✅ Puertos Seguros para Usar

| Puerto | Estado | Recomendación |
|--------|--------|---------------|
| **5050** | ✅ Libre | **Recomendado** (por defecto) |
| 3000 | ✅ Libre | Buena alternativa |
| 4200 | ✅ Libre | Común para Angular |
| 5173 | ✅ Libre | Puerto default de Vite |
| 8081 | ✅ Libre | Bueno para testing |

---

## 🚨 Solución de Problemas

### Problema: "EADDRINUSE: address already in use"
```bash
# Solución 1: Usar otro puerto
./start-port.sh 5051

# Solución 2: Matar proceso en ese puerto
lsof -i :5050
kill -9 [PID]
```

### Problema: "Permission denied: ./start.sh"
```bash
# Dar permisos
chmod +x start.sh
chmod +x start-port.sh
```

### Problema: "python3: command not found"
```bash
# Iniciar sin getip (sin mostrar IP)
PORT=5050 npx vite --host
```

### Problema: "npm: command not found"
```bash
# Instalar Node.js y npm primero
brew install node  # macOS

# Verificar instalación
node --version
npm --version
```

---

## 📊 Comparación de Métodos

| Método | Facilidad | Flexibilidad | Velocidad |
|--------|-----------|--------------|-----------|
| `./start.sh` | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| `./start-port.sh [puerto]` | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| `npm run dev:5050` | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| `PORT=X npm run dev` | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| `npx vite --port X` | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

**Recomendado**: `./start.sh` para desarrollo normal

---

## 🎓 Resumen Ejecutivo

### Configuración Actual
- ✅ **Puerto por defecto**: 5050
- ✅ **Configurable**: Sí, múltiples formas
- ✅ **Scripts disponibles**: 7 opciones
- ✅ **Documentación**: 3 guías completas

### Formas de Iniciar
1. `./start.sh` - Más simple (puerto 5050)
2. `./start-port.sh [puerto]` - Con parámetro
3. `npm run dev:5050` - Script npm
4. `PORT=5050 npm run dev` - Variable de entorno

### Archivos Modificados
- ✅ `vite.config.ts` - Configuración de servidor
- ✅ `package.json` - Scripts npm

### Archivos Creados
- ✅ `start.sh` - Script simple
- ✅ `start-port.sh` - Script con parámetro
- ✅ `README_PUERTOS.md` - Guía completa
- ✅ `COMO_INICIAR.md` - Guía de uso
- ✅ `CONFIGURACION_PUERTO.md` - Este archivo

---

**Estado**: ✅ Completado  
**Puerto**: 5050 (configurable)  
**Formas de iniciar**: 5 opciones  
**Documentación**: 3 guías

## 🎉 ¡Listo para Usar!

