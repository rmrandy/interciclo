import mongoose from 'mongoose';

function getLocalIp(): string {
  try {
    const os = require('os');
    const ifaces = os.networkInterfaces();
    for (const name of Object.keys(ifaces)) {
      const list = ifaces[name] || [];
      for (const i of list) {
        if (i && i.family === 'IPv4' && !i.internal) {
          return i.address;
        }
      }
    }
  } catch {}
  return '127.0.0.1';
}

async function tryConnect(uri: string, timeoutMs: number) {
  return mongoose.connect(uri, {
    maxPoolSize: 10,
    serverSelectionTimeoutMS: timeoutMs,
    socketTimeoutMS: 45000,
    bufferCommands: false
  });
}

const connectDB = async (): Promise<void> => {
  try {
    const host = (process.env.MONGO_HOST || '').trim() || getLocalIp();
    const port = (process.env.MONGO_PORT || '').trim();
    const db   = (process.env.MONGO_DB || '').trim() || 'agencia-viajes';
    const explicit = (process.env.MONGODB_URI || '').trim();

    const candidates: string[] = [];
    if (explicit) candidates.push(explicit);
    if (host && port) candidates.push(`mongodb://${host}:${port}/${db}`);
    // mismos host/IP con puertos comunes
    candidates.push(`mongodb://${host}:27017/${db}`);
    candidates.push(`mongodb://${host}:5001/${db}`);
    // localhost por si corre local
    candidates.push(`mongodb://127.0.0.1:27017/${db}`);
    candidates.push(`mongodb://127.0.0.1:5001/${db}`);

    let connected = false;
    let lastErr: any = null;
    for (const uri of Array.from(new Set(candidates))) {
      try {
        console.log(`🔎 Intentando conectar a MongoDB: ${uri}`);
        const conn = await tryConnect(uri, 4000);
        console.log(`✅ MongoDB conectado: ${conn.connection.host}`);
        connected = true;
        break;
      } catch (e) {
        lastErr = e;
        console.warn(`⚠️ Falló conexión a ${uri}: ${(e as Error)?.message}`);
        try { await mongoose.disconnect(); } catch {}
      }
    }
    if (!connected) {
      throw lastErr || new Error('No se pudo conectar a ninguna URI de Mongo');
    }
    
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
