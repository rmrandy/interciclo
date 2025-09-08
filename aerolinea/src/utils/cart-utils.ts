// Utilidades para el manejo del carrito de vuelos
export interface CartItem {
  flight: any
  selectedCategory: string
  selectedSeat: string
  availableSeats: { [key: string]: string[] }
  processing: boolean
}

export interface CartSummary {
  items: CartItem[]
  totalAmount: number
  totalFares: number
  totalTaxes: number
  totalFees: number
}

// Clave para almacenar el carrito en localStorage
const CART_STORAGE_KEY = 'flight_cart'

/**
 * Obtiene el carrito actual desde localStorage
 */
export const getCart = (): CartItem[] => {
  try {
    const cartData = localStorage.getItem(CART_STORAGE_KEY)
    return cartData ? JSON.parse(cartData) : []
  } catch (error) {
    console.error('Error obteniendo carrito:', error)
    return []
  }
}

/**
 * Guarda el carrito en localStorage
 */
export const saveCart = (cart: CartItem[]): void => {
  try {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart))
  } catch (error) {
    console.error('Error guardando carrito:', error)
  }
}

/**
 * Agrega un vuelo al carrito
 */
export const addToCart = (flight: any): boolean => {
  try {
    const cart = getCart()
    
    // Verificar si el vuelo ya está en el carrito
    const existingItem = cart.find(item => item.flight.idFlight === flight.idFlight)
    if (existingItem) {
      return false // Vuelo ya existe en el carrito
    }
    
    // Crear nuevo item del carrito
    const cartItem: CartItem = {
      flight,
      selectedCategory: '',
      selectedSeat: '',
      availableSeats: {},
      processing: false
    }
    
    cart.push(cartItem)
    saveCart(cart)
    return true
    
  } catch (error) {
    console.error('Error agregando al carrito:', error)
    return false
  }
}

/**
 * Remueve un vuelo del carrito
 */
export const removeFromCart = (flightId: number): boolean => {
  try {
    const cart = getCart()
    const updatedCart = cart.filter(item => item.flight.idFlight !== flightId)
    saveCart(updatedCart)
    return true
  } catch (error) {
    console.error('Error removiendo del carrito:', error)
    return false
  }
}

/**
 * Actualiza un item del carrito
 */
export const updateCartItem = (flightId: number, updates: Partial<CartItem>): boolean => {
  try {
    const cart = getCart()
    const itemIndex = cart.findIndex(item => item.flight.idFlight === flightId)
    
    if (itemIndex === -1) {
      return false // Item no encontrado
    }
    
    cart[itemIndex] = { ...cart[itemIndex], ...updates }
    saveCart(cart)
    return true
    
  } catch (error) {
    console.error('Error actualizando item del carrito:', error)
    return false
  }
}

/**
 * Limpia el carrito completamente
 */
export const clearCart = (): void => {
  try {
    localStorage.removeItem(CART_STORAGE_KEY)
  } catch (error) {
    console.error('Error limpiando carrito:', error)
  }
}

/**
 * Obtiene el resumen del carrito con cálculos
 */
export const getCartSummary = (): CartSummary => {
  const cart = getCart()
  
  const totalFares = cart.reduce((total, item) => {
    return total + (item.flight.fares?.[item.selectedCategory] || 0)
  }, 0)
  
  const totalTaxes = totalFares * 0.15 // 15% impuestos
  const totalFees = totalFares * 0.05 // 5% cargos
  const totalAmount = totalFares + totalTaxes + totalFees
  
  return {
    items: cart,
    totalAmount: Math.round(totalAmount * 100) / 100,
    totalFares: Math.round(totalFares * 100) / 100,
    totalTaxes: Math.round(totalTaxes * 100) / 100,
    totalFees: Math.round(totalFees * 100) / 100
  }
}

/**
 * Verifica si el carrito está listo para checkout
 */
export const isCartReadyForCheckout = (): boolean => {
  const cart = getCart()
  return cart.length > 0 && cart.every(item => 
    item.selectedCategory && item.selectedSeat
  )
}

/**
 * Obtiene el número de items en el carrito
 */
export const getCartItemCount = (): number => {
  return getCart().length
}

/**
 * Verifica si un vuelo específico está en el carrito
 */
export const isFlightInCart = (flightId: number): boolean => {
  const cart = getCart()
  return cart.some(item => item.flight.idFlight === flightId)
}

/**
 * Obtiene un item específico del carrito
 */
export const getCartItem = (flightId: number): CartItem | null => {
  const cart = getCart()
  return cart.find(item => item.flight.idFlight === flightId) || null
}

/**
 * Calcula el precio total de un item específico
 */
export const calculateItemTotal = (item: CartItem): number => {
  const basePrice = item.flight.fares?.[item.selectedCategory] || 0
  const taxes = basePrice * 0.15
  const fees = basePrice * 0.05
  return Math.round((basePrice + taxes + fees) * 100) / 100
}

/**
 * Formatea el precio para mostrar
 */
export const formatPrice = (price: number): string => {
  return `$${price.toFixed(2)}`
}

/**
 * Obtiene el nombre de la categoría de asiento
 */
export const getCategoryName = (category: string): string => {
  const categoryMap: { [key: string]: string } = {
    'FIRST_CLASS': 'Primera Clase',
    'BUSINESS': 'Ejecutiva',
    'ECONOMY': 'Económica'
  }
  return categoryMap[category] || category
}

