<template>
  <div class="rating-component">
    <!-- Mostrar rating existente -->
    <div class="rating-display">
      <div class="stars">
        <span 
          v-for="star in 5" 
          :key="star" 
          class="star" 
          :class="{ filled: star <= (averageRating || 0) }"
        >
          {{ star <= (averageRating || 0) ? '★' : '☆' }}
        </span>
      </div>
      <span class="rating-text">
        {{ (averageRating || 0).toFixed(1) }} ({{ ratingCount || 0 }} calificaciones)
      </span>
    </div>

    <!-- Formulario para calificar (solo si el usuario está logueado) -->
    <div v-if="showRatingForm && isUserLoggedIn" class="rating-form">
      <h4>Califica este producto</h4>
      <div class="rating-input">
        <span 
          v-for="star in 5" 
          :key="star" 
          class="star-input" 
          :class="{ filled: star <= userRating }"
          @click="setUserRating(star)"
          @mouseenter="hoverRating = star"
          @mouseleave="hoverRating = 0"
        >
          {{ star <= (hoverRating || userRating) ? '★' : '☆' }}
        </span>
      </div>
      <textarea 
        v-model="userComment" 
        placeholder="Escribe tu opinión sobre este producto (opcional)"
        class="rating-comment"
        rows="3"
      ></textarea>
      <button @click="submitRating" class="submit-rating-btn" :disabled="submitting">
        {{ submitting ? 'Enviando...' : 'Enviar Calificación' }}
      </button>
    </div>

    <!-- Botón para mostrar formulario de calificación -->
    <button 
      v-if="!showRatingForm && isUserLoggedIn" 
      @click="showRatingForm = true"
      class="rate-product-btn"
    >
      Calificar Producto
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/userStore';
import ApiService from '../services/ApiService';
import axios from 'axios';

const props = defineProps({
  productId: {
    type: [String, Number],
    required: true
  },
  averageRating: {
    type: Number,
    default: 0
  },
  ratingCount: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['rating-updated']);

const userStore = useUserStore();
const showRatingForm = ref(false);
const userRating = ref(0);
const hoverRating = ref(0);
const userComment = ref('');
const submitting = ref(false);

const isUserLoggedIn = computed(() => {
  const user = userStore.getUser();
  return user && user.idUser;
});

const setUserRating = (rating) => {
  userRating.value = rating;
};

const submitRating = async () => {
  if (userRating.value === 0) {
    alert('Por favor selecciona una calificación.');
    return;
  }

  submitting.value = true;
  try {
    const user = userStore.getUser();
    const ratingData = {
      productId: props.productId,
      userId: user.idUser,
      rating: userRating.value,
      comment: userComment.value,
      date: new Date().toISOString()
    };

    // Aquí harías la llamada a la API para guardar la calificación
    // await axios.post(ApiService.getPharmacyApiUrl('/ratings'), ratingData);
    
    // Por ahora, simulamos el éxito
    console.log('Rating submitted:', ratingData);
    
    // Limpiar formulario
    userRating.value = 0;
    userComment.value = '';
    showRatingForm.value = false;
    
    // Emitir evento para actualizar el rating
    emit('rating-updated');
    
    alert('¡Gracias por tu calificación!');
  } catch (error) {
    console.error('Error submitting rating:', error);
    alert('Error al enviar la calificación.');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  // Aquí podrías cargar la calificación del usuario si ya calificó este producto
});
</script>

<style scoped>
.rating-component {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stars {
  display: flex;
  gap: 2px;
}

.star {
  color: #e2e8f0;
  font-size: 1.2rem;
  cursor: default;
}

.star.filled {
  color: #fbbf24;
}

.rating-text {
  font-size: 0.9rem;
  color: #64748b;
}

.rating-form {
  background: #f8fafc;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.rating-form h4 {
  margin-bottom: 0.5rem;
  color: #374151;
  font-size: 0.9rem;
}

.rating-input {
  display: flex;
  gap: 4px;
  margin-bottom: 1rem;
}

.star-input {
  color: #e2e8f0;
  font-size: 1.5rem;
  cursor: pointer;
  transition: color 0.2s ease;
}

.star-input:hover,
.star-input.filled {
  color: #fbbf24;
}

.rating-comment {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 0.9rem;
  resize: vertical;
  margin-bottom: 1rem;
}

.submit-rating-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.submit-rating-btn:hover:not(:disabled) {
  background: #2563eb;
}

.submit-rating-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.rate-product-btn {
  background: #fbbf24;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.rate-product-btn:hover {
  background: #f59e0b;
}
</style> 