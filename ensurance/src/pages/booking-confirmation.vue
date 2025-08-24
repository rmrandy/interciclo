<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();
const booking = ref<any>(null);
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

// Cargar detalles de la reserva
const loadBookingDetails = async () => {
  try {
    const bookingId = route.params.bookingId;
    
    // Simular datos de reserva
    booking.value = {
      id: bookingId,
      status: 'confirmed',
      confirmationNumber: bookingId,
      bookingDate: new Date().toISOString(),
      flight: {
        flightNumber: "AE001",
        origin: "Ciudad de Guatemala",
        destination: "Miami",
        departureTime: "08:00",
        arrivalTime: "11:30",
        date: "2025-01-15",
        aircraft: "Boeing 737",
        gate: "A12",
        terminal: "Terminal 1"
      },
      passengers: 1,
      seatNumbers: ["12A"],
      totalPrice: 450.00,
      paymentMethod: "Tarjeta de crédito",
      specialRequests: "Comida vegetariana"
    };
    
  } catch (err) {
    console.error("Error al cargar detalles de la reserva:", err);
    error.value = "No se pudieron cargar los detalles de la reserva.";
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
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// Imprimir boleto
const printTicket = () => {
  window.print();
};

// Enviar por email
const sendByEmail = () => {
  const subject = `Confirmación de Reserva - ${booking.value.flight.flightNumber}`;
  const body = `
Estimado/a ${user.value?.firstName},

Su reserva ha sido confirmada exitosamente.

Número de confirmación: ${booking.value.confirmationNumber}
Vuelo: ${booking.value.flight.flightNumber}
Ruta: ${booking.value.flight.origin} → ${booking.value.flight.destination}
Fecha: ${booking.value.flight.date}
Hora de salida: ${booking.value.flight.departureTime}

Gracias por elegir AeroLinea.
  `;
  
  const mailtoLink = `mailto:${user.value?.email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
  window.location.href = mailtoLink;
};

onMounted(() => {
  checkAuth();
  loadBookingDetails();
});
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">Cargando confirmación...</p>
    </div>

    <!-- Error -->
    <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-center">
      <h3 class="font-semibold mb-2">Error</h3>
      <p>{{ error }}</p>
      <router-link to="/flights" class="btn-airline-primary mt-4 inline-block">
        Volver a Vuelos
      </router-link>
    </div>

    <!-- Confirmación -->
    <div v-if="!loading && !error && booking" class="max-w-4xl mx-auto">
      <!-- Header de éxito -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 bg-green-500 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
          </svg>
        </div>
        <h1 class="text-4xl font-bold text-gray-800 mb-2">¡Reserva Confirmada!</h1>
        <p class="text-xl text-gray-600">Tu vuelo ha sido reservado exitosamente</p>
      </div>

      <!-- Información de confirmación -->
      <div class="bg-green-50 border border-green-200 rounded-xl p-6 mb-8">
        <div class="grid md:grid-cols-2 gap-6">
          <div>
            <h3 class="text-lg font-semibold text-green-800 mb-2">Número de Confirmación</h3>
            <p class="text-2xl font-bold text-green-700 font-mono">{{ booking.confirmationNumber }}</p>
            <p class="text-sm text-green-600 mt-1">Guarda este número para futuras consultas</p>
          </div>
          <div>
            <h3 class="text-lg font-semibold text-green-800 mb-2">Estado de la Reserva</h3>
            <div class="flex items-center gap-2">
              <div class="w-3 h-3 bg-green-500 rounded-full"></div>
              <span class="text-green-700 font-semibold">Confirmada</span>
            </div>
            <p class="text-sm text-green-600 mt-1">Reservado el {{ formatDate(booking.bookingDate) }}</p>
          </div>
        </div>
      </div>

      <!-- Detalles del vuelo -->
      <div class="airline-card mb-8">
        <h2 class="text-2xl font-bold text-gray-800 mb-6">Detalles del Vuelo</h2>
        
        <div class="grid lg:grid-cols-2 gap-8">
          <!-- Información del vuelo -->
          <div>
            <div class="flex items-center gap-4 mb-6">
              <div class="bg-blue-600 text-white px-3 py-1 rounded-full text-sm font-medium">
                {{ booking.flight.flightNumber }}
              </div>
              <div class="text-sm text-gray-500">{{ booking.flight.aircraft }}</div>
            </div>
            
            <div class="space-y-4">
              <div class="flex items-center gap-6">
                <div class="text-center">
                  <div class="text-2xl font-bold text-gray-800">{{ booking.flight.departureTime }}</div>
                  <div class="text-sm text-gray-600">{{ booking.flight.origin }}</div>
                  <div class="text-xs text-gray-500">{{ booking.flight.date }}</div>
                </div>
                
                <div class="flex-1 flex items-center justify-center">
                  <div class="h-px bg-gray-300 flex-1"></div>
                  <svg class="w-6 h-6 text-gray-400 mx-2" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
                  </svg>
                  <div class="h-px bg-gray-300 flex-1"></div>
                </div>
                
                <div class="text-center">
                  <div class="text-2xl font-bold text-gray-800">{{ booking.flight.arrivalTime }}</div>
                  <div class="text-sm text-gray-600">{{ booking.flight.destination }}</div>
                  <div class="text-xs text-gray-500">{{ booking.flight.date }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Información adicional -->
          <div class="space-y-4">
            <div class="bg-gray-50 p-4 rounded-lg">
              <h3 class="font-semibold text-gray-700 mb-2">Puerta de Embarque</h3>
              <p class="text-gray-600">{{ booking.flight.gate }} - {{ booking.flight.terminal }}</p>
              <p class="text-xs text-gray-500 mt-1">Confirma en el aeropuerto el día del vuelo</p>
            </div>
            
            <div class="bg-gray-50 p-4 rounded-lg">
              <h3 class="font-semibold text-gray-700 mb-2">Asientos Asignados</h3>
              <p class="text-gray-600">{{ booking.seatNumbers.join(', ') }}</p>
            </div>

            <div v-if="booking.specialRequests" class="bg-gray-50 p-4 rounded-lg">
              <h3 class="font-semibold text-gray-700 mb-2">Solicitudes Especiales</h3>
              <p class="text-gray-600">{{ booking.specialRequests }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Información del pasajero -->
      <div class="airline-card mb-8">
        <h2 class="text-2xl font-bold text-gray-800 mb-6">Información del Pasajero</h2>
        
        <div class="grid md:grid-cols-2 gap-6">
          <div class="bg-blue-50 p-4 rounded-lg">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center">
                <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-6-3a2 2 0 11-4 0 2 2 0 014 0zm-2 4a5 5 0 00-4.546 2.916A5.986 5.986 0 0010 16a5.986 5.986 0 004.546-2.084A5 5 0 0010 11z" clip-rule="evenodd"/>
                </svg>
              </div>
              <div>
                <p class="font-semibold text-blue-800">{{ user?.firstName }} {{ user?.lastName }}</p>
                <p class="text-blue-600 text-sm">{{ user?.email }}</p>
                <p class="text-blue-600 text-sm">Pasaporte: {{ user?.passportNumber }}</p>
              </div>
            </div>
          </div>

          <div class="bg-gray-50 p-4 rounded-lg">
            <h3 class="font-semibold text-gray-700 mb-2">Resumen de Pago</h3>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-600">Total Pagado</span>
                <span class="font-semibold">{{ formatPrice(booking.totalPrice) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Método de Pago</span>
                <span class="text-gray-600">{{ booking.paymentMethod }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Instrucciones importantes -->
      <div class="airline-card mb-8">
        <h2 class="text-2xl font-bold text-gray-800 mb-6">Instrucciones Importantes</h2>
        
        <div class="grid md:grid-cols-2 gap-6">
          <div>
            <h3 class="font-semibold text-gray-700 mb-3">Antes del Vuelo</h3>
            <ul class="space-y-2 text-sm text-gray-600">
              <li class="flex items-start gap-2">
                <svg class="w-4 h-4 text-blue-500 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                Llega al aeropuerto 2 horas antes para vuelos nacionales
              </li>
              <li class="flex items-start gap-2">
                <svg class="w-4 h-4 text-blue-500 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                3 horas antes para vuelos internacionales
              </li>
              <li class="flex items-start gap-2">
                <svg class="w-4 h-4 text-blue-500 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                Verifica la documentación requerida
              </li>
              <li class="flex items-start gap-2">
                <svg class="w-4 h-4 text-blue-500 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                Realiza el check-in online 24h antes
              </li>
            </ul>
          </div>

          <div>
            <h3 class="font-semibold text-gray-700 mb-3">Equipaje</h3>
            <ul class="space-y-2 text-sm text-gray-600">
              <li class="flex items-start gap-2">
                <svg class="w-4 h-4 text-green-500 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                Equipaje de mano: 8kg incluido
              </li>
              <li class="flex items-start gap-2">
                <svg class="w-4 h-4 text-green-500 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                Equipaje facturado: 23kg incluido
              </li>
              <li class="flex items-start gap-2">
                <svg class="w-4 h-4 text-yellow-500 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                Revisa las restricciones de líquidos
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Acciones -->
      <div class="flex flex-wrap gap-4 justify-center">
        <button @click="printTicket" class="btn-airline-primary">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M5 4v3H4a2 2 0 00-2 2v3a2 2 0 002 2h1v2a2 2 0 002 2h6a2 2 0 002-2v-2h1a2 2 0 002-2V9a2 2 0 00-2-2h-1V4a2 2 0 00-2-2H7a2 2 0 00-2 2zm8 0H7v3h6V4zm0 8H7v4h6v-4z" clip-rule="evenodd"/>
          </svg>
          Imprimir Boleto
        </button>
        
        <button @click="sendByEmail" class="btn-airline-outline">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
            <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
          </svg>
          Enviar por Email
        </button>
        
        <router-link to="/my-bookings" class="btn-airline-outline">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"/>
            <path fill-rule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v6a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3z" clip-rule="evenodd"/>
          </svg>
          Ver Mis Reservas
        </router-link>
        
        <router-link to="/flights" class="btn-airline-secondary">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
          </svg>
          Buscar Más Vuelos
        </router-link>
      </div>
    </div>
  </div>
</template>

<style>
@media print {
  .btn-airline-primary,
  .btn-airline-outline,
  .btn-airline-secondary {
    display: none;
  }
}
</style>
