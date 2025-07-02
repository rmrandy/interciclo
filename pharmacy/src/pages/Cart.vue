<template>
  <div class="cart-container">
    <div class="cart-header">
      <h2 class="cart-title">
        <span class="cart-icon">🛒</span>
        Carrito de Compras
      </h2>
      <div style="font-size:0.95rem; color:#64748b; margin-top:4px;">
        Local: puerto {{ localPort }} | Internacional: puerto {{ intPort }}
      </div>
    </div>
    <!-- Sección productos locales -->
    <div class="cart-section">
      <h3 class="section-title">Productos locales</h3>
      <div v-if="cartLocal.length === 0" class="empty-cart">
        <div class="empty-cart-icon">📦</div>
        <h4>No hay productos locales en tu carrito</h4>
      </div>
      <div v-else class="cart-content">
        <div class="cart-items">
          <div v-for="(item, idx) in cartLocal" :key="item.idMedicine" class="cart-item">
            <div class="item-image">
              <img :src="getProductImage(item)" :alt="item.name" />
            </div>
            <div class="item-details">
              <h3 class="item-name">{{ item.name }}</h3>
              <div class="item-price">Q{{ (item.price || 0).toFixed(2) }}</div>
            </div>
            <div class="item-quantity">
              <button @click="decreaseQuantityLocal(idx)" class="quantity-btn" :disabled="item.quantity <= 1">
                <span class="quantity-icon">➖</span>
              </button>
              <span class="quantity-value">{{ item.quantity }}</span>
              <button @click="increaseQuantityLocal(idx)" class="quantity-btn">
                <span class="quantity-icon">➕</span>
              </button>
            </div>
            <div class="item-total">
              <span class="total-amount">Q{{ ((item.price || 0) * item.quantity).toFixed(2) }}</span>
            </div>
            <button @click="removeItemLocal(idx)" class="remove-btn">
              <span class="remove-icon">🗑️</span>
            </button>
          </div>
        </div>
        <div class="cart-summary">
          <div class="summary-header">
            <h3>Resumen de Compra Local</h3>
          </div>
          <div class="summary-items">
            <div class="summary-item">
              <span>Subtotal:</span>
              <span>Q{{ subtotalLocal.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span>IVA (12%):</span>
              <span>Q{{ taxLocal.toFixed(2) }}</span>
            </div>
            <div class="summary-item total">
              <span>Total:</span>
              <span>Q{{ totalLocal.toFixed(2) }}</span>
            </div>
          </div>
          <button @click="checkoutLocal" class="btn btn-success">
            <span class="btn-icon">💳</span>
            Comprar productos locales
          </button>
          <button @click="clearCartLocal" class="btn btn-secondary" style="margin-top:12px;">
            <span class="btn-icon">🗑️</span>
            Vaciar productos locales
          </button>
          <div v-if="mensajeLocal" class="mensaje">{{ mensajeLocal }}</div>
        </div>
      </div>
    </div>
    <!-- Sección productos internacionales -->
    <div class="cart-section">
      <h3 class="section-title">Productos internacionales</h3>
      <div v-if="cartInt.length === 0" class="empty-cart">
        <div class="empty-cart-icon">🌎</div>
        <h4>No hay productos internacionales en tu carrito</h4>
      </div>
      <div v-else class="cart-content">
        <div class="cart-items">
          <div v-for="(item, idx) in cartInt" :key="item._id" class="cart-item">
            <div class="item-image">
              <img :src="item.imagenes && item.imagenes.length ? item.imagenes[0] : ''" :alt="item.nombre" />
            </div>
            <div class="item-details">
              <h3 class="item-name">{{ item.nombre }}</h3>
              <div class="item-price">Q{{ (item.precio || 0).toFixed(2) }}</div>
            </div>
            <div class="item-quantity">
              <button @click="decreaseQuantityInt(idx)" class="quantity-btn" :disabled="item.cantidad <= 1">
                <span class="quantity-icon">➖</span>
              </button>
              <span class="quantity-value">{{ item.cantidad }}</span>
              <button @click="increaseQuantityInt(idx)" class="quantity-btn">
                <span class="quantity-icon">➕</span>
              </button>
            </div>
            <div class="item-total">
              <span class="total-amount">Q{{ ((item.precio || 0) * item.cantidad).toFixed(2) }}</span>
            </div>
            <button @click="removeItemInt(idx)" class="remove-btn">
              <span class="remove-icon">🗑️</span>
            </button>
          </div>
        </div>
        <div class="cart-summary">
          <div class="summary-header">
            <h3>Resumen de Compra Internacional</h3>
          </div>
          <div class="summary-items">
            <div class="summary-item">
              <span>Subtotal:</span>
              <span>Q{{ subtotalInt.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span>IVA (12%):</span>
              <span>Q{{ taxInt.toFixed(2) }}</span>
            </div>
            <div class="summary-item total">
              <span>Total:</span>
              <span>Q{{ totalInt.toFixed(2) }}</span>
            </div>
          </div>
          <form @submit.prevent="checkoutInt">
            <div class="form-group">
              <label>Dirección de entrega</label>
              <input v-model="addressInt" type="text" class="form-control" required />
            </div>
            <div class="form-group">
              <label>Número de tarjeta</label>
              <input v-model="cardInt" type="text" class="form-control" maxlength="19" required />
            </div>
            <div class="form-group">
              <label>CVV</label>
              <input v-model="cvvInt" type="text" class="form-control" maxlength="4" required />
            </div>
            <button type="submit" class="btn btn-success">
              <span class="btn-icon">💳</span>
              Comprar productos internacionales
            </button>
          </form>
          <button @click="clearCartInt" class="btn btn-secondary" style="margin-top:12px;">
            <span class="btn-icon">🗑️</span>
            Vaciar productos internacionales
          </button>
          <div v-if="mensajeInt" class="mensaje">{{ mensajeInt }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watchEffect, onMounted } from 'vue';
import { useUserStore } from '@/stores/userStore';
import ApiService from '../services/ApiService';
import eventBus from '@/eventBus';
const userStore = useUserStore();
const internationalPort = localStorage.getItem('internationalPort') || '8001';
const ip = process.env.VUE_APP_API_HOST || process.env.VUE_APP_IP || 'localhost';

function getActiveLocalPort() {
  return window.location.port || '8081';
}
function getActiveIntPort() {
  return localStorage.getItem('internationalPort') || '8001';
}
const localPort = getActiveLocalPort();
const intPort = getActiveIntPort();
const cartLocal = ref(JSON.parse(localStorage.getItem(`cartLocal_${localPort}`) || '[]'));
const cartInt = ref(JSON.parse(localStorage.getItem(`cartInt_${intPort}`) || '[]'));
const mensajeLocal = ref('');
const mensajeInt = ref('');

// Carrito local (simulación: puedes adaptar a tu lógica real)
const subtotalLocal = computed(() => cartLocal.value.reduce((sum, item) => sum + (item.price * item.quantity), 0));
const taxLocal = computed(() => subtotalLocal.value * 0.12);
const totalLocal = computed(() => subtotalLocal.value + taxLocal.value);

function getProductImage(item) {
  if (Array.isArray(item.images) && item.images.length > 0) return item.images[0];
  if (item.image) return item.image;
  return '';
}
const increaseQuantityLocal = (idx) => { cartLocal.value[idx].quantity++; saveCartLocal(); };
const decreaseQuantityLocal = (idx) => { if (cartLocal.value[idx].quantity > 1) { cartLocal.value[idx].quantity--; saveCartLocal(); } };
const removeItemLocal = (idx) => { cartLocal.value.splice(idx, 1); saveCartLocal(); };
const clearCartLocal = () => { if (confirm('¿Vaciar productos locales?')) { cartLocal.value = []; saveCartLocal(); } };
function saveCartLocal() { localStorage.setItem(`cartLocal_${localPort}`, JSON.stringify(cartLocal.value)); }

const checkoutLocal = async () => {
  mensajeLocal.value = '';
  const user = userStore.getUser();
  if (!user || !user.idUser) {
    mensajeLocal.value = 'Debes iniciar sesión para comprar productos locales.';
    return;
  }
  // Aquí iría la lógica de compra local (puedes adaptar a tu endpoint real)
  mensajeLocal.value = '¡Compra local simulada! (Implementa tu lógica real aquí)';
  cartLocal.value = [];
  saveCartLocal();
};

// Carrito internacional
const subtotalInt = computed(() => cartInt.value.reduce((sum, item) => sum + (item.precio * item.cantidad), 0));
const taxInt = computed(() => subtotalInt.value * 0.12);
const totalInt = computed(() => subtotalInt.value + taxInt.value);
const addressInt = ref('Guatemala, Carretera a El Salvador');
const cardInt = ref('');
const cvvInt = ref('');
const increaseQuantityInt = (idx) => { cartInt.value[idx].cantidad++; saveCartInt(); };
const decreaseQuantityInt = (idx) => { if (cartInt.value[idx].cantidad > 1) { cartInt.value[idx].cantidad--; saveCartInt(); } };
const removeItemInt = (idx) => { cartInt.value.splice(idx, 1); saveCartInt(); };
const clearCartInt = () => { if (confirm('¿Vaciar productos internacionales?')) { cartInt.value = []; saveCartInt(); } };
function saveCartInt() { localStorage.setItem(`cartInt_${intPort}`, JSON.stringify(cartInt.value)); }

const checkoutInt = async () => {
  mensajeInt.value = '';
  // Usuario quemado
  const usuario = {
    _id: '685ad1573cc65431e00dbe97',
    username: 'DaniN',
    email: 'danielanatareno@gmail.com',
    address: addressInt.value,
    role: 'Usuario particular',
    estado: true
  };
  const productos = cartInt.value.map(item => ({ _id: item._id, cantidad: item.cantidad }));
  try {
    const url = `http://${ip}:${internationalPort}/pedidos/`;
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usuario, productos, direccion: addressInt.value, tarjeta: cardInt.value, cvv: cvvInt.value })
    });
    const data = await resp.json();
    if (resp.ok) {
      mensajeInt.value = `¡Compra internacional exitosa! Código de orden: ${data.codigo_orden}`;
      cartInt.value = [];
      saveCartInt();
    } else {
      mensajeInt.value = data.detail || data.mensaje || 'Error en la compra internacional';
    }
  } catch (e) {
    mensajeInt.value = 'Error de red o servidor';
  }
};

function reloadCarts() {
  cartLocal.value = JSON.parse(localStorage.getItem(`cartLocal_${localPort}`) || '[]');
  cartInt.value = JSON.parse(localStorage.getItem(`cartInt_${intPort}`) || '[]');
}

onMounted(() => {
  eventBus.on('carrito-actualizado', reloadCarts);
  window.addEventListener('storage', reloadCarts);
  reloadCarts();
});
</script>

<style scoped>
@import '../components/Cart.vue';
.cart-section { margin-bottom: 48px; }
.section-title { font-size: 1.3rem; color: #1e40af; margin-bottom: 12px; }
.mensaje { margin-top: 16px; color: #059669; font-weight: 600; }
</style> 