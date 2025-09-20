import app from './app';
import connectDB from './utils/database';

const PORT = parseInt(process.env.PORT || '5000', 10);

// Conectar a la base de datos
connectDB();

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
  process.exit(1);
});

// Manejo de señales de terminación
process.on('SIGTERM', () => {
  console.log('🛑 SIGTERM recibido. Cerrando servidor...');
  server.close(() => {
    console.log('✅ Servidor cerrado');
    process.exit(0);
  });
});

export default server;
