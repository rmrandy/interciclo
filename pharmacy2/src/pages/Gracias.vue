<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="thank-you-container">
    <div class="thank-you-card">
      <div class="card-icon">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
      </div>
      <h1 class="card-title">¡Gracias por tu compra!</h1>
      <p class="card-message">
        Tu pedido ha sido procesado exitosamente. Recibirás una confirmación por correo electrónico en breve.
      </p>
      <div class="order-details">
        <p><strong>Número de Pedido:</strong> #{{ orderId }}</p>
        <p><strong>Fecha del Pedido:</strong> {{ new Date().toLocaleDateString() }}</p>
      </div>
      <router-link to="/catalogo" class="back-to-shop-btn">
        Seguir Comprando
      </router-link>
      <router-link to="/perfil" class="view-orders-btn">
        Ver mis pedidos
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router';

const route = useRoute();
const orderId = route.query.orderId || 'N/A';
</script>

<style scoped>
.thank-you-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  background-color: #f4f7f6;
  padding: 2rem;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.thank-you-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  text-align: center;
  max-width: 500px;
  width: 100%;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  background-color: #28a745;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon svg {
  width: 40px;
  height: 40px;
  color: white;
}

.card-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 1rem;
}

.card-message {
  font-size: 1rem;
  color: #666;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.order-details {
  background-color: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 2rem;
  text-align: left;
}

.order-details p {
  margin: 0.5rem 0;
  color: #555;
}

.back-to-shop-btn, .view-orders-btn {
  display: inline-block;
  text-decoration: none;
  border-radius: 8px;
  padding: 0.8rem 1.5rem;
  font-weight: 600;
  transition: all 0.3s ease;
  margin: 0.5rem;
}

.back-to-shop-btn {
  background-color: #007bff;
  color: white;
}

.back-to-shop-btn:hover {
  background-color: #0056b3;
  transform: translateY(-2px);
}

.view-orders-btn {
  background-color: transparent;
  color: #007bff;
  border: 2px solid #007bff;
}

.view-orders-btn:hover {
  background-color: #007bff;
  color: white;
}
</style> 