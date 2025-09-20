const express = require('express');
const cors = require('cors');
const axios = require('axios');
const mongoose = require('mongoose');
require('dotenv').config();

const app = express();
const PORT = parseInt(process.env.PORT || '5001', 10);
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/agencia-viajes';

// CORS - Configuración más flexible para desarrollo
const corsOptions = {
  origin: function (origin, callback) {
    // Permitir requests sin origin (como mobile apps o Postman)
    if (!origin) return callback(null, true);
    
    const allowedOrigins = [
      process.env.FRONTEND_URL || 'http://localhost:3000',
      'http://192.168.0.26:3000',
      'http://localhost:3000',
      'http://127.0.0.1:3000',
      'http://192.168.0.26:3000/',
      'http://localhost:3000/',
      'http://127.0.0.1:3000/'
    ];
    
    if (allowedOrigins.indexOf(origin) !== -1) {
      console.log('✅ CORS: Origin permitido:', origin);
      callback(null, true);
    } else {
      console.log('❌ CORS: Origin no permitido:', origin);
      console.log('🔍 Origins permitidos:', allowedOrigins);
      callback(new Error('No permitido por CORS'));
    }
  },
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'PATCH'],
  allowedHeaders: [
    'Content-Type', 
    'Authorization', 
    'X-Requested-With',
    'Accept',
    'Origin',
    'Access-Control-Request-Method',
    'Access-Control-Request-Headers'
  ],
  exposedHeaders: ['Authorization'],
  optionsSuccessStatus: 200
};

app.use(cors(corsOptions));

// Middleware adicional para manejar preflight requests
// app.options('/*', cors(corsOptions)); // Comentado para evitar errores

// Body parser
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Conexión Mongo (para airline_providers)
async function connectMongo() {
  try {
    if (mongoose.connection.readyState === 1) return;
    await mongoose.connect(MONGODB_URI, { maxPoolSize: 10, serverSelectionTimeoutMS: 8000 });
    console.log(`✅ Mongo conectado (working-server)`);
  } catch (err) {
    console.error('❌ Error conectando a Mongo:', err.message);
  }
}

// Helpers para proveedores
async function getProviderById(id) {
  await connectMongo();
  const db = mongoose.connection.db;
  try {
    const _id = new mongoose.Types.ObjectId(id);
    return db.collection('airline_providers').findOne({ _id });
  } catch (e) {
    return null;
  }
}

async function callProvider(provider, endpointPath, method, data, params) {
  const base = provider.baseUrl || (provider.ip && provider.port ? `http://${provider.ip}:${provider.port}` : '');
  const instance = axios.create({ baseURL: base, timeout: 8000 });
  const doCall = () => instance.request({ url: endpointPath, method, data, params });
  const attempts = 0;
  const backoff = 300;
  let lastErr;
  for (let i = 0; i <= attempts; i++) {
    try { return await doCall(); } catch (e) { lastErr = e; if (i < attempts) await new Promise(r => setTimeout(r, backoff)); }
  }
  throw lastErr;
}

// Ruta de salud
app.get('/health', (req, res) => {
  res.json({
    success: true,
    message: 'Servidor funcionando correctamente',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || 'development',
    port: PORT
  });
});

// Ruta de bienvenida
app.get('/', (req, res) => {
  res.json({
    success: true,
    message: 'Bienvenido a la API de Agencia de Viajes',
    version: '1.0.0',
    endpoints: {
      health: '/health',
      auth: '/api/auth'
    }
  });
});

// Datos dummy de vuelos expandidos
const dummyFlights = [
  {
    _id: '677f1234567890abcdef0001',
    flightNumber: 'AV1234',
    airline: 'Avianca',
    origin: { code: 'BOG', name: 'El Dorado', city: 'Bogotá' },
    destination: { code: 'MEX', name: 'Benito Juárez', city: 'Ciudad de México' },
    departure: new Date('2025-01-15T08:30:00Z'),
    arrival: new Date('2025-01-15T14:45:00Z'),
    duration: '6h 15m',
    price: { economy: 420, business: 1200, firstClass: 2800 },
    availability: { economy: 45, business: 12, firstClass: 4 },
    aircraft: 'A320',
    stops: 0
  },
  {
    _id: '677f1234567890abcdef0002',
    flightNumber: 'LA8901',
    airline: 'LATAM',
    origin: { code: 'LIM', name: 'Jorge Chávez', city: 'Lima' },
    destination: { code: 'SCL', name: 'Arturo Merino Benítez', city: 'Santiago' },
    departure: new Date('2025-01-16T10:15:00Z'),
    arrival: new Date('2025-01-16T13:30:00Z'),
    duration: '3h 15m',
    price: { economy: 280, business: 850, firstClass: 1900 },
    availability: { economy: 32, business: 8, firstClass: 2 },
    aircraft: 'B737',
    stops: 0
  },
  {
    _id: '677f1234567890abcdef0003',
    flightNumber: 'IB6754',
    airline: 'Iberia',
    origin: { code: 'MAD', name: 'Madrid-Barajas', city: 'Madrid' },
    destination: { code: 'BOG', name: 'El Dorado', city: 'Bogotá' },
    departure: new Date('2025-01-20T22:10:00Z'),
    arrival: new Date('2025-01-21T05:45:00Z'),
    duration: '9h 35m',
    price: { economy: 650, business: 1800, firstClass: 3500 },
    availability: { economy: 67, business: 15, firstClass: 6 },
    aircraft: 'A350',
    stops: 0
  },
  {
    _id: '677f1234567890abcdef0004',
    flightNumber: 'CM2156',
    airline: 'Copa Airlines',
    origin: { code: 'GUA', name: 'La Aurora', city: 'Guatemala' },
    destination: { code: 'MIA', name: 'Miami International', city: 'Miami' },
    departure: new Date('2025-01-18T06:45:00Z'),
    arrival: new Date('2025-01-18T11:20:00Z'),
    duration: '4h 35m',
    price: { economy: 380, business: 950, firstClass: 2200 },
    availability: { economy: 58, business: 16, firstClass: 8 },
    aircraft: 'B737 MAX',
    stops: 0
  },
  {
    _id: '677f1234567890abcdef0005',
    flightNumber: 'AA4567',
    airline: 'American Airlines',
    origin: { code: 'MIA', name: 'Miami International', city: 'Miami' },
    destination: { code: 'JFK', name: 'John F. Kennedy', city: 'Nueva York' },
    departure: new Date('2025-01-19T14:30:00Z'),
    arrival: new Date('2025-01-19T17:45:00Z'),
    duration: '3h 15m',
    price: { economy: 220, business: 680, firstClass: 1500 },
    availability: { economy: 42, business: 10, firstClass: 4 },
    aircraft: 'A321',
    stops: 0
  },
  {
    _id: '677f1234567890abcdef0006',
    flightNumber: 'AF1890',
    airline: 'Air France',
    origin: { code: 'CDG', name: 'Charles de Gaulle', city: 'París' },
    destination: { code: 'GUA', name: 'La Aurora', city: 'Guatemala' },
    departure: new Date('2025-01-22T11:25:00Z'),
    arrival: new Date('2025-01-22T16:40:00Z'),
    duration: '11h 15m',
    price: { economy: 780, business: 2200, firstClass: 4800 },
    availability: { economy: 89, business: 24, firstClass: 12 },
    aircraft: 'B777',
    stops: 0
  }
];

// Endpoint de búsqueda de vuelos
app.post('/api/flights/search', (req, res) => {
  try {
    const { origin, destination, departureDate, passengers = 1, class: travelClass = 'economy' } = req.body;
    
    let results = [...dummyFlights];
    
    // Filtrar por origen si se proporciona
    if (origin) {
      results = results.filter(f => 
        f.origin.code.toLowerCase().includes(origin.toLowerCase()) ||
        f.origin.city.toLowerCase().includes(origin.toLowerCase())
      );
    }
    
    // Filtrar por destino si se proporciona
    if (destination) {
      results = results.filter(f => 
        f.destination.code.toLowerCase().includes(destination.toLowerCase()) ||
        f.destination.city.toLowerCase().includes(destination.toLowerCase())
      );
    }
    
    // Filtrar por fecha (mismo día)
    if (departureDate) {
      const searchDate = new Date(departureDate);
      results = results.filter(f => {
        const flightDate = new Date(f.departure);
        return flightDate.toDateString() === searchDate.toDateString();
      });
    }
    
    // Filtrar por disponibilidad según número de pasajeros
    results = results.filter(f => f.availability[travelClass] >= passengers);
    
    console.log(`🔍 Búsqueda: ${origin} → ${destination}, ${results.length} vuelos encontrados`);
    
    res.json({
      success: true,
      data: results,
      searchParams: { origin, destination, departureDate, passengers, class: travelClass }
    });
    
  } catch (error) {
    console.error('❌ Error en búsqueda de vuelos:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// Listar todos los vuelos disponibles
app.get('/api/flights', (req, res) => {
  try {
    res.json({
      success: true,
      data: dummyFlights
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Error obteniendo vuelos'
    });
  }
});

// Rutas de autenticación básicas
app.post('/api/auth/register', (req, res) => {
  try {
    const { firstName, lastName, email, password, phone, dateOfBirth } = req.body;
    
    // Validaciones básicas
    if (!firstName || !lastName || !email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Faltan campos requeridos'
      });
    }
    
    if (password.length < 6) {
      return res.status(400).json({
        success: false,
        message: 'La contraseña debe tener al menos 6 caracteres'
      });
    }
    
    // Simular creación de usuario (en producción usarías una base de datos)
    const user = {
      id: Date.now().toString(),
      firstName,
      lastName,
      email,
      phone: phone || '',
      dateOfBirth: dateOfBirth || '',
      createdAt: new Date().toISOString()
    };
    
    // Simular token JWT
    const token = `token_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    console.log('✅ Usuario registrado:', { email, firstName, lastName });
    
    res.status(201).json({
      success: true,
      message: 'Usuario registrado exitosamente',
      data: {
        user,
        token
      }
    });
    
  } catch (error) {
    console.error('❌ Error en registro:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// CRUD de proveedores de aerolíneas
app.get('/api/airline-providers', async (req, res) => {
  try {
    await connectMongo();
    const items = await mongoose.connection.db.collection('airline_providers').find({}).toArray();
    res.json({ success: true, data: items });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error listando proveedores' });
  }
});

app.post('/api/airline-providers', async (req, res) => {
  try {
    await connectMongo();
    const doc = { name: req.body.name, ip: req.body.ip, port: Number(req.body.port), endpoints: req.body.endpoints || { search: '/api/airline/flights' }, createdAt: new Date(), updatedAt: new Date() };
    const r = await mongoose.connection.db.collection('airline_providers').insertOne(doc);
    res.status(201).json({ success: true, data: { ...doc, _id: r.insertedId } });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error creando proveedor' });
  }
});

app.get('/api/airline-providers/:id', async (req, res) => {
  try {
    const item = await getProviderById(req.params.id);
    if (!item) return res.status(404).json({ success: false, message: 'Proveedor no encontrado' });
    res.json({ success: true, data: item });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error obteniendo proveedor' });
  }
});

app.put('/api/airline-providers/:id', async (req, res) => {
  try {
    await connectMongo();
    const updates = { name: req.body.name, ip: req.body.ip, port: Number(req.body.port), endpoints: req.body.endpoints || { search: '/api/airline/flights' }, updatedAt: new Date() };
    const r = await mongoose.connection.db.collection('airline_providers').findOneAndUpdate(
      { _id: new mongoose.Types.ObjectId(req.params.id) }, { $set: updates }, { returnDocument: 'after' }
    );
    if (!r.value) return res.status(404).json({ success: false, message: 'Proveedor no encontrado' });
    res.json({ success: true, data: r.value });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error actualizando proveedor' });
  }
});

app.delete('/api/airline-providers/:id', async (req, res) => {
  try {
    await connectMongo();
    const r = await mongoose.connection.db.collection('airline_providers').deleteOne({ _id: new mongoose.Types.ObjectId(req.params.id) });
    if (!r.deletedCount) return res.status(404).json({ success: false, message: 'Proveedor no encontrado' });
    res.json({ success: true });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error eliminando proveedor' });
  }
});

// Búsqueda de vuelos vía proveedor
app.post('/api/airlines/:id/search', async (req, res) => {
  try {
    const provider = await getProviderById(req.params.id);
    if (!provider) return res.status(404).json({ success: false, message: 'Proveedor no encontrado' });
    const endpoint = (provider.endpoints && provider.endpoints.search) || '/api/airline/flights';
    const response = await callProvider(provider, endpoint, 'POST', req.body, undefined);
    res.json({ success: true, provider: provider._id, data: response.data });
  } catch (e) {
    const msg = e?.response?.data || e.message;
    res.status(502).json({ success: false, message: 'Error consultando proveedor', detail: msg });
  }
});

// =====================
// Páginas informativas
// =====================

// Public: listar páginas publicadas (opcional por categoría)
app.get('/api/info-pages', async (req, res) => {
  try {
    await connectMongo();
    const q = { published: true };
    if (req.query.category) q.category = String(req.query.category);
    const pages = await mongoose.connection.db.collection('info_pages')
      .find(q, { projection: { content: 0 } })
      .sort({ updatedAt: -1 })
      .toArray();
    res.json({ success: true, data: pages });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error listando páginas' });
  }
});

// Public: obtener por slug
app.get('/api/info-pages/:slug', async (req, res) => {
  try {
    await connectMongo();
    const page = await mongoose.connection.db.collection('info_pages').findOne({ slug: req.params.slug, published: true });
    if (!page) return res.status(404).json({ success: false, message: 'Página no encontrada' });
    res.json({ success: true, data: page });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error obteniendo página' });
  }
});

// Admin: listar todas
app.get('/api/admin/info-pages', async (req, res) => {
  try {
    await connectMongo();
    const pages = await mongoose.connection.db.collection('info_pages').find({}).sort({ updatedAt: -1 }).toArray();
    res.json({ success: true, data: pages });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error listando páginas' });
  }
});

// Admin: crear
app.post('/api/admin/info-pages', async (req, res) => {
  try {
    await connectMongo();
    const body = req.body || {};
    const doc = {
      title: body.title || 'Nueva página',
      slug: body.slug || (body.title || '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, ''),
      category: body.category || 'general',
      hero: body.hero || { heading: body.title || 'Título', subheading: '', image: '' },
      content: body.content || { sections: [] },
      published: Boolean(body.published),
      createdAt: new Date(),
      updatedAt: new Date()
    };
    const r = await mongoose.connection.db.collection('info_pages').insertOne(doc);
    res.status(201).json({ success: true, data: { ...doc, _id: r.insertedId } });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error creando página' });
  }
});

// Admin: actualizar
app.put('/api/admin/info-pages/:id', async (req, res) => {
  try {
    await connectMongo();
    const updates = { ...req.body, updatedAt: new Date() };
    const r = await mongoose.connection.db.collection('info_pages').findOneAndUpdate(
      { _id: new mongoose.Types.ObjectId(req.params.id) }, { $set: updates }, { returnDocument: 'after' }
    );
    if (!r.value) return res.status(404).json({ success: false, message: 'Página no encontrada' });
    res.json({ success: true, data: r.value });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error actualizando página' });
  }
});

// Admin: eliminar
app.delete('/api/admin/info-pages/:id', async (req, res) => {
  try {
    await connectMongo();
    const r = await mongoose.connection.db.collection('info_pages').deleteOne({ _id: new mongoose.Types.ObjectId(req.params.id) });
    if (!r.deletedCount) return res.status(404).json({ success: false, message: 'Página no encontrada' });
    res.json({ success: true });
  } catch (e) {
    res.status(500).json({ success: false, message: 'Error eliminando página' });
  }
});

app.post('/api/auth/login', (req, res) => {
  try {
    const { email, password } = req.body;
    
    // Validaciones básicas
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Email y contraseña son requeridos'
      });
    }
    
    // Simular autenticación (en producción verificarías contra la base de datos)
    if (email === 'admin@test.com' && password === '123456') {
      const user = {
        id: '1',
        firstName: 'Admin',
        lastName: 'Test',
        email: 'admin@test.com',
        phone: '',
        dateOfBirth: '',
        createdAt: new Date().toISOString()
      };
      
      const token = `token_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      
      console.log('✅ Usuario autenticado:', { email });
      
      return res.json({
        success: true,
        message: 'Login exitoso',
        data: {
          user,
          token
        }
      });
    }
    
    // Para otros usuarios, simular que existen
    const user = {
      id: Date.now().toString(),
      firstName: 'Usuario',
      lastName: 'Test',
      email,
      phone: '',
      dateOfBirth: '',
      createdAt: new Date().toISOString()
    };
    
    const token = `token_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    console.log('✅ Usuario autenticado:', { email });
    
    res.json({
      success: true,
      message: 'Login exitoso',
      data: {
        user,
        token
      }
    });
    
  } catch (error) {
    console.error('❌ Error en login:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// Ruta para obtener perfil de usuario
app.get('/api/auth/profile', (req, res) => {
  try {
    const authHeader = req.headers.authorization;
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        success: false,
        message: 'Token de autorización requerido'
      });
    }
    
    // Simular usuario autenticado
    const user = {
      id: '1',
      firstName: 'Usuario',
      lastName: 'Test',
      email: 'test@example.com',
      phone: '',
      dateOfBirth: '',
      createdAt: new Date().toISOString()
    };
    
    res.json({
      success: true,
      data: { user }
    });
    
  } catch (error) {
    console.error('❌ Error obteniendo perfil:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// Iniciar servidor
const server = app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Servidor ejecutándose en puerto ${PORT}`);
  console.log(`🌍 Entorno: ${process.env.NODE_ENV || 'development'}`);
  console.log(`📊 Health check: http://localhost:${PORT}/health`);
  console.log(`🔗 API Base URL: http://localhost:${PORT}/api`);
  console.log(`🌐 Acceso desde red: http://192.168.0.26:${PORT}/api`);
  console.log(`🔧 Frontend configurado para: ${process.env.FRONTEND_URL || 'http://localhost:3000'}`);
});

// Manejo de errores no capturados
process.on('unhandledRejection', (err) => {
  console.error('❌ Error no manejado:', err.message);
  server.close(() => {
    process.exit(1);
  });
});

process.on('uncaughtException', (err) => {
  console.error('❌ Excepción no capturada:', err.message);
  server.close(() => {
    process.exit(1);
  });
});
