/**
 * Configuración de puertos para desarrollo
 * 
 * Puedes cambiar estos valores o establecer variables de entorno:
 * - VITE_BACKEND_PORT: Puerto del backend Django (por defecto 5001)
 * - VITE_FRONTEND_PORT: Puerto del frontend (por defecto 5173)
 */

// Obtener IP del sistema (se puede configurar manualmente)
const getServerIP = () => {
  // Puedes configurar la IP manualmente aquí si es necesario
  const manualIP = import.meta.env.VITE_SERVER_IP;
  if (manualIP) return manualIP;
  
  // Por defecto usa localhost en desarrollo
  return window.location.hostname || 'localhost';
};

export const PORTS = {
  // Puerto del backend Django
  BACKEND: import.meta.env.VITE_BACKEND_PORT || '5001',
  
  // Puerto del frontend (actual)
  FRONTEND: import.meta.env.VITE_FRONTEND_PORT || window.location.port || '5173',
  
  // IP del servidor
  SERVER_IP: getServerIP(),
};

/**
 * Construye la URL base del backend
 */
export const getBackendURL = () => {
  const protocol = window.location.protocol || 'http:';
  return `${protocol}//${PORTS.SERVER_IP}:${PORTS.BACKEND}`;
};

/**
 * Construye una URL completa del backend con el path especificado
 * @param {string} path - Path del endpoint (ej: '/api/flights')
 * @returns {string} URL completa
 */
export const getBackendEndpoint = (path) => {
  const baseURL = getBackendURL();
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  return `${baseURL}${cleanPath}`;
};

// Log de configuración en desarrollo
if (import.meta.env.DEV) {
  console.log('📡 Configuración de puertos:', {
    backend: PORTS.BACKEND,
    frontend: PORTS.FRONTEND,
    serverIP: PORTS.SERVER_IP,
    backendURL: getBackendURL(),
  });
}

