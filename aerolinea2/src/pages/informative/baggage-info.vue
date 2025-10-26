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

    <!-- Tipos de Equipaje -->
    <div class="grid lg:grid-cols-3 gap-8 mb-12">
      <!-- Equipaje de Mano -->
      <div class="airline-card group hover:scale-105 transition-all duration-300">
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-primary rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Equipaje de Mano</h3>
          <p class="text-gray-600 mb-4">Lo que puedes llevar contigo en cabina</p>
        </div>
        
        <div class="space-y-4">
          <div class="bg-blue-50 p-4 rounded-lg">
            <h4 class="font-semibold text-blue-800 mb-2">Dimensiones permitidas</h4>
            <p class="text-sm text-blue-700">55 x 40 x 20 cm (21.5 x 15.5 x 8 pulgadas)</p>
          </div>
          <div class="bg-blue-50 p-4 rounded-lg">
            <h4 class="font-semibold text-blue-800 mb-2">Peso máximo</h4>
            <p class="text-sm text-blue-700">8 kg (17.6 libras)</p>
          </div>
          <div class="bg-blue-50 p-4 rounded-lg">
            <h4 class="font-semibold text-blue-800 mb-2">Cantidad</h4>
            <p class="text-sm text-blue-700">1 pieza por pasajero</p>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <h4 class="font-semibold mb-3 text-gray-800">Incluye:</h4>
          <ul class="space-y-2 text-sm text-gray-600">
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Artículos personales</span>
            </li>
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Dispositivos electrónicos</span>
            </li>
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Medicamentos</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- Equipaje Facturado -->
      <div class="airline-card airline-card-premium group hover:scale-105 transition-all duration-300 relative">
        <div class="absolute -top-3 left-1/2 transform -translate-x-1/2">
          <span class="bg-gradient-to-r from-yellow-400 to-orange-500 text-white px-4 py-1 rounded-full text-sm font-semibold">
            Más Popular
          </span>
        </div>
        
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-secondary rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Equipaje Facturado</h3>
          <p class="text-gray-600 mb-4">Equipaje que va en la bodega del avión</p>
        </div>
        
        <div class="space-y-4">
          <div class="bg-orange-50 p-4 rounded-lg">
            <h4 class="font-semibold text-orange-800 mb-2">Dimensiones permitidas</h4>
            <p class="text-sm text-orange-700">158 cm (62 pulgadas) suma de las 3 dimensiones</p>
          </div>
          <div class="bg-orange-50 p-4 rounded-lg">
            <h4 class="font-semibold text-orange-800 mb-2">Peso máximo</h4>
            <p class="text-sm text-orange-700">23 kg (50 libras) por pieza</p>
          </div>
          <div class="bg-orange-50 p-4 rounded-lg">
            <h4 class="font-semibold text-orange-800 mb-2">Cantidad</h4>
            <p class="text-sm text-orange-700">1 pieza incluida (Economy)</p>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <h4 class="font-semibold mb-3 text-gray-800">Incluye:</h4>
          <ul class="space-y-2 text-sm text-gray-600">
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Ropa y artículos personales</span>
            </li>
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Objetos frágiles (bien empacados)</span>
            </li>
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Líquidos en cantidades mayores</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- Equipaje Especial -->
      <div class="airline-card group hover:scale-105 transition-all duration-300">
        <div class="text-center mb-6">
          <div class="w-20 h-20 airline-gradient-accent rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">Equipaje Especial</h3>
          <p class="text-gray-600 mb-4">Artículos que requieren manejo especial</p>
        </div>
        
        <div class="space-y-4">
          <div class="bg-purple-50 p-4 rounded-lg">
            <h4 class="font-semibold text-purple-800 mb-2">Instrumentos musicales</h4>
            <p class="text-sm text-purple-700">Requieren reserva previa</p>
          </div>
          <div class="bg-purple-50 p-4 rounded-lg">
            <h4 class="font-semibold text-purple-800 mb-2">Equipos deportivos</h4>
            <p class="text-sm text-purple-700">Bicicletas, esquíes, tablas de surf</p>
          </div>
          <div class="bg-purple-50 p-4 rounded-lg">
            <h4 class="font-semibold text-purple-800 mb-2">Equipos médicos</h4>
            <p class="text-sm text-purple-700">Sillas de ruedas, oxígeno</p>
          </div>
        </div>
        
        <div class="mt-6 pt-6 border-t border-gray-200">
          <h4 class="font-semibold mb-3 text-gray-800">Requisitos:</h4>
          <ul class="space-y-2 text-sm text-gray-600">
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Reserva previa obligatoria</span>
            </li>
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Documentación especial</span>
            </li>
            <li class="flex items-center gap-2">
              <svg class="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Cargo adicional aplicable</span>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Restricciones de Líquidos -->
    <div class="airline-card mb-12">
      <h2 class="text-3xl font-bold text-center mb-8">Restricciones de Líquidos</h2>
      
      <div class="grid md:grid-cols-2 gap-8">
        <div>
          <h3 class="text-2xl font-bold mb-6 text-red-600">Líquidos en Equipaje de Mano</h3>
          <div class="space-y-4">
            <div class="bg-red-50 p-4 rounded-lg">
              <h4 class="font-semibold text-red-800 mb-2">Regla 3-1-1</h4>
              <ul class="text-sm text-red-700 space-y-1">
                <li>• Máximo 100ml por contenedor</li>
                <li>• Todos los líquidos en 1 bolsa transparente</li>
                <li>• 1 bolsa por pasajero</li>
                <li>• Bolsa de máximo 1 litro</li>
              </ul>
            </div>
            <div class="bg-yellow-50 p-4 rounded-lg">
              <h4 class="font-semibold text-yellow-800 mb-2">Excepciones</h4>
              <ul class="text-sm text-yellow-700 space-y-1">
                <li>• Medicamentos líquidos</li>
                <li>• Leche para bebés</li>
                <li>• Líquidos comprados después del control</li>
              </ul>
            </div>
          </div>
        </div>

        <div>
          <h3 class="text-2xl font-bold mb-6 text-green-600">Líquidos en Equipaje Facturado</h3>
          <div class="space-y-4">
            <div class="bg-green-50 p-4 rounded-lg">
              <h4 class="font-semibold text-green-800 mb-2">Sin restricciones</h4>
              <ul class="text-sm text-green-700 space-y-1">
                <li>• Cualquier cantidad de líquidos</li>
                <li>• Bebidas alcohólicas</li>
                <li>• Perfumes y cosméticos</li>
                <li>• Productos de limpieza</li>
              </ul>
            </div>
            <div class="bg-blue-50 p-4 rounded-lg">
              <h4 class="font-semibold text-blue-800 mb-2">Recomendaciones</h4>
              <ul class="text-sm text-blue-700 space-y-1">
                <li>• Empaque bien los líquidos</li>
                <li>• Use bolsas herméticas</li>
                <li>• Proteja botellas frágiles</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Artículos Prohibidos -->
    <div class="airline-card mb-12">
      <h2 class="text-3xl font-bold text-center mb-8">Artículos Prohibidos</h2>
      
      <div class="grid md:grid-cols-3 gap-6">
        <div class="text-center">
          <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-red-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M13.477 14.89A6 6 0 015.11 6.524l8.367 8.368zm1.414-1.414L6.524 5.11a6 6 0 018.367 8.367zM18 10a8 8 0 11-16 0 8 8 0 0116 0z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-red-600">Armas y Objetos Peligrosos</h3>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>• Armas de fuego</li>
            <li>• Cuchillos y navajas</li>
            <li>• Herramientas afiladas</li>
            <li>• Objetos punzantes</li>
          </ul>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-orange-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-orange-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-orange-600">Líquidos y Gases</h3>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>• Combustibles</li>
            <li>• Gases comprimidos</li>
            <li>• Líquidos inflamables</li>
            <li>• Pinturas y solventes</li>
          </ul>
        </div>
        
        <div class="text-center">
          <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-purple-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2 text-purple-600">Sustancias Químicas</h3>
          <ul class="text-sm text-gray-600 space-y-1">
            <li>• Explosivos</li>
            <li>• Materiales tóxicos</li>
            <li>• Sustancias corrosivas</li>
            <li>• Baterías de litio grandes</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Consejos para Empacar -->
    <div class="airline-card">
      <h2 class="text-3xl font-bold text-center mb-8">Consejos para Empacar</h2>
      
      <div class="grid md:grid-cols-2 gap-8">
        <div>
          <h3 class="text-2xl font-bold mb-6 text-blue-600">Equipaje de Mano</h3>
          <ul class="space-y-3">
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Lleva objetos de valor y documentos importantes</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Incluye un cambio de ropa por si se pierde el equipaje</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Medicamentos y artículos de higiene personal</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-blue-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Dispositivos electrónicos y cargadores</span>
            </li>
          </ul>
        </div>

        <div>
          <h3 class="text-2xl font-bold mb-6 text-green-600">Equipaje Facturado</h3>
          <ul class="space-y-3">
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Etiqueta claramente con tu información de contacto</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Usa candados aprobados por TSA</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Empaca objetos frágiles en el centro</span>
            </li>
            <li class="flex items-start gap-3">
              <svg class="w-5 h-5 text-green-600 mt-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
              </svg>
              <span>Distribuye el peso uniformemente</span>
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
const pageTitle = ref('Información de Equipaje');
const pageDescription = ref('Políticas y restricciones de equipaje para tu vuelo');

// Methods
const loadPageContent = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const page = await informativePagesApi.getPageBySlug('baggage-info');
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
</style>
