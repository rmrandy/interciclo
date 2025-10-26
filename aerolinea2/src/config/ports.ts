// Configuración centralizada de puertos del sistema
export const PORTS = {
  // Puerto principal del sistema de aerolíneas
  AIRLINE: 2020,
  
  // Puerto para el sistema de farmacia
  PHARMACY: 8082,
  
  // Puerto para servicios externos
  EXTERNAL_SERVICES: 2020,
} as const;

// Obtener la IP del entorno o el hostname actual, sin caer en localhost
const getServerIP = (): string => {
  const envIp = (import.meta.env.VITE_IP as string | undefined)?.trim();
  if (envIp && envIp !== 'localhost' && envIp !== '127.0.0.1') return envIp;

  const host = typeof window !== 'undefined' ? window.location.hostname : '';
  if (host && host !== 'localhost' && host !== '127.0.0.1') return host;

  console.warn('[PORTS] No se detectó IP LAN válida. Sirve el frontend mediante IP (no localhost).');
  return host || '';
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
