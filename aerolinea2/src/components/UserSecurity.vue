<template>
  <div class="user-security">
    <div class="security-header">
      <h3>🔐 Seguridad</h3>
      <p>Gestiona la seguridad de tu cuenta</p>
    </div>

    <!-- Cambio de Contraseña -->
    <div class="security-section">
      <h4>🔑 Cambiar Contraseña</h4>
      
      <form @submit.prevent="handlePasswordChange" class="password-form">
        <div class="form-group">
          <label for="currentPassword">Contraseña Actual *</label>
          <input 
            id="currentPassword"
            v-model="passwordForm.currentPassword" 
            type="password" 
            required
            class="form-input"
            placeholder="Ingresa tu contraseña actual"
          />
        </div>
        
        <div class="form-group">
          <label for="newPassword">Nueva Contraseña *</label>
          <input 
            id="newPassword"
            v-model="passwordForm.newPassword" 
            type="password" 
            required
            class="form-input"
            placeholder="Ingresa tu nueva contraseña"
            :class="{ 'password-weak': passwordStrength === 'weak', 'password-medium': passwordStrength === 'medium', 'password-strong': passwordStrength === 'strong' }"
          />
          <div class="password-strength">
            <div class="strength-bar">
              <div 
                class="strength-fill"
                :class="strengthClass"
                :style="{ width: strengthWidth }"
              ></div>
            </div>
            <span class="strength-text">{{ strengthText }}</span>
          </div>
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">Confirmar Nueva Contraseña *</label>
          <input 
            id="confirmPassword"
            v-model="passwordForm.confirmPassword" 
            type="password" 
            required
            class="form-input"
            placeholder="Confirma tu nueva contraseña"
            :class="{ 'password-match': passwordsMatch, 'password-mismatch': !passwordsMatch && passwordForm.confirmPassword }"
          />
          <div v-if="passwordForm.confirmPassword" class="password-feedback">
            <span v-if="passwordsMatch" class="feedback-success">✅ Las contraseñas coinciden</span>
            <span v-else class="feedback-error">❌ Las contraseñas no coinciden</span>
          </div>
        </div>
        
        <div class="password-requirements">
          <h5>Requisitos de la contraseña:</h5>
          <ul>
            <li :class="{ 'requirement-met': hasMinLength }">
              {{ hasMinLength ? '✅' : '❌' }} Mínimo 8 caracteres
            </li>
            <li :class="{ 'requirement-met': hasUppercase }">
              {{ hasUppercase ? '✅' : '❌' }} Al menos una mayúscula
            </li>
            <li :class="{ 'requirement-met': hasLowercase }">
              {{ hasLowercase ? '✅' : '❌' }} Al menos una minúscula
            </li>
            <li :class="{ 'requirement-met': hasNumber }">
              {{ hasNumber ? '✅' : '❌' }} Al menos un número
            </li>
            <li :class="{ 'requirement-met': hasSpecialChar }">
              {{ hasSpecialChar ? '✅' : '❌' }} Al menos un carácter especial
            </li>
          </ul>
        </div>
        
        <button 
          type="submit" 
          class="btn-primary"
          :disabled="!canSubmitPassword || changingPassword"
        >
          {{ changingPassword ? '🔄 Cambiando...' : '🔑 Cambiar Contraseña' }}
        </button>
      </form>
    </div>

    <!-- Configuraciones de Seguridad -->
    <div class="security-section">
      <h4>🛡️ Configuraciones de Seguridad</h4>
      
      <div class="security-options">
        <label class="checkbox-label">
          <input 
            type="checkbox" 
            v-model="securitySettings.twoFactorAuth"
            class="form-checkbox"
          />
          <span>Autenticación en dos pasos (2FA)</span>
          <small class="option-description">Recibirás un código por SMS o email para verificar tu identidad</small>
        </label>
        
        <label class="checkbox-label">
          <input 
            type="checkbox" 
            v-model="securitySettings.loginNotifications"
            class="form-checkbox"
          />
          <span>Notificaciones de inicio de sesión</span>
          <small class="option-description">Recibirás un email cada vez que inicies sesión desde un dispositivo nuevo</small>
        </label>
        
        <label class="checkbox-label">
          <input 
            type="checkbox" 
            v-model="securitySettings.sessionTimeout"
            class="form-checkbox"
          />
          <span>Timeout de sesión automático</span>
          <small class="option-description">Tu sesión se cerrará automáticamente después de 30 minutos de inactividad</small>
        </label>
      </div>
      
      <button 
        @click="saveSecuritySettings"
        class="btn-secondary"
        :disabled="savingSettings"
      >
        {{ savingSettings ? '💾 Guardando...' : '💾 Guardar Configuraciones' }}
      </button>
    </div>

    <!-- Sesiones Activas -->
    <div class="security-section">
      <h4>💻 Sesiones Activas</h4>
      
      <div v-if="activeSessions.length === 0" class="no-sessions">
        <p>No hay sesiones activas para mostrar</p>
      </div>
      
      <div v-else class="sessions-list">
        <div 
          v-for="session in activeSessions" 
          :key="session.id"
          class="session-item"
        >
          <div class="session-info">
            <div class="session-device">
              <span class="device-icon">{{ getDeviceIcon(session.deviceType) }}</span>
              <div class="device-details">
                <strong>{{ session.deviceName }}</strong>
                <small>{{ session.browser }} en {{ session.os }}</small>
              </div>
            </div>
            
            <div class="session-details">
              <div class="session-location">
                <span class="location-icon">📍</span>
                {{ session.location }}
              </div>
              <div class="session-time">
                Última actividad: {{ formatTime(session.lastActivity) }}
              </div>
            </div>
          </div>
          
          <div class="session-actions">
            <button 
              @click="terminateSession(session.id)"
              class="btn-terminate"
              :disabled="terminatingSession === session.id"
            >
              {{ terminatingSession === session.id ? '🔄' : '❌' }} Terminar
            </button>
          </div>
        </div>
      </div>
      
      <button 
        @click="terminateAllSessions"
        class="btn-danger"
        :disabled="terminatingAllSessions"
      >
        {{ terminatingAllSessions ? '🔄 Terminando...' : '🚫 Terminar Todas las Sesiones' }}
      </button>
    </div>

    <!-- Mensajes de Estado -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

// Emits
const emit = defineEmits<{
  passwordChanged: []
}>()

// State
const changingPassword = ref(false)
const savingSettings = ref(false)
const terminatingSession = ref<string | null>(null)
const terminatingAllSessions = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error' | 'info'>('info')

// Password form
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// Security settings
const securitySettings = ref({
  twoFactorAuth: false,
  loginNotifications: true,
  sessionTimeout: true
})

// Active sessions (simulado)
const activeSessions = ref([
  {
    id: '1',
    deviceType: 'desktop',
    deviceName: 'Mi PC de Escritorio',
    browser: 'Chrome 120.0',
    os: 'Windows 11',
    location: 'Guatemala City, GT',
    lastActivity: new Date().toISOString()
  },
  {
    id: '2',
    deviceType: 'mobile',
    deviceName: 'Mi iPhone',
    browser: 'Safari 17.0',
    os: 'iOS 17.0',
    location: 'Guatemala City, GT',
    lastActivity: new Date(Date.now() - 3600000).toISOString()
  }
])

// Computed
const passwordStrength = computed(() => {
  const password = passwordForm.value.newPassword
  if (!password) return 'none'
  
  let score = 0
  
  if (password.length >= 8) score++
  if (/[A-Z]/.test(password)) score++
  if (/[a-z]/.test(password)) score++
  if (/[0-9]/.test(password)) score++
  if (/[^A-Za-z0-9]/.test(password)) score++
  
  if (score <= 2) return 'weak'
  if (score <= 3) return 'medium'
  return 'strong'
})

const strengthClass = computed(() => {
  switch (passwordStrength.value) {
    case 'weak': return 'strength-weak'
    case 'medium': return 'strength-medium'
    case 'strong': return 'strength-strong'
    default: return ''
  }
})

const strengthWidth = computed(() => {
  switch (passwordStrength.value) {
    case 'weak': return '33%'
    case 'medium': return '66%'
    case 'strong': return '100%'
    default: return '0%'
  }
})

const strengthText = computed(() => {
  switch (passwordStrength.value) {
    case 'weak': return 'Débil'
    case 'medium': return 'Media'
    case 'strong': return 'Fuerte'
    default: return ''
  }
})

const passwordsMatch = computed(() => {
  return passwordForm.value.newPassword === passwordForm.value.confirmPassword
})

const hasMinLength = computed(() => passwordForm.value.newPassword.length >= 8)
const hasUppercase = computed(() => /[A-Z]/.test(passwordForm.value.newPassword))
const hasLowercase = computed(() => /[a-z]/.test(passwordForm.value.newPassword))
const hasNumber = computed(() => /[0-9]/.test(passwordForm.value.newPassword))
const hasSpecialChar = computed(() => /[^A-Za-z0-9]/.test(passwordForm.value.newPassword))

const canSubmitPassword = computed(() => {
  return passwordForm.value.currentPassword &&
         passwordForm.value.newPassword &&
         passwordForm.value.confirmPassword &&
         passwordsMatch.value &&
         passwordStrength.value !== 'weak'
})

// Methods
const handlePasswordChange = async () => {
  try {
    changingPassword.value = true
    message.value = ''
    
    // Simular cambio de contraseña (después se conectará con la API)
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Emitir evento
    emit('passwordChanged')
    
    // Limpiar formulario
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    
    // Mostrar mensaje de éxito
    showMessage('Contraseña cambiada exitosamente', 'success')
    
  } catch (error) {
    console.error('Error cambiando contraseña:', error)
    showMessage('Error al cambiar la contraseña', 'error')
  } finally {
    changingPassword.value = false
  }
}

const saveSecuritySettings = async () => {
  try {
    savingSettings.value = true
    
    // Simular guardado (después se conectará con la API)
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Guardar en localStorage
    localStorage.setItem('user_security_settings', JSON.stringify(securitySettings.value))
    
    showMessage('Configuraciones de seguridad guardadas', 'success')
    
  } catch (error) {
    console.error('Error guardando configuraciones:', error)
    showMessage('Error al guardar las configuraciones', 'error')
  } finally {
    savingSettings.value = false
  }
}

const terminateSession = async (sessionId: string) => {
  try {
    terminatingSession.value = sessionId
    
    // Simular terminación (después se conectará con la API)
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Remover sesión de la lista
    activeSessions.value = activeSessions.value.filter(s => s.id !== sessionId)
    
    showMessage('Sesión terminada exitosamente', 'success')
    
  } catch (error) {
    console.error('Error terminando sesión:', error)
    showMessage('Error al terminar la sesión', 'error')
  } finally {
    terminatingSession.value = null
  }
}

const terminateAllSessions = async () => {
  if (!confirm('¿Estás seguro de que quieres terminar todas las sesiones activas? Esto te cerrará la sesión en todos los dispositivos.')) {
    return
  }
  
  try {
    terminatingAllSessions.value = true
    
    // Simular terminación (después se conectará con la API)
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Limpiar lista de sesiones
    activeSessions.value = []
    
    showMessage('Todas las sesiones han sido terminadas', 'success')
    
  } catch (error) {
    console.error('Error terminando sesiones:', error)
    showMessage('Error al terminar las sesiones', 'error')
  } finally {
    terminatingAllSessions.value = false
  }
}

const getDeviceIcon = (deviceType: string): string => {
  const icons: Record<string, string> = {
    'desktop': '💻',
    'mobile': '📱',
    'tablet': '📱',
    'default': '🖥️'
  }
  return icons[deviceType] || icons.default
}

const formatTime = (timestamp: string): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60))
  
  if (diffInMinutes < 1) {
    return 'Hace unos segundos'
  } else if (diffInMinutes < 60) {
    return `Hace ${diffInMinutes} minuto${diffInMinutes > 1 ? 's' : ''}`
  } else {
    const diffInHours = Math.floor(diffInMinutes / 60)
    if (diffInHours < 24) {
      return `Hace ${diffInHours} hora${diffInHours > 1 ? 's' : ''}`
    } else {
      return date.toLocaleDateString('es-ES', {
        day: 'numeric',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
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

// Lifecycle
onMounted(() => {
  // Cargar configuraciones guardadas
  const savedSettings = localStorage.getItem('user_security_settings')
  if (savedSettings) {
    try {
      securitySettings.value = { ...securitySettings.value, ...JSON.parse(savedSettings) }
    } catch (error) {
      console.error('Error cargando configuraciones de seguridad:', error)
    }
  }
})
</script>

<style scoped>
.user-security {
  max-width: 800px;
  margin: 0 auto;
}

.security-header {
  text-align: center;
  margin-bottom: 2rem;
}

.security-header h3 {
  font-size: 1.5rem;
  color: white;
  margin-bottom: 0.5rem;
}

.security-header p {
  color: rgba(255, 255, 255, 0.7);
}

.security-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.security-section h4 {
  color: #48bb78;
  font-size: 1.2rem;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.password-form {
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  color: white;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #48bb78;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.password-strength {
  margin-top: 0.5rem;
}

.strength-bar {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.25rem;
}

.strength-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.strength-weak {
  background: #f56565;
}

.strength-medium {
  background: #ed8936;
}

.strength-strong {
  background: #48bb78;
}

.strength-text {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
}

.password-feedback {
  margin-top: 0.5rem;
  font-size: 0.9rem;
}

.feedback-success {
  color: #48bb78;
}

.feedback-error {
  color: #f56565;
}

.password-requirements {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.5rem;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.password-requirements h5 {
  color: white;
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
}

.password-requirements ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.password-requirements li {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.8rem;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.requirement-met {
  color: #48bb78;
}

.security-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  cursor: pointer;
  color: white;
  font-size: 0.9rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  transition: background-color 0.3s ease;
}

.checkbox-label:hover {
  background: rgba(255, 255, 255, 0.05);
}

.form-checkbox {
  width: 18px;
  height: 18px;
  accent-color: #48bb78;
  margin-top: 0.125rem;
}

.option-description {
  display: block;
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.sessions-list {
  margin-bottom: 1.5rem;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.5rem;
  margin-bottom: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.session-info {
  flex: 1;
}

.session-device {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.device-icon {
  font-size: 1.5rem;
}

.device-details {
  display: flex;
  flex-direction: column;
}

.device-details strong {
  color: white;
  font-size: 0.9rem;
}

.device-details small {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
}

.session-details {
  display: flex;
  gap: 1rem;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
}

.location-icon {
  margin-right: 0.25rem;
}

.session-actions {
  margin-left: 1rem;
}

.no-sessions {
  text-align: center;
  padding: 2rem;
  color: rgba(255, 255, 255, 0.6);
}

.btn-primary,
.btn-secondary,
.btn-danger {
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

.btn-danger {
  background: rgba(245, 101, 101, 0.2);
  color: #f56565;
  border: 1px solid rgba(245, 101, 101, 0.3);
  width: 100%;
}

.btn-danger:hover:not(:disabled) {
  background: rgba(245, 101, 101, 0.3);
  border-color: rgba(245, 101, 101, 0.5);
}

.btn-terminate {
  padding: 0.5rem 1rem;
  background: rgba(245, 101, 101, 0.2);
  color: #f56565;
  border: 1px solid rgba(245, 101, 101, 0.3);
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
}

.btn-terminate:hover:not(:disabled) {
  background: rgba(245, 101, 101, 0.3);
  border-color: rgba(245, 101, 101, 0.5);
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
  .session-item {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .session-actions {
    margin-left: 0;
    text-align: center;
  }
  
  .session-details {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
