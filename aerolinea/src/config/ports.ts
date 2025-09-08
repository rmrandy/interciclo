// Configuración centralizada de puertos del sistema
export const PORTS = {
  // Puerto principal del sistema de aerolíneas
  AIRLINE: 8080,
  
  // Puerto para el sistema de farmacia
  PHARMACY: 8082,
  
  // Puerto para servicios externos
  EXTERNAL_SERVICES: 8080,
} as const;

// Obtener la IP del entorno o usar la IP local del usuario
const getServerIP = (): string => {
  // Usar la IP del entorno si está configurada, sino usar la IP local del usuario
  return import.meta.env.VITE_IP || '192.168.0.26';
};

// Función para obtener la URL base de un servicio
export function getServiceUrl(service: keyof typeof PORTS, path: string = ''): string {
  const port = PORTS[service];
  const ip = getServerIP();
  return `http://${ip}:${port}${path}`;
}

// Función para obtener la URL de la API de actualización de perfil
export function getUserProfileUpdateUrl(path: string = ''): string {
  return getServiceUrl('AIRLINE', path);
}

// Función para obtener la URL de la API de aerolíneas
export function getAirlineUrl(path: string = ''): string {
  return getServiceUrl('AIRLINE', path);
}

// Función para obtener la URL de la API de farmacia
export function getPharmacyUrl(path: string = ''): string {
  return getServiceUrl('PHARMACY', path);
}
