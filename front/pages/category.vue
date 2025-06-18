<template>
  <div class="container">
    <h1>Categorías</h1>
    <div class="categories">
      <div v-if="loading">Cargando categorías...</div>
      <div v-else-if="error">{{ error }}</div>
      <div v-else class="category-grid">
        <div v-for="category in categories" :key="category.idCategory" class="category-card">
          <h3>{{ category.name }}</h3>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      categories: [],
      loading: true,
      error: null
    };
  },
  async created() {
    try {
      const response = await axios.get('http://localhost:8080/api2/categories');
      this.categories = response.data;
      this.loading = false;
    } catch (err) {
      this.error = 'Error al cargar las categorías';
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
.categories {
  margin-top: 20px;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}
.category-card {
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 5px;
}
</style> 