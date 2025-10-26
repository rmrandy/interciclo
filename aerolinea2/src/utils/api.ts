/**
 * Utilidades para manejar las URLs de las APIs con puertos configurables
 */

// Resolver IP de forma dinámica: prioriza VITE_IP, luego el hostname del navegador.
// Nunca forzar "localhost"; si el frontend se sirve como localhost, se advertirá.
const resolveHostIP = (): string => {
  // 1) Variable de entorno (inyectada por Vite, p.ej. desde getip.py)
  const envIp = (import.meta.env.VITE_IP as string | undefined)?.trim();
  if (envIp && envIp !== 'localhost' && envIp !== '127.0.0.1') return envIp;

  // 2) Hostname desde la URL donde se sirve el frontend
  const host = typeof window !== 'undefined' ? window.location.hostname : '';
  if (host && host !== 'localhost' && host !== '127.0.0.1') return host;

  // 3) Sin IP válida: advertir para que el usuario abra el frontend mediante IP LAN
  console.warn('[API] No se detectó IP LAN válida. Abre el frontend usando la IP (no localhost).');
  return host || '';
};

const ip = resolveHostIP();

// Interfaces
interface PortConfig {
  ensurance: string;  // Puerto para el backend de seguros
  pharmacy: string;   // Puerto para el backend de farmacia
  airline: string;    // Puerto para el backend de aerolínea
}

// Almacenar la configuración de puertos
let portConfig: PortConfig = {
  ensurance: "2020",  // Puerto por defecto para ensurance
  pharmacy: "2020",   // Puerto por defecto para pharmacy
  airline: "2020"     // Puerto por defecto para aerolínea (mismo servidor)
};

/**
 * Configura los puertos para las APIs
 * @param ports Configuración de puertos
 */
export const configureApiPorts = (ports: Partial<PortConfig>): void => {
  if (ports.ensurance) portConfig.ensurance = ports.ensurance;
  if (ports.pharmacy) portConfig.pharmacy = ports.pharmacy;
  if (ports.airline) portConfig.airline = ports.airline;
  
  // Guardar la configuración en localStorage para persistencia
  localStorage.setItem("apiPortConfig", JSON.stringify(portConfig));
  
  console.log(`Puertos configurados: Ensurance=${portConfig.ensurance}, Pharmacy=${portConfig.pharmacy}, Airline=${portConfig.airline}`);
};

/**
 * Carga la configuración de puertos desde localStorage
 */
export const loadPortConfiguration = (): void => {
  const savedConfig = localStorage.getItem("apiPortConfig");
  if (savedConfig) {
    try {
      const config = JSON.parse(savedConfig);
      portConfig = { ...portConfig, ...config };
      console.log(`Configuración de puertos cargada: Ensurance=${portConfig.ensurance}, Pharmacy=${portConfig.pharmacy}, Airline=${portConfig.airline}`);
    } catch (error) {
      console.warn("Error al cargar configuración de puertos:", error);
    }
  }
};

/**
 * Obtiene la URL de la API de seguros con el puerto configurado
 * 
 * @param endpoint - El endpoint de la API sin la barra inicial, ej: "users"
 * @returns URL completa con el puerto correcto
 */
export const getInsuranceApiUrl = (endpoint: string = ''): string => {
  // Normalizar endpoint y permitir vacío
  const cleanEndpoint = (endpoint || '').replace(/^\//, '');
  const suffix = cleanEndpoint ? `/api/${cleanEndpoint}` : '/api';
  return `http://${ip}:${portConfig.ensurance}${suffix}`;
};

/**
 * Obtiene la URL de la API de farmacia con el puerto configurado
 * 
 * @param endpoint - El endpoint de la API sin la barra inicial
 * @returns URL completa de la API de farmacia
 */
export const getPharmacyApiUrl = (endpoint: string = ''): string => {
  const cleanEndpoint = (endpoint || '').replace(/^\//, '');
  const suffix = cleanEndpoint ? `/api/${cleanEndpoint}` : '/api';
  return `http://${ip}:${portConfig.pharmacy}${suffix}`;
};

/**
 * Obtiene la URL de la API de aerolínea con el puerto configurado
 * 
 * @param endpoint - El endpoint de la API sin la barra inicial
 * @returns URL completa de la API de aerolínea
 */
export const getAirlineApiUrl = (endpoint: string = ''): string => {
  const cleanEndpoint = (endpoint || '').replace(/^\//, '');
  const suffix = cleanEndpoint ? `/api/airline/${cleanEndpoint}` : '/api/airline';
  return `http://${ip}:${portConfig.airline}${suffix}`;
};