# 🌐 Guía de Puertos - Frontend Aerolínea

## 🚀 Formas de Iniciar en Diferentes Puertos

### Opción 1: Script con Puerto Personalizado (Recomendado)

```bash
# Puerto por defecto (5050)
./start-port.sh

# Puerto personalizado
./start-port.sh 3000
./start-port.sh 8081
./start-port.sh 4200
```

### Opción 2: Scripts npm Predefinidos

```bash
# Puerto 5050 (por defecto)
npm run dev:5050

# Puerto 3000
npm run dev:3000

# Puerto 8081
npm run dev:8081

# Puerto original con getip
npm run dev
```

### Opción 3: Variable de Entorno

```bash
# Especificar puerto con variable de entorno
PORT=5050 npm run dev

# Cualquier puerto que quieras
PORT=9090 npm run dev
PORT=4000 npm run dev
```

### Opción 4: Comando Directo de Vite

```bash
# Puerto específico directamente
npm run dev:custom 5050

# O con vite directamente
npx vite --host --port 5050
```

---

## 📋 Puertos Comunes

| Puerto | Uso Sugerido | Comando |
|--------|--------------|---------|
| **5050** | Puerto por defecto aerolínea | `./start-port.sh` o `npm run dev:5050` |
| **3000** | Desarrollo alternativo | `./start-port.sh 3000` o `npm run dev:3000` |
| **8081** | Testing | `./start-port.sh 8081` o `npm run dev:8081` |
| **4200** | Producción local | `./start-port.sh 4200` |

---

## ⚙️ Configuración Actual

El archivo `vite.config.ts` está configurado con:

```typescript
server: {
  port: parseInt(process.env.PORT || '5050'), // Puerto por defecto 5050
  host: true, // Permite acceso desde la red local
  strictPort: false, // Si está ocupado, busca otro disponible
  open: false, // No abre el navegador automáticamente
}
```

**Características:**
- ✅ Puerto por defecto: **5050**
- ✅ Configurable con variable de entorno `PORT`
- ✅ Si el puerto está ocupado, busca uno disponible
- ✅ Accesible desde la red local
- ✅ No abre el navegador automáticamente

---

## 🔧 Cambiar Puerto Por Defecto

Si quieres cambiar el puerto por defecto permanentemente:

### Opción 1: Editar vite.config.ts
```typescript
server: {
  port: 3000, // Cambia 5050 por el puerto que prefieras
  host: true,
  strictPort: false,
  open: false,
}
```

### Opción 2: Crear archivo .env
```bash
# Crear archivo .env en la raíz del proyecto
echo "PORT=5050" > .env
```

---

## 📡 Verificar en Qué Puerto Está Corriendo

Cuando inicies el servidor, verás algo como:

```
🚀 Ensurance Pharmacy - Frontend Aerolínea
===========================================

✅ Usando puerto personalizado: 5050

📡 Obteniendo dirección IP...
[IP info...]

🌐 Iniciando servidor de desarrollo en puerto 5050...

  VITE v4.5.14  ready in 523 ms

  ➜  Local:   http://localhost:5050/
  ➜  Network: http://192.168.1.100:5050/
```

---

## 🛠️ Ejemplos de Uso

### Desarrollo Normal
```bash
cd /Users/randyrivera/Documents/Farmacia/ensurancePharmacy/aerolinea

# Puerto 5050 (por defecto)
./start-port.sh
```

### Testing con Puerto Diferente
```bash
# Para testing en puerto 8081
./start-port.sh 8081
```

### Múltiples Instancias
```bash
# Terminal 1 - Puerto 5050
PORT=5050 npm run dev

# Terminal 2 - Puerto 5051
PORT=5051 npm run dev
```

---

## 🔒 Puertos Ocupados

Si el puerto está ocupado:

### Ver qué está usando el puerto
```bash
# macOS/Linux
lsof -i :5050

# Matar el proceso si es necesario
kill -9 [PID]
```

### El sistema buscará otro puerto
Si `strictPort: false` (configuración actual), Vite automáticamente usará el siguiente puerto disponible.

---

## 📊 Compatibilidad con Backend

Recuerda que el backend Java corre en:
- **Puerto**: 8080 (por defecto)
- **API**: http://localhost:8080/api

El frontend (puerto 5050) se comunicará con el backend (puerto 8080).

---

## 💡 Tips

1. **Puerto 5050**: Buena opción, no conflictúa con otros servicios comunes
2. **Puerto 3000**: Común para desarrollo React/Vue
3. **Puerto 8081**: Buena alternativa si 8080 está ocupado
4. **Puerto 4200**: Común para Angular (por si lo necesitas)

---

## 🆘 Solución de Problemas

### "Port already in use"
```bash
# Cambiar a otro puerto
./start-port.sh 5051
```

### "Permission denied"
```bash
# Dar permisos al script
chmod +x start-port.sh
```

### Script no funciona
```bash
# Usar comando directo
PORT=5050 npm run dev
```

---

## ✨ Resumen de Comandos

```bash
# Puerto 5050 (recomendado)
./start-port.sh

# Puerto personalizado
./start-port.sh 3000

# Con variable de entorno
PORT=4000 npm run dev

# Script npm predefinido
npm run dev:5050
```

---

**Puerto por defecto configurado**: 5050  
**Configurable**: ✅ Sí  
**Scripts disponibles**: 4 opciones  
**Estado**: ✅ Listo para usar

