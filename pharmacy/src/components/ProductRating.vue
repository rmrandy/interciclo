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
      v-if="!showRatingForm && isUserLoggedIn && !userHasRated" 
      @click="showRatingForm = true"
      class="rate-product-btn"
    >
      Calificar Producto
    </button>

    <!-- Mensaje si el usuario ya calificó -->
    <div v-if="isUserLoggedIn && userHasRated" class="already-rated">
      <i class="fas fa-check-circle"></i>
      Ya calificaste este producto
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, defineProps, defineEmits } from 'vue';
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
  },
  readOnly: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['rating-updated']);

const userStore = useUserStore();
const showRatingForm = ref(false);
const userRating = ref(0);
const hoverRating = ref(0);
const userComment = ref('');
const submitting = ref(false);
const userHasRated = ref(false);

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
    
    // Crear el payload para el endpoint de comentarios
    const commentData = {
      user: user,
      commentText: userComment.value || `Calificación: ${userRating.value} estrellas`,
      rating: userRating.value,
      medicine: { idMedicine: Number(props.productId) }
    };

    // Usar el endpoint correcto de comentarios
    const response = await axios.post(ApiService.getPharmacyApiUrl("/comments"), commentData);
    
    if (response.status === 201) {
      console.log('Rating submitted successfully:', response.data);
      
      // Limpiar formulario
      userRating.value = 0;
      userComment.value = '';
      showRatingForm.value = false;
      userHasRated.value = true;
      
      // Emitir evento para actualizar el rating
      emit('rating-updated');
      
      alert('¡Gracias por tu calificación!');
    } else {
      throw new Error('Error en la respuesta del servidor');
    }
  } catch (error) {
    console.error('Error submitting rating:', error);
    alert('Error al enviar la calificación. Por favor intenta de nuevo.');
  } finally {
    submitting.value = false;
  }
};

const checkUserRating = async () => {
  if (!isUserLoggedIn.value) return;
  
  try {
    const user = userStore.getUser();
    const response = await axios.get(ApiService.getPharmacyApiUrl("/comments"));
    const comments = response.data;
    
    // Verificar si el usuario ya calificó este producto
    const userRating = comments.find(comment => 
      comment.user && comment.user.idUser === user.idUser &&
      comment.medicine && Number(comment.medicine.idMedicine) === Number(props.productId) &&
      comment.rating !== null
    );
    
    userHasRated.value = !!userRating;
  } catch (error) {
    console.error('Error checking user rating:', error);
  }
};

onMounted(() => {
  checkUserRating();
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

.already-rated {
  color: #64748b;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background-color: #f1f5f9;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.already-rated i {
  color: #10b981;
}
</style> 