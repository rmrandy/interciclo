# 🏥 Pharmacy - Sistema de Gestión Farmacéutica

Sistema web completo para la gestión de una farmacia, incluyendo venta de medicamentos, procesamiento de recetas médicas, integración con seguros y administración de inventario.

## 📋 Descripción General

Pharmacy es una aplicación web desarrollada en Vue.js que permite gestionar todos los aspectos de una farmacia moderna:

- **Venta de medicamentos** con carrito de compras
- **Procesamiento de recetas médicas** con validación de seguros
- **Gestión de inventario** en tiempo real
- **Integración con sistema de seguros** para verificación de cobertura
- **Panel administrativo** para gestión de productos y usuarios
- **Sistema de calificaciones y comentarios** para productos

## 🚀 Funcionalidades Principales

### Para Clientes
- 📦 **Catálogo de productos** con filtros avanzados
- 🛒 **Carrito de compras** con gestión de cantidades
- 💳 **Proceso de checkout** con validación de tarjetas
- 📋 **Visualización de recetas médicas** con detalles completos
- 💊 **Compra de medicamentos por receta** con cálculo automático de dosis
- ⭐ **Sistema de calificaciones** y comentarios de productos
- 👤 **Gestión de perfil** de usuario

### Para Administradores
- 📊 **Dashboard administrativo** con estadísticas
- 🏥 **Gestión de productos** (CRUD completo)
- 📦 **Control de inventario** con alertas de stock
- 👥 **Gestión de usuarios** y roles
- 📋 **Procesamiento de recetas** médicas
- 💰 **Gestión de pedidos** y ventas
- 🏢 **Configuración de aseguradoras**

### Integración con Seguros
- 🔍 **Verificación de recetas** con códigos de aprobación
- 💊 **Validación de cobertura** de medicamentos
- 📋 **Procesamiento automático** de recetas aprobadas
- ✅ **Marcado de recetas completadas** en sistema de seguros

## 🛠️ Tecnologías Utilizadas

- **Frontend**: Vue.js 3, Vue Router, Pinia (State Management)
- **UI/UX**: Tailwind CSS, FontAwesome
- **HTTP Client**: Axios
- **Gráficos**: Chart.js, Vue-Chartjs
- **Documentación**: JSDoc con tema Clean-JSDoc-Theme

## 📁 Estructura del Proyecto

```
pharmacy/
├── src/
│   ├── components/          # Componentes reutilizables
│   │   ├── Cart.vue        # Carrito de compras
│   │   ├── Header.vue      # Encabezado de navegación
│   │   ├── Footer.vue      # Pie de página
│   │   └── ...
│   ├── pages/              # Páginas principales
│   │   ├── Home.vue        # Página de inicio
│   │   ├── Catalogo.vue    # Catálogo de productos
│   │   ├── Prescriptions.vue # Gestión de recetas
│   │   ├── admin/          # Páginas administrativas
│   │   └── ...
│   ├── services/           # Servicios de API
│   │   ├── ApiService.js   # Configuración de APIs
│   │   ├── PrescriptionService.js # Lógica de recetas
│   │   └── authService.js  # Autenticación
│   ├── stores/             # Estado global (Pinia)
│   │   └── userStore.js    # Estado del usuario
│   └── views/              # Vistas especializadas
│       └── ProcessPrescription.vue # Procesamiento de recetas
├── public/                 # Archivos estáticos
└── docs/                   # Documentación generada
```

## 🚀 Instalación y Configuración

### Prerrequisitos
- Node.js (versión 16 o superior)
- npm o yarn
- Backend de Pharmacy corriendo en puerto 8080
- Backend de Ensurance corriendo en puerto 8082

### Instalación

1. **Clonar el repositorio**
```bash
git clone <url-del-repositorio>
cd pharmacy
```

2. **Instalar dependencias**
```bash
npm install
```

3. **Configurar variables de entorno**
```bash
# Crear archivo .env en la raíz del proyecto
VUE_APP_API_HOST=localhost
VUE_APP_IP=localhost
```

4. **Ejecutar en modo desarrollo**
```bash
npm run serve
```

La aplicación estará disponible en `http://localhost:8080`

## 📚 Scripts Disponibles

```bash
# Desarrollo
npm run serve          # Inicia servidor de desarrollo
npm run build          # Construye para producción
npm run lint           # Ejecuta linter

# Documentación
npm run docs           # Genera documentación JSDoc
npm run docs:watch     # Genera documentación en modo watch
npm run docs:serve     # Sirve la documentación en http://localhost:8081
```

## 🔧 Configuración de APIs

El sistema utiliza dos APIs principales:

- **Pharmacy API** (puerto 8080): Gestión de productos, inventario y pedidos
- **Ensurance API** (puerto 8082): Verificación de recetas y cobertura de seguros

Los puertos se pueden configurar dinámicamente desde la interfaz de usuario.

## 📖 Documentación

### Generar Documentación
```bash
npm run docs
```

### Ver Documentación
```bash
npm run docs:serve
```

La documentación estará disponible en `http://localhost:8081`

## 🤝 Cómo Contribuir

1. **Fork** el proyecto
2. Crea una **rama** para tu feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. **Push** a la rama (`git push origin feature/AmazingFeature`)
5. Abre un **Pull Request**

### Estándares de Código
- Usar **ESLint** para mantener consistencia
- Documentar funciones con **JSDoc**
- Seguir convenciones de **Vue.js**
- Escribir **tests** para nuevas funcionalidades

## 📞 Contacto

- **Desarrollador**: Randy Rivera
- **Email**: [tu-email@ejemplo.com]
- **Proyecto**: [URL del repositorio]

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 🙏 Agradecimientos

- Vue.js por el framework
- Tailwind CSS por los estilos
- FontAwesome por los iconos
- Chart.js por las gráficas

---

**Desarrollado con ❤️ para la gestión farmacéutica moderna**
