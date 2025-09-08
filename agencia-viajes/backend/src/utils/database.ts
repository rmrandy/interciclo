import mongoose from 'mongoose';

const connectDB = async (): Promise<void> => {
  try {
    const mongoURI = process.env.MONGODB_URI || 'mongodb://localhost:27017/agencia-viajes';
    
    const conn = await mongoose.connect(mongoURI, {
      // Opciones de conexión para MongoDB
      maxPoolSize: 10, // Mantener hasta 10 conexiones en el pool
      serverSelectionTimeoutMS: 5000, // Mantener intentando enviar operaciones por 5 segundos
      socketTimeoutMS: 45000, // Cerrar sockets después de 45 segundos de inactividad
      bufferCommands: false, // Deshabilitar el buffering de mongoose
      bufferMaxEntries: 0 // Deshabilitar el buffering de mongoose
    });

    console.log(`✅ MongoDB conectado: ${conn.connection.host}`);
    
    // Manejar eventos de conexión
    mongoose.connection.on('error', (err) => {
      console.error('❌ Error de conexión a MongoDB:', err);
    });

    mongoose.connection.on('disconnected', () => {
      console.log('⚠️ MongoDB desconectado');
    });

    // Cerrar conexión cuando la aplicación se cierre
    process.on('SIGINT', async () => {
      await mongoose.connection.close();
      console.log('🔌 Conexión a MongoDB cerrada por terminación de la aplicación');
      process.exit(0);
    });

  } catch (error) {
    console.error('❌ Error al conectar con MongoDB:', error);
    process.exit(1);
  }
};

export default connectDB;
