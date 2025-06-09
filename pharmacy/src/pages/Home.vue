<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="main-layout">
    <!-- Menú lateral de principios activos -->
    <aside class="sidebar">
      <h2 class="sidebar-title">Principios Activos</h2>
      <ul class="actives-list">
        <li v-for="active in actives" :key="active" @click="selectActive(active)" :class="{selected: selectedActive === active}">
          {{ active }}
        </li>
      </ul>
    </aside>

    <!-- Contenido principal -->
    <div class="main-content">
      <!-- Banner principal -->
      <div class="banner-container">
        <img :src="bannerImage" alt="Banner" class="banner-img" />
      </div>

      <!-- Grid de productos (principios activos seleccionados) -->
      <section class="products-section">
        <h2 class="section-title">Productos con "{{ selectedActive || 'Todos' }}"</h2>
        <div class="products-grid">
          <div v-for="product in filteredProducts" :key="product.id" class="product-card" @click="goToProduct(product)">
            <img :src="product.image" alt="Imagen producto" class="product-img" />
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-active">{{ product.activeMedicament }}</p>
              <p class="product-price">Q{{ product.price.toFixed(2) }}</p>
              <button class="add-cart-btn" @click.stop="addToCart(product)">Agregar al carrito</button>
            </div>
          </div>
        </div>
      </section>

      <!-- Recomendados para ti -->
      <section class="recommended-section">
        <h2 class="section-title">Recomendados para ti</h2>
        <div class="recommended-grid">
          <div v-for="product in recommended" :key="product.id" class="recommended-card" @click="goToProduct(product)">
            <img :src="product.image" alt="Imagen recomendado" class="recommended-img" />
            <div class="recommended-info">
              <h3>{{ product.name }}</h3>
              <p>{{ product.activeMedicament }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- Más vendidos -->
      <section class="bestsellers-section">
        <h2 class="section-title">Los Más Vendidos</h2>
        <div class="bestsellers-grid">
          <div v-for="product in bestsellers" :key="product.id" class="bestseller-card" @click="goToProduct(product)">
            <img :src="product.image" alt="Imagen más vendido" class="bestseller-img" />
            <div class="bestseller-info">
              <h3>{{ product.name }}</h3>
              <p>{{ product.activeMedicament }}</p>
              <span class="sold-units">Vendidos: {{ product.soldUnits }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

// Imagen estática para el banner
import bannerImage from '@/assets/farmacia.png';

const router = useRouter();

const actives = ref([]);
const allProducts = ref([]);
const recommended = ref([]);
const bestsellers = ref([]);
const selectedActive = ref(null);

const API_BASE = 'http://192.168.0.4:8080/api2/medicines';

onMounted(async () => {
  // Obtener principios activos
  try {
    const res = await axios.get(`${API_BASE}/actives`);
    actives.value = res.data;
  } catch (e) {
    actives.value = [];
  }

  // Obtener todos los productos
  try {
    const res = await axios.get(`${API_BASE}`);
    allProducts.value = res.data;
  } catch (e) {
    allProducts.value = [];
  }

  // Obtener recomendados
  try {
    const res = await axios.get(`${API_BASE}/recommended`);
    recommended.value = res.data;
  } catch (e) {
    recommended.value = [];
  }

  // Obtener más vendidos
  try {
    const res = await axios.get(`${API_BASE}/bestsellers`);
    bestsellers.value = res.data;
  } catch (e) {
    bestsellers.value = [];
  }
});

const filteredProducts = computed(() => {
  if (!selectedActive.value) return allProducts.value;
  return allProducts.value.filter(p => p.activeMedicament === selectedActive.value);
});

function selectActive(active) {
  selectedActive.value = active;
}

function goToProduct(product) {
  router.push({ name: 'ProductoDetalle', params: { id: product.idMedicine || product.id } });
}

function addToCart(product) {
  // Aquí iría la lógica real de agregar al carrito
  alert(`Producto agregado al carrito: ${product.name}`);
}
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
  background: #f4f7fb;
}

.sidebar {
  width: 260px;
  background: #1e3a8a;
  color: #fff;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  box-shadow: 2px 0 8px rgba(30,58,138,0.08);
}
.sidebar-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 18px;
  color: #fff;
}
.actives-list {
  list-style: none;
  padding: 0;
  width: 100%;
}
.actives-list li {
  padding: 10px 12px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.actives-list li.selected, .actives-list li:hover {
  background: #2563eb;
  color: #fff;
}

.main-content {
  flex: 1;
  padding: 32px 40px;
}
.banner-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 32px;
}
.banner-img {
  width: 80%;
  max-width: 900px;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(30,58,138,0.10);
}
.section-title {
  font-size: 24px;
  color: #1e3a8a;
  margin-bottom: 18px;
  font-weight: bold;
}
.products-section {
  margin-bottom: 36px;
}
.products-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
.product-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(30,58,138,0.07);
  width: 210px;
  padding: 18px 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.product-card:hover {
  box-shadow: 0 6px 24px rgba(30,58,138,0.13);
}
.product-img {
  width: 90px;
  height: 90px;
  object-fit: contain;
  margin-bottom: 10px;
}
.product-info {
  text-align: center;
}
.product-name {
  font-size: 18px;
  color: #1e3a8a;
  font-weight: bold;
}
.product-active {
  color: #64748b;
  font-size: 14px;
  margin: 4px 0;
}
.product-price {
  color: #059669;
  font-weight: bold;
  margin-bottom: 8px;
}
.add-cart-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 16px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}
.add-cart-btn:hover {
  background: #1e40af;
}
.recommended-section, .bestsellers-section {
  margin-bottom: 36px;
}
.recommended-grid, .bestsellers-grid {
  display: flex;
  gap: 20px;
}
.recommended-card, .bestseller-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(30,58,138,0.07);
  width: 180px;
  padding: 14px 10px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: box-shadow 0.2s;
}
.recommended-card:hover, .bestseller-card:hover {
  box-shadow: 0 6px 24px rgba(30,58,138,0.13);
}
.recommended-img, .bestseller-img {
  width: 70px;
  height: 70px;
  object-fit: contain;
  margin-bottom: 8px;
}
.recommended-info, .bestseller-info {
  text-align: center;
}
.sold-units {
  color: #f59e42;
  font-size: 13px;
  font-weight: bold;
  margin-top: 4px;
}
</style>