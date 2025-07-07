# 📋 API Overview - Sistema Pharmacy

## 🏥 Descripción General

El sistema Pharmacy es una aplicación web completa para la gestión de una farmacia moderna, desarrollada en Vue.js 3. El sistema integra múltiples funcionalidades que van desde la venta de medicamentos hasta el procesamiento de recetas médicas con validación de seguros.

## 🔧 Arquitectura del Sistema

### Frontend (Vue.js 3)
- **Framework**: Vue.js 3 con Composition API
- **Estado**: Pinia para gestión de estado global
- **Routing**: Vue Router para navegación
- **UI**: Tailwind CSS + FontAwesome
- **HTTP**: Axios para comunicación con APIs

### Backend Integration
- **Pharmacy API** (Puerto 8080): Gestión de productos, inventario, pedidos
- **Ensurance API** (Puerto 8082): Verificación de recetas y cobertura de seguros

## 🚀 Funcionalidades Principales

### 1. Gestión de Productos (Catálogo)

#### Características:
- **Catálogo dinámico** con filtros avanzados
- **Búsqueda por principio activo**, marca, precio
- **Filtros de precio** (mínimo/máximo)
- **Sistema de calificaciones** y comentarios
- **Gestión de stock** en tiempo real

#### Endpoints Principales:
```javascript
// Obtener todos los medicamentos
GET /api2/medicines

// Obtener medicamento específico
GET /api2/medicines/{id}

// Buscar por principio activo
GET /api2/medicines/search?activeMedicament={nombre}

// Obtener categorías
GET /api2/medicines/actives
```

### 2. Sistema de Carrito de Compras

#### Características:
- **Gestión de cantidades** dinámica
- **Cálculo automático** de totales
- **Persistencia** en base de datos
- **Validación de stock** antes de agregar

#### Flujo de Compra:
1. Usuario agrega productos al carrito
2. Sistema valida disponibilidad de stock
3. Usuario procede al checkout
4. Validación de datos de pago
5. Procesamiento de la orden
6. Actualización de inventario

### 3. Procesamiento de Recetas Médicas

#### Características:
- **Verificación con seguros** mediante códigos de aprobación
- **Cálculo automático** de dosis basado en prescripción
- **Validación de cobertura** de medicamentos
- **Generación de recibos** oficiales
- **Integración completa** con sistema de seguros

#### Flujo de Receta:
1. Usuario ingresa código de aprobación
2. Sistema verifica con API de seguros
3. Se valida cobertura y monto aprobado
4. Se calculan cantidades basadas en dosis
5. Se verifica disponibilidad en inventario
6. Se procesa la dispensación
7. Se marca como completada en sistema de seguros

### 4. Panel Administrativo

#### Funcionalidades:
- **Dashboard** con estadísticas de ventas
- **Gestión CRUD** de productos
- **Control de inventario** con alertas
- **Gestión de usuarios** y roles
- **Reportes** de ventas y recetas
- **Configuración** de aseguradoras

### 5. Sistema de Autenticación

#### Características:
- **Registro** de usuarios
- **Login/Logout** con persistencia
- **Gestión de perfiles** de usuario
- **Roles diferenciados** (cliente, admin, empleado)

## 📊 Estructura de Datos

### Producto (Medicine)
```javascript
{
  idMedicine: string,
  name: string,
  activeMedicament: string,
  concentration: string,
  presentacion: string,
  price: number,
  stock: number,
  image: string,
  averageRating: number,
  ratingCount: number
}
```

### Receta (Prescription)
```javascript
{
  id: {
    prescriptionId: string
  },
  prescription: {
    idPrescription: string,
    approved: boolean,
    hospital: Object,
    user: Object
  },
  medicines: [
    {
      idMedicine: string,
      name: string,
      dosis: number,
      frecuencia: number,
      duracion: number
    }
  ]
}
```

### Orden (Order)
```javascript
{
  idOrder: string,
  user: Object,
  status: string, // 'recibido', 'Completado', 'cancelado'
  orderDate: Date,
  total: number
}
```

## 🔌 Integración con APIs

### Pharmacy API (Puerto 8080)
```javascript
// Configuración dinámica de puertos
const pharmacyUrl = `http://${host}:${port}/api2/`;

// Endpoints principales
- GET /medicines - Listar productos
- POST /orders - Crear orden
- POST /order_medicines - Agregar productos a orden
- PUT /orders/{id} - Actualizar orden
- DELETE /medicines/{id} - Eliminar producto
```

### Ensurance API (Puerto 8082)
```javascript
// Verificación de recetas
- GET /service-approvals/check/{code} - Verificar receta
- PUT /service-approvals/complete/{code} - Marcar como completada
- GET /medication-coverage/check - Verificar cobertura
```

## 🛠️ Servicios Principales

### ApiService
- **Configuración dinámica** de puertos
- **Gestión de URLs** para ambas APIs
- **Persistencia** de configuración en localStorage
- **Funciones helper** para construcción de URLs

### PrescriptionService
- **Verificación** de recetas con seguros
- **Procesamiento** completo de dispensación
- **Control de inventario** automático
- **Generación de recibos**
- **Integración** con sistema de seguros

### AuthService
- **Gestión de autenticación** de usuarios
- **Persistencia** de sesiones
- **Validación** de tokens
- **Gestión de roles** y permisos

## 📱 Componentes Principales

### Páginas de Usuario
- **Home.vue** - Página principal con ofertas
- **Catalogo.vue** - Catálogo de productos con filtros
- **ProductoDetalle.vue** - Detalle completo de producto
- **Cart.vue** - Carrito de compras
- **Prescriptions.vue** - Lista de recetas del usuario
- **PrescriptionPay.vue** - Procesamiento de recetas

### Páginas Administrativas
- **AdminDash.vue** - Dashboard administrativo
- **GestionProductos.vue** - CRUD de productos
- **admin/Pedidos.vue** - Gestión de pedidos

### Componentes Reutilizables
- **Header.vue** - Navegación principal
- **Footer.vue** - Pie de página
- **ProductRating.vue** - Sistema de calificaciones
- **Comentarios.vue** - Sistema de comentarios
- **PortSelector.vue** - Selector de puertos

## 🔒 Seguridad y Validaciones

### Validaciones de Frontend
- **Formularios** con validación en tiempo real
- **Verificación de stock** antes de agregar al carrito
- **Validación de tarjetas** de crédito
- **Sanitización** de datos de entrada

### Integridad de Datos
- **Verificación de recetas** con sistema de seguros
- **Control de inventario** en tiempo real
- **Validación de cobertura** de medicamentos
- **Persistencia** de configuraciones críticas

## 📈 Métricas y Analytics

### Dashboard Administrativo
- **Ventas diarias/mensuales**
- **Productos más vendidos**
- **Recetas procesadas**
- **Stock bajo** (alertas)
- **Usuarios activos**

### Reportes Disponibles
- **Reporte de ventas** por período
- **Análisis de inventario**
- **Estadísticas de recetas**
- **Métricas de usuarios**

## 🚀 Deployment y Configuración

### Variables de Entorno
```bash
VUE_APP_API_HOST=localhost
VUE_APP_IP=localhost
```

### Scripts de Desarrollo
```bash
npm run serve      # Desarrollo
npm run build      # Producción
npm run docs       # Generar documentación
npm run docs:serve # Servir documentación
```

### Configuración de Puertos
- **Desarrollo**: Pharmacy (8080), Ensurance (8082)
- **Configuración dinámica** desde interfaz de usuario
- **Persistencia** en localStorage

## 🔮 Roadmap y Mejoras Futuras

### Funcionalidades Planificadas
- **Sistema de notificaciones** push
- **Integración con WhatsApp** para confirmaciones
- **App móvil** nativa
- **Sistema de fidelización** de clientes
- **Analytics avanzados** con machine learning
- **Integración con más aseguradoras**

### Mejoras Técnicas
- **PWA** (Progressive Web App)
- **Offline mode** para consultas básicas
- **Caching inteligente** de productos
- **Optimización de rendimiento**
- **Tests automatizados** completos

---

**Documentación generada automáticamente con JSDoc**  
**Última actualización**: Diciembre 2024  
**Versión**: 1.0.0 