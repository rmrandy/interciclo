<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-4xl font-bold text-gray-900 mb-2">
          👔 Gestión de Usuarios Empresariales
        </h1>
        <p class="text-gray-600">
          Administra agencias de viaje y usuarios corporativos con acceso API
        </p>
      </div>

      <!-- Actions Bar -->
      <div class="bg-white rounded-lg shadow-sm p-4 mb-6 flex justify-between items-center">
        <div class="flex gap-4 items-center flex-1">
          <!-- Search -->
          <div class="relative flex-1 max-w-md">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Buscar por empresa, email o API Key..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              @input="applyFilters"
            />
            <svg class="absolute left-3 top-3 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
          </div>

          <!-- Stats -->
          <div class="flex gap-4">
            <div class="px-4 py-2 bg-blue-50 rounded-lg">
              <span class="text-sm text-gray-600">Total:</span>
              <span class="ml-2 font-bold text-blue-600">{{ corporateUsers.length }}</span>
            </div>
            <div class="px-4 py-2 bg-green-50 rounded-lg">
              <span class="text-sm text-gray-600">Activos:</span>
              <span class="ml-2 font-bold text-green-600">{{ activeUsersCount }}</span>
            </div>
          </div>
        </div>

        <!-- Create Button -->
        <button
          @click="openCreateModal"
          class="flex items-center gap-2 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors shadow-md hover:shadow-lg"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          Nuevo Usuario Empresarial
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <span class="ml-3 text-gray-600">Cargando usuarios...</span>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
        <div class="flex items-center">
          <svg class="w-6 h-6 text-red-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <span class="text-red-800">{{ error }}</span>
        </div>
      </div>

      <!-- Success Message -->
      <div v-if="success" class="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
        <div class="flex items-center">
          <svg class="w-6 h-6 text-green-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
          </svg>
          <span class="text-green-800">{{ success }}</span>
        </div>
      </div>

      <!-- Users Table -->
      <div v-else class="bg-white rounded-lg shadow-md overflow-hidden">
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Empresa
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Contacto
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  API Key
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fecha Registro
                </th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-if="filteredUsers.length === 0">
                <td colspan="6" class="px-6 py-12 text-center text-gray-500">
                  <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
                  </svg>
                  <p class="text-lg">No se encontraron usuarios empresariales</p>
                  <p class="text-sm mt-2">Crea el primer usuario empresarial para comenzar</p>
                </td>
              </tr>
              <tr v-for="user in paginatedUsers" :key="user.idUser" class="hover:bg-gray-50 transition-colors">
                <td class="px-6 py-4">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10 bg-blue-100 rounded-full flex items-center justify-center">
                      <span class="text-blue-600 font-bold text-lg">{{ getInitials(user.companyName) }}</span>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{{ user.companyName }}</div>
                      <div class="text-sm text-gray-500">ID: {{ user.idUser }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="text-sm text-gray-900">{{ user.name }}</div>
                  <div class="text-sm text-gray-500">{{ user.email }}</div>
                  <div class="text-sm text-gray-500">📞 {{ user.phone }}</div>
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center gap-2">
                    <code class="text-xs bg-gray-100 px-2 py-1 rounded font-mono">
                      {{ user.apiKey ? maskApiKey(user.apiKey) : 'N/A' }}
                    </code>
                    <button
                      v-if="user.apiKey"
                      @click="copyApiKey(user.apiKey)"
                      class="text-gray-400 hover:text-blue-600 transition-colors"
                      title="Copiar API Key"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                      </svg>
                    </button>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span :class="[
                    'px-2 inline-flex text-xs leading-5 font-semibold rounded-full',
                    user.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  ]">
                    {{ user.enabled ? '✓ Activo' : '✗ Inactivo' }}
                  </span>
                </td>
                <td class="px-6 py-4 text-sm text-gray-500">
                  {{ formatDate(user.createdAt) }}
                </td>
                <td class="px-6 py-4 text-right text-sm font-medium space-x-2">
                  <button
                    @click="openEditModal(user)"
                    class="text-blue-600 hover:text-blue-900 transition-colors"
                    title="Editar"
                  >
                    <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                    </svg>
                  </button>
                  <button
                    @click="regenerateApiKey(user)"
                    class="text-green-600 hover:text-green-900 transition-colors"
                    title="Regenerar API Key"
                  >
                    <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path>
                    </svg>
                  </button>
                  <button
                    @click="toggleUserStatus(user)"
                    :class="[
                      'transition-colors',
                      user.enabled ? 'text-yellow-600 hover:text-yellow-900' : 'text-green-600 hover:text-green-900'
                    ]"
                    :title="user.enabled ? 'Desactivar' : 'Activar'"
                  >
                    <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                    </svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="bg-gray-50 px-6 py-4 flex items-center justify-between border-t border-gray-200">
          <div class="text-sm text-gray-700">
            Mostrando {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, filteredUsers.length) }} de {{ filteredUsers.length }} usuarios
          </div>
          <div class="flex gap-2">
            <button
              @click="currentPage--"
              :disabled="currentPage === 1"
              class="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100 transition-colors"
            >
              Anterior
            </button>
            <button
              v-for="page in visiblePages"
              :key="page"
              @click="currentPage = page"
              :class="[
                'px-4 py-2 border rounded-lg transition-colors',
                currentPage === page ? 'bg-blue-600 text-white border-blue-600' : 'border-gray-300 hover:bg-gray-100'
              ]"
            >
              {{ page }}
            </button>
            <button
              @click="currentPage++"
              :disabled="currentPage === totalPages"
              class="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100 transition-colors"
            >
              Siguiente
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h2 class="text-2xl font-bold text-gray-900">
            {{ editingUser ? '✏️ Editar Usuario Empresarial' : '➕ Nuevo Usuario Empresarial' }}
          </h2>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600 transition-colors">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <form @submit.prevent="saveUser" class="p-6 space-y-6">
          <!-- Company Name -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              🏢 Nombre de la Empresa *
            </label>
            <input
              v-model="formData.companyName"
              type="text"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Ej: Agencia de Viajes XYZ"
            />
          </div>

          <!-- Contact Name -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              👤 Nombre del Contacto *
            </label>
            <input
              v-model="formData.name"
              type="text"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Ej: Juan Pérez"
            />
          </div>

          <!-- Grid 2 columns -->
          <div class="grid grid-cols-2 gap-4">
            <!-- Email -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                📧 Email *
              </label>
              <input
                v-model="formData.email"
                type="email"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="contacto@empresa.com"
              />
            </div>

            <!-- Phone -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                📞 Teléfono *
              </label>
              <input
                v-model="formData.phone"
                type="tel"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="12345678"
              />
            </div>
          </div>

          <!-- Grid 2 columns -->
          <div class="grid grid-cols-2 gap-4">
            <!-- CUI -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                🆔 CUI/NIT *
              </label>
              <input
                v-model="formData.cui"
                type="text"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="1234567890"
              />
            </div>

            <!-- Birth Date (optional) -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                📅 Fecha de Nacimiento
              </label>
              <input
                v-model="formData.birthDate"
                type="date"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>

          <!-- Address -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              📍 Dirección *
            </label>
            <textarea
              v-model="formData.address"
              required
              rows="2"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Dirección completa de la empresa"
            ></textarea>
          </div>

          <!-- Password (only for new users) -->
          <div v-if="!editingUser">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              🔒 Contraseña *
            </label>
            <input
              v-model="formData.password"
              type="password"
              :required="!editingUser"
              minlength="6"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Mínimo 6 caracteres"
            />
          </div>

          <!-- Enabled Status -->
          <div class="flex items-center gap-3">
            <input
              v-model="formData.enabled"
              type="checkbox"
              id="enabled"
              class="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <label for="enabled" class="text-sm font-medium text-gray-700">
              Usuario activo (puede usar el sistema)
            </label>
          </div>

          <!-- API Key (show if editing) -->
          <div v-if="editingUser && formData.apiKey" class="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              🔑 API Key Actual
            </label>
            <div class="flex items-center gap-2">
              <code class="flex-1 text-sm bg-white px-3 py-2 rounded font-mono border border-gray-300">
                {{ formData.apiKey }}
              </code>
              <button
                type="button"
                @click="copyApiKey(formData.apiKey)"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                Copiar
              </button>
            </div>
            <p class="text-xs text-gray-600 mt-2">
              💡 Usa el botón "Regenerar API Key" en la lista para crear una nueva clave
            </p>
          </div>

          <!-- Actions -->
          <div class="flex gap-3 pt-4 border-t border-gray-200">
            <button
              type="button"
              @click="closeModal"
              class="flex-1 px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              :disabled="saving"
              class="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ saving ? 'Guardando...' : (editingUser ? 'Actualizar' : 'Crear Usuario') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { getInsuranceApiUrl } from '../../utils/api';

// Interfaces
interface CorporateUser {
  idUser: number;
  name: string;
  cui: string;
  phone: string;
  email: string;
  address: string;
  birthDate: string;
  role: string;
  enabled: number;
  isCorporate: number;
  companyName: string;
  apiKey: string;
  password?: string;
  createdAt?: string;
}

// State
const corporateUsers = ref<CorporateUser[]>([]);
const filteredUsers = ref<CorporateUser[]>([]);
const loading = ref(true);
const error = ref('');
const success = ref('');
const searchQuery = ref('');
const showModal = ref(false);
const editingUser = ref<CorporateUser | null>(null);
const saving = ref(false);

// Form Data
const formData = ref({
  companyName: '',
  name: '',
  email: '',
  phone: '',
  cui: '',
  birthDate: '',
  address: '',
  password: '',
  enabled: true,
  apiKey: ''
});

// Pagination
const currentPage = ref(1);
const itemsPerPage = ref(10);

// Computed
const activeUsersCount = computed(() => 
  corporateUsers.value.filter(u => u.enabled === 1).length
);

const totalPages = computed(() => 
  Math.ceil(filteredUsers.value.length / itemsPerPage.value)
);

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredUsers.value.slice(start, end);
});

const visiblePages = computed(() => {
  const pages: number[] = [];
  const maxVisible = 5;
  let startPage = Math.max(1, currentPage.value - Math.floor(maxVisible / 2));
  let endPage = Math.min(totalPages.value, startPage + maxVisible - 1);

  if (endPage - startPage < maxVisible - 1) {
    startPage = Math.max(1, endPage - maxVisible + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }

  return pages;
});

// Methods
const fetchCorporateUsers = async () => {
  try {
    loading.value = true;
    error.value = '';
    
    const response = await axios.get(getInsuranceApiUrl('/corporate-users'));
    
    // Los usuarios ya vienen filtrados desde el backend
    corporateUsers.value = response.data;
    applyFilters();
  } catch (err: any) {
    error.value = err.response?.data?.message || err.response?.data?.error || 'Error al cargar usuarios empresariales';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const applyFilters = () => {
  const query = searchQuery.value.toLowerCase();
  
  if (!query) {
    filteredUsers.value = [...corporateUsers.value];
  } else {
    filteredUsers.value = corporateUsers.value.filter(user =>
      user.companyName?.toLowerCase().includes(query) ||
      user.name?.toLowerCase().includes(query) ||
      user.email?.toLowerCase().includes(query) ||
      user.apiKey?.toLowerCase().includes(query)
    );
  }
  
  currentPage.value = 1;
};

const openCreateModal = () => {
  editingUser.value = null;
  formData.value = {
    companyName: '',
    name: '',
    email: '',
    phone: '',
    cui: '',
    birthDate: '',
    address: '',
    password: '',
    enabled: true,
    apiKey: ''
  };
  showModal.value = true;
};

const openEditModal = (user: CorporateUser) => {
  editingUser.value = user;
  formData.value = {
    companyName: user.companyName || '',
    name: user.name || '',
    email: user.email || '',
    phone: user.phone || '',
    cui: user.cui || '',
    birthDate: user.birthDate || '',
    address: user.address || '',
    password: '',
    enabled: user.enabled === 1,
    apiKey: user.apiKey || ''
  };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  editingUser.value = null;
  error.value = '';
  success.value = '';
};

const generateApiKey = (): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let apiKey = '';
  for (let i = 0; i < 32; i++) {
    apiKey += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return apiKey;
};

const saveUser = async () => {
  try {
    saving.value = true;
    error.value = '';
    
    const userData: any = {
      name: formData.value.name,
      cui: formData.value.cui,
      phone: formData.value.phone,
      email: formData.value.email,
      address: formData.value.address,
      birthDate: formData.value.birthDate || null,
      enabled: formData.value.enabled ? 1 : 0,
      companyName: formData.value.companyName,
    };

    if (editingUser.value) {
      // Update existing user
      userData.idUser = editingUser.value.idUser;
      
      if (formData.value.password) {
        userData.password = formData.value.password;
      }

      const response = await axios.put(getInsuranceApiUrl(`/corporate-users/${editingUser.value.idUser}`), userData);
      success.value = '✓ Usuario empresarial actualizado correctamente';
    } else {
      // Create new user
      userData.password = formData.value.password;
      
      const response = await axios.post(getInsuranceApiUrl('/corporate-users'), userData);
      
      // El backend genera y retorna el API Key
      if (response.data.apiKey) {
        // Copiar automáticamente el API Key al portapapeles
        await navigator.clipboard.writeText(response.data.apiKey);
        success.value = `✓ Usuario creado. API Key: ${response.data.apiKey} (copiado al portapapeles)`;
      } else {
        success.value = '✓ Usuario empresarial creado correctamente';
      }
    }

    await fetchCorporateUsers();
    closeModal();
    
    setTimeout(() => {
      success.value = '';
    }, 5000);
  } catch (err: any) {
    error.value = err.response?.data?.error || err.response?.data?.message || 'Error al guardar el usuario';
    console.error(err);
  } finally {
    saving.value = false;
  }
};

const regenerateApiKey = async (user: CorporateUser) => {
  if (!confirm(`¿Estás seguro de regenerar el API Key para ${user.companyName}?\n\nEsto invalidará el API Key actual y deberás actualizar todas las integraciones.`)) {
    return;
  }

  try {
    const response = await axios.put(getInsuranceApiUrl(`/corporate-users/${user.idUser}/regenerate-key`));
    
    const newApiKey = response.data.apiKey;
    
    // Copiar automáticamente al portapapeles
    await navigator.clipboard.writeText(newApiKey);
    success.value = `✓ API Key regenerado: ${newApiKey} (copiado al portapapeles)`;
    
    await fetchCorporateUsers();
    
    setTimeout(() => {
      success.value = '';
    }, 8000);
  } catch (err: any) {
    error.value = err.response?.data?.error || 'Error al regenerar el API Key';
    console.error(err);
  }
};

const toggleUserStatus = async (user: CorporateUser) => {
  const action = user.enabled ? 'desactivar' : 'activar';
  
  if (!confirm(`¿Estás seguro de ${action} a ${user.companyName}?`)) {
    return;
  }

  try {
    await axios.put(getInsuranceApiUrl(`/corporate-users/${user.idUser}/toggle-status`));

    success.value = `✓ Usuario ${user.enabled ? 'desactivado' : 'activado'} correctamente`;
    await fetchCorporateUsers();
    
    setTimeout(() => {
      success.value = '';
    }, 3000);
  } catch (err: any) {
    error.value = err.response?.data?.error || 'Error al cambiar el estado del usuario';
    console.error(err);
  }
};

const copyApiKey = async (apiKey: string) => {
  try {
    await navigator.clipboard.writeText(apiKey);
    success.value = '✓ API Key copiado al portapapeles';
    setTimeout(() => {
      success.value = '';
    }, 2000);
  } catch (err) {
    error.value = 'Error al copiar API Key';
  }
};

const maskApiKey = (apiKey: string): string => {
  if (!apiKey || apiKey.length < 8) return apiKey;
  return apiKey.substring(0, 8) + '•'.repeat(apiKey.length - 12) + apiKey.substring(apiKey.length - 4);
};

const getInitials = (name: string): string => {
  if (!name) return '?';
  const words = name.split(' ');
  if (words.length >= 2) {
    return (words[0][0] + words[1][0]).toUpperCase();
  }
  return name.substring(0, 2).toUpperCase();
};

const formatDate = (date: string): string => {
  if (!date) return 'N/A';
  const d = new Date(date);
  return d.toLocaleDateString('es-ES', { year: 'numeric', month: 'short', day: 'numeric' });
};

// Lifecycle
onMounted(() => {
  fetchCorporateUsers();
});
</script>

