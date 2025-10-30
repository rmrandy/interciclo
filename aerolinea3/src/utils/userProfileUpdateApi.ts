import { getUserProfileUpdateUrl } from '../config/ports';

// API Client para actualización de perfil de usuario
export interface UserProfileUpdateRequest {
  userId: number;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
}

export interface UserProfileResponse {
  success: boolean;
  message?: string;
  error?: string;
  user?: {
    idUser: number;
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    cui: number;
    birthDate: string;
    createdAt: string;
  };
}

class UserProfileUpdateApi {
  private baseUrl: string;
  private timeout: number;

  constructor() {
    this.baseUrl = getUserProfileUpdateUrl();
    this.timeout = 5000;
  }

  private async makeRequest(
    endpoint: string,
    method: 'GET' | 'PUT' | 'POST' | 'DELETE',
    data?: any
  ): Promise<Response> {
    const url = `${this.baseUrl}${endpoint}`;
    
    const options: RequestInit = {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      signal: AbortSignal.timeout(this.timeout),
    };

    if (data && (method === 'PUT' || method === 'POST')) {
      options.body = JSON.stringify(data);
    }

    try {
      const response = await fetch(url, options);
      return response;
    } catch (error) {
      if (error instanceof Error && error.name === 'TimeoutError') {
        throw new Error('Timeout: La solicitud tardó demasiado en completarse');
      }
      throw error;
    }
  }

  /**
   * Actualiza el perfil del usuario
   */
  async updateUserProfile(request: UserProfileUpdateRequest): Promise<UserProfileResponse> {
    try {
      const response = await this.makeRequest('/api/airline/user/profile', 'PUT', request);
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `Error HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error actualizando perfil:', error);
      throw error;
    }
  }

  /**
   * Obtiene el perfil del usuario
   */
  async getUserProfile(userId: number): Promise<UserProfileResponse> {
    try {
      const response = await this.makeRequest(`/api/airline/user/profile?userId=${userId}`, 'GET');
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `Error HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error obteniendo perfil:', error);
      throw error;
    }
  }

  /**
   * Prueba la conexión con el servidor
   */
  async testConnection(): Promise<boolean> {
    try {
      // Usar el endpoint de health check del sistema principal
      const response = await this.makeRequest('/api/health', 'GET');
      return response.ok;
    } catch (error) {
      return false;
    }
  }
}

// Exportar instancia singleton
export const userProfileUpdateApi = new UserProfileUpdateApi();

// Función para verificar si el servidor está disponible
export const isProfileUpdateServerAvailable = async (): Promise<boolean> => {
  try {
    return await userProfileUpdateApi.testConnection();
  } catch {
    return false;
  }
};
