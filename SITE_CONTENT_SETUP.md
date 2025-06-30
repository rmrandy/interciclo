# 🎯 Configuración del Sistema de Contenido Dinámico

## 📋 Resumen
Este sistema permite a los administradores editar dinámicamente los textos del header, footer y otros elementos del sitio web de la farmacia, guardando los cambios en la base de datos.

## 🚀 Configuración del Backend (backv5)

### 1. Crear la tabla en la base de datos
Ejecuta el siguiente script SQL en tu base de datos:

```sql
CREATE TABLE IF NOT EXISTS site_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_key VARCHAR(255) NOT NULL UNIQUE,
    content_value TEXT,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_site_content_key ON site_content(content_key);
CREATE INDEX IF NOT EXISTS idx_site_content_updated ON site_content(updated_at);

-- Insertar contenido inicial por defecto
INSERT IGNORE INTO site_content (content_key, content_value, description) VALUES
('header_title', 'Farmacia Seguros', 'Título principal del header'),
('header_subtitle', 'Tu salud, nuestra prioridad', 'Subtítulo del header'),
('footer_text', '© 2024 Farmacia Seguros. Todos los derechos reservados.', 'Texto del footer'),
('footer_contact', 'Contacto: info@farmaciaseguros.com | Tel: (502) 1234-5678', 'Información de contacto del footer'),
('welcome_message', 'Bienvenido a Farmacia Seguros', 'Mensaje de bienvenida'),
('about_us', 'Somos una farmacia comprometida con tu salud y bienestar', 'Texto sobre nosotros'),
('contact_info', 'Estamos disponibles 24/7 para atenderte', 'Información de contacto general');
```

### 2. Archivos del Backend
Los siguientes archivos ya están implementados:

- ✅ `SiteContent.java` - Entidad JPA
- ✅ `SiteContentDAO.java` - Data Access Object
- ✅ `SiteContentHandler.java` - Handler original
- ✅ `SiteContentController.java` - Controlador mejorado (nuevo)
- ✅ `App.java` - Servidor configurado

### 3. Endpoints disponibles

#### API Original (`/api2/site-content`):
- `GET /api2/site-content` - Obtener todo el contenido
- `GET /api2/site-content/{key}` - Obtener contenido específico
- `PUT /api2/site-content/{key}` - Actualizar contenido (solo admin)
- `POST /api2/site-content/initialize` - Inicializar contenido (solo admin)

#### API Mejorada (`/api2/site-content-v2`):
- `GET /api2/site-content-v2` - Obtener todo el contenido
- `GET /api2/site-content-v2/{key}` - Obtener contenido específico
- `POST /api2/site-content-v2` - Crear nuevo contenido (solo admin)
- `PUT /api2/site-content-v2/{key}` - Actualizar contenido (solo admin)
- `DELETE /api2/site-content-v2/{key}` - Eliminar contenido (solo admin)
- `POST /api2/site-content-v2/initialize` - Inicializar contenido (solo admin)

## 🎨 Configuración del Frontend (pharmacy)

### 1. Archivos del Frontend
Los siguientes archivos ya están implementados:

- ✅ `SiteContentService.js` - Servicio mejorado con cache
- ✅ `SiteContentManager.vue` - Componente de administración
- ✅ `site-content.vue` - Página de administración
- ✅ `Header.vue` - Enlaces agregados
- ✅ `Footer.vue` - Edición inline
- ✅ `router/index.js` - Ruta agregada

### 2. Acceso al Administrador
1. Inicia sesión como administrador
2. Ve al menú de navegación
3. Haz clic en "📝 Contenido del Sitio"
4. O navega directamente a `/admin/site-content`

### 3. Funcionalidades disponibles

#### Edición de Header:
- Título del header
- Subtítulo del header

#### Edición de Footer:
- Texto del footer
- Información de contacto

#### Contenido Personalizado:
- Crear nuevos elementos de contenido
- Editar contenido existente
- Eliminar contenido
- Vista previa en tiempo real

#### Gestión de Cache:
- Recargar contenido
- Limpiar cache
- Inicializar contenido por defecto

## 🔧 Cómo usar

### Para Administradores:

1. **Acceder al administrador:**
   ```
   http://localhost:8080/admin/site-content
   ```

2. **Editar contenido del header:**
   - Haz clic en el campo del título o subtítulo
   - Modifica el texto
   - Presiona Enter o haz clic en 💾 para guardar

3. **Editar contenido del footer:**
   - Haz clic en el campo del texto o contacto
   - Modifica el contenido
   - Presiona Enter o haz clic en 💾 para guardar

4. **Crear contenido personalizado:**
   - Completa la clave (ej: `welcome_message`)
   - Escribe el valor del contenido
   - Agrega una descripción opcional
   - Haz clic en "➕ Crear Contenido"

5. **Gestionar contenido existente:**
   - Ver la lista de todo el contenido
   - Editar cualquier elemento
   - Eliminar contenido no deseado

### Para Desarrolladores:

#### Usar el servicio en componentes:

```javascript
import SiteContentService from '@/services/SiteContentService';

// Obtener contenido específico
const headerTitle = await SiteContentService.getHeaderTitle();
const footerText = await SiteContentService.getFooterText();

// Obtener todo el contenido del header
const headerContent = await SiteContentService.getHeaderContent();

// Obtener todo el contenido del footer
const footerContent = await SiteContentService.getFooterContent();

// Actualizar contenido (solo admin)
await SiteContentService.updateHeaderTitle('Nuevo Título');
await SiteContentService.updateFooterText('Nuevo texto del footer');
```

#### Usar contenido en templates:

```vue
<template>
  <div>
    <h1>{{ headerTitle }}</h1>
    <p>{{ headerSubtitle }}</p>
    <footer>{{ footerText }}</footer>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import SiteContentService from '@/services/SiteContentService';

export default {
  setup() {
    const headerTitle = ref('');
    const headerSubtitle = ref('');
    const footerText = ref('');

    onMounted(async () => {
      headerTitle.value = await SiteContentService.getHeaderTitle();
      headerSubtitle.value = await SiteContentService.getHeaderSubtitle();
      footerText.value = await SiteContentService.getFooterText();
    });

    return {
      headerTitle,
      headerSubtitle,
      footerText
    };
  }
};
</script>
```

## 🔒 Seguridad

- Solo los usuarios con rol `admin` o `administrador` pueden:
  - Acceder al administrador de contenido
  - Editar contenido del sitio
  - Crear nuevo contenido
  - Eliminar contenido
  - Inicializar contenido por defecto

- Los usuarios normales solo pueden ver el contenido

## 🚀 Iniciar el Sistema

1. **Backend:**
   ```bash
   cd backv5
   mvn clean compile
   mvn exec:java -Dexec.mainClass="com.sources.app.App"
   ```

2. **Frontend:**
   ```bash
   cd pharmacy
   npm install
   npm run serve
   ```

3. **Base de datos:**
   - Asegúrate de que la tabla `site_content` esté creada
   - Ejecuta el script SQL proporcionado

## 🎯 Características Principales

- ✅ **Edición en tiempo real** - Los cambios se reflejan inmediatamente
- ✅ **Cache inteligente** - Mejora el rendimiento
- ✅ **Interfaz intuitiva** - Fácil de usar para administradores
- ✅ **Contenido personalizable** - Crear cualquier tipo de contenido
- ✅ **Seguridad** - Solo administradores pueden editar
- ✅ **Responsive** - Funciona en móviles y desktop
- ✅ **Notificaciones** - Feedback visual para todas las acciones
- ✅ **Validación** - Previene errores de entrada

## 🐛 Solución de Problemas

### Error 404 en endpoints:
- Verifica que el servidor esté corriendo
- Confirma que los handlers estén registrados en `App.java`

### Error de permisos:
- Asegúrate de estar logueado como administrador
- Verifica que el rol sea `admin` o `administrador`

### Contenido no se actualiza:
- Limpia el cache con el botón "🗑️ Limpiar Cache"
- Recarga la página
- Verifica la conexión a la base de datos

### Error de base de datos:
- Ejecuta el script SQL para crear la tabla
- Verifica la configuración de Hibernate
- Revisa los logs del servidor

## 📞 Soporte

Si tienes problemas:
1. Revisa los logs del navegador (F12)
2. Revisa los logs del servidor
3. Verifica la conexión a la base de datos
4. Confirma que todos los archivos estén en su lugar

¡El sistema está listo para usar! 🎉 