<template>
  <div class="flight-details-container">
    <!-- Contenido principal -->
    <div class="main-content">
      <!-- Loading state -->
      <div v-if="loading" class="loading-section">
        <div class="loading-container">
          <div class="loading-plane">✈️</div>
          <div class="loading-text">
            <h2>Cargando detalles del vuelo...</h2>
            <p>Preparando toda la información para tu viaje</p>
          </div>
          <div class="loading-progress">
            <div class="progress-bar">
              <div class="progress-fill"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-section">
        <div class="error-icon">❌</div>
        <h2>Error al cargar el vuelo</h2>
        <p>{{ error }}</p>
        <button @click="goBack" class="back-btn">Volver a Vuelos</button>
      </div>

      <!-- Flight details -->
      <div v-else-if="flight" class="flight-details-section">
        <!-- Flight header -->
        <div class="flight-header-card">
          <div class="flight-number-section">
            <h1>Vuelo {{ flight.flightNumber }}</h1>
            <div class="flight-status" :class="getStatusClass(flight.status)">
              {{ getStatusText(flight.status) }}
            </div>
          </div>
          <div class="flight-route">
            <div class="route-item">
              <div class="city">{{ flight.originCity }}</div>
              <div class="airport-code">{{ getCityCode(flight.originCity) }}</div>
            </div>
            <div class="route-arrow">→</div>
            <div class="route-item">
              <div class="city">{{ flight.destinationCity }}</div>
              <div class="airport-code">{{ getCityCode(flight.destinationCity) }}</div>
            </div>
          </div>
        </div>

        <!-- Flight itinerary -->
        <div class="itinerary-section">
          <h2>
            Itinerario del Vuelo
            <span v-if="flight && flight._legs && flight._legs.length === 2" class="badge-one-stop">1 escala</span>
          </h2>
          
          <!-- Main flight info -->
          <div class="main-flight-card">
            <div class="flight-timeline">
              <div class="timeline-item departure">
                <div class="time">{{ formatTime(flight.departureTime) }}</div>
                <div class="date">{{ formatDate(flight.departureDate) }}</div>
                <div class="city">{{ flight.originCity }}</div>
                <div class="details">
                  <div class="gate">
                    Puerta: {{ flight.gate || 'Por confirmar' }}
                    <span v-if="!flight.gate" class="info-note">(24h antes)</span>
                  </div>
                  <div class="terminal">
                    Terminal: {{ flight.terminal || 'Por confirmar' }}
                    <span v-if="!flight.terminal" class="info-note">(24h antes)</span>
                  </div>
                </div>
              </div>
              
              <div class="flight-connection">
                <div class="connection-line">
                  <div class="line"></div>
                  <div class="plane-icon">✈️</div>
                  <div class="line"></div>
                </div>
                <div class="connection-info">
                  <div class="duration">{{ calculateDuration(flight) }}</div>
                  <div class="flight-type">
                    <span v-if="flight.hasStops && flight.flightLegs && flight.flightLegs.length > 0">
                      {{ flight.flightLegs.length }} escala{{ flight.flightLegs.length > 1 ? 's' : '' }}
                    </span>
                    <span v-else>Vuelo directo</span>
                  </div>
                </div>
              </div>
              
              <div class="timeline-item arrival">
                <div class="time">{{ formatTime(flight.arrivalTime) }}</div>
                <div class="date">{{ formatDate(flight.arrivalDate) }}</div>
                <div class="city">{{ flight.destinationCity }}</div>
                <div class="details">
                  <div class="gate">
                    Puerta: {{ flight.gate || 'Por confirmar' }}
                    <span v-if="!flight.gate" class="info-note">(24h antes)</span>
                  </div>
                  <div class="terminal">
                    Terminal: {{ flight.terminal || 'Por confirmar' }}
                    <span v-if="!flight.terminal" class="info-note">(24h antes)</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Stops information -->
          <div v-if="flight.hasStops && flight.flightLegs && flight.flightLegs.length > 0" class="stops-section">
            <h3>🛫 Escalas del Vuelo</h3>
            <div class="stops-list">
              <div 
                v-for="(leg, index) in flight.flightLegs" 
                :key="index"
                class="stop-card"
              >
                <div class="stop-header">
                  <div class="stop-number">Escala {{ index + 1 }}</div>
                  <div class="stop-city">
                    {{ getCityName(leg.cityId) }}
                    <span v-if="!leg.cityId" class="city-warning">⚠️ ID de ciudad no disponible</span>
                  </div>
                </div>
                <div class="stop-details">
                  <div class="stop-times">
                    <div class="time-item">
                      <span class="label">Llegada:</span>
                      <span class="time">{{ formatTime(leg.arrivalTime) || 'No disponible' }}</span>
                    </div>
                    <div class="time-item">
                      <span class="label">Salida:</span>
                      <span class="time">{{ formatTime(leg.departureTime) || 'No disponible' }}</span>
                    </div>
                  </div>
                  <div class="stop-info">
                    <div class="connection-time">
                      <span class="label">Tiempo de conexión:</span>
                      <span class="value">{{ leg.connectionTimeMinutes || 0 }} minutos</span>
                    </div>
                    <div class="aircraft-change">
                      <span class="label">Cambio de aeronave:</span>
                      <span class="value">{{ leg.aircraftChange === 'Y' ? 'Sí' : 'No' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Flight details -->
        <div class="details-section">
          <h2>Detalles del Vuelo</h2>
          <div class="details-grid">
            <div class="detail-item">
              <span class="label">Operado por:</span>
              <span class="value">AeroLinea</span>
            </div>
            <div class="detail-item">
              <span class="label">Número de vuelo:</span>
              <span class="value">{{ flight.flightNumber }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Estado:</span>
              <span class="value" :class="getStatusClass(flight.status)">
                {{ getStatusText(flight.status) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="label">Asientos disponibles:</span>
              <span class="value">{{ flight.availableSeats }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Precio base (Económico):</span>
              <span class="value price">USD {{ formatPrice(flight.basePrice) }}</span>
            </div>
          </div>
        </div>

        <!-- Seat categories and prices -->
        <div v-if="flight.fares && Object.keys(flight.fares).length > 0" class="fares-section">
          <h2>Precios por Categoría de Asiento</h2>
          <div class="fares-grid">
            <div 
              v-for="(price, categoryKey) in flight.fares" 
              :key="String(categoryKey)"
              class="fare-card"
              :class="{ 'economy': String(categoryKey) === 'ECONOMY', 'business': String(categoryKey) === 'BUSINESS', 'first-class': String(categoryKey) === 'FIRST_CLASS' }"
            >
              <div class="fare-header">
                <div class="category-name">{{ getCategoryName(String(categoryKey)) }}</div>
                <div class="category-icon">{{ getCategoryIcon(String(categoryKey)) }}</div>
              </div>
              <div class="fare-price">
                <span class="currency">USD</span>
                <span class="amount">{{ formatPrice(price) }}</span>
              </div>
              <div class="fare-features">
                <div class="feature">{{ getCategoryFeatures(String(categoryKey)) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Check-in information -->
        <div class="checkin-section">
          <h2>Información de Check-in</h2>
          <div class="checkin-grid">
            <div class="checkin-item">
              <span class="label">Inicio de check-in:</span>
              <span class="value">{{ formatTime(flight.checkInStart) || 'Por confirmar' }}</span>
            </div>
            <div class="checkin-item">
              <span class="label">Fin de check-in:</span>
              <span class="value">{{ formatTime(flight.checkInEnd) || 'Por confirmar' }}</span>
            </div>
            <div class="checkin-item">
              <span class="label">Hora de embarque:</span>
              <span class="value">{{ formatTime(flight.boardingTime) || 'Por confirmar' }}</span>
            </div>
          </div>
          <div class="checkin-note">
            <p>ℹ️ La información de check-in se confirma 24 horas antes del vuelo</p>
          </div>
        </div>

        <!-- Reviews -->
        <div class="details-section">
          <h2>Comentarios y Calificaciones</h2>
          <div v-if="!reviews.length" class="text-gray-600">Aún no hay comentarios para este vuelo.</div>
          <CommentsThread v-else :items="reviews" :flight-id="Number(flight.idFlight)" :current-user-id="currentUserId" @refresh="loadReviews" />
          <div class="mt-4">
            <div class="flex items-center gap-2 text-yellow-500 mb-2">
              <button v-for="i in 5" :key="i" @click="newRating = i">{{ i <= newRating ? '★' : '☆' }}</button>
            </div>
            <textarea v-model="newComment" rows="3" class="form-textarea w-full" placeholder="Escribe tu comentario (opcional)"></textarea>
            <button class="btn-primary mt-2" @click="submitReview">Publicar</button>
          </div>
        </div>

        <!-- Action buttons -->
        <div class="action-buttons">
          <button @click="goBack" class="btn-secondary">
            ← Volver a Vuelos
          </button>
          <button @click="addToCart" class="btn-cart">
            🛒 Agregar al Carrito
          </button>
          <button @click="showBookingFormModal" class="btn-primary">
            🎫 Reservar Ahora
          </button>
        </div>
      </div>

      <!-- Booking Form Modal -->
      <div v-if="showBookingForm" class="booking-modal-overlay" @click="closeBookingForm">
        <div class="booking-modal" @click.stop>
          <div class="modal-header">
            <h2>Reservar Vuelo {{ flight.flightNumber }}</h2>
            <button @click="closeBookingForm" class="close-button">×</button>
          </div>
          
          <div class="modal-content">
            <!-- Passenger Information -->
            <div class="form-section">
              <h3>Información de Pasajeros</h3>
              
              <!-- Number of passengers selector -->
              <div class="passenger-count-selector">
                <label for="passengerCount">Número de Pasajeros</label>
                <select
                  id="passengerCount"
                  v-model="passengerCount"
                  @change="updatePassengerForms"
                  class="form-select"
                >
                  <option value="1">1 Pasajero</option>
                  <option value="2">2 Pasajeros</option>
                  <option value="3">3 Pasajeros</option>
                  <option value="4">4 Pasajeros</option>
                  <option value="5">5 Pasajeros</option>
                </select>
              </div>

              <!-- Passenger forms -->
              <div v-for="(passenger, index) in passengerForms" :key="index" class="passenger-form-card">
                <div class="passenger-header">
                  <h4>Pasajero {{ index + 1 }}</h4>
                  <div v-if="index === 0" class="primary-passenger-badge">
                    <span>👤 Principal</span>
                  </div>
                </div>
                
                <div class="form-grid">
                  <div class="form-group">
                    <label :for="`firstName${index}`">Nombre *</label>
                    <input
                      :id="`firstName${index}`"
                      v-model="passenger.firstName"
                      type="text"
                      required
                      :placeholder="index === 0 ? 'Tu nombre' : 'Nombre del pasajero'"
                      class="form-input"
                    />
                  </div>
                  
                  <div class="form-group">
                    <label :for="`lastName${index}`">Apellido *</label>
                    <input
                      :id="`lastName${index}`"
                      v-model="passenger.lastName"
                      type="text"
                      required
                      :placeholder="index === 0 ? 'Tu apellido' : 'Apellido del pasajero'"
                      class="form-input"
                    />
                  </div>
                  
                  <div class="form-group">
                    <label :for="`documentType${index}`">Tipo de Documento *</label>
                    <select
                      :id="`documentType${index}`"
                      v-model="passenger.documentType"
                      required
                      class="form-select"
                    >
                      <option value="">Selecciona...</option>
                      <option value="PASSPORT">Pasaporte</option>
                      <option value="ID_CARD">Cédula de Identidad</option>
                      <option value="DRIVER_LICENSE">Licencia de Conducir</option>
                    </select>
                  </div>
                  
                  <div class="form-group">
                    <label :for="`documentNumber${index}`">Número de Documento *</label>
                    <input
                      :id="`documentNumber${index}`"
                      v-model="passenger.documentNumber"
                      type="text"
                      required
                      :placeholder="index === 0 ? 'Tu número de documento' : 'Número de documento'"
                      class="form-input"
                    />
                  </div>
                  
                  <div class="form-group">
                    <label :for="`email${index}`">Email *</label>
                    <input
                      :id="`email${index}`"
                      v-model="passenger.email"
                      type="email"
                      required
                      :placeholder="index === 0 ? 'tu@email.com' : 'email@ejemplo.com'"
                      class="form-input"
                    />
                  </div>
                  
                  <div class="form-group">
                    <label :for="`phone${index}`">Teléfono *</label>
                    <input
                      :id="`phone${index}`"
                      v-model="passenger.phone"
                      type="tel"
                      required
                      :placeholder="index === 0 ? '+1234567890' : '+1234567890'"
                      class="form-input"
                    />
                  </div>
                </div>
                
                <div class="form-group full-width">
                  <label :for="`specialRequests${index}`">Solicitudes Especiales</label>
                  <textarea
                    :id="`specialRequests${index}`"
                    v-model="passenger.specialRequests"
                    :placeholder="index === 0 ? 'Comidas especiales, asistencia, etc. (opcional)' : 'Solicitudes especiales (opcional)'"
                    class="form-textarea"
                    rows="2"
                  ></textarea>
                </div>
              </div>
            </div>

            <!-- Seat Selection -->
            <div class="form-section">
              <h3>Selección de Asiento</h3>
              
              <!-- Seat Category Selection -->
              <div class="seat-category-selection">
                <h4>Categoría de Asiento</h4>
                
                <!-- Loading indicator for seats -->
                <div v-if="!availableSeats || Object.keys(availableSeats).length === 0" class="seats-loading">
                  <div class="loading-spinner"></div>
                  <p>Cargando asientos disponibles...</p>
                </div>
                
                <div v-else class="category-options" v-if="!(flight && flight._legs && flight._legs.length === 2)">
                  <div
                    v-for="(seats, categoryKey) in availableSeats"
                    :key="String(categoryKey)"
                    class="category-option"
                    :class="{ 
                      'selected': selectedCategory === String(categoryKey),
                      'unavailable': seats.length === 0
                    }"
                    @click="seats.length > 0 ? selectCategory(String(categoryKey)) : null"
                  >
                    <div class="category-info">
                      <div class="category-name">{{ getCategoryName(String(categoryKey)) }}</div>
                      <div class="category-price">USD {{ formatPrice(flight.fares?.[String(categoryKey)] || 0) }}</div>
                      <div class="category-availability">
                        {{ seats.length }} asientos disponibles
                      </div>
                    </div>
                    <div class="category-icon">{{ getCategoryIcon(String(categoryKey)) }}</div>
                  </div>
                </div>
              </div>
              
              <!-- Seat Number Selection -->
              <div v-if="selectedCategory && !(flight && flight._legs && flight._legs.length === 2)" class="seat-number-selection">
                <h4>Número de Asiento</h4>
                
                <!-- Seat map legend -->
                <div class="seat-map-legend">
                  <div class="legend-item">
                    <div class="legend-color available"></div>
                    <span>Disponible</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color selected"></div>
                    <span>Seleccionado</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color occupied"></div>
                    <span>Ocupado</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color emergency"></div>
                    <span>Salida Emergencia</span>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color premium"></div>
                    <span>Premium</span>
                  </div>
                </div>

                <!-- Aircraft seat map -->
                <div class="aircraft-seat-map">
                  <!-- First Class Section -->
                  <div v-if="selectedCategory === 'FIRST_CLASS'" class="seat-section first-class">
                    <div class="section-title">Primera Clase</div>
                    <div class="seat-rows">
                      <div v-for="row in getFirstClassRows()" :key="row" class="seat-row">
                        <div class="row-number">{{ row }}</div>
                        <div class="seat-group">
                          <div
                            v-for="seat in getSeatsForRow(row, 'FIRST_CLASS')"
                            :key="seat"
                            class="seat-item"
                            :class="getSeatClasses(seat)"
                            @click="selectSeat(seat)"
                          >
                            {{ seat }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Business Section -->
                  <div v-if="selectedCategory === 'BUSINESS'" class="seat-section business">
                    <div class="section-title">Business</div>
                    <div class="seat-rows">
                      <div v-for="row in getBusinessRows()" :key="row" class="seat-row">
                        <div class="row-number">{{ row }}</div>
                        <div class="seat-group">
                          <div
                            v-for="seat in getSeatsForRow(row, 'BUSINESS')"
                            :key="seat"
                            class="seat-item"
                            :class="getSeatClasses(seat)"
                            @click="selectSeat(seat)"
                          >
                            {{ seat }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Economy Section -->
                  <div v-if="selectedCategory === 'ECONOMY'" class="seat-section economy">
                    <div class="section-title">Económico</div>
                    <div class="seat-rows">
                      <div v-for="row in getEconomyRows()" :key="row" class="seat-row">
                        <div class="row-number">{{ row }}</div>
                        <div class="seat-group">
                          <div
                            v-for="seat in getSeatsForRow(row, 'ECONOMY')"
                            :key="seat"
                            class="seat-item"
                            :class="getSeatClasses(seat)"
                            @click="selectSeat(seat)"
                          >
                            {{ seat }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Seat selection info -->
                <div class="seat-selection-info">
                  <div v-if="selectedSeat" class="selected-seat-info">
                    <strong>Asiento seleccionado:</strong> {{ selectedSeat }}
                    <div class="seat-details">
                      <span class="seat-type">{{ getSeatType(selectedSeat) }}</span>
                      <span class="seat-location">{{ getSeatLocation(selectedSeat) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Payment Information -->
            <div v-if="selectedSeat" class="form-section">
              <h3>Información de Pago</h3>
              
              <div class="payment-method-selection">
                <h4>Método de Pago</h4>
                <div class="payment-options">
                  <div class="payment-option selected">
                    <div class="payment-icon">💳</div>
                    <div class="payment-name">Tarjeta de Crédito</div>
                  </div>
                </div>
                <p class="payment-note">Solo se acepta pago con tarjeta de crédito</p>
              </div>
              
              <!-- Price Breakdown -->
              <div class="price-breakdown">
                <h4>Desglose de Precios</h4>
                <div class="price-items">
                  <div class="price-item">
                    <span>Tarifa base ({{ getCategoryName(selectedCategory) }})</span>
                    <span>USD {{ formatPrice(selectedPrice) }}</span>
                  </div>
                  <div class="price-item">
                    <span>Impuestos (15%)</span>
                    <span>USD {{ formatPrice(taxes) }}</span>
                  </div>
                  <div class="price-item">
                    <span>Cargos (5%)</span>
                    <span>USD {{ formatPrice(fees) }}</span>
                  </div>
                  <div class="price-item total">
                    <span>Total a pagar</span>
                    <span>USD {{ formatPrice(totalAmount) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="modal-footer">
            <button @click="closeBookingForm" class="btn-secondary">
              Cancelar
            </button>
            <button
              @click="submitBooking"
              :disabled="!canSubmit || loading"
              class="btn-primary"
            >
              <span v-if="loading" class="loading-spinner"></span>
              {{ loading ? 'Procesando...' : 'Confirmar Reserva' }}
            </button>
          </div>
          
          <!-- Validation Status -->
          <div v-if="!canSubmit" class="validation-status">
            <h4>Para continuar, completa:</h4>
            <ul>
              <li v-if="!selectedCategory" class="validation-item missing">
                <span class="validation-icon">❌</span>
                Selecciona una categoría de asiento
              </li>
              <li v-if="!selectedSeat" class="validation-item missing">
                <span class="validation-icon">❌</span>
                Selecciona un asiento específico
              </li>
              <li v-if="!selectedPaymentMethod" class="validation-item missing">
                <span class="validation-icon">❌</span>
                Selecciona método de pago
              </li>
              <li v-for="(passenger, index) in passengerForms" :key="index">
                <template v-if="!passenger.firstName || !passenger.lastName || !passenger.documentType || !passenger.documentNumber || !passenger.email || !passenger.phone">
                  <div class="validation-item missing">
                    <span class="validation-icon">❌</span>
                    Completa todos los campos del pasajero {{ index + 1 }}
                  </div>
                </template>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Success Message -->
      <div v-if="bookingSuccess" class="success-overlay">
        <div class="success-modal">
          <div class="success-icon">✅</div>
          <h2>¡Reserva Confirmada!</h2>
          <p v-if="passengerCount === 1">Tu boleto ha sido reservado exitosamente.</p>
          <p v-else>{{ passengerCount }} boletos han sido reservados exitosamente.</p>
          
          <div class="booking-details">
            <div v-if="flight && flight._legs && flight._legs.length === 2" class="itinerary-summary">
              <p><strong>Itinerario:</strong> 1 escala</p>
              <ul class="itinerary-list">
                <li>
                  Tramo 1: {{ flight._legs[0]?.originCity }} → {{ flight._legs[0]?.destinationCity }}
                </li>
                <li>
                  Tramo 2: {{ flight._legs[1]?.originCity }} → {{ flight._legs[1]?.destinationCity }}
                </li>
              </ul>
            </div>

            <div class="tickets-summary">
              <template v-if="passengerCount === 1">
                <p v-if="bookingResult.tickets && bookingResult.tickets.length === 1">
                  <strong>ID de Boleto:</strong> {{ bookingResult.tickets[0].ticketId }}
                </p>
                <p v-else>
                  <strong>Boletos creados:</strong> {{ bookingResult.tickets?.map((t:any)=>t.ticketId).join(', ') }}
                </p>
                <p><strong>Total Pagado:</strong> USD {{ formatPrice(bookingResult.totalAmount) }}</p>
              </template>
              <template v-else>
                <p><strong>Boletos creados:</strong> {{ bookingResult.tickets?.length }}</p>
                <p><strong>Total Pagado:</strong> USD {{ formatPrice(bookingResult.totalAmount) }}</p>
                <p><strong>Precio promedio por boleto:</strong> USD {{ formatPrice(bookingResult.totalAmount / bookingResult.tickets?.length) }}</p>
              </template>
            </div>
          </div>
          
          <div class="success-actions">
            <button @click="closeSuccess" class="btn-secondary">Cerrar</button>
            <button @click="goToTickets" class="btn-primary">Ver Mis Boletos</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { airlineApi } from '../utils/airlineApi'
import CommentsThread from '../components/CommentsThread.vue'
// import CartCounter from '../components/CartCounter.vue'
import eventBus from '../eventBus'

const route = useRoute()
const router = useRouter()

// State
const loading = ref(false)
const error = ref('')
const flight = ref<any>(null)
const cities = ref<any[]>([])
const flightId = route.params.id as string
const reviews = ref<any[]>([])
const newRating = ref(5)
const newComment = ref('')
const currentUserId = ref<number | null>(null)

// Booking state
const showBookingForm = ref(false)
const bookingSuccess = ref(false)
const bookingResult = ref<any>(null)
const availableSeats = ref<{[key: string]: string[]}>({})
const selectedCategory = ref('')
const selectedSeat = ref('')
const selectedPaymentMethod = ref('')
const passengerCount = ref(1)

// Passenger forms array
const passengerForms = ref([
  {
    firstName: '',
    lastName: '',
    documentType: '',
    documentNumber: '',
    email: '',
    phone: '',
    specialRequests: ''
  }
])

// User data for auto-fill
const currentUser = ref<any>(null)

// Auto-select credit card as default payment method
selectedPaymentMethod.value = 'CREDIT_CARD'

// Payment methods (solo tarjeta de crédito)

// Computed properties
const selectedPrice = computed(() => flight.value?.fares?.[selectedCategory.value] || 0)
const taxes = computed(() => selectedPrice.value * 0.15) // 15% taxes
const fees = computed(() => selectedPrice.value * 0.05) // 5% fees
const totalAmount = computed(() => selectedPrice.value + taxes.value + fees.value)

const canSubmit = computed(() => {
  // Permitir compra sin asiento seleccionado (se usará 'AUTO')
  return Boolean(selectedCategory.value && selectedPaymentMethod.value) &&
    passengerForms.value.every(passenger => 
      passenger.firstName &&
      passenger.lastName &&
      passenger.documentType &&
      passenger.documentNumber &&
      passenger.email &&
      passenger.phone
    )
})

// Methods
const loadFlightDetails = async () => {
  try {
    loading.value = true
    // Intentar usar un itinerario seleccionado previamente (directo o con escala)
    try {
      const cached = sessionStorage.getItem('selectedItinerary')
      if (cached) {
        const parsed = JSON.parse(cached)
        if (parsed && (parsed.idFlight || parsed._legs)) {
          flight.value = parsed
        }
      }
    } catch {}

    if (!flight.value) {
      const response = await airlineApi.getFlights()
      if (response.success) {
        const flightIdNum = parseInt(flightId)
        flight.value = response.flights.find((f: any) => f.idFlight === flightIdNum)
      }
    }

    if (flight.value) {
      // Si es itinerario con escala, combinar tarifas y preparar selección automática
      const isOneStop = !!(flight.value._legs && flight.value._legs.length === 2)
      if (isOneStop) {
        const leg1 = flight.value._legs[0] || {}
        const leg2 = flight.value._legs[1] || {}
        const keys = new Set<string>([
          ...Object.keys(leg1.fares || {}),
          ...Object.keys(leg2.fares || {})
        ])
        const combinedFares: Record<string, number> = {}
        if (keys.size === 0) {
          // Fallback: usar basePrice sumado como ECONOMY
          combinedFares['ECONOMY'] = Number(leg1.basePrice || 0) + Number(leg2.basePrice || 0)
        } else {
          keys.forEach((k) => {
            const a = Number((leg1.fares || {})[k] ?? leg1.basePrice ?? 0)
            const b = Number((leg2.fares || {})[k] ?? leg2.basePrice ?? 0)
            combinedFares[k] = a + b
          })
        }
        flight.value.fares = combinedFares
        if (!selectedCategory.value) {
          selectedCategory.value = combinedFares['ECONOMY'] !== undefined
            ? 'ECONOMY'
            : Object.keys(combinedFares)[0] || ''
        }
      }
      await loadAvailableSeats()
      await loadReviews()
    }
  } catch (err) {
    error.value = 'Error cargando detalles del vuelo'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadReviews = async () => {
  try {
    if (!flight.value) return
    const res = await airlineApi.getFlightReviews(flight.value.idFlight, { mode: 'tree' })
    reviews.value = Array.isArray(res?.reviews) ? res.reviews : []
  } catch (e) {
    console.warn('No se pudieron cargar reseñas', e)
    reviews.value = []
  }
}

const submitReview = async () => {
  try {
    if (!flight.value) return
    if (!currentUserId.value) {
      router.push('/login?redirect=' + encodeURIComponent(`/flight-details/${flight.value.idFlight}`))
      return
    }
    await airlineApi.createFlightReview(flight.value.idFlight, {
      userId: currentUserId.value,
      rating: newRating.value,
      comment: newComment.value
    })
    newComment.value = ''
    newRating.value = 5
    await loadReviews()
  } catch (e) {
    alert('No se pudo publicar tu reseña')
  }
}

// Carga de ciudades (no utilizada actualmente)

const loadAvailableSeats = async () => {
  try {
    console.log('🔍 Cargando asientos para vuelo:', flight.value.idFlight)
    if (flight.value && flight.value._legs && flight.value._legs.length === 2) {
      // Para itinerarios con escala, omitimos carga de asientos por tramo y usamos asignación AUTO
      availableSeats.value = {}
      return
    }
    const response = await airlineApi.getAvailableSeats(flight.value.idFlight)
    console.log('📡 Respuesta de asientos:', response)
    
    if (response.success) {
      // El backend envía 'seatsByCategory', no 'availableSeats'
      if (response.seatsByCategory) {
        availableSeats.value = response.seatsByCategory
        console.log('✅ Asientos cargados:', availableSeats.value)
      } else {
        console.warn('⚠️ No se encontró seatsByCategory en la respuesta')
        availableSeats.value = {}
      }
    } else {
      console.error('❌ Error en respuesta de asientos:', response.error)
      availableSeats.value = {}
    }
  } catch (err) {
    console.error('❌ Error cargando asientos:', err)
    availableSeats.value = {}
  }
}

// Load current user data
const loadCurrentUser = () => {
  try {
    const userData = localStorage.getItem('user')
    if (userData) {
      currentUser.value = JSON.parse(userData)
      // Auto-fill first passenger form with user data
      if (currentUser.value) {
        passengerForms.value[0] = {
          firstName: currentUser.value.firstName || currentUser.value.name || '',
          lastName: currentUser.value.lastName || currentUser.value.surname || '',
          documentType: currentUser.value.documentType || 'ID_CARD',
          documentNumber: currentUser.value.documentNumber || currentUser.value.id || '',
          email: currentUser.value.email || '',
          phone: currentUser.value.phone || currentUser.value.telephone || '',
          specialRequests: ''
        }
      }
    }
  } catch (err) {
    console.error('Error cargando usuario:', err)
  }
}

// Update passenger forms based on count
const updatePassengerForms = () => {
  const count = parseInt(passengerCount.value.toString())
  const currentForms = passengerForms.value.length
  
  if (count > currentForms) {
    // Add new forms
    for (let i = currentForms; i < count; i++) {
      passengerForms.value.push({
        firstName: '',
        lastName: '',
        documentType: '',
        documentNumber: '',
        email: '',
        phone: '',
        specialRequests: ''
      })
    }
  } else if (count < currentForms) {
    // Remove extra forms
    passengerForms.value = passengerForms.value.slice(0, count)
  }
}

// Booking methods
const showBookingFormModal = () => {
  showBookingForm.value = true
  loadCurrentUser() // Load user data when opening modal
}

const addToCart = () => {
  // Check if user is logged in
  const userData = localStorage.getItem('user')
  if (!userData) {
    // Redirect to login if not authenticated
    router.push('/login')
    return
  }
  
  // Check if flight has required data
  if (!flight.value) {
    error.value = 'Error: No se pudo cargar la información del vuelo'
    return
  }
  
  try {
    // Get current cart
    const currentCart = localStorage.getItem('flight_cart') || '[]'
    const cartItems = JSON.parse(currentCart)
    
    // 🔍 DETECTAR SI ES VUELO CON ESCALA
    const stopoverRoute = sessionStorage.getItem('stopoverRoute')
    const isStopover = stopoverRoute !== null
    
    if (isStopover) {
      // ✈️ VUELO CON ESCALA: Agregar 2 vuelos al carrito
      const route = JSON.parse(stopoverRoute)
      
      console.log('✈️ Agregando vuelo con escala al carrito:', route)
      
      // Agregar segmento 1
      const cartItem1 = {
        flight: {
          ...route.firstSegment,
          isStopoverSegment: true,
          segmentNumber: 1,
          totalSegments: 2,
          viaCityName: route.viaCityName
        },
        selectedCategory: '',
        selectedSeat: '',
        availableSeats: {},
        processing: false
      }
      
      // Agregar segmento 2
      const cartItem2 = {
        flight: {
          ...route.secondSegment,
          isStopoverSegment: true,
          segmentNumber: 2,
          totalSegments: 2,
          viaCityName: route.viaCityName
        },
        selectedCategory: '',
        selectedSeat: '',
        availableSeats: {},
        processing: false
      }
      
      cartItems.push(cartItem1)
      cartItems.push(cartItem2)
      
      // Limpiar sessionStorage
      sessionStorage.removeItem('stopoverRoute')
      
      alert(`✅ Vuelo con escala agregado al carrito (2 segmentos)\\n\\nSegmento 1: ${route.firstSegment.originCity} → ${route.firstSegment.destinationCity}\\nSegmento 2: ${route.secondSegment.originCity} → ${route.secondSegment.destinationCity}\\n\\nTotal: $${route.totalPrice}`)
      
    } else {
      // 👤 VUELO DIRECTO: Agregar 1 vuelo normal
      const existingItem = cartItems.find((item: any) => item.flight.idFlight === flight.value.idFlight)
      if (existingItem) {
        error.value = 'Este vuelo ya está en tu carrito'
        return
      }
      
      const cartItem = {
        flight: flight.value,
        selectedCategory: '',
        selectedSeat: '',
        availableSeats: {},
        processing: false
      }
      
      cartItems.push(cartItem)
      
      alert('✅ Vuelo agregado al carrito exitosamente')
    }
    
    // Save updated cart
    localStorage.setItem('flight_cart', JSON.stringify(cartItems))
    
    // Actualizar contador del carrito (emitir evento)
    window.dispatchEvent(new Event('cart-updated'))
    
  } catch (err) {
    console.error('Error agregando al carrito:', err)
    error.value = 'Error agregando el vuelo al carrito'
  }
}

const closeBookingForm = () => {
  showBookingForm.value = false
  resetForm()
}

const resetForm = () => {
  selectedCategory.value = ''
  selectedSeat.value = ''
  selectedPaymentMethod.value = ''
  passengerCount.value = 1
  passengerForms.value = [{
    firstName: '',
    lastName: '',
    documentType: '',
    documentNumber: '',
    email: '',
    phone: '',
    specialRequests: ''
  }]
}

const selectCategory = (category: string) => {
  selectedCategory.value = category
  selectedSeat.value = '' // Reset seat selection
}

const selectSeat = (seat: string) => {
  if (!isSeatOccupied(seat)) {
    selectedSeat.value = seat
  }
}

// selectPaymentMethod no utilizado; mantenemos método de pago por defecto

const isSeatOccupied = (seat: string) => {
  if (!availableSeats.value || !selectedCategory.value) return false
  
  const categorySeats = availableSeats.value[selectedCategory.value]
  if (!categorySeats) return false
  
  // Si el asiento no está en la lista de disponibles, está ocupado
  return !categorySeats.includes(seat)
}

const submitBooking = async () => {
  if (!canSubmit.value) return
  
  try {
    loading.value = true
    error.value = ''
    
    const isOneStop = !!(flight.value && flight.value._legs && flight.value._legs.length === 2)
    const tickets: any[] = []

    for (let i = 0; i < passengerForms.value.length; i++) {
      const passenger = passengerForms.value[i]

      if (isOneStop) {
        // Dos tramos: crear dos boletos (uno por cada segmento)
        for (let legIndex = 0; legIndex < 2; legIndex++) {
          const seg = flight.value._legs[legIndex]
          const legFare = Number(seg?.fares?.[selectedCategory.value] || seg?.basePrice || 0)
          const bookingData = {
            flightId: seg.idFlight,
            userId: currentUser.value?.idUser || 1,
            seatNumber: selectedSeat.value ? `${selectedSeat.value}${i > 0 ? i + 1 : ''}` : 'AUTO',
            seatCategory: selectedCategory.value,
            fare: legFare,
            passengerFirstName: passenger.firstName,
            passengerLastName: passenger.lastName,
            passengerDocumentType: passenger.documentType,
            passengerDocumentNumber: passenger.documentNumber,
            passengerEmail: passenger.email,
            passengerPhone: passenger.phone,
            specialRequests: passenger.specialRequests,
            paymentMethod: selectedPaymentMethod.value,
            totalAmount: legFare
          }
          const resp = await airlineApi.createTicket(bookingData)
          if (resp && (resp.success || resp.ticketId)) {
            tickets.push(resp)
          } else {
            throw new Error(`Error creando boleto (tramo ${legIndex + 1}) para ${passenger.firstName}: ${resp?.error || 'Respuesta inválida'}`)
          }
        }
      } else {
        // Vuelo directo: un boleto
        const bookingData = {
          flightId: flight.value.idFlight,
          userId: currentUser.value?.idUser || 1,
          seatNumber: selectedSeat.value ? `${selectedSeat.value}${i > 0 ? i + 1 : ''}` : 'AUTO',
          seatCategory: selectedCategory.value,
          fare: selectedPrice.value,
          passengerFirstName: passenger.firstName,
          passengerLastName: passenger.lastName,
          passengerDocumentType: passenger.documentType,
          passengerDocumentNumber: passenger.documentNumber,
          passengerEmail: passenger.email,
          passengerPhone: passenger.phone,
          specialRequests: passenger.specialRequests,
          paymentMethod: selectedPaymentMethod.value,
          totalAmount: selectedPrice.value
        }
        const response = await airlineApi.createTicket(bookingData)
        if (response && (response.success || response.ticketId)) {
          tickets.push(response)
        } else {
          throw new Error(`Error creando boleto para ${passenger.firstName}: ${response?.error || 'Respuesta inválida del servidor'}`)
        }
      }
    }

    // Totales (si es con escala, sumar tarifas de ambos tramos)
    const totalPerPassenger = isOneStop
      ? (Number(flight.value?._legs?.[0]?.fares?.[selectedCategory.value] || flight.value?._legs?.[0]?.basePrice || 0) +
         Number(flight.value?._legs?.[1]?.fares?.[selectedCategory.value] || flight.value?._legs?.[1]?.basePrice || 0))
      : selectedPrice.value

    bookingResult.value = {
      success: true,
      tickets,
      totalAmount: totalPerPassenger * passengerForms.value.length
    }
    bookingSuccess.value = true
    closeBookingForm()
    
    // Refrescar availableSeats del vuelo desde backend y emitir evento global
    try {
      const fresh = await airlineApi.getFlight(flight.value.idFlight)
      if (fresh && (fresh.flight || fresh)) {
        const updated = fresh.flight ? fresh.flight : fresh
        if (updated.availableSeats !== undefined) {
          flight.value.availableSeats = updated.availableSeats
          // Notificar a otras vistas (listado, etc.)
          eventBus.emit('inventory:updated', {
            flightId: flight.value.idFlight,
            availableSeats: updated.availableSeats
          })
        }
      }
    } catch (e) {
      console.warn('No se pudo refrescar availableSeats:', e)
    }
    
  } catch (err) {
    error.value = 'Error procesando la reserva: ' + (err as Error).message
    console.error(err)
  } finally {
    loading.value = false
  }
}

const closeSuccess = () => {
  bookingSuccess.value = false
  bookingResult.value = null
}

const goToTickets = () => {
  router.push('/my-bookings')
}

const goBack = () => {
  router.push('/flights')
}

// Funciones auxiliares
const getCityName = (cityId: number | string) => {
  console.log('🔍 Buscando ciudad con ID:', cityId, 'en ciudades:', cities.value)
  
  // Si cityId es undefined o null, retornar un valor por defecto
  if (!cityId) {
    console.log('❌ cityId es undefined o null')
    return 'Ciudad no especificada'
  }
  
  const city = cities.value.find(c => c.idCity == cityId)
  if (city) {
    console.log('✅ Ciudad encontrada:', city.name)
    return city.name
  } else {
    console.log('❌ Ciudad no encontrada para ID:', cityId)
    return `Ciudad ${cityId}`
  }
}

const getCityCode = (cityName: string) => {
  // Generar código de aeropuerto basado en el nombre de la ciudad
  if (!cityName) return '---'
  return cityName.split(' ').map(word => word.charAt(0)).join('').toUpperCase().substring(0, 3)
}

const formatDate = (dateStr: string | number | null | undefined) => {
  if (!dateStr && dateStr !== 0) return '--'
  try {
    const date = new Date(String(dateStr))
    return date.toLocaleDateString('es-ES', { 
      weekday: 'short', 
      day: 'numeric', 
      month: 'short' 
    })
  } catch {
    return '--'
  }
}

const formatTime = (timeStr: string | number | null | undefined) => {
  if (!timeStr && timeStr !== 0) return '--:--'
  return String(timeStr)
}

const formatPrice = (price: number | string) => {
  const n = Number(price)
  if (Number.isNaN(n)) return '0.00'
  return n.toFixed(2)
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
  
  // Para vuelos directos, simulación
  return '2h 30m'
}

const getStatusClass = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'SCHEDULED': return 'status-scheduled'
    case 'BOARDING': return 'status-boarding'
    case 'DEPARTED': return 'status-departed'
    case 'ARRIVED': return 'status-arrived'
    case 'CANCELLED': return 'status-cancelled'
    case 'DELAYED': return 'status-delayed'
    default: return 'status-unknown'
  }
}

const getStatusText = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'SCHEDULED': return 'Programado'
    case 'BOARDING': return 'Embarcando'
    case 'DEPARTED': return 'Partió'
    case 'ARRIVED': return 'Llegó'
    case 'CANCELLED': return 'Cancelado'
    case 'DELAYED': return 'Retrasado'
    default: return 'Desconocido'
  }
}

// Funciones auxiliares para categorías de asientos
const getCategoryName = (category: string) => {
  switch (category?.toUpperCase()) {
    case 'ECONOMY': return 'Económico'
    case 'BUSINESS': return 'Business'
    case 'FIRST_CLASS': return 'Primera Clase'
    default: return category
  }
}

const getCategoryIcon = (category: string) => {
  switch (category?.toUpperCase()) {
    case 'ECONOMY': return '💺'
    case 'BUSINESS': return '🛋️'
    case 'FIRST_CLASS': return '👑'
    default: return '✈️'
  }
}

const getCategoryFeatures = (category: string) => {
  switch (category?.toUpperCase()) {
    case 'ECONOMY': return 'Asiento estándar con servicio básico'
    case 'BUSINESS': return 'Asiento amplio con servicio premium'
    case 'FIRST_CLASS': return 'Máximo confort y servicio exclusivo'
    default: return 'Servicio estándar'
  }
}

// Seat selection logic - Dinámico basado en disponibilidad real
const getFirstClassRows = () => {
  if (!availableSeats.value || !availableSeats.value.FIRST_CLASS) return []
  return ['A', 'B', 'C', 'D', 'E', 'F'].filter(row => 
    getSeatsForRow(row, 'FIRST_CLASS').some(seat => 
      availableSeats.value.FIRST_CLASS.includes(seat)
    )
  )
}

const getBusinessRows = () => {
  if (!availableSeats.value || !availableSeats.value.BUSINESS) return []
  return ['G', 'H', 'J', 'K', 'L', 'M'].filter(row => 
    getSeatsForRow(row, 'BUSINESS').some(seat => 
      availableSeats.value.BUSINESS.includes(seat)
    )
  )
}

const getEconomyRows = () => {
  if (!availableSeats.value || !availableSeats.value.ECONOMY) return []
  return ['N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'].filter(row => 
    getSeatsForRow(row, 'ECONOMY').some(seat => 
      availableSeats.value.ECONOMY.includes(seat)
    )
  )
}

const getSeatsForRow = (row: string, category: string) => {
  const seats: string[] = []
  const totalSeats = 6 // 6 asientos por fila
  
  for (let i = 1; i <= totalSeats; i++) {
    const seatNumber = `${row}${i}`
    // Solo incluir asientos que estén realmente disponibles en esa categoría
    if (availableSeats.value && 
        availableSeats.value[category] && 
        availableSeats.value[category].includes(seatNumber)) {
      seats.push(seatNumber)
    }
  }
  return seats
}

const getSeatClasses = (seat: string) => {
  if (isSeatOccupied(seat)) {
    return { occupied: true }
  }
  if (selectedSeat.value === seat) {
    return { selected: true }
  }
  return {}
}

const getSeatType = (seat: string) => {
  if (seat.includes('A') || seat.includes('B') || seat.includes('C') || seat.includes('D') || seat.includes('E') || seat.includes('F')) {
    return 'Primera Clase'
  } else if (seat.includes('G') || seat.includes('H') || seat.includes('J') || seat.includes('K') || seat.includes('L') || seat.includes('M')) {
    return 'Business'
  } else {
    return 'Económico'
  }
}

const getSeatLocation = (seat: string) => {
  const row = seat.charAt(0)
  const number = parseInt(seat.substring(1))
  return `${row}${number}`
}

// const getAvailableSeatsCount = (category: string) => {
//   if (!availableSeats.value || !availableSeats.value[category]) return 0
//   return availableSeats.value[category].length
// }

// Lifecycle
onMounted(() => {
  console.log('🚀 Componente flight-details montado')
  console.log('🔍 Flight ID de la URL:', flightId)
  try {
    const u = localStorage.getItem('user')
    if (u) currentUserId.value = JSON.parse(u)?.idUser || null
  } catch {}
  loadFlightDetails()
})

// Watcher para debug
import { watch } from 'vue'

watch(flight, (newFlight) => {
  console.log('👀 Flight cambió:', newFlight)
  if (newFlight && newFlight.fares) {
    console.log('💰 Tarifas disponibles:', newFlight.fares)
    console.log('🔍 Tipo de tarifas:', typeof newFlight.fares)
    console.log('📋 Claves de tarifas:', Object.keys(newFlight.fares))
  }
}, { deep: true })

watch(loading, (newLoading) => {
  console.log('👀 Loading cambió:', newLoading)
})

watch(error, (newError) => {
  console.log('👀 Error cambió:', newError)
})
</script>

<style scoped>
.flight-details-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* Header */
.flight-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.nav-link:hover {
  color: #3b82f6;
}

/* Main Content */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

/* Debug Info */
.debug-info {
  background: #f8fafc;
  border-radius: 0.5rem;
  padding: 1rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.debug-info h3 {
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.debug-info p {
  color: #4b5563;
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
}

.flight-fields {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.flight-fields h4 {
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.flight-fields pre {
  background: #f3f4f6;
  border-radius: 0.375rem;
  padding: 0.75rem 1rem;
  font-size: 0.75rem;
  color: #374151;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* Loading Section */
.loading-section {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 70vh;
  padding: 2rem;
}

.loading-container {
  background: white;
  border-radius: 2rem;
  padding: 4rem 3rem;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
  position: relative;
  overflow: hidden;
}

.loading-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6, #06b6d1);
  animation: shimmer 2s infinite;
}

.loading-plane {
  font-size: 5rem;
  margin-bottom: 2rem;
  animation: fly 3s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.loading-text h2 {
  color: #1f2937;
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.loading-text p {
  color: #6b7280;
  font-size: 1.125rem;
  margin-bottom: 2rem;
  font-weight: 500;
}

.loading-progress {
  margin-top: 2rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  border-radius: 4px;
  animation: progress 2s ease-in-out infinite;
  width: 0%;
}

@keyframes fly {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-10px) rotate(5deg);
  }
  50% {
    transform: translateY(-5px) rotate(0deg);
  }
  75% {
    transform: translateY(-15px) rotate(-5deg);
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes progress {
  0% {
    width: 0%;
  }
  50% {
    width: 70%;
  }
  100% {
    width: 100%;
  }
}

/* Error Section */
.error-section {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 1rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.error-icon {
  font-size: 3rem;
  color: #ef4444;
}

.error-section h2 {
  color: #374151;
  margin: 1rem 0;
}

.error-section p {
  color: #6b7280;
  margin-bottom: 2rem;
}

.back-btn {
  padding: 0.75rem 1.5rem;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.back-btn:hover {
  background: #4b5563;
}

/* Flight Header Card */
.flight-header-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.flight-number-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.flight-number-section h1 {
  font-size: 2.5rem;
  color: #1f2937;
  margin: 0;
}

.flight-status {
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-scheduled { background: #dbeafe; color: #1e40af; }
.status-boarding { background: #fef3c7; color: #d97706; }
.status-departed { background: #d1fae5; color: #059669; }
.status-arrived { background: #d1fae5; color: #059669; }
.status-cancelled { background: #fee2e2; color: #dc2626; }
.status-delayed { background: #fef3c7; color: #d97706; }
.status-unknown { background: #f3f4f6; color: #6b7280; }

.flight-route {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
}

.route-item {
  text-align: center;
}

.route-item .city {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.route-item .airport-code {
  font-size: 1rem;
  color: #6b7280;
  font-weight: 500;
}

.route-arrow {
  font-size: 2rem;
  color: #3b82f6;
  font-weight: bold;
}

/* Itinerary Section */
.itinerary-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.itinerary-section h2 {
  color: #1f2937;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.badge-one-stop {
  display: inline-block;
  margin-left: 0.5rem;
  font-size: 0.875rem;
  background: #f59e0b;
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 0.375rem;
}

.main-flight-card {
  margin-bottom: 2rem;
}

.flight-timeline {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.timeline-item {
  text-align: center;
  flex: 1;
}

.timeline-item .time {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.timeline-item .date {
  font-size: 1rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.timeline-item .city {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
}

.timeline-item .details {
  font-size: 0.875rem;
  color: #6b7280;
}

.flight-connection {
  text-align: center;
  flex: 1;
}

.connection-line {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.line {
  height: 2px;
  background: #e5e7eb;
  flex: 1;
}

.plane-icon {
  font-size: 1.5rem;
  margin: 0 1rem;
  color: #3b82f6;
}

.connection-info {
  text-align: center;
}

.connection-info .duration {
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.connection-info .flight-type {
  font-size: 0.875rem;
  color: #6b7280;
}

/* Stops Section */
.stops-section {
  margin-top: 2rem;
}

.stops-section h3 {
  color: #1f2937;
  margin-bottom: 1rem;
  font-size: 1.25rem;
}

.stops-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stop-card {
  background: #f8fafc;
  border-radius: 0.5rem;
  padding: 1.5rem;
  border-left: 4px solid #3b82f6;
}

.stop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.stop-number {
  font-weight: 600;
  color: #3b82f6;
  font-size: 0.875rem;
}

.stop-city {
  font-weight: 600;
  color: #1f2937;
  font-size: 1.125rem;
}

.stop-city .city-warning {
  font-size: 0.75rem;
  color: #d97706;
  margin-left: 0.5rem;
}

.info-note {
  font-size: 0.75rem;
  color: #6b7280;
  font-style: italic;
  margin-left: 0.25rem;
}

.stop-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.stop-times, .stop-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.time-item, .connection-time, .aircraft-change {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  font-size: 0.875rem;
  color: #6b7280;
}

.time, .value {
  font-weight: 500;
  color: #1f2937;
}

/* Details Section */
.details-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.details-section h2 {
  color: #1f2937;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 0.5rem;
}

.detail-item .label {
  font-weight: 500;
  color: #6b7280;
}

.detail-item .value {
  font-weight: 600;
  color: #1f2937;
}

.detail-item .value.price {
  color: #059669;
  font-size: 1.125rem;
}

/* Seat categories and prices */
.fares-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.fares-section h2 {
  color: #1f2937;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.fares-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.fare-card {
  background: #f8fafc;
  border-radius: 0.5rem;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.fare-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.fare-header .category-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.fare-header .category-icon {
  font-size: 2rem;
  color: #3b82f6;
}

.fare-price {
  font-size: 2rem;
  font-weight: 700;
  color: #059669;
  margin-bottom: 0.5rem;
}

.fare-price .currency {
  font-size: 0.875rem;
  color: #6b7280;
  margin-right: 0.25rem;
}

.fare-price .amount {
  font-size: 1.5rem;
  color: #059669;
}

.fare-features {
  font-size: 0.875rem;
  color: #6b7280;
  text-align: left;
  width: 100%;
}

.fare-features .feature {
  margin-bottom: 0.5rem;
}

.fare-card.economy { border-left: 4px solid #3b82f6; }
.fare-card.business { border-left: 4px solid #d97706; }
.fare-card.first-class { border-left: 4px solid #1d4ed8; }

/* Check-in Section */
.checkin-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.checkin-section h2 {
  color: #1f2937;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.checkin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.checkin-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 0.5rem;
}

.checkin-item .label {
  font-weight: 500;
  color: #6b7280;
}

.checkin-item .value {
  font-weight: 600;
  color: #1f2937;
}

.checkin-note {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
  font-size: 0.875rem;
  color: #6b7280;
}

/* Actions Section */
.actions-section {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
}

.primary-btn, .secondary-btn {
  padding: 1rem 2rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.primary-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
}

.primary-btn:hover {
  transform: translateY(-2px);
}

.secondary-btn {
  background: #6b7280;
  color: white;
}

.secondary-btn:hover {
  transform: translateY(-2px);
}

/* Action buttons */
.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.btn-primary,
.btn-secondary,
.btn-cart {
  padding: 1rem 2rem;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background: #4b5563;
  transform: translateY(-1px);
}

.btn-cart {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.btn-cart:hover {
  background: #b45309;
  transform: translateY(-1px);
}

/* Booking Modal */
.booking-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.booking-modal {
  background: white;
  border-radius: 1rem;
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(0,0,0,0.25);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 1.5rem;
}

.close-button {
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-button:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-content {
  padding: 2rem;
}

.form-section {
  margin-bottom: 2rem;
}

.form-section h3 {
  color: #1f2937;
  margin-bottom: 1rem;
  font-size: 1.25rem;
}

.form-section h4 {
  color: #374151;
  margin-bottom: 0.75rem;
  font-size: 1.125rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.form-input,
.form-select,
.form-textarea {
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.5rem;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

/* Passenger count selector */
.passenger-count-selector {
  margin-bottom: 1.5rem;
  max-width: 200px;
}

.passenger-count-selector label {
  display: block;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

/* Passenger form cards */
.passenger-form-card {
  background: #f8fafc;
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border: 1px solid #e5e7eb;
}

.passenger-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
}

.passenger-header h4 {
  margin: 0;
  color: #1f2937;
  font-size: 1.125rem;
  font-weight: 600;
}

.primary-passenger-badge {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.primary-passenger-badge span {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

/* Seat Selection */
.seat-category-selection {
  margin-bottom: 1.5rem;
}

.category-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.category-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.category-option:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.category-option.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.category-option.unavailable {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f3f4f6;
  border-color: #d1d5db;
}

.category-option.unavailable:hover {
  border-color: #d1d5db;
  background: #f3f4f6;
}

.category-info {
  flex: 1;
}

.category-name {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.category-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: #059669;
}

.category-availability {
  font-size: 0.875rem;
  color: #6b7280;
  margin-top: 0.25rem;
  font-weight: 500;
}

.category-icon {
  font-size: 1.5rem;
  margin-left: 1rem;
}

/* Seats Loading */
.seats-loading {
  text-align: center;
  padding: 2rem;
  background: #f8fafc;
  border-radius: 0.75rem;
  border: 2px dashed #d1d5db;
}

.seats-loading .loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.seats-loading p {
  color: #6b7280;
  font-size: 1rem;
  margin: 0;
}

.seat-number-selection {
  margin-bottom: 1.5rem;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(50px, 1fr));
  gap: 0.5rem;
  margin-bottom: 1rem;
  max-width: 400px;
}

.seat-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e5e7eb;
  border-radius: 0.5rem;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
  transition: all 0.2s;
  background: white;
}

.seat-item:hover:not(.occupied) {
  border-color: #3b82f6;
  background: #eff6ff;
}

.seat-item.selected {
  border-color: #3b82f6;
  background: #3b82f6;
  color: white;
}

.seat-item.occupied {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  border-color: #d1d5db;
}

.seat-legend {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 0.25rem;
}

.legend-color.available {
  background: white;
  border: 2px solid #e5e7eb;
}

.legend-color.selected {
  background: #3b82f6;
}

.legend-color.occupied {
  background: #f3f4f6;
  border: 2px solid #d1d5db;
}

.legend-color.emergency {
  background: #fef3c7;
  border: 2px solid #f59e0b;
}

.legend-color.premium {
  background: #e0e7ff;
  border: 2px solid #6366f1;
}

/* Aircraft seat map */
.aircraft-seat-map {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.seat-section {
  margin-bottom: 2rem;
}

.seat-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1rem;
  text-align: center;
  padding: 0.5rem;
  border-radius: 0.5rem;
}

.first-class .section-title {
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%);
  color: white;
}

.business .section-title {
  background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%);
  color: white;
}

.economy .section-title {
  background: linear-gradient(135deg, #059669 0%, #10b981 100%);
  color: white;
}

.seat-rows {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.seat-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.row-number {
  font-weight: 600;
  color: #6b7280;
  min-width: 30px;
  text-align: center;
}

.seat-group {
  display: flex;
  gap: 0.5rem;
  flex: 1;
  justify-content: center;
}

.seat-item {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e5e7eb;
  border-radius: 0.5rem;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
  transition: all 0.2s;
  background: white;
  position: relative;
}

.seat-item:hover:not(.occupied) {
  border-color: #3b82f6;
  background: #eff6ff;
  transform: scale(1.05);
}

.seat-item.selected {
  border-color: #3b82f6;
  background: #3b82f6;
  color: white;
  transform: scale(1.1);
}

.seat-item.occupied {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  border-color: #d1d5db;
}

.seat-item.emergency {
  background: #fef3c7;
  border-color: #f59e0b;
  color: #d97706;
}

.seat-item.premium {
  background: #e0e7ff;
  border-color: #6366f1;
  color: #4f46e5;
}

/* Seat selection info */
.seat-selection-info {
  background: #f8fafc;
  border-radius: 0.5rem;
  padding: 1rem;
  margin-top: 1rem;
}

.selected-seat-info {
  text-align: center;
}

.seat-details {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 0.5rem;
}

.seat-type,
.seat-location {
  background: #e5e7eb;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  color: #374151;
}

/* Payment Section */
.payment-method-selection {
  margin-bottom: 1.5rem;
}

.payment-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.payment-option:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.payment-option.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.payment-icon {
  font-size: 1.5rem;
}

.payment-name {
  font-weight: 600;
  color: #1f2937;
}

.payment-note {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
  text-align: center;
  font-style: italic;
}

.price-breakdown {
  background: #f8fafc;
  border-radius: 0.5rem;
  padding: 1.5rem;
}

.price-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e5e7eb;
}

.price-item:last-child {
  border-bottom: none;
}

.price-item.total {
  font-weight: 700;
  font-size: 1.125rem;
  color: #059669;
  border-top: 2px solid #e5e7eb;
  padding-top: 1rem;
  margin-top: 0.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem 2rem;
  border-top: 1px solid #e5e7eb;
  background: #f8fafc;
  border-radius: 0 0 1rem 1rem;
}

/* Validation Status */
.validation-status {
  margin-top: 1.5rem;
  padding: 1.5rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 0.75rem;
}

.validation-status h4 {
  color: #dc2626;
  margin: 0 0 1rem 0;
  font-size: 1rem;
  font-weight: 600;
}

.validation-status ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.validation-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
  color: #dc2626;
  font-size: 0.875rem;
}

.validation-item.missing {
  color: #dc2626;
}

.validation-icon {
  font-size: 1rem;
}

/* Success Modal */
.success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  padding: 1rem;
}

.success-modal {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  text-align: center;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 25px 50px rgba(0,0,0,0.25);
}

.success-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.success-modal h2 {
  color: #059669;
  margin-bottom: 1rem;
}

.booking-details {
  background: #f0fdf4;
  border-radius: 0.5rem;
  padding: 1rem;
  margin: 1.5rem 0;
  text-align: left;
}

.booking-details p {
  margin: 0.5rem 0;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

/* Loading spinner */
.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
  
  .flight-timeline {
    flex-direction: column;
    gap: 2rem;
  }
  
  .flight-route {
    flex-direction: column;
    gap: 1rem;
  }
  
  .actions-section {
    flex-direction: column;
  }
  
  .details-grid, .checkin-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }
  
  .modal-content {
    padding: 1rem;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .category-options {
    grid-template-columns: 1fr;
  }
  
  .payment-options {
    grid-template-columns: 1fr;
  }
  
  .modal-footer {
    flex-direction: column;
  }
  
  .success-actions {
    flex-direction: column;
  }
}
</style>
