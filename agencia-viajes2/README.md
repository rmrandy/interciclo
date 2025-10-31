# 🛩️ Agencia de Viajes - Sistema Completo

Sistema completo de agencia de viajes desarrollado con **React + TypeScript** en el frontend y **Node.js + Express + MongoDB** en el backend.

## 🚀 Características Principales

### Frontend (React + TypeScript)
- ✅ **React 18** con TypeScript
- ✅ **Tailwind CSS** para estilos
- ✅ **React Router** para navegación
- ✅ **Context API** para manejo de estado
- ✅ **Axios** para peticiones HTTP
- ✅ **Componentes reutilizables**
- ✅ **Diseño responsivo**

### Backend (Node.js + Express + MongoDB)
- ✅ **Node.js** con TypeScript
- ✅ **Express.js** framework web
- ✅ **MongoDB Atlas** base de datos
- ✅ **Mongoose** ODM
- ✅ **JWT** autenticación
- ✅ **Validación** con express-validator
- ✅ **Middleware** de seguridad
- ✅ **Manejo de errores** centralizado

## 📋 Requerimientos Implementados

### i. Páginas Informativas
- ✅ **Sobre Nosotros** (`/about`) - Historia, misión, visión y equipo
- ✅ **Servicios** (`/services`) - Catálogo completo de servicios
- ✅ **Contacto** (`/contact`) - Formulario de contacto e información
- ✅ **Navegación** integrada en el menú principal

### ii. Perfiles de Usuario
- ✅ **Información Personal** - Edición de datos del usuario
- ✅ **Seguridad** - Cambio de contraseñas
- ✅ **Mis Reservas** - Gestión de reservas del usuario
- ✅ **Preferencias** - Configuración de notificaciones y preferencias
- ✅ **Roles** - Sistema de roles (usuario, admin, empleado)

### iii. Búsquedas (Sin vuelos a escala)
- ✅ **Búsqueda Avanzada** - Filtros específicos para vuelos directos
- ✅ **Solo Vuelos Directos** - Opción para excluir escalas
- ✅ **Fechas Flexibles** - Búsqueda con rangos de fechas
- ✅ **Filtros Adicionales** - Pasajeros, clase, aeropuertos
- ✅ **Interfaz Optimizada** - Diseño específico para vuelos directos

## 📁 Estructura del Proyecto

```
agencia-viajes/
├── backend/                 # Servidor Node.js
│   ├── src/
│   │   ├── controllers/     # Controladores de rutas
│   │   ├── models/          # Modelos de MongoDB
│   │   ├── routes/          # Definición de rutas
│   │   ├── middleware/      # Middleware personalizado
│   │   ├── utils/           # Utilidades
│   │   ├── types/           # Tipos TypeScript
│   │   ├── app.ts           # Configuración de Express
│   │   └── server.ts        # Servidor principal
│   ├── package.json
│   └── tsconfig.json
├── frontend/                # Aplicación React
│   ├── src/
│   │   ├── components/      # Componentes React
│   │   ├── pages/           # Páginas de la aplicación
│   │   ├── context/         # Context API
│   │   ├── services/        # Servicios de API
│   │   ├── types/           # Tipos TypeScript
│   │   └── utils/           # Utilidades
│   ├── package.json
│   └── tailwind.config.js
└── README.md
```

## 🛠️ Instalación y Configuración

### Prerrequisitos
- Node.js (v16 o superior)
- MongoDB Atlas (ya configurado)
- npm o yarn

### 1. Clonar el repositorio
```bash
git clone <url-del-repositorio>
cd agencia-viajes
```

### 2. Instalación Rápida (Recomendada)

```bash
# Instalar todas las dependencias
npm run install-all

# Iniciar ambos servicios (backend + frontend)
npm start
```

### 3. Instalación Manual

#### Backend
```bash
cd backend
npm install
npm run dev
```

#### Frontend (en otra terminal)
```bash
cd frontend
npm install
npm start
```

### 4. Scripts de Inicio

#### Linux/Mac
```bash
./start.sh
```

#### Windows
```cmd
start.bat
```

### 5. URLs de Acceso
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:5000
- **Health Check**: http://localhost:5000/health

## 🔧 Variables de Entorno

### Backend (.env)
```env
PORT=5000
NODE_ENV=development
MONGODB_URI=mongodb+srv://usuario:password@cluster.mongodb.net/agencia-viajes
JWT_SECRET=tu_jwt_secret_muy_seguro
JWT_EXPIRE=7d
FRONTEND_URL=http://localhost:3000
```

### Frontend (.env)
```env
REACT_APP_API_URL=http://localhost:5000/api
```

## 📊 Modelos de Base de Datos

### Usuario
- Información personal (nombre, email, teléfono)
- Autenticación (contraseña encriptada)
- Roles (usuario, admin, empleado)
- Estado de cuenta

### Aeropuerto
- Código IATA (3 letras)
- Nombre y ubicación
- Coordenadas geográficas

### Vuelo
- Número de vuelo
- Aeropuertos de origen y destino
- Fechas y horarios
- Información de la aeronave
- Inventario de asientos por categoría
- Estado del vuelo

### Reserva
- Usuario y vuelo
- Información de pasajeros
- Monto total y método de pago
- Estado de la reserva
- Referencia de reserva

## 🚀 API Endpoints

### Autenticación
- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Inicio de sesión
- `GET /api/auth/profile` - Obtener perfil
- `PUT /api/auth/profile` - Actualizar perfil

### Vuelos
- `GET /api/flights/search` - Buscar vuelos
- `GET /api/flights/:id` - Obtener vuelo por ID
- `GET /api/flights` - Listar todos los vuelos
- `POST /api/flights` - Crear vuelo (admin)
- `PUT /api/flights/:id` - Actualizar vuelo (admin)
- `DELETE /api/flights/:id` - Eliminar vuelo (admin)

### Aeropuertos
- `GET /api/airports` - Listar aeropuertos
- `GET /api/airports/search/:query` - Buscar aeropuertos
- `GET /api/airports/:id` - Obtener aeropuerto por ID

### Reservas
- `POST /api/bookings` - Crear reserva
- `GET /api/bookings` - Listar reservas del usuario
- `GET /api/bookings/:id` - Obtener reserva por ID
- `PUT /api/bookings/:id/cancel` - Cancelar reserva
- `PUT /api/bookings/:id/confirm` - Confirmar reserva

## 🎨 Características del Frontend

### Páginas Principales
- **Inicio**: Búsqueda de vuelos y características
- **Login**: Inicio de sesión
- **Registro**: Creación de cuenta
- **Vuelos**: Lista de vuelos disponibles
- **Reservas**: Gestión de reservas del usuario

### Componentes
- **FlightSearch**: Formulario de búsqueda
- **FlightCard**: Tarjeta de vuelo
- **Navbar**: Navegación principal
- **AuthContext**: Contexto de autenticación

### Funcionalidades
- Búsqueda de vuelos por origen, destino y fecha
- Autenticación de usuarios
- Gestión de perfil
- Reservas de vuelos
- Diseño responsivo
- Validación de formularios

## 🔒 Seguridad

- Contraseñas encriptadas con bcrypt
- Autenticación JWT
- Validación de entrada
- Middleware de seguridad (helmet)
- CORS configurado
- Manejo de errores centralizado

## 🚀 Despliegue

### Backend
- Desplegar en Heroku, Railway, o Vercel
- Configurar MongoDB Atlas
- Variables de entorno en producción

### Frontend
- Desplegar en Vercel, Netlify, o GitHub Pages
- Configurar variables de entorno
- Build de producción

## 📝 Scripts Disponibles

### Backend
```bash
npm run dev      # Modo desarrollo
npm run build    # Compilar TypeScript
npm start        # Modo producción
npm run clean    # Limpiar dist
```

### Frontend
```bash
npm start        # Modo desarrollo
npm run build    # Compilar para producción
npm test         # Ejecutar tests
```

## 🤝 Contribución

1. Fork el proyecto
2. Crear una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abrir un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

## 👥 Autores

- **Desarrollador**: Tu Nombre
- **Email**: tu@email.com

## 🙏 Agradecimientos

- React Team
- Express.js Team
- MongoDB Team
- Tailwind CSS Team
