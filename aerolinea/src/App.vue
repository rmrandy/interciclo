<script setup lang="ts">
import { ref, provide, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import eventBus from './eventBus';
import PortSelector from './components/PortSelector.vue';
import { loadPortConfiguration, getInsuranceApiUrl } from './utils/api';
import axios from 'axios'

const router = useRouter();
const isLoggedIn = ref(false);
const userProfile = ref<any>(null);
const showPortSelector = ref(false);

// Branding dinámico
const brandTitle = ref('AeroLinea')
const brandSubtitle = ref('Tu compañía de seguros aeronáuticos')
const footerText = ref('© 2025 AeroLinea. Todos los derechos reservados.')
const brandLogoUrl = ref('')

const loadSiteSettings = async () => {
  try {
    const url = getInsuranceApiUrl('/site-settings')
    const { data } = await axios.get(url)
    if (data) {
      brandTitle.value = data['brand.title'] || brandTitle.value
      brandSubtitle.value = data['brand.subtitle'] || brandSubtitle.value
      footerText.value = data['footer.text'] || footerText.value
      brandLogoUrl.value = data['brand.logoUrl'] || ''
    }
  } catch (e) {
    console.warn('No se pudieron cargar los site-settings, usando defaults', e)
  }
}

// Verificar si se debe mostrar el selector de puertos
const checkShowPortSelector = () => {
  const skipPortSelector = localStorage.getItem('skipPortSelector') === 'true';
  showPortSelector.value = !skipPortSelector;
};

const checkAuth = () => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  isLoggedIn.value = profile !== null && profile !== "null";
  userProfile.value = profile;
  console.log("Usuario:", profile);
  console.log("Es admin:", isAdmin.value);
};

// Computed para verificar roles (case-insensitive)
const isAdmin = computed(() => {
  const role = String(userProfile.value?.role || '').toUpperCase()
  return role === 'ADMIN'
});

const isEmployee = computed(() => {
  const role = String(userProfile.value?.role || '').toUpperCase()
  return role === 'EMPLOYEE' || role === 'ADMIN'
});

// Inicialización
onMounted(() => {
  loadPortConfiguration();
  checkAuth();
  checkShowPortSelector();
  loadSiteSettings();
  
  eventBus.on('login', () => {
    checkAuth();
  });
  
  eventBus.on('logout', () => {
    checkAuth();
  });
});

// Mostrar el selector de puertos de forma manual
const openPortSelector = () => {
  showPortSelector.value = true;
};

function logout() {
  localStorage.removeItem("user");
  isLoggedIn.value = false;
  userProfile.value = null;
  eventBus.emit('logout');
  router.push("/login");
}

// Funciones de navegación
function navigateToUsers() {
  router.push("/admin/users");
}

function navigateToServices() {
  router.push("/admin/insurance-services");
}

function navigateToHospitals() {
  router.push("/admin/hospital-services");
}

function navigateToInsuranceServicesCatalog() {
  router.push("/catalog/insurance-services");
}

function navigateToHospitalsCatalog() {
  router.push("/catalog/hospitals");
}

function navigateToPolicies() {
  router.push("/admin/policies");
}

function navigateToRegisterClient() {
  router.push("/employee/register-client");
}

function navigateToFlightOperations() {
  router.push("/flight-operations");
}

function navigateToFlightSchedule() {
  router.push("/admin/flight-schedule");
}

function navigateToAircraftMaintenance() {
  router.push("/admin/aircraft-maintenance");
}

function navigateToAircrafts() {
  router.push("/admin/aircrafts");
}

function navigateToAircraftSeatConfig() {
  router.push("/admin/aircraft-seat-config");
}

function navigateToConfiguration() {
  router.push("/admin/configuration");
}

function navigateToUserServices() {
  router.push("/user-services");
}

function navigateToCities() {
  router.push("/admin/flight-operations");
}

function navigateToFlights() {
  router.push("/flights");
}

// Nuevas funciones de navegación del panel Admin
function navigateToFlightManagement() {
  router.push("/admin/flight-management");
}

function navigateToHospitalConfiguration() {
  router.push("/admin/hospital-configuration");
}

function navigateToHospitalServicesImport() {
  router.push("/admin/hospital-services-import");
}

function navigateToClientManagement() {
  router.push("/admin/client-management");
}

function navigateToInformativePages() {
  router.push("/admin/informative-pages");
}
</script>

<template>
  <header class="airline-nav sticky top-0 z-50">
    <div class="container mx-auto px-6 py-4 flex justify-between items-center">
      <!-- Logo -->
      <div class="flex items-center space-x-3">
        <template v-if="brandLogoUrl">
          <img :src="brandLogoUrl" alt="logo" class="w-10 h-10 rounded-full object-cover" />
        </template>
        <template v-else>
          <div class="w-10 h-10 bg-gradient-to-r from-blue-600 to-blue-800 rounded-full flex items-center justify-center">
            <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"/>
            </svg>
          </div>
        </template>
        <div class="airline-title text-2xl">{{ brandTitle }}</div>
      </div>

      <!-- Navegación -->
      <nav class="flex gap-2">
        <!-- Usuario no logueado -->
        <div v-if="!isLoggedIn" class="flex gap-2">
          <router-link
            to="/flights"
            class="airline-nav-item"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
            Vuelos
          </router-link>
          <router-link
            to="/login"
            class="btn-airline-primary airline-nav-item"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M3 3a1 1 0 011 1v12a1 1 0 11-2 0V4a1 1 0 011-1zm7.707 3.293a1 1 0 010 1.414L9.414 9H17a1 1 0 110 2H9.414l1.293 1.293a1 1 0 01-1.414 1.414l-3-3a1 1 0 010-1.414l3-3a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            Iniciar Sesión
          </router-link>
          <router-link
            to="/register"
            class="btn-airline-outline airline-nav-item"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6z"/>
            </svg>
            Registrarse
          </router-link>
        </div>

        <!-- Usuario logueado -->
        <div v-if="isLoggedIn" class="flex items-center gap-2">
          <router-link
            to="/home"
            class="airline-nav-item"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z"/>
            </svg>
            Inicio
          </router-link>
          
          <!-- Menú de catálogos -->
          <div class="relative group">
            <button class="airline-nav-item flex items-center gap-1">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path d="M7 3a1 1 0 000 2h6a1 1 0 100-2H7zM4 7a1 1 0 011-1h10a1 1 0 110 2H5a1 1 0 01-1-1zM2 11a2 2 0 012-2h12a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z"/>
              </svg>
              Catálogos 
              <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd"/>
              </svg>
            </button>
            <div class="absolute hidden group-hover:block bg-white mt-2 py-2 rounded-xl airline-shadow-medium z-10 w-64 right-0 border border-gray-100">
              <button 
                @click="navigateToInsuranceServicesCatalog" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd"/>
                </svg>
                Servicios Cubiertos
              </button>
              <button 
                @click="navigateToHospitalsCatalog" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-red-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm0 4a1 1 0 011-1h12a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1V8zm8 2a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd"/>
                </svg>
                Hospitales Aprobados
              </button>
            </div>
          </div>

          <!-- Operaciones de Vuelo -->
          <button 
            @click="navigateToFlightOperations" 
            class="airline-nav-item"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
            Operaciones de Vuelo
          </button>

          <!-- Ver Vuelos Disponibles -->
          <button 
            @click="navigateToFlights" 
            class="airline-nav-item"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
            ✈️ Ver Vuelos
          </button>
          
          <!-- Carrito -->
          <router-link
            to="/cart"
            class="airline-nav-item"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M3 1a1 1 0 000 2h1.22l.305 1.222a.997.997 0 00.01.042l1.358 5.43-.893.892C3.74 11.846 4.632 14 6.414 14H15a1 1 0 000-2H6.414l-.893-.892a.997.997 0 00-.01-.042l-1.358-5.43L3 1zM16 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM6.5 18a1.5 1.5 0 100-3 1.5 1.5 0 000 3z"/>
            </svg>
            🛒 Carrito
          </router-link>
        
          <!-- Menú de empleado -->
          <div v-if="isEmployee" class="relative group">
            <button class="airline-nav-item flex items-center gap-1">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
              </svg>
              Clientes
              <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd"/>
              </svg>
            </button>
            <div class="absolute hidden group-hover:block bg-white mt-2 py-2 rounded-xl airline-shadow-medium z-10 w-64 right-0 border border-gray-100">
              <button 
                @click="navigateToRegisterClient" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd"/>
                </svg>
                Registrar Cliente
              </button>
            </div>
          </div>

          <!-- Menú de administrador -->
          <div v-if="isAdmin" class="relative group">
            <button class="airline-nav-item flex items-center gap-1">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/>
              </svg>
              Administración
              <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd"/>
              </svg>
            </button>
            <div class="absolute hidden group-hover:block bg-white mt-2 py-2 rounded-xl airline-shadow-medium z-10 w-72 right-0 border border-gray-100 max-h-96 overflow-y-auto">
              <button 
                @click="navigateToUsers" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
                </svg>
                Gestión de Usuarios
              </button>
              <button 
                @click="navigateToServices" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd"/>
                </svg>
                Catálogo de Servicios
              </button>
              <button 
                @click="navigateToHospitals" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-red-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zm0 4a1 1 0 011-1h12a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1V8zm8 2a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd"/>
                </svg>
                Hospitales y Servicios
              </button>
              <button 
                @click="() => router.push('/admin/purchases')" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-indigo-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M3 3a1 1 0 000 2h1.22l.305 1.222a1 1 0 00.97.778H16a1 1 0 010 2H7.08l-.3 1.2A2 2 0 008.72 12H15a1 1 0 010 2H8.72a4 4 0 01-3.88-2.905L3.28 4.222A1 1 0 002.31 3H3zM6 16a2 2 0 104 0 2 2 0 00-4 0zm7 0a2 2 0 104 0 2 2 0 00-4 0z"/>
                </svg>
                Compras / Boletos
              </button>
              <button 
                @click="navigateToFlightManagement" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-indigo-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M3 3h14v2H3zM3 7h14v2H3zM3 11h14v2H3zM3 15h14v2H3z"/>
                </svg>
                Gestión de Vuelos
              </button>
              <button 
                @click="navigateToAircrafts" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-sky-600" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M2.5 19l8-6-8-6v4l6 2-6 2v4zm10 .5l3.5-2.5 5.5.5 2-1.5-5.5-4.5L22 6l-2-1.5-5.5.5L13 2.5h-2l.5 3.5L6 6.5 4 8l7.5 6.5-.5 3.5h2z"/>
                </svg>
                Aeronaves
              </button>
              <button 
                @click="navigateToAircraftSeatConfig" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-cyan-600" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M6 3a2 2 0 00-2 2v6a3 3 0 003 3h5a2 2 0 002-2V8a2 2 0 00-2-2H9V5a2 2 0 00-2-2H6zM4 16a1 1 0 000 2h11a1 1 0 100-2H4z"/>
                </svg>
                Configuración de Asientos
              </button>
              <button 
                @click="navigateToFlightSchedule" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-purple-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M6 2a1 1 0 000 2h1v2H6a1 1 0 100 2h1v2H6a1 1 0 100 2h1v2H6a1 1 0 100 2h8a1 1 0 100-2h-1v-2h1a1 1 0 100-2h-1V8h1a1 1 0 100-2h-1V4h1a1 1 0 100-2H6z"/>
                </svg>
                Programación de Vuelos
              </button>
              <button 
                @click="navigateToCities" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-teal-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M10 2a8 8 0 100 16 8 8 0 000-16zM8 7h4v6H8z"/>
                </svg>
                Ciudades
              </button>
              <button 
                @click="navigateToClientManagement" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-amber-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM2 16a6 6 0 1112 0H2z"/>
                </svg>
                Gestión de Clientes
              </button>
              <button 
                @click="navigateToHospitalConfiguration" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-rose-500" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M4 3h12v14H4zM8 5v10M12 5v10"/>
                </svg>
                Configuración Hospitalaria
              </button>
              <button 
                @click="navigateToHospitalServicesImport" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-lime-600" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M3 3h14v2H3zM3 7h10v2H3zM3 11h6v2H3zM3 15h2v2H3z"/>
                </svg>
                Importar Servicios Hospitalarios
              </button>
              <button 
                @click="navigateToConfiguration" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-gray-600" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M11.983 1.077a1 1 0 00-1.966 0l-.143.859a7.965 7.965 0 00-1.709.99l-.79-.456a1 1 0 00-1.366.366l-.983 1.702a1 1 0 00.366 1.366l.79.456c-.086.559-.086 1.139 0 1.698l-.79.456a1 1 0 00-.366 1.366l.983 1.702a1 1 0 001.366.366l.79-.456c.53.417 1.11.77 1.709.99l.143.859a1 1 0 001.966 0l.143-.859a7.965 7.965 0 001.709-.99l.79.456a1 1 0 001.366-.366l.983-1.702a1 1 0 00-.366-1.366l-.79-.456c.086-.559.086-1.139 0-1.698l.79-.456a1 1 0 00.366-1.366l-.983-1.702a1 1 0 00-1.366-.366l-.79.456a7.965 7.965 0 00-1.709-.99l-.143-.859z"/>
                </svg>
                Configuración del Sistema
              </button>
              <button 
                @click="navigateToInformativePages" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                Páginas Informativas
              </button>
              <button
                @click="router.push('/admin/site-settings')"
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-blue-700" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M11.983 1.077a1 1 0 00-1.966 0l-.143.859a7.965 7.965 0 00-1.709.99l-.79-.456a1 1 0 00-1.366.366l-.983 1.702a1 1 0 00.366 1.366l.79.456c-.086.559-.086 1.139 0 1.698l-.79.456a1 1 0 00-.366 1.366l.983 1.702a1 1 0 001.366.366l.79-.456c.53.417 1.11.77 1.709.99l.143.859a1 1 0 001.966 0l.143-.859a7.965 7.965 0 001.709-.99l.79.456a1 1 0 001.366-.366l.983-1.702a1 1 0 00-.366-1.366l-.79-.456c.086-.559.086-1.139 0-1.698l.79-.456a1 1 0 00.366-1.366l-.983-1.702a1 1 0 00-1.366-.366l-.79.456a7.965 7.965 0 00-1.709-.99l-.143-.859z"/>
                </svg>
                Ajustes del Sitio
              </button>
              <button 
                @click="() => router.push('/admin/analytics')" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-fuchsia-600" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M3 3h2v14H3zM7 9h2v8H7zM11 6h2v11h-2zM15 12h2v5h-2z"/>
                </svg>
                Analítica
              </button>
            </div>
          </div>
        
          <!-- Mis Servicios -->
          <button 
            @click="navigateToUserServices" 
            class="airline-nav-item"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-6-3a2 2 0 11-4 0 2 2 0 014 0zm-2 4a5 5 0 00-4.546 2.916A5.986 5.986 0 0010 16a5.986 5.986 0 004.546-2.084A5 5 0 0010 11z" clip-rule="evenodd"/>
            </svg>
            Mis Servicios
          </button>

          <!-- Mis Reservas -->
          <router-link to="/my-bookings" class="airline-nav-item">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M6 2a2 2 0 00-2 2v12a2 2 0 002 2h8a2 2 0 002-2V4a2 2 0 00-2-2H6zm2 3h4a1 1 0 010 2H8a1 1 0 110-2zm0 4h6a1 1 0 010 2H8a1 1 0 110-2zm0 4h6a1 1 0 010 2H8a1 1 0 110-2z"/>
            </svg>
            Mis Reservas
          </router-link>
          
          <!-- Logout -->
          <button
            @click="logout"
            class="btn-airline-secondary"
          >
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/>
            </svg>
            Salir
          </button>
        </div>
      </nav>
    </div>
  </header>

  <!-- Contenido principal -->
  <main class="min-h-[calc(100vh-140px)] p-6 bg-gradient-to-br from-slate-50 to-blue-50">
    <div class="animate-fade-in">
      <RouterView />
    </div>
  </main>

  <!-- Footer -->
  <footer class="airline-gradient-primary text-white py-6 px-8 mt-auto">
    <div class="container mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-center">
        <div class="flex items-center space-x-3 mb-4 md:mb-0">
          <div class="w-8 h-8 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"/>
            </svg>
          </div>
          <div>
            <p class="font-bold text-lg">{{ brandTitle }}</p>
            <p class="text-blue-200 text-sm">{{ brandSubtitle }}</p>
          </div>
        </div>
        <div class="flex flex-col md:flex-row items-center space-y-2 md:space-y-0 md:space-x-6">
          <p class="text-blue-200 text-sm">{{ footerText }}</p>
          <div class="flex space-x-4">
            <a href="#" class="text-blue-200 hover:text-white transition-colors">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M12.586 4.586a2 2 0 112.828 2.828l-3 3a2 2 0 01-2.828 0 1 1 0 00-1.414 1.414 4 4 0 005.656 0l3-3a4 4 0 00-5.656-5.656l-1.5 1.5a1 1 0 101.414 1.414l1.5-1.5zm-5 5a2 2 0 012.828 0 1 1 0 101.414-1.414 4 4 0 00-5.656 0l-3 3a4 4 0 105.656 5.656l1.5-1.5a1 1 0 10-1.414-1.414l-1.5 1.5a2 2 0 11-2.828-2.828l3-3z" clip-rule="evenodd"/>
              </svg>
            </a>
            <a href="#" class="text-blue-200 hover:text-white transition-colors">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
              </svg>
            </a>
          </div>
            </div>
      </div>
    </div>
  </footer>
  
  <!-- Selector de puertos -->
  <PortSelector v-if="showPortSelector" />
</template>

<style>
.group:hover .group-hover\:block {
  display: block;
}
</style>
