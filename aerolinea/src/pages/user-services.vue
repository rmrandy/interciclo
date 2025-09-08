<script setup lang="ts">
import { ref, onMounted } from "vue";
import { userProfileUpdateApi, isProfileUpdateServerAvailable, type UserProfileUpdateRequest } from "../utils/userProfileUpdateApi";

// Estado
const user = JSON.parse(localStorage.getItem('user') || 'null');
const error = ref("");
const activeTab = ref("profile");



// Métodos para el Panel de Usuario
const handleProfileUpdate = (updatedUser: any) => {
  console.log('Perfil actualizado:', updatedUser);
  // Actualizar el usuario en localStorage
  localStorage.setItem('user', JSON.stringify(updatedUser));
  // Aquí podrías hacer una llamada a la API para actualizar en el backend
};

const handleSettingsUpdate = (updatedSettings: any) => {
  console.log('Configuraciones actualizadas:', updatedSettings);
  // Guardar configuraciones en localStorage
  localStorage.setItem('user_settings', JSON.stringify(updatedSettings));
};

const handlePasswordChange = () => {
  console.log('Contraseña cambiada');
  // Aquí podrías mostrar un mensaje de confirmación o redirigir
};

// Estado para el panel de usuario
const userPanelTab = ref('profile');
const serverStatus = ref<'checking' | 'available' | 'unavailable'>('checking');

// Métodos para el panel de usuario
const showUserProfile = () => {
  userPanelTab.value = 'profile';
};

const showUserSettings = () => {
  userPanelTab.value = 'settings';
};

const showUserSecurity = () => {
  userPanelTab.value = 'security';
};

const showUserHistory = () => {
  userPanelTab.value = 'history';
};

// Estado para el formulario de perfil
const profileForm = ref({
  firstName: user?.firstName || '',
  lastName: user?.lastName || '',
  email: user?.email || '',
  phone: user?.phone || ''
});

// Método para guardar cambios del perfil
const saveProfileChanges = async () => {
  try {
    // Verificar si el servidor está disponible
    const serverAvailable = await isProfileUpdateServerAvailable();
    
    if (serverAvailable) {
      // Usar la API del backend
      const updateRequest: UserProfileUpdateRequest = {
        userId: user.idUser,
        firstName: profileForm.value.firstName,
        lastName: profileForm.value.lastName,
        email: profileForm.value.email,
        phone: profileForm.value.phone
      };
      
      const response = await userProfileUpdateApi.updateUserProfile(updateRequest);
      
      if (response.success && response.user) {
        // Actualizar el usuario en localStorage con los datos del backend
        const updatedUser = { ...user, ...response.user };
        localStorage.setItem('user', JSON.stringify(updatedUser));
        
        // Mostrar mensaje de éxito
        alert('✅ Perfil actualizado correctamente en el servidor');
        
        // Recargar la página para reflejar los cambios
        window.location.reload();
      } else {
        throw new Error(response.error || 'Error desconocido del servidor');
      }
    } else {
      // Fallback a localStorage si el servidor no está disponible
      const updatedUser = { ...user, ...profileForm.value };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      
      alert('⚠️ Servidor no disponible. Cambios guardados localmente.');
      window.location.reload();
    }
  } catch (error) {
    console.error('Error al guardar perfil:', error);
    alert(`❌ Error al guardar el perfil: ${error instanceof Error ? error.message : 'Error desconocido'}`);
  }
};

// Función para verificar el estado del servidor
const checkServerStatus = async () => {
  try {
    serverStatus.value = 'checking';
    const available = await isProfileUpdateServerAvailable();
    serverStatus.value = available ? 'available' : 'unavailable';
  } catch (error) {
    console.error('Error verificando estado del servidor:', error);
    serverStatus.value = 'unavailable';
  }
};

// Inicializar el formulario con los datos del usuario
onMounted(async () => {
  if (user) {
    profileForm.value = {
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      email: user.email || '',
      phone: user.phone || ''
    };
  }
  
  // Verificar estado del servidor
  await checkServerStatus();
});
</script>

<template>
  <div class="container mx-auto p-6">
    <h1 class="text-2xl font-bold mb-6">👤 Panel de Usuario</h1>
    
    <!-- Banner del Panel de Usuario -->
    <div class="bg-gradient-to-r from-green-500 to-blue-500 rounded-lg p-4 mb-6 text-white shadow-lg">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-lg font-semibold mb-1">🌟 Panel de Usuario Completo</h3>
          <p class="text-green-100 text-sm">Gestiona tu perfil, configuraciones, seguridad y actividad en el sistema</p>
        </div>
        <div class="text-right">
          <div class="text-2xl">👤</div>
          <div class="text-xs text-green-100">Disponible</div>
        </div>
      </div>
    </div>
    
    <!-- Indicador de Estado del Servidor -->
    <div class="mb-6">
      <div class="flex items-center justify-center">
        <div class="bg-white rounded-lg shadow-md p-4 border-l-4" 
             :class="{
               'border-yellow-500': serverStatus === 'checking',
               'border-green-500': serverStatus === 'available',
               'border-red-500': serverStatus === 'unavailable'
             }">
          <div class="flex items-center space-x-3">
            <div class="w-3 h-3 rounded-full animate-pulse"
                 :class="{
                   'bg-yellow-500': serverStatus === 'checking',
                   'bg-green-500': serverStatus === 'available',
                   'bg-red-500': serverStatus === 'unavailable'
                 }">
            </div>
            <span class="text-sm font-medium text-gray-700">
              <span v-if="serverStatus === 'checking'">🔄 Verificando conexión con el servidor...</span>
              <span v-else-if="serverStatus === 'available'">✅ Servidor conectado - Cambios se guardan en la base de datos</span>
              <span v-else-if="serverStatus === 'unavailable'">❌ Servidor no disponible - Cambios se guardan localmente</span>
            </span>
            <button 
              @click="checkServerStatus"
              class="ml-2 px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 rounded text-gray-600"
            >
              🔄
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Mensajes de error -->
    <div v-if="error" class="bg-red-100 text-red-700 p-3 mb-4 rounded">{{ error }}</div>
    
    <!-- Mensaje de no autenticado -->
    <div v-if="!user" class="bg-yellow-100 text-yellow-700 p-4 rounded">
      <p>Debe iniciar sesión para acceder al Panel de Usuario.</p>
    </div>
    
    <div v-else>
      <!-- Panel de Usuario - Contenido Principal -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <!-- Información básica del usuario -->
        <div class="text-center py-8">
          <div class="text-6xl mb-4">👤</div>
          <h3 class="text-xl font-semibold text-gray-700 mb-2">Panel de Usuario</h3>
          <p class="text-gray-500 mb-4">Usuario: {{ user.firstName }} {{ user.lastName }}</p>
          
          <!-- Información básica del usuario -->
          <div class="bg-gray-50 rounded-lg p-4 mb-4 text-left">
            <h4 class="font-semibold mb-2">📋 Información Personal:</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p><strong>Email:</strong> {{ user.email || 'No especificado' }}</p>
                <p><strong>Teléfono:</strong> {{ user.phone || 'No especificado' }}</p>
              </div>
              <div>
                <p><strong>CUI:</strong> {{ user.cui || 'No especificado' }}</p>
                <p><strong>Fecha de Nacimiento:</strong> {{ user.birthDate ? new Date(user.birthDate).toLocaleDateString() : 'No especificado' }}</p>
              </div>
            </div>
          </div>
          
          <!-- Botones de acción -->
          <div class="flex gap-3 justify-center flex-wrap mb-6">
            <button 
              @click="showUserProfile"
              :class="[
                'px-4 py-2 rounded-lg transition-colors',
                userPanelTab === 'profile' 
                  ? 'bg-blue-600 text-white' 
                  : 'bg-blue-500 text-white hover:bg-blue-600'
              ]"
            >
              ✏️ Editar Perfil
            </button>
            <button 
              @click="showUserSettings"
              :class="[
                'px-4 py-2 rounded-lg transition-colors',
                userPanelTab === 'settings' 
                  ? 'bg-green-600 text-white' 
                  : 'bg-green-500 text-white hover:bg-green-600'
              ]"
            >
              ⚙️ Configuraciones
            </button>
            <button 
              @click="showUserSecurity"
              :class="[
                'px-4 py-2 rounded-lg transition-colors',
                userPanelTab === 'security' 
                  ? 'bg-purple-600 text-white' 
                  : 'bg-purple-500 text-white hover:bg-purple-600'
              ]"
            >
              🔐 Seguridad
            </button>
            <button 
              @click="showUserHistory"
              :class="[
                'px-4 py-2 rounded-lg transition-colors',
                userPanelTab === 'history' 
                  ? 'bg-orange-600 text-white' 
                  : 'bg-orange-500 text-white hover:bg-orange-600'
              ]"
            >
              📊 Historial
            </button>
          </div>
          
          <!-- Contenido del panel según la pestaña seleccionada -->
          <div v-if="userPanelTab === 'profile'" class="bg-white border rounded-lg p-6">
            <h4 class="text-lg font-semibold mb-4">📝 Editar Perfil</h4>
            <form @submit.prevent="saveProfileChanges" class="space-y-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
                  <input 
                    type="text" 
                    v-model="profileForm.firstName"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Tu nombre"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Apellido</label>
                  <input 
                    type="text" 
                    v-model="profileForm.lastName"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Tu apellido"
                  >
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input 
                  type="email" 
                  v-model="profileForm.email"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="tu@email.com"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                <input 
                  type="tel" 
                  v-model="profileForm.phone"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Tu teléfono"
                >
              </div>
              <button type="submit" class="w-full bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600">
                💾 Guardar Cambios
              </button>
            </form>
          </div>
          
          <div v-if="userPanelTab === 'settings'" class="bg-white border rounded-lg p-6">
            <h4 class="text-lg font-semibold mb-4">⚙️ Configuraciones</h4>
            <div class="space-y-4">
              <div class="flex items-center justify-between">
                <span class="text-gray-700">Notificaciones por email</span>
                <label class="relative inline-flex items-center cursor-pointer">
                  <input type="checkbox" class="sr-only peer" checked>
                  <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-gray-700">Tema oscuro</span>
                <label class="relative inline-flex items-center cursor-pointer">
                  <input type="checkbox" class="sr-only peer">
                  <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>
            </div>
          </div>
          
          <div v-if="userPanelTab === 'security'" class="bg-white border rounded-lg p-6">
            <h4 class="text-lg font-semibold mb-4">🔐 Seguridad</h4>
            <form class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Contraseña actual</label>
                <input 
                  type="password" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Tu contraseña actual"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Nueva contraseña</label>
                <input 
                  type="password" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Nueva contraseña"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Confirmar nueva contraseña</label>
                <input 
                  type="password" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Confirma la nueva contraseña"
                >
              </div>
              <button type="submit" class="w-full bg-purple-500 text-white py-2 px-4 rounded-lg hover:bg-purple-600">
                🔒 Cambiar Contraseña
              </button>
            </form>
          </div>
          
          <div v-if="userPanelTab === 'history'" class="bg-white border rounded-lg p-6">
            <h4 class="text-lg font-semibold mb-4">📊 Historial de Actividad</h4>
            <div class="space-y-3">
              <div class="flex items-center p-3 bg-gray-50 rounded-lg">
                <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
                  <span class="text-blue-600 text-sm">✈️</span>
                </div>
                <div class="flex-1">
                  <p class="font-medium text-gray-800">Búsqueda de vuelo</p>
                  <p class="text-sm text-gray-600">Ciudad Guatemala → Nueva York</p>
                </div>
                <span class="text-xs text-gray-500">Hace 2 horas</span>
              </div>
              <div class="flex items-center p-3 bg-gray-50 rounded-lg">
                <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center mr-3">
                  <span class="text-green-600 text-sm">🏥</span>
                </div>
                <div class="flex-1">
                  <p class="font-medium text-gray-800">Consulta médica</p>
                  <p class="text-sm text-gray-600">Hospital General</p>
                </div>
                <span class="text-xs text-gray-500">Ayer</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #3498db;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 