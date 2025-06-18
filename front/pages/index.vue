<script setup lang="ts">
import axios from 'axios';

interface Product {
  idMedicine: number;
  name: string;
  description: string;
  price: number;
}

const featuredProducts = ref<Product[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

async function fetchFeaturedProducts() {
  try {
    const response = await axios.get('http://localhost:8080/api2/medicines');
    featuredProducts.value = response.data;
    loading.value = false;
  } catch (err) {
    error.value = 'Error al cargar los productos destacados';
    loading.value = false;
  }
}

onMounted(() => {
  fetchFeaturedProducts();
});
</script>

<template>
  <main class="bg-image-[url('/carCrash.jpg')]">
    <Carrousel />
    <div class="container">
      <h1>Bienvenido a nuestra tienda</h1>
      <div class="featured-products">
        <h2>Productos Destacados</h2>
        <div v-if="loading">Cargando productos...</div>
        <div v-else-if="error">{{ error }}</div>
        <div v-else class="product-grid">
          <div v-for="product in featuredProducts" :key="product.idMedicine" class="product-card">
            <h3>{{ product.name }}</h3>
            <p>{{ product.description }}</p>
            <p>Precio: ${{ product.price }}</p>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.featured-products {
  margin-top: 20px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}
.product-card {
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 5px;
}
</style>
