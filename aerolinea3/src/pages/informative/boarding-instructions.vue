<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Hero Section -->
    <div class="airline-gradient-sky text-white rounded-3xl p-8 mb-8 relative overflow-hidden">
      <div class="absolute top-0 right-0 w-64 h-64 opacity-10">
        <svg fill="currentColor" viewBox="0 0 24 24" class="w-full h-full">
          <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
        </svg>
      </div>
      <div class="relative z-10">
        <h1 class="text-4xl md:text-5xl font-bold mb-4 animate-fade-in-up">{{ pageTitle }}</h1>
        <p class="text-xl md:text-2xl text-blue-100 animate-fade-in-up" style="animation-delay: 0.2s;">
          {{ pageDescription }}
        </p>
      </div>
    </div>

    <!-- Timeline del Proceso de Abordaje -->
    <div class="airline-card mb-12">
      <h2 class="text-3xl font-bold text-center mb-8">Proceso de Abordaje</h2>
      
      <div class="relative">
        <!-- Línea vertical -->
        <div class="absolute left-8 top-0 bottom-0 w-0.5 bg-blue-200"></div>
        
        <div class="space-y-8">
          <!-- Paso 1 -->
          <div class="flex items-start gap-6">
            <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl z-10">
              1
            </div>
            <div class="flex-1">
              <h3 class="text-2xl font-bold text-gray-800 mb-2">Llegada al Aeropuerto</h3>
              <p class="text-gray-600 mb-4">Llega al aeropuerto con suficiente anticipación</p>
              <div class="bg-blue-50 p-4 rounded-lg">
                <ul class="space-y-2 text-sm">
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Vuelos nacionales: 2 horas antes</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Vuelos internacionales: 3 horas antes</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Paso 2 -->
          <div class="flex items-start gap-6">
            <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl z-10">
              2
            </div>
            <div class="flex-1">
              <h3 class="text-2xl font-bold text-gray-800 mb-2">Check-in y Documentación</h3>
              <p class="text-gray-600 mb-4">Proceso de verificación de documentos y asignación de asiento</p>
              <div class="bg-blue-50 p-4 rounded-lg">
                <ul class="space-y-2 text-sm">
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Presenta tu identificación y boleto</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Verifica tu asiento asignado</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Factura tu equipaje si es necesario</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Paso 3 -->
          <div class="flex items-start gap-6">
            <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl z-10">
              3
            </div>
            <div class="flex-1">
              <h3 class="text-2xl font-bold text-gray-800 mb-2">Control de Seguridad</h3>
              <p class="text-gray-600 mb-4">Revisión de seguridad antes de acceder a la zona de embarque</p>
              <div class="bg-blue-50 p-4 rounded-lg">
                <ul class="space-y-2 text-sm">
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Retira objetos metálicos y electrónicos</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Coloca líquidos en bolsas transparentes</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Sigue las instrucciones del personal</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Paso 4 -->
          <div class="flex items-start gap-6">
            <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl z-10">
              4
            </div>
            <div class="flex-1">
              <h3 class="text-2xl font-bold text-gray-800 mb-2">Zona de Embarque</h3>
              <p class="text-gray-600 mb-4">Espera en la puerta de embarque asignada</p>
              <div class="bg-blue-50 p-4 rounded-lg">
                <ul class="space-y-2 text-sm">
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Ubica tu puerta de embarque</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Escucha los anuncios de embarque</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Mantén tu boleto y documentos a la mano</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Paso 5 -->
          <div class="flex items-start gap-6">
            <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl z-10">
              5
            </div>
            <div class="flex-1">
              <h3 class="text-2xl font-bold text-gray-800 mb-2">Abordaje</h3>
              <p class="text-gray-600 mb-4">Proceso de abordaje por grupos</p>
              <div class="bg-blue-50 p-4 rounded-lg">
                <ul class="space-y-2 text-sm">
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Grupo 1: Premium y Business</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Grupo 2: Economy (asientos traseros)</span>
                  </li>
                  <li class="flex items-center gap-2">
                    <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                    </svg>
                    <span>Grupo 3: Economy (asientos delanteros)</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Horarios de Abordaje -->
    <div class="grid md:grid-cols-2 gap-8 mb-12">
      <div class="airline-card">
        <h3 class="text-2xl font-bold mb-6 text-blue-600">Horarios de Abordaje</h3>
        
        <div class="space-y-4">
          <div class="flex justify-between items-center p-4 bg-blue-50 rounded-lg">
            <span class="font-semibold">Vuelos Nacionales</span>
            <span class="text-blue-600 font-bold">30 min antes</span>
          </div>
          <div class="flex justify-between items-center p-4 bg-orange-50 rounded-lg">
            <span class="font-semibold">Vuelos Internacionales</span>
            <span class="text-orange-600 font-bold">45 min antes</span>
          </div>
          <div class="flex justify-between items-center p-4 bg-purple-50 rounded-lg">
            <span class="font-semibold">Vuelos de Conexión</span>
            <span class="text-purple-600 font-bold">60 min antes</span>
          </div>
        </div>
      </div>

      <div class="airline-card">
        <h3 class="text-2xl font-bold mb-6 text-green-600">Documentos Requeridos</h3>
        
        <div class="space-y-3">
          <div class="flex items-center gap-3">
            <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span>Boleto de avión (impreso o digital)</span>
          </div>
          <div class="flex items-center gap-3">
            <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span>Identificación oficial vigente</span>
          </div>
          <div class="flex items-center gap-3">
            <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span>Pasaporte (vuelos internacionales)</span>
          </div>
          <div class="flex items-center gap-3">
            <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span>Visa (si es requerida)</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Consejos Importantes -->
    <div class="airline-card">
      <h2 class="text-3xl font-bold text-center mb-8">Consejos Importantes</h2>
      
      <div class="grid md:grid-cols-3 gap-6">
        <div class="text-center">
          <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-red-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.721-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-red-600">No llegues tarde</h3>
          <p class="text-gray-600 text-sm">El avión no espera. Si llegas tarde, perderás tu vuelo sin reembolso.</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-yellow-600">Escucha los anuncios</h3>
          <p class="text-gray-600 text-sm">Los horarios pueden cambiar. Mantente atento a los anuncios del aeropuerto.</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-green-600">Ten paciencia</h3>
          <p class="text-gray-600 text-sm">El proceso puede tomar tiempo. Mantén la calma y sigue las instrucciones.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { informativePagesApi } from '../../utils/informativePagesApi';

// State
const loading = ref(false);
const error = ref('');
const pageTitle = ref('Instrucciones de Abordaje');
const pageDescription = ref('Guía completa para el proceso de abordaje exitoso');

// Methods
const loadPageContent = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const page = await informativePagesApi.getPageBySlug('boarding-instructions');
    pageTitle.value = page.title;
    pageDescription.value = page.description;
  } catch (err) {
    console.error('Error loading page content:', err);
    // Keep default values if API fails
  } finally {
    loading.value = false;
  }
};

// Meta tags para SEO
onMounted(() => {
  loadPageContent();
  
  document.title = `${pageTitle.value} - AeroLinea`;
  
  // Actualizar meta description
  const metaDescription = document.querySelector('meta[name="description"]');
  if (metaDescription) {
    metaDescription.setAttribute('content', pageDescription.value);
  }
});
</script>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.6s ease-out forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.airline-gradient-sky {
  background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 50%, #1d4ed8 100%);
}

.airline-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
}
</style>
