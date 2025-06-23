<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>Mi Perfil</h1>
      <p>Gestiona tu información personal y métodos de pago</p>
    </div>

    <div class="profile-content">
      <!-- Información Personal -->
      <div class="profile-section">
        <h2>Información Personal</h2>
        <form @submit.prevent="savePersonalInfo" class="profile-form">
          <div class="form-row">
            <div class="form-group">
              <label for="firstName">Nombre</label>
              <input 
                type="text" 
                id="firstName" 
                v-model="personalInfo.firstName" 
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="lastName">Apellido</label>
              <input 
                type="text" 
                id="lastName" 
                v-model="personalInfo.lastName" 
                required
                class="form-input"
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="email">Correo Electrónico</label>
              <input 
                type="email" 
                id="email" 
                v-model="personalInfo.email" 
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="phone">Teléfono</label>
              <input 
                type="tel" 
                id="phone" 
                v-model="personalInfo.phone" 
                class="form-input"
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="dateOfBirth">Fecha de Nacimiento</label>
            <input 
              type="date" 
              id="dateOfBirth" 
              v-model="personalInfo.dateOfBirth" 
              class="form-input"
            />
          </div>
          
          <button type="submit" class="save-btn" :disabled="savingPersonal">
            {{ savingPersonal ? 'Guardando...' : 'Guardar Información Personal' }}
          </button>
        </form>
      </div>

      <!-- Dirección de Envío -->
      <div class="profile-section">
        <h2>Dirección de Envío</h2>
        <form @submit.prevent="saveShippingAddress" class="profile-form">
          <div class="form-group">
            <label for="street">Dirección</label>
            <input 
              type="text" 
              id="street" 
              v-model="shippingAddress.street" 
              required
              class="form-input"
              placeholder="Calle y número"
            />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="city">Ciudad</label>
              <input 
                type="text" 
                id="city" 
                v-model="shippingAddress.city" 
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="state">Departamento</label>
              <input 
                type="text" 
                id="state" 
                v-model="shippingAddress.state" 
                required
                class="form-input"
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="zipCode">Código Postal</label>
              <input 
                type="text" 
                id="zipCode" 
                v-model="shippingAddress.zipCode" 
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label for="country">País</label>
              <input 
                type="text" 
                id="country" 
                v-model="shippingAddress.country" 
                required
                class="form-input"
                value="Guatemala"
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="additionalInfo">Información Adicional</label>
            <textarea 
              id="additionalInfo" 
              v-model="shippingAddress.additionalInfo" 
              class="form-textarea"
              placeholder="Referencias, instrucciones especiales, etc."
              rows="3"
            ></textarea>
          </div>
          
          <button type="submit" class="save-btn" :disabled="savingAddress">
            {{ savingAddress ? 'Guardando...' : 'Guardar Dirección' }}
          </button>
        </form>
      </div>

      <!-- Métodos de Pago -->
      <div class="profile-section">
        <h2>Métodos de Pago</h2>
        
        <!-- Tarjetas guardadas -->
        <div v-if="savedCards.length > 0" class="saved-cards">
          <h3>Tarjetas Guardadas</h3>
          <div class="card-list">
            <div v-for="card in savedCards" :key="card.id" class="card-item">
              <div class="card-info">
                <div class="card-number">•••• •••• •••• {{ card.lastFour }}</div>
                <div class="card-details">
                  <span class="card-brand">{{ card.brand }}</span>
                  <span class="card-expiry">Expira: {{ card.expiryMonth }}/{{ card.expiryYear }}</span>
                </div>
              </div>
              <button @click="deleteCard(card.id)" class="delete-card-btn">Eliminar</button>
            </div>
          </div>
        </div>

        <!-- Agregar nueva tarjeta -->
        <form @submit.prevent="saveCard" class="profile-form">
          <h3>Agregar Nueva Tarjeta</h3>
          
          <div class="form-group">
            <label for="cardNumber">Número de Tarjeta</label>
            <input 
              type="text" 
              id="cardNumber" 
              v-model="newCard.number" 
              required
              class="form-input"
              placeholder="1234 5678 9012 3456"
              maxlength="19"
              @input="formatCardNumber"
            />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="cardName">Nombre en la Tarjeta</label>
              <input 
                type="text" 
                id="cardName" 
                v-model="newCard.name" 
                required
                class="form-input"
                placeholder="Como aparece en la tarjeta"
              />
            </div>
            <div class="form-group">
              <label for="cardCvv">CVV</label>
              <input 
                type="text" 
                id="cardCvv" 
                v-model="newCard.cvv" 
                required
                class="form-input"
                placeholder="123"
                maxlength="4"
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="expiryMonth">Mes de Expiración</label>
              <select id="expiryMonth" v-model="newCard.expiryMonth" required class="form-input">
                <option value="">Mes</option>
                <option v-for="month in 12" :key="month" :value="month.toString().padStart(2, '0')">
                  {{ month.toString().padStart(2, '0') }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="expiryYear">Año de Expiración</label>
              <select id="expiryYear" v-model="newCard.expiryYear" required class="form-input">
                <option value="">Año</option>
                <option v-for="year in expiryYears" :key="year" :value="year">
                  {{ year }}
                </option>
              </select>
            </div>
          </div>
          
          <div class="form-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="newCard.isDefault"
                class="checkbox-input"
              />
              <span class="checkbox-text">Establecer como tarjeta predeterminada</span>
            </label>
          </div>
          
          <button type="submit" class="save-btn" :disabled="savingCard">
            {{ savingCard ? 'Guardando...' : 'Guardar Tarjeta' }}
          </button>
        </form>
      </div>

      <!-- Cambiar Contraseña -->
      <div class="profile-section">
        <h2>Cambiar Contraseña</h2>
        <form @submit.prevent="changePassword" class="profile-form">
          <div class="form-group">
            <label for="currentPassword">Contraseña Actual</label>
            <input 
              type="password" 
              id="currentPassword" 
              v-model="passwordChange.currentPassword" 
              required
              class="form-input"
            />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="newPassword">Nueva Contraseña</label>
              <input 
                type="password" 
                id="newPassword" 
                v-model="passwordChange.newPassword" 
                required
                class="form-input"
                minlength="8"
              />
            </div>
            <div class="form-group">
              <label for="confirmPassword">Confirmar Nueva Contraseña</label>
              <input 
                type="password" 
                id="confirmPassword" 
                v-model="passwordChange.confirmPassword" 
                required
                class="form-input"
                minlength="8"
              />
            </div>
          </div>
          
          <button type="submit" class="save-btn" :disabled="changingPassword">
            {{ changingPassword ? 'Cambiando...' : 'Cambiar Contraseña' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '@/stores/userStore';
import ApiService from '../services/ApiService';
import axios from 'axios';

const userStore = useUserStore();

// Estado reactivo
const savingPersonal = ref(false);
const savingAddress = ref(false);
const savingCard = ref(false);
const changingPassword = ref(false);

// Información personal
const personalInfo = ref({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  dateOfBirth: ''
});

// Dirección de envío
const shippingAddress = ref({
  street: '',
  city: '',
  state: '',
  zipCode: '',
  country: 'Guatemala',
  additionalInfo: ''
});

// Tarjetas guardadas
const savedCards = ref([]);

// Nueva tarjeta
const newCard = ref({
  number: '',
  name: '',
  cvv: '',
  expiryMonth: '',
  expiryYear: '',
  isDefault: false
});

// Cambio de contraseña
const passwordChange = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// Años de expiración (próximos 10 años)
const expiryYears = computed(() => {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: 10 }, (_, i) => currentYear + i);
});

// Cargar información del usuario
const loadUserInfo = async () => {
  try {
    const user = userStore.getUser();
    if (!user || !user.idUser) {
      alert('Debes iniciar sesión para ver tu perfil.');
      return;
    }

    // Cargar información personal
    const userResponse = await axios.get(ApiService.getPharmacyApiUrl(`/users/${user.idUser}`));
    const userData = userResponse.data;
    
    personalInfo.value = {
      firstName: userData.firstName || userData.name || '',
      lastName: userData.lastName || '',
      email: userData.email || '',
      phone: userData.phone || '',
      dateOfBirth: userData.dateOfBirth || ''
    };

    // Cargar dirección de envío (si existe)
    try {
      const addressResponse = await axios.get(ApiService.getPharmacyApiUrl(`/users/${user.idUser}/address`));
      const addressData = addressResponse.data;
      if (addressData) {
        shippingAddress.value = { ...shippingAddress.value, ...addressData };
      }
    } catch (error) {
      console.log('No se encontró dirección guardada');
    }

    // Cargar tarjetas guardadas
    try {
      const cardsResponse = await axios.get(ApiService.getPharmacyApiUrl(`/users/${user.idUser}/cards`));
      savedCards.value = cardsResponse.data || [];
    } catch (error) {
      console.log('No se encontraron tarjetas guardadas');
      savedCards.value = [];
    }

  } catch (error) {
    console.error('Error loading user info:', error);
    alert('Error al cargar la información del usuario.');
  }
};

// Guardar información personal
const savePersonalInfo = async () => {
  savingPersonal.value = true;
  try {
    const user = userStore.getUser();
    await axios.put(ApiService.getPharmacyApiUrl(`/users/${user.idUser}`), {
      firstName: personalInfo.value.firstName,
      lastName: personalInfo.value.lastName,
      email: personalInfo.value.email,
      phone: personalInfo.value.phone,
      dateOfBirth: personalInfo.value.dateOfBirth
    });
    
    alert('Información personal guardada exitosamente.');
  } catch (error) {
    console.error('Error saving personal info:', error);
    alert('Error al guardar la información personal.');
  } finally {
    savingPersonal.value = false;
  }
};

// Guardar dirección de envío
const saveShippingAddress = async () => {
  savingAddress.value = true;
  try {
    const user = userStore.getUser();
    await axios.post(ApiService.getPharmacyApiUrl(`/users/${user.idUser}/address`), shippingAddress.value);
    
    alert('Dirección guardada exitosamente.');
  } catch (error) {
    console.error('Error saving address:', error);
    alert('Error al guardar la dirección.');
  } finally {
    savingAddress.value = false;
  }
};

// Formatear número de tarjeta
const formatCardNumber = (event) => {
  let value = event.target.value.replace(/\D/g, '');
  value = value.replace(/(\d{4})(?=\d)/g, '$1 ');
  newCard.value.number = value;
};

// Guardar nueva tarjeta
const saveCard = async () => {
  if (passwordChange.value.newPassword !== passwordChange.value.confirmPassword) {
    alert('Las contraseñas no coinciden.');
    return;
  }

  savingCard.value = true;
  try {
    const user = userStore.getUser();
    const cardData = {
      ...newCard.value,
      lastFour: newCard.value.number.slice(-4),
      brand: getCardBrand(newCard.value.number)
    };
    
    await axios.post(ApiService.getPharmacyApiUrl(`/users/${user.idUser}/cards`), cardData);
    
    // Limpiar formulario
    newCard.value = {
      number: '',
      name: '',
      cvv: '',
      expiryMonth: '',
      expiryYear: '',
      isDefault: false
    };
    
    // Recargar tarjetas
    await loadUserInfo();
    
    alert('Tarjeta guardada exitosamente.');
  } catch (error) {
    console.error('Error saving card:', error);
    alert('Error al guardar la tarjeta.');
  } finally {
    savingCard.value = false;
  }
};

// Eliminar tarjeta
const deleteCard = async (cardId) => {
  if (!confirm('¿Estás seguro de que quieres eliminar esta tarjeta?')) {
    return;
  }

  try {
    const user = userStore.getUser();
    await axios.delete(ApiService.getPharmacyApiUrl(`/users/${user.idUser}/cards/${cardId}`));
    
    // Recargar tarjetas
    await loadUserInfo();
    
    alert('Tarjeta eliminada exitosamente.');
  } catch (error) {
    console.error('Error deleting card:', error);
    alert('Error al eliminar la tarjeta.');
  }
};

// Cambiar contraseña
const changePassword = async () => {
  if (passwordChange.value.newPassword !== passwordChange.value.confirmPassword) {
    alert('Las contraseñas no coinciden.');
    return;
  }

  if (passwordChange.value.newPassword.length < 8) {
    alert('La nueva contraseña debe tener al menos 8 caracteres.');
    return;
  }

  changingPassword.value = true;
  try {
    const user = userStore.getUser();
    await axios.put(ApiService.getPharmacyApiUrl(`/users/${user.idUser}/password`), {
      currentPassword: passwordChange.value.currentPassword,
      newPassword: passwordChange.value.newPassword
    });
    
    // Limpiar formulario
    passwordChange.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
    
    alert('Contraseña cambiada exitosamente.');
  } catch (error) {
    console.error('Error changing password:', error);
    alert('Error al cambiar la contraseña. Verifica que la contraseña actual sea correcta.');
  } finally {
    changingPassword.value = false;
  }
};

// Detectar marca de tarjeta
const getCardBrand = (number) => {
  const cleanNumber = number.replace(/\s/g, '');
  
  if (/^4/.test(cleanNumber)) return 'Visa';
  if (/^5[1-5]/.test(cleanNumber)) return 'Mastercard';
  if (/^3[47]/.test(cleanNumber)) return 'American Express';
  if (/^6/.test(cleanNumber)) return 'Discover';
  
  return 'Tarjeta';
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8fafc;
  min-height: 100vh;
}

.profile-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.profile-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.profile-header p {
  font-size: 1.1rem;
  opacity: 0.9;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.profile-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.profile-section h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e2e8f0;
}

.profile-section h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.form-input, .form-textarea {
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.save-btn {
  padding: 0.75rem 1.5rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.save-btn:hover:not(:disabled) {
  background: #2563eb;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Tarjetas guardadas */
.saved-cards {
  margin-bottom: 2rem;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.card-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.card-number {
  font-weight: 600;
  color: #1e293b;
  font-size: 1rem;
}

.card-details {
  display: flex;
  gap: 1rem;
  font-size: 0.8rem;
  color: #64748b;
}

.card-brand {
  font-weight: 500;
}

.delete-card-btn {
  padding: 0.5rem 1rem;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.delete-card-btn:hover {
  background: #b91c1c;
}

/* Checkbox personalizado */
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  accent-color: #3b82f6;
}

.checkbox-text {
  font-size: 0.9rem;
  color: #374151;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 1rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .card-item {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .card-details {
    flex-direction: column;
    gap: 0.25rem;
  }
}
</style> 