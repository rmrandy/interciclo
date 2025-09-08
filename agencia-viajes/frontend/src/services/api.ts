import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { 
  AuthResponse, 
  ApiResponse, 
  FlightSearchParams, 
  BookingRequest,
  LoginCredentials,
  RegisterData,
  Flight,
  Booking,
  Airport,
  User
} from '../types';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: process.env.REACT_APP_API_URL || 'http://localhost:5000/api',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Interceptor para agregar token a las peticiones
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Interceptor para manejar respuestas
    this.api.interceptors.response.use(
      (response: AxiosResponse) => {
        return response;
      },
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth endpoints
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await this.api.post('/auth/login', credentials);
    return response.data;
  }

  async register(data: RegisterData): Promise<AuthResponse> {
    const response = await this.api.post('/auth/register', data);
    return response.data;
  }

  async getProfile(): Promise<ApiResponse<{ user: User }>> {
    const response = await this.api.get('/auth/profile');
    return response.data;
  }

  async updateProfile(data: Partial<RegisterData>): Promise<ApiResponse<{ user: User }>> {
    const response = await this.api.put('/auth/profile', data);
    return response.data;
  }

  // Flight endpoints
  async searchFlights(params: FlightSearchParams): Promise<ApiResponse<Flight[]>> {
    const response = await this.api.get('/flights/search', { params });
    return response.data;
  }

  async getFlightById(id: string): Promise<ApiResponse<Flight>> {
    const response = await this.api.get(`/flights/${id}`);
    return response.data;
  }

  async getAllFlights(page = 1, limit = 10): Promise<ApiResponse<Flight[]>> {
    const response = await this.api.get('/flights', { 
      params: { page, limit } 
    });
    return response.data;
  }

  // Airport endpoints
  async getAirports(page = 1, limit = 50): Promise<ApiResponse<Airport[]>> {
    const response = await this.api.get('/airports', { 
      params: { page, limit } 
    });
    return response.data;
  }

  async searchAirports(query: string): Promise<ApiResponse<Airport[]>> {
    const response = await this.api.get(`/airports/search/${query}`);
    return response.data;
  }

  // Booking endpoints
  async createBooking(booking: BookingRequest): Promise<ApiResponse<Booking>> {
    const response = await this.api.post('/bookings', booking);
    return response.data;
  }

  async getUserBookings(page = 1, limit = 10): Promise<ApiResponse<Booking[]>> {
    const response = await this.api.get('/bookings', { 
      params: { page, limit } 
    });
    return response.data;
  }

  async getBookingById(id: string): Promise<ApiResponse<Booking>> {
    const response = await this.api.get(`/bookings/${id}`);
    return response.data;
  }

  async cancelBooking(id: string): Promise<ApiResponse<Booking>> {
    const response = await this.api.put(`/bookings/${id}/cancel`);
    return response.data;
  }

  async confirmBooking(id: string): Promise<ApiResponse<Booking>> {
    const response = await this.api.put(`/bookings/${id}/confirm`);
    return response.data;
  }

  // Health check
  async healthCheck(): Promise<ApiResponse<any>> {
    const response = await this.api.get('/health');
    return response.data;
  }
}

const apiService = new ApiService();
export default apiService;
