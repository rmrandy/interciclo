# 🛒 Sistema de Carrito de Vuelos - AeroLinea

## Descripción

Este sistema implementa un carrito de compras completo para vuelos de aerolínea, donde los usuarios pueden:

- Agregar vuelos al carrito
- Seleccionar categorías de asientos específicas
- Tratar los asientos como inventario disponible
- Procesar el pago desde el carrito
- Gestionar múltiples vuelos antes del checkout

## 🚀 Características Principales

### 1. **Página del Carrito** (`/cart`)
- Vista completa de todos los vuelos agregados
- Selección de categorías de asientos por vuelo
- Selección de asientos específicos disponibles
- Cálculo automático de precios (tarifa + impuestos + cargos)
- Resumen del carrito con totales
- Botón para proceder al checkout

### 2. **Página de Checkout** (`/checkout`)
- Formulario de información del pasajero
- Formulario de información de pago
- Resumen del pedido con detalles
- Procesamiento de la reserva
- Confirmación de la reserva exitosa

### 3. **Integración con Vuelos**
- Botón "Agregar al Carrito" en detalles del vuelo
- Verificación de duplicados
- Navegación integrada entre páginas

### 4. **Gestión de Estado**
- Carrito persistente en localStorage
- Sincronización entre componentes
- Contador de items en navegación

## 🏗️ Arquitectura del Sistema

### Componentes Principales

```
src/
├── pages/
│   ├── cart.vue              # Página principal del carrito
│   ├── checkout.vue          # Página de pago
│   ├── flight-details.vue    # Detalles del vuelo (con botón agregar)
│   └── flights.vue           # Lista de vuelos
├── components/
│   └── CartCounter.vue       # Contador de items del carrito
├── utils/
│   ├── cart-utils.ts         # Utilidades para manejo del carrito
│   └── airlineApi.ts         # API de la aerolínea
└── router.ts                 # Configuración de rutas
```

### Flujo de Usuario

1. **Explorar Vuelos** → Usuario navega por vuelos disponibles
2. **Ver Detalles** → Usuario selecciona un vuelo específico
3. **Agregar al Carrito** → Usuario agrega el vuelo al carrito
4. **Gestionar Carrito** → Usuario selecciona asientos y categorías
5. **Proceder al Pago** → Usuario va al checkout
6. **Completar Reserva** → Usuario completa el pago y confirma

## 📱 Funcionalidades del Carrito

### Gestión de Items
- ✅ Agregar vuelos
- ✅ Remover vuelos
- ✅ Actualizar selecciones de asientos
- ✅ Validación de disponibilidad

### Selección de Asientos
- **Categorías disponibles:**
  - Primera Clase (FIRST_CLASS)
  - Ejecutiva (BUSINESS)
  - Económica (ECONOMY)

- **Proceso de selección:**
  1. Usuario selecciona categoría
  2. Sistema carga asientos disponibles
  3. Usuario selecciona asiento específico
  4. Sistema calcula precio total

### Cálculo de Precios
- **Tarifa base:** Precio del vuelo por categoría
- **Impuestos:** 15% sobre tarifa base
- **Cargos:** 5% sobre tarifa base
- **Total:** Suma de todos los conceptos

## 🔧 Implementación Técnica

### Estado del Carrito
```typescript
interface CartItem {
  flight: any                    // Información del vuelo
  selectedCategory: string       // Categoría seleccionada
  selectedSeat: string          // Asiento seleccionado
  availableSeats: { [key: string]: string[] }  // Asientos disponibles
  processing: boolean           // Estado de procesamiento
}
```

### Persistencia de Datos
- **localStorage:** Almacena el carrito del usuario
- **Sincronización:** Entre diferentes páginas y componentes
- **Recuperación:** Al recargar la página

### API Integration
- **getAvailableSeats:** Obtiene asientos disponibles por vuelo
- **createTicket:** Crea el boleto final
- **Validación:** Verifica disponibilidad en tiempo real

## 🎨 Interfaz de Usuario

### Diseño Responsivo
- Adaptable a móviles y tablets
- Navegación intuitiva
- Estados de carga y error manejados

### Elementos Visuales
- Iconos descriptivos (✈️, 🛒, 💳)
- Colores consistentes con la marca
- Animaciones suaves y feedback visual

## 🚦 Estados del Sistema

### Carrito Vacío
- Mensaje informativo
- Botón para explorar vuelos
- Diseño centrado y atractivo

### Carrito con Items
- Lista de vuelos con detalles
- Selección de asientos
- Resumen de precios
- Botones de acción

### Procesando
- Indicadores de carga
- Botones deshabilitados
- Mensajes de estado

### Éxito/Error
- Confirmaciones claras
- Opciones de navegación
- Manejo de errores

## 📋 Rutas del Sistema

```typescript
// Rutas protegidas (requieren autenticación)
{
  path: '/cart',
  component: () => import('./pages/cart.vue'),
  beforeEnter: requireAuth
},
{
  path: '/checkout',
  component: () => import('./pages/checkout.vue'),
  beforeEnter: requireAuth
}
```

## 🔒 Seguridad y Validación

### Autenticación
- Todas las rutas del carrito requieren login
- Verificación de usuario en cada operación
- Redirección automática a login si no autenticado

### Validación de Datos
- Verificación de campos requeridos
- Validación de disponibilidad de asientos
- Prevención de duplicados en el carrito

### Manejo de Errores
- Mensajes de error claros y específicos
- Fallback para operaciones fallidas
- Logging de errores para debugging

## 🧪 Testing y Debugging

### Funciones de Debug
- Console logs en operaciones críticas
- Validación de estado del carrito
- Verificación de localStorage

### Casos de Uso
- Agregar múltiples vuelos
- Cambiar selecciones de asientos
- Proceso completo de checkout
- Manejo de errores de API

## 🚀 Próximas Mejoras

### Funcionalidades Futuras
- [ ] Persistencia en base de datos
- [ ] Sincronización entre dispositivos
- [ ] Historial de carritos
- [ ] Notificaciones push
- [ ] Integración con sistemas de pago reales

### Optimizaciones
- [ ] Lazy loading de componentes
- [ ] Cache de asientos disponibles
- [ ] Compresión de datos del carrito
- [ ] PWA para uso offline

## 📖 Uso del Sistema

### Para Usuarios
1. Navegar a `/flights` para ver vuelos disponibles
2. Hacer clic en un vuelo para ver detalles
3. Hacer clic en "🛒 Agregar al Carrito"
4. Ir a `/cart` para gestionar el carrito
5. Seleccionar categorías y asientos
6. Hacer clic en "💳 Proceder al Pago"
7. Completar formularios en `/checkout`
8. Confirmar la reserva

### Para Desarrolladores
1. Importar utilidades del carrito: `import { addToCart, getCart } from '../utils/cart-utils'`
2. Usar funciones para manipular el carrito
3. Implementar listeners para cambios de estado
4. Integrar con APIs existentes

## 🐛 Solución de Problemas

### Problemas Comunes
- **Carrito no se actualiza:** Verificar localStorage y eventos
- **Asientos no disponibles:** Verificar API y estado del vuelo
- **Error en checkout:** Verificar validación de formularios

### Debugging
- Revisar console logs
- Verificar estado del localStorage
- Comprobar respuestas de API
- Validar rutas del router

---

**Desarrollado para AeroLinea** ✈️  
**Sistema de Carrito de Vuelos v1.0** 🛒

