<template>
  <div class="cart-container">
    <!-- Header con navegación -->
    <header class="cart-header">
      <div class="header-content">
        <div class="logo-section">
          <div class="logo">✈️</div>
          <h1>AeroLinea</h1>
        </div>
        <nav class="nav-links">
          <a href="/" class="nav-link">Inicio</a>
          <a href="/flights" class="nav-link">Vuelos</a>
          <a href="/profile" class="nav-link">Mi Cuenta</a>
        </nav>
      </div>
    </header>

    <!-- Contenido principal -->
    <div class="main-content">
      <!-- Loading state -->
      <div v-if="loading" class="loading-section">
        <div class="loading-container">
          <div class="loading-plane">✈️</div>
          <div class="loading-text">
            <h2>Cargando carrito...</h2>
            <p>Preparando tu selección de vuelos</p>
          </div>
        </div>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-section">
        <div class="error-icon">❌</div>
        <h2>Error al cargar el carrito</h2>
        <p>{{ error }}</p>
        <button @click="goToFlights" class="back-btn">Ver Vuelos</button>
      </div>

      <!-- Cart content -->
      <div v-else class="cart-content">
        <!-- Cart header -->
        <div class="cart-header-section">
          <h1>🛒 Mi Carrito</h1>
          <p class="cart-subtitle">Revisa y confirma tus selecciones de vuelos</p>
        </div>

        <!-- Empty cart state -->
        <div v-if="cartItems.length === 0" class="empty-cart">
          <div class="empty-cart-icon">🛍️</div>
          <h2>Tu carrito está vacío</h2>
          <p>No tienes vuelos agregados al carrito</p>
          <button @click="goToFlights" class="primary-btn">Explorar Vuelos</button>
        </div>

        <!-- Cart items -->
        <div v-else class="cart-items-section">
          <div class="cart-items">
            <div 
              v-for="(item, index) in cartItems" 
              :key="index" 
              class="cart-item"
            >
              <!-- Flight info -->
              <div class="flight-info">
                <div class="flight-header">
                  <h3>Vuelo {{ item.flight.flightNumber }}</h3>
                  <div class="flight-status" :class="getStatusClass(item.flight.status)">
                    {{ getStatusText(item.flight.status) }}
                  </div>
                </div>
                
                <div class="flight-route">
                  <div class="route-item">
                    <div class="city">{{ item.flight.originCity }}</div>
                    <div class="airport-code">{{ getCityCode(item.flight.originCity) }}</div>
                  </div>
                  <div class="route-arrow">→</div>
                  <div class="route-item">
                    <div class="city">{{ item.flight.destinationCity }}</div>
                    <div class="airport-code">{{ getCityCode(item.flight.destinationCity) }}</div>
                  </div>
                </div>

                <div class="flight-details">
                  <div class="detail-item">
                    <span class="label">Fecha:</span>
                    <span class="value">{{ formatDate(item.flight.departureDate) }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Hora:</span>
                    <span class="value">{{ formatTime(item.flight.departureTime) }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Duración:</span>
                    <span class="value">{{ calculateDuration(item.flight) }}</span>
                  </div>
                </div>
              </div>

              <!-- Inventory selection -->
              <div class="inventory-selection">
                <h4>Selección de Asientos</h4>
                
                <!-- Category selection -->
                <div class="category-selection">
                  <label class="category-label">Categoría:</label>
                  <select 
                    v-model="item.selectedCategory" 
                    @change="updateInventoryAvailability(item, index)"
                    class="category-select"
                    :disabled="item.processing"
                  >
                    <option value="">Selecciona categoría</option>
                    <option 
                      v-for="(inventory, category) in item.inventoryByCategory" 
                      :key="category"
                      :value="category"
                      :disabled="inventory.availableSeats <= 0"
                    >
                      {{ getCategoryNameLocal(String(category)) }} - {{ inventory.availableSeats }} disponibles
                    </option>
                  </select>
                </div>

                <!-- Quantity selection -->
                <div v-if="item.selectedCategory && item.inventoryByCategory[item.selectedCategory]?.availableSeats > 0" class="quantity-selection">
                  <label class="quantity-label">Cantidad de asientos:</label>
                  <div class="quantity-controls">
                    <button 
                      @click="decreaseQuantity(index)" 
                      class="quantity-btn"
                      :disabled="item.quantity <= 1 || item.processing"
                    >
                      -
                    </button>
                    <span class="quantity-display">{{ item.quantity }}</span>
                    <button 
                      @click="increaseQuantity(index)" 
                      class="quantity-btn"
                      :disabled="item.quantity >= item.inventoryByCategory[item.selectedCategory]?.availableSeats || item.processing"
                    >
                      +
                    </button>
                  </div>
                  <div class="quantity-info">
                    <span class="available-info">
                      {{ item.inventoryByCategory[item.selectedCategory]?.availableSeats }} asientos disponibles
                    </span>
                  </div>
                </div>

                <!-- Price info -->
                <div v-if="item.selectedCategory && item.quantity > 0" class="price-info">
                  <div class="price-breakdown">
                    <div class="price-item">
                      <span>Tarifa base (x{{ item.quantity }}):</span>
                      <span class="amount">${{ (item.flight.fares?.[item.selectedCategory] || 0) * item.quantity }}</span>
                    </div>
                    <div class="price-item">
                      <span>Impuestos (15%):</span>
                      <span class="amount">${{ calculateTaxes((item.flight.fares?.[item.selectedCategory] || 0) * item.quantity) }}</span>
                    </div>
                    <div class="price-item">
                      <span>Cargos (5%):</span>
                      <span class="amount">${{ calculateFees((item.flight.fares?.[item.selectedCategory] || 0) * item.quantity) }}</span>
                    </div>
                    <div class="price-item total">
                      <span>Total:</span>
                      <span class="amount">${{ calculateTotal((item.flight.fares?.[item.selectedCategory] || 0) * item.quantity) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Actions -->
              <div class="item-actions">
                <button 
                  @click="removeFromCartLocal(index)" 
                  class="remove-btn"
                  :disabled="item.processing"
                >
                  🗑️ Eliminar
                </button>
                
                <button 
                  @click="updateItem(index)" 
                  class="update-btn"
                  :disabled="!item.selectedCategory || item.quantity <= 0 || item.processing"
                >
                  {{ item.processing ? '🔄 Actualizando...' : '✅ Confirmar Selección' }}
                </button>
              </div>
            </div>
          </div>

          <!-- Cart summary -->
          <div class="cart-summary">
            <h3>Resumen del Carrito</h3>
            
            <div class="summary-items">
              <div class="summary-item">
                <span>Vuelos en carrito:</span>
                <span>{{ cartItems.length }}</span>
              </div>
              <div class="summary-item">
                <span>Total asientos:</span>
                <span>{{ totalSeats }}</span>
              </div>
              <div class="summary-item">
                <span>Total tarifas:</span>
                <span>${{ totalFares }}</span>
              </div>
              <div class="summary-item">
                <span>Total impuestos:</span>
                <span>${{ totalTaxes }}</span>
              </div>
              <div class="summary-item">
                <span>Total cargos:</span>
                <span>${{ totalFees }}</span>
              </div>
              <div class="summary-item total">
                <span>Total general:</span>
                <span>${{ totalAmount }}</span>
              </div>
            </div>

            <div class="summary-actions">
              <button 
                @click="goToFlights" 
                class="secondary-btn"
              >
                Agregar Más Vuelos
              </button>
              
              <button 
                @click="proceedToCheckout" 
                class="primary-btn checkout-btn"
                :disabled="!canProceedToCheckout"
              >
                {{ processingCheckout ? '🔄 Procesando...' : '💳 Proceder al Pago' }}
              </button>
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
import { 
  getCart, 
  saveCart, 
  removeFromCart, 
  updateCartItem, 
  getCartSummary,
  isCartReadyForCheckout,
  getCategoryName,
  formatPrice
} from '../utils/cart-utils'

const router = useRouter()

// State
const loading = ref(false)
const error = ref('')
const cartItems = ref<any[]>([])
const processingCheckout = ref(false)

// Computed properties
const totalSeats = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + (item.quantity || 0)
  }, 0)
})

const totalFares = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + ((item.flight.fares?.[item.selectedCategory] || 0) * (item.quantity || 0))
  }, 0)
})

const totalTaxes = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + calculateTaxes((item.flight.fares?.[item.selectedCategory] || 0) * (item.quantity || 0))
  }, 0)
})

const totalFees = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + calculateFees((item.flight.fares?.[item.selectedCategory] || 0) * (item.quantity || 0))
  }, 0)
})

const totalAmount = computed(() => {
  return totalFares.value + totalTaxes.value + totalFees.value
})

const canProceedToCheckout = computed(() => {
  return cartItems.value.length > 0 && 
         cartItems.value.every(item => item.selectedCategory && item.quantity > 0)
})

// Methods
const loadCart = () => {
  try {
    const savedCart = localStorage.getItem('flight_cart')
    if (savedCart) {
      cartItems.value = JSON.parse(savedCart)
      // Initialize quantity for each item if not set
      cartItems.value.forEach((item, index) => {
        if (!item.quantity) {
          item.quantity = 1
        }
        loadInventoryForFlight(item, index)
      })
    }
  } catch (err) {
    console.error('Error cargando carrito:', err)
    error.value = 'Error cargando el carrito'
  }
}

const loadInventoryForFlight = async (item: any, index: number) => {
  try {
    const response = await airlineApi.getFlightInventory(item.flight.idFlight)
    if (response.success && response.inventory) {
      cartItems.value[index].inventoryByCategory = response.inventory
    }
  } catch (err) {
    console.error('Error cargando inventario:', err)
    // Fallback: create basic inventory structure
    cartItems.value[index].inventoryByCategory = {
      'ECONOMY': { availableSeats: 150, totalSeats: 150, reservedSeats: 0, soldSeats: 0 },
      'BUSINESS': { availableSeats: 30, totalSeats: 30, reservedSeats: 0, soldSeats: 0 },
      'FIRST_CLASS': { availableSeats: 20, totalSeats: 20, reservedSeats: 0, soldSeats: 0 }
    }
  }
}

const updateInventoryAvailability = async (item: any, index: number) => {
  // Reset quantity when category changes
  item.quantity = 1
  
  // Reload inventory for the selected category
  await loadInventoryForFlight(item, index)
}

const increaseQuantity = (index: number) => {
  const item = cartItems.value[index]
  const maxAvailable = item.inventoryByCategory[item.selectedCategory]?.availableSeats || 0
  if (item.quantity < maxAvailable) {
    item.quantity++
  }
}

const decreaseQuantity = (index: number) => {
  const item = cartItems.value[index]
  if (item.quantity > 1) {
    item.quantity--
  }
}

const updateItem = async (index: number) => {
  const item = cartItems.value[index]
  if (!item.selectedCategory || item.quantity <= 0) return
  
  try {
    item.processing = true
    
    // Validate inventory availability
    const inventory = item.inventoryByCategory[item.selectedCategory]
    if (!inventory || inventory.availableSeats < item.quantity) {
      throw new Error('No hay suficientes asientos disponibles')
    }
    
    // Update the cart item
    const updatedItem = {
      ...item,
      processing: false
    }
    
    cartItems.value[index] = updatedItem
    saveCartLocal()
    
  } catch (err) {
    console.error('Error actualizando item:', err)
    error.value = 'Error actualizando la selección: ' + (err as Error).message
  } finally {
    item.processing = false
  }
}

const removeFromCartLocal = (index: number) => {
  cartItems.value.splice(index, 1)
  saveCartLocal()
}

const saveCartLocal = () => {
  localStorage.setItem('flight_cart', JSON.stringify(cartItems.value))
}

const proceedToCheckout = async () => {
  if (!canProceedToCheckout.value) return
  
  try {
    processingCheckout.value = true
    
    // Save cart data for checkout
    const checkoutData = {
      items: cartItems.value,
      totalAmount: totalAmount.value,
      totalFares: totalFares.value,
      totalTaxes: totalTaxes.value,
      totalFees: totalFees.value,
      totalSeats: totalSeats.value
    }
    
    localStorage.setItem('checkout_data', JSON.stringify(checkoutData))
    
    // Navigate to checkout page
    router.push('/checkout')
    
  } catch (err) {
    console.error('Error procediendo al checkout:', err)
    error.value = 'Error procesando el checkout'
  } finally {
    processingCheckout.value = false
  }
}

const goToFlights = () => {
  router.push('/flights')
}

// Utility functions
const getStatusClass = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'SCHEDULED': 'status-scheduled',
    'BOARDING': 'status-boarding',
    'DEPARTED': 'status-departed',
    'ARRIVED': 'status-arrived',
    'CANCELLED': 'status-cancelled',
    'DELAYED': 'status-delayed'
  }
  return statusMap[status] || 'status-unknown'
}

const getStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'SCHEDULED': 'Programado',
    'BOARDING': 'Abordando',
    'DEPARTED': 'Partido',
    'ARRIVED': 'Llegado',
    'CANCELLED': 'Cancelado',
    'DELAYED': 'Retrasado'
  }
  return statusMap[status] || status
}

const getCategoryNameLocal = (category: string) => {
  const categoryMap: { [key: string]: string } = {
    'FIRST_CLASS': 'Primera Clase',
    'BUSINESS': 'Ejecutiva',
    'ECONOMY': 'Económica'
  }
  return categoryMap[category] || category
}

const getCityCode = (cityName: string) => {
  if (!cityName) return '---'
  return cityName.split(' ').map(word => word.charAt(0)).join('').toUpperCase().substring(0, 3)
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatTime = (timeString: string) => {
  if (!timeString) return 'N/A'
  return timeString
}

const calculateDuration = (flight: any) => {
  if (!flight.departureTime || !flight.arrivalTime) return 'N/A'
  
  const departure = new Date(`2000-01-01T${flight.departureTime}`)
  const arrival = new Date(`2000-01-01T${flight.arrivalTime}`)
  
  const diffMs = arrival.getTime() - departure.getTime()
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  return `${diffHours}h ${diffMinutes}m`
}

const calculateTaxes = (basePrice: number) => {
  return Math.round(basePrice * 0.15 * 100) / 100
}

const calculateFees = (basePrice: number) => {
  return Math.round(basePrice * 0.05 * 100) / 100
}

const calculateTotal = (basePrice: number) => {
  return Math.round(basePrice * 1.2 * 100) / 100
}

// Lifecycle
onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.cart-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
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
  gap: 0.75rem;
}

.logo {
  font-size: 2.5rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.logo-section h1 {
  font-size: 1.75rem;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.nav-links {
  display: flex;
  gap: 2.5rem;
}

.nav-link {
  text-decoration: none;
  color: #4a5568;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  padding: 0.5rem 0;
}

.nav-link:hover {
  color: #667eea;
  transform: translateY(-2px);
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

.nav-link:hover::after {
  width: 100%;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.loading-section {
  text-align: center;
  padding: 6rem 2rem;
}

.loading-container {
  max-width: 400px;
  margin: 0 auto;
}

.loading-plane {
  font-size: 5rem;
  animation: float 3s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.loading-text h2 {
  color: white;
  margin: 1.5rem 0 0.75rem 0;
  font-size: 1.75rem;
  font-weight: 700;
}

.loading-text p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.error-section {
  text-align: center;
  padding: 4rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1.5rem;
  margin: 2rem 0;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.error-section h2 {
  color: white;
  margin-bottom: 1rem;
  font-size: 1.75rem;
  font-weight: 700;
}

.error-section p {
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.back-btn {
  background: linear-gradient(135deg, #e53e3e, #c53030);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
  box-shadow: 0 4px 15px rgba(229, 62, 62, 0.3);
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(229, 62, 62, 0.4);
}

.cart-content {
  color: white;
}

.cart-header-section {
  text-align: center;
  margin-bottom: 4rem;
}

.cart-header-section h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
  font-weight: 800;
  background: linear-gradient(135deg, #ffffff, #f7fafc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.cart-subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
  font-weight: 500;
}

.empty-cart {
  text-align: center;
  padding: 6rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.empty-cart-icon {
  font-size: 5rem;
  margin-bottom: 2rem;
  animation: bounce 2s infinite;
}

.empty-cart h2 {
  margin-bottom: 1rem;
  font-size: 2rem;
  font-weight: 700;
}

.empty-cart p {
  margin-bottom: 3rem;
  opacity: 0.9;
  font-size: 1.2rem;
}

.primary-btn {
  background: linear-gradient(135deg, #48bb78, #38a169);
  color: white;
  border: none;
  padding: 1rem 2.5rem;
  border-radius: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
  font-size: 1.1rem;
  box-shadow: 0 4px 15px rgba(72, 187, 120, 0.3);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(72, 187, 120, 0.4);
}

.primary-btn:disabled {
  background: #a0aec0;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.cart-items-section {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 2.5rem;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.cart-item {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1.5rem;
  padding: 2rem;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.cart-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.flight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.flight-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff, #f7fafc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.flight-status {
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-scheduled { background: linear-gradient(135deg, #4299e1, #3182ce); }
.status-boarding { background: linear-gradient(135deg, #ed8936, #dd6b20); }
.status-departed { background: linear-gradient(135deg, #38a169, #2f855a); }
.status-arrived { background: linear-gradient(135deg, #48bb78, #38a169); }
.status-cancelled { background: linear-gradient(135deg, #e53e3e, #c53030); }
.status-delayed { background: linear-gradient(135deg, #ed8936, #dd6b20); }
.status-unknown { background: linear-gradient(135deg, #a0aec0, #718096); }

.flight-route {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 1rem;
}

.route-item {
  text-align: center;
  flex: 1;
}

.city {
  font-weight: 700;
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
}

.airport-code {
  font-size: 0.875rem;
  opacity: 0.8;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.5rem 1rem;
  border-radius: 0.75rem;
  font-weight: 600;
  letter-spacing: 1px;
}

.route-arrow {
  font-size: 2rem;
  opacity: 0.7;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.7; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

.flight-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 1rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  text-align: center;
}

.label {
  font-size: 0.875rem;
  opacity: 0.7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.value {
  font-weight: 600;
  font-size: 1.1rem;
}

.inventory-selection {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 2rem;
  margin-bottom: 2rem;
}

.inventory-selection h4 {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  text-align: center;
  background: linear-gradient(135deg, #ffffff, #f7fafc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.category-selection,
.quantity-selection {
  margin-bottom: 1.5rem;
}

.category-label,
.quantity-label {
  display: block;
  margin-bottom: 0.75rem;
  font-weight: 600;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  opacity: 0.9;
}

.category-select {
  width: 100%;
  padding: 1rem;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.category-select:focus {
  outline: none;
  border-color: #48bb78;
  box-shadow: 0 0 0 4px rgba(72, 187, 120, 0.1);
  transform: translateY(-2px);
}

.category-select option {
  background: #2d3748;
  color: white;
  padding: 0.5rem;
}

.quantity-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.quantity-btn {
  width: 50px;
  height: 50px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 1rem;
  font-size: 1.5rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.quantity-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.1);
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.quantity-display {
  font-size: 1.5rem;
  font-weight: 700;
  min-width: 60px;
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.75rem 1rem;
  border-radius: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.quantity-info {
  text-align: center;
}

.available-info {
  font-size: 0.875rem;
  opacity: 0.8;
  color: #68d391;
  font-weight: 500;
}

.price-info {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem;
  padding: 1.5rem;
  margin-top: 1.5rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.price-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.price-item.total {
  border-bottom: none;
  border-top: 2px solid rgba(255, 255, 255, 0.3);
  padding-top: 1rem;
  font-weight: 700;
  font-size: 1.2rem;
}

.amount {
  font-weight: 600;
  color: #48bb78;
  font-size: 1.1rem;
}

.item-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.remove-btn,
.update-btn {
  padding: 1rem 1.5rem;
  border: none;
  border-radius: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.remove-btn {
  background: linear-gradient(135deg, rgba(229, 62, 62, 0.2), rgba(229, 62, 62, 0.3));
  color: #fc8181;
  border: 2px solid rgba(229, 62, 62, 0.3);
}

.remove-btn:hover {
  background: linear-gradient(135deg, rgba(229, 62, 62, 0.3), rgba(229, 62, 62, 0.4));
  transform: translateY(-2px);
}

.update-btn {
  background: linear-gradient(135deg, rgba(72, 187, 120, 0.2), rgba(72, 187, 120, 0.3));
  color: #68d391;
  border: 2px solid rgba(72, 187, 120, 0.3);
}

.update-btn:hover {
  background: linear-gradient(135deg, rgba(72, 187, 120, 0.3), rgba(72, 187, 120, 0.4));
  transform: translateY(-2px);
}

.remove-btn:disabled,
.update-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.cart-summary {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1.5rem;
  padding: 2rem;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  height: fit-content;
  position: sticky;
  top: 120px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.cart-summary h3 {
  margin: 0 0 2rem 0;
  font-size: 1.5rem;
  text-align: center;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff, #f7fafc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.summary-items {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  margin-bottom: 2.5rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  font-weight: 500;
}

.summary-item.total {
  border-bottom: none;
  border-top: 2px solid rgba(255, 255, 255, 0.3);
  padding-top: 1.25rem;
  font-weight: 700;
  font-size: 1.25rem;
}

.summary-actions {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  padding: 1rem 1.5rem;
  border-radius: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  text-align: center;
  backdrop-filter: blur(10px);
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.checkout-btn {
  background: linear-gradient(135deg, #48bb78, #38a169);
  font-size: 1.2rem;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 4px 15px rgba(72, 187, 120, 0.3);
}

.checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(72, 187, 120, 0.4);
}

/* Responsive design */
@media (max-width: 768px) {
  .cart-items-section {
    grid-template-columns: 1fr;
  }
  
  .cart-summary {
    position: static;
    margin-top: 2rem;
  }
  
  .header-content {
    padding: 1rem;
  }
  
  .main-content {
    padding: 1rem;
  }
  
  .flight-details {
    grid-template-columns: 1fr;
  }
  
  .item-actions {
    flex-direction: column;
  }
  
  .cart-header-section h1 {
    font-size: 2.5rem;
  }
  
  .cart-item {
    padding: 1.5rem;
  }
}
</style>
