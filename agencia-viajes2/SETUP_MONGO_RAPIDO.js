// ============================================================================
// CONFIGURACIÓN RÁPIDA DE MONGODB PARA USUARIO EMPRESARIAL
// Ejecutar en MongoDB Compass o mongo shell
// ============================================================================

// ✅ QUERY PRINCIPAL - Ejecuta esto:

db.corporate_config.insertOne({
  _id: "airline_corporate_user",
  apiKey: "AGV-WQDJGN5KLBMEXJ1LE9N44EV0VRVFAC3L",
  corporateUserEmail: "corporate@agencia-elvuelo.com",
  companyName: "Agencia de Viajes El Vuelo",
  enabled: true,
  createdAt: new Date(),
  updatedAt: new Date()
});

// ✅ Verificar que se guardó:
db.corporate_config.findOne({ _id: "airline_corporate_user" });

