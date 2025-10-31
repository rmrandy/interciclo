"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const app_1 = __importDefault(require("./app"));
const database_1 = __importDefault(require("./utils/database"));
const PORT = parseInt(process.env.PORT || '5000', 10);
(0, database_1.default)();
const server = app_1.default.listen(PORT, '0.0.0.0', () => {
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
    process.exit(1);
});
process.on('SIGTERM', () => {
    console.log('🛑 SIGTERM recibido. Cerrando servidor...');
    server.close(() => {
        console.log('✅ Servidor cerrado');
        process.exit(0);
    });
});
exports.default = server;
//# sourceMappingURL=server.js.map