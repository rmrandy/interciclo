"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const cors_1 = __importDefault(require("cors"));
const helmet_1 = __importDefault(require("helmet"));
const morgan_1 = __importDefault(require("morgan"));
const dotenv_1 = __importDefault(require("dotenv"));
const errorHandler_1 = require("./middleware/errorHandler");
const auth_1 = __importDefault(require("./routes/auth"));
const flights_1 = __importDefault(require("./routes/flights"));
const bookings_1 = __importDefault(require("./routes/bookings"));
const airports_1 = __importDefault(require("./routes/airports"));
dotenv_1.default.config();
const app = (0, express_1.default)();
app.use((0, helmet_1.default)());
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
if (process.env.NODE_ENV === 'development') {
    app.use((0, morgan_1.default)('dev'));
}
else {
    app.use((0, morgan_1.default)('combined'));
}
app.use(express_1.default.json({ limit: '10mb' }));
app.use(express_1.default.urlencoded({ extended: true, limit: '10mb' }));
app.get('/health', (req, res) => {
    res.json({
        success: true,
        message: 'Servidor funcionando correctamente',
        timestamp: new Date().toISOString(),
        environment: process.env.NODE_ENV || 'development'
    });
});
app.use('/api/auth', auth_1.default);
app.use('/api/flights', flights_1.default);
app.use('/api/bookings', bookings_1.default);
app.use('/api/airports', airports_1.default);
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
app.use(errorHandler_1.notFound);
app.use(errorHandler_1.errorHandler);
exports.default = app;
//# sourceMappingURL=app.js.map