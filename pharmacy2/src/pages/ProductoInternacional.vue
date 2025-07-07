<template>
  <div class="detalle-container" v-if="product">
    <div class="detalle-header">
      <h1>{{ product.nombre }}</h1>
      <p class="categoria">{{ product.categoria }}</p>
    </div>
    <div class="detalle-main">
      <div class="galeria">
        <img :src="product.imagenes && product.imagenes.length ? product.imagenes[activeImage] : ''" :alt="product.nombre" class="main-img" />
        <div v-if="product.imagenes && product.imagenes.length > 1" class="thumbnails">
          <img v-for="(img, idx) in product.imagenes" :key="idx" :src="img" :alt="'Miniatura '+(idx+1)" class="thumb" :class="{active: idx===activeImage}" @click="activeImage=idx" />
        </div>
      </div>
      <div class="detalle-info">
        <p class="descripcion">{{ product.descripcion }}</p>
        <p><strong>Marca:</strong> {{ product.marca }}</p>
        <p><strong>Precio:</strong> Q{{ product.precio.toFixed(2) }}</p>
        <p><strong>Inventario:</strong> {{ product.inventario }}</p>
        <p><strong>Rating:</strong> {{ product.rating }}</p>
        <div v-if="product.caracteristicas && product.caracteristicas.length">
          <h4>Características:</h4>
          <ul>
            <li v-for="(car, i) in product.caracteristicas" :key="i">
              <strong>{{ car.nombre }}:</strong> {{ car.valor }}
            </li>
          </ul>
        </div>
        <div class="cart-int-actions">
          <input type="number" min="1" v-model.number="cantidad" class="qty-input" style="width:60px; margin-right:8px;" />
          <button class="add-cart-btn" @click="addToCartLocal">
            <span class="icon">🛒</span> Agregar al carrito
          </button>
        </div>
        <div v-if="mensaje" class="mensaje">{{ mensaje }}</div>
      </div>
    </div>
  </div>
  <div v-else class="loading-container">
    <div class="loading-spinner"></div>
    <p>Cargando producto...</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const product = ref(null);
const activeImage = ref(0);
const cantidad = ref(1);
const mensaje = ref('');
const internationalPort = localStorage.getItem('internationalPort') || '8001';
const ip = process.env.VUE_APP_API_HOST || process.env.VUE_APP_IP || 'localhost';

const fetchProduct = async () => {
  try {
    const url = `http://${ip}:${internationalPort}/productos/${route.params.id}`;
    const response = await axios.get(url);
    product.value = response.data;
  } catch (e) {
    product.value = null;
  }
};

function addToCartLocal() {
  mensaje.value = '';
  const qty = cantidad.value || 1;
  let cart = JSON.parse(localStorage.getItem('cartLocal') || '[]');
  const idx = cart.findIndex(p => p._id === product.value._id);
  if (idx >= 0) {
    cart[idx].cantidad += qty;
  } else {
    cart.push({ ...product.value, cantidad: qty });
  }
  localStorage.setItem('cartLocal', JSON.stringify(cart));
  mensaje.value = 'Producto internacional agregado al carrito.';
}

onMounted(fetchProduct);
</script>

<style scoped>
.detalle-container {
  max-width: 900px;
  margin: 40px auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(30,58,138,0.08);
  padding: 32px;
}
.detalle-header {
  text-align: center;
  margin-bottom: 2rem;
}
.detalle-header h1 {
  font-size: 2.2rem;
  font-weight: 700;
  color: #1e40af;
}
.categoria {
  color: #64748b;
  font-size: 1.1rem;
}
.detalle-main {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}
.galeria {
  flex: 1 1 320px;
  text-align: center;
}
.main-img {
  max-width: 350px;
  max-height: 350px;
  border-radius: 12px;
  margin-bottom: 1rem;
  background: #f1f5f9;
}
.thumbnails {
  display: flex;
  gap: 8px;
  justify-content: center;
}
.thumb {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
}
.thumb.active {
  border-color: #2563eb;
}
.detalle-info {
  flex: 2 1 320px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  font-size: 1.1rem;
}
.descripcion {
  color: #334155;
  margin-bottom: 1rem;
}
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 2rem;
}
.loading-spinner {
  border: 4px solid #e5e7eb;
  border-top: 4px solid #2563eb;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.cart-int-actions {
  display: flex;
  align-items: center;
  margin-top: 0.5rem;
  gap: 0.5rem;
}
.add-cart-btn {
  background: linear-gradient(135deg, #2563eb 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.add-cart-btn:hover {
  background: linear-gradient(135deg, #059669 0%, #2563eb 100%);
}
.qty-input {
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 1rem;
  width: 60px;
}
.mensaje {
  margin: 16px 0;
  color: #059669;
  font-weight: 600;
}
</style> 