<template>
  <div class="user-settings">
    <div class="settings-header">
      <h3>⚙️ Configuraciones</h3>
      <p>Personaliza tu experiencia en el sistema</p>
    </div>

    <form @submit.prevent="handleSubmit" class="settings-form">
      <!-- Notificaciones -->
      <div class="settings-section">
        <h4>🔔 Notificaciones</h4>
        
        <div class="settings-group">
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="form.notifications.email"
              class="form-checkbox"
            />
            <span>Notificaciones por Email</span>
          </label>
          
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="form.notifications.push"
              class="form-checkbox"
            />
            <span>Notificaciones Push (si están disponibles)</span>
          </label>
          
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="form.notifications.sms"
              class="form-checkbox"
            />
            <span>Notificaciones por SMS</span>
          </label>
        </div>
      </div>

      <!-- Preferencias de Idioma -->
      <div class="settings-section">
        <h4>🌍 Idioma</h4>
        
        <div class="form-group">
          <label for="language">Idioma preferido:</label>
          <select 
            id="language"
            v-model="form.language" 
            class="form-select"
          >
            <option value="es">Español</option>
            <option value="en">English</option>
            <option value="fr">Français</option>
            <option value="de">Deutsch</option>
          </select>
        </div>
      </div>

      <!-- Tema de Interfaz -->
      <div class="settings-section">
        <h4>🎨 Tema de Interfaz</h4>
        
        <div class="theme-options">
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="form.theme"
              value="light"
              class="form-radio"
            />
            <span class="theme-option">
              <div class="theme-preview light-theme"></div>
              <div class="theme-info">
                <strong>Claro</strong>
                <small>Fondo blanco, texto oscuro</small>
              </div>
            </span>
          </label>
          
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="form.theme"
              value="dark"
              class="form-radio"
            />
            <span class="theme-option">
              <div class="theme-preview dark-theme"></div>
              <div class="theme-info">
                <strong>Oscuro</strong>
                <small>Fondo oscuro, texto claro</small>
              </div>
            </span>
          </label>
          
          <label class="radio-label">
            <input 
              type="radio" 
              v-model="form.theme"
              value="auto"
              class="form-radio"
            />
            <span class="theme-option">
              <div class="theme-preview auto-theme"></div>
              <div class="theme-info">
                <strong>Automático</strong>
                <small>Se adapta al sistema</small>
              </div>
            </span>
          </label>
        </div>
      </div>

      <!-- Configuración de Privacidad -->
      <div class="settings-section">
        <h4>🔒 Privacidad</h4>
        
        <div class="settings-group">
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="form.privacy.shareProfile"
              class="form-checkbox"
            />
            <span>Permitir que otros usuarios vean mi perfil público</span>
          </label>
          
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="form.privacy.showActivity"
              class="form-checkbox"
            />
            <span>Mostrar mi actividad reciente en el sistema</span>
          </label>
          
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="form.privacy.analytics"
              class="form-checkbox"
            />
            <span>Permitir análisis de uso para mejorar el servicio</span>
          </label>
        </div>
      </div>

      <!-- Configuraciones Avanzadas -->
      <div class="settings-section">
        <h4>🔧 Configuraciones Avanzadas</h4>
        
        <div class="form-group">
          <label for="timezone">Zona horaria:</label>
          <select 
            id="timezone"
            v-model="form.timezone" 
            class="form-select"
          >
            <option value="America/Guatemala_City">Guatemala (GMT-6)</option>
            <option value="America/Mexico_City">México (GMT-6)</option>
            <option value="America/New_York">Nueva York (GMT-5)</option>
            <option value="America/Los_Angeles">Los Ángeles (GMT-8)</option>
            <option value="Europe/Madrid">Madrid (GMT+1)</option>
            <option value="Europe/London">Londres (GMT+0)</option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="dateFormat">Formato de fecha:</label>
          <select 
            id="dateFormat"
            v-model="form.dateFormat" 
            class="form-select"
          >
            <option value="DD/MM/YYYY">DD/MM/YYYY</option>
            <option value="MM/DD/YYYY">MM/DD/YYYY</option>
            <option value="YYYY-MM-DD">YYYY-MM-DD</option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="currency">Moneda preferida:</label>
          <select 
            id="currency"
            v-model="form.currency" 
            class="form-select"
          >
            <option value="GTQ">Quetzal (GTQ)</option>
            <option value="USD">Dólar Estadounidense (USD)</option>
            <option value="EUR">Euro (EUR)</option>
            <option value="MXN">Peso Mexicano (MXN)</option>
          </select>
        </div>
      </div>

      <!-- Botones de Acción -->
      <div class="form-actions">
        <button 
          type="button" 
          @click="resetSettings"
          class="btn-secondary"
          :disabled="saving"
        >
          🔄 Restablecer
        </button>
        
        <button 
          type="submit" 
          class="btn-primary"
          :disabled="saving"
        >
          {{ saving ? '💾 Guardando...' : '💾 Guardar Configuraciones' }}
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
  settings: any
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  settingsUpdated: [settings: any]
}>()

// State
const saving = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error' | 'info'>('info')

// Form data
const form = ref({
  notifications: {
    email: true,
    push: false,
    sms: false
  },
  language: 'es',
  theme: 'light',
  privacy: {
    shareProfile: false,
    showActivity: true,
    analytics: true
  },
  timezone: 'America/Guatemala_City',
  dateFormat: 'DD/MM/YYYY',
  currency: 'GTQ'
})

// Computed
const hasChanges = computed(() => {
  if (!props.settings) return false
  
  return JSON.stringify(form.value) !== JSON.stringify(props.settings)
})

// Methods
const loadSettings = () => {
  if (!props.settings) return
  
  form.value = { ...props.settings }
}

const handleSubmit = async () => {
  try {
    saving.value = true
    message.value = ''
    
    // Simular guardado (después se conectará con la API)
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Emitir evento
    emit('settingsUpdated', form.value)
    
    // Mostrar mensaje de éxito
    showMessage('Configuraciones guardadas exitosamente', 'success')
    
  } catch (error) {
    console.error('Error guardando configuraciones:', error)
    showMessage('Error al guardar las configuraciones', 'error')
  } finally {
    saving.value = false
  }
}

const resetSettings = () => {
  loadSettings()
  showMessage('Configuraciones restablecidas', 'info')
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
watch(() => props.settings, loadSettings, { immediate: true })

// Lifecycle
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.user-settings {
  max-width: 800px;
  margin: 0 auto;
}

.settings-header {
  text-align: center;
  margin-bottom: 2rem;
}

.settings-header h3 {
  font-size: 1.5rem;
  color: white;
  margin-bottom: 0.5rem;
}

.settings-header p {
  color: rgba(255, 255, 255, 0.7);
}

.settings-form {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  padding: 2rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.settings-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.settings-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.settings-section h4 {
  color: #48bb78;
  font-size: 1.2rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.settings-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
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

.form-select:focus {
  outline: none;
  border-color: #48bb78;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

.checkbox-label,
.radio-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  color: white;
  font-size: 0.9rem;
  padding: 0.5rem;
  border-radius: 0.5rem;
  transition: background-color 0.3s ease;
}

.checkbox-label:hover,
.radio-label:hover {
  background: rgba(255, 255, 255, 0.05);
}

.form-checkbox,
.form-radio {
  width: 18px;
  height: 18px;
  accent-color: #48bb78;
}

.theme-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid transparent;
  border-radius: 0.5rem;
  transition: all 0.3s ease;
}

.radio-label:has(input:checked) .theme-option {
  border-color: #48bb78;
  background: rgba(72, 187, 120, 0.1);
}

.theme-preview {
  width: 40px;
  height: 40px;
  border-radius: 0.5rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.light-theme {
  background: linear-gradient(45deg, #ffffff 50%, #f3f4f6 50%);
}

.dark-theme {
  background: linear-gradient(45deg, #1f2937 50%, #374151 50%);
}

.auto-theme {
  background: linear-gradient(45deg, #3b82f6 50%, #60a5fa 50%);
}

.theme-info {
  display: flex;
  flex-direction: column;
}

.theme-info strong {
  color: white;
  font-size: 0.9rem;
}

.theme-info small {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
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
  .theme-options {
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
