import mongoose from 'mongoose';
import dotenv from 'dotenv';
import bcrypt from 'bcryptjs';

dotenv.config();

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/agencia-viajes';

function generateBookingRef(): string {
  return `BK${Date.now().toString(36).toUpperCase()}${Math.random().toString(36).substr(2, 4).toUpperCase()}`;
}

async function upsertOne(collection: string, filter: any, doc: any) {
  const db = mongoose.connection.db!;
  await db.collection(collection).updateOne(filter, { $setOnInsert: doc }, { upsert: true });
}

async function main() {
  try {
    await mongoose.connect(MONGODB_URI, {
      maxPoolSize: 10,
      serverSelectionTimeoutMS: 10000,
    });

    const db = mongoose.connection.db!;

    // 1) Admin user
    const adminEmail = 'admin@agencia.com';
    const adminExists = await db.collection('users').findOne({ email: adminEmail });
    if (!adminExists) {
      const passwordHash = await bcrypt.genSalt(12).then((salt) => bcrypt.hash('Admin123', salt));
      await db.collection('users').insertOne({
        firstName: 'Admin',
        lastName: 'User',
        email: adminEmail,
        password: passwordHash,
        role: 'admin',
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      });
      console.log('✅ Usuario admin creado: admin@agencia.com / Admin123');
    } else {
      console.log('ℹ️  Usuario admin ya existe');
    }

    // 2) Airports (upsert por code)
    const airports = [
      { code: 'BOG', name: 'El Dorado', city: 'Bogotá', country: 'Colombia', coordinates: { latitude: 4.7016, longitude: -74.1469 } },
      { code: 'MEX', name: 'Benito Juárez', city: 'Ciudad de México', country: 'México', coordinates: { latitude: 19.4361, longitude: -99.0719 } },
      { code: 'SCL', name: 'Arturo Merino Benítez', city: 'Santiago', country: 'Chile', coordinates: { latitude: -33.3929, longitude: -70.7858 } },
      { code: 'LIM', name: 'Jorge Chávez', city: 'Lima', country: 'Perú', coordinates: { latitude: -12.0219, longitude: -77.1143 } },
      { code: 'MAD', name: 'Adolfo Suárez Madrid-Barajas', city: 'Madrid', country: 'España', coordinates: { latitude: 40.4983, longitude: -3.5676 } },
    ];

    for (const ap of airports) {
      await upsertOne('airports', { code: ap.code }, { ...ap, createdAt: new Date(), updatedAt: new Date() });
    }
    console.log('✅ Aeropuertos listos');

    // Map codes to _id
    const airportDocs = await db.collection('airports').find({ code: { $in: airports.map(a => a.code) } }).toArray();
    const codeToId = new Map(airportDocs.map((d: any) => [d.code, d._id]));

    // 3) Flights (simple set)
    const now = new Date();
    const addDays = (d: number) => new Date(now.getTime() + d * 24 * 60 * 60 * 1000);

    const flights = [
      { flightNumber: 'AV1234', origin: 'BOG', destination: 'MEX', departure: addDays(7), arrival: addDays(7 + 1),
        aircraft: { model: 'A320', capacity: 180 },
        inventory: {
          economy: { total: 120, available: 100, price: 220 },
          business: { total: 40, available: 30, price: 650 },
          firstClass: { total: 20, available: 18, price: 1200 },
        },
        status: 'scheduled' },
      { flightNumber: 'LA2045', origin: 'SCL', destination: 'LIM', departure: addDays(10), arrival: addDays(10),
        aircraft: { model: 'B737', capacity: 160 },
        inventory: {
          economy: { total: 110, available: 90, price: 180 },
          business: { total: 30, available: 25, price: 520 },
          firstClass: { total: 20, available: 15, price: 950 },
        },
        status: 'scheduled' },
      { flightNumber: 'IB3001', origin: 'MAD', destination: 'BOG', departure: addDays(15), arrival: addDays(16),
        aircraft: { model: 'A350', capacity: 300 },
        inventory: {
          economy: { total: 200, available: 180, price: 780 },
          business: { total: 70, available: 60, price: 2200 },
          firstClass: { total: 30, available: 28, price: 4200 },
        },
        status: 'scheduled' },
    ];

    for (const f of flights) {
      const doc = {
        flightNumber: f.flightNumber,
        origin: codeToId.get(f.origin),
        destination: codeToId.get(f.destination),
        departure: f.departure,
        arrival: f.arrival,
        aircraft: f.aircraft,
        inventory: f.inventory,
        status: f.status,
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      if (!doc.origin || !doc.destination) continue;
      await upsertOne('flights', { flightNumber: f.flightNumber }, doc);
    }
    console.log('✅ Vuelos listos');

    // 3.5) Airline providers (for external API connections)
    const providers = [
      {
        key: 'demo-airline',
        name: 'Demo Airline',
        ip: '192.168.0.10',
        port: 8080,
        createdAt: new Date(),
        updatedAt: new Date()
      }
    ];
    for (const p of providers) {
      await upsertOne('airline_providers', { key: p.key }, p);
    }
    console.log('✅ Proveedores de aerolíneas listos');

    // 5) Info Pages (contenido base agencia)
    const pages = [
      { title: 'Tipos de Asientos', slug: 'asientos', description: 'Comparación de asientos y precios', category: 'info', hero: { heading: 'Tipos de Asientos', subheading: 'Economy, Business y más', image: '' }, content: { sections: [] }, published: true },
      { title: 'Proceso de Check-in', slug: 'check-in', description: 'Guía paso a paso para hacer check-in', category: 'info', hero: { heading: 'Check-in', subheading: 'Online, aeropuerto y automático', image: '' }, content: { sections: [] }, published: true },
      { title: 'Instrucciones de Abordaje', slug: 'abordaje', description: 'Qué esperar en el proceso de abordaje', category: 'info', hero: { heading: 'Abordaje', subheading: 'Puertas, grupos y tiempos', image: '' }, content: { sections: [] }, published: true },
      { title: 'Políticas de Equipaje', slug: 'equipaje', description: 'Medidas, pesos y artículos permitidos', category: 'info', hero: { heading: 'Equipaje', subheading: 'Cabina y bodega', image: '' }, content: { sections: [] }, published: true },
      { title: 'Consejos de Viaje', slug: 'consejos', description: 'Recomendaciones para viajeros', category: 'info', hero: { heading: 'Consejos', subheading: 'Antes y durante el vuelo', image: '' }, content: { sections: [] }, published: true },
      { title: 'Contacto', slug: 'contacto', description: 'Soporte y canales de atención', category: 'info', hero: { heading: 'Contacto', subheading: 'Estamos para ayudarte', image: '' }, content: { sections: [] }, published: true }
    ];
    for (const p of pages) {
      await db.collection('info_pages').updateOne(
        { slug: p.slug },
        { $set: { ...p, updatedAt: new Date() }, $setOnInsert: { createdAt: new Date() } },
        { upsert: true }
      );
    }
    console.log('✅ Páginas informativas listas');

    // 4) Optional: sample booking for admin on first flight
    const firstFlight = await db.collection('flights').findOne({});
    const admin = await db.collection('users').findOne({ email: adminEmail });
    if (firstFlight && admin) {
      await upsertOne('bookings', { bookingReference: { $regex: /^BK/ } }, {
        userId: String(admin._id),
        flightId: String(firstFlight._id),
        passengers: [{
          firstName: admin.firstName,
          lastName: admin.lastName,
          dateOfBirth: new Date('1990-01-01'),
          seatCategory: 'economy',
          seatNumber: `AUTO-${String(firstFlight._id)}-economy-${Date.now()}-0`
        }],
        totalAmount: 220,
        status: 'confirmed',
        paymentMethod: 'credit_card',
        paymentStatus: 'completed',
        bookingReference: generateBookingRef(),
        createdAt: new Date(),
        updatedAt: new Date(),
      });
      console.log('✅ Reserva de ejemplo creada');
    }

    console.log('🎉 Seed completado.');
  } catch (err) {
    console.error('❌ Error en seed:', err);
    process.exit(1);
  } finally {
    await mongoose.disconnect();
  }
}

main();


