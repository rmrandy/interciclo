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

// Estados de la interfaz
const showCreateForm = ref(false)
const selectedFlight = ref<any>(null)
const showEditForm = ref(false)

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
      createdBy: JSON.parse(localStorage.getItem('user') || '{}').idUser || 1
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
  try {
    const result = await airlineApi.updateFlightStatus(flightId, newStatus)
    if (result.success) {
      await loadInitialData()
      success.value = `Estado del vuelo actualizado a ${newStatus}`
    }
  } catch (e: any) {
    error.value = 'Error actualizando estado del vuelo'
  }
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

// Obtener clase CSS para el estado del vuelo
const getStatusClass = (status: string) => {
  const baseClass = 'status-badge'
  switch (status) {
    case 'DRAFT':
      return `${baseClass} status-draft`
    case 'PUBLISHED':
      return `${baseClass} status-published`
    case 'CANCELLED':
      return `${baseClass} status-cancelled`
    case 'COMPLETED':
      return `${baseClass} status-completed`
    default:
      return baseClass
  }
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
          @click="showCreateForm = !showCreateForm" 
          class="btn-airline-primary"
        >
          {{ showCreateForm ? 'Cancelar' : 'Crear Nuevo Vuelo' }}
        </button>
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
              :placeholder="category === 'ECONOMY' ? '150.00' : category === 'BUSINESS' ? '450.00' : '800.00'"
            />
          </div>
        </div>
      </div>

      <!-- Escalas -->
      <div class="section-divider">
        <div class="section-header">
          <h3 class="section-title">Escalas (Opcional)</h3>
          <button @click="addLeg" type="button" class="btn-airline-secondary">
            + Agregar Escala
          </button>
        </div>
        
        <div v-for="(leg, index) in legsForm" :key="index" class="leg-container">
          <div class="leg-header">
            <span class="leg-title">Escala {{ leg.legOrder }}</span>
            <button 
              @click="removeLeg(index)" 
              type="button" 
              class="remove-btn"
              v-if="legsForm.length > 1"
            >
              Eliminar
            </button>
          </div>
          
          <div class="grid-container">
            <div class="airline-form-field">
              <label class="airline-form-label">Ciudad de Escala</label>
              <select v-model="leg.cityId" class="airline-form-input">
                <option value="">Seleccionar ciudad</option>
                <option 
                  v-for="city in cities" 
                  :key="city.idCity" 
                  :value="city.idCity"
                  :disabled="city.idCity == flightForm.originCityId || city.idCity == flightForm.destinationCityId"
                >
                  {{ city.name }}, {{ city.country }}
                </option>
              </select>
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Hora de Llegada</label>
              <input 
                v-model="leg.arrivalTime" 
                type="datetime-local" 
                class="airline-form-input"
              />
            </div>
            
            <div class="airline-form-field">
              <label class="airline-form-label">Hora de Salida</label>
              <input 
                v-model="leg.departureTime" 
                type="datetime-local" 
                class="airline-form-input"
              />
            </div>
            
            <div class="checkbox-container">
              <input 
                v-model="leg.aircraftChange" 
                type="checkbox" 
                id="aircraftChange"
                class="checkbox-input"
              />
              <label for="aircraftChange" class="checkbox-label">Cambio de Aeronave</label>
            </div>
          </div>
        </div>
      </div>

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
                <span :class="getStatusClass(flight.status)">
                  {{ flight.status || 'N/A' }}
                </span>
              </td>
              <td>{{ flight.availableSeats || 'N/A' }}</td>
              <td>
                <div class="action-buttons-small">
                  <button 
                    v-if="flight.status === 'DRAFT'"
                    @click="changeFlightStatus(flight.idFlight, 'PUBLISHED')"
                    class="status-btn publish-btn"
                  >
                    Publicar
                  </button>
                  <button 
                    v-if="flight.status === 'PUBLISHED'"
                    @click="changeFlightStatus(flight.idFlight, 'CANCELLED')"
                    class="status-btn cancel-btn"
                  >
                    Cancelar
                  </button>
                  <button 
                    v-if="flight.status === 'PUBLISHED'"
                    @click="changeFlightStatus(flight.idFlight, 'COMPLETED')"
                    class="status-btn complete-btn"
                  >
                    Completar
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
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
}

.status-btn {
  padding: 0.25rem 0.5rem;
  border: none;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  cursor: pointer;
  text-transform: uppercase;
  font-weight: 600;
}

.publish-btn {
  background: #10b981;
  color: white;
}

.publish-btn:hover {
  background: #059669;
}

.cancel-btn {
  background: #ef4444;
  color: white;
}

.cancel-btn:hover {
  background: #dc2626;
}

.complete-btn {
  background: #3b82f6;
  color: white;
}

.complete-btn:hover {
  background: #2563eb;
}

.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-draft {
  background: #fef3c7;
  color: #92400e;
}

.status-published {
  background: #d1fae5;
  color: #065f46;
}

.status-cancelled {
  background: #fee2e2;
  color: #991b1b;
}

.status-completed {
  background: #e0e7ff;
  color: #3730a3;
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
</style>
