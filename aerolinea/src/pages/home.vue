<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { AirlineApiClient } from "../utils/airlineApi";
import { getAirlineApiUrl, getInsuranceApiUrl } from "../utils/api";
import { informativePagesApi } from "../utils/informativePagesApi";

const router = useRouter();
const user = ref<any>(null);
const isAdmin = ref(false);
const isEmployee = ref(false);
const isLoading = ref(true);
const policyDetails = ref<any>(null);
// Estado de vuelos próximos
const upcomingTickets = ref<any[]>([])
const loadingUpcoming = ref(false)
// Reservas del usuario (mis tickets)
const myTickets = ref<any[]>([])
const loadingMyTickets = ref(false)
const reservationSearch = ref('')
const showTicketModal = ref(false)
const ticketDetail = ref<any | null>(null)
// Alertas operativas simuladas (podemos conectarlas a backend luego)
const operationalAlerts = ref<Array<{id:number; title:string; description:string; severity:'info'|'warning'|'critical'}>>([
  { id: 1, title: 'Clima', description: 'Posibles demoras por tormenta en MIA', severity: 'warning' },
  { id: 2, title: 'Seguridad', description: 'Refuerzo de controles en todos los aeropuertos', severity: 'info' }
])
// Promos de ejemplo
const promos = ref([
  { id: 1, title: '2x1 fin de semana', desc: 'Compra hoy y recibe 2x1 en rutas seleccionadas', color: 'from-pink-500 to-rose-500' },
  { id: 2, title: 'Tarifa Flash', desc: 'Descuentos hasta 35% por 24 horas', color: 'from-blue-500 to-cyan-500' },
  { id: 3, title: 'Business Upgrade', desc: 'Upgrade a Business desde Q399', color: 'from-amber-500 to-orange-500' }
])
const error = ref<string | null>(null);
// Contenido dinámico del Home (misma dinámica que páginas informativas)
const pageTitle = ref<string>('¡Bienvenido a AeroLinea!')
const pageDescription = ref<string>('Tu compañía de seguros aeronáuticos de confianza')

const loadHomeContent = async (): Promise<void> => {
  try {
    const page = await informativePagesApi.getPageBySlug('home')
    if (page?.title) pageTitle.value = page.title
    if (page?.description) pageDescription.value = page.description
  } catch (e) {
    console.warn('No se pudo cargar contenido de Home (slug=home)', e)
  } finally {
    // Actualizar meta tags independientemente del resultado
    document.title = `${pageTitle.value} - AeroLinea`
    const metaDescription = document.querySelector('meta[name="description"]')
    if (metaDescription) metaDescription.setAttribute('content', pageDescription.value)
  }
}

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
  // Cargar contenido del Home desde páginas informativas
  loadHomeContent()
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
  // cargar ciudades para caja de compras
  loadCitiesForBooking()
  // cargar estado de vuelos si aplica
  loadUpcomingTickets()
  loadMyTickets()
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

// ============ Caja de compras (Aerolíneas) ============
const booking = ref({
  flightType: 'roundtrip', // roundtrip | oneway
  origin: '',
  destination: '',
  departureDate: '',
  returnDate: '',
  seatCategory: 'ECONOMY', // ECONOMY | BUSINESS
  passengers: 1
})
const cities = ref<{ idCity: number; name: string; country: string }[]>([])
const loadingCities = ref(false)
const airlineApi = new AirlineApiClient()

const loadCitiesForBooking = async () => {
  try {
    loadingCities.value = true
    const res = await airlineApi.getCities()
    if (Array.isArray(res)) {
      cities.value = res
    } else if (res && Array.isArray(res.cities)) {
      cities.value = res.cities
    }
  } catch (e) {
    console.warn('No se pudieron cargar ciudades, usando fallback', e)
    cities.value = [
      { idCity: 1, name: 'Ciudad de Guatemala', country: 'Guatemala' },
      { idCity: 2, name: 'Miami', country: 'USA' },
      { idCity: 3, name: 'Orlando', country: 'USA' }
    ]
  } finally {
    loadingCities.value = false
  }
}

const submitBooking = () => {
  const payload = { ...booking.value }
  localStorage.setItem('preSearch', JSON.stringify(payload))
  router.push('/flights')
}

// Cargar tickets próximos del usuario
const loadUpcomingTickets = async () => {
  if (!user.value || !user.value.idUser) return
  loadingUpcoming.value = true
  try {
    const url = getAirlineApiUrl(`/tickets?userId=${user.value.idUser}`)
    const res = await fetch(url)
    const data = await res.json()
    const list = Array.isArray(data?.tickets) ? data.tickets : (Array.isArray(data) ? data : [])
    // Tomar próximos por fecha (si está disponible)
    const now = new Date()
    const norm = (t: any): { when: Date; t: any } => {
      const d = t?.flight?.departureDate || t?.departureDate
      const tm = t?.flight?.departureTime || t?.departureTime
      const when = d ? new Date(`${d} ${tm || '00:00'}`) : now
      return { when, t }
      
    }
    upcomingTickets.value = list
      .map(norm)
      .filter((x: { when: Date; t: any }) => x.when >= now)
      .sort((a: { when: Date }, b: { when: Date }) => a.when.getTime() - b.when.getTime())
      .slice(0,3)
      .map((x: { when: Date; t: any }) => x.t)
  } catch (e) {
    console.warn('No se pudo cargar estado de vuelos del usuario', e)
    upcomingTickets.value = []
  } finally {
    loadingUpcoming.value = false
  }
}

// Cargar todas las reservas del usuario
const loadMyTickets = async () => {
  if (!user.value || !user.value.idUser) return
  loadingMyTickets.value = true
  try {
    const url = getAirlineApiUrl(`/tickets?userId=${user.value.idUser}`)
    const res = await fetch(url)
    const data = await res.json()
    myTickets.value = Array.isArray(data?.tickets) ? data.tickets : (Array.isArray(data) ? data : [])
  } catch (e) {
    console.warn('No se pudo cargar tus reservas', e)
    myTickets.value = []
  } finally {
    loadingMyTickets.value = false
  }
}

// Buscar por código de reservación (si está disponible)
const searchReservation = async () => {
  const code = reservationSearch.value?.trim()
  if (!code) return
  try {
    const api = new AirlineApiClient()
    const res = await api.getTicketByCode(code)
    if (res?.success && res.ticket) {
      ticketDetail.value = res.ticket
      showTicketModal.value = true
    } else {
      alert(res?.error || 'Código no encontrado')
    }
  } catch (e) {
    alert('No se pudo buscar el código')
  }
}

// Detalle se navega a su propia ruta ahora

const closeTicketModal = () => {
  showTicketModal.value = false
  ticketDetail.value = null
}

// Descargar PDF con manejo de error 501
const downloadTicketPdf = async (id: number) => {
  try {
    const api = new AirlineApiClient()
    const blob = await api.downloadTicketPdf(id)
    if (!blob || (blob.type && blob.type !== 'application/pdf')) {
      alert('El PDF no está disponible por el momento')
      return
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `ticket-${id}.pdf`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch (e) {
    alert('El PDF no está disponible por el momento')
  }
}
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Hero Section -->
    <div class="airline-gradient-sky text-white rounded-3xl p-6 mb-8 relative overflow-hidden">
      <div class="absolute top-0 right-0 w-64 h-64 opacity-10">
        <svg fill="currentColor" viewBox="0 0 24 24" class="w-full h-full">
          <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
        </svg>
      </div>
      <div class="relative z-10">
        <h1 class="text-3xl md:text-4xl font-bold mb-3 animate-fade-in-up">{{ pageTitle }}</h1>
        <p class="text-lg md:text-xl mb-5 text-blue-100 animate-fade-in-up" style="animation-delay: 0.2s;">{{ pageDescription }}</p>
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
        <div v-if="!user" class="mt-6 flex flex-wrap gap-3 animate-fade-in-up" style="animation-delay: 0.6s;">
          <button 
            @click="navigateToFlights"
            class="bg-white text-blue-800 px-4 py-2 rounded-full font-semibold hover:bg-blue-50 transition-colors flex items-center gap-2"
          >
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
            Ver Vuelos Disponibles
          </button>
          <button 
            @click="tryBookFlight"
            class="bg-blue-900 text-white px-4 py-2 rounded-full font-semibold hover:bg-blue-800 transition-colors flex items-center gap-2"
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
          <div class="w-12 h-12 airline-gradient-primary rounded-full flex items-center justify-center mx-auto mb-3">
            <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
          </div>
          <h3 class="text-lg font-bold text-gray-800 mb-1">Consultar Vuelos</h3>
          <p class="text-gray-600 mb-3 text-sm">Explora nuestra amplia oferta de vuelos nacionales e internacionales.</p>
          <div class="bg-blue-50 text-blue-800 px-2.5 py-0.5 rounded-full text-xs font-medium">Sin registro requerido</div>
        </div>
      </div>

      <!-- Reservar -->
      <div class="airline-card hover:scale-105 transition-transform cursor-pointer" @click="tryBookFlight">
        <div class="text-center">
          <div class="w-12 h-12 airline-gradient-secondary rounded-full flex items-center justify-center mx-auto mb-3">
            <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <h3 class="text-lg font-bold text-gray-800 mb-1">Reservar Vuelo</h3>
          <p class="text-gray-600 mb-3 text-sm">Asegura tu lugar en el vuelo de tu elección con unos pocos clics.</p>
          <div class="bg-orange-50 text-orange-800 px-2.5 py-0.5 rounded-full text-xs font-medium">Requiere registro</div>
        </div>
      </div>

      <!-- Registrarse -->
      <div class="airline-card hover:scale-105 transition-transform cursor-pointer" @click="router.push('/register')">
        <div class="text-center">
          <div class="w-12 h-12 airline-gradient-accent rounded-full flex items-center justify-center mx-auto mb-3">
            <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6z"/>
            </svg>
          </div>
          <h3 class="text-lg font-bold text-gray-800 mb-1">Crear Cuenta</h3>
          <p class="text-gray-600 mb-3 text-sm">Regístrate para acceder a reservas, historial y beneficios exclusivos.</p>
          <div class="bg-green-50 text-green-800 px-2.5 py-0.5 rounded-full text-xs font-medium">¡Gratis!</div>
        </div>
      </div>
    </div>

    <!-- Enlaces a páginas informativas -->
    <div class="airline-card mb-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 airline-gradient-sunset rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <h2 class="airline-subtitle">Información Útil</h2>
      </div>
      
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <div 
          @click="router.push('/informative/seat-types')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group text-center"
        >
          <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:airline-gradient-primary group-hover:text-white transition-all">
            <svg class="w-6 h-6 text-blue-600 group-hover:text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
          </div>
          <h3 class="font-bold text-sm mb-1 group-hover:text-blue-700">Asientos</h3>
          <p class="text-xs text-gray-600">Tipos y precios</p>
        </div>
        
        <div 
          @click="router.push('/informative/checkin-process')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group text-center"
        >
          <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:airline-gradient-primary group-hover:text-white transition-all">
            <svg class="w-6 h-6 text-green-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="font-bold text-sm mb-1 group-hover:text-blue-700">Check-in</h3>
          <p class="text-xs text-gray-600">Proceso paso a paso</p>
        </div>
        
        <div 
          @click="router.push('/informative/boarding-instructions')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group text-center"
        >
          <div class="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:airline-gradient-primary group-hover:text-white transition-all">
            <svg class="w-6 h-6 text-orange-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="font-bold text-sm mb-1 group-hover:text-blue-700">Abordaje</h3>
          <p class="text-xs text-gray-600">Instrucciones</p>
        </div>
        
        <div 
          @click="router.push('/informative/baggage-info')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group text-center"
        >
          <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:airline-gradient-primary group-hover:text-white transition-all">
            <svg class="w-6 h-6 text-purple-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M8 2a1 1 0 000 2h2a1 1 0 100-2H8zM3 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v6h-4.586l1.293-1.293a1 1 0 00-1.414-1.414l-3 3a1 1 0 000 1.414l3 3a1 1 0 001.414-1.414L10.414 13H15v3a2 2 0 01-2 2H5a2 2 0 01-2-2V5z"/>
            </svg>
          </div>
          <h3 class="font-bold text-sm mb-1 group-hover:text-blue-700">Equipaje</h3>
          <p class="text-xs text-gray-600">Políticas y restricciones</p>
        </div>
        
        <div 
          @click="router.push('/informative/travel-tips')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group text-center"
        >
          <div class="w-12 h-12 bg-pink-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:airline-gradient-primary group-hover:text-white transition-all">
            <svg class="w-6 h-6 text-pink-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/>
            </svg>
          </div>
          <h3 class="font-bold text-sm mb-1 group-hover:text-blue-700">Consejos</h3>
          <p class="text-xs text-gray-600">Para tu viaje</p>
        </div>
        
        <div 
          @click="router.push('/informative/contact')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group text-center"
        >
          <div class="w-12 h-12 bg-teal-100 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:airline-gradient-primary group-hover:text-white transition-all">
            <svg class="w-6 h-6 text-teal-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
            </svg>
          </div>
          <h3 class="font-bold text-sm mb-1 group-hover:text-blue-700">Contacto</h3>
          <p class="text-xs text-gray-600">Ayuda y soporte</p>
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
        
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-3">
          <div class="flex items-center gap-2 text-red-700 text-sm">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
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
    
    <!-- Caja de Compras (Aerolíneas) - MOVIDA ARRIBA -->
    <div class="airline-card mb-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 airline-gradient-secondary rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path d="M7 3a1 1 0 000 2h6a1 1 0 100-2H7zM4 7a1 1 0 011-1h10a1 1 0 110 2H5a1 1 0 01-1-1zM2 11a2 2 0 012-2h12a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z"/>
          </svg>
        </div>
        <h2 class="airline-subtitle">Reserva tu vuelo</h2>
      </div>

      <form class="grid md:grid-cols-6 gap-4" @submit.prevent="submitBooking">
        <!-- Tipo de vuelo -->
        <div class="md:col-span-2 flex items-center gap-4">
          <label class="flex items-center gap-2 cursor-pointer">
            <input type="radio" value="roundtrip" v-model="booking.flightType"> Ida y vuelta
          </label>
          <label class="flex items-center gap-2 cursor-pointer">
            <input type="radio" value="oneway" v-model="booking.flightType"> Solo ida
          </label>
        </div>

        <!-- Origen/Destino -->
        <div class="md:col-span-2">
          <label class="airline-form-label">Origen</label>
          <select v-model="booking.origin" class="airline-input">
            <option value="" disabled>Selecciona origen</option>
            <option v-for="c in cities" :key="c.idCity" :value="c.idCity">{{ c.name }} ({{ c.country }})</option>
          </select>
        </div>
        <div class="md:col-span-2">
          <label class="airline-form-label">Destino</label>
          <select v-model="booking.destination" class="airline-input">
            <option value="" disabled>Selecciona destino</option>
            <option v-for="c in cities" :key="c.idCity" :value="c.idCity">{{ c.name }} ({{ c.country }})</option>
          </select>
        </div>

        <!-- Fechas -->
        <div class="md:col-span-2">
          <label class="airline-form-label">Fecha ida</label>
          <input type="date" v-model="booking.departureDate" class="airline-input"/>
        </div>
        <div class="md:col-span-2" v-if="booking.flightType==='roundtrip'">
          <label class="airline-form-label">Fecha vuelta</label>
          <input type="date" v-model="booking.returnDate" class="airline-input"/>
        </div>

        <!-- Categoría / Pasajeros -->
        <div>
          <label class="airline-form-label">Asiento</label>
          <select v-model="booking.seatCategory" class="airline-input">
            <option value="ECONOMY">Turista (Economy)</option>
            <option value="BUSINESS">Business</option>
          </select>
        </div>
        <div>
          <label class="airline-form-label">Pasajeros</label>
          <input type="number" min="1" v-model.number="booking.passengers" class="airline-input"/>
        </div>

        <div class="md:col-span-2 flex items-end">
          <button type="submit" class="btn-primary w-full">Buscar vuelos</button>
        </div>
      </form>
    </div>
    
    <!-- Promos y Ofertas -->
    <div class="airline-card mb-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 airline-gradient-accent rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" viewBox="0 0 20 20" fill="currentColor"><path d="M3 3h14l-2 14-5 2-5-2L3 3z"/></svg>
        </div>
        <h2 class="airline-subtitle">Promociones activas</h2>
      </div>
      <div class="grid md:grid-cols-3 gap-4">
        <div v-for="p in promos" :key="p.id" class="rounded-2xl p-5 text-white bg-gradient-to-br" :class="p.color">
          <div class="text-xl font-bold mb-1">{{ p.title }}</div>
          <div class="opacity-90">{{ p.desc }}</div>
        </div>
      </div>
    </div>

    <!-- Mis Reservas -->
    <div v-if="user" class="airline-card mb-8">
      <div class="flex items-center justify-between mb-6">
        <h2 class="airline-subtitle">Mis Reservas</h2>
        <div class="flex gap-2">
          <input v-model="reservationSearch" placeholder="Ingresa tu código de reservación" class="airline-input w-80" />
          <button class="btn-primary" @click="searchReservation">Buscar</button>
        </div>
      </div>

      <div v-if="loadingMyTickets" class="text-gray-500">Cargando tus reservas…</div>
      <div v-else-if="!myTickets.length" class="text-gray-500">No tienes reservas.</div>
      <div v-else class="space-y-4">
        <div v-for="t in myTickets" :key="t.idTicket" class="p-4 rounded-xl border border-gray-200 flex items-center justify-between">
          <div>
            <div class="text-lg font-bold">Ticket #{{ t.idTicket }}</div>
            <div class="text-sm text-gray-600">Vuelo: {{ t.flightNumber }} | Asiento: {{ t.seatNumber || '—' }} | {{ t.seatCategory }}</div>
            <div class="text-sm text-gray-600">Estado: {{ t.status }} | Pago: {{ t.paymentStatus }}</div>
            <div class="text-sm text-gray-700">Total: {{ t.totalAmount }}</div>
          </div>
          <div class="flex gap-2">
            <button class="btn-secondary" @click="router.push(`/reservation/${t.idTicket}`)">Ver detalle</button>
            <button class="bg-blue-600 text-white px-4 py-2 rounded-lg" @click="downloadTicketPdf(t.idTicket)">PDF</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Detalle de Reserva -->
    <div v-if="showTicketModal" class="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-semibold">Detalle de la Reserva</h3>
          <button class="text-gray-500 hover:text-gray-700" @click="closeTicketModal">✕</button>
        </div>
        <div v-if="ticketDetail" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <div class="text-sm text-gray-500 mb-1">Ticket</div>
            <div class="font-semibold">#{{ ticketDetail.idTicket }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500 mb-1">Código</div>
            <div class="font-semibold">{{ ticketDetail.reservationCode || '—' }}</div>
          </div>
          <div class="md:col-span-2 h-px bg-gray-100 my-2"></div>
          <div>
            <div class="text-sm text-gray-500 mb-1">Vuelo</div>
            <div class="font-semibold">{{ ticketDetail.flightNumber }}</div>
            <div class="text-sm text-gray-600">{{ ticketDetail.originCity }} → {{ ticketDetail.destinationCity }}</div>
            <div class="text-sm text-gray-600">{{ ticketDetail.departureDate }} {{ ticketDetail.departureTime }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500 mb-1">Pasajero</div>
            <div class="font-semibold">{{ ticketDetail.passengerFirstName }} {{ ticketDetail.passengerLastName }}</div>
            <div class="text-sm text-gray-600">Documento: {{ ticketDetail.passengerDocumentType }} {{ ticketDetail.passengerDocumentNumber }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500 mb-1">Asiento</div>
            <div class="font-semibold">{{ ticketDetail.seatNumber || 'Asignación pendiente' }}</div>
            <div class="text-sm text-gray-600">Categoría: {{ ticketDetail.seatCategory }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500 mb-1">Pago</div>
            <div class="font-semibold">{{ ticketDetail.totalAmount }}</div>
            <div class="text-sm text-gray-600">Estado: {{ ticketDetail.paymentStatus }} | Método: {{ ticketDetail.paymentMethod }}</div>
          </div>
        </div>
        <div class="mt-6 flex justify-end gap-2">
          <button class="btn-secondary" @click="closeTicketModal">Cerrar</button>
          <button class="btn-primary" v-if="ticketDetail" @click="downloadTicketPdf(ticketDetail.idTicket)">Descargar PDF</button>
        </div>
      </div>
    </div>

    <!-- Estado de Vuelos (si hay usuario) -->
    <div v-if="user" class="airline-card mb-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 airline-gradient-primary rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" viewBox="0 0 20 20" fill="currentColor"><path d="M2 11l6-2 4-6 2 6 4 2-5 1-2 5-3-5-6-1z"/></svg>
        </div>
        <h2 class="airline-subtitle">Vuelos próximos</h2>
      </div>
      <div v-if="loadingUpcoming" class="text-gray-500">Cargando tus vuelos…</div>
      <div v-else-if="upcomingTickets.length === 0" class="text-gray-500">No tienes vuelos próximos.</div>
      <div v-else class="grid md:grid-cols-3 gap-4">
        <div v-for="t in upcomingTickets" :key="t.idTicket || t.id" class="p-4 rounded-xl border border-gray-200">
          <div class="text-sm text-gray-500 mb-1">{{ t.flight?.flightNumber || 'Vuelo' }}</div>
          <div class="font-semibold">{{ t.flight?.originCity?.name || t.origin }} → {{ t.flight?.destinationCity?.name || t.destination }}</div>
          <div class="text-gray-600 text-sm mt-1">{{ t.flight?.departureDate || t.departureDate }} {{ t.flight?.departureTime || t.departureTime }}</div>
          <div class="mt-2 inline-flex items-center gap-2 text-xs px-2 py-1 rounded-full" :class="t.status==='CONFIRMED' ? 'bg-green-50 text-green-700' : 'bg-yellow-50 text-yellow-700'">{{ t.status || 'PENDING' }}</div>
        </div>
      </div>
    </div>

    <!-- Consejos de viaje -->
    <div class="airline-card mb-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-10 h-10 airline-gradient-secondary rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" viewBox="0 0 20 20" fill="currentColor"><path d="M10 2l2 5 5 1-4 3 1 5-4-3-4 3 1-5-4-3 5-1 2-5z"/></svg>
        </div>
        <h2 class="airline-subtitle">Consejos de viaje</h2>
      </div>
      <div class="grid md:grid-cols-4 gap-4">
        <div class="p-4 rounded-xl border border-gray-200"><div class="font-semibold mb-1">Documentos</div><div class="text-gray-600 text-sm">Verifica pasaporte y visas vigentes.</div></div>
        <div class="p-4 rounded-xl border border-gray-200"><div class="font-semibold mb-1">Equipaje</div><div class="text-gray-600 text-sm">Etiqueta y respeta medidas permitidas.</div></div>
        <div class="p-4 rounded-xl border border-gray-200"><div class="font-semibold mb-1">Tiempo</div><div class="text-gray-600 text-sm">Llega 2 h antes (nacional) / 3 h (internacional).</div></div>
        <div class="p-4 rounded-xl border border-gray-200"><div class="font-semibold mb-1">Salud</div><div class="text-gray-600 text-sm">Hidrátate y camina si el vuelo es largo.</div></div>
      </div>
    </div>

    <!-- Programa de fidelidad -->
    <div class="airline-card airline-card-premium mb-8">
      <div class="flex items-center gap-3 mb-4">
        <div class="w-10 h-10 airline-gradient-sunset rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" viewBox="0 0 20 20" fill="currentColor"><path d="M10 2a8 8 0 100 16 8 8 0 000-16zm1 5H9v2H7v2h2v2h2v-2h2V9h-2V7z"/></svg>
        </div>
        <h2 class="airline-subtitle">AeroLinea Rewards</h2>
      </div>
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div class="text-gray-700">Acumula millas, accede a salas VIP y obtén embarque prioritario.</div>
        <button class="btn-primary">Conoce beneficios</button>
      </div>
    </div>

    <!-- Alertas operativas -->
    <div v-if="operationalAlerts.length" class="airline-card mb-8">
      <div class="flex items-center gap-3 mb-4">
        <div class="w-10 h-10 airline-gradient-primary rounded-full flex items-center justify-center">
          <svg class="w-5 h-5 text-white" viewBox="0 0 20 20" fill="currentColor"><path d="M8.257 3.099c.765-1.36 2.721-1.36 3.486 0l6.518 11.59c.75 1.335-.213 2.99-1.743 2.99H3.482c-1.53 0-2.493-1.655-1.743-2.99l6.518-11.59zM11 14H9v-2h2v2zm0-4H9V6h2v4z"/></svg>
        </div>
        <h2 class="airline-subtitle">Alertas operativas</h2>
      </div>
      <div class="space-y-3">
        <div v-for="a in operationalAlerts" :key="a.id" class="p-3 rounded-lg border" :class="a.severity==='critical' ? 'border-red-300 bg-red-50' : a.severity==='warning' ? 'border-amber-300 bg-amber-50' : 'border-blue-300 bg-blue-50'">
          <div class="font-semibold">{{ a.title }}</div>
          <div class="text-sm text-gray-700">{{ a.description }}</div>
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
        
        <div 
          @click="router.push('/admin/corporate-users')" 
          class="airline-card cursor-pointer transform hover:scale-105 transition-all duration-300 group"
        >
          <div class="flex flex-col items-center text-center">
            <div class="w-14 h-14 bg-indigo-100 rounded-2xl flex items-center justify-center mb-3 group-hover:airline-gradient-primary transition-all">
              <svg class="w-7 h-7 text-indigo-600 group-hover:text-white" fill="currentColor" viewBox="0 0 20 20">
                <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z"/>
              </svg>
            </div>
            <h3 class="font-bold mb-2 group-hover:text-blue-700">🏢 Usuarios Empresariales</h3>
            <p class="text-sm text-gray-600">Gestión de clientes corporativos</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
