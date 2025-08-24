<template>
  <div class="flight-search-container">
    <!-- Header con logo y navegación -->
    <header class="flight-header">
      <div class="header-content">
        <div class="logo-section">
          <div class="logo">✈️</div>
          <h1>AeroLinea</h1>
        </div>
        <nav class="nav-links">
          <a href="/" class="nav-link">Inicio</a>
          <a href="/flights" class="nav-link active">Vuelos</a>
          <CartCounter />
          <a href="/profile" class="nav-link">Mi Cuenta</a>
        </nav>
      </div>
    </header>

    <!-- Sección de búsqueda principal -->
    <div class="search-section">
      <div class="search-container">
        <!-- Selector de tipo de vuelo -->
        <div class="flight-type-section">
          <FlightTypeSelector 
            v-model="searchParams.flightType"
            @type-changed="handleFlightTypeChange"
          />
        </div>
        
        <div class="search-form">
          <div class="form-row-horizontal">
            <div class="form-group">
              <label>Origen</label>
              <div class="input-with-icon">
                <span class="input-icon">✈️</span>
                <select v-model="searchParams.origin" class="form-select">
                  <option value="">Seleccionar origen</option>
                  <option v-for="city in cities" :key="city.idCity" :value="city.idCity">
                    {{ city.name }}, {{ city.country }}
                  </option>
                </select>
              </div>
            </div>
            
            <div class="form-divider"></div>
            
            <div class="form-group">
              <label>Destino</label>
              <div class="input-with-icon">
                <span class="input-icon">✈️</span>
                <select v-model="searchParams.destination" class="form-select">
                  <option value="">Seleccionar destino</option>
                  <option v-for="city in cities" :key="city.idCity" :value="city.idCity">
                    {{ city.name }}, {{ city.country }}
                  </option>
                </select>
              </div>
            </div>
            
            <div class="form-divider"></div>
            
            <div class="form-group date-range-group">
              <label>Fechas de viaje</label>
              <DateRangePicker 
                :departure-date="searchParams.departureDate"
                :return-date="searchParams.returnDate"
                :flight-type="searchParams.flightType"
                @update:departure-date="searchParams.departureDate = $event"
                @update:return-date="searchParams.returnDate = $event"
                label="Seleccionar fechas"
              />
            </div>
            
            <div class="form-divider"></div>
            
            <div class="form-group">
              <label>Pasajeros</label>
              <div class="input-with-icon">
                <span class="input-icon">👤</span>
                <select v-model="searchParams.passengers" class="form-select">
                  <option value="1">1</option>
                  <option value="2">2</option>
                  <option value="3">3</option>
                  <option value="4">4</option>
                </select>
                <span class="dropdown-arrow">▼</span>
              </div>
            </div>
            
            <div class="search-actions">
              <button @click="searchFlights" class="search-btn" :disabled="loading">
                {{ loading ? 'Buscando...' : 'Buscar' }}
              </button>
            </div>
          </div>
          
          <div class="secondary-actions">
            <button @click="showAllFlights" class="show-all-btn" :disabled="loading">
              {{ loading ? 'Cargando...' : '📋 Ver Todos los Vuelos' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Resultados de búsqueda -->
    <div v-if="searchPerformed" class="results-section">
      <!-- Mensaje informativo cuando no hay filtros -->
      <div v-if="!searchParams.origin && !searchParams.destination && !searchParams.departureDate" class="info-message">
        <div class="info-icon">ℹ️</div>
        <div class="info-content">
          <h3>Explorando todos los vuelos</h3>
          <p>Usa los filtros de arriba para refinar tu búsqueda o selecciona un filtro específico abajo.</p>
        </div>
      </div>
      
      <div class="results-header">
        <h2 v-if="searchParams.origin && searchParams.destination">
          Vuelos de {{ getCityName(searchParams.origin) }} a {{ getCityName(searchParams.destination) }}
        </h2>
        <h2 v-else-if="searchParams.origin">
          Vuelos desde {{ getCityName(searchParams.origin) }}
        </h2>
        <h2 v-else-if="searchParams.destination">
          Vuelos hacia {{ getCityName(searchParams.destination) }}
        </h2>
        <h2 v-else>
          Todos los Vuelos Disponibles
        </h2>
        <div class="results-info">
          <span v-if="searchParams.departureDate" class="date-info">
            {{ formatSearchDate(searchParams.departureDate) }}
          </span>
          <span v-if="searchParams.passengers" class="passenger-info">
            {{ searchParams.passengers }} {{ searchParams.passengers === 1 ? 'Adulto' : 'Adultos' }}
          </span>
          <span v-if="!searchParams.origin && !searchParams.destination && !searchParams.departureDate" class="date-info">
            Mostrando todos los vuelos disponibles
          </span>
        </div>
        <div class="progress-bar">
          <div class="progress-step active">Paso 1 de 3</div>
          <div class="progress-fill"></div>
        </div>
      </div>

      <!-- Filtros y ordenamiento -->
      <div class="filters-section">
        <div class="filter-buttons">
          <button 
            v-for="filter in filters" 
            :key="filter.value"
            @click="setActiveFilter(filter.value)"
            :class="['filter-btn', { active: activeFilter === filter.value }]"
          >
            {{ filter.label }}
          </button>
        </div>
        <div class="sort-section">
          <label>Ordenar por:</label>
          <select v-model="sortBy" class="sort-select">
            <option value="price">Mejor precio</option>
            <option value="duration">Duración</option>
            <option value="departure">Hora de salida</option>
          </select>
        </div>
      </div>

      <!-- Lista de vuelos -->
      <div class="flights-list">
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>Buscando vuelos disponibles...</p>
        </div>
        
        <div v-else-if="filteredFlights.length === 0" class="no-flights">
          <div class="no-flights-icon">✈️</div>
          <h3>No se encontraron vuelos</h3>
          <p>Intenta cambiar las fechas o destinos de búsqueda</p>
        </div>

        <div v-else class="flight-cards">
          <div 
            v-for="flight in sortedFlights" 
            :key="flight.idFlight"
            class="flight-card"
            :class="{ 'best-price': flight.isBestPrice }"
          >
            <!-- Información del vuelo -->
            <div class="flight-info">
              <div class="flight-times">
                <div class="departure">
                  <div class="time">{{ formatTime(flight.departureTime) || '--:--' }}</div>
                  <div class="city">{{ flight.originCity }}</div>
                  <div class="date">{{ formatDate(flight.departureDate) }}</div>
                </div>
                
                <div class="flight-duration">
                  <div class="duration-line">
                    <div class="line"></div>
                    <div class="plane-icon">✈️</div>
                  </div>
                  <div class="duration-text">
                    {{ calculateDuration(flight) }}
                  </div>
                  <div class="stops">
                    <span v-if="flight.hasStops && flight.flightLegs && flight.flightLegs.length > 0">
                      {{ flight.flightLegs.length }} escala{{ flight.flightLegs.length > 1 ? 's' : '' }}
                    </span>
                    <span v-else>Directo</span>
                  </div>
                </div>
                
                <div class="arrival">
                  <div class="time">{{ formatTime(flight.arrivalTime) || '--:--' }}</div>
                  <div class="city">{{ flight.destinationCity }}</div>
                  <div class="date">{{ formatDate(flight.arrivalDate) || formatDate(flight.departureDate) }}</div>
                </div>
              </div>
              
              <div class="flight-details">
                <div class="airline">Operado por AeroLinea</div>
                <div class="flight-number">Vuelo {{ flight.flightNumber }}</div>
                <div class="seats">Asientos disponibles: {{ flight.availableSeats }}</div>
              </div>
              
              <!-- Información de escalas -->
              <div v-if="flight.hasStops && flight.flightLegs && flight.flightLegs.length > 0" class="flight-stops">
                <div class="stops-header">
                  <span class="stops-icon">🛫</span>
                  <span class="stops-title">Escalas del vuelo</span>
                </div>
                <div class="stops-list">
                  <div 
                    v-for="(leg, index) in flight.flightLegs" 
                    :key="index"
                    class="stop-item"
                  >
                    <div class="stop-info">
                      <div class="stop-city">{{ getCityName(leg.cityId) }}</div>
                      <div class="stop-times">
                        <span class="arrival-time">Llegada: {{ formatTime(leg.arrivalTime) }}</span>
                        <span class="departure-time">Salida: {{ formatTime(leg.departureTime) }}</span>
                      </div>
                      <div class="connection-time">
                        Tiempo de conexión: {{ leg.connectionTimeMinutes }} min
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Precio y botón de reserva -->
            <div class="flight-pricing">
              <div class="price-section">
                <div v-if="flight.isBestPrice" class="best-price-tag">Mejor precio</div>
                <div class="price">USD {{ formatPrice(flight.basePrice) }}</div>
                <div class="price-detail">por pasajero</div>
              </div>
              <div class="action-buttons">
                <button @click="viewFlightDetails(flight)" class="details-btn">
                  📋 Ver Detalles
                </button>
                <button @click="bookFlight(flight)" class="book-btn">
                  Seleccionar
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Estado inicial -->
    <div v-if="!searchPerformed" class="initial-state">
      <div class="welcome-section">
        <div class="welcome-icon">✈️</div>
        <h2>Encuentra tu vuelo ideal</h2>
        <p>Busca entre miles de vuelos disponibles a destinos increíbles</p>
      </div>
      
      <!-- Calendario de disponibilidad -->
      <div class="availability-section">
        <FlightAvailabilityCalendar 
          :flights="flights"
          :selected-date="searchParams.departureDate"
          :on-date-select="handleDateSelect"
        />
      </div>
    </div>

    <!-- Debug info (solo en desarrollo) -->
    <div v-if="showDebug" class="debug-section">
      <h3>🐛 Debug Info</h3>
      <p>Vuelos cargados: {{ flights.length }}</p>
      <p>Filtrados: {{ filteredFlights.length }}</p>
      <p>Búsqueda realizada: {{ searchPerformed }}</p>
      <button @click="toggleDebug" class="debug-btn">Ocultar Debug</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { airlineApi } from '../utils/airlineApi'
import CartCounter from '../components/CartCounter.vue'
import DatePicker from '../components/DatePicker.vue'
import DateRangePicker from '../components/DateRangePicker.vue'
import FlightAvailabilityCalendar from '../components/FlightAvailabilityCalendar.vue'
import FlightTypeSelector from '../components/FlightTypeSelector.vue'

const router = useRouter()

// Estados reactivos
const loading = ref(false)
const error = ref('')
const flights = ref<any[]>([])
const cities = ref<any[]>([])
const searchPerformed = ref(false)
const showDebug = ref(false)

// Parámetros de búsqueda
const searchParams = ref({
  origin: '',
  destination: '',
  departureDate: '',
  returnDate: '',
  passengers: 1,
  flightType: 'round-trip' // 'one-way', 'return-only', 'round-trip'
})

// Filtros y ordenamiento
const activeFilter = ref('all')
const sortBy = ref('price')

const filters = [
  { label: 'Todos', value: 'all' },
  { label: 'Directos', value: 'direct' },
  { label: 'Con escalas', value: 'stops' },
  { label: 'Económico', value: 'economy' }
]

// Computed properties
const filteredFlights = computed(() => {
  if (!searchPerformed.value) return []
  
  let filtered = flights.value.filter(flight => 
    flight.status !== 'DRAFT' && flight.status !== 'CANCELLED'
  )
  
  // Aplicar filtros de búsqueda si existen
  if (searchParams.value.origin) {
    filtered = filtered.filter(flight => 
      flight.originCityId === searchParams.value.origin || 
      flight.originCity === searchParams.value.origin
    )
  }
  
  if (searchParams.value.destination) {
    filtered = filtered.filter(flight => 
      flight.destinationCityId === searchParams.value.destination || 
      flight.destinationCity === searchParams.value.destination
    )
  }
  
  if (searchParams.value.departureDate) {
    filtered = filtered.filter(flight => 
      flight.departureDate === searchParams.value.departureDate
    )
  }
  
  if (searchParams.value.returnDate) {
    // Para vuelos de ida y vuelta, buscar vuelos que regresen en esa fecha
    // Por ahora solo filtramos por fecha de salida
    filtered = filtered.filter(flight => 
      flight.departureDate === searchParams.value.returnDate
    )
  }
  
  // Aplicar filtros de tipo de vuelo
  if (activeFilter.value === 'direct') {
    filtered = filtered.filter(flight => !flight.hasStops)
  } else if (activeFilter.value === 'stops') {
    filtered = filtered.filter(flight => flight.hasStops)
  } else if (activeFilter.value === 'economy') {
    // Filtrar por vuelos económicos (precio menor al promedio)
    const avgPrice = filtered.reduce((sum, f) => sum + f.basePrice, 0) / filtered.length
    filtered = filtered.filter(flight => flight.basePrice <= avgPrice)
  }
  
  return filtered
})

const sortedFlights = computed(() => {
  const sorted = [...filteredFlights.value]
  
  switch (sortBy.value) {
    case 'price':
      sorted.sort((a, b) => a.basePrice - b.basePrice)
      break
    case 'duration':
      sorted.sort((a, b) => calculateDurationMinutes(a) - calculateDurationMinutes(b))
      break
    case 'departure':
      sorted.sort((a, b) => (a.departureTime || '').localeCompare(b.departureTime || ''))
      break
  }
  
  // Marcar el mejor precio
  if (sorted.length > 0) {
    const bestPrice = Math.min(...sorted.map(f => f.basePrice))
    sorted.forEach(flight => {
      flight.isBestPrice = flight.basePrice === bestPrice
    })
  }
  
  return sorted
})

// Métodos
const loadInitialData = async () => {
  try {
    loading.value = true
    error.value = ''
    
    console.log('🔄 Cargando ciudades...')
    const citiesResponse = await airlineApi.getCities()
    if (citiesResponse && Array.isArray(citiesResponse)) {
      cities.value = citiesResponse
      console.log('✅ Ciudades cargadas:', cities.value.length)
    }
    
    console.log('🔄 Cargando vuelos...')
    const flightsResponse = await airlineApi.getFlights()
    if (flightsResponse && flightsResponse.success) {
      flights.value = flightsResponse.flights || []
      console.log('✅ Vuelos cargados:', flights.value.length)
    }
  } catch (e: any) {
    console.error('❌ Error cargando datos:', e)
    error.value = 'Error cargando datos: ' + (e?.message || 'Error desconocido')
  } finally {
    loading.value = false
  }
}

const searchFlights = async () => {
  // Validar que al menos haya un criterio de búsqueda
  const hasSearchCriteria = searchParams.value.origin || 
                           searchParams.value.destination || 
                           searchParams.value.departureDate ||
                           searchParams.value.returnDate
  
  if (!hasSearchCriteria) {
    // Si no hay criterios, mostrar todos los vuelos disponibles
    searchPerformed.value = true
    error.value = ''
    console.log('🔍 Mostrando todos los vuelos disponibles')
    return
  }
  
  // Validar que si hay origen y destino, sean diferentes
  if (searchParams.value.origin && searchParams.value.destination && 
      searchParams.value.origin === searchParams.value.destination) {
    error.value = 'El origen y destino no pueden ser iguales'
    return
  }
  
  searchPerformed.value = true
  error.value = ''
  
  console.log('🔍 Búsqueda realizada:', searchParams.value)
}

const showAllFlights = async () => {
  searchParams.value = {
    origin: '',
    destination: '',
    departureDate: '',
    returnDate: '',
    passengers: 1
  }
  searchPerformed.value = true
  error.value = ''
  console.log('🔍 Mostrando todos los vuelos disponibles')
}

const setActiveFilter = (filter: string) => {
  activeFilter.value = filter
}

const bookFlight = (flight: any) => {
  console.log('🎫 Reservando vuelo:', flight)
  router.push(`/book-flight/${flight.idFlight}`)
}

const viewFlightDetails = (flight: any) => {
  console.log('📋 Viendo detalles del vuelo:', flight)
  router.push(`/flight-details/${flight.idFlight}`)
}

// Funciones auxiliares
const getCityName = (cityId: number | string) => {
  const city = cities.value.find(c => c.idCity === cityId)
  return city ? city.name : 'Ciudad'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '--'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('es-ES', { 
      weekday: 'short', 
      day: 'numeric', 
      month: 'short' 
    })
  } catch {
    return '--'
  }
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '--:--'
  return timeStr
}

const formatSearchDate = (dateStr: string) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('es-ES', { 
      weekday: 'long', 
      day: 'numeric', 
      month: 'long',
      year: 'numeric'
    })
  } catch {
    return dateStr
  }
}

const getTodayString = () => {
  const today = new Date()
  return today.toISOString().split('T')[0]
}

const handleDateSelect = (date: string) => {
  searchParams.value.departureDate = date
  searchPerformed.value = true
  error.value = ''
  console.log('📅 Fecha seleccionada del calendario:', date)
}

const handleFlightTypeChange = (type: string) => {
  console.log('✈️ Tipo de vuelo cambiado a:', type)
  
  // Limpiar fechas según el tipo seleccionado
  if (type === 'one-way') {
    searchParams.value.returnDate = ''
  } else if (type === 'return-only') {
    searchParams.value.departureDate = ''
  }
  
  // Resetear búsqueda
  searchPerformed.value = false
  error.value = ''
}

const calculateDuration = (flight: any) => {
  // Si el vuelo tiene escalas, calcular duración total incluyendo conexiones
  if (flight.hasStops && flight.flightLegs && flight.flightLegs.length > 0) {
    let totalMinutes = 0
    
    // Calcular duración del primer segmento (origen a primera escala)
    if (flight.departureTime && flight.flightLegs[0]?.arrivalTime) {
      const departure = new Date(`2000-01-01T${flight.departureTime}`)
      const arrival = new Date(`2000-01-01T${flight.flightLegs[0].arrivalTime}`)
      totalMinutes += (arrival.getTime() - departure.getTime()) / (1000 * 60)
    }
    
    // Agregar tiempo de conexión
    flight.flightLegs.forEach((leg: any) => {
      if (leg.connectionTimeMinutes) {
        totalMinutes += leg.connectionTimeMinutes
      }
    })
    
    // Calcular duración del último segmento (última escala a destino)
    if (flight.flightLegs.length > 0) {
      const lastLeg = flight.flightLegs[flight.flightLegs.length - 1]
      if (lastLeg.departureTime && flight.arrivalTime) {
        const departure = new Date(`2000-01-01T${lastLeg.departureTime}`)
        const arrival = new Date(`2000-01-01T${flight.arrivalTime}`)
        totalMinutes += (arrival.getTime() - departure.getTime()) / (1000 * 60)
      }
    }
    
    const hours = Math.floor(totalMinutes / 60)
    const minutes = Math.round(totalMinutes % 60)
    return `${hours}h ${minutes}m`
  }
  
  // Para vuelos directos, simulación (en un sistema real esto vendría del backend)
  return '2h 30m'
}

const calculateDurationMinutes = (flight: any) => {
  // Si el vuelo tiene escalas, calcular duración total incluyendo conexiones
  if (flight.hasStops && flight.flightLegs && flight.flightLegs.length > 0) {
    let totalMinutes = 0
    
    // Calcular duración del primer segmento (origen a primera escala)
    if (flight.departureTime && flight.flightLegs[0]?.arrivalTime) {
      const departure = new Date(`2000-01-01T${flight.departureTime}`)
      const arrival = new Date(`2000-01-01T${flight.flightLegs[0].arrivalTime}`)
      totalMinutes += (arrival.getTime() - departure.getTime()) / (1000 * 60)
    }
    
    // Agregar tiempo de conexión
    flight.flightLegs.forEach((leg: any) => {
      if (leg.connectionTimeMinutes) {
        totalMinutes += leg.connectionTimeMinutes
      }
    })
    
    // Calcular duración del último segmento (última escala a destino)
    if (flight.flightLegs.length > 0) {
      const lastLeg = flight.flightLegs[flight.flightLegs.length - 1]
      if (lastLeg.departureTime && flight.arrivalTime) {
        const departure = new Date(`2000-01-01T${lastLeg.departureTime}`)
        const arrival = new Date(`2000-01-01T${flight.arrivalTime}`)
        totalMinutes += (arrival.getTime() - departure.getTime()) / (1000 * 60)
      }
    }
    
    return totalMinutes
  }
  
  // Para vuelos directos, simulación (en un sistema real esto vendría del backend)
  return 150 // 2h 30m en minutos
}

const formatPrice = (price: number) => {
  return price.toFixed(2)
}

const toggleDebug = () => {
  showDebug.value = !showDebug.value
}

// Lifecycle
onMounted(() => {
  loadInitialData()
})
</script>

<style scoped>
.flight-search-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* Header */
.flight-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  padding: 1rem 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.logo {
  font-size: 2rem;
}

.logo-section h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-link {
  text-decoration: none;
  color: #6b7280;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-link:hover,
.nav-link.active {
  color: #3b82f6;
}

/* Search Section */
.search-section {
  padding: 2rem;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.search-container {
  max-width: 1200px;
  margin: 0 auto;
}

.search-form {
  background: white;
  border-radius: 1.5rem;
  padding: 2rem;
  box-shadow: 0 20px 40px rgba(0,0,0,0.08);
  border: 1px solid #e2e8f0;
}

.form-row-horizontal {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.form-group label {
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Estilos para los nuevos calendarios */
.form-group .date-picker-container {
  flex: 1;
}

/* Estilos para el grupo de fechas */
.date-range-group {
  flex: 2;
}

.date-range-group .date-range-picker-container {
  width: 100%;
}

/* Estilos para la sección de disponibilidad */
.availability-section {
  margin-top: 40px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

/* Estilos para la sección de tipo de vuelo */
.flight-type-section {
  margin-bottom: 32px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.input-with-icon {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
  font-size: 1rem;
  pointer-events: none;
  z-index: 10;
}

.form-select,
.form-input {
  width: 100%;
  padding: 0.875rem 2.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 0.875rem;
  transition: all 0.2s;
  background: white;
  color: #1f2937;
  font-weight: 500;
}

.form-select:focus,
.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-select:hover,
.form-input:hover {
  border-color: #d1d5db;
}

.form-divider {
  width: 1px;
  height: 40px;
  background: linear-gradient(to bottom, transparent, #e5e7eb, transparent);
  margin: 0 0.5rem;
  align-self: center;
}

.search-actions {
  display: flex;
  align-items: center;
  margin-left: 1rem;
}

.search-btn {
  background: #1f2937;
  color: white;
  border: none;
  padding: 0.875rem 2rem;
  border-radius: 2rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  min-width: 120px;
}

.search-btn:hover:not(:disabled) {
  background: #111827;
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(31, 41, 55, 0.2);
}

.search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.secondary-actions {
  display: flex;
  justify-content: center;
  padding-top: 1rem;
  border-top: 1px solid #f3f4f6;
}

.show-all-btn {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: white;
  border: none;
  padding: 0.875rem 2rem;
  border-radius: 2rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.show-all-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(139, 92, 246, 0.3);
}

.show-all-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.dropdown-arrow {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 0.75rem;
  pointer-events: none;
}

/* Info Message */
.info-message {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  padding: 1.5rem;
  border-radius: 1rem;
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 10px 25px rgba(59, 130, 246, 0.3);
}

.info-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.info-content h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.info-content p {
  margin: 0;
  opacity: 0.9;
  line-height: 1.5;
}

/* Results Section */
.results-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.results-header {
  text-align: center;
  margin-bottom: 2rem;
}

.results-header h2 {
  color: white;
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.results-info {
  color: white;
  margin-bottom: 1rem;
}

.date-info,
.passenger-info {
  background: rgba(255,255,255,0.2);
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  margin: 0 0.5rem;
  font-size: 0.875rem;
}

.progress-bar {
  background: rgba(255,255,255,0.2);
  border-radius: 1rem;
  padding: 0.5rem;
  margin-top: 1rem;
}

.progress-step {
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
}

.progress-fill {
  height: 4px;
  background: #10b981;
  border-radius: 2px;
  margin-top: 0.5rem;
  width: 33%;
}

/* Filters */
.filters-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  background: white;
  padding: 1rem 1.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.filter-buttons {
  display: flex;
  gap: 0.5rem;
}

.filter-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.875rem;
}

.filter-btn:hover,
.filter-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.sort-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sort-section label {
  font-weight: 500;
  color: #374151;
}

.sort-select {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
  background: white;
}

/* Flight Cards */
.flights-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.flight-card {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: transform 0.2s;
  border: 2px solid transparent;
}

.flight-card:hover {
  transform: translateY(-2px);
}

.flight-card.best-price {
  border-color: #10b981;
  position: relative;
}

.best-price-tag {
  position: absolute;
  top: -10px;
  left: 1rem;
  background: #10b981;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 600;
}

.flight-info {
  flex: 1;
}

.flight-times {
  display: flex;
  align-items: center;
  gap: 2rem;
  margin-bottom: 1rem;
}

.departure,
.arrival {
  text-align: center;
}

.time {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
}

.city {
  font-weight: 600;
  color: #374151;
  margin-top: 0.25rem;
}

.date {
  font-size: 0.875rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.flight-duration {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.duration-line {
  position: relative;
  width: 100px;
}

.line {
  height: 2px;
  background: #d1d5db;
  width: 100%;
}

.plane-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  padding: 0.25rem;
  border-radius: 50%;
  font-size: 0.875rem;
}

.duration-text {
  font-weight: 600;
  color: #374151;
}

.stops {
  font-size: 0.75rem;
  color: #6b7280;
  background: #f3f4f6;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

.flight-details {
  margin-top: 1rem;
}

.airline {
  font-weight: 600;
  color: #374151;
}

.flight-number,
.seats {
  font-size: 0.875rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.flight-pricing {
  text-align: center;
  min-width: 150px;
}

.price-section {
  margin-bottom: 1rem;
}

.price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #10b981;
}

.price-detail {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 1rem;
}

.details-btn {
  background: #4f46e5;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  width: 100%;
}

.details-btn:hover {
  background: #3730a3;
}

.book-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  width: 100%;
}

.book-btn:hover {
  background: #2563eb;
}

/* Estados */
.loading-state,
.no-flights {
  text-align: center;
  padding: 3rem;
  color: white;
}

.spinner {
  border: 4px solid rgba(255,255,255,0.3);
  border-top: 4px solid white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-flights-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.no-flights h3 {
  margin-bottom: 0.5rem;
}

/* Estado inicial */
.initial-state {
  text-align: center;
  padding: 4rem 2rem;
  color: white;
}

.welcome-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.welcome-section h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.welcome-section p {
  font-size: 1.125rem;
  opacity: 0.9;
}

/* Debug section */
.debug-section {
  background: rgba(0,0,0,0.8);
  color: white;
  padding: 1rem;
  margin: 2rem;
  border-radius: 0.5rem;
  font-family: monospace;
}

.debug-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  cursor: pointer;
  margin-top: 0.5rem;
}

/* Responsive */
@media (max-width: 1024px) {
  .form-row-horizontal {
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .form-group {
    flex: 1 1 calc(50% - 0.5rem);
    min-width: 200px;
  }
  
  .form-divider {
    display: none;
  }
  
  .search-actions {
    margin-left: 0;
    margin-top: 1rem;
    width: 100%;
    justify-content: center;
  }
  
  .search-btn {
    width: 100%;
    max-width: 200px;
  }
}

@media (max-width: 768px) {
  .search-section {
    padding: 1rem;
  }
  
  .search-form {
    padding: 1.5rem;
  }
  
  .form-row-horizontal {
    flex-direction: column;
    gap: 1rem;
  }
  
  .form-group {
    flex: 1 1 100%;
    min-width: 100%;
  }
  
  .input-with-icon {
    position: relative;
  }
  
  .input-icon {
    position: absolute;
    left: 0.75rem;
    top: 50%;
    transform: translateY(-50%);
  }
  
  .form-divider {
    display: none;
  }
  
  .search-actions {
    margin-left: 0;
    margin-top: 1rem;
    width: 100%;
  }
  
  .search-btn {
    width: 100%;
    max-width: none;
  }
  
  .secondary-actions {
    width: 100%;
  }
  
  .show-all-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .search-form {
    padding: 1rem;
  }
  
  .form-group label {
    font-size: 0.7rem;
  }
  
  .form-select,
  .form-input {
    padding: 0.75rem 2.25rem;
    font-size: 0.8rem;
  }
  
  .search-btn,
  .show-all-btn {
    padding: 0.75rem 1.5rem;
    font-size: 0.875rem;
  }
}

  .flight-details .seats {
    color: #6b7280;
    font-size: 0.875rem;
  }
  
  /* Flight Stops Section */
  .flight-stops {
    margin-top: 1rem;
    padding: 1rem;
    background: #f8fafc;
    border-radius: 0.5rem;
    border-left: 4px solid #3b82f6;
  }
  
  .stops-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.75rem;
  }
  
  .stops-icon {
    font-size: 1.25rem;
  }
  
  .stops-title {
    font-weight: 600;
    color: #374151;
    font-size: 0.875rem;
  }
  
  .stops-list {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .stop-item {
    padding: 0.75rem;
    background: white;
    border-radius: 0.375rem;
    border: 1px solid #e5e7eb;
  }
  
  .stop-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .stop-city {
    font-weight: 600;
    color: #1f2937;
    font-size: 0.875rem;
  }
  
  .stop-times {
    display: flex;
    gap: 1rem;
    font-size: 0.75rem;
    color: #6b7280;
  }
  
  .connection-time {
    font-size: 0.75rem;
    color: #059669;
    font-weight: 500;
  }
</style>
