<template>
  <div class="user-profile-form">
    <div class="form-header">
      <h3>📝 Editar Perfil</h3>
      <p>Actualiza tu información personal y preferencias de viaje</p>
    </div>

    <form @submit.prevent="handleSubmit" class="profile-form">
      <!-- Información Personal -->
      <div class="form-section">
        <h4>👤 Información Personal</h4>
        
        <div class="form-row">
          <div class="form-group">
            <label for="firstName">Nombre *</label>
            <input 
              id="firstName"
              v-model="form.firstName" 
              type="text" 
              required
              class="form-input"
              placeholder="Tu nombre"
            />
          </div>
          
          <div class="form-group">
            <label for="lastName">Apellido *</label>
            <input 
              id="lastName"
              v-model="form.lastName" 
              type="text" 
              required
              class="form-input"
              placeholder="Tu apellido"
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="email">Email *</label>
            <input 
              id="email"
              v-model="form.email" 
              type="email" 
              required
              class="form-input"
              placeholder="tu@email.com"
            />
          </div>
          
          <div class="form-group">
            <label for="phone">Teléfono</label>
            <input 
              id="phone"
              v-model="form.phone" 
              type="tel" 
              class="form-input"
              placeholder="+502 1234 5678"
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="birthDate">Fecha de Nacimiento</label>
            <input 
              id="birthDate"
              v-model="form.birthDate" 
              type="date" 
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="country">País</label>
            <select 
              id="country"
              v-model="form.country" 
              class="form-select"
            >
              <option value="">Seleccionar país</option>
              <option value="GT">Guatemala</option>
              <option value="MX">México</option>
              <option value="US">Estados Unidos</option>
              <option value="ES">España</option>
              <option value="CA">Canadá</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Documentos de Identificación -->
      <div class="form-section">
        <h4>🆔 Documentos de Identificación</h4>
        
        <div class="form-row">
          <div class="form-group">
            <label for="documentType">Tipo de Documento</label>
            <select 
              id="documentType"
              v-model="form.documentType" 
              class="form-select"
            >
              <option value="">Seleccionar tipo</option>
              <option value="PASSPORT">Pasaporte</option>
              <option value="ID_CARD">Cédula de Identidad</option>
              <option value="DRIVER_LICENSE">Licencia de Conducir</option>
            </select>
          </div>
          
          <div class="form-group">
            <label for="documentNumber">Número de Documento</label>
            <input 
              id="documentNumber"
              v-model="form.documentNumber" 
              type="text" 
              class="form-input"
              placeholder="Número del documento"
            />
          </div>
        </div>
      </div>

      <!-- Preferencias de Viaje -->
      <div class="form-section">
        <h4>✈️ Preferencias de Viaje</h4>
        
        <div class="form-row">
          <div class="form-group">
            <label for="preferredSeat">Asiento Preferido</label>
            <select 
              id="preferredSeat"
              v-model="form.preferences.preferredSeat" 
              class="form-select"
            >
              <option value="">Sin preferencia</option>
              <option value="WINDOW">Ventana</option>
              <option value="AISLE">Pasillo</option>
              <option value="FRONT">Frente</option>
              <option value="BACK">Atrás</option>
            </select>
          </div>
          
          <div class="form-group">
            <label for="preferredMeal">Comida Preferida</label>
            <select 
              id="preferredMeal"
              v-model="form.preferences.preferredMeal" 
              class="form-select"
            >
              <option value="">Sin preferencia</option>
              <option value="REGULAR">Regular</option>
              <option value="VEGETARIAN">Vegetariana</option>
              <option value="VEGAN">Vegana</option>
              <option value="GLUTEN_FREE">Sin Gluten</option>
              <option value="HALAL">Halal</option>
              <option value="KOSHER">Kosher</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>Servicios Adicionales</label>
          <div class="checkbox-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="form.preferences.services.extraLegroom"
                class="form-checkbox"
              />
              <span>Asiento con más espacio para piernas</span>
            </label>
            
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="form.preferences.services.priorityBoarding"
                class="form-checkbox"
              />
              <span>Embarque prioritario</span>
            </label>
            
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="form.preferences.services.luggageInsurance"
                class="form-checkbox"
              />
              <span>Seguro de equipaje</span>
            </label>
          </div>
        </div>
      </div>

      <!-- Métodos de Pago Favoritos -->
      <div class="form-section">
        <h4>💳 Métodos de Pago Favoritos</h4>
        
        <div class="form-group">
          <label>Guardar Métodos de Pago</label>
          <div class="checkbox-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="form.preferences.payment.saveCards"
                class="form-checkbox"
              />
              <span>Guardar tarjetas de crédito/débito</span>
            </label>
            
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="form.preferences.payment.saveBankAccounts"
                class="form-checkbox"
              />
              <span>Guardar cuentas bancarias</span>
            </label>
          </div>
        </div>
      </div>

      <!-- Botones de Acción -->
      <div class="form-actions">
        <button 
          type="button" 
          @click="resetForm"
          class="btn-secondary"
          :disabled="saving"
        >
          🔄 Restablecer
        </button>
        
        <button 
          type="submit" 
          class="btn-primary"
          :disabled="saving || !canSubmit"
        >
          {{ saving ? '💾 Guardando...' : '💾 Guardar Cambios' }}
        </button>
      </div>
    </form>

    <!-- Mensajes de Estado -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'

// Props
interface Props {
  user: any
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  profileUpdated: [user: any]
}>()

// State
const saving = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error' | 'info'>('info')

// Form data
const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  birthDate: '',
  country: '',
  documentType: '',
  documentNumber: '',
  preferences: {
    preferredSeat: '',
    preferredMeal: '',
    services: {
      extraLegroom: false,
      priorityBoarding: false,
      luggageInsurance: false
    },
    payment: {
      saveCards: false,
      saveBankAccounts: false
    }
  }
})

// Computed
const canSubmit = computed(() => {
  return form.value.firstName.trim() !== '' &&
         form.value.lastName.trim() !== '' &&
         form.value.email.trim() !== ''
})

const hasChanges = computed(() => {
  if (!props.user) return false
  
  return form.value.firstName !== props.user.firstName ||
         form.value.lastName !== props.user.lastName ||
         form.value.email !== props.user.email ||
         form.value.phone !== (props.user.phone || '') ||
         form.value.birthDate !== (props.user.birthDate || '') ||
         form.value.country !== (props.user.country || '') ||
         form.value.documentType !== (props.user.documentType || '') ||
         form.value.documentNumber !== (props.user.documentNumber || props.user.passportNumber || props.user.cui || '')
})

// Methods
const loadUserData = () => {
  if (!props.user) return
  
  form.value = {
    firstName: props.user.firstName || props.user.name || '',
    lastName: props.user.lastName || props.user.surname || '',
    email: props.user.email || '',
    phone: props.user.phone || props.user.telephone || '',
    birthDate: props.user.birthDate || props.user.birthdate || '',
    country: props.user.country || '',
    documentType: props.user.documentType || '',
    documentNumber: props.user.documentNumber || props.user.passportNumber || props.user.cui || '',
    preferences: {
      preferredSeat: '',
      preferredMeal: '',
      services: {
        extraLegroom: false,
        priorityBoarding: false,
        luggageInsurance: false
      },
      payment: {
        saveCards: false,
        saveBankAccounts: false
      }
    }
  }
}

const handleSubmit = async () => {
  try {
    saving.value = true
    message.value = ''
    
    // Simular guardado (después se conectará con la API)
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Preparar datos actualizados
    const updatedUser = {
      ...props.user,
      ...form.value
    }
    
    // Emitir evento
    emit('profileUpdated', updatedUser)
    
    // Mostrar mensaje de éxito
    showMessage('Perfil actualizado exitosamente', 'success')
    
  } catch (error) {
    console.error('Error guardando perfil:', error)
    showMessage('Error al guardar el perfil', 'error')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  loadUserData()
  showMessage('Formulario restablecido', 'info')
}

const showMessage = (msg: string, type: 'success' | 'error' | 'info') => {
  message.value = msg
  messageType.value = type
  
  // Auto-hide success messages
  if (type === 'success') {
    setTimeout(() => {
      message.value = ''
    }, 3000)
  }
}

// Watchers
watch(() => props.user, loadUserData, { immediate: true })

// Lifecycle
onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.user-profile-form {
  max-width: 800px;
  margin: 0 auto;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-header h3 {
  font-size: 1.5rem;
  color: white;
  margin-bottom: 0.5rem;
}

.form-header p {
  color: rgba(255, 255, 255, 0.7);
}

.profile-form {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  padding: 2rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.form-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.form-section h4 {
  color: #48bb78;
  font-size: 1.2rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  color: white;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-input,
.form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #48bb78;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  color: white;
  font-size: 0.9rem;
}

.form-checkbox {
  width: 18px;
  height: 18px;
  accent-color: #48bb78;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-primary,
.btn-secondary {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 150px;
}

.btn-primary {
  background: #48bb78;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #38a169;
  transform: translateY(-2px);
}

.btn-primary:disabled {
  background: #a0aec0;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
}

.message {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 0.5rem;
  text-align: center;
  font-weight: 500;
}

.message.success {
  background: rgba(72, 187, 120, 0.2);
  color: #48bb78;
  border: 1px solid rgba(72, 187, 120, 0.3);
}

.message.error {
  background: rgba(245, 101, 101, 0.2);
  color: #f56565;
  border: 1px solid rgba(245, 101, 101, 0.3);
}

.message.info {
  background: rgba(66, 153, 225, 0.2);
  color: #4299e1;
  border: 1px solid rgba(66, 153, 225, 0.3);
}

/* Responsive */
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
    max-width: 300px;
  }
}
</style>
