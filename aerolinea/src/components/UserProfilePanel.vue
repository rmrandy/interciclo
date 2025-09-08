<template>
  <div class="user-profile-panel">
    <!-- Header del Panel -->
    <div class="panel-header">
      <h2>👤 Panel de Usuario</h2>
      <p class="user-welcome">Bienvenido, {{ userProfile?.firstName || 'Usuario' }}</p>
    </div>

    <!-- Navegación por Pestañas -->
    <div class="tab-navigation">
      <button 
        v-for="tab in tabs" 
        :key="tab.id"
        :class="['tab-button', { active: activeTab === tab.id }]"
        @click="activeTab = tab.id"
      >
        {{ tab.icon }} {{ tab.label }}
      </button>
    </div>

    <!-- Contenido de las Pestañas -->
    <div class="tab-content">
      <!-- Pestaña: Perfil de Usuario -->
      <div v-if="activeTab === 'profile'" class="tab-pane">
        <UserProfileForm 
          :user="userProfile" 
          @profile-updated="handleProfileUpdate"
        />
      </div>

      <!-- Pestaña: Historial de Actividad -->
      <div v-if="activeTab === 'activity'" class="tab-pane">
        <UserActivityHistory 
          :activities="userActivities"
          @clear-history="clearActivityHistory"
        />
      </div>

      <!-- Pestaña: Configuraciones -->
      <div v-if="activeTab === 'settings'" class="tab-pane">
        <UserSettings 
          :settings="userSettings"
          @settings-updated="handleSettingsUpdate"
        />
      </div>

      <!-- Pestaña: Seguridad -->
      <div v-if="activeTab === 'security'" class="tab-pane">
        <UserSecurity 
          @password-changed="handlePasswordChange"
        />
      </div>
    </div>

    <!-- Indicador de Carga -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>Cargando panel de usuario...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import UserProfileForm from './UserProfileForm.vue'
import UserActivityHistory from './UserActivityHistory.vue'
import UserSettings from './UserSettings.vue'
import UserSecurity from './UserSecurity.vue'
import { userProfileApi, isServerAvailable } from '../utils/userProfileApi'

// Props
interface Props {
  userId?: number
}

const props = withDefaults(defineProps<Props>(), {
  userId: undefined
})

// Emits
const emit = defineEmits<{
  profileUpdated: [user: any]
  settingsUpdated: [settings: any]
  passwordChanged: []
}>()

// State
const loading = ref(false)
const activeTab = ref('profile')
const userProfile = ref<any>(null)
const userActivities = ref<any[]>([])
const userSettings = ref<any>({})

// Tabs disponibles
const tabs = [
  { id: 'profile', label: 'Perfil', icon: '👤' },
  { id: 'activity', label: 'Actividad', icon: '📋' },
  { id: 'settings', label: 'Configuración', icon: '⚙️' },
  { id: 'security', label: 'Seguridad', icon: '🔐' }
]

// Computed
const currentUserId = computed(() => {
  return props.userId || userProfile.value?.idUser || 1
})

// Methods
const loadUserProfile = async () => {
  try {
    loading.value = true
    
    // Verificar si el servidor está disponible
    const serverAvailable = await isServerAvailable()
    
    if (serverAvailable) {
      // Cargar desde la API
      const userData = localStorage.getItem('user')
      if (userData) {
        const user = JSON.parse(userData)
        if (user.idUser) {
          try {
            // Cargar perfil completo desde la API
            const profile = await userProfileApi.getUserProfile(user.idUser)
            userProfile.value = profile
            
            // Cargar actividades del usuario
            await loadUserActivities()
            
            // Cargar configuraciones del usuario
            await loadUserSettings()
          } catch (apiError) {
            console.error('Error cargando desde API, usando localStorage:', apiError)
            userProfile.value = user
          }
        } else {
          userProfile.value = user
        }
      }
    } else {
      // Servidor no disponible, usar localStorage
      console.log('Servidor no disponible, usando localStorage')
      const userData = localStorage.getItem('user')
      if (userData) {
        userProfile.value = JSON.parse(userData)
      }
      
      // Cargar actividades del usuario
      await loadUserActivities()
      
      // Cargar configuraciones del usuario
      await loadUserSettings()
    }
    
  } catch (error) {
    console.error('Error cargando perfil de usuario:', error)
  } finally {
    loading.value = false
  }
}

const loadUserActivities = async () => {
  try {
    if (userProfile.value?.idUser && await isServerAvailable()) {
      // Cargar desde la API
      const activities = await userProfileApi.getUserActivityHistory(userProfile.value.idUser)
      userActivities.value = activities
    } else {
      // Usar datos simulados si no hay servidor o usuario
      userActivities.value = [
        {
          id: 1,
          type: 'flight_search',
          description: 'Búsqueda de vuelo: Guatemala → Miami',
          timestamp: new Date().toISOString(),
          details: { origin: 'Guatemala', destination: 'Miami', date: '2025-08-25' }
        },
        {
          id: 2,
          type: 'profile_update',
          description: 'Perfil actualizado',
          timestamp: new Date(Date.now() - 86400000).toISOString(),
          details: { field: 'email', oldValue: 'old@email.com', newValue: 'new@email.com' }
        }
      ]
    }
  } catch (error) {
    console.error('Error cargando actividades:', error)
    // En caso de error, usar datos simulados
    userActivities.value = [
      {
        id: 1,
        type: 'flight_search',
        description: 'Búsqueda de vuelo: Guatemala → Miami',
        timestamp: new Date().toISOString(),
        details: { origin: 'Guatemala', destination: 'Miami', date: '2025-08-25' }
      }
    ]
  }
}

const loadUserSettings = async () => {
  try {
    if (userProfile.value?.idUser && await isServerAvailable()) {
      // Cargar desde la API
      const settings = await userProfileApi.getUserSettings(userProfile.value.idUser)
      userSettings.value = settings
    } else {
      // Cargar configuraciones desde localStorage
      const savedSettings = localStorage.getItem('user_settings')
      if (savedSettings) {
        userSettings.value = JSON.parse(savedSettings)
      } else {
        // Configuraciones por defecto
        userSettings.value = {
          notifications: {
            email: true,
            push: false,
            sms: false
          },
          language: 'es',
          theme: 'light',
          privacy: {
            shareProfile: false,
            showActivity: true
          },
          timezone: 'America/Guatemala_City',
          dateFormat: 'DD/MM/YYYY',
          currency: 'GTQ'
        }
      }
    }
  } catch (error) {
    console.error('Error cargando configuraciones:', error)
    // En caso de error, usar configuraciones por defecto
    userSettings.value = {
      notifications: {
        email: true,
        push: false,
        sms: false
      },
      language: 'es',
      theme: 'light',
      privacy: {
        shareProfile: false,
        showActivity: true
      },
      timezone: 'America/Guatemala_City',
      dateFormat: 'DD/MM/YYYY',
      currency: 'GTQ'
    }
  }
}

const handleProfileUpdate = (updatedUser: any) => {
  userProfile.value = { ...userProfile.value, ...updatedUser }
  
  // Guardar en localStorage
  localStorage.setItem('user', JSON.stringify(userProfile.value))
  
  // Emitir evento
  emit('profileUpdated', userProfile.value)
  
  // Agregar actividad
  addActivity('profile_update', 'Perfil actualizado', { updatedFields: Object.keys(updatedUser) })
}

const handleSettingsUpdate = (updatedSettings: any) => {
  userSettings.value = { ...userSettings.value, ...updatedSettings }
  
  // Guardar en localStorage
  localStorage.setItem('user_settings', JSON.stringify(userSettings.value))
  
  // Emitir evento
  emit('settingsUpdated', userSettings.value)
  
  // Agregar actividad
  addActivity('settings_update', 'Configuraciones actualizadas', { updatedSettings })
}

const handlePasswordChange = () => {
  // Emitir evento
  emit('passwordChanged')
  
  // Agregar actividad
  addActivity('security_update', 'Contraseña cambiada', {})
}

const addActivity = (type: string, description: string, details: any) => {
  const newActivity = {
    id: Date.now(),
    type,
    description,
    timestamp: new Date().toISOString(),
    details
  }
  
  userActivities.value.unshift(newActivity)
  
  // Mantener solo las últimas 50 actividades
  if (userActivities.value.length > 50) {
    userActivities.value = userActivities.value.slice(0, 50)
  }
}

const clearActivityHistory = () => {
  userActivities.value = []
  localStorage.removeItem('user_activities')
}

// Lifecycle
onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.user-profile-panel {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 1rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.panel-header {
  text-align: center;
  margin-bottom: 2rem;
}

.panel-header h2 {
  font-size: 2rem;
  color: white;
  margin-bottom: 0.5rem;
}

.user-welcome {
  color: rgba(255, 255, 255, 0.8);
  font-size: 1.1rem;
}

.tab-navigation {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.tab-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.tab-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
}

.tab-button.active {
  background: #48bb78;
  border-color: #48bb78;
  box-shadow: 0 0 20px rgba(72, 187, 120, 0.3);
}

.tab-content {
  min-height: 400px;
}

.tab-pane {
  animation: fadeIn 0.3s ease;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top: 3px solid #48bb78;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .user-profile-panel {
    padding: 1rem;
  }
  
  .tab-navigation {
    flex-direction: column;
    align-items: center;
  }
  
  .tab-button {
    width: 100%;
    max-width: 300px;
  }
}
</style>
