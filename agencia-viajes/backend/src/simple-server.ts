import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';

// Cargar variables de entorno
dotenv.config();

const app = express();
const PORT = parseInt(process.env.PORT || '5001', 10);

// CORS - Configuración más flexible para desarrollo
const corsOptions = {
  origin: function (origin: string | undefined, callback: Function) {
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
app.options('*', cors(corsOptions));

// Body parser
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

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
process.on('unhandledRejection', (err: Error) => {
  console.error('❌ Error no manejado:', err.message);
  server.close(() => {
    process.exit(1);
  });
});

process.on('uncaughtException', (err: Error) => {
  console.error('❌ Excepción no capturada:', err.message);
  server.close(() => {
    process.exit(1);
  });
});
