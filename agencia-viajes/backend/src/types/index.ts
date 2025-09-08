import { Request } from 'express';

export interface IUser {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  dateOfBirth?: Date;
  role: 'user' | 'admin' | 'employee';
  isActive: boolean;
}

export interface IAirport {
  _id?: string;
  code: string;
  name: string;
  city: string;
  country: string;
  coordinates: {
    latitude: number;
    longitude: number;
  };
}

export interface IFlight {
  _id?: string;
  flightNumber: string;
  origin: IAirport;
  destination: IAirport;
  departure: Date;
  arrival: Date;
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
  createdAt?: Date;
  updatedAt?: Date;
}

export interface IPassenger {
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  passportNumber?: string;
  seatCategory: 'economy' | 'business' | 'firstClass';
  seatNumber: string;
}

export interface IBooking {
  _id?: string;
  userId: string;
  flightId: string;
  passengers: IPassenger[];
  totalAmount: number;
  status: 'pending' | 'confirmed' | 'cancelled' | 'completed';
  paymentMethod: 'credit_card' | 'debit_card' | 'paypal';
  paymentStatus: 'pending' | 'completed' | 'failed' | 'refunded';
  bookingReference: string;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface IAuthRequest extends Request {
  user?: IUser;
}

export interface ILoginRequest {
  email: string;
  password: string;
}

export interface IRegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  dateOfBirth?: string;
}

export interface IFlightSearchRequest {
  origin: string;
  destination: string;
  departureDate: string;
  returnDate?: string;
  passengers: number;
  seatCategory?: 'economy' | 'business' | 'firstClass';
}

export interface IBookingRequest {
  flightId: string;
  passengers: Omit<IPassenger, 'seatNumber'>[];
  paymentMethod: 'credit_card' | 'debit_card' | 'paypal';
}
