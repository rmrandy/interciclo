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

    <!-- Tipos de Asientos -->
    <div class="grid lg:grid-cols-3 gap-8 mb-12">
      <!-- Economy Class -->
      <div class="airline-card group hover:scale-105 transition-all duration-300">
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-primary rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Economy Class</h3>
          <p class="text-gray-600 mb-4">La opción perfecta para viajes cómodos y económicos</p>
          <div class="text-3xl font-bold text-blue-600 mb-2">Desde Q299</div>
        </div>
        
        <div class="space-y-4">
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Asiento estándar con espacio adecuado</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Entretenimiento a bordo</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Refrigerio incluido</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Equipaje de mano incluido</span>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <button class="w-full btn-primary">Seleccionar Economy</button>
        </div>
      </div>

      <!-- Business Class -->
      <div class="airline-card airline-card-premium group hover:scale-105 transition-all duration-300 relative">
        <div class="absolute -top-3 left-1/2 transform -translate-x-1/2">
          <span class="bg-gradient-to-r from-yellow-400 to-orange-500 text-white px-4 py-1 rounded-full text-sm font-semibold">
            Más Popular
          </span>
        </div>
        
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-secondary rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Business Class</h3>
          <p class="text-gray-600 mb-4">Lujo y comodidad para viajes de negocios</p>
          <div class="text-3xl font-bold text-orange-600 mb-2">Desde Q899</div>
        </div>
        
        <div class="space-y-4">
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Asiento reclinable con más espacio</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Entretenimiento premium</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Comida gourmet</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Equipaje de 32kg incluido</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Acceso a salas VIP</span>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <button class="w-full btn-primary">Seleccionar Business</button>
        </div>
      </div>

      <!-- Premium Class -->
      <div class="airline-card group hover:scale-105 transition-all duration-300">
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-accent rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Premium Class</h3>
          <p class="text-gray-600 mb-4">La máxima experiencia de vuelo</p>
          <div class="text-3xl font-bold text-purple-600 mb-2">Desde Q1,299</div>
        </div>
        
        <div class="space-y-4">
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Asiento cama completamente reclinable</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Entretenimiento de lujo</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Menú de chef a la carta</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Equipaje ilimitado</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Servicio personalizado</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Embarque prioritario</span>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <button class="w-full btn-primary">Seleccionar Premium</button>
        </div>
      </div>
    </div>

    <!-- Comparación de Asientos -->
    <div class="airline-card mb-12">
      <h2 class="text-3xl font-bold text-center mb-8">Comparación de Asientos</h2>
      
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b-2 border-gray-200">
              <th class="text-left py-4 px-6 font-semibold text-gray-800">Características</th>
              <th class="text-center py-4 px-6 font-semibold text-blue-600">Economy</th>
              <th class="text-center py-4 px-6 font-semibold text-orange-600">Business</th>
              <th class="text-center py-4 px-6 font-semibold text-purple-600">Premium</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr>
              <td class="py-4 px-6 font-medium">Ancho del asiento</td>
              <td class="text-center py-4 px-6">43 cm</td>
              <td class="text-center py-4 px-6">56 cm</td>
              <td class="text-center py-4 px-6">76 cm</td>
            </tr>
            <tr class="bg-gray-50">
              <td class="py-4 px-6 font-medium">Espacio para piernas</td>
              <td class="text-center py-4 px-6">81 cm</td>
              <td class="text-center py-4 px-6">96 cm</td>
              <td class="text-center py-4 px-6">193 cm</td>
            </tr>
            <tr>
              <td class="py-4 px-6 font-medium">Reclinación</td>
              <td class="text-center py-4 px-6">6°</td>
              <td class="text-center py-4 px-6">45°</td>
              <td class="text-center py-4 px-6">180° (cama)</td>
            </tr>
            <tr class="bg-gray-50">
              <td class="py-4 px-6 font-medium">Entretenimiento</td>
              <td class="text-center py-4 px-6">Pantalla 9"</td>
              <td class="text-center py-4 px-6">Pantalla 15"</td>
              <td class="text-center py-4 px-6">Pantalla 18"</td>
            </tr>
            <tr>
              <td class="py-4 px-6 font-medium">Equipaje incluido</td>
              <td class="text-center py-4 px-6">8 kg</td>
              <td class="text-center py-4 px-6">32 kg</td>
              <td class="text-center py-4 px-6">Ilimitado</td>
            </tr>
            <tr class="bg-gray-50">
              <td class="py-4 px-6 font-medium">Comida</td>
              <td class="text-center py-4 px-6">Refrigerio</td>
              <td class="text-center py-4 px-6">Gourmet</td>
              <td class="text-center py-4 px-6">Chef a la carta</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Consejos para elegir asiento -->
    <div class="airline-card">
      <h2 class="text-3xl font-bold text-center mb-8">Consejos para Elegir tu Asiento</h2>
      
      <div class="grid md:grid-cols-2 gap-8">
        <div>
          <h3 class="text-xl font-semibold mb-4 text-blue-600">Para viajes cortos (1-3 horas)</h3>
          <ul class="space-y-2 text-gray-700">
            <li class="flex items-start gap-2">
              <span class="text-blue-500 mt-1">•</span>
              <span>Economy Class es perfecta para viajes cortos</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-blue-500 mt-1">•</span>
              <span>Elige asientos de ventana para mejor vista</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-blue-500 mt-1">•</span>
              <span>Evita asientos del medio si es posible</span>
            </li>
          </ul>
        </div>
        
        <div>
          <h3 class="text-xl font-semibold mb-4 text-orange-600">Para viajes largos (4+ horas)</h3>
          <ul class="space-y-2 text-gray-700">
            <li class="flex items-start gap-2">
              <span class="text-orange-500 mt-1">•</span>
              <span>Business o Premium para mayor comodidad</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-orange-500 mt-1">•</span>
              <span>Asientos de pasillo para movilidad</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-orange-500 mt-1">•</span>
              <span>Considera asientos con más espacio para piernas</span>
            </li>
          </ul>
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
const pageTitle = ref('Tipos de Asientos');
const pageDescription = ref('Descubre la comodidad perfecta para tu viaje');

// Methods
const loadPageContent = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const page = await informativePagesApi.getPageBySlug('seat-types');
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

.airline-gradient-primary {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}

.airline-gradient-secondary {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.airline-gradient-accent {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.airline-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
}

.airline-card-premium {
  border: 2px solid #fed7aa;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.btn-primary {
  background: #2563eb;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  background: #1d4ed8;
}
</style>
