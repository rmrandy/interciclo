<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="checkout-container">
    <h1 class="checkout-title">Finalizar Compra</h1>
    <div v-if="cartItems.length === 0" class="empty-cart">
      <p>Tu carrito está vacío.</p>
      <router-link to="/catalogo" class="btn btn-primary">Ir al catálogo</router-link>
    </div>
    <div v-else class="checkout-content">
      <div class="cart-summary-box">
        <h2>Resumen del Carrito</h2>
        <div class="cart-list">
          <div v-for="item in cartItems" :key="item.medicine?.idMedicine || item.id" class="cart-product">
            <img :src="item.image" :alt="item.name" class="cart-img" />
            <div class="cart-info">
              <div class="cart-name">{{ item.name }}</div>
              <div class="cart-qty">Cantidad: {{ item.quantity }}</div>
              <div class="cart-price">Q{{ (item.price || 0).toFixed(2) }}</div>
              <div class="cart-total">Total: Q{{ ((item.price || 0) * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>
        <div class="cart-totals">
          <div>Subtotal: <span>Q{{ subtotal.toFixed(2) }}</span></div>
          <div>IVA (12%): <span>Q{{ tax.toFixed(2) }}</span></div>
          <div class="total">Total a pagar: <span>Q{{ total.toFixed(2) }}</span></div>
        </div>
      </div>
      <div class="payment-box">
        <h2>Datos de Pago</h2>
        <form @submit.prevent="confirmPurchase">
          <div class="form-group">
            <label for="cardName">Titular de la Tarjeta</label>
            <input id="cardName" v-model="cardName" required />
          </div>
          <div class="form-group">
            <label for="cardNumber">Número de Tarjeta</label>
            <input id="cardNumber" v-model="cardNumber" maxlength="19" required @input="onCardNumberInput" inputmode="numeric" pattern="[0-9 ]*" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="cardExpiry">Expiración</label>
              <input id="cardExpiry" v-model="cardExpiry" placeholder="MM/AA" maxlength="5" required @input="onCardExpiryInput" inputmode="numeric" pattern="[0-9/]*" />
            </div>
            <div class="form-group">
              <label for="cardCVC">CVC</label>
              <input id="cardCVC" v-model="cardCVC" maxlength="4" required />
            </div>
          </div>
          <button type="submit" class="btn btn-success" :disabled="isProcessing">Confirmar Compra</button>
        </form>
        <div v-if="success" class="success-message">
          <h3>¡Pedido enviado con éxito!</h3>
          <p>Tu pedido ha sido enviado y está siendo procesado. Pronto recibirás la confirmación.</p>
          <router-link to="/catalogo" class="btn btn-primary">Seguir comprando</router-link>
        </div>
        <div v-if="error" class="error-message">
          <p>{{ error }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="CheckoutPage">
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import ApiService from '../services/ApiService';
import { useUserStore } from '@/stores/userStore';

const userStore = useUserStore();
const cartItems = ref([]);
const subtotal = computed(() => cartItems.value.reduce((sum, item) => sum + ((item.price || 0) * item.quantity), 0));
const tax = computed(() => subtotal.value * 0.12);
const total = computed(() => subtotal.value + tax.value);

const cardName = ref('');
const cardNumber = ref('');
const cardExpiry = ref('');
const cardCVC = ref('');
const isProcessing = ref(false);
const success = ref(false);
const error = ref('');

const fetchCart = async () => {
  try {
    const ordersResponse = await axios.get(ApiService.getPharmacyApiUrl('/orders'));
    const orders = ordersResponse.data;
    const userId = userStore.getUser().idUser;
    const inProgressOrder = orders.find(o => o.user.idUser === userId && o.status === 'En progreso');
    if (!inProgressOrder) {
      cartItems.value = [];
      return;
    }
    const response = await axios.get(ApiService.getPharmacyApiUrl('/order_medicines'));
    const allItems = response.data;
    cartItems.value = allItems
      .filter(item => item.orders.idOrder === inProgressOrder.idOrder)
      .map(item => ({
        ...item,
        price: typeof item.price === 'number' ? item.price : (item.cost || (item.medicine && item.medicine.price) || 0),
        name: item.name || (item.medicine && item.medicine.name) || '',
        image: item.image || (item.medicine && item.medicine.image) || '',
        activeMedicament: item.activeMedicament || (item.medicine && item.medicine.activeMedicament) || '',
      }));
  } catch (err) {
    cartItems.value = [];
  }
};

const onCardNumberInput = (e) => {
  let value = e.target.value.replace(/\D/g, '').slice(0, 16);
  value = value.replace(/(.{4})/g, '$1 ').trim();
  cardNumber.value = value;
};
const onCardExpiryInput = (e) => {
  let value = e.target.value.replace(/\D/g, '').slice(0, 4);
  if (value.length > 2) value = value.slice(0, 2) + '/' + value.slice(2);
  cardExpiry.value = value;
};

const cleanCardNumber = computed(() => cardNumber.value.replace(/\D/g, ''));

const confirmPurchase = async () => {
  isProcessing.value = true;
  error.value = '';
  success.value = false;
  // Validación mejorada de campos
  if (!cardName.value || cleanCardNumber.value.length !== 16 || !/^\d{2}\/\d{2}$/.test(cardExpiry.value) || !/^\d{3,4}$/.test(cardCVC.value)) {
    error.value = 'Por favor, completa los datos de pago correctamente.';
    isProcessing.value = false;
    return;
  }
  try {
    const userId = userStore.getUser().idUser;
    const productos = cartItems.value.map(item => ({
      idMedicine: item.medicine?.idMedicine || item.idMedicine || item.id,
      quantity: item.quantity
    }));
    const pago = {
      cardName: cardName.value,
      cardNumber: cleanCardNumber.value,
      expDate: cardExpiry.value,
      cvc: cardCVC.value
    };
    const response = await ApiService.checkoutOrder({ userId, productos, pago });
    if (response && response.success) {
      success.value = true;
      cartItems.value = [];
    } else {
      error.value = response?.error || 'Error al procesar la compra. Intenta de nuevo.';
    }
  } catch (err) {
    error.value = 'Error al procesar la compra. Intenta de nuevo.';
  } finally {
    isProcessing.value = false;
  }
};

onMounted(() => {
  fetchCart();
});
</script>

<style scoped>
.checkout-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  min-height: 100vh;
}
.checkout-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 2rem;
  text-align: center;
}
.checkout-content {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}
.cart-summary-box {
  flex: 2;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.07);
  padding: 2rem;
}
.cart-list {
  margin-bottom: 2rem;
}
.cart-product {
  display: flex;
  gap: 1.2rem;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
  padding: 1rem 0;
}
.cart-img {
  width: 70px;
  height: 70px;
  object-fit: contain;
  border-radius: 8px;
  background: #f1f5f9;
}
.cart-info {
  flex: 1;
}
.cart-name {
  font-weight: 600;
  font-size: 1.1rem;
}
.cart-qty, .cart-price, .cart-total {
  font-size: 0.98rem;
  color: #334155;
}
.cart-totals {
  margin-top: 1.5rem;
  font-size: 1.1rem;
}
.cart-totals .total {
  font-size: 1.3rem;
  font-weight: 700;
  color: #16a34a;
}
.payment-box {
  flex: 1.2;
  background: #f8fafc;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(30,58,138,0.08);
  padding: 2rem;
}
.form-group {
  margin-bottom: 1.2rem;
}
.form-row {
  display: flex;
  gap: 1rem;
}
input {
  width: 100%;
  padding: 0.7rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 1rem;
}
.btn {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
  display: inline-block;
}
.btn-success {
  background: linear-gradient(135deg, #16a34a 0%, #22d3ee 100%);
  color: white;
}
.btn-primary {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  color: white;
}
.success-message {
  background: #e0fce0;
  color: #166534;
  border-radius: 8px;
  padding: 1.5rem;
  margin-top: 2rem;
  text-align: center;
}
.error-message {
  background: #fee2e2;
  color: #b91c1c;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  text-align: center;
}
.empty-cart {
  text-align: center;
  margin-top: 4rem;
  color: #64748b;
}
</style> 