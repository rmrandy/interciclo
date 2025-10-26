<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-6">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">Gestión de Páginas Informativas</h1>
            <p class="mt-2 text-gray-600">Administra el contenido de las páginas informativas del sitio</p>
          </div>
          <button
            @click="goBack"
            class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
            </svg>
            Volver
          </button>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
            </svg>
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">Error al cargar las páginas</h3>
            <div class="mt-2 text-sm text-red-700">
              <p>{{ error }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Pages Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="page in pages"
          :key="page.id"
          class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200"
        >
          <div class="p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-gray-900">{{ page.title }}</h3>
              <span
                :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  page.is_active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                ]"
              >
                {{ page.is_active ? 'Activa' : 'Inactiva' }}
              </span>
            </div>
            
            <p class="text-gray-600 text-sm mb-4">{{ page.description }}</p>
            
            <div class="flex space-x-2">
              <button
                @click="editPage(page)"
                class="flex-1 inline-flex items-center justify-center px-3 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
                Editar
              </button>
              
              <button
                @click="togglePageStatus(page)"
                :class="[
                  'px-3 py-2 text-sm font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2',
                  page.is_active
                    ? 'border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 focus:ring-gray-500'
                    : 'border border-transparent text-white bg-green-600 hover:bg-green-700 focus:ring-green-500'
                ]"
              >
                {{ page.is_active ? 'Desactivar' : 'Activar' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div
      v-if="showEditModal"
      class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    >
      <div class="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
        <div class="mt-3">
          <!-- Modal Header -->
          <div class="flex items-center justify-between pb-4 border-b">
            <h3 class="text-lg font-medium text-gray-900">
              Editar: {{ editingPage?.title }}
            </h3>
            <button
              @click="closeEditModal"
              class="text-gray-400 hover:text-gray-600"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- Modal Content -->
          <div class="mt-6 space-y-4">
            <!-- Title -->
            <div>
              <label class="block text-sm font-medium text-gray-700">Título</label>
              <input
                v-model="editingPage.title"
                type="text"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              />
            </div>

            <!-- Description -->
            <div>
              <label class="block text-sm font-medium text-gray-700">Descripción</label>
              <textarea
                v-model="editingPage.description"
                rows="3"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              ></textarea>
            </div>

            <!-- Content -->
            <div>
              <label class="block text-sm font-medium text-gray-700">Contenido</label>
              <textarea
                v-model="editingPage.content"
                rows="10"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                placeholder="Escribe el contenido de la página en HTML o Markdown..."
              ></textarea>
            </div>

            <!-- Active Status -->
            <div class="flex items-center">
              <input
                v-model="editingPage.is_active"
                type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label class="ml-2 block text-sm text-gray-900">
                Página activa
              </label>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="flex justify-end space-x-3 mt-6 pt-4 border-t">
            <button
              @click="closeEditModal"
              class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Cancelar
            </button>
            <button
              @click="savePage"
              :disabled="saving"
              class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            >
              {{ saving ? 'Guardando...' : 'Guardar Cambios' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { informativePagesApi, type InformativePage } from '../../utils/informativePagesApi'

const router = useRouter()

// State
const loading = ref(false)
const error = ref('')
const pages = ref<InformativePage[]>([])
const showEditModal = ref(false)
const editingPage = ref<InformativePage | null>(null)
const saving = ref(false)

// Methods
const loadPages = async () => {
  loading.value = true
  error.value = ''
  
  try {
    pages.value = await informativePagesApi.getAllPages()
  } catch (err) {
    error.value = 'Error al cargar las páginas informativas'
    console.error('Error loading pages:', err)
  } finally {
    loading.value = false
  }
}

const editPage = (page) => {
  editingPage.value = { ...page }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editingPage.value = null
}

const savePage = async () => {
  if (!editingPage.value) return
  
  saving.value = true
  
  try {
    const updatedPage = await informativePagesApi.updatePage(editingPage.value.id, {
      title: editingPage.value.title,
      description: editingPage.value.description,
      content: editingPage.value.content,
      is_active: editingPage.value.is_active
    })
    
    // Update local state
    const index = pages.value.findIndex(p => p.id === editingPage.value!.id)
    if (index !== -1) {
      pages.value[index] = updatedPage
    }
    
    closeEditModal()
  } catch (err) {
    error.value = 'Error al guardar los cambios'
    console.error('Error saving page:', err)
  } finally {
    saving.value = false
  }
}

const togglePageStatus = async (page: InformativePage) => {
  try {
    const updatedPage = await informativePagesApi.togglePageStatus(page.id)
    
    // Update local state
    const index = pages.value.findIndex(p => p.id === page.id)
    if (index !== -1) {
      pages.value[index] = updatedPage
    }
  } catch (err) {
    error.value = 'Error al cambiar el estado de la página'
    console.error('Error toggling page status:', err)
  }
}

const goBack = () => {
  router.go(-1)
}

// Lifecycle
onMounted(() => {
  loadPages()
})
</script>

<style scoped>
/* Custom styles if needed */
</style>
