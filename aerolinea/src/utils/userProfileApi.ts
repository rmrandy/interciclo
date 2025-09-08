// API Client para el Panel de Usuario
const API_BASE_URL = 'http://localhost:8080/api/user';

export interface UserProfile {
  idUser: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  birthDate?: string;
  country?: string;
  documentType?: string;
  documentNumber?: string;
  passportNumber?: string;
  preferences?: any;
}

export interface UserSettings {
  notifications: {
    email: boolean;
    push: boolean;
    sms: boolean;
  };
  language: string;
  theme: string;
  privacy: {
    shareProfile: boolean;
    showActivity: boolean;
    analytics: boolean;
  };
  timezone: string;
  dateFormat: string;
  currency: string;
}

export interface UserSecuritySettings {
  twoFactorAuth: boolean;
  loginNotifications: boolean;
  sessionTimeout: boolean;
}

export interface UserActivity {
  id: number;
  type: string;
  description: string;
  timestamp: string;
  details: any;
}

export interface UserSession {
  id: string;
  deviceType: string;
  deviceName: string;
  browser: string;
  os: string;
  location: string;
  lastActivity: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

class UserProfileApi {
  private async makeRequest<T>(
    endpoint: string, 
    method: 'GET' | 'POST' = 'POST', 
    data?: any
  ): Promise<ApiResponse<T>> {
    try {
      const url = `${API_BASE_URL}${endpoint}`;
      const options: RequestInit = {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
      };

      if (data && method === 'POST') {
        options.body = JSON.stringify(data);
      }

      const response = await fetch(url, options);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result = await response.json();
      return result;
    } catch (error) {
      console.error(`Error en API ${endpoint}:`, error);
      throw error;
    }
  }

  // Obtener perfil del usuario
  async getUserProfile(userId: number): Promise<UserProfile> {
    const response = await this.makeRequest<{ profile: UserProfile }>('/profile', 'POST', { userId });
    return response.data.profile;
  }

  // Actualizar perfil del usuario
  async updateUserProfile(profileData: Partial<UserProfile>): Promise<UserProfile> {
    const response = await this.makeRequest<{ profile: UserProfile }>('/profile', 'POST', profileData);
    return response.data.profile;
  }

  // Obtener configuraciones del usuario
  async getUserSettings(userId: number): Promise<UserSettings> {
    const response = await this.makeRequest<{ settings: UserSettings }>('/settings', 'POST', { userId });
    return response.data.settings;
  }

  // Actualizar configuraciones del usuario
  async updateUserSettings(settings: Partial<UserSettings>): Promise<UserSettings> {
    const response = await this.makeRequest<{ settings: UserSettings }>('/settings', 'POST', settings);
    return response.data.settings;
  }

  // Obtener configuraciones de seguridad
  async getUserSecuritySettings(userId: number): Promise<UserSecuritySettings> {
    const response = await this.makeRequest<{ security: UserSecuritySettings }>('/security', 'POST', { userId });
    return response.data.security;
  }

  // Actualizar configuraciones de seguridad
  async updateUserSecuritySettings(security: Partial<UserSecuritySettings>): Promise<UserSecuritySettings> {
    const response = await this.makeRequest<{ security: UserSecuritySettings }>('/security', 'POST', security);
    return response.data.security;
  }

  // Obtener historial de actividad
  async getUserActivityHistory(userId: number): Promise<UserActivity[]> {
    const response = await this.makeRequest<{ activities: UserActivity[] }>('/activity', 'POST', { userId });
    return response.data.activities;
  }

  // Limpiar historial de actividad
  async clearUserActivityHistory(userId: number): Promise<void> {
    await this.makeRequest('/activity', 'POST', { userId, action: 'clear' });
  }

  // Obtener sesiones activas
  async getUserActiveSessions(userId: number): Promise<UserSession[]> {
    const response = await this.makeRequest<{ sessions: UserSession[] }>('/sessions', 'POST', { userId });
    return response.data.sessions;
  }

  // Terminar sesión específica
  async terminateUserSession(userId: number, sessionId: string): Promise<void> {
    await this.makeRequest('/sessions', 'POST', { userId, sessionId, action: 'terminate' });
  }

  // Terminar todas las sesiones
  async terminateAllUserSessions(userId: number): Promise<void> {
    await this.makeRequest('/sessions', 'POST', { userId, action: 'terminateAll' });
  }

  // Cambiar contraseña
  async changeUserPassword(userId: number, currentPassword: string, newPassword: string): Promise<void> {
    await this.makeRequest('/security', 'POST', {
      userId,
      currentPassword,
      newPassword,
      action: 'changePassword'
    });
  }

  // Probar conexión con el servidor
  async testConnection(): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/test`);
      return response.ok;
    } catch (error) {
      console.error('Error probando conexión:', error);
      return false;
    }
  }
}

// Instancia singleton
export const userProfileApi = new UserProfileApi();

// Función para verificar si el servidor está disponible
export const isServerAvailable = async (): Promise<boolean> => {
  try {
    return await userProfileApi.testConnection();
  } catch {
    return false;
  }
};

// Función para obtener la URL base de la API
export const getApiBaseUrl = (): string => {
  return API_BASE_URL;
};
