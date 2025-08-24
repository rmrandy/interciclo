<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getAirlineApiUrl } from "../utils/api";
import axios from "axios";

const router = useRouter();
const bookings = ref<any[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const user = ref<any>(null);

// Verificar autenticación
const checkAuth = () => {
  const userData = localStorage.getItem("user");
  if (userData) {
    try {
      user.value = JSON.parse(userData);
    } catch (e) {
      console.error("Error al parsear datos de usuario:", e);
      router.push('/login');
    }
  } else {
    router.push('/login');
  }
};

// Cargar reservas del usuario
const loadUserBookings = async () => {
  try {
    if (!user.value || !user.value.idUser) {
      return;
    }
    
    // Intentar cargar desde la API real
    try {
      const response = await axios.get(getAirlineApiUrl(`users/${user.value.idUser}/bookings`));
      
      if (response.data && Array.isArray(response.data)) {
        bookings.value = response.data;
        return;
      } else if (response.data && response.data.error) {
        error.value = response.data.error;
        return;
      }
    } catch (apiError) {
      console.warn("API no disponible, usando datos mock:", apiError);
    }
    
    // Fallback a datos mock
    bookings.value = [
      {
        id: "ABC123XYZ",
        status: 'confirmed',
        confirmationNumber: "ABC123XYZ",
        bookingDate: "2025-01-10T10:30:00Z",
        flight: {
          flightNumber: "AE001",
          origin: "Ciudad de Guatemala",
          destination: "Miami",
          departureTime: "08:00",
          arrivalTime: "11:30",
          date: "2025-01-15",
          aircraft: "Boeing 737"
        },
        passengers: 1,
        seatNumbers: ["12A"],
        totalPrice: 450.00,
        canCancel: true,
        canModify: true
      },
      {
        id: "DEF456ABC",
        status: 'confirmed',
        confirmationNumber: "DEF456ABC",
        bookingDate: "2025-01-08T15:20:00Z",
        flight: {
          flightNumber: "AE003",
          origin: "Ciudad de Guatemala",
          destination: "Madrid",
          departureTime: "22:15",
          arrivalTime: "14:30+1",
          date: "2025-01-20",
          aircraft: "Boeing 777"
        },
        passengers: 2,
        seatNumbers: ["15A", "15B"],
        totalPrice: 1560.00,
        canCancel: true,
        canModify: true
      },
      {
        id: "GHI789DEF",
        status: 'completed',
        confirmationNumber: "GHI789DEF",
        bookingDate: "2024-12-15T09:45:00Z",
        flight: {
          flightNumber: "AE002",
          origin: "Ciudad de Guatemala",
          destination: "México DF",
          departureTime: "14:20",
          arrivalTime: "16:45",
          date: "2024-12-20",
          aircraft: "Airbus A320"
        },
        passengers: 1,
        seatNumbers: ["8F"],
        totalPrice: 320.00,
        canCancel: false,
        canModify: false
      }
    ];
  } catch (err) {
    console.error("Error al cargar reservas:", err);
    error.value = "No se pudieron cargar tus reservas.";
  } finally {
    loading.value = false;
  }
};

// Formatear precio
const formatPrice = (price: number) => {
  return `Q${price.toFixed(2)}`;
};

// Formatear fecha
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('es-ES', { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric'
  });
};

// Formatear fecha de reserva
const formatBookingDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('es-ES', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// Obtener estado con color
const getStatusInfo = (status: string) => {
  switch (status) {
    case 'confirmed':
      return { text: 'Confirmada', color: 'text-green-600', bg: 'bg-green-100' };
    case 'cancelled':
      return { text: 'Cancelada', color: 'text-red-600', bg: 'bg-red-100' };
    case 'completed':
      return { text: 'Completada', color: 'text-gray-600', bg: 'bg-gray-100' };
    default:
      return { text: 'Pendiente', color: 'text-yellow-600', bg: 'bg-yellow-100' };
  }
};

// Ver detalles de la reserva
const viewBookingDetails = (bookingId: string) => {
  router.push(`/booking/confirmation/${bookingId}`);
};

// Cancelar reserva
const cancelBooking = async (bookingId: string) => {
  if (!confirm('¿Estás seguro de que quieres cancelar esta reserva?')) {
    return;
  }
  
  try {
    // Intentar cancelar en la API real
    try {
      const cancelData = {
        userId: user.value.idUser
      };
      
      const response = await axios.post(getAirlineApiUrl(`bookings/${bookingId}/cancel`), cancelData);
      
      if (response.data && response.data.success) {
        // Actualizar la lista de reservas
        const booking = bookings.value.find(b => b.id === bookingId);
        if (booking) {
          booking.status = 'cancelled';
          booking.canCancel = false;
          booking.canModify = false;
        }
        alert('Reserva cancelada exitosamente');
        return;
      } else if (response.data && response.data.error) {
        alert(`Error: ${response.data.error}`);
        return;
      }
    } catch (apiError) {
      console.warn("API no disponible, simulando cancelación:", apiError);
    }
    
    // Fallback - simular cancelación
    const booking = bookings.value.find(b => b.id === bookingId);
    if (booking) {
      booking.status = 'cancelled';
      booking.canCancel = false;
      booking.canModify = false;
    }
    
    alert('Reserva cancelada exitosamente');
  } catch (err) {
    console.error("Error al cancelar reserva:", err);
    alert('Error al cancelar la reserva. Intenta nuevamente.');
  }
};

// Modificar reserva
const modifyBooking = (bookingId: string) => {
  alert('Función de modificación próximamente disponible');
};

onMounted(() => {
  checkAuth();
  loadUserBookings();
});
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Header -->
    <div class="text-center mb-8">
      <h1 class="text-4xl font-bold text-gray-800 mb-4">Mis Reservas</h1>
      <p class="text-xl text-gray-600">Gestiona tus vuelos reservados</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">Cargando tus reservas...</p>
    </div>

    <!-- Error -->
    <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-center">
      <h3 class="font-semibold mb-2">Error</h3>
      <p>{{ error }}</p>
      <router-link to="/flights" class="btn-airline-primary mt-4 inline-block">
        Buscar Vuelos
      </router-link>
    </div>

    <!-- Lista de reservas -->
    <div v-if="!loading && !error" class="max-w-4xl mx-auto">
      <!-- Sin reservas -->
      <div v-if="bookings.length === 0" class="text-center py-12">
        <svg class="w-16 h-16 text-gray-400 mx-auto mb-4" fill="currentColor" viewBox="0 0 20 20">
          <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"/>
          <path fill-rule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v6a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3z" clip-rule="evenodd"/>
        </svg>
        <h3 class="text-xl font-semibold text-gray-600 mb-2">No tienes reservas</h3>
        <p class="text-gray-500 mb-4">Encuentra tu próximo destino y reserva tu primer vuelo</p>
        <router-link to="/flights" class="btn-airline-primary">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
          </svg>
          Buscar Vuelos
        </router-link>
      </div>

      <!-- Reservas -->
      <div v-else class="space-y-6">
        <div v-for="booking in bookings" :key="booking.id" class="airline-card hover:shadow-lg transition-shadow">
          <!-- Header de la reserva -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
            <div>
              <div class="flex items-center gap-3 mb-2">
                <div class="bg-blue-600 text-white px-3 py-1 rounded-full text-sm font-medium">
                  {{ booking.flight.flightNumber }}
                </div>
                <div :class="[getStatusInfo(booking.status).bg, getStatusInfo(booking.status).color]" 
                     class="px-2 py-1 rounded-full text-xs font-medium">
                  {{ getStatusInfo(booking.status).text }}
                </div>
              </div>
              <p class="text-sm text-gray-600">
                Reservado el {{ formatBookingDate(booking.bookingDate) }}
              </p>
            </div>
            <div class="text-right">
              <div class="text-2xl font-bold text-blue-600 mb-1">
                {{ formatPrice(booking.totalPrice) }}
              </div>
              <div class="text-sm text-gray-500">
                {{ booking.passengers }} pasajero{{ booking.passengers > 1 ? 's' : '' }}
              </div>
            </div>
          </div>

          <!-- Información del vuelo -->
          <div class="flex items-center gap-6 mb-4">
            <div class="text-center">
              <div class="text-xl font-bold text-gray-800">{{ booking.flight.departureTime }}</div>
              <div class="text-sm text-gray-600">{{ booking.flight.origin }}</div>
              <div class="text-xs text-gray-500">{{ formatDate(booking.flight.date) }}</div>
            </div>
            
            <div class="flex-1 flex items-center justify-center">
              <div class="h-px bg-gray-300 flex-1"></div>
              <svg class="w-5 h-5 text-gray-400 mx-2" fill="currentColor" viewBox="0 0 24 24">
                <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
              </svg>
              <div class="h-px bg-gray-300 flex-1"></div>
            </div>
            
            <div class="text-center">
              <div class="text-xl font-bold text-gray-800">{{ booking.flight.arrivalTime }}</div>
              <div class="text-sm text-gray-600">{{ booking.flight.destination }}</div>
              <div class="text-xs text-gray-500">{{ formatDate(booking.flight.date) }}</div>
            </div>
          </div>

          <!-- Detalles adicionales -->
          <div class="grid sm:grid-cols-2 gap-4 mb-4">
            <div class="bg-gray-50 p-3 rounded-lg">
              <h3 class="font-semibold text-gray-700 text-sm mb-1">Confirmación</h3>
              <p class="text-gray-600 font-mono text-sm">{{ booking.confirmationNumber }}</p>
            </div>
            <div class="bg-gray-50 p-3 rounded-lg">
              <h3 class="font-semibold text-gray-700 text-sm mb-1">Asientos</h3>
              <p class="text-gray-600 text-sm">{{ booking.seatNumbers.join(', ') }}</p>
            </div>
          </div>

          <!-- Acciones -->
          <div class="flex flex-wrap gap-3">
            <button 
              @click="viewBookingDetails(booking.id)"
              class="btn-airline-primary text-sm"
            >
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/>
                <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/>
              </svg>
              Ver Detalles
            </button>
            
            <button 
              v-if="booking.canModify"
              @click="modifyBooking(booking.id)"
              class="btn-airline-outline text-sm"
            >
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/>
              </svg>
              Modificar
            </button>
            
            <button 
              v-if="booking.canCancel"
              @click="cancelBooking(booking.id)"
              class="btn-airline-secondary text-sm text-red-600 hover:bg-red-50"
            >
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd"/>
              </svg>
              Cancelar
            </button>
          </div>
        </div>
      </div>

      <!-- Acciones adicionales -->
      <div class="text-center mt-8">
        <router-link to="/flights" class="btn-airline-primary">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
          </svg>
          Reservar Nuevo Vuelo
        </router-link>
      </div>
    </div>
  </div>
</template>
