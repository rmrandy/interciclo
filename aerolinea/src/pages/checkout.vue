<template>
  <div class="checkout-container">
    <!-- Contenido principal -->
    <div class="main-content">
      <!-- Loading state -->
      <div v-if="loading" class="loading-section">
        <div class="loading-container">
          <div class="loading-plane">✈️</div>
          <div class="loading-text">
            <h2>Procesando tu reserva...</h2>
            <p>Creando tus boletos de vuelo</p>
          </div>
        </div>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-section">
        <div class="error-icon">❌</div>
        <h2>Error en el proceso de pago</h2>
        <p>{{ error }}</p>
        <button @click="goToCart" class="back-btn">Volver al Carrito</button>
      </div>

      <!-- Success state -->
      <div v-else-if="bookingSuccess" class="success-section">
        <div class="success-icon">✅</div>
        <h2>¡Reserva Exitosa!</h2>
        <p>Tu reserva ha sido confirmada y procesada correctamente</p>
        
        <div class="booking-summary">
          <h3>Resumen de la Reserva</h3>
          <div class="summary-details">
            <div class="summary-item">
              <span>Número de reserva:</span>
              <span class="value">{{ bookingResult.bookingId }}</span>
            </div>
            <div class="summary-item">
              <span>Total pagado:</span>
              <span class="value">${{ bookingResult.totalAmount }}</span>
            </div>
            <div class="summary-item">
              <span>Fecha de reserva:</span>
              <span class="value">{{ formatDate(new Date()) }}</span>
            </div>
          </div>
        </div>

        <!-- Tickets generados -->
        <div class="booking-summary">
          <h3>Boletos generados</h3>
          <div v-for="t in bookingResult.tickets" :key="t.ticketId" class="summary-details">
            <div class="summary-item">
              <span>Ticket:</span>
              <span class="value">#{{ t.ticketId }}</span>
            </div>
            <div class="summary-item">
              <span>Código:</span>
              <span class="value">{{ t.reservationCode || '—' }}</span>
            </div>
            <div class="summary-item">
              <button class="primary-btn" @click="downloadTicketPdf(t.ticketId)">Descargar PDF</button>
            </div>
          </div>
        </div>

        <div class="success-actions">
          <button @click="goToMyBookings" class="primary-btn">Ver Mis Reservas</button>
          <button @click="goToFlights" class="secondary-btn">Reservar Otro Vuelo</button>
        </div>
      </div>

      <!-- Checkout form -->
      <div v-else class="checkout-content">
        <!-- Checkout header -->
        <div class="checkout-header-section">
          <h1>💳 Proceso de Pago</h1>
          <p class="checkout-subtitle">Confirma tus datos y completa el pago</p>
        </div>

        <!-- Checkout form -->
        <div class="checkout-form-section">
          <form @submit.prevent="processPayment" class="checkout-form">
            <!-- Passenger Information -->
            <div class="form-section">
              <h3>👤 Información del Pasajero Principal</h3>
              
              <div class="form-row">
                <div class="form-group">
                  <label for="firstName">Nombre *</label>
                  <input 
                    id="firstName"
                    v-model="passengerForm.firstName" 
                    type="text" 
                    required
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
                    class="form-input"
                  />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label for="documentType">Tipo de Documento *</label>
                  <select 
                    id="documentType"
                    v-model="passengerForm.documentType" 
                    required
                    class="form-select"
                  >
                    <option value="">Selecciona tipo</option>
                    <option value="ID_CARD">Cédula de Identidad</option>
                    <option value="PASSPORT">Pasaporte</option>
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
                    class="form-input"
                  />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label for="email">Email *</label>
                  <input 
                    id="email"
                    v-model="passengerForm.email" 
                    type="email" 
                    required
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
                    class="form-input"
                  />
                </div>
              </div>

              <div class="form-group">
                <label for="specialRequests">Solicitudes Especiales</label>
                <textarea 
                  id="specialRequests"
                  v-model="passengerForm.specialRequests" 
                  class="form-textarea"
                  placeholder="Asiento de ventana, comida especial, etc."
                  rows="3"
                ></textarea>
              </div>
            </div>

            <!-- Payment Information -->
            <div class="form-section">
              <h3>💳 Información de Pago</h3>
              
              <div class="form-row">
                <div class="form-group">
                  <CreditCardInput
                    v-model="paymentForm.cardNumber"
                    type="cardNumber"
                    label="Número de Tarjeta"
                    placeholder="1234 5678 9012 3456"
                    hint="Ingresa los 16 dígitos de tu tarjeta"
                  />
                </div>
                
                <div class="form-group">
                  <CreditCardInput
                    v-model="paymentForm.cardHolder"
                    type="cardholderName"
                    label="Titular de la Tarjeta"
                    placeholder="NOMBRE APELLIDO"
                    hint="Nombre completo como aparece en la tarjeta"
                  />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <CreditCardInput
                    v-model="paymentForm.expiryDate"
                    type="expiry"
                    label="Fecha de Vencimiento"
                    placeholder="MM/YY"
                    hint="Mes y año de vencimiento"
                  />
                </div>
                
                <div class="form-group">
                  <CreditCardInput
                    v-model="paymentForm.cvv"
                    type="cvv"
                    label="CVV"
                    placeholder="123"
                    hint="Código de seguridad de 3-4 dígitos"
                  />
                </div>
              </div>

              <div class="form-group">
                <label for="billingAddress">Dirección de Facturación</label>
                <textarea 
                  id="billingAddress"
                  v-model="paymentForm.billingAddress" 
                  class="form-textarea"
                  placeholder="Dirección completa para facturación"
                  rows="2"
                ></textarea>
              </div>
            </div>

            <!-- Terms and Conditions -->
            <div class="form-section">
              <div class="form-group checkbox-group">
                <label class="checkbox-label">
                  <input 
                    v-model="termsAccepted" 
                    type="checkbox" 
                    required
                    class="form-checkbox"
                  />
                  <span class="checkmark"></span>
                  Acepto los <a href="#" class="terms-link">términos y condiciones</a> y la <a href="#" class="terms-link">política de privacidad</a> *
                </label>
              </div>

              <div class="form-group checkbox-group">
                <label class="checkbox-label">
                  <input 
                    v-model="marketingAccepted" 
                    type="checkbox" 
                    class="form-checkbox"
                  />
                  <span class="checkmark"></span>
                  Acepto recibir información sobre ofertas y promociones
                </label>
              </div>
            </div>

            <!-- Submit button -->
            <div class="form-actions">
              <button 
                type="submit" 
                class="submit-btn"
                :disabled="!canSubmit || processing"
              >
                {{ processing ? '🔄 Procesando Pago...' : `💳 Pagar $${totalFares}` }}
              </button>
            </div>
          </form>
        </div>

        <!-- Order Summary -->
        <div class="order-summary">
          <h3>📋 Resumen del Pedido</h3>
          
          <div class="order-items">
            <div 
              v-for="(item, index) in checkoutItems" 
              :key="index" 
              class="order-item"
            >
              <div class="item-header">
                <h4>Vuelo {{ item.flight.flightNumber }}</h4>
                <span class="item-status">{{ getCategoryName(item.selectedCategory) }}</span>
              </div>
              
              <div class="item-details">
                <div class="route">
                  <span>{{ item.flight.originCity }}</span>
                  <span class="arrow">→</span>
                  <span>{{ item.flight.destinationCity }}</span>
                </div>
                <div class="seat-info">
                  <span>Asiento: {{ item.selectedSeat }}</span>
                  <span>Fecha: {{ formatDate(item.flight.departureDate) }}</span>
                </div>
              </div>
              
              <div class="item-price">
                <span class="base-price">${{ item.flight.fares?.[item.selectedCategory] || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="order-totals">
            <div class="total-item grand-total">
              <span>Total:</span>
              <span>${{ totalFares }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { airlineApi } from '../utils/airlineApi'
import CreditCardInput from '../components/CreditCardInput.vue'

const router = useRouter()

// State
const loading = ref(false)
const error = ref('')
const processing = ref(false)
const bookingSuccess = ref(false)
const bookingResult = ref<any>(null)
const checkoutItems = ref<any[]>([])
const termsAccepted = ref(false)
const marketingAccepted = ref(false)

// Form data
const passengerForm = ref({
  firstName: '',
  lastName: '',
  documentType: '',
  documentNumber: '',
  email: '',
  phone: '',
  specialRequests: ''
})

const paymentForm = ref({
  cardNumber: '',
  cardHolder: '',
  expiryDate: '',
  cvv: '',
  billingAddress: ''
})

// Computed properties
const totalFares = computed(() => {
  return checkoutItems.value.reduce((total, item) => {
    return total + (item.flight.fares?.[item.selectedCategory] || 0)
  }, 0)
})

// Propiedades computadas de impuestos removidas - ahora solo precio base

const canSubmit = computed(() => {
  return termsAccepted.value && 
         passengerForm.value.firstName &&
         passengerForm.value.lastName &&
         passengerForm.value.documentType &&
         passengerForm.value.documentNumber &&
         passengerForm.value.email &&
         passengerForm.value.phone &&
         paymentForm.value.cardNumber &&
         paymentForm.value.cardHolder &&
         paymentForm.value.expiryDate &&
         paymentForm.value.cvv &&
         checkoutItems.value.length > 0 &&
         checkoutItems.value.every(item => item.selectedCategory && item.quantity > 0)
})

// Methods
const loadCheckoutData = async () => {
  try {
    const checkoutData = localStorage.getItem('checkout_data')
    if (checkoutData) {
      const data = JSON.parse(checkoutData)
      checkoutItems.value = data.items
      
      // Cargar datos del usuario actual desde la base de datos
      await loadCurrentUserData()
    } else {
      // No checkout data, redirect to cart
      router.push('/cart')
    }
  } catch (err) {
    console.error('Error cargando datos de checkout:', err)
    error.value = 'Error cargando los datos del checkout'
  }
}

const loadCurrentUserData = async () => {
  try {
    // Obtener datos del usuario del localStorage
    const userData = localStorage.getItem('user')
    if (!userData) {
      console.log('No hay usuario autenticado, usando datos por defecto')
      return
    }
    
    const user = JSON.parse(userData)
    console.log('Usuario encontrado en localStorage:', user)
    
    // Llenar el formulario con los datos del usuario
    passengerForm.value = {
      firstName: user.firstName || user.name || '',
      lastName: user.lastName || user.surname || '',
      // Determinar tipo de documento basado en los datos disponibles
      documentType: determineDocumentType(user),
      // Usar el número de documento disponible
      documentNumber: user.documentNumber || user.passportNumber || user.cui || user.id || '',
      email: user.email || '',
      phone: user.phone || user.telephone || '',
      specialRequests: ''
    }
    
    console.log('✅ Datos del usuario cargados:', passengerForm.value)
  } catch (err) {
    console.error('Error cargando datos del usuario:', err)
    error.value = 'Error cargando datos del usuario'
  }
}

const determineDocumentType = (user: any): string => {
  // Si tiene passportNumber, usar PASSPORT
  if (user.passportNumber) {
    return 'PASSPORT'
  }
  // Si tiene cui, usar ID_CARD
  if (user.cui) {
    return 'ID_CARD'
  }
  // Si tiene documentType específico, usarlo
  if (user.documentType) {
    return user.documentType
  }
  // Por defecto, usar ID_CARD
  return 'ID_CARD'
}

const getDocumentPlaceholder = (): string => {
  switch (passengerForm.value.documentType) {
    case 'PASSPORT':
      return 'Ej: A12345678'
    case 'ID_CARD':
      return 'Ej: 1234567890123'
    case 'DRIVER_LICENSE':
      return 'Ej: 123456789'
    default:
      return 'Número de documento'
  }
}

const getDocumentHint = (): string => {
  switch (passengerForm.value.documentType) {
    case 'PASSPORT':
      return 'Ingresa el número de tu pasaporte'
    case 'ID_CARD':
      return 'Ingresa tu número de cédula de identidad'
    case 'DRIVER_LICENSE':
      return 'Ingresa tu número de licencia de conducir'
    default:
      return 'Selecciona primero el tipo de documento'
  }
}

const processPayment = async () => {
  if (!canSubmit.value) return
  
  try {
    processing.value = true
    error.value = ''
    
    // Simulate payment processing
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Create tickets for all items
    const tickets = []
    
    for (const item of checkoutItems.value) {
      // Generar fecha y hora de reserva
      const now = new Date()
      const bookingDate = now.toISOString().split('T')[0] // YYYY-MM-DD
      const bookingTime = now.toTimeString().slice(0, 5) // HH:MM
      
      const currentUser = JSON.parse(localStorage.getItem('user') || 'null')
      const ticketData = {
        flightId: item.flight.idFlight,
        userId: currentUser?.idUser || currentUser?.id || 1,
        seatNumber: `${item.flight.flightNumber.slice(-2)}${item.selectedCategory.slice(0, 1)}${Math.floor(Math.random() * 1000).toString().padStart(3, '0')}`, // Generar número automático de 6 caracteres
        seatCategory: item.selectedCategory,
        fare: item.flight.fares?.[item.selectedCategory] || 0,
        status: 'CONFIRMED', // Valor por defecto según DDL
        paymentMethod: 'CREDIT_CARD',
        totalAmount: item.flight.fares?.[item.selectedCategory] || 0, // Precio base sin impuestos
        passengerFirstName: passengerForm.value.firstName,
        passengerLastName: passengerForm.value.lastName,
        passengerDocumentType: passengerForm.value.documentType,
        passengerDocumentNumber: passengerForm.value.documentNumber,
        passengerEmail: passengerForm.value.email,
        passengerPhone: passengerForm.value.phone,
        specialRequests: passengerForm.value.specialRequests,
        paymentStatus: 'PENDING', // Valor por defecto según DDL
        quantity: item.quantity || 1, // Cantidad del carrito
        bookingDate: bookingDate, // Fecha de reserva
        bookingTime: bookingTime, // Hora de reserva
        refundAmount: 0, // Valor por defecto según DDL
        discountAmount: 0, // Valor por defecto según DDL
        discountCode: null // Campo opcional
      }
      
      const response = await airlineApi.createTicket(ticketData)
      
      if (response && (response.success || response.ticketId)) {
        tickets.push(response)
      } else {
        throw new Error(`Error creando boleto para vuelo ${item.flight.flightNumber}`)
      }
    }
    
    // All tickets created successfully
    bookingResult.value = {
      success: true,
      tickets: tickets,
              totalAmount: totalFares.value,
      bookingId: `BK${Date.now()}`
    }
    
    // Clear cart and checkout data
    localStorage.removeItem('flight_cart')
    localStorage.removeItem('checkout_data')
    
    bookingSuccess.value = true
 
  } catch (err) {
    error.value = 'Error procesando el pago: ' + (err as Error).message
    console.error(err)
  } finally {
    processing.value = false
  }
}

const goToCart = () => {
  router.push('/cart')
}

const goToMyBookings = () => {
  router.push('/my-bookings')
}

const goToFlights = () => {
  router.push('/flights')
}

const downloadTicketPdf = async (ticketId: number) => {
  try {
    const blob = await airlineApi.downloadTicketPdf(ticketId)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `Ticket-${ticketId}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    alert('No se pudo descargar el PDF')
  }
}

// Utility functions
const getCategoryName = (category: string) => {
  const categoryMap: { [key: string]: string } = {
    'FIRST_CLASS': 'Primera Clase',
    'BUSINESS': 'Ejecutiva',
    'ECONOMY': 'Económica'
  }
  return categoryMap[category] || category
}

const formatDate = (date: Date | string) => {
  if (!date) return 'N/A'
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return dateObj.toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// Funciones de impuestos removidas - ahora solo precio base

// Lifecycle
onMounted(() => {
  loadCheckoutData()
})
</script>

<style scoped>
.checkout-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.checkout-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
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
  color: #2d3748;
  margin: 0;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-link {
  text-decoration: none;
  color: #4a5568;
  font-weight: 500;
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #667eea;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.loading-section,
.error-section,
.success-section {
  text-align: center;
  padding: 4rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem;
  margin: 2rem 0;
}

.loading-container {
  max-width: 400px;
  margin: 0 auto;
}

.loading-plane {
  font-size: 4rem;
  animation: bounce 2s infinite;
}

.loading-text h2,
.error-section h2,
.success-section h2 {
  color: white;
  margin: 1rem 0 0.5rem 0;
}

.loading-text p,
.error-section p,
.success-section p {
  color: rgba(255, 255, 255, 0.8);
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.success-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  color: #48bb78;
}

.back-btn {
  background: #e53e3e;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease;
  margin-top: 1rem;
}

.back-btn:hover {
  background: #c53030;
}

.booking-summary {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem;
  padding: 1.5rem;
  margin: 2rem 0;
  text-align: left;
}

.booking-summary h3 {
  margin: 0 0 1rem 0;
  color: white;
}

.summary-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.summary-item .value {
  font-weight: 600;
  color: #48bb78;
}

.success-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
}

.primary-btn,
.secondary-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
}

.primary-btn {
  background: #48bb78;
  color: white;
}

.primary-btn:hover {
  background: #38a169;
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.checkout-content {
  color: white;
}

.checkout-header-section {
  text-align: center;
  margin-bottom: 3rem;
}

.checkout-header-section h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.checkout-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

.checkout-form-section {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 2rem;
  margin-bottom: 2rem;
}

.checkout-form {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem;
  padding: 2rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.form-section {
  margin-bottom: 2rem;
}

.form-section h3 {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  color: #48bb78;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #48bb78;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.form-select option {
  background: #2d3748;
  color: white;
}

.form-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
  display: block;
  font-style: italic;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.checkbox-group {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  cursor: pointer;
  font-size: 0.9rem;
  line-height: 1.4;
}

.form-checkbox {
  margin: 0;
  width: 18px;
  height: 18px;
  accent-color: #48bb78;
}

.terms-link {
  color: #48bb78;
  text-decoration: underline;
}

.terms-link:hover {
  color: #38a169;
}

.form-actions {
  text-align: center;
  margin-top: 2rem;
}

.submit-btn {
  background: #48bb78;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 0.5rem;
  font-weight: 600;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
  min-width: 200px;
}

.submit-btn:hover:not(:disabled) {
  background: #38a169;
}

.submit-btn:disabled {
  background: #a0aec0;
  cursor: not-allowed;
}

.order-summary {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem;
  padding: 1.5rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  height: fit-content;
  position: sticky;
  top: 100px;
}

.order-summary h3 {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  text-align: center;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
}

.order-item {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.5rem;
  padding: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.item-header h4 {
  margin: 0;
  font-size: 1rem;
  color: #48bb78;
}

.item-status {
  background: rgba(72, 187, 120, 0.2);
  color: #68d391;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.item-details {
  margin-bottom: 0.75rem;
}

.route {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.arrow {
  color: #48bb78;
  font-weight: bold;
}

.seat-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  opacity: 0.8;
}

.item-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
}

.base-price {
  font-weight: 600;
  color: #48bb78;
}

.taxes,
.fees {
  opacity: 0.7;
}

.order-totals {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 1rem;
}

.total-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.total-item.grand-total {
  border-bottom: none;
  border-top: 2px solid rgba(255, 255, 255, 0.3);
  padding-top: 1rem;
  font-weight: 600;
  font-size: 1.1rem;
  color: #48bb78;
}

/* Responsive design */
@media (max-width: 768px) {
  .checkout-form-section {
    grid-template-columns: 1fr;
  }
  
  .order-summary {
    position: static;
    margin-top: 2rem;
  }
  
  .header-content {
    padding: 1rem;
  }
  
  .main-content {
    padding: 1rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .success-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>
