import mongoose, { Document, Schema } from 'mongoose';
import { IBooking } from '../types';

export interface IBookingDocument extends IBooking, Document {}

const PassengerSchema = new Schema({
  firstName: {
    type: String,
    required: [true, 'El nombre del pasajero es requerido'],
    trim: true,
    maxlength: [50, 'El nombre no puede exceder 50 caracteres']
  },
  lastName: {
    type: String,
    required: [true, 'El apellido del pasajero es requerido'],
    trim: true,
    maxlength: [50, 'El apellido no puede exceder 50 caracteres']
  },
  dateOfBirth: {
    type: Date,
    required: [true, 'La fecha de nacimiento del pasajero es requerida'],
    validate: {
      validator: function(value: Date) {
        return value < new Date();
      },
      message: 'La fecha de nacimiento debe ser anterior a hoy'
    }
  },
  passportNumber: {
    type: String,
    trim: true,
    uppercase: true,
    match: [/^[A-Z0-9]{6,12}$/, 'Formato de pasaporte inválido']
  },
  seatCategory: {
    type: String,
    enum: ['economy', 'business', 'firstClass'],
    required: [true, 'La categoría de asiento es requerida']
  },
  seatNumber: {
    type: String,
    required: [true, 'El número de asiento es requerido'],
    trim: true
  }
}, { _id: false });

const BookingSchema = new Schema<IBookingDocument>({
  userId: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'El ID del usuario es requerido']
  },
  flightId: {
    type: Schema.Types.ObjectId,
    ref: 'Flight',
    required: [true, 'El ID del vuelo es requerido']
  },
  passengers: [PassengerSchema],
  totalAmount: {
    type: Number,
    required: [true, 'El monto total es requerido'],
    min: [0, 'El monto total no puede ser negativo']
  },
  status: {
    type: String,
    enum: ['pending', 'confirmed', 'cancelled', 'completed'],
    default: 'pending'
  },
  paymentMethod: {
    type: String,
    enum: ['credit_card', 'debit_card', 'paypal'],
    required: [true, 'El método de pago es requerido']
  },
  paymentStatus: {
    type: String,
    enum: ['pending', 'completed', 'failed', 'refunded'],
    default: 'pending'
  },
  bookingReference: {
    type: String,
    required: [true, 'La referencia de reserva es requerida'],
    unique: true,
    uppercase: true,
    trim: true,
    match: [/^[A-Z0-9]{6,12}$/, 'Formato de referencia de reserva inválido']
  }
}, {
  timestamps: true
});

// Índices
BookingSchema.index({ userId: 1 });
BookingSchema.index({ flightId: 1 });
BookingSchema.index({ bookingReference: 1 });
BookingSchema.index({ status: 1 });
BookingSchema.index({ paymentStatus: 1 });
BookingSchema.index({ createdAt: -1 });

// Middleware pre-save para generar referencia de reserva
BookingSchema.pre('save', function(next) {
  if (!this.bookingReference) {
    const timestamp = Date.now().toString(36).toUpperCase();
    const random = Math.random().toString(36).substr(2, 4).toUpperCase();
    this.bookingReference = `BK${timestamp}${random}`;
  }
  next();
});

// Middleware pre-save para generar números de asiento
BookingSchema.pre('save', function(next) {
  this.passengers.forEach((passenger, index) => {
    if (!passenger.seatNumber) {
      const timestamp = Date.now();
      passenger.seatNumber = `AUTO-${this.flightId}-${passenger.seatCategory}-${timestamp}-${index}`;
    }
  });
  next();
});

// Virtual para número de pasajeros
BookingSchema.virtual('passengerCount').get(function() {
  return this.passengers.length;
});

// Virtual para verificar si la reserva está activa
BookingSchema.virtual('isActive').get(function() {
  return this.status === 'confirmed' || this.status === 'pending';
});

export default mongoose.model<IBookingDocument>('Booking', BookingSchema);
