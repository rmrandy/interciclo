<template>
  <div class="catalog-container">
    <div class="catalog-header">
      <h2 class="title">Catálogo Internacional</h2>
      <p class="subtitle">Explora productos de la red internacional</p>
    </div>
    <div class="filters-container">
      <div class="search-box">
        <i class="search-icon">🔍</i>
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Buscar producto..."
          class="search-input"
        />
      </div>
      <div class="filter-group">
        <select v-model="categoryFilter" class="filter-input select-input">
          <option value="">Todas las categorías</option>
          <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
        </select>
        <input
          type="number"
          v-model.number="minPrice"
          placeholder="Precio mín"
          class="filter-input price-input"
        />
        <span class="price-separator">-</span>
        <input
          type="number"
          v-model.number="maxPrice"
          placeholder="Precio máx"
          class="filter-input price-input"
        />
        <button @click="showPortDialog = true" class="btn btn-secondary" style="margin-left: 12px;">🌐 Puerto</button>
      </div>
    </div>
    <div class="product-grid">
      <div v-for="product in filteredProducts" :key="product._id" class="product-card">
        <div class="product-content">
          <div class="product-image-box">
            <img v-if="product.imagenes && product.imagenes.length > 0" :src="product.imagenes[0]" :alt="product.nombre" class="product-img" />
            <div v-else class="product-img-placeholder">🌎</div>
          </div>
          <h3 class="product-name">{{ product.nombre }}</h3>
          <p class="product-category">{{ product.categoria }}</p>
          <p class="product-price">Q{{ product.precio.toFixed(2) }}</p>
          <div class="product-actions">
            <button class="details-button" @click="openModal(product)">
              <span class="icon">ⓘ</span> Detalles
            </button>
            <router-link :to="`/producto-internacional/${product._id}`" class="buy-button">
              <span class="icon">🛒</span> Ver producto
            </router-link>
          </div>
          <div class="cart-int-actions">
            <input type="number" min="1" v-model.number="addQty[product._id]" class="qty-input" style="width:60px; margin-right:8px;" />
            <button class="add-cart-btn" @click="addToCartLocal(product)">
              <span class="icon">🛒</span> Agregar al carrito
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="products.length === 0" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Cargando productos...</p>
    </div>
    <div v-if="showPortDialog" class="modal-overlay">
      <div class="modal-container">
        <div class="modal-header">
          <h2>Puerto de Catálogo Internacional</h2>
        </div>
        <div class="modal-body">
          <label for="internationalPort">Puerto:</label>
          <input id="internationalPort" v-model="internationalPort" type="text" class="form-control" placeholder="8001" />
          <p class="help-text">Ejemplo: 8001</p>
        </div>
        <div class="modal-footer">
          <button @click="savePort" class="btn btn-primary">Guardar</button>
          <button @click="showPortDialog = false" class="btn btn-secondary">Cancelar</button>
        </div>
      </div>
    </div>
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <button class="close-modal" @click="closeModal">✖</button>
        <div v-if="modalProduct">
          <h2>{{ modalProduct.nombre }}</h2>
          <div v-if="modalProduct.imagenes && modalProduct.imagenes.length > 0" class="modal-image-gallery">
            <img :src="modalProduct.imagenes[activeImage]" :alt="modalProduct.nombre" class="modal-main-img" />
            <div v-if="modalProduct.imagenes.length > 1" class="modal-thumbnails">
              <img v-for="(img, idx) in modalProduct.imagenes" :key="idx" :src="img" :alt="'Miniatura '+(idx+1)" class="modal-thumb" :class="{active: idx===activeImage}" @click="activeImage=idx" />
            </div>
          </div>
          <p><strong>Descripción:</strong> {{ modalProduct.descripcion }}</p>
          <p><strong>Categoría:</strong> {{ modalProduct.categoria }}</p>
          <p><strong>Marca:</strong> {{ modalProduct.marca }}</p>
          <p><strong>Precio:</strong> Q{{ modalProduct.precio.toFixed(2) }}</p>
          <p><strong>Inventario:</strong> {{ modalProduct.inventario }}</p>
          <p><strong>Rating:</strong> {{ modalProduct.rating }}</p>
          <div v-if="modalProduct.caracteristicas && modalProduct.caracteristicas.length">
            <h4>Características:</h4>
            <ul>
              <li v-for="(car, i) in modalProduct.caracteristicas" :key="i">
                <strong>{{ car.nombre }}:</strong> {{ car.valor }}
              </li>
            </ul>
          </div>
        </div>
        <div v-else>
          <p>Cargando...</p>
        </div>
      </div>
    </div>
    <div v-if="mensaje" class="mensaje" style="text-align:center; margin:16px 0; color:#059669; font-weight:600;">{{ mensaje }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watchEffect } from 'vue';
import axios from 'axios';
import eventBus from '@/eventBus';

const products = ref([]);
const categories = ref([]);
const searchQuery = ref('');
const categoryFilter = ref('');
const minPrice = ref(null);
const maxPrice = ref(null);
const showPortDialog = ref(false);
const internationalPort = ref(localStorage.getItem('internationalPort') || '8001');
const ip = process.env.VUE_APP_API_HOST || process.env.VUE_APP_IP || 'localhost';
const showModal = ref(false);
const modalProduct = ref(null);
const activeImage = ref(0);
const addQty = ref({});
const mensaje = ref('');

const fetchProducts = async () => {
  try {
    const url = `http://${ip}:${internationalPort.value}/productos/`;
    const response = await axios.get(url);
    products.value = response.data;
    // Extraer categorías únicas
    categories.value = [...new Set(response.data.map(p => p.categoria).filter(Boolean))];
    // Inicializar cantidades
    response.data.forEach(p => { if (!(p._id in addQty.value)) addQty.value[p._id] = 1; });
  } catch (error) {
    products.value = [];
    categories.value = [];
  }
};

onMounted(fetchProducts);

watchEffect(() => {
  fetchProducts();
});

const savePort = () => {
  localStorage.setItem('internationalPort', internationalPort.value);
  showPortDialog.value = false;
  fetchProducts();
};

const filteredProducts = computed(() => {
  return products.value.filter(product => {
    const matchesName = !searchQuery.value || product.nombre.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesCategory = !categoryFilter.value || product.categoria === categoryFilter.value;
    const matchesMinPrice = !minPrice.value || product.precio >= minPrice.value;
    const matchesMaxPrice = !maxPrice.value || product.precio <= maxPrice.value;
    return matchesName && matchesCategory && matchesMinPrice && matchesMaxPrice;
  });
});

const openModal = async (product) => {
  showModal.value = true;
  modalProduct.value = null;
  activeImage.value = 0;
  try {
    const url = `http://${ip}:${internationalPort.value}/productos/${product._id}`;
    const response = await axios.get(url);
    modalProduct.value = response.data;
  } catch (e) {
    modalProduct.value = null;
  }
};

const closeModal = () => {
  showModal.value = false;
  modalProduct.value = null;
  activeImage.value = 0;
};

function getActiveIntPort() {
  return localStorage.getItem('internationalPort') || '8001';
}

function addToCartLocal(product) {
  mensaje.value = '';
  const qty = addQty.value[product._id] || 1;
  const port = getActiveIntPort();
  const key = `cartInt_${port}`;
  let cart = JSON.parse(localStorage.getItem(key) || '[]');
  const idx = cart.findIndex(p => p._id === product._id);
  if (idx >= 0) {
    cart[idx].cantidad += qty;
  } else {
    cart.push({ ...product, cantidad: qty });
  }
  localStorage.setItem(key, JSON.stringify(cart));
  mensaje.value = 'Producto internacional agregado al carrito.';
  eventBus.emit('carrito-actualizado');
}
</script>

<style scoped>
.catalog-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8fafc;
  min-height: 100vh;
}
.catalog-header {
  text-align: center;
  margin-bottom: 2.5rem;
}
.title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 0.5rem;
}
.subtitle {
  font-size: 1.2rem;
  color: #64748b;
}
.filters-container {
  background-color: white;
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
}
.search-box {
  display: flex;
  align-items: center;
  flex: 1;
}
.search-icon {
  margin-right: 8px;
  font-size: 1.2rem;
}
.search-input {
  flex: 1;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.filter-input {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
.price-input {
  width: 100px;
}
.price-separator {
  margin: 0 8px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 2rem;
}
.product-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(30,58,138,0.08);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: box-shadow 0.2s;
}
.product-card:hover {
  box-shadow: 0 4px 24px rgba(30,58,138,0.12);
}
.product-image-box {
  width: 120px;
  height: 120px;
  background: #f1f5f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}
.product-img {
  max-width: 100%;
  max-height: 100%;
  border-radius: 8px;
}
.product-img-placeholder {
  font-size: 2.5rem;
  color: #64748b;
}
.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.3rem;
  text-align: center;
}
.product-category {
  font-size: 0.95rem;
  color: #64748b;
  margin-bottom: 0.5rem;
}
.product-price {
  font-size: 1.2rem;
  font-weight: 700;
  color: #059669;
  margin-bottom: 0.5rem;
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
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal-container {
  background-color: white;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}
.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}
.modal-body {
  padding: 20px;
}
.form-control {
  width: 100%;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #ccc;
  margin-bottom: 12px;
}
.help-text {
  color: #64748b;
  font-size: 0.95rem;
}
.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}
.btn-primary {
  background: #2563eb;
  color: #fff;
}
.btn-secondary {
  background: #64748b;
  color: #fff;
}
.modal-content {
  background: #fff;
  border-radius: 12px;
  padding: 2rem;
  max-width: 500px;
  width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}
.close-modal {
  position: absolute;
  top: 12px;
  right: 12px;
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}
.modal-image-gallery {
  text-align: center;
  margin-bottom: 1rem;
}
.modal-main-img {
  max-width: 100%;
  max-height: 250px;
  border-radius: 8px;
  margin-bottom: 0.5rem;
}
.modal-thumbnails {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: 0.5rem;
}
.modal-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
  border: 2px solid transparent;
  cursor: pointer;
}
.modal-thumb.active {
  border-color: #2563eb;
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