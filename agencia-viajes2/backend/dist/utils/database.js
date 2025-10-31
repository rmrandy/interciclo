"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const mongoose_1 = __importDefault(require("mongoose"));
const connectDB = async () => {
    try {
        const mongoURI = process.env.MONGODB_URI || 'mongodb://localhost:27017/agencia-viajes';
        const conn = await mongoose_1.default.connect(mongoURI, {
            maxPoolSize: 10,
            serverSelectionTimeoutMS: 5000,
            socketTimeoutMS: 45000,
            bufferCommands: false
        });
        console.log(`✅ MongoDB conectado: ${conn.connection.host}`);
        mongoose_1.default.connection.on('error', (err) => {
            console.error('❌ Error de conexión a MongoDB:', err);
        });
        mongoose_1.default.connection.on('disconnected', () => {
            console.log('⚠️ MongoDB desconectado');
        });
        process.on('SIGINT', async () => {
            await mongoose_1.default.connection.close();
            console.log('🔌 Conexión a MongoDB cerrada por terminación de la aplicación');
            process.exit(0);
        });
    }
    catch (error) {
        console.error('❌ Error al conectar con MongoDB:', error);
        process.exit(1);
    }
};
exports.default = connectDB;
//# sourceMappingURL=database.js.map