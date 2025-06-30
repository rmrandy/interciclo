<!-- eslint-disable vue/multi-word-component-names -->
<!-- CatalogoAlt.vue: Catálogo alternativo que consume el backend en otro puerto -->
<template>
  <div class="catalog-container">
    <!-- Configuración de puerto alternativo (solo admin/empleado) -->
    <div v-if="isAdminOrEmployee" class="alt-port-config">
      <label for="alt-port-input">Puerto alternativo:</label>
      <input id="alt-port-input" v-model="altPortInput" type="number" min="1" max="65535" style="width:100px; margin-right:8px;" />
      <button @click="saveAltPort" class="save-alt-port-btn">Guardar</button>
      <span v-if="showSavedMsg" class="saved-msg">✔ Guardado</span>
    </div>
    <!-- Banner y título -->
    <div class="catalog-header">
      <h2 class="title">Catálogo de Productos (ALT)</h2>
      <p class="subtitle">Explora y compra lo que más te gusta (fuente alternativa)</p>
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
          <div class="product-image-box">
            <img v-if="getProductImage(product)" :src="getProductImage(product)" :alt="product.name" class="product-img" />
            <div v-else class="product-img-placeholder">🛍️</div>
          </div>
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-active">{{ product.activeMedicament }}</p>
          <div class="product-rating">
            <div class="stars">
              <span v-for="star in 5" :key="star" class="star" :class="{ filled: star <= (product.averageRating || 0) }">
                {{ star <= (product.averageRating || 0) ? '★' : '☆' }}
              </span>
            </div>
            <span class="rating-text">{{ (product.averageRating || 0).toFixed(1) }} ({{ product.ratingCount || 0 }})</span>
          </div>
          <p class="product-price">Q{{ product.price.toFixed(2) }}</p>
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
    <div v-if="products.length === 0" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Cargando productos...</p>
    </div>
  </div>
  <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <button class="close-modal" @click="closeModal">✖</button>
      <div class="modal-body">
        <h3>{{ selectedProduct.name }}</h3>
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
import { getApiUrlWithPort } from '../services/ApiService';
import { useUserStore } from '@/stores/userStore';

const ALT_PORT_KEY = 'altCatalogPort';
const DEFAULT_ALT_PORT = 8085;

const userStore = useUserStore();
const isAdminOrEmployee = computed(() => {
  const user = userStore.getUser();
  return user && ['admin','administrador','employee','empleado'].includes(user.role);
});

const altPort = ref(Number(localStorage.getItem(ALT_PORT_KEY)) || DEFAULT_ALT_PORT);
const altPortInput = ref(altPort.value);
const showSavedMsg = ref(false);

function saveAltPort() {
  altPort.value = Number(altPortInput.value) || DEFAULT_ALT_PORT;
  localStorage.setItem(ALT_PORT_KEY, altPort.value);
  showSavedMsg.value = true;
  setTimeout(() => showSavedMsg.value = false, 1200);
  fetchProduct();
  fetchCategories();
}

const products = ref([]);
const categories = ref([]);
const quantities = ref({});

const fetchProduct = async () => {
  try {
    const response = await axios.get(getApiUrlWithPort("/medicines", altPort.value));
    products.value = response.data;
  } catch (error) {
    console.error('Error fetching products:', error);
  }
};

const fetchCategories = async () => {
  try {
    const response = await axios.get(getApiUrlWithPort("/medicines/actives", altPort.value));
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
    const ordersResponse = await axios.get(getApiUrlWithPort("/orders", altPort.value));
    const orders = ordersResponse.data;
    let order = orders.find(o => o.user.idUser === userId && o.status === 'recibido');
    if (!order) {
      const newOrderResp = await axios.post(getApiUrlWithPort("/orders", altPort.value), {
        user: { idUser: userId },
        status: 'recibido'
      });
      order = newOrderResp.data;
    }
    const orderMedResp = await axios.get(getApiUrlWithPort(`/order_medicines?id=${order.idOrder}%2C${product.idMedicine}`, altPort.value));
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
      await axios.put(getApiUrlWithPort("/order_medicines", altPort.value), { ...payload, id: existing.id });
    } else {
      await axios.post(getApiUrlWithPort("/order_medicines", altPort.value), payload);
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
    const ordersResponse = await axios.get(getApiUrlWithPort("/orders", altPort.value));
    const orders = ordersResponse.data;
    let order = orders.find(o => o.user.idUser === userId && o.status === 'recibido');
    if (!order) {
      const newOrderResp = await axios.post(getApiUrlWithPort("/orders", altPort.value), {
        user: { idUser: userId },
        status: 'recibido'
      });
      order = newOrderResp.data;
    }
    const orderMedResp = await axios.get(getApiUrlWithPort(`/order_medicines?id=${order.idOrder}%2C${product.idMedicine}`, altPort.value));
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
      await axios.put(getApiUrlWithPort("/order_medicines", altPort.value), { ...payload, id: existing.id });
    } else {
      await axios.post(getApiUrlWithPort("/order_medicines", altPort.value), payload);
    }
    alert('Producto agregado al carrito.');
  } catch (error) {
    alert('Error al agregar al carrito.');
    console.error(error);
  }
};

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
.alt-port-config {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
  background: #f1f5f9;
  padding: 0.7rem 1.2rem;
  border-radius: 8px;
  font-size: 1rem;
  gap: 0.5rem;
}
.save-alt-port-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0.3rem 0.9rem;
  font-weight: 600;
  cursor: pointer;
  margin-left: 4px;
}
.saved-msg {
  color: #16a34a;
  font-weight: 600;
  margin-left: 8px;
}
/* Copia los estilos de Catalogo.vue */
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
</style> 