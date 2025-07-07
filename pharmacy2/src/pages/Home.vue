<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="main-layout">
    <!-- Menú lateral de categorías -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">Categorías</h2>
        <p class="sidebar-subtitle">Explora nuestras categorías</p>
      </div>
      <ul class="actives-list">
        <li v-for="active in actives" :key="active" @click="selectActive(active)" :class="{selected: selectedActive === active}">
          <span class="active-icon">🛍️</span>
          {{ active }}
        </li>
      </ul>
      <div class="sidebar-footer">
        <button @click="selectActive(null)" class="clear-filter-btn">
          Ver todos los productos
        </button>
      </div>
    </aside>

    <!-- Contenido principal -->
    <div class="main-content">
      <!-- Banner principal -->
      <div class="banner-container">
        <div class="banner-content">
          <img :src="bannerImage" alt="Banner" class="banner-img" />
          <div class="banner-overlay">
            <h1 class="banner-title">¡Bienvenido a tu tienda online!</h1>
            <p class="banner-subtitle">¡Encuentra lo que buscas al mejor precio!</p>
          </div>
        </div>
      </div>

      <!-- Grid de productos (categoría seleccionada) -->
      <section class="products-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🛒</span>
            Productos en "{{ selectedActive || 'Todas las categorías' }}"
          </h2>
          <p class="section-subtitle">Descubre nuestra variedad</p>
        </div>
        <div v-if="filteredProducts.length" class="product-grid">
          <div v-for="product in filteredProducts.slice(0, 12)" :key="product.idMedicine" class="product-card" @click="goToProduct(product)">
            <div class="product-image-container">
              <img :src="getProductImage(product)" alt="Imagen producto" class="product-img" />
              <div class="product-overlay">
                <button class="view-details-btn">Ver detalles</button>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-active">{{ product.activeMedicament }}</p>
              
              <!-- Product Rating -->
              <div class="product-rating">
                <span v-for="star in 5" :key="star" class="star" :class="{ 'filled': star <= (product.averageRating || 0) }">
                  {{ star <= (product.averageRating || 0) ? '★' : '☆' }}
                </span>
                <span class="rating-text">
                  {{ (product.averageRating || 0).toFixed(1) }} ({{ product.ratingCount || 0 }})
                </span>
              </div>

              <div class="product-price-container">
                <span class="product-price">Q{{ product.price.toFixed(2) }}</span>
                <button class="add-cart-btn" @click.stop="addToCart(product)">
                  <span class="cart-icon">🛒</span>
                  Agregar
                </button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-products">
          <div class="no-products-icon">🔍</div>
          <h3>No se encontraron productos</h3>
          <p>Intenta con otra categoría</p>
        </div>
      </section>

      <!-- Mostrar banners solo si NO hay categoría seleccionada -->
      <section v-if="!selectedActive" class="recommended-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">⭐</span>
            Recomendados para ti
          </h2>
          <p class="section-subtitle">Basado en tus intereses</p>
        </div>
        <div class="recommended-grid">
          <div v-for="product in recommended" :key="product.id" class="recommended-card" @click="goToProduct(product)">
            <div class="recommended-image-container">
              <img :src="getProductImage(product)" alt="Imagen recomendado" class="recommended-img" />
              <div class="recommended-badge">Recomendado</div>
            </div>
            <div class="recommended-info">
              <h3 class="recommended-name">{{ product.name }}</h3>
              <p class="recommended-active">{{ product.activeMedicament }}</p>
              
              <!-- Product Rating -->
              <div class="product-rating">
                <span v-for="star in 5" :key="star" class="star" :class="{ 'filled': star <= (product.averageRating || 0) }">
                  {{ star <= (product.averageRating || 0) ? '★' : '☆' }}
                </span>
                <span class="rating-text">
                  {{ (product.averageRating || 0).toFixed(1) }} ({{ product.ratingCount || 0 }})
                </span>
              </div>
              
              <span class="recommended-price">Q{{ product.price?.toFixed(2) || '0.00' }}</span>
            </div>
          </div>
        </div>
      </section>

      <section v-if="!selectedActive" class="bestsellers-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🔥🔥</span>
            Top Ventas
          </h2>
          <p class="section-subtitle">Nuestros productos más populares</p>
        </div>
        <div class="bestsellers-grid">
          <div v-for="product in bestsellers.slice(0, 5)" :key="product.id" class="bestseller-card" @click="goToProduct(product)">
            <div class="bestseller-image-container">
              <img :src="getProductImage(product)" alt="Imagen más vendido" class="bestseller-img" />
              <div class="bestseller-badge">Más vendido</div>
            </div>
            <div class="bestseller-info">
              <h3 class="bestseller-name">{{ product.name }}</h3>
              <p class="bestseller-active">{{ product.activeMedicament }}</p>

              <!-- Product Rating -->
              <div class="product-rating">
                <span v-for="star in 5" :key="star" class="star" :class="{ 'filled': star <= (product.averageRating || 0) }">
                  {{ star <= (product.averageRating || 0) ? '★' : '☆' }}
                </span>
                <span class="rating-text">
                  {{ (product.averageRating || 0).toFixed(1) }} ({{ product.ratingCount || 0 }})
                </span>
              </div>

              <div class="bestseller-stats">
                <span class="sold-units">Vendidos: {{ product.soldUnits || 0 }}</span>
                <span class="bestseller-price">Q{{ product.price?.toFixed(2) || '0.00' }}</span>
              </div>
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

const API_BASE = `http://${window.location.hostname}:8080/api2/medicines`;

onMounted(async () => {
  // Obtener categorías
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
  alert(`Producto agregado al carrito: ${product.name}`);
}

// Función helper para obtener la imagen principal de un producto
const getProductImage = (product) => {
  if (Array.isArray(product.images) && product.images.length > 0) {
    return product.images[0];
  } else if (product.image) {
    return product.image;
  }
  return null;
};
</script>

<style scoped>
/* Estilos principales */
.main-layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Sidebar mejorado */
.sidebar {
  width: 280px;
  background: linear-gradient(180deg, #1e3a8a 0%, #1e40af 100%);
  color: #fff;
  padding: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 20px rgba(30, 58, 138, 0.15);
  position: fixed;
  height: 100vh;
  overflow-y: auto;
  z-index: 1000;
}

.sidebar-header {
  padding: 30px 25px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #fff;
  text-align: center;
}

.sidebar-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  text-align: center;
}

.actives-list {
  list-style: none;
  padding: 20px 0;
  margin: 0;
  flex: 1;
}

.actives-list li {
  padding: 12px 25px;
  margin: 0;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  border-left: 3px solid transparent;
}

.actives-list li:hover {
  background: rgba(255, 255, 255, 0.1);
  border-left-color: #60a5fa;
}

.actives-list li.selected {
  background: rgba(255, 255, 255, 0.15);
  border-left-color: #3b82f6;
  color: #fff;
  font-weight: 600;
}

.active-icon {
  margin-right: 12px;
  font-size: 16px;
}

.sidebar-footer {
  padding: 20px 25px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.clear-filter-btn {
  width: 100%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-filter-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

/* Contenido principal */
.main-content {
  flex: 1;
  margin-left: 280px;
  padding: 0;
  background: transparent;
}

/* Banner mejorado */
.banner-container {
  margin-bottom: 40px;
  padding: 0 40px;
}

.banner-content {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(30, 58, 138, 0.15);
}

.banner-img {
  width: 100%;
  height: 300px;
  object-fit: cover;
  display: block;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(30, 58, 138, 0.8) 0%, rgba(59, 130, 246, 0.6) 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  color: white;
}

.banner-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.banner-subtitle {
  font-size: 20px;
  margin: 0;
  opacity: 0.9;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

/* Secciones */
.products-section,
.recommended-section,
.bestsellers-section {
  margin-bottom: 50px;
  padding: 0 40px;
}

.section-header {
  text-align: center;
  margin-bottom: 30px;
}

.section-title {
  font-size: 32px;
  color: #1e3a8a;
  margin: 0 0 8px 0;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  font-size: 28px;
}

.section-subtitle {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

/* Grid de productos */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 20px;
}

.product-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(30, 58, 138, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(30, 58, 138, 0.05);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(30, 58, 138, 0.15);
}

.product-image-container {
  position: relative;
  height: 200px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-img {
  width: 80%;
  height: 80%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.product-card:hover .product-img {
  transform: scale(1.05);
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(30, 58, 138, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.view-details-btn {
  background: #fff;
  color: #1e3a8a;
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-details-btn:hover {
  background: #f1f5f9;
  transform: scale(1.05);
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 18px;
  color: #1e3a8a;
  font-weight: 600;
  margin: 0 0 8px 0;
  line-height: 1.3;
}

.product-active {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 8px;
  height: 20px;
  overflow: hidden;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #64748b;
}

.product-rating .star {
  color: #e2e8f0;
  font-size: 14px;
}

.product-rating .star.filled {
  color: #fbbf24;
}

.product-price-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  color: #059669;
  font-weight: 700;
  font-size: 20px;
}

.add-cart-btn {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.add-cart-btn:hover {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
  transform: translateY(-1px);
}

.cart-icon {
  font-size: 12px;
}

/* No products */
.no-products {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(30, 58, 138, 0.08);
}

.no-products-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.no-products h3 {
  color: #1e3a8a;
  margin: 0 0 8px 0;
  font-size: 24px;
}

.no-products p {
  color: #64748b;
  margin: 0;
  font-size: 16px;
}

/* Grids de recomendados y más vendidos */
.recommended-grid,
.bestsellers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.recommended-card,
.bestseller-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(30, 58, 138, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(30, 58, 138, 0.05);
}

.recommended-card:hover,
.bestseller-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(30, 58, 138, 0.15);
}

.recommended-image-container,
.bestseller-image-container {
  position: relative;
  height: 150px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recommended-img,
.bestseller-img {
  width: 70%;
  height: 70%;
  object-fit: contain;
}

.recommended-badge,
.bestseller-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #f59e0b;
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 600;
}

.bestseller-badge {
  background: #ef4444;
}

.recommended-info,
.bestseller-info {
  padding: 16px;
}

.recommended-name,
.bestseller-name {
  font-size: 16px;
  color: #1e3a8a;
  font-weight: 600;
  margin: 0 0 6px 0;
  line-height: 1.3;
}

.recommended-active,
.bestseller-active {
  color: #64748b;
  font-size: 12px;
  margin: 0 0 12px 0;
}

.recommended-price {
  color: #059669;
  font-weight: 700;
  font-size: 16px;
}

.bestseller-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sold-units {
  color: #f59e0b;
  font-size: 12px;
  font-weight: 600;
}

.bestseller-price {
  color: #059669;
  font-weight: 700;
  font-size: 16px;
}

/* Responsive */
@media (max-width: 1024px) {
  .sidebar {
    width: 240px;
  }
  
  .main-content {
    margin-left: 240px;
  }
  
  .banner-title {
    font-size: 36px;
  }
  
  .banner-subtitle {
    font-size: 18px;
  }
}

@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
    order: 2;
  }
  
  .main-content {
    margin-left: 0;
    order: 1;
  }
  
  .banner-container,
  .products-section,
  .recommended-section,
  .bestsellers-section {
    padding: 0 20px;
  }
  
  .banner-title {
    font-size: 28px;
  }
  
  .banner-subtitle {
    font-size: 16px;
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }
  
  .recommended-grid,
  .bestsellers-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .banner-container,
  .products-section,
  .recommended-section,
  .bestsellers-section {
    padding: 0 16px;
  }
  
  .banner-title {
    font-size: 24px;
  }
  
  .banner-subtitle {
    font-size: 14px;
  }
  
  .section-title {
    font-size: 20px;
  }
  
  .product-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .recommended-grid,
  .bestsellers-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>