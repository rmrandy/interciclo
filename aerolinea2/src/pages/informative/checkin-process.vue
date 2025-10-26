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

    <!-- Opciones de Check-in -->
    <div class="grid lg:grid-cols-3 gap-8 mb-12">
      <!-- Check-in Online -->
      <div class="airline-card group hover:scale-105 transition-all duration-300">
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-primary rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Check-in Online</h3>
          <p class="text-gray-600 mb-4">La forma más rápida y conveniente</p>
          <div class="text-3xl font-bold text-blue-600 mb-2">24 horas antes</div>
        </div>
        
        <div class="space-y-4">
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Acceso desde cualquier dispositivo</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Selecciona tu asiento preferido</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Recibe tu tarjeta de embarque digital</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Evita filas en el aeropuerto</span>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <button class="w-full btn-primary">Hacer Check-in Online</button>
        </div>
      </div>

      <!-- Check-in en Aeropuerto -->
      <div class="airline-card airline-card-premium group hover:scale-105 transition-all duration-300 relative">
        <div class="absolute -top-3 left-1/2 transform -translate-x-1/2">
          <span class="bg-gradient-to-r from-yellow-400 to-orange-500 text-white px-4 py-1 rounded-full text-sm font-semibold">
            Recomendado
          </span>
        </div>
        
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-secondary rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Check-in en Aeropuerto</h3>
          <p class="text-gray-600 mb-4">Asistencia personalizada en mostradores</p>
          <div class="text-3xl font-bold text-orange-600 mb-2">2-3 horas antes</div>
        </div>
        
        <div class="space-y-4">
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Asistencia del personal especializado</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Facturación de equipaje</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Resolución de problemas</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Tarjeta de embarque impresa</span>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <button class="w-full btn-primary">Ir al Aeropuerto</button>
        </div>
      </div>

      <!-- Check-in Automático -->
      <div class="airline-card group hover:scale-105 transition-all duration-300">
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-accent rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Check-in Automático</h3>
          <p class="text-gray-600 mb-4">Máquinas de autoservicio en el aeropuerto</p>
          <div class="text-3xl font-bold text-purple-600 mb-2">1-2 horas antes</div>
        </div>
        
        <div class="space-y-4">
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Proceso rápido y sencillo</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Múltiples idiomas disponibles</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Facturación de equipaje incluida</span>
          </div>
          <div class="flex items-center gap-3">
            <div class="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
            </div>
            <span class="text-gray-700">Asistencia disponible si necesitas ayuda</span>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <button class="w-full btn-primary">Usar Máquina</button>
        </div>
      </div>
    </div>

    <!-- Guía Paso a Paso -->
    <div class="airline-card mb-12">
      <h2 class="text-3xl font-bold text-center mb-8">Guía Paso a Paso</h2>
      
      <div class="grid md:grid-cols-2 gap-8">
        <!-- Check-in Online -->
        <div>
          <h3 class="text-2xl font-bold mb-6 text-blue-600">Check-in Online</h3>
          <div class="space-y-4">
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                1
              </div>
              <div>
                <h4 class="font-semibold mb-1">Accede a nuestro sitio web</h4>
                <p class="text-gray-600 text-sm">Ve a la sección de check-in online</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                2
              </div>
              <div>
                <h4 class="font-semibold mb-1">Ingresa tus datos</h4>
                <p class="text-gray-600 text-sm">Número de confirmación y apellido</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                3
              </div>
              <div>
                <h4 class="font-semibold mb-1">Selecciona tu asiento</h4>
                <p class="text-gray-600 text-sm">Elige el asiento que prefieras</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                4
              </div>
              <div>
                <h4 class="font-semibold mb-1">Confirma y descarga</h4>
                <p class="text-gray-600 text-sm">Recibe tu tarjeta de embarque digital</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Check-in en Aeropuerto -->
        <div>
          <h3 class="text-2xl font-bold mb-6 text-orange-600">Check-in en Aeropuerto</h3>
          <div class="space-y-4">
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-orange-600 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                1
              </div>
              <div>
                <h4 class="font-semibold mb-1">Localiza el mostrador</h4>
                <p class="text-gray-600 text-sm">Busca el mostrador de AeroLinea</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-orange-600 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                2
              </div>
              <div>
                <h4 class="font-semibold mb-1">Presenta documentos</h4>
                <p class="text-gray-600 text-sm">Boleto e identificación oficial</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-orange-600 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                3
              </div>
              <div>
                <h4 class="font-semibold mb-1">Factura equipaje</h4>
                <p class="text-gray-600 text-sm">Si llevas equipaje facturado</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-orange-600 rounded-full flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                4
              </div>
              <div>
                <h4 class="font-semibold mb-1">Recibe tu tarjeta</h4>
                <p class="text-gray-600 text-sm">Tarjeta de embarque impresa</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Información Importante -->
    <div class="grid md:grid-cols-2 gap-8 mb-12">
      <div class="airline-card">
        <h3 class="text-2xl font-bold mb-6 text-red-600">Restricciones de Tiempo</h3>
        
        <div class="space-y-4">
          <div class="flex justify-between items-center p-4 bg-red-50 rounded-lg">
            <span class="font-semibold">Check-in Online</span>
            <span class="text-red-600 font-bold">24h - 1h antes</span>
          </div>
          <div class="flex justify-between items-center p-4 bg-orange-50 rounded-lg">
            <span class="font-semibold">Check-in en Aeropuerto</span>
            <span class="text-orange-600 font-bold">3h - 45min antes</span>
          </div>
          <div class="flex justify-between items-center p-4 bg-yellow-50 rounded-lg">
            <span class="font-semibold">Check-in Automático</span>
            <span class="text-yellow-600 font-bold">2h - 30min antes</span>
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

    <!-- Consejos Útiles -->
    <div class="airline-card">
      <h2 class="text-3xl font-bold text-center mb-8">Consejos Útiles</h2>
      
      <div class="grid md:grid-cols-3 gap-6">
        <div class="text-center">
          <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-blue-600">Haz check-in temprano</h3>
          <p class="text-gray-600 text-sm">Haz check-in tan pronto como sea posible para tener más opciones de asientos.</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-green-600">Guarda tu tarjeta</h3>
          <p class="text-gray-600 text-sm">Guarda una copia de tu tarjeta de embarque en tu teléfono como respaldo.</p>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-purple-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-purple-600">Verifica tu vuelo</h3>
          <p class="text-gray-600 text-sm">Confirma los horarios de tu vuelo antes de ir al aeropuerto.</p>
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
const pageTitle = ref('Proceso de Check-in');
const pageDescription = ref('Conoce el proceso de check-in paso a paso');

// Methods
const loadPageContent = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const page = await informativePagesApi.getPageBySlug('checkin-process');
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
