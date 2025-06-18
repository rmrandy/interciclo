<template>
  <div class="container">
    <h1>Detalle del Producto</h1>
    <div v-if="loading">Cargando detalle del producto...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else class="product-detail">
      <h2>{{ product.name }}</h2>
      <p>{{ product.description }}</p>
      <p>Precio: ${{ product.price }}</p>
      <p>Stock: {{ product.stock }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      product: null,
      loading: true,
      error: null
    };
  },
  async created() {
    const productId = this.$route.params.id;
    try {
      const response = await axios.get(`http://localhost:8080/api2/medicines/${productId}`);
      this.product = response.data;
      this.loading = false;
    } catch (err) {
      this.error = 'Error al cargar el detalle del producto';
      this.loading = false;
    }
  }
};
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.product-detail {
  margin-top: 20px;
  border: 1px solid #ccc;
  padding: 20px;
  border-radius: 5px;
}
</style> 