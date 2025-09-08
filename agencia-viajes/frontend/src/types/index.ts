export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  dateOfBirth?: string;
  role: 'user' | 'admin' | 'employee';
  isActive: boolean;
  createdAt?: string;
}

export interface Airport {
  _id: string;
  code: string;
  name: string;
  city: string;
  country: string;
  coordinates: {
    latitude: number;
    longitude: number;
  };
}

export interface Flight {
  _id: string;
  flightNumber: string;
  origin: Airport;
  destination: Airport;
  departure: string;
  arrival: string;
  aircraft: {
    model: string;
    capacity: number;
  };
  inventory: {
    economy: {
      total: number;
      available: number;
      price: number;
    };
    business: {
      total: number;
      available: number;
      price: number;
    };
    firstClass: {
      total: number;
      available: number;
      price: number;
    };
  };
  status: 'scheduled' | 'boarding' | 'departed' | 'arrived' | 'cancelled' | 'delayed';
  createdAt?: string;
  updatedAt?: string;
}

export interface Passenger {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  passportNumber?: string;
  seatCategory: 'economy' | 'business' | 'firstClass';
  seatNumber?: string;
}

export interface Booking {
  _id: string;
  userId: string;
  flightId: string | Flight;
  passengers: Passenger[];
  totalAmount: number;
  status: 'pending' | 'confirmed' | 'cancelled' | 'completed';
  paymentMethod: 'credit_card' | 'debit_card' | 'paypal';
  paymentStatus: 'pending' | 'completed' | 'failed' | 'refunded';
  bookingReference: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  data: {
    user: User;
    token: string;
  };
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  count?: number;
  total?: number;
  page?: number;
  pages?: number;
  errors?: any[];
}

export interface FlightSearchParams {
  origin?: string;
  destination?: string;
  departureDate?: string;
  returnDate?: string;
  passengers?: number;
  seatCategory?: 'economy' | 'business' | 'firstClass';
}

export interface BookingRequest {
  flightId: string;
  passengers: Omit<Passenger, 'seatNumber'>[];
  paymentMethod: 'credit_card' | 'debit_card' | 'paypal';
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  dateOfBirth?: string;
}

export interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  loading: boolean;
  isAuthenticated: boolean;
}
