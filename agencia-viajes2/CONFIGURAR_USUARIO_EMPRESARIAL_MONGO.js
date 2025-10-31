// ============================================================================
// CONFIGURACIÓN DE USUARIO EMPRESARIAL EN MONGODB
// Agencia de Viajes - Base de Datos MongoDB
// ============================================================================

// Ejecutar en MongoDB Compass o en mongo shell

// ============================================================================
// PASO 1: Guardar configuración del usuario empresarial de la aerolínea
// ============================================================================

// Conecta a tu base de datos: agencia-viajes

db.corporate_config.insertOne({
  _id: "airline_corporate_user",
  name: "Configuración Usuario Empresarial Aerolínea",
  description: "Credenciales empresariales para comprar vuelos en la aerolínea",
  
  // 🔑 API_KEY de la aerolínea (el que obtuviste)
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  
  // Usuario empresarial en la aerolínea
  corporateUserId: null, // Se actualizará cuando sepamos el ID
  corporateUserEmail: "corporate@agencia-elvuelo.com",
  companyName: "Agencia de Viajes El Vuelo",
  
  // Configuración
  enabled: true,
  createdAt: new Date(),
  updatedAt: new Date(),
  
  // Notas
  notes: "Este API_KEY permite a la agencia comprar vuelos en nombre de sus clientes"
});


// ============================================================================
// PASO 2: Verificar que se guardó
// ============================================================================

db.corporate_config.findOne({ _id: "airline_corporate_user" });

// Deberías ver:
/*
{
  _id: "airline_corporate_user",
  name: "Configuración Usuario Empresarial Aerolínea",
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  enabled: true,
  ...
}
*/


// ============================================================================
// PASO 3: Actualizar aerolínea activa con información empresarial (OPCIONAL)
// ============================================================================

// Ver aerolínea activa actual
db.airlines.findOne({ enabled: true });

// Agregar info del usuario empresarial a la aerolínea activa
db.airlines.updateOne(
  { enabled: true },
  { 
    $set: { 
      corporateApiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
      corporateUserEmail: "corporate@agencia-elvuelo.com",
      updatedAt: new Date()
    } 
  }
);


// ============================================================================
// PASO 4: Ver configuración completa
// ============================================================================

// Ver configuración empresarial
db.corporate_config.find().pretty();

// Ver aerolínea con credenciales empresariales
db.airlines.find({ enabled: true }).pretty();


// ============================================================================
// QUERIES ÚTILES
// ============================================================================

// Actualizar API_KEY si cambia
db.corporate_config.updateOne(
  { _id: "airline_corporate_user" },
  { $set: { apiKey: "NUEVO_API_KEY", updatedAt: new Date() } }
);

// Deshabilitar compras empresariales temporalmente
db.corporate_config.updateOne(
  { _id: "airline_corporate_user" },
  { $set: { enabled: false, updatedAt: new Date() } }
);

// Habilitar nuevamente
db.corporate_config.updateOne(
  { _id: "airline_corporate_user" },
  { $set: { enabled: true, updatedAt: new Date() } }
);

// Ver todas las configuraciones
db.corporate_config.find();

// Eliminar configuración (si quieres empezar de cero)
// db.corporate_config.deleteOne({ _id: "airline_corporate_user" });


// ============================================================================
// ESTRUCTURA DE LA COLECCIÓN
// ============================================================================

/*
Colección: corporate_config

Documentos:
{
  _id: "airline_corporate_user",              // ID único
  name: "Configuración...",                   // Nombre descriptivo
  description: "...",                         // Descripción
  apiKey: "AGV-WQDJGN5...",                  // API_KEY de la aerolínea
  corporateUserId: 123,                      // ID en Oracle (opcional)
  corporateUserEmail: "corporate@...",       // Email del usuario empresarial
  companyName: "Agencia...",                 // Nombre de la empresa
  enabled: true,                              // Activo/Inactivo
  createdAt: ISODate("..."),                 // Fecha creación
  updatedAt: ISODate("..."),                 // Fecha actualización
  notes: "..."                                // Notas
}
*/


// ============================================================================
// FIN
// ============================================================================

