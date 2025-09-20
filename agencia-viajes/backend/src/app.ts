import express from 'express';
import type { Request, Response, NextFunction } from 'express';
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
import airlineIntegrationRoutes from './routes/airline';
import infoPagesRoutes from './routes/infoPages';

// Cargar variables de entorno
dotenv.config();

const app = express();

// CORS: permitir cualquier origen/puerto (agencia - petición del cliente)
app.use(cors({
  origin: true,
  credentials: true,
  methods: ['GET','POST','PUT','PATCH','DELETE','OPTIONS','HEAD'],
  allowedHeaders: ['Content-Type','Authorization','X-Requested-With','Accept','Origin'],
  exposedHeaders: ['Authorization']
}));
// Headers CORS explícitos y preflight universal (por si algún proxy/helmet interfiere)
app.use((req: Request, res: Response, next: NextFunction): void => {
  const origin = req.headers.origin as string | undefined;
  if (origin) res.setHeader('Access-Control-Allow-Origin', origin);
  else res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Vary', 'Origin');
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With, Accept, Origin');
  // Chrome Private Network Access (PNA) preflight support
  if (req.headers['access-control-request-private-network'] === 'true') {
    res.setHeader('Access-Control-Allow-Private-Network', 'true');
  }
  if (req.method === 'OPTIONS') { res.sendStatus(204); return; }
  next();
});
app.options('*', cors());

// Middleware de seguridad (después de CORS)
app.use(helmet());


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
app.use('/api/integrations/airline', airlineIntegrationRoutes);
app.use('/api/info-pages', infoPagesRoutes);

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
