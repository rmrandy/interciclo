<script setup lang="ts">
import { ref, provide, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import eventBus from './eventBus';
import PortSelector from './components/PortSelector.vue';
import { loadPortConfiguration } from './utils/api';

const router = useRouter();
const isLoggedIn = ref(false);
const userProfile = ref<any>(null);
const showPortSelector = ref(false);

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

// Computed para verificar roles
const isAdmin = computed(() => {
  return userProfile.value && userProfile.value.role === "ADMIN";
});

const isEmployee = computed(() => {
  return userProfile.value && (userProfile.value.role === "EMPLOYEE" || userProfile.value.role === "ADMIN");
});

// Inicialización
onMounted(() => {
  loadPortConfiguration();
  checkAuth();
  checkShowPortSelector();
  
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
</script>

<template>
  <header class="airline-nav sticky top-0 z-50">
    <div class="container mx-auto px-6 py-4 flex justify-between items-center">
      <!-- Logo -->
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 bg-gradient-to-r from-blue-600 to-blue-800 rounded-full flex items-center justify-center">
          <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"/>
          </svg>
        </div>
        <div class="airline-title text-2xl">AeroLinea</div>
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
                @click="navigateToPolicies" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-purple-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
                </svg>
                Gestión de Pólizas
              </button>
              <button 
                @click="navigateToCities" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-teal-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 20s6-5.686 6-10A6 6 0 104 10c0 4.314 6 10 6 10zM10 11a3 3 0 110-6 3 3 0 010 6z" clip-rule="evenodd"/>
                </svg>
                Ciudades (Operaciones de Vuelo)
              </button>
              <button 
                @click="navigateToFlightSchedule" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-orange-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
                </svg>
                Programación de Vuelos
              </button>
              <button 
                @click="navigateToAircraftMaintenance" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-cyan-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                Mantenimiento de Aeronaves
              </button>
              <button 
                @click="navigateToConfiguration" 
                class="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <svg class="w-4 h-4 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/>
                </svg>
                Configuración del Sistema
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
            <p class="font-bold text-lg">AeroLinea</p>
            <p class="text-blue-200 text-sm">Tu compañía de seguros aeronáuticos</p>
          </div>
        </div>
        <div class="flex flex-col md:flex-row items-center space-y-2 md:space-y-0 md:space-x-6">
          <p class="text-blue-200 text-sm">&copy; 2025 AeroLinea. Todos los derechos reservados.</p>
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
