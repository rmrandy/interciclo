# Guía de Configuración para Múltiples Instancias

Esta guía explica cómo levantar múltiples instancias del backend y frontend para simular diferentes aerolíneas y agencias de viaje.

## 📋 Requisitos

- **Backend (Java):** Al menos 1 instancia
- **Aerolíneas (Vue.js):** Al menos 2 instancias en diferentes puertos
- **Agencias de Viaje:** Al menos 3 instancias en diferentes puertos

## 🚀 Iniciar Backend

### Opción 1: Con script (Recomendado)

```bash
cd backv5

# Backend principal (puerto 8080)
./start-backend.sh 8080

# Backend adicional en otro puerto (si lo necesitas)
./start-backend.sh 9090
```

### Opción 2: Con Maven directamente

```bash
cd backv5

# Puerto 8080 (por defecto)
mvn exec:java -Dexec.mainClass="com.sources.app.App" -Dport=8080

# Puerto 9090
mvn exec:java -Dexec.mainClass="com.sources.app.App" -Dport=9090

# Cualquier otro puerto
mvn exec:java -Dexec.mainClass="com.sources.app.App" -Dport=PUERTO
```

## ✈️ Iniciar Aerolíneas (Frontend Vue.js)

Necesitas **2 aerolíneas mínimo** en puertos diferentes.

### Terminal 1 - Aerolínea 1 (Puerto 5050)
```bash
cd aerolinea
npm run dev
# Acceso: http://localhost:5050
```

### Terminal 2 - Aerolínea 2 (Puerto 5051)
```bash
cd aerolinea
PORT=5051 npm run dev
# Acceso: http://localhost:5051
```

### Terminal 3 - Aerolínea 3 (Opcional - Puerto 5052)
```bash
cd aerolinea
PORT=5052 npm run dev
# Acceso: http://localhost:5052
```

## 🏢 Iniciar Agencias de Viaje

Necesitas **3 agencias mínimo** en puertos diferentes.

### Terminal 4 - Agencia 1 (Puerto 3000)
```bash
cd agencia-viajes/agencia
npm run dev
# Acceso: http://localhost:3000
```

### Terminal 5 - Agencia 2 (Puerto 3001)
```bash
cd agencia-viajes/agencia
PORT=3001 npm run dev
# Acceso: http://localhost:3001
```

### Terminal 6 - Agencia 3 (Puerto 3002)
```bash
cd agencia-viajes/agencia
PORT=3002 npm run dev
# Acceso: http://localhost:3002
```

### Terminal 7 - Agencia 4 (Opcional - Puerto 3003)
```bash
cd agencia-viajes/agencia
PORT=3003 npm run dev
# Acceso: http://localhost:3003
```

## 🔧 Configuración de CORS

El backend **backv5** ahora está configurado con un filtro CORS robusto que:

✅ Permite solicitudes desde **cualquier origen** (cualquier puerto)  
✅ Soporta **credenciales** (cookies, autorización HTTP)  
✅ Permite **todos los métodos HTTP** comunes (GET, POST, PUT, DELETE, OPTIONS, PATCH)  
✅ Permite **headers personalizados** (Content-Type, Authorization, X-API-Key, etc.)  
✅ Maneja correctamente **solicitudes preflight** (OPTIONS)  
✅ Cachea las respuestas preflight por **1 hora** para mejor rendimiento  

## 📊 Ejemplo de Configuración Completa

### Configuración para 2 Aerolíneas y 3 Agencias

```
┌─────────────────────────────────────────────────┐
│              Backend (Java)                      │
│         http://localhost:8080/api                │
└─────────────────────────────────────────────────┘
                     ▲
                     │ (CORS habilitado)
        ┌────────────┼────────────┬────────────┐
        │            │            │            │
┌───────▼──────┐ ┌──▼──────┐ ┌──▼──────┐ ┌──▼──────┐
│ Aerolínea 1  │ │Aerolínea│ │ Agencia │ │ Agencia │
│   :5050      │ │ 2:5051  │ │  1:3000 │ │  2:3001 │
└──────────────┘ └─────────┘ └─────────┘ └─────────┘
                                           ┌─────────┐
                                           │ Agencia │
                                           │  3:3002 │
                                           └─────────┘
```

## 🌐 URLs de Acceso

### Backend
- Principal: `http://localhost:8080/api`
- Tu IP en red local: `http://172.20.10.2:8080/api` (o la IP que tengas)

### Aerolíneas
- Aerolínea 1: `http://localhost:5050`
- Aerolínea 2: `http://localhost:5051`
- Aerolínea 3: `http://localhost:5052` (opcional)

### Agencias
- Agencia 1: `http://localhost:3000`
- Agencia 2: `http://localhost:3001`
- Agencia 3: `http://localhost:3002`
- Agencia 4: `http://localhost:3003` (opcional)

## 🔍 Verificar que Todo Funciona

### 1. Verificar Backend
```bash
curl http://localhost:8080/api/health
```

### 2. Verificar CORS
```bash
curl -H "Origin: http://localhost:5050" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     -v \
     http://localhost:8080/api/airline/flights
```

Deberías ver en los headers:
- `Access-Control-Allow-Origin: http://localhost:5050`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD`
- `Access-Control-Allow-Credentials: true`

## ⚠️ Solución de Problemas

### Error: "Could not connect to the server"
- Verifica que el backend esté corriendo
- Verifica que la URL del backend en el frontend sea correcta
- Verifica que el puerto del backend esté abierto

### Error: "CORS policy"
- Asegúrate de estar usando **backv5** y no backv4
- Verifica que el filtro CORS esté compilado: `mvn clean compile`
- Reinicia el backend después de cambios

### Error: "Port already in use"
- El puerto está ocupado por otra aplicación
- Usa un puerto diferente
- Verifica qué proceso está usando el puerto: `lsof -i :PUERTO`

### Frontend no se conecta al Backend
1. Verifica la configuración de la API en el frontend
2. Abre las herramientas de desarrollo del navegador (F12)
3. Ve a la pestaña "Network" para ver las solicitudes fallidas
4. Verifica que la URL del backend sea correcta

## 📝 Notas Importantes

1. **Cada instancia necesita su propia terminal**
2. **No cierres las terminales** mientras las aplicaciones estén corriendo
3. **Los puertos deben ser únicos** para cada instancia
4. **CORS está configurado** para permitir cualquier origen
5. **Hot reload** está habilitado en las instancias de frontend (Vue.js/React)

## 🎯 Comandos Rápidos

### Detener todas las instancias
```bash
# En cada terminal, presiona:
Ctrl + C
```

### Verificar puertos en uso
```bash
# macOS/Linux
lsof -i :8080
lsof -i :5050

# Matar proceso en un puerto específico
kill -9 $(lsof -t -i:8080)
```

## 📚 Recursos Adicionales

- [Documentación de CORS](https://developer.mozilla.org/es/docs/Web/HTTP/CORS)
- [Vite Configuration](https://vitejs.dev/config/)
- [Maven Exec Plugin](https://www.mojohaus.org/exec-maven-plugin/)

