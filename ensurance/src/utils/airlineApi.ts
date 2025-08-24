// Configuración dinámica de API para el sistema de aerolíneas
export class AirlineApiConfig {
  private static instance: AirlineApiConfig
  private baseUrl: string = ''
  private port: string = '8080'
  private ip: string = (import.meta as any).env?.VITE_IP || window.location.hostname || 'localhost'

  private constructor() {
    this.loadConfig()
  }

  public static getInstance(): AirlineApiConfig {
    if (!AirlineApiConfig.instance) {
      AirlineApiConfig.instance = new AirlineApiConfig()
    }
    return AirlineApiConfig.instance
  }

  private loadConfig() {
    // Cargar configuración desde localStorage o usar valores por defecto
    this.ip = localStorage.getItem('api_ip') || window.location.hostname || 'localhost'
    this.port = localStorage.getItem('api_port') || '8080'
    this.baseUrl = `http://${this.ip}:${this.port}`
  }

  public updateConfig(ip: string, port: string) {
    this.ip = ip
    this.port = port
    this.baseUrl = `http://${ip}:${port}`
    
    // Guardar en localStorage
    localStorage.setItem('api_ip', ip)
    localStorage.setItem('api_port', port)
  }

  public resetConfig() {
    // Limpiar configuración y usar valores por defecto
    localStorage.removeItem('api_ip')
    localStorage.removeItem('api_port')
    this.loadConfig()
  }

  public getBaseUrl(): string {
    return this.baseUrl
  }

  public getIp(): string {
    return this.ip
  }

  public getPort(): string {
    return this.port
  }
}

// Cliente API para operaciones de aerolíneas
export class AirlineApiClient {
  private config: AirlineApiConfig

  constructor() {
    this.config = AirlineApiConfig.getInstance()
  }

  private async makeRequest(endpoint: string, options: RequestInit = {}): Promise<any> {
    const url = `${this.config.getBaseUrl()}${endpoint}`
    
    const defaultOptions: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    }

    try {
      const response = await fetch(url, { ...defaultOptions, ...options })
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      return await response.json()
    } catch (error) {
      console.error('API request failed:', error)
      throw error
    }
  }

  // ===== CIUDADES =====
  async getCities() {
    try {
      const response = await this.makeRequest('/api/airline/cities', { method: 'GET' })
      console.log('🔍 Respuesta de getCities:', response)
      return response
    } catch (error) {
      console.error('❌ Error en getCities:', error)
      throw error
    }
  }

  async createCity(payload: { name: string; country: string }) {
    return this.makeRequest('/api/airline/cities', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  }

  // ===== VUELOS =====
  async getFlights() {
    try {
      const response = await this.makeRequest('/api/airline/flights', { method: 'GET' })
      console.log('🔍 Respuesta de getFlights:', response)
      return response
    } catch (error) {
      console.error('❌ Error en getFlights:', error)
      throw error
    }
  }

  async createFlight(flightData: any) {
    return this.makeRequest('/api/airline/flights', {
      method: 'POST',
      body: JSON.stringify(flightData),
    })
  }

  // Obtener inventario de asientos disponibles de un vuelo
  async getFlightInventory(flightId: number): Promise<any> {
    try {
      const response = await fetch(`${this.config.getBaseUrl()}/api/airline/inventory/${flightId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      
      // El nuevo endpoint ya devuelve la estructura correcta
      if (data.success && data.inventory) {
        return {
          success: true,
          inventory: data.inventory,
          summary: data.summary
        };
      }
      
      return data;
    } catch (error) {
      console.error('Error obteniendo inventario del vuelo:', error);
      throw error;
    }
  }

  // Obtener resumen del inventario de un vuelo
  async getFlightInventorySummary(flightId: number): Promise<any> {
    try {
      const response = await fetch(`${this.config.getBaseUrl()}/api/airline/inventory/${flightId}/summary`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error obteniendo resumen del inventario:', error);
      throw error;
    }
  }

  // Obtener asientos disponibles de un vuelo (método legacy mantenido por compatibilidad)
  async getAvailableSeats(flightId: number): Promise<any> {
    try {
      const response = await fetch(`${this.config.getBaseUrl()}/api/airline/seats/${flightId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error obteniendo asientos disponibles:', error);
      throw error;
    }
  }

  // Crear boleto
  async createTicket(ticketData: any): Promise<any> {
    try {
      const response = await fetch(`${this.config.getBaseUrl()}/api/airline/tickets`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(ticketData),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error creando boleto:', error);
      throw error;
    }
  }

  async updateFlightStatus(flightId: number, status: string) {
    return this.makeRequest(`/api/airline/flights/${flightId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    })
  }

  // ===== USUARIOS =====
  async registerVisitor(userData: {
    firstName: string
    lastName: string
    email: string
    password: string
    age: number
    country: string
    passportNumber: string
    phone?: string
    address?: string
    captchaToken: string
  }) {
    return this.makeRequest('/api/airline/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    })
  }

  async login(email: string, password: string) {
    return this.makeRequest('/api/airline/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    })
  }

  // ===== ROLES =====
  async getRoles() {
    return this.makeRequest('/api/airline/roles', {
      method: 'GET',
    })
  }

  async assignUserRole(userId: number, role: string, adminToken: string) {
    return this.makeRequest('/api/airline/assign-role', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${adminToken}`,
      },
      body: JSON.stringify({ userId, role }),
    })
  }

  async getUsers(adminToken: string) {
    return this.makeRequest('/api/airline/users', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${adminToken}`,
      },
    })
  }

  async initializeDefaultRoles(adminToken: string) {
    return this.makeRequest('/api/airline/initialize-roles', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${adminToken}`,
      },
    })
  }

  // ===== ESTADO DE LA API =====
  async checkApiStatus() {
    try {
      const response = await fetch(`${this.config.getBaseUrl()}/api/health`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })
      return response.ok
    } catch (error) {
      return false
    }
  }

  // Resetear configuración
  resetConfig() {
    this.config.resetConfig()
  }
}

// Instancia global del cliente API
export const airlineApi = new AirlineApiClient()

// Tipos TypeScript para las respuestas
export interface RegisterResponse {
  success: boolean
  message?: string
  error?: string
  userId?: number
  email?: string
  role?: string
}

export interface LoginResponse {
  success: boolean
  message?: string
  error?: string
  user?: {
    id: number
    email: string
    role: string
    firstName: string
    lastName: string
  }
  token?: string
}

export interface Role {
  idRole: number
  roleName: string
  description: string
  permissions: string
  enabled: number
  createdAt: string
  updatedAt: string
}

export interface RolesResponse {
  success: boolean
  roles?: Role[]
  error?: string
}

export interface User {
  idUser: number
  name: string
  firstName: string
  lastName: string
  email: string
  role: string
  age: number
  country: string
  passportNumber: string
  enabled: number
  createdAt: string
}

export interface UsersResponse {
  success: boolean
  users?: User[]
  error?: string
}

