<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="category-container">
    <!-- Header de la categoría -->
    <div class="category-header">
      <h1 class="category-title">{{ categoryName }}</h1>
      <p v-if="categoryDescription" class="category-description">{{ categoryDescription }}</p>
      <p v-else class="category-description">Explora nuestra selección de productos en esta categoría.</p>
    </div>

    <!-- Filtros -->
    <div class="filters-container">
      <div class="search-box">
        <i class="search-icon">🔍</i>
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Buscar en esta categoría..."
          class="search-input"
        />
      </div>
      <div class="filter-group">
        <input
          type="text"
          v-model="brandFilter"
          placeholder="Filtrar por marca"
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
        <select v-model="sortBy" class="filter-input select-input">
          <option value="">Ordenar por</option>
          <option value="name">Nombre</option>
          <option value="price-asc">Precio: Menor a Mayor</option>
          <option value="price-desc">Precio: Mayor a Menor</option>
          <option value="rating">Mejor Calificados</option>
        </select>
      </div>
    </div>

    <!-- Información de resultados -->
    <div class="results-info">
      <p>{{ filteredProducts.length }} productos encontrados</p>
    </div>

    <!-- Grid de productos -->
    <div class="product-grid">
      <div v-for="product in sortedProducts" :key="product.idMedicine" class="product-card">
        <div class="product-content">
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
              <span class="icon">🛒</span> Agregar
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
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Cargando productos...</p>
    </div>

    <!-- Mensaje si no hay productos -->
    <div v-if="!loading && filteredProducts.length === 0" class="no-products">
      <p>No se encontraron productos en esta categoría.</p>
    </div>
  </div>

  <!-- Modal de detalles del producto -->
  <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <button class="close-modal" @click="closeModal">✖</button>
      <div class="modal-body">
        <div class="modal-header">
          <h2 class="modal-title">{{ selectedProduct.name }}</h2>
          <div class="modal-price">Q{{ selectedProduct.price ? selectedProduct.price.toFixed(2) : '0.00' }}</div>
        </div>
        
        <!-- Rating en modal -->
        <div class="modal-rating">
          <div class="stars">
            <span v-for="star in 5" :key="star" class="star" :class="{ filled: star <= (selectedProduct.averageRating || 0) }">
              {{ star <= (selectedProduct.averageRating || 0) ? '★' : '☆' }}
            </span>
          </div>
          <span class="rating-text">{{ (selectedProduct.averageRating || 0).toFixed(1) }} ({{ selectedProduct.ratingCount || 0 }} calificaciones)</span>
        </div>
        
        <div class="info-grid">
          <div class="info-item">
            <h4>Categoría</h4>
            <p>{{ selectedProduct.activeMedicament || 'No disponible' }}</p>
          </div>
          <div class="info-item">
            <h4>Marca</h4>
            <p>{{ selectedProduct.brand || 'No disponible' }}</p>
          </div>
          <div class="info-item">
            <h4>Concentración</h4>
            <p>{{ selectedProduct.concentration || 'No disponible' }}</p>
          </div>
          <div class="info-item">
            <h4>Presentación</h4>
            <p>{{ selectedProduct.presentacion || 'No disponible' }}</p>
          </div>
          <div class="info-item">
            <h4>Stock Disponible</h4>
            <p class="stock-indicator" :class="{'low-stock': selectedProduct.stock < 10}">
              {{ selectedProduct.stock }} unidades
            </p>
          </div>
          <div class="info-item full-width">
            <h4>Descripción</h4>
            <p class="description-text">{{ selectedProduct.description || 'No hay descripción disponible para este producto.' }}</p>
          </div>
        </div>
        
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
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from "axios";
import ApiService from '../services/ApiService';
import { useUserStore } from '@/stores/userStore';

const route = useRoute();
const userStore = useUserStore();

// Estado reactivo
const products = ref([]);
const loading = ref(true);
const quantities = ref({});
const categoryName = ref('');
const categoryDescription = ref('');

// Filtros
const searchQuery = ref('');
const brandFilter = ref('');
const minPrice = ref(null);
const maxPrice = ref(null);
const sortBy = ref('');

// Modal
const showModal = ref(false);
const selectedProduct = ref({});
const modalQuantity = ref(1);

// Obtener el nombre de la categoría de la URL
const getCategoryFromUrl = () => {
  return decodeURIComponent(route.params.category);
};

// Cargar productos de la categoría
const fetchProductsByCategory = async () => {
  loading.value = true;
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl("/medicines"));
    const allProducts = response.data;
    const category = getCategoryFromUrl();
    
    // Filtrar productos por categoría
    products.value = allProducts.filter(product => 
      product.activeMedicament === category
    );
    
    categoryName.value = category;
    
    // Cargar descripción de la categoría (si existe)
    await loadCategoryDescription(category);
    
  } catch (error) {
    console.error('Error fetching products:', error);
  } finally {
    loading.value = false;
  }
};

// Cargar descripción de la categoría
const loadCategoryDescription = async (category) => {
  try {
    // Aquí podrías hacer una llamada a la API para obtener la descripción
    // Por ahora, usaremos descripciones hardcodeadas
    const descriptions = {
      'Analgésicos': 'Medicamentos para aliviar el dolor y reducir la fiebre. Incluye paracetamol, ibuprofeno y otros analgésicos de venta libre.',
      'Antibióticos': 'Medicamentos para tratar infecciones bacterianas. Requieren receta médica.',
      'Antiinflamatorios': 'Medicamentos para reducir la inflamación y el dolor.',
      'Antihistamínicos': 'Medicamentos para tratar alergias y síntomas alérgicos.',
      'Vitaminas': 'Suplementos vitamínicos para mantener una buena salud.',
      'Cuidado Personal': 'Productos de higiene personal y cuidado de la salud.'
    };
    
    categoryDescription.value = descriptions[category] || `Productos de la categoría ${category}`;
  } catch (error) {
    console.error('Error loading category description:', error);
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

// Productos filtrados
const filteredProducts = computed(() => {
  return products.value.filter((product) => {
    const matchesSearch = !searchQuery.value || 
      product.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      (product.description && product.description.toLowerCase().includes(searchQuery.value.toLowerCase()));
    const matchesBrand = !brandFilter.value || 
      product.brand.toLowerCase().includes(brandFilter.value.toLowerCase());
    const matchesMinPrice = !minPrice.value || product.price >= minPrice.value;
    const matchesMaxPrice = !maxPrice.value || product.price <= maxPrice.value;
    
    return matchesSearch && matchesBrand && matchesMinPrice && matchesMaxPrice;
  });
});

// Productos ordenados
const sortedProducts = computed(() => {
  const products = [...filteredProducts.value];
  
  switch (sortBy.value) {
    case 'name':
      return products.sort((a, b) => a.name.localeCompare(b.name));
    case 'price-asc':
      return products.sort((a, b) => a.price - b.price);
    case 'price-desc':
      return products.sort((a, b) => b.price - a.price);
    case 'rating':
      return products.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
    default:
      return products;
  }
});

// Funciones del modal
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

// Funciones de cantidad
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

// Funciones del carrito
const addToCart = async (product) => {
  const quantity = getQuantity(product);
  if (!userStore.getUser() || !userStore.getUser().idUser) {
    alert('Debes iniciar sesión para agregar productos al carrito.');
    return;
  }
  
  await addProductToCart(product, quantity);
};

const addToCartFromModal = async () => {
  const product = selectedProduct.value;
  const quantity = modalQuantity.value;
  await addProductToCart(product, quantity);
  closeModal();
};

const addProductToCart = async (product, quantity) => {
  const userId = userStore.getUser().idUser;
  try {
    // Buscar o crear orden en progreso
    const ordersResponse = await axios.get(ApiService.getPharmacyApiUrl("/orders"));
    const orders = ordersResponse.data;
    let order = orders.find(o => o.user.idUser === userId && o.status === 'En progreso');
    
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

// Observar cambios en la ruta
watch(() => route.params.category, () => {
  fetchProductsByCategory();
});

onMounted(() => {
  fetchProductsByCategory();
});
</script>

<style scoped>
.category-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8fafc;
  min-height: 100vh;
}

.category-header {
  text-align: center;
  margin-bottom: 2.5rem;
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.category-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.category-description {
  font-size: 1.1rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
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
  transition: all 0.3s ease;
}

.filter-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.select-input {
  background-color: white;
}

.price-filters {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.price-input {
  min-width: 120px;
}

.price-separator {
  color: #64748b;
  font-weight: 500;
}

.results-info {
  margin-bottom: 1rem;
  color: #64748b;
  font-size: 0.9rem;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.product-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.product-content {
  padding: 1.5rem;
}

.product-image-box {
  width: 100%;
  height: 200px;
  margin-bottom: 1rem;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-img-placeholder {
  font-size: 3rem;
  color: #94a3b8;
}

.product-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.product-active {
  color: #64748b;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
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

.product-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: #059669;
  margin-bottom: 1rem;
}

.product-cart-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.qty-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.qty-btn:hover:not(:disabled) {
  background: #f1f5f9;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-input {
  width: 60px;
  height: 32px;
  text-align: center;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.9rem;
}

.add-cart-btn {
  flex: 1;
  padding: 0.5rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.add-cart-btn:hover {
  background: #2563eb;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
}

.details-button, .buy-button {
  flex: 1;
  padding: 0.5rem 1rem;
  border: 1px solid #e2e8f0;
  background: white;
  color: #374151;
  text-decoration: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
  text-align: center;
}

.details-button:hover, .buy-button:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.loading-container, .no-products {
  text-align: center;
  padding: 3rem;
  color: #64748b;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.close-modal {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #64748b;
  z-index: 1;
}

.modal-body {
  padding: 2rem;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
}

.modal-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: #059669;
}

.modal-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.info-item {
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
}

.info-item h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.info-item p {
  color: #64748b;
  font-size: 0.9rem;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.description-text {
  line-height: 1.6;
}

.stock-indicator {
  font-weight: 600;
}

.stock-indicator.low-stock {
  color: #dc2626;
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
}

.modal-qty-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
}

.modal-qty-input {
  width: 70px;
  height: 36px;
  text-align: center;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}

.modal-add-cart-btn {
  flex: 1;
  padding: 0.75rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.modal-buy-button {
  padding: 0.75rem 1rem;
  background: #059669;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  text-align: center;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .category-container {
    padding: 1rem;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1rem;
  }
  
  .filter-group {
    flex-direction: column;
  }
  
  .filter-input {
    min-width: auto;
  }
}
</style> 