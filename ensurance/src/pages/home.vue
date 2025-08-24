<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const user = ref<any>(null);
const isAdmin = ref(false);
const isEmployee = ref(false);
const isLoading = ref(true);
const policyDetails = ref<any>(null);
const error = ref<string | null>(null);
import { getInsuranceApiUrl } from "../utils/api";
const fetchUserPolicyDetails = async () => {
  if (!user.value || !user.value.policy || !user.value.policy.idPolicy) {
    isLoading.value = false;
    return;
  }
  
  try {
    const response = await axios.get(getInsuranceApiUrl(`/policy?id=${user.value.policy.idPolicy}`));
    if (response.data) {
      
      policyDetails.value = response.data;
    }
  } catch (err) {
    console.error("Error al cargar detalles de la póliza:", err);
    error.value = "No se pudieron cargar los detalles de tu póliza.";
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  // Obtener datos de usuario del localStorage (opcional para página pública)
  const userData = localStorage.getItem("user");
  if (userData) {
    try {
      user.value = JSON.parse(userData);
      isAdmin.value = user.value.role === "ADMIN";
      isEmployee.value = user.value.role === "EMPLOYEE" || user.value.role === "ADMIN";
      
      // Cargar detalles de la póliza solo si está logueado
      fetchUserPolicyDetails();
    } catch (e) {
      console.error("Error al parsear datos de usuario:", e);
      isLoading.value = false;
    }
  } else {
    // No hay usuario logueado, página sigue siendo accesible
    isLoading.value = false;
  }
});

// Formatear precio
const formatPrice = (price: number | null | undefined): string => {
  if (!price) return "N/A";
  return `Q${price.toFixed(2)}`;
};

// Formatear fecha
const formatDate = (dateString: string | null | undefined): string => {
  if (!dateString) return "N/A";
  const date = new Date(dateString);
  return date.toLocaleDateString();
};

// Navegación a vuelos
const navigateToFlights = () => {
  router.push('/flights');
};

// Intentar reservar vuelo (requiere login)
const tryBookFlight = () => {
  if (user.value) {
    // Usuario logueado, ir a vuelos
    router.push('/flights');
  } else {
    // Usuario no logueado, redirigir a login con parámetro de retorno
    router.push('/login?redirect=' + encodeURIComponent('/flights'));
  }
};
</script>

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
        <h1 class="text-4xl md:text-5xl font-bold mb-4 animate-fade-in-up">¡Bienvenido a AeroLinea!</h1>
        <p class="text-xl md:text-2xl mb-6 text-blue-100 animate-fade-in-up" style="animation-delay: 0.2s;">Tu compañía de seguros aeronáuticos de confianza</p>
        <div class="flex flex-wrap gap-3 animate-fade-in-up" style="animation-delay: 0.4s;">
          <div class="bg-white bg-opacity-20 px-4 py-2 rounded-full backdrop-blur-sm">
            <span class="font-semibold">🛡️ Protección Total</span>
          </div>
          <div class="bg-white bg-opacity-20 px-4 py-2 rounded-full backdrop-blur-sm">
            <span class="font-semibold">✈️ Cobertura Global</span>
          </div>
          <div class="bg-white bg-opacity-20 px-4 py-2 rounded-full backdrop-blur-sm">
            <span class="font-semibold">⚡ Atención 24/7</span>
          </div>
        </div>
        
        <!-- Botones de acción para usuarios no autenticados -->
        <div v-if="!user" class="mt-8 flex flex-wrap gap-4 animate-fade-in-up" style="animation-delay: 0.6s;">
          <button 
            @click="navigateToFlights"
            class="bg-white text-blue-800 px-6 py-3 rounded-full font-semibold hover:bg-blue-50 transition-colors flex items-center gap-2"
          >
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
            Ver Vuelos Disponibles
          </button>
          <button 
            @click="tryBookFlight"
            class="bg-blue-900 text-white px-6 py-3 rounded-full font-semibold hover:bg-blue-800 transition-colors flex items-center gap-2"
          >
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6z"/>
            </svg>
            Reservar Ahora
          </button>
        </div>
      </div>
    </div>
    
    <!-- Información para usuarios no autenticados -->
    <div v-if="!user" class="grid md:grid-cols-3 gap-6 mb-8">
      <!-- Ver Vuelos -->
      <div class="airline-card hover:scale-105 transition-transform cursor-pointer" @click="navigateToFlights">
        <div class="text-center">
          <div class="w-16 h-16 airline-gradient-primary rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-gray-800 mb-2">Consultar Vuelos</h3>
          <p class="text-gray-600 mb-4">Explora nuestra amplia oferta de vuelos nacionales e internacionales.</p>
          <div class="bg-blue-50 text-blue-800 px-3 py-1 rounded-full text-sm font-medium">Sin registro requerido</div>
        </div>
      </div>

      <!-- Reservar -->
      <div class="airline-card hover:scale-105 transition-transform cursor-pointer" @click="tryBookFlight">
        <div class="text-center">
          <div class="w-16 h-16 airline-gradient-secondary rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-gray-800 mb-2">Reservar Vuelo</h3>
          <p class="text-gray-600 mb-4">Asegura tu lugar en el vuelo de tu elección con unos pocos clics.</p>
          <div class="bg-orange-50 text-orange-800 px-3 py-1 rounded-full text-sm font-medium">Requiere registro</div>
        </div>
      </div>

      <!-- Registrarse -->
      <div class="airline-card hover:scale-105 transition-transform cursor-pointer" @click="router.push('/register')">
        <div class="text-center">
          <div class="w-16 h-16 airline-gradient-accent rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6z"/>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-gray-800 mb-2">Crear Cuenta</h3>
          <p class="text-gray-600 mb-4">Regístrate para acceder a reservas, historial y beneficios exclusivos.</p>
          <div class="bg-green-50 text-green-800 px-3 py-1 rounded-full text-sm font-medium">¡Gratis!</div>
        </div>
      </div>
    </div>

    <!-- Información de usuario y póliza -->
    <div v-if="user" class="airline-card airline-card-premium mb-8">
      <div class="flex items-center gap-4 mb-6">
        <div class="w-16 h-16 airline-gradient-primary rounded-full flex items-center justify-center">
          <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-6-3a2 2 0 11-4 0 2 2 0 014 0zm-2 4a5 5 0 00-4.546 2.916A5.986 5.986 0 0010 16a5.986 5.986 0 004.546-2.084A5 5 0 0010 11z" clip-rule="evenodd"/>
          </svg>
        </div>
        <div>
          <h2 class="airline-subtitle">{{ user.name }}</h2>
          <div class="flex flex-wrap gap-4 text-sm text-gray-600">
            <span class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
                <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
              </svg>
              {{ user.email }}
            </span>
            <span class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd"/>
              </svg>
              {{ user.role || 'Sin rol asignado' }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- Detalles de la póliza -->
      <div v-if="user.role === 'patient'">
        <div class="flex items-center gap-2 mb-4">
          <svg class="w-6 h-6 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd"/>
          </svg>
          <h3 class="airline-subtitle text-lg">Tu Póliza AeroLinea</h3>
        </div>
        
        <div v-if="isLoading" class="flex items-center justify-center py-8">
          <div class="flex items-center space-x-3 text-gray-500">
            <div class="w-6 h-6 rounded-full border-3 border-t-transparent border-blue-500 animate-spin"></div>
            <span class="text-lg">Cargando detalles de tu póliza...</span>
          </div>
        </div>
        
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-xl p-4">
          <div class="flex items-center gap-2 text-red-700">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
            {{ error }}
          </div>
        </div>
        
        <div v-else-if="policyDetails" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- Cobertura -->
          <div class="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-2xl p-6 text-center">
            <div class="w-12 h-12 bg-white bg-opacity-20 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div class="text-3xl font-bold mb-2">{{ policyDetails.percentage }}%</div>
            <div class="text-blue-100 mb-3">Cobertura</div>
            <div 
              class="px-3 py-1 text-xs font-semibold rounded-full inline-block"
              :class="{
                'bg-white bg-opacity-20': policyDetails.percentage === 70,
                'bg-yellow-400 text-yellow-900': policyDetails.percentage === 90,
                'bg-purple-400 text-purple-900': policyDetails.percentage !== 70 && policyDetails.percentage !== 90
              }"
            >
              {{ policyDetails.percentage === 90 ? '✈️ Premium' : policyDetails.percentage === 70 ? '🛩️ Básica' : '🚁 Personalizada' }}
            </div>
          </div>

          <!-- Costo -->
          <div class="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-2xl p-6 text-center">
            <div class="w-12 h-12 bg-white bg-opacity-20 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                <path d="M8.433 7.418c.155-.103.346-.196.567-.267v1.698a2.305 2.305 0 01-.567-.267C8.07 8.34 8 8.114 8 8c0-.114.07-.34.433-.582zM11 12.849v-1.698c.22.071.412.164.567.267.364.243.433.468.433.582 0 .114-.07.34-.433.582a2.305 2.305 0 01-.567.267z"/>
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-13a1 1 0 10-2 0v.092a4.535 4.535 0 00-1.676.662C6.602 6.234 6 7.009 6 8c0 .99.602 1.765 1.324 2.246.48.32 1.054.545 1.676.662v1.941c-.391-.127-.68-.317-.843-.504a1 1 0 10-1.51 1.31c.562.649 1.413 1.076 2.353 1.253V15a1 1 0 102 0v-.092a4.535 4.535 0 001.676-.662C13.398 13.766 14 12.991 14 12c0-.99-.602-1.765-1.324-2.246A4.535 4.535 0 0011 9.092V7.151c.391.127.68.317.843.504a1 1 0 101.511-1.31c-.563-.649-1.413-1.076-2.354-1.253V5z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div class="text-3xl font-bold mb-2">{{ formatPrice(policyDetails.cost) }}</div>
            <div class="text-green-100">Costo Mensual</div>
          </div>

          <!-- Vigencia -->
          <div class="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-2xl p-6 text-center">
            <div class="w-12 h-12 bg-white bg-opacity-20 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div class="text-lg font-bold mb-1">Vigente hasta</div>
            <div class="text-purple-100 text-sm mb-2">{{ formatDate(policyDetails.expDate) }}</div>
            <div class="text-xs text-purple-200">Desde: {{ formatDate(policyDetails.creationDate) }}</div>
          </div>
          
          <!-- Explicación -->
          <div class="lg:col-span-3 bg-gradient-to-r from-orange-400 to-pink-400 text-white rounded-2xl p-6">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 bg-white bg-opacity-20 rounded-full flex items-center justify-center flex-shrink-0">
                <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
                </svg>
              </div>
              <div>
                <h4 class="font-bold text-lg mb-2">🛡️ Tu Protección AeroLinea</h4>
                <p class="text-orange-100 leading-relaxed">
                  Tu póliza cubre el <strong>{{ policyDetails.percentage }}%</strong> de los gastos en servicios aeronáuticos y emergencias médicas durante vuelos 
                  aprobados por AeroLinea en nuestra red global. ¡Vuela tranquilo sabiendo que estás protegido!
                </p>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="text-center py-8">
          <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
          <p class="text-gray-500 text-lg">No hay información disponible sobre tu póliza.</p>
        </div>
      </div>
    </div>
    
    <!-- Catálogos disponibles para todos los usuarios -->
    <div class="airline-card mb-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 airline-gradient-sunset rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path d="M7 3a1 1 0 000 2h6a1 1 0 100-2H7zM4 7a1 1 0 011-1h10a1 1 0 110 2H5a1 1 0 01-1-1zM2 11a2 2 0 012-2h12a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z"/>
          </svg>
        </div>
        <h2 class="airline-subtitle">Catálogos AeroLinea</h2>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div 
          @click="router.push('/catalog/insurance-services')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group"
        >
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center group-hover:airline-gradient-primary group-hover:text-white transition-all">
              <svg class="w-6 h-6 text-blue-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-bold text-lg mb-2 group-hover:text-blue-700">🛩️ Servicios Cubiertos</h3>
              <p class="text-gray-600 leading-relaxed">Consulta todos los servicios aeronáuticos cubiertos por tu seguro y sus precios especiales</p>
            </div>
          </div>
        </div>
        
        <div 
          @click="router.push('/catalog/hospitals')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group"
        >
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-red-100 rounded-xl flex items-center justify-center group-hover:airline-gradient-primary group-hover:text-white transition-all">
              <svg class="w-6 h-6 text-red-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm0 4a1 1 0 011-1h12a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1V8zm8 2a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-bold text-lg mb-2 group-hover:text-blue-700">🏥 Centros Médicos Aeroportuarios</h3>
              <p class="text-gray-600 leading-relaxed">Red global de centros médicos aeroportuarios con convenio AeroLinea para emergencias y atención especializada</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Panel de empleado -->
    <div v-if="isEmployee" class="airline-card mb-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 bg-teal-500 rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3z"/>
          </svg>
        </div>
        <h2 class="airline-subtitle">Panel de Empleado</h2>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div 
          @click="router.push('/employee/register-client')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group"
        >
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-teal-100 rounded-xl flex items-center justify-center group-hover:bg-teal-500 group-hover:text-white transition-all">
              <svg class="w-6 h-6 text-teal-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd"/>
              </svg>
            </div>
            <div>
              <h3 class="font-bold text-lg mb-2 group-hover:text-teal-700">👥 Registrar Cliente</h3>
              <p class="text-gray-600 leading-relaxed">Registrar un nuevo cliente AeroLinea con su póliza de seguro aeronáutico</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Panel de administrador -->
    <div v-if="isAdmin" class="airline-card airline-card-premium mb-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 airline-gradient-primary rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/>
          </svg>
        </div>
        <h2 class="airline-subtitle">Centro de Control AeroLinea</h2>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div 
          @click="router.push('/admin/users')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group"
        >
          <div class="flex flex-col items-center text-center">
            <div class="w-14 h-14 bg-blue-100 rounded-2xl flex items-center justify-center mb-3 group-hover:airline-gradient-primary transition-all">
              <svg class="w-7 h-7 text-blue-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
              </svg>
            </div>
            <h3 class="font-bold mb-2 group-hover:text-blue-700">👥 Usuarios</h3>
            <p class="text-sm text-gray-600">Gestión de pilotos, tripulación y personal</p>
          </div>
        </div>
        
        <div 
          @click="router.push('/admin/insurance-services')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group"
        >
          <div class="flex flex-col items-center text-center">
            <div class="w-14 h-14 bg-green-100 rounded-2xl flex items-center justify-center mb-3 group-hover:airline-gradient-primary transition-all">
              <svg class="w-7 h-7 text-green-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd"/>
              </svg>
            </div>
            <h3 class="font-bold mb-2 group-hover:text-blue-700">🛩️ Servicios</h3>
            <p class="text-sm text-gray-600">Catálogo de coberturas aeronáuticas</p>
          </div>
        </div>
        
        <div 
          @click="router.push('/admin/hospital-services')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group"
        >
          <div class="flex flex-col items-center text-center">
            <div class="w-14 h-14 bg-purple-100 rounded-2xl flex items-center justify-center mb-3 group-hover:airline-gradient-primary transition-all">
              <svg class="w-7 h-7 text-purple-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm0 4a1 1 0 011-1h12a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1V8zm8 2a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd"/>
              </svg>
            </div>
            <h3 class="font-bold mb-2 group-hover:text-blue-700">🏥 Hospitales</h3>
            <p class="text-sm text-gray-600">Red médica aeroportuaria</p>
          </div>
        </div>
        
        <div 
          @click="router.push('/admin/policies')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group"
        >
          <div class="flex flex-col items-center text-center">
            <div class="w-14 h-14 bg-amber-100 rounded-2xl flex items-center justify-center mb-3 group-hover:airline-gradient-primary transition-all">
              <svg class="w-7 h-7 text-amber-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
              </svg>
            </div>
            <h3 class="font-bold mb-2 group-hover:text-blue-700">📜 Pólizas</h3>
            <p class="text-sm text-gray-600">Planes de cobertura aeronáutica</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
