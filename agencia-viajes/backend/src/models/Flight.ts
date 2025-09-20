import mongoose, { Document, Schema } from 'mongoose';
import { IFlight } from '../types';

export interface IFlightDocument extends Omit<IFlight, '_id'>, Document {}

const FlightSchema = new Schema<IFlightDocument>({
  flightNumber: {
    type: String,
    required: [true, 'El número de vuelo es requerido'],
    unique: true,
    uppercase: true,
    trim: true,
    match: [/^[A-Z]{2,3}\d{3,4}$/, 'Formato de número de vuelo inválido']
  },
  origin: {
    type: Schema.Types.ObjectId,
    ref: 'Airport',
    required: [true, 'El aeropuerto de origen es requerido']
  },
  destination: {
    type: Schema.Types.ObjectId,
    ref: 'Airport',
    required: [true, 'El aeropuerto de destino es requerido']
  },
  departure: {
    type: Date,
    required: [true, 'La fecha de salida es requerida']
  },
  arrival: {
    type: Date,
    required: [true, 'La fecha de llegada es requerida']
  },
  aircraft: {
    model: {
      type: String,
      required: [true, 'El modelo de aeronave es requerido'],
      trim: true,
      maxlength: [50, 'El modelo no puede exceder 50 caracteres']
    },
    capacity: {
      type: Number,
      required: [true, 'La capacidad es requerida'],
      min: [1, 'La capacidad debe ser al menos 1']
    }
  },
  inventory: {
    economy: {
      total: {
        type: Number,
        required: true,
        min: [0, 'El total de asientos económicos no puede ser negativo']
      },
      available: {
        type: Number,
        required: true,
        min: [0, 'Los asientos económicos disponibles no pueden ser negativos']
      },
      price: {
        type: Number,
        required: true,
        min: [0, 'El precio no puede ser negativo']
      }
    },
    business: {
      total: {
        type: Number,
        required: true,
        min: [0, 'El total de asientos ejecutivos no puede ser negativo']
      },
      available: {
        type: Number,
        required: true,
        min: [0, 'Los asientos ejecutivos disponibles no pueden ser negativos']
      },
      price: {
        type: Number,
        required: true,
        min: [0, 'El precio no puede ser negativo']
      }
    },
    firstClass: {
      total: {
        type: Number,
        required: true,
        min: [0, 'El total de asientos de primera clase no puede ser negativo']
      },
      available: {
        type: Number,
        required: true,
        min: [0, 'Los asientos de primera clase disponibles no pueden ser negativos']
      },
      price: {
        type: Number,
        required: true,
        min: [0, 'El precio no puede ser negativo']
      }
    }
  },
  status: {
    type: String,
    enum: ['scheduled', 'boarding', 'departed', 'arrived', 'cancelled', 'delayed'],
    default: 'scheduled'
  }
}, {
  timestamps: true
});

// Índices
FlightSchema.index({ flightNumber: 1 });
FlightSchema.index({ origin: 1, destination: 1 });
FlightSchema.index({ departure: 1 });
FlightSchema.index({ status: 1 });
FlightSchema.index({ 'inventory.economy.available': 1 });
FlightSchema.index({ 'inventory.business.available': 1 });
FlightSchema.index({ 'inventory.firstClass.available': 1 });

// Middleware pre-save para validar disponibilidad
FlightSchema.pre('save', function(next) {
  const doc = this as any;
  const inventory = doc.inventory;
  
  // Validar que los asientos disponibles no excedan el total
  if (inventory.economy.available > inventory.economy.total) {
    return next(new Error('Los asientos económicos disponibles no pueden exceder el total'));
  }
  if (inventory.business.available > inventory.business.total) {
    return next(new Error('Los asientos ejecutivos disponibles no pueden exceder el total'));
  }
  if (inventory.firstClass.available > inventory.firstClass.total) {
    return next(new Error('Los asientos de primera clase disponibles no pueden exceder el total'));
  }
  
  next();
});

// Virtual para duración del vuelo
FlightSchema.virtual('duration').get(function() {
  const doc = this as any;
  return doc.arrival.getTime() - doc.departure.getTime();
});

// Virtual para verificar si hay asientos disponibles
FlightSchema.virtual('hasAvailableSeats').get(function() {
  const doc = this as any;
  const inventory = doc.inventory;
  return inventory.economy.available > 0 || 
         inventory.business.available > 0 || 
         inventory.firstClass.available > 0;
});

export default mongoose.model<IFlightDocument>('Flight', FlightSchema);
