// Script para corregir los endpoints de las aerolíneas en MongoDB
// Ejecutar en MongoDB Shell (mongosh) o MongoDB Compass

// OPCIÓN 1: Actualizar TODAS las aerolíneas
db.airlines.updateMany(
  {},
  {
    $set: {
      'endpoints.search': 'airline/flights',
      'endpoints.book': 'airline/tickets',
      'endpoints.cancel': 'airline/tickets',
      'endpoints.health': 'airline/health',
      'endpoints.cities': 'airline/cities',
      'endpoints.flights': 'airline/flights',
      'endpoints.seats': 'airline/flights/{flightId}/seats'
    }
  }
);

// OPCIÓN 2: Actualizar aerolínea específica por nombre
// db.airlines.updateOne(
//   { name: "Agenciá2" },
//   {
//     $set: {
//       'endpoints.book': 'airline/tickets',
//       'endpoints.search': 'airline/flights',
//       'endpoints.cities': 'airline/cities',
//       'endpoints.seats': 'airline/flights/{flightId}/seats'
//     }
//   }
// );

// db.airlines.updateOne(
//   { name: "Gio" },
//   {
//     $set: {
//       'endpoints.book': 'airline/tickets',
//       'endpoints.search': 'airline/flights',
//       'endpoints.cities': 'airline/cities',
//       'endpoints.seats': 'airline/flights/{flightId}/seats'
//     }
//   }
// );

// Verificar que se actualizaron correctamente
db.airlines.find({}, { name: 1, code: 1, endpoints: 1, apiKey: 1 }).pretty();

// VERIFICAR que cada aerolínea tenga su API Key único
db.airlines.find({}, { name: 1, apiKey: 1 }).pretty();

