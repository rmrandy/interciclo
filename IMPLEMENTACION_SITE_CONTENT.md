# Implementación de Edición de Textos del Sitio

## Resumen
Se ha implementado una funcionalidad completa para que los administradores puedan editar los textos del header y footer del sitio web de forma dinámica, guardando los cambios en la base de datos.

## Backend Implementado ✅

### 1. Entidad SiteContent
- **Archivo**: `backv5/src/main/java/com/sources/app/entities/SiteContent.java`
- **Descripción**: Entidad JPA para almacenar contenido del sitio
- **Campos**:
  - `id`: Identificador único
  - `contentKey`: Clave única del contenido (ej: "header_title")
  - `contentValue`: Valor del contenido (texto)
  - `description`: Descripción del contenido
  - `createdAt` y `updatedAt`: Timestamps

### 2. DAO SiteContentDAO
- **Archivo**: `backv5/src/main/java/com/sources/app/dao/SiteContentDAO.java`
- **Métodos**:
  - `getAllContent()`: Obtiene todo el contenido
  - `getContentByKey(key)`: Obtiene contenido por clave
  - `updateContentValue(key, value)`: Actualiza contenido
  - `initializeDefaultContent()`: Inicializa contenido por defecto

### 3. Handler SiteContentHandler
- **Archivo**: `backv5/src/main/java/com/sources/app/handlers/SiteContentHandler.java`
- **Endpoints**:
  - `GET /api2/site-content`: Obtener todo el contenido
  - `GET /api2/site-content/{key}`: Obtener contenido específico
  - `PUT /api2/site-content/{key}`: Actualizar contenido (solo admin)
  - `POST /api2/site-content/initialize`: Inicializar contenido (solo admin)

### 4. Configuración del Servidor
- **Archivo**: `backv5/src/main/java/com/sources/app/App.java`
- **Cambio**: Agregado el handler de SiteContent al servidor HTTP

## Frontend Implementado ✅

### 1. Servicio SiteContentService
- **Archivo**: `pharmacy/src/services/SiteContentService.js`
- **Funcionalidades**:
  - Cache de contenido para mejor rendimiento
  - Métodos para obtener y actualizar contenido
  - Manejo de errores

### 2. Header Dinámico
- **Archivo**: `pharmacy/src/components/Header.vue`
- **Funcionalidades**:
  - Título editable para administradores
  - Input inline para edición
  - Guardado automático al hacer blur o presionar Enter
  - Cancelar con Escape

### 3. Footer Dinámico
- **Archivo**: `pharmacy/src/components/Footer.vue`
- **Funcionalidades**:
  - Texto del footer editable
  - Información de contacto editable
  - Misma funcionalidad de edición que el header

### 4. Integración en App.vue
- **Archivo**: `pharmacy/src/App.vue`
- **Cambio**: Agregado el componente Footer

## Script SQL para Base de Datos

### Archivo: `backv5/site_content_table.sql`
```sql
-- Script para crear la tabla site_content
CREATE TABLE IF NOT EXISTS site_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_key VARCHAR(255) NOT NULL UNIQUE,
    content_value TEXT,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insertar contenido por defecto
INSERT IGNORE INTO site_content (content_key, content_value, description) VALUES
('header_title', 'Farmacia Seguros', 'Título principal del header'),
('header_subtitle', 'Tu salud, nuestra prioridad', 'Subtítulo del header'),
('footer_text', '© 2024 Farmacia Seguros. Todos los derechos reservados.', 'Texto del footer'),
('footer_contact', 'Contacto: info@farmaciaseguros.com | Tel: (502) 1234-5678', 'Información de contacto del footer');

-- Crear índice para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_site_content_key ON site_content(content_key);
```

## Instrucciones de Implementación

### 1. Ejecutar Script SQL
```bash
# Conectar a tu base de datos MySQL y ejecutar:
mysql -u tu_usuario -p tu_base_de_datos < backv5/site_content_table.sql
```

### 2. Compilar y Ejecutar Backend
```bash
cd backv5
mvn clean compile
mvn exec:java -Dexec.mainClass="com.sources.app.App"
```

### 3. Ejecutar Frontend
```bash
cd pharmacy
npm install
npm run dev
```

## Funcionalidades Implementadas

### Para Administradores:
1. **Edición de Título del Header**:
   - Hacer clic en el título del header
   - Aparece un ícono de edición ✏️ al pasar el mouse
   - Click en el ícono para editar
   - Enter para guardar, Escape para cancelar

2. **Edición de Footer**:
   - Hacer clic en el texto del footer o información de contacto
   - Misma funcionalidad de edición que el header
   - Cambios se guardan automáticamente en la base de datos

### Para Todos los Usuarios:
- Los textos se cargan dinámicamente desde la base de datos
- Si no hay contenido configurado, se muestran valores por defecto
- Cache implementado para mejor rendimiento

## Seguridad
- Solo usuarios con rol "admin" o "administrador" pueden editar
- Verificación de permisos en el backend
- Headers de autenticación requeridos para operaciones de escritura

## Estructura de Datos
```json
{
  "header_title": "Farmacia Seguros",
  "header_subtitle": "Tu salud, nuestra prioridad", 
  "footer_text": "© 2024 Farmacia Seguros. Todos los derechos reservados.",
  "footer_contact": "Contacto: info@farmaciaseguros.com | Tel: (502) 1234-5678"
}
```

## Notas Técnicas
- **Cache**: Implementado en el frontend para evitar llamadas innecesarias
- **Responsive**: Funciona en desktop y móvil
- **UX**: Feedback visual al editar (hover effects, focus states)
- **Error Handling**: Manejo de errores en frontend y backend
- **CORS**: Configurado para permitir peticiones desde el frontend

## Próximos Pasos Opcionales
1. Agregar más campos editables (subtítulo del header, etc.)
2. Implementar historial de cambios
3. Agregar validación de contenido
4. Implementar preview de cambios
5. Agregar soporte para HTML básico en los textos 