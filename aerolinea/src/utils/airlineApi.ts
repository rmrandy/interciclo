// Configuración dinámica de API para el sistema de aerolíneas
export class AirlineApiConfig {
  private static instance: AirlineApiConfig
  private baseUrl: string = ''
  private port: string = '8080'
  private ip: string = (import.meta as any).env?.VITE_IP || window.location.hostname || 'localhost'

  private constructor() {
    this.loadConfig() // Cargar configuración desde localStorage o usar valores por defecto
  }

  public static getInstance(): AirlineApiConfig {
    if (!AirlineApiConfig.instance) {
      AirlineApiConfig.instance = new AirlineApiConfig()
    }
    return AirlineApiConfig.instance
  }

  private loadConfig() {
    // Cargar configuración desde localStorage o usar valores por defecto
    this.ip = localStorage.getItem('api_ip') || (import.meta as any).env?.VITE_IP || window.location.hostname || 'localhost'
    this.port = localStorage.getItem('api_port') || '8080'
    this.baseUrl = `http://${this.ip}:${this.port}`
    console.log('🔧 AirlineApiConfig cargada:', { ip: this.ip, port: this.port, baseUrl: this.baseUrl })
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
    
    // Construir headers dinámicos: solo forzar JSON si el body no es FormData
    const headers: Record<string, string> = { ...(options.headers as any) }
    const isFormData = typeof FormData !== 'undefined' && (options as any).body instanceof FormData
    if (!isFormData) {
      headers['Content-Type'] = headers['Content-Type'] || 'application/json'
    }

    const finalOptions: RequestInit = { ...options, headers }

    try {
      const response = await fetch(url, finalOptions)
      
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

  // Creación masiva de vuelos
  async bulkCreateFlights(payload: any) {
    return this.makeRequest('/api/airline/flights/bulk', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  }

  // Carga masiva desde archivo JSON (multipart/form-data)
  async bulkCreateFlightsFromFile(file: File) {
    const form = new FormData()
    form.append('file', file)
    return this.makeRequest('/api/airline/flights/bulk', {
      method: 'POST',
      body: form
    })
  }

  // ===== AERONAVES =====
  async getAircrafts() {
    return this.makeRequest('/api/airline/aircrafts', { method: 'GET' })
  }

  async createAircraft(payload: { registration: string; model: string; manufacturer: string; seatCapacity: number }) {
    return this.makeRequest('/api/airline/aircrafts', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  }

  async updateAircraft(id: number, payload: Partial<{ registration: string; model: string; manufacturer: string; seatCapacity: number }>) {
    return this.makeRequest(`/api/airline/aircrafts/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  }

  async deleteAircraft(id: number) {
    return this.makeRequest(`/api/airline/aircrafts/${id}`, { method: 'DELETE' })
  }

  async getSeatConfig(aircraftId: number) {
    return this.makeRequest(`/api/airline/aircrafts/${aircraftId}/seat-config`, { method: 'GET' })
  }

  async updateSeatConfig(aircraftId: number, config: Record<string, { seats: number; priceMultiplier: number }>) {
    return this.makeRequest(`/api/airline/aircrafts/${aircraftId}/seat-config`, {
      method: 'PUT',
      body: JSON.stringify({ config })
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

  // Obtener detalle de un vuelo por ID
  async getFlight(flightId: number): Promise<any> {
    try {
      const response = await this.makeRequest(`/api/airline/flights/${flightId}`, {
        method: 'GET'
      })
      return response
    } catch (error) {
      console.error('Error obteniendo vuelo por id:', error)
      throw error
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

  // Obtener ticket por id
  async getTicketById(ticketId: number): Promise<any> {
    return this.makeRequest(`/api/airline/tickets/${ticketId}`, { method: 'GET' })
  }

  // Consultar por código de reservación
  async getTicketByCode(code: string): Promise<any> {
    return this.makeRequest(`/api/airline/tickets/code/${code}`, { method: 'GET' })
  }

  // Descargar PDF del ticket
  async downloadTicketPdf(ticketId: number): Promise<Blob> {
    const url = `${this.config.getBaseUrl()}/api/airline/tickets/${ticketId}/pdf`
    const res = await fetch(url, { method: 'GET' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return await res.blob()
  }

  // ===== Reseñas de vuelos =====
  async getFlightReviews(flightId: number, options?: { mode?: 'tree' | 'flat' }): Promise<any> {
    const query = options?.mode ? `?mode=${options.mode}` : ''
    const res = await this.makeRequest(`/api/airline/flights/${flightId}/reviews${query}`, { method: 'GET' })
    return res
  }

  async createFlightReview(flightId: number, payload: { userId: number; rating: number; comment?: string }): Promise<any> {
    return this.makeRequest(`/api/airline/flights/${flightId}/reviews`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  }

  async updateFlightStatus(flightId: number, status: string) {
    return this.makeRequest(`/api/airline/flights/${flightId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    })
  }

  // ===== NUEVOS MÉTODOS PARA GESTIÓN COMPLETA DE VUELOS =====
  
  /**
   * Edita un vuelo completo
   */
  async updateFlight(flightId: number, flightData: any) {
    try {
      const response = await this.makeRequest(`/api/airline/flights/${flightId}`, {
        method: 'PUT',
        body: JSON.stringify(flightData),
      })
      console.log('🔍 Respuesta de updateFlight:', response)
      return response
    } catch (error) {
      console.error('❌ Error en updateFlight:', error)
      throw error
    }
  }

  /**
   * Cancela un vuelo
   */
  async cancelFlight(flightId: number, cancellationReason: string, cancelledBy: number) {
    try {
      const response = await this.makeRequest(`/api/airline/flights/${flightId}/cancel`, {
        method: 'PUT',
        body: JSON.stringify({ cancellationReason, cancelledBy }),
      })
      console.log('🔍 Respuesta de cancelFlight:', response)
      return response
    } catch (error) {
      console.error('❌ Error en cancelFlight:', error)
      throw error
    }
  }

  /**
   * Actualiza solo el estado de un vuelo
   */
  async updateFlightStatusWithUser(flightId: number, status: string, updatedBy: number) {
    try {
      const response = await this.makeRequest(`/api/airline/flights/${flightId}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status, updatedBy }),
      })
      console.log('🔍 Respuesta de updateFlightStatusWithUser:', response)
      return response
    } catch (error) {
      console.error('❌ Error en updateFlightStatusWithUser:', error)
      throw error
    }
  }

  /**
   * Elimina un vuelo
   */
  async deleteFlight(flightId: number) {
    try {
      const response = await this.makeRequest(`/api/airline/flights/${flightId}`, {
        method: 'DELETE',
      })
      console.log('🔍 Respuesta de deleteFlight:', response)
      return response
    } catch (error) {
      console.error('❌ Error en deleteFlight:', error)
      throw error
    }
  }

  /**
   * Crea escalas para un vuelo existente
   */
  async createFlightLeg(flightId: number, legData: any) {
    try {
      const response = await this.makeRequest(`/api/admin/flights/${flightId}/legs`, {
        method: 'POST',
        body: JSON.stringify(legData),
      })
      console.log('🔍 Respuesta de createFlightLeg:', response)
      return response
    } catch (error) {
      console.error('❌ Error en createFlightLeg:', error)
      throw error
    }
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

