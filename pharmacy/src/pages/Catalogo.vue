<!-- eslint-disable vue/multi-word-component-names -->
<!-- Catalog.vue (o como se llame tu componente) -->
<template>
  <div class="catalog-container">
    <!-- Banner y título -->
    <div class="catalog-header">
      <h2 class="title">Catálogo de Productos</h2>
      <p class="subtitle">Explora y compra lo que más te gusta</p>
    </div>

    <!-- Filtros con diseño moderno -->
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
        <!-- Dropdown de categorías -->
        <select v-model="activeIngredientFilter" class="filter-input select-input">
          <option value="">Todas las categorías</option>
          <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
        </select>
        <input
          type="text"
          v-model="brandFilter"
          placeholder="Marca"
          class="filter-input"
        />
        <div class="price-filters">
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
        </div>
      </div>
    </div>
  
    <!-- Categorías destacadas -->
    <div class="categories-section">
      <h3>Explorar por Categoría</h3>
      <div class="categories-grid">
        <router-link 
          v-for="category in categories" 
          :key="category" 
          :to="`/categoria/${encodeURIComponent(category)}`"
          class="category-card"
        >
          <div class="category-icon">💊</div>
          <span class="category-name">{{ category }}</span>
        </router-link>
      </div>
    </div>
  
    <!-- Grid de productos con nuevo diseño -->
    <div class="product-grid">
      <div v-for="product in filteredProducts" :key="product.idMedicine" class="product-card">
        <div class="product-content">
          <!-- Quitar badge de receta -->
          <div class="product-image-box">
            <img v-if="getProductImage(product)" :src="getProductImage(product)" :alt="product.name" class="product-img" />
            <div v-else class="product-img-placeholder">🛍️</div>
          </div>
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-active">{{ product.activeMedicament }}</p>
          
          <!-- Rating -->
          <div class="product-rating">
            <div class="stars">
              <span v-for="star in 5" :key="star" class="star" :class="{ filled: star <= (product.averageRating || 0) }">
                {{ star <= (product.averageRating || 0) ? '★' : '☆' }}
              </span>
            </div>
            <span class="rating-text">{{ (product.averageRating || 0).toFixed(1) }} ({{ product.ratingCount || 0 }})</span>
          </div>
          
          <p class="product-price">Q{{ product.price.toFixed(2) }}</p>
          <!-- Selector de cantidad y botón agregar al carrito -->
          <div class="product-cart-actions">
            <button class="qty-btn" @click="decrementQuantity(product)" :disabled="getQuantity(product) <= 1">-</button>
            <input type="number" class="qty-input" v-model.number="quantities[product.idMedicine]" :min="1" :max="product.stock" />
            <button class="qty-btn" @click="incrementQuantity(product)" :disabled="getQuantity(product) >= product.stock">+</button>
            <button class="add-cart-btn" @click="addToCart(product)">
              <span class="icon">🛒</span> Agregar al carrito
            </button>
          </div>
          <div class="product-actions">
            <button class="details-button" @click="openModal(product)">
              <span class="icon">ⓘ</span> Detalles
            </button>
            <router-link :to="`/producto/${product.idMedicine}`" class="buy-button">
              <span class="icon">🛒</span> Comprar
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Indicador de carga -->
    <div v-if="products.length === 0" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Cargando productos...</p>
    </div>
  </div>

  <!-- Modal de detalles del producto con diseño moderno -->
  <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <button class="close-modal" @click="closeModal">✖</button>
      <div class="modal-body">
        <h3>{{ selectedProduct.name }}</h3>
        
        <!-- Galería de imágenes en el modal -->
        <div v-if="selectedProduct.images && selectedProduct.images.length > 0" class="modal-image-gallery">
          <div class="modal-main-image">
            <img 
              :src="selectedProduct.images[0]" 
              :alt="selectedProduct.name" 
              class="modal-product-img"
            />
          </div>
          <div v-if="selectedProduct.images.length > 1" class="modal-thumbnails">
            <img 
              v-for="(image, index) in selectedProduct.images" 
              :key="index"
              :src="image" 
              :alt="`${selectedProduct.name} - Imagen ${index + 1}`" 
              class="modal-thumbnail"
              @click="selectedProduct.images[0] = image"
            />
        </div>
          </div>
        <div v-else-if="selectedProduct.image" class="modal-image">
          <img :src="selectedProduct.image" :alt="selectedProduct.name" class="modal-product-img" />
          </div>
        
        <p><strong>Descripción:</strong> {{ selectedProduct.description }}</p>
        <p><strong>Ingrediente Activo:</strong> {{ selectedProduct.activeMedicament }}</p>
        <p><strong>Marca:</strong> {{ selectedProduct.brand }}</p>
        <p><strong>Precio:</strong> Q{{ selectedProduct.price.toFixed(2) }}</p>
        <p><strong>Stock:</strong> {{ selectedProduct.stock }} unidades</p>
        <div class="modal-actions">
          <div class="modal-cart-actions">
            <button class="modal-qty-btn" @click="decrementModalQuantity" :disabled="modalQuantity <= 1">-</button>
            <input type="number" class="modal-qty-input" v-model.number="modalQuantity" :min="1" :max="selectedProduct.stock" />
            <button class="modal-qty-btn" @click="incrementModalQuantity" :disabled="modalQuantity >= selectedProduct.stock">+</button>
            <button class="modal-add-cart-btn" @click="addToCartFromModal">
              <span class="icon">🛒</span> Agregar al carrito
            </button>
          </div>
          <router-link :to="`/producto/${selectedProduct.idMedicine}`" class="modal-buy-button">
            Comprar Ahora
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from "axios";
import ApiService from '../services/ApiService';
import { useUserStore } from '@/stores/userStore';

const products = ref([]);
const categories = ref([]);
const userStore = useUserStore();
const quantities = ref({});

const fetchProduct = async () => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl("/medicines"));
    products.value = response.data;
  } catch (error) {
    console.error('Error fetching products:', error);
  }
};

const fetchCategories = async () => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl("/medicines/actives"));
    categories.value = response.data;
  } catch (error) {
    categories.value = [];
  }
};

onMounted(() => {
  fetchProduct();
  fetchCategories();
});

// Manejo de modal
const showModal = ref(false);
const selectedProduct = ref({});
const modalQuantity = ref(1);

const openModal = (product) => {
  selectedProduct.value = product;
  modalQuantity.value = 1;
  showModal.value = true;
  document.body.style.overflow = 'hidden';
};

const closeModal = () => {
  showModal.value = false;
  document.body.style.overflow = 'auto';
};

const incrementModalQuantity = () => {
  if (modalQuantity.value < selectedProduct.value.stock) {
    modalQuantity.value++;
  }
};

const decrementModalQuantity = () => {
  if (modalQuantity.value > 1) {
    modalQuantity.value--;
  }
};

const addToCartFromModal = async () => {
  const product = selectedProduct.value;
  const quantity = modalQuantity.value;
  if (!userStore.getUser() || !userStore.getUser().idUser) {
    alert('Debes iniciar sesión para agregar productos al carrito.');
    return;
  }
  const userId = userStore.getUser().idUser;
  try {
    // Buscar o crear orden en progreso
    const ordersResponse = await axios.get(ApiService.getPharmacyApiUrl("/orders"));
    const orders = ordersResponse.data;
    let order = orders.find(o => o.user.idUser === userId && o.status === 'recibido');
    if (!order) {
      const newOrderResp = await axios.post(ApiService.getPharmacyApiUrl("/orders"), {
        user: { idUser: userId },
        status: 'recibido'
      });
      order = newOrderResp.data;
    }
    // Buscar si ya existe el producto en la orden
    const orderMedResp = await axios.get(ApiService.getPharmacyApiUrl(`/order_medicines?id=${order.idOrder}%2C${product.idMedicine}`));
    let items = orderMedResp.data;
    if (!Array.isArray(items)) items = items ? [items] : [];
    const existing = items.find(om => om.medicine.idMedicine === product.idMedicine);
    const payload = {
      orders: order,
      medicine: { idMedicine: product.idMedicine },
      quantity: quantity,
      cost: product.price,
      total: product.price * quantity
    };
    if (existing) {
      payload.quantity += existing.quantity;
      await axios.put(ApiService.getPharmacyApiUrl("/order_medicines"), { ...payload, id: existing.id });
    } else {
      await axios.post(ApiService.getPharmacyApiUrl("/order_medicines"), payload);
    }
    alert('Producto agregado al carrito.');
    closeModal();
  } catch (error) {
    alert('Error al agregar al carrito.');
    console.error(error);
  }
};

// Filtros
const searchQuery = ref('');
const activeIngredientFilter = ref('');
const brandFilter = ref('');
const minPrice = ref(null);
const maxPrice = ref(null);

const filteredProducts = computed(() => {
  return products.value.filter((product) => {
    const matchesName = !searchQuery.value || product.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesDescription = !searchQuery.value || (product.description && product.description.toLowerCase().includes(searchQuery.value.toLowerCase()));
    const matchesIngredient = !activeIngredientFilter.value || product.activeMedicament === activeIngredientFilter.value;
    const matchesBrand = !brandFilter.value || product.brand.toLowerCase().includes(brandFilter.value.toLowerCase());
    const matchesMinPrice = !minPrice.value || product.price >= minPrice.value;
    const matchesMaxPrice = !maxPrice.value || product.price <= maxPrice.value;
    return (
      (matchesName || matchesDescription) &&
      matchesIngredient &&
      matchesBrand &&
      matchesMinPrice &&
      matchesMaxPrice
    );
  });
});

const getQuantity = (product) => {
  return quantities.value[product.idMedicine] || 1;
};
const incrementQuantity = (product) => {
  if (!quantities.value[product.idMedicine]) quantities.value[product.idMedicine] = 1;
  if (quantities.value[product.idMedicine] < product.stock) quantities.value[product.idMedicine]++;
};
const decrementQuantity = (product) => {
  if (!quantities.value[product.idMedicine]) quantities.value[product.idMedicine] = 1;
  if (quantities.value[product.idMedicine] > 1) quantities.value[product.idMedicine]--;
};

const addToCart = async (product) => {
  const quantity = getQuantity(product);
  if (!userStore.getUser() || !userStore.getUser().idUser) {
    alert('Debes iniciar sesión para agregar productos al carrito.');
    return;
  }
  const userId = userStore.getUser().idUser;
  try {
    // Buscar o crear orden en progreso
    const ordersResponse = await axios.get(ApiService.getPharmacyApiUrl("/orders"));
    const orders = ordersResponse.data;
    let order = orders.find(o => o.user.idUser === userId && o.status === 'recibido');
    if (!order) {
      const newOrderResp = await axios.post(ApiService.getPharmacyApiUrl("/orders"), {
        user: { idUser: userId },
        status: 'recibido'
      });
      order = newOrderResp.data;
    }
    // Buscar si ya existe el producto en la orden
    const orderMedResp = await axios.get(ApiService.getPharmacyApiUrl(`/order_medicines?id=${order.idOrder}%2C${product.idMedicine}`));
    let items = orderMedResp.data;
    if (!Array.isArray(items)) items = items ? [items] : [];
    const existing = items.find(om => om.medicine.idMedicine === product.idMedicine);
    const payload = {
      orders: order,
      medicine: { idMedicine: product.idMedicine },
      quantity: quantity,
      cost: product.price,
      total: product.price * quantity
    };
    if (existing) {
      payload.quantity += existing.quantity;
      await axios.put(ApiService.getPharmacyApiUrl("/order_medicines"), { ...payload, id: existing.id });
    } else {
      await axios.post(ApiService.getPharmacyApiUrl("/order_medicines"), payload);
    }
    alert('Producto agregado al carrito.');
  } catch (error) {
    alert('Error al agregar al carrito.');
    console.error(error);
  }
};

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
/* Moderniza los estilos para tienda en línea */
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
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}
.search-box {
  position: relative;
  margin-bottom: 1rem;
}
.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}
.search-input {
  width: 100%;
  padding: 1rem 1rem 1rem 3rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}
.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}
.filter-group {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}
.filter-input {
  flex: 1;
  min-width: 180px;
  padding: 0.8rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
}
.select-input {
  background: #f1f5f9;
  color: #1e40af;
  font-weight: 600;
}
.price-filters {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.price-input {
  width: 100px;
  min-width: auto;
}
.price-separator {
  color: #64748b;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}
.product-card {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(30, 58, 138, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.product-card:hover {
  transform: translateY(-5px) scale(1.03);
  box-shadow: 0 8px 32px rgba(30, 58, 138, 0.15);
}
.product-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.product-image-box {
  width: 120px;
  height: 120px;
  background: #f1f5f9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  overflow: hidden;
}
.product-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.product-img-placeholder {
  font-size: 2.5rem;
  color: #cbd5e1;
}
.product-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
  text-align: center;
  height: 2.6rem;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.product-active {
  color: #64748b;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}
.product-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: #16a34a;
  margin-bottom: 1rem;
  text-align: center;
}
.product-cart-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}
.qty-btn {
  width: 32px;
  height: 32px;
  background-color: #f1f1f1;
  border: none;
  border-radius: 50%;
  font-size: 1.1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}
.qty-btn:disabled {
  color: #aaa;
  cursor: not-allowed;
}
.qty-input {
  width: 48px;
  text-align: center;
  font-size: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0.3rem;
}
.add-cart-btn {
  background: linear-gradient(135deg, #16a34a 0%, #22d3ee 100%);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.6rem 1.1rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}
.add-cart-btn:hover {
  background: linear-gradient(135deg, #22d3ee 0%, #16a34a 100%);
  transform: scale(1.05);
}
.product-actions {
  display: flex;
  gap: 0.5rem;
  width: 100%;
  justify-content: center;
}
.details-button, .buy-button {
  padding: 0.7rem 1.2rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
  font-size: 1rem;
  text-decoration: none;
}
.details-button {
  background-color: #f1f5f9;
  color: #334155;
}
.details-button:hover {
  background-color: #e2e8f0;
  transform: scale(1.05);
}
.buy-button {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  color: white;
}
.buy-button:hover {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
  transform: scale(1.05);
}
.icon {
  font-size: 1.1rem;
}
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #64748b;
}
.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #1e40af;
  border-radius: 50%;
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
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}
.modal-content {
  background-color: white;
  border-radius: 16px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 2rem;
}
.close-modal {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(0, 0, 0, 0.1);
  color: #334155;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  z-index: 10;
}
.close-modal:hover {
  background-color: rgba(0, 0, 0, 0.2);
}
.modal-body {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}
.modal-header {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.modal-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}
.modal-price {
  font-size: 1.8rem;
  font-weight: 700;
  color: #16a34a;
  margin-bottom: 2rem;
}
.modal-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 2rem;
}
.stars {
  display: flex;
  gap: 0.2rem;
}
.star {
  font-size: 1.2rem;
  color: #64748b;
}
.star.filled {
  color: #1e40af;
}
.rating-text {
  font-size: 1.2rem;
  color: #64748b;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}
.info-item {
  display: flex;
  flex-direction: column;
}
.info-item h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
}
.info-item p {
  font-size: 1.1rem;
  color: #1e293b;
}
.full-width {
  grid-column: 1 / -1;
}
.description-text {
  line-height: 1.6;
}
.stock-indicator {
  font-weight: 600;
}
.low-stock {
  color: #ef4444;
}
.modal-actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.modal-cart-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
}
.modal-qty-btn {
  width: 36px;
  height: 36px;
  background-color: #f1f1f1;
  border: none;
  border-radius: 50%;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}
.modal-qty-btn:disabled {
  color: #aaa;
  cursor: not-allowed;
}
.modal-qty-input {
  width: 60px;
  text-align: center;
  font-size: 1.1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 0.5rem;
}
.modal-add-cart-btn {
  background: linear-gradient(135deg, #16a34a 0%, #22d3ee 100%);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.8rem 1.2rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}
.modal-add-cart-btn:hover {
  background: linear-gradient(135deg, #22d3ee 0%, #16a34a 100%);
  transform: scale(1.05);
}
.modal-buy-button {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  color: white;
  border: none;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s;
  text-decoration: none;
  display: inline-block;
}
.modal-buy-button:hover {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
  transform: scale(1.05);
}
@media (max-width: 768px) {
  .modal-body {
    padding: 1.5rem;
  }
  .info-grid {
    grid-template-columns: 1fr;
  }
}
.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}
.stars {
  display: flex;
  gap: 2px;
}
.star {
  color: #e2e8f0;
  font-size: 1rem;
}
.star.filled {
  color: #fbbf24;
}
.rating-text {
  font-size: 0.8rem;
  color: #64748b;
}
.categories-section {
  margin-bottom: 2rem;
}
.categories-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}
.category-card {
  background-color: white;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  transition: background-color 0.2s;
}
.category-card:hover {
  background-color: #f1f5f9;
}
.category-icon {
  font-size: 1.2rem;
  color: #1e40af;
}
.category-name {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
}
/* Estilos para la galería de imágenes en el modal */
.modal-image-gallery {
  margin-bottom: 1.5rem;
}

.modal-main-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.modal-product-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.modal-thumbnails {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding-bottom: 0.5rem;
}

.modal-thumbnail {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.modal-thumbnail:hover {
  border-color: #3b82f6;
  transform: scale(1.05);
}

.modal-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
}
</style>