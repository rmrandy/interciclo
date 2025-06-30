-- Script para crear la tabla de contenido del sitio
-- Ejecutar este script en la base de datos para habilitar la funcionalidad de contenido dinámico

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

-- Verificar que la tabla se creó correctamente
SELECT 'Tabla site_content creada exitosamente' as status;
SELECT COUNT(*) as total_records FROM site_content; 