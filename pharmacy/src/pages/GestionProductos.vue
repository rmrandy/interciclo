<template>
  <div class="gestion-container">
    <div class="gestion-header">
      <h2 class="title">Gestión de Productos</h2>
      <p class="subtitle">Administra el catálogo de tu tienda</p>
      <button class="btn btn-primary" @click="openCreateModal">+ Nuevo Producto</button>
      <button class="btn btn-secondary" @click="openCategoryModal">Gestionar Categorías</button>
    </div>

    <!-- Tabla de productos -->
    <div class="products-table-container">
      <table class="products-table">
        <thead>
          <tr>
            <th>Imagen</th>
            <th>Nombre</th>
            <th>Categoría</th>
            <th>Marca</th>
            <th>Precio</th>
            <th>Stock</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.idMedicine">
            <td>
              <img 
                v-if="getProductImage(product)" 
                :src="getProductImage(product)" 
                :alt="product.name" 
                class="table-img" 
              />
              <span v-else class="table-img-placeholder">🛍️</span>
            </td>
            <td>{{ product.name }}</td>
            <td>{{ product.activeMedicament }}</td>
            <td>{{ product.brand }}</td>
            <td>Q{{ product.price.toFixed(2) }}</td>
            <td>{{ product.stock }}</td>
            <td>
              <button class="btn btn-secondary btn-sm" @click="openEditModal(product)">Editar</button>
              <button class="btn btn-danger btn-sm" @click="deleteProduct(product)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="products.length === 0" class="empty-table">No hay productos registrados.</div>
    </div>

    <!-- Modal de crear/editar producto -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <button class="close-modal" @click="closeModal">✖</button>
        <div class="modal-body">
          <h2 class="modal-title">{{ isEditMode ? 'Editar Producto' : 'Nuevo Producto' }}</h2>
          <form @submit.prevent="isEditMode ? updateProduct() : createProduct()">
            <div class="form-group">
              <label>Nombre</label>
              <input v-model="form.name" type="text" required class="form-input" />
            </div>
            <div class="form-group">
              <label>Categoría</label>
              <select v-model="form.activeMedicament" required class="form-input">
                <option value="" disabled>Selecciona una categoría</option>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Marca</label>
              <input v-model="form.brand" type="text" required class="form-input" />
            </div>
            <div class="form-group">
              <label>Precio</label>
              <input v-model.number="form.price" type="number" step="0.01" required class="form-input" />
            </div>
            <div class="form-group">
              <label>Stock</label>
              <input v-model.number="form.stock" type="number" required class="form-input" />
            </div>
            <div class="form-group">
              <label>Imágenes (URLs separadas por comas)</label>
              <div v-for="(image, index) in form.images" :key="index" class="image-input">
                <input v-model="form.images[index]" type="text" class="form-input" placeholder="URL de la imagen" />
                <button class="btn btn-secondary btn-sm" @click="removeImageInput(index)">Eliminar</button>
              </div>
              <button type="button" class="btn btn-secondary" @click="addImageInput">+ Agregar Imagen</button>
            </div>
            <div class="form-actions">
              <button type="submit" class="btn btn-primary">{{ isEditMode ? 'Actualizar' : 'Crear' }}</button>
              <button type="button" class="btn btn-secondary" @click="closeModal">Cancelar</button>
            </div>
            <div v-if="errorMessage" class="form-error">{{ errorMessage }}</div>
          </form>
        </div>
      </div>
    </div>

    <!-- Modal de gestión de categorías -->
    <div v-if="showCategoryModal" class="modal-overlay" @click.self="closeCategoryModal">
      <div class="modal-content">
        <button class="close-modal" @click="closeCategoryModal">✖</button>
        <div class="modal-body">
          <h2 class="modal-title">Gestionar Categorías</h2>
          <form @submit.prevent="isEditCategoryMode ? updateCategory() : createCategory()">
            <div class="form-group">
              <label>Nombre de la categoría</label>
              <input v-model="categoryForm.name" type="text" required class="form-input" />
            </div>
            <div class="form-actions">
              <button type="submit" class="btn btn-primary">{{ isEditCategoryMode ? 'Actualizar' : 'Crear' }}</button>
              <button type="button" class="btn btn-secondary" @click="closeCategoryModal">Cancelar</button>
            </div>
            <div v-if="categoryErrorMessage" class="form-error">{{ categoryErrorMessage }}</div>
          </form>
          <div class="categories-list">
            <h3>Categorías existentes</h3>
            <ul>
              <li v-for="cat in categories" :key="cat">
                <span>{{ cat }}</span>
                <button class="btn btn-secondary btn-sm" @click="editCategory(cat)">Editar</button>
                <button class="btn btn-danger btn-sm" @click="deleteCategory(cat)">Eliminar</button>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import ApiService from '../services/ApiService';

const products = ref([]);
const categories = ref([]);
const showModal = ref(false);
const isEditMode = ref(false);
const errorMessage = ref('');
const showCategoryModal = ref(false);
const isEditCategoryMode = ref(false);
const categoryForm = reactive({ name: '', oldName: '' });
const categoryErrorMessage = ref('');
const MAX_IMAGES = 5;
const form = reactive({
  idMedicine: null,
  name: '',
  activeMedicament: '',
  brand: '',
  price: 0,
  stock: 0,
  images: [''],
});

const fetchProducts = async () => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl('/medicines'));
    products.value = response.data;
  } catch (error) {
    products.value = [];
  }
};

const fetchCategories = async () => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl('/medicines/actives'));
    categories.value = response.data;
  } catch (error) {
    categories.value = [];
  }
};

onMounted(() => {
  fetchProducts();
  fetchCategories();
});

function openCreateModal() {
  isEditMode.value = false;
  errorMessage.value = '';
  Object.assign(form, { idMedicine: null, name: '', activeMedicament: '', brand: '', price: 0, stock: 0, images: [''] });
  showModal.value = true;
}

function openEditModal(product) {
  isEditMode.value = true;
  errorMessage.value = '';
  let imagesArr = [];
  // Recuperar imágenes extra de localStorage
  let extraImages = [];
  if (product.idMedicine) {
    try {
      extraImages = JSON.parse(localStorage.getItem(`product_extra_images_${product.idMedicine}`)) || [];
    } catch {
      // No hacemos nada si no hay imágenes extra
    }
  }
  if (Array.isArray(product.images)) {
    imagesArr = product.images;
  } else if (typeof product.image === 'string' && product.image.trim() !== '') {
    imagesArr = [product.image.trim()];
  } else {
    imagesArr = [''];
  }
  imagesArr = imagesArr.concat(extraImages).slice(0, MAX_IMAGES);
  Object.assign(form, { ...product, images: imagesArr });
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

function addImageInput() {
  if (form.images.length < MAX_IMAGES) {
    form.images.push('');
  }
}

function removeImageInput(index) {
  if (form.images.length > 1) {
    form.images.splice(index, 1);
  }
}

async function createProduct() {
  errorMessage.value = '';
  try {
    // Solo la primera imagen se envía al backend
    const imageString = form.images.find(url => url.trim() !== '') || '';
    const productData = { ...form, image: imageString };
    delete productData.images;
    const response = await axios.post(ApiService.getPharmacyApiUrl('/medicines'), productData);
    showModal.value = false;
    fetchProducts();
    // Guardar imágenes extra en localStorage
    if (response.data && response.data.idMedicine) {
      const extraImages = form.images.filter((url, idx) => idx > 0 && url.trim() !== '');
      if (extraImages.length > 0) {
        localStorage.setItem(`product_extra_images_${response.data.idMedicine}`, JSON.stringify(extraImages));
      }
    }
  } catch (error) {
    errorMessage.value = 'Error al crear el producto.';
  }
}

async function updateProduct() {
  errorMessage.value = '';
  try {
    const imageString = form.images.find(url => url.trim() !== '') || '';
    const productData = { ...form, image: imageString };
    delete productData.images;
    await axios.put(ApiService.getPharmacyApiUrl(`/medicines/${form.idMedicine}`), productData);
    showModal.value = false;
    fetchProducts();
    // Guardar imágenes extra en localStorage
    const extraImages = form.images.filter((url, idx) => idx > 0 && url.trim() !== '');
    if (extraImages.length > 0) {
      localStorage.setItem(`product_extra_images_${form.idMedicine}`, JSON.stringify(extraImages));
    } else {
      localStorage.removeItem(`product_extra_images_${form.idMedicine}`);
    }
  } catch (error) {
    errorMessage.value = 'Error al actualizar el producto.';
  }
}

async function deleteProduct(product) {
  if (!confirm(`¿Seguro que deseas eliminar el producto "${product.name}"?`)) return;
  try {
    await axios.delete(ApiService.getPharmacyApiUrl(`/medicines/${product.idMedicine}`));
    fetchProducts();
  } catch (error) {
    alert('Error al eliminar el producto.');
  }
}

// Gestión de categorías (solo admin/empleados)
function openCategoryModal() {
  isEditCategoryMode.value = false;
  categoryErrorMessage.value = '';
  categoryForm.name = '';
  categoryForm.oldName = '';
  showCategoryModal.value = true;
}
function closeCategoryModal() {
  showCategoryModal.value = false;
}
function editCategory(cat) {
  isEditCategoryMode.value = true;
  categoryErrorMessage.value = '';
  categoryForm.name = cat;
  categoryForm.oldName = cat;
}
async function createCategory() {
  categoryErrorMessage.value = '';
  try {
    // POST a un endpoint de categorías (ajusta según tu backend)
    await axios.post(ApiService.getPharmacyApiUrl('/medicines/actives'), { name: categoryForm.name });
    categoryForm.name = '';
    fetchCategories();
  } catch (error) {
    categoryErrorMessage.value = 'Error al crear la categoría.';
  }
}
async function updateCategory() {
  categoryErrorMessage.value = '';
  try {
    // PUT a un endpoint de categorías (ajusta según tu backend)
    await axios.put(ApiService.getPharmacyApiUrl(`/medicines/actives/${encodeURIComponent(categoryForm.oldName)}`), { name: categoryForm.name });
    isEditCategoryMode.value = false;
    categoryForm.name = '';
    categoryForm.oldName = '';
    fetchCategories();
  } catch (error) {
    categoryErrorMessage.value = 'Error al actualizar la categoría.';
  }
}
async function deleteCategory(cat) {
  if (!confirm(`¿Seguro que deseas eliminar la categoría "${cat}"?`)) return;
  try {
    // DELETE a un endpoint de categorías (ajusta según tu backend)
    await axios.delete(ApiService.getPharmacyApiUrl(`/medicines/actives/${encodeURIComponent(cat)}`));
    fetchCategories();
  } catch (error) {
    alert('Error al eliminar la categoría.');
  }
}

function getProductImage(product) {
  if (Array.isArray(product.images)) {
    return product.images[0];
  } else if (product.image) {
    return product.image;
  } else {
    return null;
  }
}
</script>

<style scoped>
.gestion-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background: #f8fafc;
  min-height: 100vh;
}
.gestion-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  gap: 1rem;
}
.title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #1e40af;
}
.subtitle {
  color: #64748b;
  font-size: 1.1rem;
}
.products-table-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(30, 58, 138, 0.08);
  padding: 2rem;
}
.products-table {
  width: 100%;
  border-collapse: collapse;
}
.products-table th, .products-table td {
  padding: 0.8rem 1rem;
  text-align: left;
}
.products-table th {
  background: #f1f5f9;
  color: #1e40af;
  font-weight: 700;
  font-size: 1rem;
}
.products-table tr {
  border-bottom: 1px solid #e2e8f0;
}
.products-table tr:last-child {
  border-bottom: none;
}
.table-img {
  width: 60px;
  height: 60px;
  object-fit: contain;
  border-radius: 8px;
  background: #f1f5f9;
}
.table-img-placeholder {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 8px;
  font-size: 2rem;
  color: #cbd5e1;
}
.btn {
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  font-size: 1rem;
  margin-right: 0.5rem;
}
.btn-sm {
  padding: 0.4rem 0.8rem;
  font-size: 0.95rem;
}
.btn-primary {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  color: white;
}
.btn-primary:hover {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
}
.btn-secondary {
  background: #f1f5f9;
  color: #1e40af;
}
.btn-secondary:hover {
  background: #e2e8f0;
}
.btn-danger {
  background: #ef4444;
  color: white;
}
.btn-danger:hover {
  background: #b91c1c;
}
.empty-table {
  text-align: center;
  color: #64748b;
  padding: 2rem 0;
}
/* Modal estilos */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}
.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  padding: 2rem;
  position: relative;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}
.close-modal {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(0,0,0,0.1);
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
  transition: background 0.2s;
  z-index: 10;
}
.close-modal:hover {
  background: rgba(0,0,0,0.2);
}
.modal-body {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.modal-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 1rem;
}
.form-group {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
}
.form-group label {
  font-weight: 600;
  color: #64748b;
  margin-bottom: 0.3rem;
}
.form-input {
  padding: 0.7rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
}
.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}
.form-error {
  color: #ef4444;
  margin-top: 0.5rem;
  font-size: 0.98rem;
}
.categories-list {
  margin-top: 2rem;
}
.categories-list ul {
  list-style: none;
  padding: 0;
}
.categories-list li {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}
.categories-list button {
  margin: 0;
}
.image-input {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
</style> 