# 🌐 Configuración de Red - Agencia de Viajes

## Problema Solucionado
Se han resuelto los errores de CORS que impedían la comunicación entre el frontend y backend cuando se accede desde la IP de red en lugar de localhost.

## 🔧 Cambios Realizados

### 1. Archivos de Configuración (.env)
- **Frontend**: `frontend/.env` - Configurado para usar IP de red
- **Backend**: `backend/.env` - Configurado para aceptar conexiones desde IP de red

### 2. Configuración CORS Mejorada
- El backend ahora acepta conexiones desde múltiples orígenes
- Configuración flexible para desarrollo y producción
- Soporte para localhost y IP de red

### 3. Servidor Backend
- Configurado para escuchar en todas las interfaces de red (`0.0.0.0`)
- Logs mejorados que muestran las URLs de acceso

## 🚀 Cómo Usar

### Opción 1: Script Automático (Recomendado)
```bash
./start-network.sh
```

### Opción 2: Manual
1. **Iniciar Backend:**
   ```bash
   cd backend
   npm start
   ```

2. **Iniciar Frontend:**
   ```bash
   cd frontend
   npm start
   ```

### Opción 3: Usar el script de inicio existente
```bash
./start.sh
```

## 🧪 Probar Conectividad
```bash
./test-connection.sh
```

## 📱 URLs de Acceso
- **Frontend**: http://192.168.0.26:3000
- **Backend API**: http://192.168.0.26:5000/api
- **Health Check**: http://192.168.0.26:5000/health

## 🔍 Solución de Problemas

### Si sigues viendo errores de CORS:
1. Verifica que ambos servicios estén ejecutándose
2. Revisa que la IP en los archivos .env sea correcta
3. Ejecuta `./test-connection.sh` para diagnosticar

### Si la IP de red cambia:
1. Ejecuta `./start-network.sh` para reconfigurar automáticamente
2. O actualiza manualmente los archivos .env

## 📋 Variables de Entorno

### Frontend (.env)
```
REACT_APP_API_URL=http://192.168.0.26:5000/api
REACT_APP_FRONTEND_URL=http://192.168.0.26:3000
GENERATE_SOURCEMAP=false
```

### Backend (.env)
```
PORT=5000
NODE_ENV=development
MONGODB_URI=mongodb://localhost:27017/agencia-viajes
JWT_SECRET=tu_jwt_secret_muy_seguro_aqui_para_agencia_viajes_2024
JWT_EXPIRE=7d
FRONTEND_URL=http://192.168.0.26:3000
```

## ✅ Verificación
- ✅ CORS configurado correctamente
- ✅ Backend escucha en todas las interfaces
- ✅ Frontend configurado para usar IP de red
- ✅ Scripts de inicio y prueba creados
- ✅ Documentación actualizada

