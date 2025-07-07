<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="cart-container">
    <div class="cart-header">
      <h2 class="cart-title">
        <span class="cart-icon">🛒</span>
        Carrito de Compras
      </h2>
      <span class="cart-count">{{ cartItems.length }} productos</span>
    </div>

    <div v-if="cartItems.length === 0" class="empty-cart">
      <div class="empty-cart-icon">📦</div>
      <h3>Tu carrito está vacío</h3>
      <p>Agrega algunos productos para comenzar</p>
      <router-link to="/" class="btn btn-primary">
        <span class="btn-icon">🏠</span>
        Ir al Inicio
      </router-link>
    </div>

    <div v-else class="cart-content">
      <div class="cart-items">
        <div v-for="item in cartItems" :key="item.id" class="cart-item">
          <div class="item-image">
            <img :src="item.image" :alt="item.name" />
          </div>
          <div class="item-details">
            <h3 class="item-name">{{ item.name }}</h3>
            <p class="item-active">{{ item.activeMedicament }}</p>
            <div class="item-price">Q{{ (item.price || 0).toFixed(2) }}</div>
          </div>
          <div class="item-quantity">
            <button @click="decreaseQuantity(item)" class="quantity-btn" :disabled="item.quantity <= 1">
              <span class="quantity-icon">➖</span>
            </button>
            <span class="quantity-value">{{ item.quantity }}</span>
            <button @click="increaseQuantity(item)" class="quantity-btn">
              <span class="quantity-icon">➕</span>
            </button>
          </div>
          <div class="item-total">
            <span class="total-amount">Q{{ ((item.price || 0) * item.quantity).toFixed(2) }}</span>
          </div>
          <button @click="removeItem(item)" class="remove-btn">
            <span class="remove-icon">🗑️</span>
          </button>
        </div>
      </div>

      <div class="cart-summary">
        <div class="summary-header">
          <h3>Resumen de Compra</h3>
        </div>
        <div class="summary-items">
          <div class="summary-item">
            <span>Subtotal:</span>
            <span>Q{{ subtotal.toFixed(2) }}</span>
          </div>
          <div class="summary-item">
            <span>IVA (12%):</span>
            <span>Q{{ tax.toFixed(2) }}</span>
          </div>
          <div class="summary-item total">
            <span>Total:</span>
            <span>Q{{ total.toFixed(2) }}</span>
          </div>
        </div>
        <div class="summary-actions">
          <button @click="clearCart" class="btn btn-secondary">
            <span class="btn-icon">🗑️</span>
            Vaciar Carrito
          </button>
          <button @click="checkout" class="btn btn-success">
            <span class="btn-icon">💳</span>
            Proceder al Pago
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="CartView">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useUserStore } from '@/stores/userStore'
import ApiService from '../services/ApiService';  
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const currentOrder = ref(null)
const cartItems = ref([])

// Buscar la orden en progreso para el usuario actual
const fetchCurrentOrder = async () => {
  try {
    const ordersResponse = await axios.get(ApiService.getPharmacyApiUrl("/orders"))
    const orders = ordersResponse.data
    const userId = userStore.getUser().idUser
    const inProgressOrder = orders.find(o => o.user.idUser === userId && o.status === 'En progreso')
    currentOrder.value = inProgressOrder || null
  } catch (error) {
    console.error('Error fetching current order:', error)
  }
}

// Obtener los ítems (OrderMedicine) de la orden en progreso
const fetchCartItems = async (orderId) => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl("/order_medicines"))
    const allItems = response.data
    // Filtrar solo los ítems que corresponden a la orden en progreso y mapear el precio
    cartItems.value = allItems
      .filter(item => item.orders.idOrder === orderId)
      .map(item => ({
        ...item,
        // Asegura que siempre haya un campo price numérico
        price: typeof item.price === 'number' ? item.price : (item.cost || (item.medicine && item.medicine.price) || 0),
        name: item.name || (item.medicine && item.medicine.name) || '',
        image: item.image || (item.medicine && item.medicine.image) || '',
        activeMedicament: item.activeMedicament || (item.medicine && item.medicine.activeMedicament) || '',
      }))
  } catch (error) {
    console.error('Error fetching cart items:', error)
  }
}

// Eliminar un ítem del carrito (se elimina directamente de la DB)
const removeItem = async (item) => {
  try {
    const orderId = item.orders.idOrder
    const medicineId = item.medicine.idMedicine
    await axios.delete(ApiService.getPharmacyApiUrl(`/order_medicines?id=${orderId},${medicineId}`))
    // Actualizar la lista local eliminando el ítem borrado
    cartItems.value = cartItems.value.filter(i => i.medicine.idMedicine !== medicineId)
  } catch (error) {
    console.error('Error removing item from cart:', error)
  }
}

const subtotal = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
});

const tax = computed(() => {
  return subtotal.value * 0.12;
});

const total = computed(() => {
  return subtotal.value + tax.value;
});

const increaseQuantity = async (item) => {
  try {
    const newQuantity = item.quantity + 1;
    await axios.put(ApiService.getPharmacyApiUrl("/order_medicines"), {
      id: item.id,
      orders: item.orders,
      medicine: item.medicine,
      quantity: newQuantity,
      cost: item.price,
      total: item.price * newQuantity
    });
    item.quantity = newQuantity;
  } catch (error) {
    console.error('Error updating quantity:', error);
    alert('Error al actualizar la cantidad.');
  }
};

const decreaseQuantity = async (item) => {
  if (item.quantity <= 1) return;
  
  try {
    const newQuantity = item.quantity - 1;
    await axios.put(ApiService.getPharmacyApiUrl("/order_medicines"), {
      id: item.id,
      orders: item.orders,
      medicine: item.medicine,
      quantity: newQuantity,
      cost: item.price,
      total: item.price * newQuantity
    });
    item.quantity = newQuantity;
  } catch (error) {
    console.error('Error updating quantity:', error);
    alert('Error al actualizar la cantidad.');
  }
};

const clearCart = async () => {
  if (!confirm('¿Estás seguro de que quieres vaciar el carrito?')) {
    return;
  }
  
  try {
    // Eliminar todos los items del carrito
    for (const item of cartItems.value) {
      await axios.delete(ApiService.getPharmacyApiUrl(`/order_medicines?id=${item.orders.idOrder},${item.medicine.idMedicine}`));
    }
  cartItems.value = [];
    alert('Carrito vaciado exitosamente.');
  } catch (error) {
    console.error('Error clearing cart:', error);
    alert('Error al vaciar el carrito.');
  }
};

const checkout = () => {
  router.push('/checkout');
};

onMounted(async () => {
  // Verificar si el usuario está autenticado
  const user = userStore.getUser();
  if (!user || !user.idUser) {
    alert('Debes iniciar sesión para ver tu carrito.');
    router.push('/login');
    return;
  }

  await fetchCurrentOrder()
  if (currentOrder.value) {
    await fetchCartItems(currentOrder.value.idOrder)
  }
})
</script>

<style scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 70px);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-color);
}

.cart-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  color: var(--text-primary);
  margin: 0;
}

.cart-icon {
  font-size: 32px;
}

.cart-count {
  background: var(--primary-color);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
}

/* Carrito vacío */
.empty-cart {
  text-align: center;
  padding: 80px 20px;
  background: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
}

.empty-cart-icon {
  font-size: 64px;
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-cart h3 {
  color: var(--text-primary);
  font-size: 24px;
  margin: 0 0 12px 0;
}

.empty-cart p {
  color: var(--text-secondary);
  font-size: 16px;
  margin: 0 0 32px 0;
}

/* Contenido del carrito */
.cart-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 32px;
}

.cart-items {
  background: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.cart-item {
  display: grid;
  grid-template-columns: 80px 1fr auto auto auto;
  gap: 20px;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
  transition: var(--transition);
}

.cart-item:hover {
  background: var(--bg-accent);
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 80px;
  height: 80px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.item-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.item-active {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.item-price {
  font-size: 16px;
  font-weight: 600;
  color: var(--accent-color);
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--bg-secondary);
  padding: 8px 12px;
  border-radius: var(--border-radius-sm);
}

.quantity-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-btn:hover:not(:disabled) {
  background: var(--primary-color);
  color: white;
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-icon {
  font-size: 12px;
}

.quantity-value {
  font-weight: 600;
  color: var(--text-primary);
  min-width: 20px;
  text-align: center;
}

.item-total {
  text-align: right;
}

.total-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent-color);
}

.remove-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: var(--border-radius-sm);
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-btn:hover {
  background: var(--danger-color);
  color: white;
}

.remove-icon {
  font-size: 16px;
}

/* Resumen del carrito */
.cart-summary {
  background: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 24px;
  height: fit-content;
  position: sticky;
  top: 90px;
}

.summary-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.summary-header h3 {
  color: var(--text-primary);
  font-size: 20px;
  margin: 0;
}

.summary-items {
  margin-bottom: 24px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 16px;
}

.summary-item:not(:last-child) {
  border-bottom: 1px solid var(--border-color);
}

.summary-item.total {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent-color);
  border-bottom: none;
  padding-top: 16px;
  margin-top: 8px;
  border-top: 2px solid var(--border-color);
}

.summary-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-actions .btn {
  width: 100%;
  justify-content: center;
}

.btn-icon {
  font-size: 16px;
}

/* Responsive */
@media (max-width: 1024px) {
  .cart-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .cart-summary {
    position: static;
  }
}

@media (max-width: 768px) {
  .cart-container {
    padding: 16px;
  }
  
  .cart-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .cart-item {
    grid-template-columns: 1fr;
    gap: 16px;
    text-align: center;
  }
  
  .item-image {
    width: 100%;
    height: 120px;
    margin: 0 auto;
  }
  
  .item-quantity {
    justify-content: center;
  }
  
  .item-total {
    text-align: center;
  }
  
  .remove-btn {
    justify-self: center;
  }
}

@media (max-width: 480px) {
  .cart-title {
    font-size: 24px;
  }
  
  .cart-icon {
    font-size: 28px;
  }
  
  .empty-cart {
    padding: 60px 16px;
  }
  
  .empty-cart-icon {
    font-size: 48px;
  }
  
  .empty-cart h3 {
    font-size: 20px;
  }
}
</style>