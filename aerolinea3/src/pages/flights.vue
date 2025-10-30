<template>
  <div class="flight-search-container">
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
                <select v-model.number="searchParams.origin" class="form-select" style="color: #1f2937 !important;">
                  <option value="" style="color: #9ca3af;">Selecciona origen</option>
                  <option v-for="city in cities" :key="city.idCity" :value="city.idCity" style="color: #1f2937;">
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
                <select v-model.number="searchParams.destination" class="form-select" style="color: #1f2937 !important;">
                  <option value="" style="color: #9ca3af;">Selecciona destino</option>
                  <option v-for="city in cities" :key="city.idCity" :value="city.idCity" style="color: #1f2937;">
                    {{ city.name }}, {{ city.country }}
                  </option>
                </select>
              </div>
            </div>
            
            <div class="form-divider"></div>
            
            <div class="form-group date-range-group">
              <label>Fechas de viaje</label>
              <div class="form-row-horizontal" style="gap:.75rem;margin-bottom:0;align-items:stretch;">
                <div class="input-with-icon" style="flex:1;">
                  <span class="input-icon">📅</span>
                  <input 
                    class="form-input"
                    type="date"
                    v-model="searchParams.departureDate"
                    style="color: #1f2937 !important; color-scheme: light;"
                  />
                </div>
                <div v-if="searchParams.flightType==='round-trip'" class="input-with-icon" style="flex:1;">
                  <span class="input-icon">🏠</span>
                  <input 
                    class="form-input"
                    type="date"
                    v-model="searchParams.returnDate"
                    style="color: #1f2937 !important; color-scheme: light;"
                  />
                </div>
              </div>
            </div>
            
            <div class="form-divider"></div>
            
            <div class="form-group">
              <label>Pasajeros</label>
              <div class="input-with-icon">
                <span class="input-icon">👤</span>
                <select v-model.number="searchParams.passengers" class="form-select" style="color: #1f2937 !important;">
                  <option value="1" style="color: #1f2937;">1 Pasajero</option>
                  <option value="2" style="color: #1f2937;">2 Pasajeros</option>
                  <option value="3" style="color: #1f2937;">3 Pasajeros</option>
                  <option value="4" style="color: #1f2937;">4 Pasajeros</option>
                </select>
                <span class="dropdown-arrow">▼</span>
              </div>
            </div>

            <div class="form-divider"></div>

            <!-- Filtros: rango de precios -->
            <div class="form-group compact">
              <label>Precio mínimo</label>
              <div class="input-with-icon">
                <span class="input-icon">💵</span>
                <input class="form-input" type="number" min="0" step="1" v-model.number="filtersState.minPrice" placeholder="0" style="color: #1f2937 !important;" />
              </div>
            </div>
            <div class="form-group compact">
              <label>Precio máximo</label>
              <div class="input-with-icon">
                <span class="input-icon">💵</span>
                <input class="form-input" type="number" min="0" step="1" v-model.number="filtersState.maxPrice" placeholder="1025" style="color: #1f2937 !important;" />
              </div>
            </div>

            <div class="form-divider"></div>

            <!-- Filtros: rating y asiento -->
            <div class="form-group compact">
              <label>Rating mínimo</label>
              <div class="input-with-icon">
                <span class="input-icon">⭐</span>
                <select class="form-select" v-model.number="filtersState.minRating" style="color: #1f2937 !important;">
                  <option :value="0" style="color: #1f2937;">Cualquiera</option>
                  <option :value="1" style="color: #1f2937;">⭐ 1+</option>
                  <option :value="2" style="color: #1f2937;">⭐⭐ 2+</option>
                  <option :value="3" style="color: #1f2937;">⭐⭐⭐ 3+</option>
                  <option :value="4" style="color: #1f2937;">⭐⭐⭐⭐ 4+</option>
                  <option :value="5" style="color: #1f2937;">⭐⭐⭐⭐⭐ 5</option>
                </select>
                <span class="dropdown-arrow">▼</span>
              </div>
            </div>
            <div class="form-group compact">
              <label>Tipo de asiento</label>
              <div class="input-with-icon">
                <span class="input-icon">🪑</span>
                <select class="form-select" v-model="filtersState.seatCategory" style="color: #1f2937 !important;">
                  <option value="" style="color: #9ca3af;">Cualquiera</option>
                  <option value="ECONOMY" style="color: #1f2937;">💺 Economy</option>
                  <option value="BUSINESS" style="color: #1f2937;">✈️ Business</option>
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
            <button @click="resetFiltersAndShowAll" class="show-all-btn show-all-elevated" :disabled="loading">
              {{ loading ? 'Cargando...' : 'Borrar filtros' }}
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

      <!-- Barra de filtros inferior removida: usamos solo la cabecera superior -->

      <!-- Lista de vuelos -->
      <div class="flights-list">
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>Buscando vuelos disponibles...</p>
        </div>
        
        <div v-else-if="filteredFlights.length === 0 && oneStopFlights.length === 0" class="no-flights">
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

          <!-- ✈️ VUELOS CON ESCALA (1 parada) -->
          <div v-if="oneStopFlights.length > 0" style="margin-top: 32px;">
            <h3 style="color: #1f2937; font-size: 1.5rem; margin-bottom: 16px; padding-left: 8px; border-left: 4px solid #f59e0b;">
              ✈️ Vuelos con 1 Escala
            </h3>
            <div 
              v-for="(route, index) in oneStopFlights" 
              :key="'onestop-' + index"
              class="flight-card"
              style="border: 2px solid #fbbf24; background: linear-gradient(to right, #fffbeb, #ffffff);"
            >
              <div class="flight-info">
                <div style="margin-bottom: 16px; padding: 8px; background: #fef3c7; border-radius: 8px;">
                  <strong style="color: #92400e;">🔄 Ruta con escala en {{ route.viaCityName }}</strong>
                </div>
                
                <!-- Segmento 1 -->
                <div style="margin-bottom: 12px; padding: 12px; background: #f9fafb; border-radius: 8px;">
                  <div style="font-weight: 600; color: #374151; margin-bottom: 8px;">
                    Segmento 1: {{ route.firstSegment.originCity }} → {{ route.firstSegment.destinationCity }}
                  </div>
                  <div style="display: flex; justify-content: space-between; font-size: 0.9rem; color: #6b7280;">
                    <span>Vuelo {{ route.firstSegment.flightNumber }}</span>
                    <span>{{ route.firstSegment.departureDate }} {{ route.firstSegment.departureTime }}</span>
                    <span>${{ route.firstSegment.basePrice }}</span>
                  </div>
                </div>
                
                <!-- Segmento 2 -->
                <div style="padding: 12px; background: #f9fafb; border-radius: 8px;">
                  <div style="font-weight: 600; color: #374151; margin-bottom: 8px;">
                    Segmento 2: {{ route.secondSegment.originCity }} → {{ route.secondSegment.destinationCity }}
                  </div>
                  <div style="display: flex; justify-content: space-between; font-size: 0.9rem; color: #6b7280;">
                    <span>Vuelo {{ route.secondSegment.flightNumber }}</span>
                    <span>{{ route.secondSegment.departureDate }} {{ route.secondSegment.departureTime }}</span>
                    <span>${{ route.secondSegment.basePrice }}</span>
                  </div>
                </div>
              </div>
              
              <div class="flight-pricing">
                <div class="price-section">
                  <div class="price">USD {{ route.totalPrice }}</div>
                  <div class="price-detail">Total (2 vuelos)</div>
                </div>
                <div class="action-buttons">
                  <button 
                    @click="bookStopoverFlight(route)" 
                    class="book-btn"
                    style="background: #f59e0b;"
                  >
                    Comprar con Escala
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 🔄 VUELOS DE VUELTA (Round-Trip) -->
          <div v-if="returnFlights.length > 0 && searchParams.flightType === 'round-trip'" style="margin-top: 32px;">
            <h3 style="color: #1f2937; font-size: 1.5rem; margin-bottom: 16px; padding-left: 8px; border-left: 4px solid #10b981;">
              🔄 Vuelos de Vuelta (Round-Trip)
            </h3>
            <div 
              v-for="returnFlight in returnFlights" 
              :key="'return-' + returnFlight.idFlight"
              class="flight-card"
              style="border: 2px solid #10b981; background: linear-gradient(to right, #d1fae5, #ffffff);"
            >
              <div class="flight-info">
                <div style="margin-bottom: 8px; padding: 6px 12px; background: #d1fae5; border-radius: 999px; display: inline-block;">
                  <strong style="color: #065f46;">🔙 Vuelo de Vuelta</strong>
                </div>
                <div class="flight-times">
                  <div class="departure">
                    <div class="time">{{ formatTime(returnFlight.departureTime) || '--:--' }}</div>
                    <div class="city">{{ returnFlight.originCity }}</div>
                    <div class="date">{{ formatDate(returnFlight.departureDate) }}</div>
                  </div>
                  
                  <div class="flight-duration">
                    <div class="duration-line">
                      <div class="line"></div>
                      <div class="plane-icon">🔙</div>
                    </div>
                    <div class="duration-text">Vuelta</div>
                    <div class="stops"><span>Directo</span></div>
                  </div>
                  
                  <div class="arrival">
                    <div class="time">{{ formatTime(returnFlight.arrivalTime) || '--:--' }}</div>
                    <div class="city">{{ returnFlight.destinationCity }}</div>
                    <div class="date">{{ formatDate(returnFlight.arrivalDate) }}</div>
                  </div>
                </div>
                
                <div class="flight-details">
                  <div class="flight-number">Vuelo {{ returnFlight.flightNumber }}</div>
                </div>
              </div>
              
              <div class="flight-pricing">
                <div class="price-section">
                  <div class="price" style="color: #10b981;">USD {{ formatPrice(returnFlight.basePrice) }}</div>
                  <div class="price-detail">vuelo de vuelta</div>
                </div>
                <div class="action-buttons">
                  <button @click="viewFlightDetails(returnFlight)" class="details-btn" style="border-color: #10b981; color: #10b981;">
                    📋 Ver Detalles
                  </button>
                  <button @click="bookFlight(returnFlight)" class="book-btn" style="background: #10b981;">
                    Seleccionar Vuelta
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Estado inicial: solo cabecera, se removió el calendario inferior de disponibilidad -->
    <div v-if="!searchPerformed" class="initial-state">
      <div class="welcome-section">
        <div class="welcome-icon">✈️</div>
        <h2>Encuentra tu vuelo ideal</h2>
        <p>Busca entre miles de vuelos disponibles a destinos increíbles</p>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { airlineApi } from '../utils/airlineApi'
import CartCounter from '../components/CartCounter.vue'
import DatePicker from '../components/DatePicker.vue'
import DateRangePicker from '../components/DateRangePicker.vue'
import FlightAvailabilityCalendar from '../components/FlightAvailabilityCalendar.vue'
import FlightTypeSelector from '../components/FlightTypeSelector.vue'
import eventBus from '../eventBus'

const router = useRouter()

// Estados reactivos
const loading = ref(false)
const error = ref('')
const flights = ref<any[]>([])
const oneStopFlights = ref<any[]>([])  // Vuelos con 1 escala
const returnFlights = ref<any[]>([])    // Vuelos de vuelta (round-trip)
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
  flightType: 'round-trip' // 'one-way', 'round-trip'
})

// Estado de filtros de UI (no auto-ejecuta búsqueda)
const filtersState = ref({
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  minRating: 0 as number,
  seatCategory: '' as string
})

// Filtros y ordenamiento
const activeFilter = ref('all')
const sortBy = ref('price')
const destinationQuery = ref('')

const filters = [
  { label: 'Todos', value: 'all' },
  { label: 'Directos', value: 'direct' },
  { label: 'Con escalas', value: 'stops' },
  { label: 'Económico', value: 'economy' }
]

// Computed properties
const findCityIdByName = (name: string | number | undefined) => {
  if (!name) return undefined
  const nm = String(name).toLowerCase().trim()
  const city = cities.value.find((c: any) => String(c.name).toLowerCase() === nm)
  return city?.idCity
}

// Helpers para lógica de itinerarios
const normalizeCityId = (flight: any, key: 'origin' | 'destination') => {
  if (key === 'origin') {
    return flight.originCityId ?? findCityIdByName(flight.originCity)
  }
  return flight.destinationCityId ?? findCityIdByName(flight.destinationCity)
}

const timeToMinutes = (hhmm?: string) => {
  if (!hhmm) return undefined
  const [h, m] = hhmm.split(':').map((x: string) => parseInt(x, 10))
  if (Number.isNaN(h) || Number.isNaN(m)) return undefined
  return h * 60 + m
}

const diffMinutesClock = (from?: string, to?: string) => {
  const a = timeToMinutes(from)
  const b = timeToMinutes(to)
  if (a === undefined || b === undefined) return undefined
  let d = b - a
  if (d < 0) d += 24 * 60 // siguiente día
  return d
}

type Itinerary = any // mantenemos forma compatible con UI existente

// Construye vuelos directos y con UNA escala que conecte tiempos válidos
const buildItinerariesForDirection = (
  allFlights: any[],
  originId: number,
  destId: number,
  date?: string
): Itinerary[] => {
  const MIN_CONNECTION_MIN = 30
  const MAX_CONNECTION_MIN = 5 * 60

  // Filtrar por fecha si viene definida
  const sameDate = (f: any) => (!date || f.departureDate === date)

  // Directos
  const direct = allFlights.filter(f =>
    normalizeCityId(f, 'origin') === originId &&
    normalizeCityId(f, 'destination') === destId &&
    sameDate(f)
  )

  // Candidatos por ciudad intermedia
  const fromOrigin = allFlights.filter(f => normalizeCityId(f, 'origin') === originId && sameDate(f))
  const toDest = allFlights.filter(f => normalizeCityId(f, 'destination') === destId)

  const oneStops: Itinerary[] = []
  for (const f1 of fromOrigin) {
    const midId = normalizeCityId(f1, 'destination')
    const arrTime = f1.arrivalTime
    for (const f2 of toDest) {
      if (normalizeCityId(f2, 'origin') !== midId) continue
      // La segunda pierna puede ser el mismo día o al siguiente
      // Si se provee fecha, permitimos que f2.departureDate sea igual a date o a la misma que f1.arrivalDate (si existiera)
      if (date && f2.departureDate !== date && f2.departureDate !== f1.departureDate) continue

      const conn = diffMinutesClock(arrTime, f2.departureTime)
      if (conn === undefined) continue
      if (conn < MIN_CONNECTION_MIN || conn > MAX_CONNECTION_MIN) continue

      oneStops.push({
        idFlight: `${f1.idFlight}-${f2.idFlight}`,
        originCityId: originId,
        destinationCityId: destId,
        originCity: f1.originCity,
        destinationCity: f2.destinationCity,
        departureDate: f1.departureDate,
        arrivalDate: f2.arrivalDate || f2.departureDate,
        departureTime: f1.departureTime,
        arrivalTime: f2.arrivalTime,
        basePrice: (Number(f1.basePrice) || 0) + (Number(f2.basePrice) || 0),
        availableSeats: Math.min(Number(f1.availableSeats) || 0, Number(f2.availableSeats) || 0),
        hasStops: true,
        flightLegs: [
          {
            cityId: midId,
            arrivalTime: f1.arrivalTime,
            departureTime: f2.departureTime,
            connectionTimeMinutes: conn
          }
        ],
        _legs: [f1, f2]
      })
    }
  }

  return [...direct, ...oneStops]
}

const filteredFlights = computed(() => {
  if (!searchPerformed.value) return []

  const all = flights.value.filter(f => f.status !== 'DRAFT' && f.status !== 'CANCELLED')
  const originId = Number(searchParams.value.origin) || 0
  const destId = Number(searchParams.value.destination) || 0
  const depDate = searchParams.value.departureDate
  const retDate = searchParams.value.returnDate
  const type = searchParams.value.flightType

  let result: any[] = []

  if (type === 'one-way' || (!depDate && !retDate)) {
    result = buildItinerariesForDirection(all, originId, destId, depDate)
  } else {
    const outbound = buildItinerariesForDirection(all, originId, destId, depDate)
      .map(f => ({ ...f, __trip: 'OUTBOUND' }))

    const inbound = (retDate && originId && destId)
      ? buildItinerariesForDirection(all, destId, originId, retDate)
          .map(f => ({ ...f, __trip: 'INBOUND' }))
      : []

    result = [...outbound, ...inbound]
  }

  // Filtros rápidos existentes
  if (activeFilter.value === 'direct') {
    result = result.filter(f => !f.hasStops)
  } else if (activeFilter.value === 'stops') {
    result = result.filter(f => f.hasStops)
  } else if (activeFilter.value === 'economy') {
    const avgPrice = result.length ? result.reduce((s, f) => s + f.basePrice, 0) / result.length : 0
    result = result.filter(f => f.basePrice <= avgPrice)
  }

  // Filtros nuevos: rango de precios, rating y tipo de asiento (aplicados en cliente por seguridad)
  const minPrice = filtersState.value.minPrice
  const maxPrice = filtersState.value.maxPrice
  const minRating = filtersState.value.minRating || 0
  const seatCategory = (filtersState.value.seatCategory || '').toUpperCase()

  const priceForFlight = (f: any): number => {
    if (seatCategory && f?.fares && f.fares[seatCategory] != null) {
      return Number(f.fares[seatCategory])
    }
    return Number(f.basePrice || 0)
  }

  result = result.filter((f) => {
    const price = priceForFlight(f)
    if (minPrice != null && price < minPrice) return false
    if (maxPrice != null && price > maxPrice) return false

    const rating = Number(
      f?.averageRating ?? f?.avgRating ?? f?.rating ?? 0
    )
    if (minRating && rating && rating < minRating) return false

    if (seatCategory) {
      // Si se especifica categoría, exigir que exista tarifa para esa categoría
      const hasFare = f?.fares && f.fares[seatCategory] != null
      if (!hasFare && seatCategory !== 'ECONOMY') return false
    }
    return true
  })

  return result
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

    // Cargar criterios preseleccionados desde la home (si existen)
    const pre = localStorage.getItem('preSearch')
    if (pre) {
      try {
        const parsed = JSON.parse(pre)
        searchParams.value.origin = parsed.origin ? Number(parsed.origin) : ''
        searchParams.value.destination = parsed.destination ? Number(parsed.destination) : ''
        searchParams.value.departureDate = parsed.departureDate || ''
        searchParams.value.returnDate = parsed.returnDate || ''
        searchParams.value.passengers = parsed.passengers ? Number(parsed.passengers) : 1
        searchParams.value.flightType = parsed.flightType === 'oneway' ? 'one-way' : (parsed.flightType || 'round-trip')
        searchPerformed.value = Boolean(searchParams.value.origin || searchParams.value.destination || searchParams.value.departureDate)
      } catch {}
    }
  } catch (e: any) {
    console.error('❌ Error cargando datos:', e)
    error.value = 'Error cargando datos: ' + (e?.message || 'Error desconocido')
  } finally {
    loading.value = false
  }
}

const searchFlights = async () => {
  // Validar que al menos haya un criterio de búsqueda (origen/destino o fecha)
  const hasSearchCriteria = searchParams.value.origin || searchParams.value.destination || searchParams.value.departureDate
  
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
  
  // Ejecutar consulta al backend con parámetros y filtros
  try {
    loading.value = true
    error.value = ''
    const response = await airlineApi.getFlights({
      origin: searchParams.value.origin,
      destination: searchParams.value.destination,
      departureDate: searchParams.value.departureDate,
      returnDate: searchParams.value.returnDate,
      passengers: searchParams.value.passengers,
      minPrice: filtersState.value.minPrice,
      maxPrice: filtersState.value.maxPrice,
      minRating: filtersState.value.minRating || undefined,
      seatCategory: filtersState.value.seatCategory || undefined
    })
    if (response && response.success) {
      flights.value = response.flights || []
      oneStopFlights.value = response.oneStopFlights || []
      returnFlights.value = response.returnFlights || []
      
      console.log('📊 Resultados:', {
        directos: flights.value.length,
        conEscala: oneStopFlights.value.length,
        vuelta: returnFlights.value.length
      })
    }
    searchPerformed.value = true
    console.log('🔍 Búsqueda realizada con filtros')
  } catch (e: any) {
    error.value = 'No se pudo realizar la búsqueda'
  } finally {
    loading.value = false
  }
}

const resetFiltersAndShowAll = async () => {
  searchParams.value = {
    origin: '',
    destination: '',
    departureDate: '',
    returnDate: '',
    passengers: 1,
    flightType: 'round-trip'
  }
  filtersState.value = { minPrice: undefined, maxPrice: undefined, minRating: 0, seatCategory: '' }
  try {
    loading.value = true
    const resp = await airlineApi.getFlights()
    if (resp && resp.success) {
      flights.value = resp.flights || []
      oneStopFlights.value = resp.oneStopFlights || []
      returnFlights.value = resp.returnFlights || []
    }
    searchPerformed.value = true
    error.value = ''
  } finally {
    loading.value = false
  }
}

const setActiveFilter = (filter: string) => {
  activeFilter.value = filter
}

// Navegación conservando itinerarios (directo o con escala)
const bookFlight = (flight: any) => {
  console.log('🎫 Reservando vuelo:', flight)
  try {
    sessionStorage.setItem('selectedItinerary', JSON.stringify(flight))
  } catch {}
  router.push(`/book-flight/${flight.idFlight}`)
}

/**
 * Comprar vuelo con escala (2 segmentos)
 */
const bookStopoverFlight = (route: any) => {
  console.log('✈️ Comprando vuelo con escala:', route)
  try {
    // Guardar información de la ruta con escala
    sessionStorage.setItem('stopoverRoute', JSON.stringify(route))
    sessionStorage.setItem('bookingType', 'STOPOVER')
  } catch {}
  // Ir a página de checkout con información de escala
  router.push(`/flight-details/${route.firstSegment.idFlight}?stopover=true&segment2=${route.secondSegment.idFlight}`)
}

const viewFlightDetails = (flight: any) => {
  console.log('📋 Viendo detalles del vuelo:', flight)
  try {
    sessionStorage.setItem('selectedItinerary', JSON.stringify(flight))
  } catch {}
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
  eventBus.on('inventory:updated', (payload: any) => {
    try {
      const { flightId, availableSeats } = payload || {}
      const f = flights.value.find((x: any) => x.idFlight === flightId)
      if (f && availableSeats !== undefined) f.availableSeats = availableSeats
    } catch {}
  })
})

// Disparar búsqueda cuando cambian fechas u origen/destino desde el calendario o selects
// Ya no auto-ejecutamos búsquedas al cambiar selects/fechas. Solo habilitamos el estado para UI.
watch(() => [searchParams.value.departureDate, searchParams.value.returnDate, searchParams.value.origin, searchParams.value.destination], () => {
  searchPerformed.value = false
  error.value = ''
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
  background: #ffffff;
  border-radius: 1.25rem;
  padding: 1.5rem 1.75rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.06);
  border: 1px solid #e5e7eb;
}

.search-form .compact .form-input,
.search-form .compact .form-select {
  height: 48px;
  padding-left: 2.25rem;
}

.search-form .compact label {
  font-size: 0.7rem;
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
  height: 56px;
  padding: 0 2.25rem 0 2.5rem;
  border: 2px solid #d1d5db !important;
  border-radius: 0.75rem;
  font-size: 1.1rem !important;
  transition: all 0.2s;
  background: #ffffff !important;
  color: #000000 !important;
  font-weight: 700 !important;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08) inset;
  -webkit-text-fill-color: #000000 !important;
}

/* Mejorar visibilidad de placeholders */
.form-input::placeholder {
  color: #9ca3af !important;
  font-weight: 500 !important;
  -webkit-text-fill-color: #9ca3af !important;
}

/* Estilos para opciones de select */
.form-select option {
  color: #000000 !important;
  background: #ffffff !important;
  padding: 0.75rem !important;
  font-size: 1rem !important;
  font-weight: 600 !important;
}

.form-select option:first-child {
  color: #9ca3af !important;
  font-weight: 500 !important;
}

.form-select option:checked {
  background: #eff6ff !important;
  color: #000000 !important;
  font-weight: 700 !important;
}

/* Asegurar visibilidad en inputs de fecha */
input[type="date"].form-input {
  color: #000000 !important;
  font-weight: 700 !important;
  -webkit-text-fill-color: #000000 !important;
}

input[type="date"].form-input::-webkit-datetime-edit-text,
input[type="date"].form-input::-webkit-datetime-edit-month-field,
input[type="date"].form-input::-webkit-datetime-edit-day-field,
input[type="date"].form-input::-webkit-datetime-edit-year-field {
  color: #000000 !important;
  -webkit-text-fill-color: #000000 !important;
}

/* Inputs de número */
input[type="number"].form-input {
  color: #000000 !important;
  font-weight: 700 !important;
  -webkit-text-fill-color: #000000 !important;
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
  background: #111827;
  color: #ffffff;
  border: none;
  padding: 0.9rem 1.75rem;
  border-radius: 9999px;
  font-size: 0.95rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  min-width: 120px;
  box-shadow: 0 6px 16px rgba(17,24,39,0.18);
}

.search-btn:hover:not(:disabled) {
  background: #0b1220;
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(17,24,39,0.22);
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

.show-all-elevated {
  box-shadow: 0 10px 24px rgba(124, 58, 237, 0.35);
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
  background: rgba(255,255,255,0.25);
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

/* animación sutil de carga */
.loading-state .spinner {
  border: 4px solid rgba(255,255,255,0.25);
  border-top: 4px solid #fff;
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

.filters-right { display: flex; align-items: center; gap: 0.75rem; }
.destination-search { position: relative; }
.search-icon {
  position: absolute; left: 10px; top: 50%; transform: translateY(-50%);
  color: #6b7280; font-size: 0.9rem; pointer-events: none;
}
.dest-input {
  padding: 0.5rem 0.75rem 0.5rem 2rem; border: 1px solid #d1d5db; border-radius: 0.5rem;
  font-size: 0.9rem; min-width: 200px;
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

.no-flights {
  background: rgba(255,255,255,0.06);
  border-radius: 1rem;
  backdrop-filter: blur(2px);
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
