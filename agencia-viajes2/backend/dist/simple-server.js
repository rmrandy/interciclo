"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const cors_1 = __importDefault(require("cors"));
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
const app = (0, express_1.default)();
const PORT = parseInt(process.env.PORT || '5001', 10);
const corsOptions = {
    origin: function (origin, callback) {
        if (!origin)
            return callback(null, true);
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
        }
        else {
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
app.use((0, cors_1.default)(corsOptions));
app.options('*', (0, cors_1.default)(corsOptions));
app.use(express_1.default.json({ limit: '10mb' }));
app.use(express_1.default.urlencoded({ extended: true, limit: '10mb' }));
app.get('/health', (req, res) => {
    res.json({
        success: true,
        message: 'Servidor funcionando correctamente',
        timestamp: new Date().toISOString(),
        environment: process.env.NODE_ENV || 'development',
        port: PORT
    });
});
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
app.post('/api/auth/register', (req, res) => {
    try {
        const { firstName, lastName, email, password, phone, dateOfBirth } = req.body;
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
        const user = {
            id: Date.now().toString(),
            firstName,
            lastName,
            email,
            phone: phone || '',
            dateOfBirth: dateOfBirth || '',
            createdAt: new Date().toISOString()
        };
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
    }
    catch (error) {
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
        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: 'Email y contraseña son requeridos'
            });
        }
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
    }
    catch (error) {
        console.error('❌ Error en login:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});
app.get('/api/auth/profile', (req, res) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({
                success: false,
                message: 'Token de autorización requerido'
            });
        }
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
    }
    catch (error) {
        console.error('❌ Error obteniendo perfil:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});
const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Servidor ejecutándose en puerto ${PORT}`);
    console.log(`🌍 Entorno: ${process.env.NODE_ENV || 'development'}`);
    console.log(`📊 Health check: http://localhost:${PORT}/health`);
    console.log(`🔗 API Base URL: http://localhost:${PORT}/api`);
    console.log(`🌐 Acceso desde red: http://192.168.0.26:${PORT}/api`);
    console.log(`🔧 Frontend configurado para: ${process.env.FRONTEND_URL || 'http://localhost:3000'}`);
});
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
//# sourceMappingURL=simple-server.js.map