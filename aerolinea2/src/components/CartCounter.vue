<template>
  <div class="cart-counter">
    <a href="/cart" class="cart-link">
      🛒 Carrito
      <span v-if="itemCount > 0" class="cart-badge">{{ itemCount }}</span>
    </a>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getCartItemCount } from '../utils/cart-utils'

const itemCount = ref(0)

// Función para actualizar el contador
const updateCounter = () => {
  itemCount.value = getCartItemCount()
}

// Escuchar cambios en localStorage
const handleStorageChange = (e: StorageEvent) => {
  if (e.key === 'flight_cart') {
    updateCounter()
  }
}

onMounted(() => {
  updateCounter()
  window.addEventListener('storage', handleStorageChange)
})

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
})
</script>

<style scoped>
.cart-counter {
  position: relative;
}

.cart-link {
  text-decoration: none;
  color: #4a5568;
  font-weight: 500;
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cart-link:hover {
  color: #667eea;
}

.cart-badge {
  background: #e53e3e;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}
</style>

