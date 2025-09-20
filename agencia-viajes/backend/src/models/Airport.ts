import mongoose, { Document, Schema } from 'mongoose';
import { IAirport } from '../types';

export interface IAirportDocument extends Omit<IAirport, '_id'>, Document {}

const AirportSchema = new Schema<IAirportDocument>({
  code: {
    type: String,
    required: [true, 'El código del aeropuerto es requerido'],
    unique: true,
    uppercase: true,
    trim: true,
    length: [3, 'El código debe tener exactamente 3 caracteres']
  },
  name: {
    type: String,
    required: [true, 'El nombre del aeropuerto es requerido'],
    trim: true,
    maxlength: [100, 'El nombre no puede exceder 100 caracteres']
  },
  city: {
    type: String,
    required: [true, 'La ciudad es requerida'],
    trim: true,
    maxlength: [50, 'La ciudad no puede exceder 50 caracteres']
  },
  country: {
    type: String,
    required: [true, 'El país es requerido'],
    trim: true,
    maxlength: [50, 'El país no puede exceder 50 caracteres']
  },
  coordinates: {
    latitude: {
      type: Number,
      required: [true, 'La latitud es requerida'],
      min: [-90, 'La latitud debe estar entre -90 y 90'],
      max: [90, 'La latitud debe estar entre -90 y 90']
    },
    longitude: {
      type: Number,
      required: [true, 'La longitud es requerida'],
      min: [-180, 'La longitud debe estar entre -180 y 180'],
      max: [180, 'La longitud debe estar entre -180 y 180']
    }
  }
}, {
  timestamps: true
});

// Índices
AirportSchema.index({ code: 1 });
AirportSchema.index({ city: 1, country: 1 });
AirportSchema.index({ 'coordinates.latitude': 1, 'coordinates.longitude': 1 });

export default mongoose.model<IAirportDocument>('Airport', AirportSchema);
