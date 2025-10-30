// Script para verificar el estado de las aerolíneas en MongoDB
// Ejecutar en MongoDB Shell o MongoDB Compass

// 1. Ver todas las aerolíneas configuradas
db.airlines.find({}).pretty()

// 2. Ver solo las aerolíneas ACTIVAS (enabled: true)
db.airlines.find({ enabled: true }).pretty()

// 3. Contar aerolíneas activas
db.airlines.countDocuments({ enabled: true })

// 4. HABILITAR TODAS LAS AEROLÍNEAS (si quieres activar todas)
// db.airlines.updateMany({}, { $set: { enabled: true } })

// 5. HABILITAR AEROLÍNEAS ESPECÍFICAS POR NOMBRE
// db.airlines.updateOne({ name: "Agencia1" }, { $set: { enabled: true } })
// db.airlines.updateOne({ name: "Agenciá2" }, { $set: { enabled: true } })

// 6. Ver resumen de aerolíneas (nombre, código, enabled)
db.airlines.find({}, { name: 1, code: 1, host: 1, port: 1, enabled: 1 }).pretty()

