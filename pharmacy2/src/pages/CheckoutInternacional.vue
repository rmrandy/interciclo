<template>
  <div class="checkout-int-container">
    <h1 class="checkout-title">Finalizar Compra Internacional</h1>
    <div v-if="cartInt.length === 0" class="empty-cart">
      <p>Tu carrito internacional está vacío.</p>
      <router-link to="/catalogo-internacional" class="btn btn-primary">Ir al catálogo internacional</router-link>
    </div>
    <div v-else class="checkout-content">
      <div class="cart-summary-box">
        <h2>Resumen del Carrito Internacional</h2>
        <div class="cart-list">
          <div v-for="item in cartInt" :key="item.id_producto" class="cart-product">
            <img :src="item.imagenes && item.imagenes.length ? item.imagenes[0] : ''" :alt="item.nombre" class="cart-img" />
            <div class="cart-info">
              <div class="cart-name">{{ item.nombre }}</div>
              <div class="cart-qty">Cantidad: {{ item.cantidad }}</div>
              <div class="cart-price">Q{{ (item.precio_unitario || item.precio || 0).toFixed(2) }}</div>
              <div class="cart-total">Total: Q{{ ((item.precio_unitario || item.precio || 0) * item.cantidad).toFixed(2) }}</div>
            </div>
          </div>
        </div>
        <div class="cart-totals">
          <div>Subtotal: <span>Q{{ subtotal.toFixed(2) }}</span></div>
          <div>Costo de envío: <span>Q{{ costoEnvio.toFixed(2) }}</span></div>
          <div class="total">Total a pagar: <span>Q{{ total.toFixed(2) }}</span></div>
        </div>
      </div>
      <div class="payment-box">
        <h2>Datos de Envío y Pago</h2>
        <form @submit.prevent="confirmPurchase">
          <div class="form-group">
            <label for="calle">Dirección</label>
            <input id="calle" v-model="direccion.calle" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="ciudad">Ciudad</label>
              <input id="ciudad" v-model="direccion.ciudad" required />
            </div>
            <div class="form-group">
              <label for="codigo_postal">Código Postal</label>
              <input id="codigo_postal" v-model="direccion.codigo_postal" required />
            </div>
          </div>
          <div class="form-group">
            <label for="horario">Horario de entrega</label>
            <select id="horario" v-model="horarioEntrega" required>
              <option value="">Selecciona un horario</option>
              <option value="Mañana">Mañana</option>
              <option value="Tarde">Tarde</option>
              <option value="Noche">Noche</option>
            </select>
          </div>
          <div class="form-group">
            <label for="cardNumber">Número de Tarjeta</label>
            <input id="cardNumber" v-model="cardNumber" maxlength="16" required />
          </div>
          <div class="form-group">
            <label for="cvv">CVV</label>
            <input id="cvv" v-model="cvv" maxlength="4" required />
          </div>
          <button type="submit" class="btn btn-success" :disabled="isProcessing">Confirmar Compra</button>
        </form>
        <div v-if="success" class="success-message">
          <h3>¡Pedido internacional enviado con éxito!</h3>
          <p>Código de orden: <b>{{ codigoOrden }}</b></p>
          <router-link to="/mis-pedidos" class="btn btn-primary">Ver mis pedidos</router-link>
        </div>
        <div v-if="error" class="error-message">
          <p>{{ error }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

// Usuario internacional predeterminado
const defaultInternationalUser = {
  _id: '685ad1573cc65431e00dbe97',
  username: 'DaniN',
  email: 'danielanatareno@gmail.com',
  address: 'Guatemala, Carretera a El Salvador',
  role: 'Usuario particular',
  estado: true
};

const internationalPort = localStorage.getItem('internationalPort') || '8001';
const ip = process.env.VUE_APP_API_HOST || process.env.VUE_APP_IP || 'localhost';

const cartInt = ref([]);
const direccion = ref({ calle: '', ciudad: '', codigo_postal: '' });
const horarioEntrega = ref('');
const cardNumber = ref('');
const cvv = ref('');
const isProcessing = ref(false);
const success = ref(false);
const error = ref('');
const codigoOrden = ref('');
const costoEnvio = ref(100);

const subtotal = computed(() => cartInt.value.reduce((sum, item) => sum + ((item.precio_unitario || item.precio || 0) * item.cantidad), 0));
const total = computed(() => subtotal.value + costoEnvio.value);

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

async function confirmPurchase() {
  isProcessing.value = true;
  error.value = '';
  success.value = false;
  codigoOrden.value = '';
  // Validaciones básicas
  if (!direccion.value.calle || !direccion.value.ciudad || !direccion.value.codigo_postal || !horarioEntrega.value || !cardNumber.value || !cvv.value) {
    error.value = 'Por favor, completa todos los campos.';
    isProcessing.value = false;
    return;
  }
  if (!/^\d{16}$/.test(cardNumber.value)) {
    error.value = 'El número de tarjeta debe tener 16 dígitos.';
    isProcessing.value = false;
    return;
  }
  if (!/^\d{3,4}$/.test(cvv.value)) {
    error.value = 'El CVV debe tener 3 o 4 dígitos.';
    isProcessing.value = false;
    return;
  }
  // Construir payload
  const productos = cartInt.value.map(item => ({
    id_producto: item.id_producto,
    nombre: item.nombre,
    precio_unitario: item.precio_unitario || item.precio,
    cantidad: item.cantidad
  }));
  const payload = {
    id_usuario: defaultInternationalUser._id,
    productos,
    subtotal: subtotal.value,
    costo_envio: costoEnvio.value,
    total: total.value,
    direccion: direccion.value,
    horario_entrega: horarioEntrega.value,
    fecha: new Date().toISOString(),
    tarjeta: cardNumber.value,
    cvv: cvv.value
  };
  try {
    const url = `http://${ip}:${internationalPort}/pedidos/`;
    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await resp.json();
    if (resp.ok) {
      success.value = true;
      codigoOrden.value = data.codigo_orden;
      cartInt.value = [];
      // Redirigir o limpiar carrito si es necesario
    } else {
      error.value = data.detail || data.mensaje || 'Error al procesar la compra.';
    }
  } catch {
    error.value = 'Error de red al procesar la compra.';
  } finally {
    isProcessing.value = false;
  }
}

fetchCartInt();
</script>

<style scoped>
.checkout-int-container {
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
.payment-box {
  flex: 1;
  background: #f9fafb;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  padding: 2rem;
}
.form-group {
  margin-bottom: 1.2rem;
}
.form-row {
  display: flex;
  gap: 1rem;
}
input, select {
  width: 100%;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  font-size: 16px;
}
.btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  background: #1e40af;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  margin-top: 1rem;
}
.btn-success {
  background: #059669;
}
.btn-primary {
  background: #1e40af;
}
.success-message {
  margin-top: 2rem;
  color: #059669;
  font-weight: 600;
  background: #e0f7ef;
  padding: 1.5rem;
  border-radius: 12px;
  text-align: center;
}
.error-message {
  margin-top: 1rem;
  color: #b91c1c;
  font-weight: 600;
  background: #fee2e2;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
}
.empty-cart {
  text-align: center;
  margin-top: 3rem;
  color: #64748b;
}
</style> 