# 🚀 Inicio Rápido - Múltiples Instancias

Esta guía te ayuda a iniciar rápidamente 2 aerolíneas y 3 agencias de viaje con 1 backend.

## 📋 Resumen de Configuración

| Servicio | Puerto | URL |
|----------|--------|-----|
| **Backend** | 8080 | http://localhost:8080/api |
| **Aerolínea 1** | 5050 | http://localhost:5050 |
| **Aerolínea 2** | 5051 | http://localhost:5051 |
| **Agencia 1** | 3000 | http://localhost:3000 |
| **Agencia 2** | 3001 | http://localhost:3001 |
| **Agencia 3** | 3002 | http://localhost:3002 |

## 🎯 Pasos para Iniciar

### 1️⃣ Abrir 6 Terminales

Necesitarás 6 terminales abiertas simultáneamente:
- 1 para el Backend
- 2 para las Aerolíneas
- 3 para las Agencias

### 2️⃣ Iniciar Backend (Terminal 1)

```bash
cd backv5
./start-backend.sh 8080
```

**Espera** a ver el mensaje: `Servidor iniciado en http://[tu-ip]:8080/api`

### 3️⃣ Iniciar Aerolíneas

#### Terminal 2 - Aerolínea 1
```bash
cd aerolinea
npm run dev
```

#### Terminal 3 - Aerolínea 2
```bash
cd aerolinea
PORT=5051 npm run dev
```

### 4️⃣ Iniciar Agencias de Viaje

#### Terminal 4 - Agencia 1
```bash
cd agencia-viajes/agencia
npm run dev
```

#### Terminal 5 - Agencia 2
```bash
cd agencia-viajes/agencia
PORT=3001 npm run dev
```

#### Terminal 6 - Agencia 3
```bash
cd agencia-viajes/agencia
PORT=3002 npm run dev
```

## ✅ Verificación

Una vez iniciados todos los servicios, abre tu navegador y verifica:

1. **Backend:** http://localhost:8080/api/health
2. **Aerolínea 1:** http://localhost:5050
3. **Aerolínea 2:** http://localhost:5051
4. **Agencia 1:** http://localhost:3000
5. **Agencia 2:** http://localhost:3001
6. **Agencia 3:** http://localhost:3002

## 🔄 Estructura de Directorios

```
ensurancePharmacy/
├── backv5/              ← Backend Java (Puerto 8080)
├── aerolinea/           ← Frontend Aerolínea (Puertos 5050, 5051)
└── agencia-viajes/
    └── agencia/         ← Frontend Agencia (Puertos 3000, 3001, 3002)
```

## 🛑 Detener Todos los Servicios

En cada terminal, presiona: `Ctrl + C`

## ⚡ Tips

- **CORS ya está configurado** en backv5 para aceptar cualquier puerto
- **No cierres ninguna terminal** mientras las aplicaciones estén corriendo
- **Hot reload está activo** - los cambios en el código se reflejarán automáticamente
- Si un puerto está ocupado, usa otro diferente

## ❓ Problemas Comunes

### "Port already in use"
```bash
# Verifica qué está usando el puerto
lsof -i :8080

# Si necesitas liberar el puerto
kill -9 $(lsof -t -i:8080)
```

### "Could not connect to the server"
- Verifica que el backend esté corriendo
- Verifica que la URL sea correcta en el frontend

### "CORS Error"
- Asegúrate de usar **backv5** (no backv4)
- Reinicia el backend: `Ctrl+C` y vuelve a ejecutar

## 📚 Documentación Completa

Para más detalles, consulta: `backv5/GUIA_PUERTOS_MULTIPLES.md`


