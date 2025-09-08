import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import dotenv from 'dotenv';
import { errorHandler, notFound } from './middleware/errorHandler';

// Importar rutas
import authRoutes from './routes/auth';
import flightRoutes from './routes/flights';
import bookingRoutes from './routes/bookings';
import airportRoutes from './routes/airports';

// Cargar variables de entorno
dotenv.config();

const app = express();

// Middleware de seguridad
app.use(helmet());

// CORS
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://localhost:3000',
  credentials: true
}));

// Logging
if (process.env.NODE_ENV === 'development') {
  app.use(morgan('dev'));
} else {
  app.use(morgan('combined'));
}

// Body parser
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Ruta de salud
app.get('/health', (req, res) => {
  res.json({
    success: true,
    message: 'Servidor funcionando correctamente',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || 'development'
  });
});

// Rutas de la API
app.use('/api/auth', authRoutes);
app.use('/api/flights', flightRoutes);
app.use('/api/bookings', bookingRoutes);
app.use('/api/airports', airportRoutes);

// Ruta de bienvenida
app.get('/', (req, res) => {
  res.json({
    success: true,
    message: 'Bienvenido a la API de Agencia de Viajes',
    version: '1.0.0',
    endpoints: {
      auth: '/api/auth',
      flights: '/api/flights',
      bookings: '/api/bookings',
      airports: '/api/airports',
      health: '/health'
    }
  });
});

// Middleware de manejo de errores
app.use(notFound);
app.use(errorHandler);

export default app;
