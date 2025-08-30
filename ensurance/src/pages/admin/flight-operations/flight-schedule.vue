<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { airlineApi } from '../../../utils/airlineApi'

// Estados reactivos
const loading = ref(false)
const error = ref('')
const success = ref('')

// Datos del formulario
const flightForm = ref({
  flightNumber: '',
  originCityId: '',
  destinationCityId: '',
  departureDate: '',
  departureTime: '',
  arrivalDate: '',
  arrivalTime: '',
  basePrice: '',
  availableSeats: '',
  gate: '',
  terminal: '',
  checkInStart: '',
  checkInEnd: '',
  boardingTime: ''
})

// Inventario por categoría
const inventoryForm = ref({
  ECONOMY: 0,
  BUSINESS: 0,
  FIRST_CLASS: 0
})

// Precios por categoría
const faresForm = ref({
  ECONOMY: 0,
  BUSINESS: 0,
  FIRST_CLASS: 0
})

// Datos de escalas (ahora usando ciudades)
const legsForm = ref([{
  cityId: '',
  legOrder: 1,
  arrivalTime: '',
  departureTime: '',
  aircraftChange: false
}])

// Listas de datos
const cities = ref<any[]>([])
const flights = ref<any[]>([])
const aircrafts = ref<any[]>([])

// Selección de aeronave (autorrelleno estricto)
const selectedAircraftId = ref<number | ''>('')
const seatConfig = ref<Record<string, { seats: number; priceMultiplier: number }>>({
  ECONOMY: { seats: 0, priceMultiplier: 1.0 },
  BUSINESS: { seats: 0, priceMultiplier: 2.0 },
  FIRST_CLASS: { seats: 0, priceMultiplier: 3.0 }
})

// Estados de la interfaz
const showCreateForm = ref(false)
const showBulkModal = ref(false)
const bulkText = ref('')
const bulkFile = ref<File | null>(null)
const selectedFlight = ref<any>(null)
const showEditForm = ref(false)

// Nuevos estados para gestión completa
const showEditModal = ref(false)
const showCancelModal = ref(false)
const showDeleteModal = ref(false)
const editingFlight = ref<any>(null)
const cancellingFlight = ref<any>(null)
const deletingFlight = ref<any>(null)

// Formularios para gestión
const editForm = ref({
  flightNumber: '',
  status: '',
  originCityId: '',
  destinationCityId: '',
  departureDate: '',
  departureTime: '',
  arrivalDate: '',
  arrivalTime: '',
  basePrice: '',
  availableSeats: '',
  gate: '',
  terminal: '',
  checkInStart: '',
  checkInEnd: '',
  boardingTime: '',
  updatedBy: '1' // Cambiar a string para mantener consistencia
})

const cancelForm = ref({
  reason: ''
})

// Computed properties
const totalSeats = computed(() => {
  return Object.values(inventoryForm.value).reduce((sum: number, seats: any) => sum + Number(seats), 0)
})

// Sincronizar asientos disponibles automáticamente
const syncAvailableSeats = () => {
  if (totalSeats.value > 0) {
    flightForm.value.availableSeats = totalSeats.value.toString()
  }
}

// Observar cambios en el inventario para sincronizar
watch(inventoryForm, () => {
  syncAvailableSeats()
}, { deep: true })

// Autorrelleno estricto al seleccionar aeronave
watch(selectedAircraftId, async (val) => {
  if (!val) {
    // Rehabilitar edición manual
    return
  }
  try {
    loading.value = true
    const cfg = await airlineApi.getSeatConfig(Number(val))
    if (cfg && cfg.success && cfg.config) {
      seatConfig.value = {
        ECONOMY: { seats: cfg.config.ECONOMY?.seats || 0, priceMultiplier: cfg.config.ECONOMY?.priceMultiplier || 1 },
        BUSINESS: { seats: cfg.config.BUSINESS?.seats || 0, priceMultiplier: cfg.config.BUSINESS?.priceMultiplier || 2 },
        FIRST_CLASS: { seats: cfg.config.FIRST_CLASS?.seats || 0, priceMultiplier: cfg.config.FIRST_CLASS?.priceMultiplier || 3 },
      }
      // Poblar inventario
      inventoryForm.value = {
        ECONOMY: seatConfig.value.ECONOMY.seats,
        BUSINESS: seatConfig.value.BUSINESS.seats,
        FIRST_CLASS: seatConfig.value.FIRST_CLASS.seats,
      }
      // Sincronizar asientos disponibles
      syncAvailableSeats()
      // Recalcular precios por categoría según basePrice actual
      recalcFaresFromBase()
    }
  } catch (e) {
    console.warn('No se pudo cargar seat-config', e)
  } finally {
    loading.value = false
  }
})

// Recalcular tarifas al cambiar el precio base
watch(() => flightForm.value.basePrice, () => {
  if (selectedAircraftId.value) recalcFaresFromBase()
})

const recalcFaresFromBase = () => {
  const base = Number(flightForm.value.basePrice) || 0
  faresForm.value = {
    ECONOMY: base * (seatConfig.value.ECONOMY.priceMultiplier || 1),
    BUSINESS: base * (seatConfig.value.BUSINESS.priceMultiplier || 2),
    FIRST_CLASS: base * (seatConfig.value.FIRST_CLASS.priceMultiplier || 3),
  }
}

// Cargar datos iniciales
const loadInitialData = async () => {
  loading.value = true
  error.value = ''
  try {
    console.log('🔄 Iniciando carga de datos...')
    
    // Cargar ciudades
    console.log('🏙️ Cargando ciudades...')
    const citiesResponse = await airlineApi.getCities();
    console.log('🏙️ Respuesta de ciudades:', citiesResponse)
    
    if (citiesResponse && citiesResponse.success !== false) {
      // Si la respuesta es exitosa o no tiene campo success, asumimos que es la lista directa
      cities.value = Array.isArray(citiesResponse) ? citiesResponse : (citiesResponse.cities || [])
      console.log('✅ Ciudades cargadas:', cities.value)
    } else {
      console.error('❌ Error cargando ciudades:', citiesResponse?.error || 'Respuesta inválida')
      cities.value = []
    }
    
    // Cargar aeronaves
    try {
      const a = await airlineApi.getAircrafts()
      aircrafts.value = Array.isArray(a) ? a : (a || [])
    } catch (e) {
      aircrafts.value = []
    }

    // Cargar vuelos
    console.log('✈️ Cargando vuelos...')
    const flightsResponse = await airlineApi.getFlights();
    console.log('✈️ Respuesta de vuelos:', flightsResponse)
    
    if (flightsResponse && flightsResponse.success !== false) {
      // Si la respuesta es exitosa o no tiene campo success, asumimos que es la lista directa
      flights.value = Array.isArray(flightsResponse) ? flightsResponse : (flightsResponse.flights || [])
      console.log('✅ Vuelos cargados:', flights.value);
      console.log('🔍 Primer vuelo:', flights.value[0]);
      console.log('🔍 Estructura del vuelo:', Object.keys(flights.value[0] || {}));
      
      // Debug adicional para el estado
      if (flights.value.length > 0) {
        console.log('🔍 Estados de vuelos:')
        flights.value.forEach((flight, index) => {
          console.log(`  Vuelo ${index + 1}:`, {
            id: flight.idFlight,
            number: flight.flightNumber,
            status: flight.status,
            hasStatus: !!flight.status,
            statusType: typeof flight.status
          })
          
          // Establecer estado por defecto si no existe
          if (!flight.status) {
            console.log(`  ⚠️ Vuelo ${flight.flightNumber} no tiene estado, estableciendo DRAFT por defecto`)
            flight.status = 'DRAFT'
          }
        })
      }
    } else {
      console.error('❌ Error cargando vuelos:', flightsResponse?.error || 'Respuesta inválida')
      flights.value = []
    }
    
  } catch (e: any) {
    console.error('💥 Error general cargando datos:', e)
    error.value = 'Error cargando datos: ' + (e?.message || 'Error desconocido')
  } finally {
    loading.value = false
  }
}

// Crear vuelo
const createFlight = async () => {
  if (!validateFlightForm()) return
  
  loading.value = true
  error.value = ''
  success.value = ''
  
  try {
    // Primero crear el vuelo base (sin escalas)
    const baseFlightData = {
      ...flightForm.value,
      inventory: inventoryForm.value,
      fares: faresForm.value,
      createdBy: JSON.parse(localStorage.getItem('user') || '{}').idUser || 1,
      aircraftId: selectedAircraftId.value ? Number(selectedAircraftId.value) : undefined
    }
    
    console.log('✈️ Creando vuelo base:', baseFlightData)
    const flightResult = await airlineApi.createFlight(baseFlightData)
    
    if (!flightResult.success) {
      error.value = flightResult.error || 'Error creando vuelo base'
      return
    }
    
    const flightId = flightResult.flight?.idFlight || flightResult.flightId
    
    // Si hay escalas configuradas, crearlas usando el nuevo endpoint
    const legsToCreate = legsForm.value.filter(leg => leg.cityId)
    if (legsToCreate.length > 0) {
      console.log('🛫 Creando escalas para vuelo:', flightId)
      
      for (const leg of legsToCreate) {
        const legData = {
          stopoverCityId: leg.cityId,
          stopoverArrivalTime: leg.arrivalTime,
          stopoverDepartureTime: leg.departureTime,
          connectionTimeMinutes: 60, // Por defecto 60 minutos
          aircraftChange: leg.aircraftChange ? 'Y' : 'N'
        }
        
        console.log('🛫 Creando escala:', legData)
        const legResult = await airlineApi.createFlightLeg(flightId, legData)
        
        if (!legResult.success) {
          console.error('❌ Error creando escala:', legResult.error)
          error.value = `Error creando escala: ${legResult.error}`
          return
        }
      }
      
      success.value = `Vuelo creado exitosamente con ${legsToCreate.length} escala(s)`
    } else {
      success.value = 'Vuelo creado exitosamente (sin escalas)'
    }
    
    resetForm()
    await loadInitialData()
    showCreateForm.value = false
    
  } catch (e: any) {
    console.error('💥 Error en createFlight:', e)
    error.value = e?.message || 'Error creando vuelo'
  } finally {
    loading.value = false
  }
}

// Validar formulario
const validateFlightForm = () => {
  const errors: string[] = []
  
  // Validar campos básicos
  if (!flightForm.value.flightNumber?.trim()) {
    errors.push('El número de vuelo es obligatorio')
  } else if (flightForm.value.flightNumber.length < 3) {
    errors.push('El número de vuelo debe tener al menos 3 caracteres')
  }
  
  if (!flightForm.value.originCityId) {
    errors.push('Debe seleccionar una ciudad de origen')
  }
  
  if (!flightForm.value.destinationCityId) {
    errors.push('Debe seleccionar una ciudad de destino')
  }
  
  if (flightForm.value.originCityId && flightForm.value.destinationCityId && 
      flightForm.value.originCityId === flightForm.value.destinationCityId) {
    errors.push('El origen y destino no pueden ser la misma ciudad')
  }
  
  // Validar fechas
  if (!flightForm.value.departureDate) {
    errors.push('La fecha de salida es obligatoria')
  }
  
  if (!flightForm.value.departureTime) {
    errors.push('La hora de salida es obligatoria')
  }
  
  if (!flightForm.value.arrivalDate) {
    errors.push('La fecha de llegada es obligatoria')
  }
  
  if (!flightForm.value.arrivalTime) {
    errors.push('La hora de llegada es obligatoria')
  }
  
  // Validar que la fecha de llegada no sea anterior a la de salida
  if (flightForm.value.departureDate && flightForm.value.arrivalDate) {
    const departureDate = new Date(flightForm.value.departureDate)
    const arrivalDate = new Date(flightForm.value.arrivalDate)
    
    if (arrivalDate < departureDate) {
      errors.push('La fecha de llegada no puede ser anterior a la fecha de salida')
    }
  }
  
  // Validar precio base
  if (!flightForm.value.basePrice || Number(flightForm.value.basePrice) <= 0) {
    errors.push('El precio base debe ser mayor a 0')
  }
  
  // Validar asientos disponibles
  if (!flightForm.value.availableSeats || Number(flightForm.value.availableSeats) <= 0) {
    errors.push('Debe especificar un número de asientos disponibles mayor a 0')
  }
  
  // Validar inventario de asientos
  const totalInventorySeats = Object.values(inventoryForm.value).reduce((sum: number, seats: any) => sum + Number(seats), 0)
  if (totalInventorySeats === 0) {
    errors.push('Debe configurar al menos una categoría de asientos')
  }
  
  // Validar que el total de asientos del inventario coincida con los asientos disponibles
  if (totalInventorySeats > 0 && Number(flightForm.value.availableSeats) > 0) {
    if (totalInventorySeats !== Number(flightForm.value.availableSeats)) {
      errors.push(`El total de asientos del inventario (${totalInventorySeats}) debe coincidir con los asientos disponibles (${flightForm.value.availableSeats})`)
    }
  }
  
  // Validar precios por categoría
  const hasValidPrices = Object.entries(inventoryForm.value).some(([category, seats]) => {
    const seatsCount = Number(seats)
    const price = Number(faresForm.value[category as keyof typeof faresForm.value])
    return seatsCount > 0 && price > 0
  })
  
  if (!hasValidPrices) {
    errors.push('Debe configurar precios para al menos una categoría de asientos')
  }
  
  // Validar que cada categoría con asientos tenga precio
  Object.entries(inventoryForm.value).forEach(([category, seats]) => {
    const seatsCount = Number(seats)
    const price = Number(faresForm.value[category as keyof typeof faresForm.value])
    
    if (seatsCount > 0 && price <= 0) {
      errors.push(`La categoría ${category} tiene ${seatsCount} asientos pero no tiene precio configurado`)
    }
  })
  
  // Validar escalas (si hay)
  if (legsForm.value.length > 1) {
    legsForm.value.forEach((leg, index) => {
      if (leg.cityId && (!leg.arrivalTime || !leg.departureTime)) {
        errors.push(`La escala ${index + 1} debe tener hora de llegada y salida configuradas`)
      }
    })
  }
  
  // Si hay errores, mostrarlos
  if (errors.length > 0) {
    error.value = errors.join('\n')
    console.error('❌ Errores de validación:', errors)
    return false
  }
  
  // Si todo está bien, limpiar errores
  error.value = ''
  return true
}

// Resetear formulario
const resetForm = () => {
  flightForm.value = {
    flightNumber: '',
    originCityId: '',
    destinationCityId: '',
    departureDate: '',
    departureTime: '',
    arrivalDate: '',
    arrivalTime: '',
    basePrice: '',
    availableSeats: '',
    gate: '',
    terminal: '',
    checkInStart: '',
    checkInEnd: '',
    boardingTime: ''
  }
  
  inventoryForm.value = { ECONOMY: 0, BUSINESS: 0, FIRST_CLASS: 0 }
  faresForm.value = { ECONOMY: 0, BUSINESS: 0, FIRST_CLASS: 0 }
  legsForm.value = [{ cityId: '', legOrder: 1, arrivalTime: '', departureTime: '', aircraftChange: false }]
  selectedAircraftId.value = ''
  seatConfig.value = { ECONOMY: { seats: 0, priceMultiplier: 1 }, BUSINESS: { seats: 0, priceMultiplier: 2 }, FIRST_CLASS: { seats: 0, priceMultiplier: 3 } }
}

// Agregar escala
const addLeg = () => {
  legsForm.value.push({
    cityId: '',
    legOrder: legsForm.value.length + 1,
    arrivalTime: '',
    departureTime: '',
    aircraftChange: false
  })
}

// Remover escala
const removeLeg = (index: number) => {
  if (legsForm.value.length > 1) {
    legsForm.value.splice(index, 1)
    // Reordenar
    legsForm.value.forEach((leg, idx) => {
      leg.legOrder = idx + 1
    })
  }
}

// Cambiar estado del vuelo
const changeFlightStatus = async (flightId: number, newStatus: string) => {
  loading.value = true
  error.value = ''
  success.value = ''
  
  try {
    const response = await airlineApi.updateFlightStatus(flightId, newStatus)
    
    if (response.success) {
      success.value = `Estado del vuelo cambiado a ${newStatus} exitosamente`
      await loadInitialData()
    } else {
      error.value = response.error || 'Error cambiando estado del vuelo'
    }
  } catch (e: any) {
    console.error('💥 Error en changeFlightStatus:', e)
    error.value = e?.message || 'Error cambiando estado del vuelo'
  } finally {
    loading.value = false
  }
}

// Editar vuelo
const editFlight = (flight: any) => {
  console.log('✏️ Editando vuelo:', flight)
  editingFlight.value = flight
  
  // Mapear los datos del vuelo al formulario de edición
  editForm.value = {
    flightNumber: flight.flightNumber || '',
    status: getFlightStatus(flight), // Usar la nueva lógica de estado
    originCityId: flight.originCityId || flight.originCity?.idCity || '',
    destinationCityId: flight.destinationCityId || flight.destinationCity?.idCity || '',
    departureDate: flight.departureDate || '',
    departureTime: flight.departureTime || '',
    arrivalDate: flight.arrivalDate || '',
    arrivalTime: flight.arrivalTime || '',
    basePrice: flight.basePrice || '',
    availableSeats: flight.availableSeats || '',
    gate: flight.gate || '',
    terminal: flight.terminal || '',
    checkInStart: flight.checkInStart || '',
    checkInEnd: flight.checkInEnd || '',
    boardingTime: flight.boardingTime || '',
    updatedBy: JSON.parse(localStorage.getItem('user') || '{}').idUser || 1
  }
  
  // Verificar que los campos obligatorios tengan valores
  console.log('🔍 Campos del formulario cargados:', editForm.value)
  
  // Cargar inventario de asientos si existe
  if (flight.inventory && typeof flight.inventory === 'object') {
    console.log('📦 Cargando inventario:', flight.inventory)
    inventoryForm.value = { ...flight.inventory }
  } else {
    // Si no hay inventario, usar valores por defecto
    console.log('📦 No hay inventario, usando valores por defecto')
    inventoryForm.value = {
      ECONOMY: parseInt(flight.availableSeats) || 0,
      BUSINESS: 0,
      FIRST_CLASS: 0
    }
  }
  
  // Cargar tarifas si existen
  if (flight.fares && typeof flight.fares === 'object') {
    console.log('💰 Cargando tarifas:', flight.fares)
    faresForm.value = { ...flight.fares }
  } else {
    // Si no hay tarifas, usar precio base
    console.log('💰 No hay tarifas, usando precio base')
    const basePrice = parseFloat(flight.basePrice) || 0
    faresForm.value = {
      ECONOMY: basePrice,
      BUSINESS: basePrice * 2,
      FIRST_CLASS: basePrice * 3
    }
  }
  
  // Cargar escalas si existen
  if (flight.flightLegs && Array.isArray(flight.flightLegs) && flight.flightLegs.length > 0) {
    console.log('🛫 Cargando escalas:', flight.flightLegs)
    legsForm.value = flight.flightLegs.map((leg: any, index: number) => ({
      cityId: leg.stopoverCityId || leg.cityId || '',
      legOrder: index + 1,
      arrivalTime: leg.stopoverArrivalTime || leg.arrivalTime || '',
      departureTime: leg.stopoverDepartureTime || leg.departureTime || '',
      aircraftChange: leg.aircraftChange === 'Y' || leg.aircraftChange === true
    }))
  } else {
    // Si no hay escalas, usar una escala vacía
    console.log('🛫 No hay escalas, usando escala vacía')
    legsForm.value = [{
      cityId: '',
      legOrder: 1,
      arrivalTime: '',
      departureTime: '',
      aircraftChange: false
    }]
  }
  
  // Sincronizar asientos disponibles
  syncAvailableSeats()
  
  // Verificar que los campos críticos no estén vacíos
  if (!editForm.value.originCityId || !editForm.value.destinationCityId) {
    console.warn('⚠️ Campos críticos vacíos, intentando cargar desde datos del vuelo')
    
    // Intentar cargar desde diferentes propiedades del vuelo
    if (flight.originCity && flight.originCity.idCity) {
      editForm.value.originCityId = flight.originCity.idCity
    }
    if (flight.destinationCity && flight.destinationCity.idCity) {
      editForm.value.destinationCityId = flight.destinationCity.idCity
    }
    
    console.log('🔍 Campos críticos después de corrección:', {
      originCityId: editForm.value.originCityId,
      destinationCityId: editForm.value.destinationCityId
    })
  }
  
  showEditModal.value = true
}

const saveFlightChanges = async () => {
  if (!editingFlight.value) return
  
  loading.value = true
  error.value = ''
  success.value = ''
  
  try {
    console.log('💾 Guardando cambios del vuelo:', editingFlight.value.idFlight)
    console.log('💾 Datos del formulario:', editForm.value)
    
    // Validar campos obligatorios
    const validationErrors = []
    
    if (!editForm.value.flightNumber?.trim()) {
      validationErrors.push('El número de vuelo es obligatorio')
    }
    
    if (!editForm.value.originCityId) {
      validationErrors.push('La ciudad de origen es obligatoria')
    }
    
    if (!editForm.value.destinationCityId) {
      validationErrors.push('La ciudad de destino es obligatoria')
    }
    
    if (!editForm.value.departureDate) {
      validationErrors.push('La fecha de salida es obligatoria')
    }
    
    if (!editForm.value.departureTime) {
      validationErrors.push('La hora de salida es obligatoria')
    }
    
    if (!editForm.value.arrivalDate) {
      validationErrors.push('La fecha de llegada es obligatoria')
    }
    
    if (!editForm.value.arrivalTime) {
      validationErrors.push('La hora de llegada es obligatoria')
    }
    
    if (!editForm.value.basePrice || parseFloat(editForm.value.basePrice) <= 0) {
      validationErrors.push('El precio base debe ser mayor a 0')
    }
    
    if (!editForm.value.availableSeats || parseInt(editForm.value.availableSeats) <= 0) {
      validationErrors.push('Los asientos disponibles deben ser mayor a 0')
    }
    
    // Si hay errores de validación, mostrarlos y detener
    if (validationErrors.length > 0) {
      error.value = 'Errores de Validación:\n' + validationErrors.join('\n')
      return
    }
    
    // Limpiar y preparar los datos para enviar
    const cleanData = {
      flightNumber: editForm.value.flightNumber.trim(),
      status: editForm.value.status,
      originCityId: editForm.value.originCityId,
      destinationCityId: editForm.value.destinationCityId,
      departureDate: editForm.value.departureDate,
      departureTime: editForm.value.departureTime,
      arrivalDate: editForm.value.arrivalDate,
      arrivalTime: editForm.value.arrivalTime,
      basePrice: editForm.value.basePrice,
      availableSeats: editForm.value.availableSeats,
      gate: editForm.value.gate || null,
      terminal: editForm.value.terminal || null,
      checkInStart: editForm.value.checkInStart || null,
      checkInEnd: editForm.value.checkInEnd || null,
      boardingTime: editForm.value.boardingTime || null,
      updatedBy: editForm.value.updatedBy
    }
    
    // Remover campos null o vacíos para evitar problemas de validación
    Object.keys(cleanData).forEach(key => {
      if (cleanData[key as keyof typeof cleanData] === null || cleanData[key as keyof typeof cleanData] === '') {
        delete cleanData[key as keyof typeof cleanData]
      }
    })
    
    console.log('💾 Datos limpios a enviar:', cleanData)
    
    const response = await airlineApi.updateFlight(
      parseInt(editingFlight.value.idFlight), 
      cleanData
    )
    
    if (response.success) {
      success.value = 'Vuelo actualizado exitosamente'
      await loadInitialData()
      closeEditModal()
    } else {
      // Manejar diferentes tipos de errores
      if (response.error && response.error.includes('JDBC exception') || response.error.includes('Operation timed out')) {
        error.value = '❌ Error de Conexión a la Base de Datos:\n\n' +
                     '• La base de datos no responde\n' +
                     '• Verificar conexión de red\n' +
                     '• Contactar al administrador del sistema\n\n' +
                     'Detalles técnicos:\n' + response.error
      } else {
        error.value = response.error || 'Error actualizando vuelo'
      }
    }
  } catch (e: any) {
    console.error('💥 Error en saveFlightChanges:', e)
    
    // Manejar errores de red y conexión
    if (e.message && (e.message.includes('timeout') || e.message.includes('network') || e.message.includes('fetch'))) {
      error.value = '❌ Error de Conexión:\n\n' +
                   '• No se puede conectar con el servidor\n' +
                   '• Verificar conexión a internet\n' +
                   '• El servidor puede estar caído\n\n' +
                   'Detalles: ' + e.message
    } else {
      error.value = e?.message || 'Error actualizando vuelo'
    }
  } finally {
    loading.value = false
  }
}

const closeEditModal = () => {
  showEditModal.value = false
  editingFlight.value = null
  editForm.value = {
    flightNumber: '',
    status: '',
    originCityId: '',
    destinationCityId: '',
    departureDate: '',
    departureTime: '',
    arrivalDate: '',
    arrivalTime: '',
    basePrice: '',
    availableSeats: '',
    gate: '',
    terminal: '',
    checkInStart: '',
    checkInEnd: '',
    boardingTime: '',
    updatedBy: '1'
  }
}

// Cancelar vuelo
const cancelFlight = (flight: any) => {
  cancellingFlight.value = flight
  cancelForm.value.reason = ''
  showCancelModal.value = true
}

const confirmCancelFlight = async () => {
  if (!cancellingFlight.value) return
  
  loading.value = true
  error.value = ''
  success.value = ''
  
  try {
    const response = await airlineApi.cancelFlight(
      cancellingFlight.value.idFlight,
      cancelForm.value.reason,
      JSON.parse(localStorage.getItem('user') || '{}').idUser || 1
    )
    
    if (response.success) {
      success.value = 'Vuelo cancelado exitosamente'
      await loadInitialData()
      closeCancelModal()
    } else {
      error.value = response.error || 'Error cancelando vuelo'
    }
  } catch (e: any) {
    console.error('💥 Error en confirmCancelFlight:', e)
    error.value = e?.message || 'Error cancelando vuelo'
  } finally {
    loading.value = false
  }
}

const closeCancelModal = () => {
  showCancelModal.value = false
  cancellingFlight.value = null
  cancelForm.value.reason = ''
}

// Eliminar vuelo
const deleteFlight = (flight: any) => {
  deletingFlight.value = flight
  showDeleteModal.value = true
}

const confirmDeleteFlight = async () => {
  if (!deletingFlight.value) return
  
  loading.value = true
  error.value = ''
  success.value = ''
  
  try {
    const response = await airlineApi.deleteFlight(deletingFlight.value.idFlight)
    
    if (response.success) {
      success.value = 'Vuelo eliminado exitosamente'
      await loadInitialData()
      closeDeleteModal()
    } else {
      error.value = response.error || 'Error eliminando vuelo'
    }
  } catch (e: any) {
    console.error('💥 Error en confirmDeleteFlight:', e)
    error.value = e?.message || 'Error eliminando vuelo'
  } finally {
    loading.value = false
  }
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  deletingFlight.value = null
}

// Formatear fecha
const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  try {
    // Si la fecha ya está en formato YYYY-MM-DD, solo la formateamos
    if (dateString.includes('-')) {
      const [year, month, day] = dateString.split('-')
      return `${day}/${month}/${year}`
    }
    // Si no, intentamos parsear
    return new Date(dateString).toLocaleDateString('es-GT')
  } catch (e) {
    return dateString || 'N/A'
  }
}

// Formatear hora
const formatTime = (timeString: string) => {
  if (!timeString) return 'N/A'
  try {
    // Si la hora ya está en formato HH:MM, solo la devolvemos
    if (timeString.includes(':') && timeString.length === 5) {
      return timeString
    }
    // Si no, intentamos parsear
    return new Date(`2000-01-01T${timeString}`).toLocaleTimeString('es-GT', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } catch (e) {
    return timeString || 'N/A'
  }
}

// Obtener nombre de ciudad por ID
const getCityName = (cityId: number) => {
  const city = cities.value.find(c => c.idCity === cityId)
  return city ? city.name : `ID: ${cityId}`
}

// Obtener nombre de ruta (origen → destino)
const getRouteName = (flight: any) => {
  // Si el vuelo ya tiene los nombres de ciudades, los usamos directamente
  if (flight.originCity && flight.destinationCity) {
    return `${flight.originCity} → ${flight.destinationCity}`
  }
  
  // Si no, buscamos por ID
  const origin = cities.value.find(c => c.idCity === flight.originCityId)
  const destination = cities.value.find(c => c.idCity === flight.destinationCityId)
  return `${origin?.name || 'N/A'} → ${destination?.name || 'N/A'}`
}

// Función para probar la API de ciudades
const testCitiesApi = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await airlineApi.getCities();
    console.log('🏙️ Respuesta de la API de ciudades:', response);
    if (response && response.success !== false) {
      alert('API de ciudades funcionando correctamente. Respuesta: ' + JSON.stringify(response));
    } else {
      alert('Error al llamar a la API de ciudades: ' + (response?.error || 'Respuesta inválida'));
    }
  } catch (e: any) {
    console.error('💥 Error general cargando datos:', e);
    error.value = 'Error cargando datos: ' + (e?.message || 'Error desconocido');
  } finally {
    loading.value = false;
  }
};

onMounted(loadInitialData)

// Métodos auxiliares - Lógica de negocio real
const canBeModified = (flight: any) => {
  // Un vuelo se puede modificar si no está completado
  // Para simplificar, asumimos que todos los vuelos se pueden modificar
  return true
}

// Enviar carga masiva
const submitBulk = async () => {
  if (!bulkText.value.trim()) {
    // Si no hay texto pero hay archivo, usar archivo
    if (bulkFile.value) {
      await submitBulkFile()
      return
    } else {
      error.value = 'Pega el JSON de vuelos a cargar o selecciona un archivo'
      return
    }
  }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    let payload: any
    try {
      payload = JSON.parse(bulkText.value)
    } catch (e: any) {
      error.value = 'JSON inválido: ' + (e?.message || '')
      return
    }
    const res = await airlineApi.bulkCreateFlights(payload)
    if (res && (res.success || res.createdCount >= 0)) {
      const created = res.createdCount ?? (Array.isArray(res.created) ? res.created.length : 0)
      const errs = res.errorCount ?? (Array.isArray(res.errors) ? res.errors.length : 0)
      success.value = `✅ Carga masiva completada: ${created} creado(s), ${errs} con error.`
      showBulkModal.value = false
      bulkText.value = ''
      await loadInitialData()
      setTimeout(() => success.value = '', 3500)
    } else {
      error.value = res?.error || 'Error en carga masiva'
    }
  } catch (e: any) {
    error.value = e?.message || 'Error en carga masiva'
  } finally {
    loading.value = false
  }
}

const onBulkFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  bulkFile.value = (input.files && input.files[0]) ? input.files[0] : null
}

const submitBulkFile = async () => {
  if (!bulkFile.value) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const res = await airlineApi.bulkCreateFlightsFromFile(bulkFile.value)
    if (res && (res.success || res.createdCount >= 0)) {
      const created = res.createdCount ?? (Array.isArray(res.created) ? res.created.length : 0)
      const errs = res.errorCount ?? (Array.isArray(res.errors) ? res.errors.length : 0)
      success.value = `✅ Carga masiva por archivo: ${created} creado(s), ${errs} con error.`
      showBulkModal.value = false
      bulkText.value = ''
      bulkFile.value = null
      await loadInitialData()
      setTimeout(() => success.value = '', 3500)
    } else {
      error.value = res?.error || 'Error en carga masiva por archivo'
    }
  } catch (e: any) {
    error.value = e?.message || 'Error en carga masiva por archivo'
  } finally {
    loading.value = false
  }
}

const canBeCancelled = (flight: any) => {
  // Un vuelo se puede cancelar si tiene reservas o está programado
  // Para simplificar, asumimos que todos los vuelos se pueden cancelar
  return true
}

const canBeDeleted = (flight: any) => {
  // Un vuelo se puede eliminar si no tiene reservas activas
  // Por ahora, permitimos eliminar todos los vuelos (se validará en el backend)
  return true
}

const getFlightStatus = (flight: any) => {
  // Determinar el estado del vuelo basado en la lógica de negocio
  if (flight.availableSeats === 0) {
    return 'COMPLETED' // Sin asientos disponibles
  } else if (flight.departureDate && new Date(flight.departureDate) < new Date()) {
    return 'COMPLETED' // Fecha de salida pasada
  } else {
    return 'ACTIVE' // Vuelo activo y disponible
  }
}

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'Activo'
    case 'COMPLETED': return 'Completado'
    case 'CANCELLED': return 'Cancelado'
    default: return 'Activo'
  }
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'status-published'
    case 'COMPLETED': return 'status-completed'
    case 'CANCELLED': return 'status-cancelled'
    default: return 'status-published'
  }
}

// Función para reintentar la última operación
const retryLastOperation = async () => {
  if (editingFlight.value) {
    console.log('🔄 Reintentando operación de edición...')
    await saveFlightChanges()
  }
}

// Función para verificar el estado de la base de datos
const checkDatabaseStatus = async () => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('🔍 Verificando estado de la base de datos...')
    
    // Intentar hacer una consulta simple para verificar la conexión
    const response = await airlineApi.getFlights()
    
    if (response && response.success !== false) {
      success.value = '✅ Base de datos funcionando correctamente'
      // Limpiar el error después de 3 segundos
      setTimeout(() => {
        success.value = ''
      }, 3000)
    } else {
      error.value = '❌ Base de datos no responde:\n' + (response?.error || 'Error desconocido')
    }
  } catch (e: any) {
    console.error('💥 Error verificando BD:', e)
    error.value = '❌ No se puede conectar con la base de datos:\n' + e.message
  } finally {
    loading.value = false
  }
}

</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="airline-subtitle">Gestión de Vuelos</h1>
      <div class="flex gap-2">
        <button 
          @click="testCitiesApi" 
          class="btn-airline-secondary"
          :disabled="loading"
        >
          🔍 Test API Ciudades
        </button>
        <button 
          @click="showBulkModal = true" 
          class="btn-airline-secondary"
          :disabled="loading"
        >
          ⬆️ Carga Masiva
        </button>
        <button 
          @click="showCreateForm = !showCreateForm" 
          class="btn-airline-primary"
        >
          {{ showCreateForm ? 'Cancelar' : 'Crear Nuevo Vuelo' }}
        </button>
      </div>
    </div>

    <!-- Información de funcionalidades -->
    <div class="info-card">
      <h3 class="info-title">🚀 Funcionalidades Disponibles</h3>
      <div class="info-grid">
        <div class="info-item">
          <span class="info-icon">✏️</span>
          <span class="info-text"><strong>Editar:</strong> Modificar cualquier campo del vuelo</span>
        </div>
        <div class="info-item">
          <span class="info-icon">🚫</span>
          <span class="info-text"><strong>Cancelar:</strong> Cancelar vuelos con motivo obligatorio</span>
        </div>
        <div class="info-item">
          <span class="info-icon">🗑️</span>
          <span class="info-text"><strong>Eliminar:</strong> Solo vuelos en estado DRAFT</span>
        </div>
        <div class="info-item">
          <span class="info-icon">🔄</span>
          <span class="info-text"><strong>Cambiar Estado:</strong> Entre DRAFT, PUBLISHED, CANCELLED, COMPLETED</span>
        </div>
      </div>
    </div>

    <!-- Mensajes de estado -->
    <div v-if="error" class="airline-card error-card">
      <h3 class="error-title">❌ Errores de Validación:</h3>
      <div class="error-list">
        <div v-for="(errorLine, index) in error.split('\n')" :key="index" class="error-item">
          {{ errorLine }}
        </div>
      </div>
      
      <!-- Botón de reintento para errores de conexión -->
      <div v-if="error.includes('Error de Conexión') || error.includes('JDBC exception')" class="retry-section">
        <button @click="retryLastOperation" class="btn-airline-primary retry-btn">
          🔄 Reintentar Operación
        </button>
        <button @click="checkDatabaseStatus" class="btn-airline-secondary">
          🔍 Verificar Estado de BD
        </button>
      </div>
    </div>
    
    <div v-if="success" class="airline-card" style="background: #f0fdf4; border-color: #bbf7d0;">
      <p style="color: #16a34a;">{{ success }}</p>
    </div>

    <!-- Formulario de creación -->
    <div v-if="showCreateForm" class="airline-card">
      <h2 class="airline-subtitle">Crear Nuevo Vuelo</h2>
      
      <!-- Información básica del vuelo -->
      <div class="grid-container">
        <div class="airline-form-field">
          <label class="airline-form-label">Número de Vuelo *</label>
          <input 
            v-model="flightForm.flightNumber" 
            class="airline-form-input" 
            :class="{ 'error-input': flightForm.flightNumber && flightForm.flightNumber.length < 3 }"
            placeholder="AE001"
          />
          <div v-if="flightForm.flightNumber && flightForm.flightNumber.length < 3" class="field-error">
            El número de vuelo debe tener al menos 3 caracteres
          </div>
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Ciudad de Origen *</label>
          <select v-model="flightForm.originCityId" class="airline-form-input">
            <option value="">Seleccionar ciudad origen</option>
            <option 
              v-for="city in cities" 
              :key="city.idCity" 
              :value="city.idCity"
            >
              {{ city.name }}, {{ city.country }}
            </option>
          </select>
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Ciudad de Destino *</label>
          <select v-model="flightForm.destinationCityId" class="airline-form-input">
            <option value="">Seleccionar ciudad destino</option>
            <option 
              v-for="city in cities" 
              :key="city.idCity" 
              :value="city.idCity"
              :disabled="city.idCity == flightForm.originCityId"
            >
              {{ city.name }}, {{ city.country }}
            </option>
          </select>
        </div>

        <div class="airline-form-field">
          <label class="airline-form-label">Aeronave (Autorrelleno estricto)</label>
          <select v-model="selectedAircraftId" class="airline-form-input">
            <option value="">Sin aeronave</option>
            <option v-for="a in aircrafts" :key="a.idAircraft" :value="a.idAircraft">
              {{ a.registration }} — {{ a.model }} ({{ a.seatCapacity }} asientos)
            </option>
          </select>
          <div v-if="selectedAircraftId" class="info-text">El inventario y precios se llenarán automáticamente y no podrán editarse.</div>
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Fecha de Salida *</label>
          <input 
            v-model="flightForm.departureDate" 
            type="date" 
            class="airline-form-input"
          />
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Hora de Salida *</label>
          <input 
            v-model="flightForm.departureTime" 
            type="time" 
            class="airline-form-input"
          />
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Fecha de Llegada *</label>
          <input 
            v-model="flightForm.arrivalDate" 
            type="date" 
            class="airline-form-input"
          />
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Hora de Llegada *</label>
          <input 
            v-model="flightForm.arrivalTime" 
            type="time" 
            class="airline-form-input"
          />
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Precio Base *</label>
          <input 
            v-model="flightForm.basePrice" 
            type="number" 
            step="0.01" 
            class="airline-form-input" 
            placeholder="150.00"
          />
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Asientos Disponibles *</label>
          <input 
            v-model="flightForm.availableSeats" 
            type="number" 
            min="1" 
            class="airline-form-input" 
            :class="{ 'error-input': flightForm.availableSeats && totalSeats > 0 && Number(flightForm.availableSeats) !== totalSeats }"
            placeholder="150"
          />
          <div v-if="flightForm.availableSeats && totalSeats > 0 && Number(flightForm.availableSeats) !== totalSeats" class="field-error">
            ⚠️ Debe coincidir con el total del inventario ({{ totalSeats }})
          </div>
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Puerta</label>
          <input 
            v-model="flightForm.gate" 
            class="airline-form-input" 
            placeholder="A1"
          />
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Terminal</label>
          <input 
            v-model="flightForm.terminal" 
            class="airline-form-input" 
            placeholder="T1"
          />
        </div>
      </div>

      <!-- Horarios de check-in y embarque -->
      <div class="grid-container">
        <div class="airline-form-field">
          <label class="airline-form-label">Inicio Check-in</label>
          <input 
            v-model="flightForm.checkInStart" 
            type="datetime-local" 
            class="airline-form-input"
          />
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Fin Check-in</label>
          <input 
            v-model="flightForm.checkInEnd" 
            type="datetime-local" 
            class="airline-form-input"
          />
        </div>
        
        <div class="airline-form-field">
          <label class="airline-form-label">Hora de Embarque</label>
          <input 
            v-model="flightForm.boardingTime" 
            type="datetime-local" 
            class="airline-form-input"
          />
        </div>
      </div>

      <!-- Inventario por categoría -->
      <div class="section-divider">
        <h3 class="section-title">Inventario de Asientos</h3>
        <div class="grid-container">
          <div v-for="(seats, category) in inventoryForm" :key="category" class="airline-form-field">
            <label class="airline-form-label">{{ category }}</label>
            <input 
              v-model="inventoryForm[category]" 
              type="number" 
              min="0" 
              class="airline-form-input" 
              :disabled="!!selectedAircraftId"
              :placeholder="category === 'ECONOMY' ? '150' : category === 'BUSINESS' ? '20' : '10'"
            />
          </div>
        </div>
        <p class="info-text">
          Total de asientos: <span class="highlight">{{ totalSeats }}</span>
          <span v-if="totalSeats !== Number(flightForm.availableSeats) && flightForm.availableSeats" class="warning-text">
            ⚠️ No coincide con asientos disponibles ({{ flightForm.availableSeats }})
          </span>
          <button 
            v-if="totalSeats > 0" 
            @click="syncAvailableSeats" 
            type="button" 
            class="sync-btn"
          >
            🔄 Sincronizar
          </button>
        </p>
      </div>

      <!-- Precios por categoría -->
      <div class="section-divider">
        <h3 class="section-title">Precios por Categoría</h3>
        <div class="grid-container">
          <div v-for="(price, category) in faresForm" :key="category" class="airline-form-field">
            <label class="airline-form-label">{{ category }}</label>
            <input 
              v-model="faresForm[category]" 
              type="number" 
              step="0.01" 
              min="0" 
              class="airline-form-input" 
              :disabled="!!selectedAircraftId"
              :placeholder="category === 'ECONOMY' ? '150.00' : category === 'BUSINESS' ? '450.00' : '800.00'"
            />
          </div>
        </div>
      </div>

      <!-- Escalas: apartado removido (lógica manejada de forma diferente) -->

      <!-- Botones de acción -->
      <div class="action-buttons">
        <button @click="resetForm" type="button" class="btn-airline-secondary">
          Limpiar Formulario
        </button>
        <button @click="createFlight" :disabled="loading" class="btn-airline-primary">
          {{ loading ? 'Creando...' : 'Crear Vuelo' }}
        </button>
      </div>
    </div>

    <!-- Lista de vuelos -->
    <div class="airline-card">
      <h2 class="airline-subtitle">Vuelos Existentes</h2>
      
      <div v-if="loading" class="loading-container">
        <p>Cargando vuelos...</p>
      </div>
      
      <div v-else-if="flights.length === 0" class="empty-container">
        <p>No hay vuelos registrados</p>
      </div>
      
      <div v-else class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>Vuelo</th>
              <th>Ruta</th>
              <th>Fecha</th>
              <th>Horario</th>
              <th>Estado</th>
              <th>Asientos</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="flight in flights" :key="flight.idFlight" class="table-row">
              <td class="flight-number">{{ flight.flightNumber || 'N/A' }}</td>
              <td>{{ getRouteName(flight) }}</td>
              <td>{{ formatDate(flight.departureDate) }}</td>
              <td>
                {{ formatTime(flight.departureTime) }} - {{ formatTime(flight.arrivalTime) }}
              </td>
              <td>
                <span :class="getStatusClass(getFlightStatus(flight))">
                  {{ getStatusLabel(getFlightStatus(flight)) }}
                </span>
              </td>
              <td>{{ flight.availableSeats || 'N/A' }}</td>
              <td>
                <div class="action-buttons-small">
                  <!-- Botón de Editar - Siempre visible -->
                  <button 
                    @click="editFlight(flight)"
                    class="status-btn edit-btn"
                    title="Editar vuelo"
                  >
                    ✏️ Editar
                  </button>
                  
                  <!-- Botón de Cancelar - Siempre visible -->
                  <button 
                    @click="cancelFlight(flight)"
                    class="status-btn cancel-btn"
                    title="Cancelar vuelo"
                  >
                    🚫 Cancelar
                  </button>
                  
                  <!-- Botón de Eliminar - Siempre visible -->
                  <button 
                    @click="deleteFlight(flight)"
                    class="status-btn delete-btn"
                    title="Eliminar vuelo"
                  >
                    🗑️ Eliminar
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal de Editar Vuelo -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-content">
        <h3 class="modal-title">Editar Vuelo</h3>
        <div class="modal-body">
          <div class="grid-container">
            <div class="airline-form-field">
              <label class="airline-form-label">Número de Vuelo *</label>
              <input 
                v-model="editForm.flightNumber" 
                class="airline-form-input" 
                :class="{ 'error-input': editForm.flightNumber && editForm.flightNumber.length < 3 }"
              />
              <div v-if="editForm.flightNumber && editForm.flightNumber.length < 3" class="field-error">
                El número de vuelo debe tener al menos 3 caracteres
              </div>
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Estado *</label>
              <select v-model="editForm.status" class="airline-form-input">
                <option value="ACTIVE">Activo</option>
                <option value="COMPLETED">Completado</option>
                <option value="CANCELLED">Cancelado</option>
              </select>
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">
                <span class="required-field">*</span>
                Ciudad de Origen
              </label>
              <select v-model="editForm.originCityId" class="airline-form-input" :class="{ 'error-input': !editForm.originCityId }">
                <option value="">Seleccionar ciudad origen</option>
                <option 
                  v-for="city in cities" 
                  :key="city.idCity" 
                  :value="city.idCity"
                >
                  {{ city.name }}, {{ city.country }}
                </option>
              </select>
              <div v-if="!editForm.originCityId" class="field-error">
                La ciudad de origen es obligatoria
              </div>
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">
                <span class="required-field">*</span>
                Ciudad de Destino
              </label>
              <select v-model="editForm.destinationCityId" class="airline-form-input" :class="{ 'error-input': !editForm.destinationCityId }">
                <option value="">Seleccionar ciudad destino</option>
                <option 
                  v-for="city in cities" 
                  :key="city.idCity" 
                  :value="city.idCity"
                  :disabled="city.idCity == editForm.originCityId"
                >
                  {{ city.name }}, {{ city.country }}
                </option>
              </select>
              <div v-if="!editForm.destinationCityId" class="field-error">
                La ciudad de destino es obligatoria
              </div>
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Fecha de Salida *</label>
              <input 
                v-model="editForm.departureDate" 
                type="date" 
                class="airline-form-input"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Hora de Salida *</label>
              <input 
                v-model="editForm.departureTime" 
                type="time" 
                class="airline-form-input"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Fecha de Llegada *</label>
              <input 
                v-model="editForm.arrivalDate" 
                type="date" 
                class="airline-form-input"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Hora de Llegada *</label>
              <input 
                v-model="editForm.arrivalTime" 
                type="time" 
                class="airline-form-input"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Precio Base *</label>
              <input 
                v-model="editForm.basePrice" 
                type="number" 
                step="0.01" 
                class="airline-form-input" 
                placeholder="150.00"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Asientos Disponibles *</label>
              <input 
                v-model="editForm.availableSeats" 
                type="number" 
                min="1" 
                class="airline-form-input" 
                :class="{ 'error-input': editForm.availableSeats && totalSeats > 0 && Number(editForm.availableSeats) !== totalSeats }"
                placeholder="150"
              />
              <div v-if="editForm.availableSeats && totalSeats > 0 && Number(editForm.availableSeats) !== totalSeats" class="field-error">
                ⚠️ Debe coincidir con el total del inventario ({{ totalSeats }})
              </div>
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Puerta</label>
              <input 
                v-model="editForm.gate" 
                class="airline-form-input" 
                placeholder="A1"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Terminal</label>
              <input 
                v-model="editForm.terminal" 
                class="airline-form-input" 
                placeholder="T1"
              />
            </div>
          </div>

          <!-- Horarios de check-in y embarque -->
          <div class="grid-container">
            <div class="airline-form-field">
              <label class="airline-form-label">Inicio Check-in</label>
              <input 
                v-model="editForm.checkInStart" 
                type="datetime-local" 
                class="airline-form-input"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Fin Check-in</label>
              <input 
                v-model="editForm.checkInEnd" 
                type="datetime-local" 
                class="airline-form-input"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Hora de Embarque</label>
              <input 
                v-model="editForm.boardingTime" 
                type="datetime-local" 
                class="airline-form-input"
              />
            </div>
          </div>

          <!-- Inventario por categoría -->
          <div class="section-divider">
            <h3 class="section-title">Inventario de Asientos</h3>
            <div class="grid-container">
              <div v-for="(seats, category) in inventoryForm" :key="category" class="airline-form-field">
                <label class="airline-form-label">{{ category }}</label>
                <input 
                  v-model="inventoryForm[category]" 
                  type="number" 
                  min="0" 
                  class="airline-form-input" 
                  :placeholder="category === 'ECONOMY' ? '150' : category === 'BUSINESS' ? '20' : '10'"
                />
              </div>
            </div>
            <p class="info-text">
              Total de asientos: <span class="highlight">{{ totalSeats }}</span>
              <span v-if="totalSeats !== Number(editForm.availableSeats) && editForm.availableSeats" class="warning-text">
                ⚠️ No coincide con asientos disponibles ({{ editForm.availableSeats }})
              </span>
              <button 
                v-if="totalSeats > 0" 
                @click="editForm.availableSeats = totalSeats.toString()" 
                type="button" 
                class="sync-btn"
              >
                🔄 Sincronizar
              </button>
            </p>
          </div>

          <!-- Precios por categoría -->
          <div class="section-divider">
            <h3 class="section-title">Precios por Categoría</h3>
            <div class="grid-container">
              <div v-for="(price, category) in faresForm" :key="category" class="airline-form-field">
                <label class="airline-form-label">{{ category }}</label>
                <input 
                  v-model="faresForm[category]" 
                  type="number" 
                  step="0.01" 
                  min="0" 
                  class="airline-form-input" 
                  :placeholder="category === 'ECONOMY' ? '150.00' : category === 'BUSINESS' ? '450.00' : '800.00'"
                />
              </div>
            </div>
          </div>

          <!-- Escalas en edición: apartado removido (lógica manejada de forma diferente) -->

          <!-- Botones de acción -->
          <div class="action-buttons">
            <button @click="closeEditModal" type="button" class="btn-airline-secondary">
              Cancelar
            </button>
            <button @click="saveFlightChanges" :disabled="loading" class="btn-airline-primary">
              {{ loading ? 'Guardando...' : 'Guardar Cambios' }}
            </button>
          </div>

          <!-- Cambio rápido de estado -->
          <div class="quick-status-change">
            <h4>Cambio Rápido de Estado:</h4>
            <div class="status-buttons">
              <button 
                @click="editForm.status = 'ACTIVE'"
                :disabled="editForm.status === 'ACTIVE'"
                class="status-btn status-published"
              >
                Activo
              </button>
              <button 
                @click="editForm.status = 'COMPLETED'"
                :disabled="editForm.status === 'COMPLETED'"
                class="status-btn status-completed"
              >
                Completado
              </button>
              <button 
                @click="editForm.status = 'CANCELLED'"
                :disabled="editForm.status === 'CANCELLED'"
                class="status-btn status-cancelled"
              >
                Cancelado
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Cancelar Vuelo -->
    <div v-if="showCancelModal" class="modal-overlay">
      <div class="modal-content">
        <h3 class="modal-title">Confirmar Cancelación</h3>
        <div class="modal-body">
          <p>¿Estás seguro de que quieres cancelar el vuelo "{{ cancellingFlight?.flightNumber || cancellingFlight?.idFlight }}"?</p>
          <p>Razón de cancelación:</p>
          <textarea v-model="cancelForm.reason" class="airline-form-input" rows="4" cols="30"></textarea>
        </div>
        <div class="action-buttons">
          <button @click="closeCancelModal" type="button" class="btn-airline-secondary">
            Cancelar
          </button>
          <button @click="confirmCancelFlight" :disabled="loading" class="btn-airline-danger">
            {{ loading ? 'Cancelando...' : 'Confirmar Cancelación' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal de Eliminar Vuelo -->
    <div v-if="showDeleteModal" class="modal-overlay">
      <div class="modal-content">
        <h3 class="modal-title">Confirmar Eliminación</h3>
        <div class="modal-body">
          <p>¿Estás seguro de que quieres eliminar el vuelo "{{ deletingFlight?.flightNumber || deletingFlight?.idFlight }}"?</p>
          <p>Esta acción no se puede deshacer.</p>
        </div>
        <div class="action-buttons">
          <button @click="closeDeleteModal" type="button" class="btn-airline-secondary">
            Cancelar
          </button>
          <button @click="confirmDeleteFlight" :disabled="loading" class="btn-airline-danger">
            {{ loading ? 'Eliminando...' : 'Confirmar Eliminación' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal de Carga Masiva -->
  <div v-if="showBulkModal" class="modal-overlay">
    <div class="modal-content">
      <h3 class="modal-title">Carga Masiva de Vuelos</h3>
      <div class="modal-body">
        <p class="info-text">Pega un arreglo JSON de vuelos o un objeto { flights: [...] }.</p>
        <textarea v-model="bulkText" rows="12" class="airline-form-input" placeholder='[\n  {"flightNumber":"O123","originCityId":1,"destinationCityId":2,"departureDate":"2025-09-01","departureTime":"08:00","arrivalDate":"2025-09-01","arrivalTime":"10:30","fares":{"ECONOMY":200,"BUSINESS":400},"availableSeats":180,"createdBy":1}\n]'></textarea>
        <div class="grid-container" style="margin-top: 0.75rem;">
          <div class="airline-form-field">
            <label class="airline-form-label">O selecciona archivo .json</label>
            <input type="file" accept="application/json,.json" class="airline-form-input" @change="onBulkFileChange" />
          </div>
        </div>
        <div class="action-buttons">
          <button class="btn-airline-secondary" @click="showBulkModal=false">Cancelar</button>
          <button class="btn-airline-primary" :disabled="loading" @click="submitBulk">
            {{ loading ? 'Procesando...' : 'Crear Vuelos' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.space-y-6 > * + * {
  margin-top: 1.5rem;
}

.flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.items-center {
  align-items: center;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.section-divider {
  border-top: 1px solid #e2e8f0;
  padding-top: 1.5rem;
  margin-top: 1.5rem;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--primary-blue);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.leg-container {
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  padding: 1rem;
  margin-bottom: 1rem;
}

.leg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.leg-title {
  font-weight: 600;
  color: var(--primary-blue);
}

.remove-btn {
  color: #dc2626;
  background: none;
  border: none;
  font-size: 0.875rem;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

.remove-btn:hover {
  background: #fef2f2;
}

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.checkbox-input {
  margin: 0;
}

.checkbox-label {
  font-size: 0.875rem;
  color: var(--neutral-dark);
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.info-text {
  font-size: 0.875rem;
  color: var(--neutral-dark);
  margin-top: 0.5rem;
}

.highlight {
  font-weight: 600;
  color: var(--primary-blue);
}

.warning-text {
  color: #f59e0b; /* Amarillo para advertencias */
  font-size: 0.875rem;
  margin-left: 0.5rem;
}

.loading-container,
.empty-container {
  text-align: center;
  padding: 2rem;
  color: var(--neutral-dark);
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.data-table th {
  font-weight: 600;
  color: var(--primary-blue);
  background: #f8fafc;
}

.table-row:hover {
  background: #f8fafc;
}

.flight-number {
  font-weight: 600;
  color: var(--primary-blue);
}

.action-buttons-small {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.action-buttons-small .status-btn {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  min-width: 80px;
  text-align: center;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
}

.action-buttons-small .status-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.action-buttons-small .status-btn:hover::before {
  left: 100%;
}

.action-buttons-small .status-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.action-buttons-small .status-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.status-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.publish-btn {
  background: #10b981;
  color: white;
}

.publish-btn:hover {
  background: #059669;
}

.cancel-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  border: 2px solid #ef4444;
}

.cancel-btn:hover {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  border-color: #dc2626;
}

.complete-btn {
  background: #3b82f6;
  color: white;
}

.complete-btn:hover {
  background: #2563eb;
}

.edit-btn {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  border: 2px solid #f59e0b;
}

.edit-btn:hover {
  background: linear-gradient(135deg, #d97706, #b45309);
  border-color: #d97706;
}

.delete-btn {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  color: white;
  border: 2px solid #dc2626;
}

.delete-btn:hover {
  background: linear-gradient(135deg, #b91c1c, #991b1b);
  border-color: #b91c1c;
}

/* Estados de vuelo */
.status-draft {
  background: #f59e0b;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-published {
  background: #10b981;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-cancelled {
  background: #ef4444;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-completed {
  background: #3b82f6;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.error-card {
  background: #fef2f2;
  border: 1px solid #fecaca;
  padding: 1rem;
  border-radius: 0.5rem;
  margin-bottom: 1.5rem;
}

.error-title {
  font-size: 1rem;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 0.75rem;
}

.error-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.error-item {
  font-size: 0.875rem;
  color: #dc2626;
  margin-bottom: 0.25rem;
}

.field-error {
  color: #dc2626;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.error-input {
  border-color: #dc2626;
  border-width: 1px;
}

.sync-btn {
  background: #4f46e5;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  text-transform: uppercase;
  transition: background-color 0.2s ease;
}

.sync-btn:hover {
  background: #4338ca;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.3s ease;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  width: 95%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  animation: slideIn 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.modal-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--primary-blue);
  margin-bottom: 1.5rem;
  text-align: center;
  position: relative;
  padding-bottom: 1rem;
}

.modal-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-blue), #60a5fa);
  border-radius: 2px;
}

.modal-body {
  margin-bottom: 2rem;
}

.modal-body .grid-container {
  gap: 1.5rem;
}

.modal-body .airline-form-field {
  margin-bottom: 1.5rem;
}

.modal-body .airline-form-label {
  font-weight: 600;
  color: var(--neutral-dark);
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.modal-body .airline-form-input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 1rem;
  color: var(--neutral-dark);
  transition: all 0.3s ease;
  background: #f8fafc;
}

.modal-body .airline-form-input:focus {
  outline: none;
  border-color: var(--primary-blue);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
  background: white;
  transform: translateY(-1px);
}

.modal-body .airline-form-input:hover {
  border-color: #cbd5e1;
  background: white;
}

/* Animaciones */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { 
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to { 
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-body .action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding-top: 0;
  border-top: none;
}

.modal-body .action-buttons .btn-airline-secondary {
  background: linear-gradient(135deg, #e5e7eb, #d1d5db);
  color: var(--neutral-dark);
  border: 2px solid #e5e7eb;
  padding: 0.875rem 1.5rem;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.modal-body .action-buttons .btn-airline-secondary:hover {
  background: linear-gradient(135deg, #d1d5db, #9ca3af);
  border-color: #d1d5db;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.modal-body .action-buttons .btn-airline-primary {
  background: linear-gradient(135deg, var(--primary-blue), #2563eb);
  color: white;
  border: 2px solid var(--primary-blue);
  padding: 0.875rem 1.5rem;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.modal-body .action-buttons .btn-airline-primary:hover {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.modal-body .action-buttons .btn-airline-danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  border: 2px solid #ef4444;
  padding: 0.875rem 1.5rem;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.modal-body .action-buttons .btn-airline-danger:hover {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  border-color: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.modal-body .info-text {
  font-size: 0.875rem;
  color: var(--neutral-dark);
  margin-top: 0.5rem;
}

.modal-body .highlight {
  font-weight: 600;
  color: var(--primary-blue);
}

.modal-body .warning-text {
  color: #f59e0b;
  font-size: 0.875rem;
  margin-left: 0.5rem;
}

.modal-body .sync-btn {
  background: #4f46e5;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  text-transform: uppercase;
  transition: background-color 0.2s ease;
}

.modal-body .sync-btn:hover {
  background: #4338ca;
}

.modal-body .edit-btn {
  background: #f59e0b;
  color: white;
}

.modal-body .edit-btn:hover {
  background: #d97706;
}

.modal-body .delete-btn {
  background: #ef4444;
  color: white;
}

.modal-body .delete-btn:hover {
  background: #dc2626;
}

.quick-status-change {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 12px;
  padding: 1.5rem;
}

.quick-status-change h4 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--neutral-dark);
  margin-bottom: 1rem;
  text-align: center;
  position: relative;
}

.quick-status-change h4::after {
  content: '';
  position: absolute;
  bottom: -0.5rem;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-blue), #60a5fa);
  border-radius: 1px;
}

.status-buttons {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  justify-content: center;
}

.status-buttons .status-btn {
  padding: 0.75rem 1.25rem;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border: 2px solid transparent;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  min-width: 100px;
}

.status-buttons .status-btn:hover {
  opacity: 1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.status-buttons .status-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.info-card {
  background: linear-gradient(135deg, #e0f2fe, #bbdefb);
  border: 2px solid #90caf9;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #2196f3, #64b5f6, #90caf9);
}

.info-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1565c0;
  margin-bottom: 1.5rem;
  text-align: center;
  position: relative;
}

.info-title::after {
  content: '';
  position: absolute;
  bottom: -0.5rem;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #2196f3, #64b5f6);
  border-radius: 2px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.95rem;
  color: #1976d2;
  background: rgba(255, 255, 255, 0.7);
  padding: 1rem;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.info-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.info-icon {
  font-size: 1.5rem;
  color: #2196f3;
  background: rgba(33, 150, 243, 0.1);
  padding: 0.5rem;
  border-radius: 50%;
  min-width: 2.5rem;
  height: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .grid-container {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .section-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
}

.modal-body .field-error {
  color: #dc2626;
  font-size: 0.875rem;
  margin-top: 0.5rem;
  padding: 0.5rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.modal-body .field-error::before {
  content: '⚠️';
  font-size: 1rem;
}

.modal-body .checkbox-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 0.75rem;
  padding: 0.75rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.modal-body .checkbox-input {
  margin: 0;
  width: 1.25rem;
  height: 1.25rem;
  accent-color: var(--primary-blue);
  cursor: pointer;
}

.modal-body .checkbox-label {
  font-size: 0.9rem;
  color: var(--neutral-dark);
  font-weight: 500;
  cursor: pointer;
  user-select: none;
}

.modal-body .info-text {
  font-size: 0.9rem;
  color: var(--neutral-dark);
  margin-top: 0.75rem;
  padding: 0.75rem;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.modal-body .highlight {
  font-weight: 700;
  color: var(--primary-blue);
  background: rgba(59, 130, 246, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.modal-body .warning-text {
  color: #f59e0b;
  font-size: 0.875rem;
  font-weight: 500;
  background: rgba(245, 158, 11, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.modal-body .sync-btn {
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  text-transform: uppercase;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.modal-body .sync-btn:hover {
  background: linear-gradient(135deg, #4338ca, #3730a3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.required-field {
  color: #ef4444;
  font-weight: 700;
  margin-right: 0.25rem;
}

.error-input {
  border-color: #ef4444 !important;
  background: #fef2f2 !important;
}

.error-input:focus {
  border-color: #ef4444 !important;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1) !important;
}

.retry-section {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #fecaca;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.retry-btn {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border: 2px solid #10b981;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.retry-btn:hover {
  background: linear-gradient(135deg, #059669, #047857);
  border-color: #059669;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.error-card {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  border: 2px solid #fecaca;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.error-title {
  color: #dc2626;
  font-size: 1.125rem;
  font-weight: 700;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.error-list {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid #fecaca;
}

.error-item {
  color: #dc2626;
  margin-bottom: 0.5rem;
  font-family: monospace;
  font-size: 0.875rem;
  line-height: 1.4;
}
</style>
