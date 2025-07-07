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
      <div style="font-size:0.95rem; color:#64748b; margin-bottom:8px;">
        Clave de carrito internacional: <b>{`cartInt_${intPort}`}</b> | Puerto internacional: <b>{intPort}</b> | Productos en localStorage: <b>{cartInt.value.length}</b>
      </div>
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
const cartInt = ref([]);
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

// Carrito internacional (igual que local, pero para productos internacionales)
const subtotalInt = computed(() => cartInt.value.reduce((sum, item) => sum + (item.precio * item.cantidad), 0));
const taxInt = computed(() => subtotalInt.value * 0.12);
const totalInt = computed(() => subtotalInt.value + taxInt.value);
const addressInt = ref('Guatemala, Carretera a El Salvador');
const cardInt = ref('');
const cvvInt = ref('');

// Usuario internacional predeterminado
const defaultInternationalUser = {
  _id: '685ad1573cc65431e00dbe97',
  username: 'DaniN',
  email: 'danielanatareno@gmail.com',
  address: 'Guatemala, Carretera a El Salvador',
  role: 'Usuario particular',
  estado: true
};

async function fetchCartInt() {
  try {
    const url = `http://${ip}:${internationalPort}/carrito/${defaultInternationalUser._id}`;
    const resp = await fetch(url);
    if (resp.ok) {
      cartInt.value = await resp.json();
    } else {
      cartInt.value = [];
    }
  } catch {
    cartInt.value = [];
  }
}

async function addToCartInt(product, cantidad = 1) {
  mensajeInt.value = '';
  try {
    const url = `http://${ip}:${internationalPort}/carrito/agregar`;
    const body = {
      id_usuario: defaultInternationalUser._id,
      producto: {
        id_producto: product._id,
        nombre: product.nombre,
        precio: product.precio,
        cantidad: cantidad
      }
    };
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (resp.ok) {
      mensajeInt.value = 'Producto internacional agregado al carrito.';
      await fetchCartInt();
    } else {
      mensajeInt.value = 'Error al agregar producto internacional.';
    }
  } catch {
    mensajeInt.value = 'Error de red al agregar producto internacional.';
  }
}

async function removeItemInt(idx) {
  const item = cartInt.value[idx];
  if (!item) return;
  try {
    const url = `http://${ip}:${internationalPort}/carrito/${defaultInternationalUser._id}/eliminar/${item.id_producto}`;
    const resp = await fetch(url, { method: 'DELETE' });
    if (resp.ok) {
      await fetchCartInt();
    }
  } catch {}
}

async function increaseQuantityInt(idx) {
  const item = cartInt.value[idx];
  if (!item) return;
  await updateQuantityInt(item, item.cantidad + 1);
}

async function decreaseQuantityInt(idx) {
  const item = cartInt.value[idx];
  if (!item || item.cantidad <= 1) return;
  await updateQuantityInt(item, item.cantidad - 1);
}

async function updateQuantityInt(item, nuevaCantidad) {
  try {
    const url = `http://${ip}:${internationalPort}/carrito/${defaultInternationalUser._id}/actualizar-cantidad`;
    const body = {
      id_producto: item.id_producto,
      cantidad: nuevaCantidad
    };
    const resp = await fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (resp.ok) {
      await fetchCartInt();
    }
  } catch {}
}

const checkoutInt = async () => {
  mensajeInt.value = '';
  if (cartInt.value.length === 0) {
    mensajeInt.value = 'No hay productos internacionales en el carrito.';
    return;
  }
  // Validaciones básicas
  if (!addressInt.value || !cardInt.value || !cvvInt.value) {
    mensajeInt.value = 'Por favor, completa todos los campos de compra internacional.';
    return;
  }
  if (!/^\d{16}$/.test(cardInt.value.replace(/\s/g, ''))) {
    mensajeInt.value = 'El número de tarjeta debe tener 16 dígitos.';
    return;
  }
  if (!/^\d{3,4}$/.test(cvvInt.value)) {
    mensajeInt.value = 'El CVV debe tener 3 o 4 dígitos.';
    return;
  }
  // Usuario real (no quemado)
  const usuario = {
    // Puedes usar el usuario logueado si lo deseas, aquí solo ejemplo:
    // _id: userStore.getUser()?.idUser || '',
    username: userStore.getUser()?.name || 'Usuario',
    email: userStore.getUser()?.email || '',
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
  fetchCartInt();
});
</script>

<style scoped>
@import '../components/Cart.vue';
.cart-section { margin-bottom: 48px; }
.section-title { font-size: 1.3rem; color: #1e40af; margin-bottom: 12px; }
.mensaje { margin-top: 16px; color: #059669; font-weight: 600; }
</style> 