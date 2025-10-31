import mongoose from 'mongoose';
import dotenv from 'dotenv';

dotenv.config();

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/agencia-viajes';

async function main() {
  try {
    await mongoose.connect(MONGODB_URI, {
      maxPoolSize: 10,
      serverSelectionTimeoutMS: 10000,
    });

    const db = mongoose.connection.db!;

    const collections = await db.listCollections().toArray();
    const existing = new Set(collections.map((c: any) => c.name));

    const needed = ['users', 'airports', 'flights', 'bookings', 'airline_providers'];
    for (const name of needed) {
      if (!existing.has(name)) {
        await db.createCollection(name);
        console.log(`✅ Colección creada: ${name}`);
      } else {
        console.log(`ℹ️  Colección ya existe: ${name}`);
      }
    }

    // Índices mínimos
    await db.collection('users').createIndex({ email: 1 }, { unique: true });
    await db.collection('airports').createIndex({ code: 1 }, { unique: true });
    await db.collection('flights').createIndex({ flightNumber: 1 }, { unique: true });
    await db.collection('bookings').createIndex({ bookingReference: 1 }, { unique: true });

    console.log('🎯 Inicialización completada.');
  } catch (err) {
    console.error('❌ Error inicializando la base de datos:', err);
    process.exit(1);
  } finally {
    await mongoose.disconnect();
  }
}

main();


