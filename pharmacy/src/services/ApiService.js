/**
 * Servicio para manejar las URLs de las APIs con puertos configurables
 * 
 * Este servicio centraliza la configuración y gestión de las URLs de las APIs
 * utilizadas en el sistema de farmacia. Permite configurar dinámicamente
 * los puertos para diferentes entornos (desarrollo, producción, testing).
 * 
 * @module ApiService
 * @author Randy Rivera
 * @version 1.0.0
 */

// Importar variables de entorno
// La IP se detecta automáticamente con getip.py
// Si necesitas configurar manualmente, cambia esta línea:
// const ip = '192.168.0.22'; // Tu IP local
const ip = process.env.VUE_APP_API_HOST || process.env.VUE_APP_IP || 'localhost';

/**
 * @typedef {Object} PortConfig
 * @property {string} pharmacy - Puerto para la API de farmacia
 * @property {string} ensurance - Puerto para la API de seguros
 */

/**
 * @typedef {Object} CheckoutData
 * @property {string} orderId - ID de la orden
 * @property {Array<Object>} items - Productos en la orden
 * @property {number} total - Monto total de la orden
 * @property {Object} customer - Información del cliente
 * @property {string} customer.name - Nombre del cliente
 * @property {string} customer.email - Email del cliente
 * @property {string} customer.phone - Teléfono del cliente
 * @property {Object} payment - Información de pago
 * @property {string} payment.method - Método de pago
 * @property {string} [payment.cardNumber] - Número de tarjeta (si aplica)
 */

// Configuración de puertos por defecto
const defaultPortConfig = {
  pharmacy: '8080',    // Puerto por defecto para pharmacy
  ensurance: '8082'    // Puerto por defecto para ensurance
};

// Almacenar la configuración de puertos
let portConfig = { ...defaultPortConfig };

/**
 * Configura los puertos para las APIs
 * 
 * Permite cambiar dinámicamente los puertos de las APIs sin necesidad
 * de reiniciar la aplicación. La configuración se guarda en localStorage
 * para persistir entre sesiones.
 * 
 * @param {PortConfig} ports - Configuración de puertos a aplicar
 * @returns {void}
 * 
 * @example
 * // Configurar puertos personalizados
 * configureApiPorts({
 *   pharmacy: '8081',
 *   ensurance: '8083'
 * });
 * 
 * @example
 * // Configurar solo un puerto
 * configureApiPorts({
 *   pharmacy: '9000'
 * });
 * // El puerto de ensurance mantiene su valor por defecto
 */
export const configureApiPorts = (ports) => {
  if (ports.pharmacy) portConfig.pharmacy = ports.pharmacy;
  if (ports.ensurance) portConfig.ensurance = ports.ensurance;
  
  // Guardar la configuración en localStorage para persistencia
  localStorage.setItem("apiPortConfig", JSON.stringify(portConfig));
  
  console.log(`Puertos configurados: Pharmacy=${portConfig.pharmacy}, Ensurance=${portConfig.ensurance}`);
};

/**
 * Carga la configuración de puertos desde localStorage
 * 
 * Restaura la configuración de puertos guardada previamente.
 * Útil para mantener la configuración personalizada entre
 * sesiones del navegador.
 * 
 * @returns {void}
 * 
 * @example
 * // Cargar configuración al iniciar la aplicación
 * loadPortConfiguration();
 * console.log('Configuración de puertos restaurada');
 */
export const loadPortConfiguration = () => {
  const savedConfig = localStorage.getItem("apiPortConfig");
  if (savedConfig) {
    try {
      const config = JSON.parse(savedConfig);
      portConfig = { ...portConfig, ...config };
      console.log(`Configuración de puertos cargada: Pharmacy=${portConfig.pharmacy}, Ensurance=${portConfig.ensurance}`);
    } catch (error) {
      console.warn("Error al cargar configuración de puertos:", error);
    }
  }
};

/**
 * Obtiene la URL de la API de farmacia con el puerto configurado
 * 
 * Construye la URL completa para la API de farmacia usando:
 * - El host configurado (por defecto localhost)
 * - El puerto configurado para pharmacy
 * - El endpoint proporcionado
 * 
 * @param {string} endpoint - El endpoint de la API sin la barra inicial
 * @returns {string} URL completa con el puerto correcto
 * 
 * @example
 * // Obtener URL para listar medicamentos
 * const url = getPharmacyApiUrl('medicines');
 * // Resultado: "http://localhost:8080/api2/medicines"
 * 
 * @example
 * // Obtener URL para un medicamento específico
 * const url = getPharmacyApiUrl('medicines/123');
 * // Resultado: "http://localhost:8080/api2/medicines/123"
 * 
 * @example
 * // Con endpoint que incluye barra inicial
 * const url = getPharmacyApiUrl('/orders/checkout');
 * // Resultado: "http://localhost:8080/api2/orders/checkout"
 */
export const getPharmacyApiUrl = (endpoint) => {
  // Eliminar la barra inicial del endpoint si existe
  const cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
  
  // Construir la URL completa
  return `http://${ip}:${portConfig.pharmacy}/api2/${cleanEndpoint}`;
};

/**
 * Obtiene la URL de la API de seguros con el puerto configurado
 * 
 * Construye la URL completa para la API de seguros usando:
 * - El host configurado (por defecto localhost)
 * - El puerto configurado para ensurance
 * - El endpoint proporcionado
 * 
 * @param {string} endpoint - El endpoint de la API sin la barra inicial
 * @returns {string} URL completa con el puerto correcto
 * 
 * @example
 * // Verificar una receta
 * const url = getEnsuranceApiUrl('service-approvals/check/APP123');
 * // Resultado: "http://localhost:8082/api2/service-approvals/check/APP123"
 * 
 * @example
 * // Verificar cobertura de medicamento
 * const url = getEnsuranceApiUrl('medication-coverage/check');
 * // Resultado: "http://localhost:8082/api2/medication-coverage/check"
 */
export const getEnsuranceApiUrl = (endpoint) => {
  // Eliminar la barra inicial del endpoint si existe
  const cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
  
  // Construir la URL completa
  return `http://${ip}:${portConfig.ensurance}/api2/${cleanEndpoint}`;
};

/**
 * Envía la orden de compra al backend
 * 
 * Procesa una orden de compra completa enviando los datos al backend
 * de farmacia para su procesamiento y confirmación.
 * 
 * @param {CheckoutData} checkoutData - Datos completos de la orden de compra
 * @returns {Promise<Object>} Respuesta del backend con el estado de la orden
 * @throws {Error} Si hay error en el procesamiento de la orden
 * 
 * @example
 * const orderData = {
 *   orderId: 'ORD123',
 *   items: [
 *     { id: 'MED001', quantity: 2, price: 10.50 }
 *   ],
 *   total: 21.00,
 *   customer: {
 *     name: 'Juan Pérez',
 *     email: 'juan@ejemplo.com',
 *     phone: '123456789'
 *   },
 *   payment: {
 *     method: 'credit_card',
 *     cardNumber: '****-****-****-1234'
 *   }
 * };
 * 
 * try {
 *   const result = await checkoutOrder(orderData);
 *   console.log('Orden procesada:', result.orderId);
 * } catch (error) {
 *   console.error('Error al procesar orden:', error);
 * }
 */
export const checkoutOrder = async (checkoutData) => {
  const url = getPharmacyApiUrl('orders/checkout');
  return await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(checkoutData)
  }).then(res => res.json());
};

/**
 * Obtiene la configuración actual de puertos
 * 
 * @returns {PortConfig} Configuración actual de puertos
 * 
 * @example
 * const config = getCurrentPortConfig();
 * console.log('Puerto Pharmacy:', config.pharmacy);
 * console.log('Puerto Ensurance:', config.ensurance);
 */
export const getCurrentPortConfig = () => {
  return { ...portConfig };
};

/**
 * Restablece la configuración de puertos a los valores por defecto
 * 
 * @returns {void}
 * 
 * @example
 * resetPortConfiguration();
 * console.log('Puertos restablecidos a valores por defecto');
 */
export const resetPortConfiguration = () => {
  portConfig = { ...defaultPortConfig };
  localStorage.removeItem("apiPortConfig");
  console.log('Configuración de puertos restablecida a valores por defecto');
};

/**
 * Métodos HTTP genéricos para consumir cualquier endpoint REST
 */
const get = async (url, options = {}) => {
  return fetch(url, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  }).then(res => res.json());
};

const post = async (url, data = {}, options = {}) => {
  return fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    body: JSON.stringify(data),
    ...options
  }).then(res => res.json());
};

const put = async (url, data = {}, options = {}) => {
  return fetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    body: JSON.stringify(data),
    ...options
  }).then(res => res.json());
};

const del = async (url, options = {}) => {
  return fetch(url, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  }).then(res => res.json());
};

/**
 * Obtiene la URL de la API en un puerto arbitrario
 * @param {string} endpoint - El endpoint de la API sin la barra inicial
 * @param {string|number} port - El puerto al que apuntar
 * @returns {string} URL completa con el puerto indicado
 * @example
 * const url = getApiUrlWithPort('medicines', 8085);
 * // Resultado: "http://localhost:8085/api2/medicines"
 */
export const getApiUrlWithPort = (endpoint, port) => {
  const cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
  return `http://${ip}:${port}/api2/${cleanEndpoint}`;
};

// Exportar funciones y configuración por defecto
export default {
  configureApiPorts,
  loadPortConfiguration,
  getPharmacyApiUrl,
  getEnsuranceApiUrl,
  getCurrentPortConfig,
  resetPortConfiguration,
  defaultPortConfig,
  checkoutOrder,
  get,
  post,
  put,
  delete: del
}; 