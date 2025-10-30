# 🔧 Solución de Errores CORS

## ❌ Problema Original

El error **"Access-Control-Allow-Origin cannot contain more than one origin"** ocurría porque:

1. El `CorsFilter` establecía headers CORS
2. Los handlers individuales también establecían headers CORS
3. Resultado: Headers duplicados que causaban el error

## ✅ Solución Implementada

He modificado el `CorsFilter.java` para que:

1. **Ejecute primero los handlers** (que pueden añadir sus propios headers)
2. **Limpie todos los headers CORS duplicados**
3. **Establezca los headers CORS correctos una sola vez**

### Flujo Corregido

```
Cliente → Request
    ↓
CorsFilter recibe la request
    ↓
¿Es OPTIONS? → Sí → Establece CORS y responde 204
    ↓
   No
    ↓
Ejecuta handler (puede añadir headers CORS)
    ↓
CorsFilter limpia headers duplicados
    ↓
CorsFilter establece headers CORS finales
    ↓
Response → Cliente
```

## 🚀 Pasos para Aplicar la Solución

### 1. Recompilar el Backend

```bash
cd backv5
mvn clean compile
```

✅ **Ya compilado exitosamente**

### 2. Reiniciar el Backend

Si tienes el backend corriendo, **detenlo** (Ctrl+C) y reinícialo:

```bash
cd backv5
./start-backend.sh 8080
```

O con Maven directamente:

```bash
cd backv5
mvn exec:java -Dexec.mainClass="com.sources.app.App" -Dport=8080
```

### 3. Limpiar Cache del Navegador

**MUY IMPORTANTE:** El navegador puede tener cacheados los headers CORS antiguos.

#### Opción A: Limpiar LocalStorage (Recomendado)

En la consola del navegador (F12), ejecuta:

```javascript
// Limpiar configuración de API guardada
localStorage.removeItem('api_ip');
localStorage.removeItem('api_port');

// Ver qué hay guardado
console.log('LocalStorage actual:', localStorage);

// Recargar la página
location.reload();
```

#### Opción B: Hard Refresh

- **Chrome/Edge:** `Ctrl + Shift + R` (Windows/Linux) o `Cmd + Shift + R` (Mac)
- **Firefox:** `Ctrl + F5` (Windows/Linux) o `Cmd + Shift + R` (Mac)
- **Safari:** `Cmd + Option + R`

#### Opción C: Limpiar Cache Completo

1. Abre DevTools (F12)
2. Ve a **Application** (Chrome) o **Storage** (Firefox)
3. Click derecho en el dominio
4. Selecciona **Clear site data** o **Eliminar todos los datos**

### 4. Verificar la Configuración del Frontend

El error también mostraba intentos de conectarse al puerto **2020**, que no está en el código.

#### Verificar localStorage

En la consola del navegador (F12):

```javascript
// Ver configuración actual
console.log('API IP:', localStorage.getItem('api_ip'));
console.log('API Port:', localStorage.getItem('api_port'));

// Si está mal, corregir:
localStorage.setItem('api_ip', '172.20.10.2');  // Tu IP de red
localStorage.setItem('api_port', '8080');       // Puerto correcto

// Recargar
location.reload();
```

## 🧪 Verificar que Funciona

### 1. Verificar Backend

```bash
# Debe responder sin error
curl http://172.20.10.2:8080/api/health
```

### 2. Verificar CORS desde el Frontend

En la consola del navegador (F12):

```javascript
// Probar solicitud con CORS
fetch('http://172.20.10.2:8080/api/airline/cities', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
})
  .then(res => res.json())
  .then(data => console.log('✅ CORS funcionando:', data))
  .catch(err => console.error('❌ Error:', err));
```

### 3. Verificar Headers CORS

En DevTools (F12):
1. Ve a la pestaña **Network**
2. Recarga la página
3. Busca una solicitud a `/api/airline/`
4. Click en la solicitud
5. Ve a **Response Headers**
6. Verifica que haya **UN SOLO** `Access-Control-Allow-Origin`

**Headers esperados:**

```
Access-Control-Allow-Origin: http://localhost:5050
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, X-API-Key, ...
Access-Control-Max-Age: 3600
```

## 🔍 Diagnóstico de Errores Comunes

### Error: "Could not connect to the server"

**Causa:** El backend no está corriendo o la IP/puerto son incorrectos

**Solución:**
1. Verifica que el backend esté corriendo: `lsof -i :8080`
2. Verifica la IP correcta: `ifconfig` o `ipconfig`
3. Actualiza localStorage con la IP correcta

### Error: "Access-Control-Allow-Origin cannot contain more than one origin"

**Causa:** Headers CORS duplicados (ya solucionado)

**Solución:**
1. Asegúrate de usar **backv5** (no backv4)
2. Recompila: `mvn clean compile`
3. Reinicia el backend
4. Limpia cache del navegador

### Error: Puerto 2020 o puerto incorrecto

**Causa:** Configuración guardada en localStorage

**Solución:**
```javascript
// En consola del navegador
localStorage.setItem('api_port', '8080');
localStorage.setItem('api_ip', '172.20.10.2'); // Tu IP
location.reload();
```

### Preflight OPTIONS falla

**Causa:** El filtro CORS no está manejando OPTIONS correctamente

**Solución:**
- El CorsFilter ahora maneja OPTIONS automáticamente
- Retorna 204 No Content
- No debería haber problema

## 📊 Configuración Final Recomendada

### Backend (backv5)

| Componente | Puerto |
|------------|--------|
| Backend Principal | 8080 |

```bash
cd backv5
./start-backend.sh 8080
```

### Frontends

| Aplicación | Puerto | Comando |
|------------|--------|---------|
| Aerolínea 1 | 5050 | `PORT=5050 npm run dev` |
| Aerolínea 2 | 5051 | `PORT=5051 npm run dev` |
| Agencia 1 | 3000 | `npm run dev` |
| Agencia 2 | 3001 | `PORT=3001 npm run dev` |
| Agencia 3 | 3002 | `PORT=3002 npm run dev` |

### LocalStorage Correcto

```javascript
localStorage.setItem('api_ip', '172.20.10.2');    // Tu IP de red
localStorage.setItem('api_port', '8080');         // Puerto del backend
```

## 🎯 Checklist de Verificación

- [ ] Backend backv5 compilado: `mvn clean compile`
- [ ] Backend corriendo en puerto 8080
- [ ] LocalStorage limpio o con valores correctos
- [ ] Cache del navegador limpio
- [ ] Frontend recargado con Hard Refresh
- [ ] Headers CORS sin duplicados en Network tab
- [ ] No hay errores CORS en la consola

## 📝 Notas Importantes

1. **Usa backv5, no backv4** - Solo backv5 tiene el CorsFilter corregido
2. **Limpia localStorage** - Configuraciones viejas pueden causar problemas
3. **Hard Refresh** - El navegador cachea headers CORS agresivamente
4. **Un solo origen por header** - El nuevo CorsFilter garantiza esto
5. **Puerto correcto** - Verifica que todos apunten a 8080

## 🆘 Si Nada Funciona

1. **Detener todo:**
   ```bash
   # Matar proceso en puerto 8080
   kill -9 $(lsof -t -i:8080)
   ```

2. **Limpiar completamente:**
   ```bash
   cd backv5
   mvn clean
   rm -rf target/
   ```

3. **Recompilar desde cero:**
   ```bash
   mvn clean compile
   ```

4. **Reiniciar backend:**
   ```bash
   ./start-backend.sh 8080
   ```

5. **Limpiar navegador completamente:**
   - Cerrar todos los tabs
   - Limpiar cache y localStorage
   - Abrir nuevo tab privado/incógnito
   - Cargar el frontend

## 📚 Archivos Modificados

- ✅ `backv5/src/main/java/com/sources/app/util/CorsFilter.java` - Corregido
- ✅ `backv5/src/main/java/com/sources/app/App.java` - Actualizado para usar filtro
- ✅ Compilación exitosa

## 🎉 Resultado Esperado

Después de aplicar estas soluciones:

✅ No más errores de CORS  
✅ Headers CORS únicos y correctos  
✅ Múltiples frontends funcionando simultáneamente  
✅ Conexión estable con el backend  
✅ Sin problemas de preflight OPTIONS  


