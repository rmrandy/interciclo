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
          Recomendaciones útiles para hacer tu viaje más cómodo y seguro
        </p>
      </div>
    </div>

    <!-- Consejos por Categoría -->
    <div class="grid lg:grid-cols-2 gap-8 mb-12">
      <!-- Antes del Viaje -->
      <div class="airline-card">
        <h2 class="text-2xl font-bold mb-6 text-blue-600">Antes del Viaje</h2>
        
        <div class="space-y-6">
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg mb-2">Verifica Documentos</h3>
              <p class="text-gray-600 text-sm">Confirma que tu pasaporte, visa y otros documentos estén vigentes y en orden.</p>
            </div>
          </div>

          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg mb-2">Haz Check-in Temprano</h3>
              <p class="text-gray-600 text-sm">Haz check-in online 24 horas antes para tener más opciones de asientos.</p>
            </div>
          </div>

          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm0 4a1 1 0 011-1h12a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1V8zm8 2a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg mb-2">Revisa el Clima</h3>
              <p class="text-gray-600 text-sm">Consulta el pronóstico del tiempo en tu destino para empacar adecuadamente.</p>
            </div>
          </div>

          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-purple-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg mb-2">Información de Salud</h3>
              <p class="text-gray-600 text-sm">Investiga si necesitas vacunas o medicamentos especiales para tu destino.</p>
            </div>
          </div>
        </div>
      </div>

      <!-- En el Aeropuerto -->
      <div class="airline-card">
        <h2 class="text-2xl font-bold mb-6 text-orange-600">En el Aeropuerto</h2>
        
        <div class="space-y-6">
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-orange-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg mb-2">Llega Temprano</h3>
              <p class="text-gray-600 text-sm">Llega 2 horas antes para vuelos nacionales y 3 horas para internacionales.</p>
            </div>
          </div>

          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.721-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg mb-2">Evita Objetos Prohibidos</h3>
              <p class="text-gray-600 text-sm">Revisa la lista de artículos prohibidos antes de empacar.</p>
            </div>
          </div>

          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg mb-2">Mantén Documentos a Mano</h3>
              <p class="text-gray-600 text-sm">Ten tu boleto e identificación fácilmente accesibles.</p>
            </div>
          </div>

          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-lg mb-2">Escucha Anuncios</h3>
              <p class="text-gray-600 text-sm">Presta atención a los anuncios de puertas y horarios de vuelo.</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Durante el Vuelo -->
    <div class="airline-card mb-12">
      <h2 class="text-2xl font-bold mb-6 text-purple-600">Durante el Vuelo</h2>
      
      <div class="grid md:grid-cols-3 gap-6">
        <div class="text-center">
          <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-purple-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm0 4a1 1 0 011-1h12a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1V8zm8 2a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-purple-600">Hidrátate</h3>
          <p class="text-gray-600 text-sm">Bebe agua regularmente para mantenerte hidratado durante el vuelo.</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 2a8 8 0 100 16 8 8 0 000-16zm1 5H9v2H7v2h2v2h2v-2h2V9h-2V7z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-blue-600">Muévete</h3>
          <p class="text-gray-600 text-sm">Levántate y camina por el pasillo cada hora en vuelos largos.</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-green-600">Relájate</h3>
          <p class="text-gray-600 text-sm">Usa entretenimiento a bordo o lleva tu propio contenido.</p>
        </div>
      </div>
    </div>

    <!-- Consejos de Salud -->
    <div class="grid md:grid-cols-2 gap-8 mb-12">
      <div class="airline-card">
        <h3 class="text-2xl font-bold mb-6 text-red-600">Consejos de Salud</h3>
        
        <div class="space-y-4">
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-red-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-700">Lleva medicamentos esenciales en tu equipaje de mano</span>
          </div>
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-red-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-700">Usa ropa cómoda y en capas</span>
          </div>
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-red-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-700">Evita alcohol y cafeína en exceso</span>
          </div>
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-red-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-700">Considera usar medias de compresión en vuelos largos</span>
          </div>
        </div>
      </div>

      <div class="airline-card">
        <h3 class="text-2xl font-bold mb-6 text-blue-600">Consejos de Seguridad</h3>
        
        <div class="space-y-4">
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-700">Mantén objetos de valor contigo</span>
          </div>
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-700">No dejes equipaje desatendido</span>
          </div>
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-700">Sigue las instrucciones de la tripulación</span>
          </div>
          <div class="flex items-start gap-3">
            <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span class="text-gray-700">Mantén el cinturón abrochado cuando esté indicado</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Consejos para Viajeros Frecuentes -->
    <div class="airline-card">
      <h2 class="text-3xl font-bold text-center mb-8">Consejos para Viajeros Frecuentes</h2>
      
      <div class="grid md:grid-cols-2 gap-8">
        <div>
          <h3 class="text-2xl font-bold mb-6 text-green-600">Optimiza tu Experiencia</h3>
          <ul class="space-y-3">
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Únete a nuestro programa de fidelidad AeroLinea Rewards</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Descarga nuestra app móvil para check-in rápido</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Configura alertas de vuelo para estar informado</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Considera el seguro de viaje para mayor tranquilidad</span>
            </li>
          </ul>
        </div>

        <div>
          <h3 class="text-2xl font-bold mb-6 text-purple-600">Ahorra Tiempo y Dinero</h3>
          <ul class="space-y-3">
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-purple-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Reserva con anticipación para mejores precios</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-purple-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Viaja en temporada baja cuando sea posible</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-purple-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Usa tarjetas de crédito con beneficios de viaje</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-purple-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Considera vuelos de conexión para ahorrar</span>
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
const pageTitle = ref('Consejos de Viaje');
const pageDescription = ref('Consejos útiles para hacer tu viaje más placentero');

// Methods
const loadPageContent = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const page = await informativePagesApi.getPageBySlug('travel-tips');
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
