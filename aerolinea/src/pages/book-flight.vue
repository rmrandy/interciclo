<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { airlineApi } from '../utils/airlineApi'

const route = useRoute()
const router = useRouter()

// State
const loading = ref(false)
const error = ref('')
const success = ref(false)
const flight = ref<any>(null)
const availableSeats = ref<{[key: string]: string[]}>({})
const selectedCategory = ref('')
const selectedSeat = ref('')
const selectedPaymentMethod = ref('')
const bookingResult = ref<any>(null)

// Passenger form
const passengerForm = ref({
  firstName: '',
  lastName: '',
  documentType: '',
  documentNumber: '',
  email: '',
  phone: '',
  specialRequests: ''
})

// Payment methods
const paymentMethods = [
  { value: 'CREDIT_CARD', name: 'Tarjeta de Crédito', icon: '💳' },
  { value: 'DEBIT_CARD', name: 'Tarjeta de Débito', icon: '💳' },
  { value: 'BANK_TRANSFER', name: 'Transferencia Bancaria', icon: '🏦' }
]

// Computed properties
const flightNumber = computed(() => route.params.id)
const selectedPrice = computed(() => flight.value?.fares?.[selectedCategory.value] || 0)
const taxes = computed(() => selectedPrice.value * 0.15) // 15% taxes
const fees = computed(() => selectedPrice.value * 0.05) // 5% fees
const totalAmount = computed(() => selectedPrice.value + taxes.value + fees.value)

const canSubmit = computed(() => {
  return selectedSeat.value && 
         selectedCategory.value && 
         selectedPaymentMethod.value &&
         passengerForm.value.firstName &&
         passengerForm.value.lastName &&
         passengerForm.value.documentType &&
         passengerForm.value.documentNumber &&
         passengerForm.value.email &&
         passengerForm.value.phone
})

// Methods
const loadFlightDetails = async () => {
  try {
    loading.value = true
    const response = await airlineApi.getFlights()
    if (response.success) {
      const flightId = parseInt(route.params.id as string)
      flight.value = response.flights.find((f: any) => f.idFlight === flightId)
      if (flight.value) {
        await loadAvailableSeats()
      }
    }
  } catch (err) {
    error.value = 'Error cargando detalles del vuelo'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadAvailableSeats = async () => {
  try {
    const response = await airlineApi.getAvailableSeats(flight.value.idFlight)
    if (response.success) {
      // El backend devuelve seatsByCategory, no availableSeats
      availableSeats.value = response.seatsByCategory
      console.log('✅ Asientos cargados:', availableSeats.value)
    }
  } catch (err) {
    console.error('Error cargando asientos:', err)
    availableSeats.value = {}
  }
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

const selectPaymentMethod = (method: string) => {
  selectedPaymentMethod.value = method
}

const isSeatOccupied = (seat: string) => {
  // This would be checked against the backend
  return false
}

const submitBooking = async () => {
  if (!canSubmit.value) return
  
  try {
    loading.value = true
    error.value = ''
    
    const bookingData = {
      flightId: flight.value.idFlight,
      userId: 1, // TODO: Get from auth context
      seatNumber: selectedSeat.value,
      seatCategory: selectedCategory.value,
      fare: selectedPrice.value,
      passengerFirstName: passengerForm.value.firstName,
      passengerLastName: passengerForm.value.lastName,
      passengerDocumentType: passengerForm.value.documentType,
      passengerDocumentNumber: passengerForm.value.documentNumber,
      passengerEmail: passengerForm.value.email,
      passengerPhone: passengerForm.value.phone,
      specialRequests: passengerForm.value.specialRequests,
      paymentMethod: selectedPaymentMethod.value,
      taxes: taxes.value,
      fees: fees.value,
      totalAmount: totalAmount.value
    }
    
    const response = await airlineApi.createTicket(bookingData)
    if (response.success) {
      bookingResult.value = response
      success.value = true
    } else {
      error.value = response.error || 'Error creando la reserva'
    }
  } catch (err) {
    error.value = 'Error procesando la reserva'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.go(-1)
}

const goToTickets = () => {
  router.push('/my-bookings')
}

// Utility methods
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('es-ES', {
    weekday: 'short',
    month: 'short',
    day: 'numeric'
  })
}

const formatPrice = (price: number) => {
  return price.toFixed(2)
}

const calculateDuration = () => {
  if (!flight.value) return ''
  // Simple duration calculation - you might want to improve this
  return '6h 30m'
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

// Lifecycle
onMounted(() => {
  loadFlightDetails()
})
</script>

<template>
  <div class="book-flight-container">
    <div class="container mx-auto px-4 py-8">
      <!-- Header -->
      <div class="book-flight-header">
        <h1 class="page-title">Comprar Boleto</h1>
        <p class="page-subtitle">Reserva tu asiento para el vuelo {{ flightNumber }}</p>
      </div>

      <!-- Flight Summary -->
      <div v-if="flight" class="flight-summary">
        <h2 class="section-title">Resumen del Vuelo</h2>
        <div class="flight-card">
          <div class="flight-route">
            <div class="route-item">
              <div class="city">{{ flight.originCity }}</div>
              <div class="time">{{ flight.departureTime }}</div>
              <div class="date">{{ formatDate(flight.departureDate) }}</div>
            </div>
            <div class="flight-arrow">
              <span class="arrow">→</span>
              <div class="flight-number">{{ flight.flightNumber }}</div>
            </div>
            <div class="route-item">
              <div class="city">{{ flight.destinationCity }}</div>
              <div class="time">{{ flight.arrivalTime }}</div>
              <div class="date">{{ formatDate(flight.arrivalDate) }}</div>
            </div>
          </div>
          <div class="flight-details">
            <div class="detail-item">
              <span class="label">Duración:</span>
              <span class="value">{{ calculateDuration() }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Estado:</span>
              <span class="value status" :class="getStatusClass(flight.status)">
                {{ getStatusText(flight.status) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Passenger Information Form -->
      <div class="passenger-form-section">
        <h2 class="section-title">Información del Pasajero</h2>
        <form @submit.prevent="submitBooking" class="passenger-form">
          <div class="form-grid">
            <div class="form-group">
              <label for="firstName">Nombre *</label>
              <input
                id="firstName"
                v-model="passengerForm.firstName"
                type="text"
                required
                placeholder="Tu nombre"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label for="lastName">Apellido *</label>
              <input
                id="lastName"
                v-model="passengerForm.lastName"
                type="text"
                required
                placeholder="Tu apellido"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label for="documentType">Tipo de Documento *</label>
              <select
                id="documentType"
                v-model="passengerForm.documentType"
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
              <label for="documentNumber">Número de Documento *</label>
              <input
                id="documentNumber"
                v-model="passengerForm.documentNumber"
                type="text"
                required
                placeholder="Número de documento"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label for="email">Email *</label>
              <input
                id="email"
                v-model="passengerForm.email"
                type="email"
                required
                placeholder="tu@email.com"
                class="form-input"
              />
            </div>
            
            <div class="form-group">
              <label for="phone">Teléfono *</label>
              <input
                id="phone"
                v-model="passengerForm.phone"
                type="tel"
                required
                placeholder="+1234567890"
                class="form-input"
              />
            </div>
          </div>
          
          <div class="form-group full-width">
            <label for="specialRequests">Solicitudes Especiales</label>
            <textarea
              id="specialRequests"
              v-model="passengerForm.specialRequests"
              placeholder="Comidas especiales, asistencia, etc. (opcional)"
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>
        </form>
      </div>

      <!-- Seat Selection -->
      <div class="seat-selection-section">
        <h2 class="section-title">Selección de Asiento</h2>
        
        <!-- Seat Category Selection -->
        <div class="seat-category-selection">
          <h3 class="subsection-title">Categoría de Asiento</h3>
          <div class="category-options">
            <div
              v-for="(price, category) in flight.fares"
              :key="category"
              class="category-option"
              :class="{ 'selected': selectedCategory === category }"
              @click="selectCategory(category)"
            >
              <div class="category-info">
                <div class="category-name">{{ getCategoryName(category) }}</div>
                <div class="category-price">USD {{ formatPrice(price) }}</div>
              </div>
              <div class="category-icon">{{ getCategoryIcon(category) }}</div>
            </div>
          </div>
        </div>
        
        <!-- Seat Number Selection -->
        <div v-if="selectedCategory" class="seat-number-selection">
          <h3 class="subsection-title">Número de Asiento</h3>
          <div class="seat-grid">
            <div
              v-for="seat in availableSeats[selectedCategory] || []"
              :key="seat"
              class="seat-item"
              :class="{ 'selected': selectedSeat === seat, 'occupied': isSeatOccupied(seat) }"
              @click="selectSeat(seat)"
            >
              {{ seat }}
            </div>
          </div>
          <div class="seat-legend">
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
          </div>
        </div>
      </div>

      <!-- Payment Information -->
      <div v-if="selectedSeat && selectedPaymentMethod" class="payment-section">
        <h2 class="section-title">Información de Pago</h2>
        
        <div class="payment-method-selection">
          <h3 class="subsection-title">Método de Pago</h3>
          <div class="payment-options">
            <div
              v-for="method in paymentMethods"
              :key="method.value"
              class="payment-option"
              :class="{ 'selected': selectedPaymentMethod === method.value }"
              @click="selectPaymentMethod(method.value)"
            >
              <div class="payment-icon">{{ method.icon }}</div>
              <div class="payment-name">{{ method.name }}</div>
            </div>
          </div>
        </div>
        
        <!-- Price Breakdown -->
        <div class="price-breakdown">
          <h3 class="subsection-title">Desglose de Precios</h3>
          <div class="price-items">
            <div class="price-item">
              <span>Tarifa base ({{ getCategoryName(selectedCategory) }})</span>
              <span>USD {{ formatPrice(selectedPrice) }}</span>
            </div>
            <div class="price-item">
              <span>Impuestos</span>
              <span>USD {{ formatPrice(taxes) }}</span>
            </div>
            <div class="price-item">
              <span>Cargos</span>
              <span>USD {{ formatPrice(fees) }}</span>
            </div>
            <div class="price-item total">
              <span>Total a pagar</span>
              <span>USD {{ formatPrice(totalAmount) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Booking Summary -->
      <div v-if="selectedSeat && selectedPaymentMethod" class="booking-summary">
        <h2 class="section-title">Resumen de la Reserva</h2>
        <div class="summary-card">
          <div class="summary-item">
            <span class="label">Pasajero:</span>
            <span class="value">{{ passengerForm.firstName }} {{ passengerForm.lastName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">Vuelo:</span>
            <span class="value">{{ flight.flightNumber }}</span>
          </div>
          <div class="summary-item">
            <span class="label">Asiento:</span>
            <span class="value">{{ selectedSeat }} ({{ getCategoryName(selectedCategory) }})</span>
          </div>
          <div class="summary-item">
            <span class="label">Total:</span>
            <span class="value total-price">USD {{ formatPrice(totalAmount) }}</span>
          </div>
        </div>
        
        <div class="booking-actions">
          <button
            type="button"
            @click="goBack"
            class="btn-secondary"
          >
            ← Volver
          </button>
          <button
            type="submit"
            @click="submitBooking"
            :disabled="!canSubmit || loading"
            class="btn-primary"
          >
            <span v-if="loading" class="loading-spinner"></span>
            {{ loading ? 'Procesando...' : 'Confirmar Reserva' }}
          </button>
        </div>
      </div>

      <!-- Loading and Error States -->
      <div v-if="loading" class="loading-overlay">
        <div class="loading-content">
          <div class="loading-spinner large"></div>
          <p>Procesando tu reserva...</p>
        </div>
      </div>

      <div v-if="error" class="error-message">
        <p>{{ error }}</p>
      </div>

      <div v-if="success" class="success-message">
        <h3>¡Reserva Confirmada!</h3>
        <p>Tu boleto ha sido reservado exitosamente.</p>
        <p><strong>ID de Boleto:</strong> {{ bookingResult.ticketId }}</p>
        <p><strong>Total Pagado:</strong> USD {{ formatPrice(bookingResult.totalAmount) }}</p>
        <button @click="goToTickets" class="btn-primary">Ver Mis Boletos</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.book-flight-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
}

.book-flight-header {
  text-align: center;
  margin-bottom: 3rem;
  color: white;
}

.page-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.page-subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
}

.section-title {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1.5rem;
  text-align: center;
}

.subsection-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
}

/* Flight Summary */
.flight-summary {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.flight-card {
  background: #f8fafc;
  border-radius: 0.5rem;
  padding: 1.5rem;
}

.flight-route {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.route-item {
  text-align: center;
  flex: 1;
}

.city {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.time {
  font-size: 2rem;
  font-weight: 700;
  color: #059669;
  margin-bottom: 0.25rem;
}

.date {
  font-size: 0.875rem;
  color: #6b7280;
  text-transform: uppercase;
}

.flight-arrow {
  text-align: center;
  flex: 0 0 auto;
  margin: 0 2rem;
}

.arrow {
  font-size: 2rem;
  color: #3b82f6;
  display: block;
  margin-bottom: 0.5rem;
}

.flight-number {
  font-size: 1rem;
  font-weight: 600;
  color: #6b7280;
  background: #e5e7eb;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
}

.flight-details {
  display: flex;
  justify-content: center;
  gap: 2rem;
}

.detail-item {
  text-align: center;
}

.detail-item .label {
  font-size: 0.875rem;
  color: #6b7280;
  display: block;
  margin-bottom: 0.25rem;
}

.detail-item .value {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
}

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-scheduled { background: #dbeafe; color: #1d4ed8; }
.status-boarding { background: #fef3c7; color: #d97706; }
.status-departed { background: #dcfce7; color: #059669; }
.status-arrived { background: #dcfce7; color: #059669; }
.status-cancelled { background: #fee2e2; color: #dc2626; }
.status-delayed { background: #fef3c7; color: #d97706; }

/* Passenger Form */
.passenger-form-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.passenger-form {
  max-width: 800px;
  margin: 0 auto;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
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

/* Seat Selection */
.seat-selection-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.seat-category-selection {
  margin-bottom: 2rem;
}

.category-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.category-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
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

.category-info {
  flex: 1;
}

.category-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.category-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #059669;
}

.category-icon {
  font-size: 2rem;
  margin-left: 1rem;
}

.seat-number-selection {
  margin-bottom: 2rem;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 0.5rem;
  margin-bottom: 1rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
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
  gap: 2rem;
  margin-top: 1rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.legend-color {
  width: 20px;
  height: 20px;
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

/* Payment Section */
.payment-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.payment-method-selection {
  margin-bottom: 2rem;
}

.payment-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
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
  font-size: 2rem;
}

.payment-name {
  font-weight: 600;
  color: #1f2937;
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

/* Booking Summary */
.booking-summary {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.summary-card {
  background: #f8fafc;
  border-radius: 0.5rem;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e5e7eb;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item .label {
  font-weight: 600;
  color: #374151;
}

.summary-item .value {
  color: #1f2937;
}

.summary-item .total-price {
  font-weight: 700;
  font-size: 1.125rem;
  color: #059669;
}

.booking-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.btn-primary,
.btn-secondary {
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
  background: #3b82f6;
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

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-spinner.large {
  width: 40px;
  height: 40px;
  border-width: 4px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Loading and Messages */
.loading-overlay {
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
}

.loading-content {
  background: white;
  padding: 2rem;
  border-radius: 1rem;
  text-align: center;
}

.error-message,
.success-message {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
}

.success-message {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #059669;
}

.success-message h3 {
  color: #059669;
  margin-bottom: 1rem;
}

/* Responsive */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
  
  .flight-route {
    flex-direction: column;
    gap: 1rem;
  }
  
  .flight-arrow {
    margin: 1rem 0;
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
  
  .booking-actions {
    flex-direction: column;
  }
}
</style>
