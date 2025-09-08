<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AirlineApiClient } from '../utils/airlineApi'
import CommentsThread from '../components/CommentsThread.vue'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)
const api = new AirlineApiClient()
const loading = ref(true)
const ticket = ref<any | null>(null)
const reviews = ref<any[]>([])
const newRating = ref(5)
const newComment = ref('')
const user = ref<any>(null)
const currentUserId = ref<number | null>(null)

onMounted(async () => {
  user.value = JSON.parse(localStorage.getItem('user') || 'null')
  currentUserId.value = user.value?.idUser ?? null
  await loadTicket()
  await loadReviews()
})

async function loadTicket() {
  loading.value = true
  try {
    const res = await api.getTicketById(id)
    if (res?.success) ticket.value = res.ticket
  } finally {
    loading.value = false
  }
}

async function loadReviews() {
  try {
    if (!ticket.value) return
    const flightId = ticket.value.flightId || ticket.value.flight?.idFlight
    if (!flightId) return
    const res = await api.getFlightReviews(flightId, { mode: 'tree' as any })
    reviews.value = Array.isArray(res?.reviews) ? res.reviews : []
  } catch {}
}

async function submitReview() {
  if (!user.value) {
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
    return
  }
  if (!ticket.value) return
  const flightId = ticket.value.flightId || ticket.value.flight?.idFlight
  if (!flightId) return
  try {
    await api.createFlightReview(flightId, { userId: user.value.idUser, rating: newRating.value, comment: newComment.value })
    newComment.value = ''
    newRating.value = 5
    await loadReviews()
  } catch (e) {
    alert('No se pudo enviar tu reseña')
  }
}
</script>

<template>
  <div class="container mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-4">Detalle de la Reserva</h1>
    <div v-if="loading">Cargando…</div>
    <div v-else-if="!ticket">No encontrado</div>
    <div v-else class="grid md:grid-cols-3 gap-6">
      <div class="md:col-span-2 space-y-4">
        <div class="airline-card">
          <h2 class="airline-subtitle mb-3">Vuelo</h2>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div><span class="text-gray-500">Vuelo:</span> <span class="font-semibold">{{ ticket.flightNumber }}</span></div>
            <div><span class="text-gray-500">Ruta:</span> <span class="font-semibold">{{ ticket.originCity }} → {{ ticket.destinationCity }}</span></div>
            <div><span class="text-gray-500">Salida:</span> <span class="font-semibold">{{ ticket.departureDate }} {{ ticket.departureTime }}</span></div>
            <div><span class="text-gray-500">Asiento:</span> <span class="font-semibold">{{ ticket.seatNumber || '—' }} ({{ ticket.seatCategory }})</span></div>
          </div>
        </div>
        <div class="airline-card">
          <h2 class="airline-subtitle mb-3">Pasajero</h2>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div><span class="text-gray-500">Nombre:</span> <span class="font-semibold">{{ ticket.passengerFirstName }} {{ ticket.passengerLastName }}</span></div>
            <div><span class="text-gray-500">Documento:</span> <span class="font-semibold">{{ ticket.passengerDocumentType }} {{ ticket.passengerDocumentNumber }}</span></div>
            <div><span class="text-gray-500">Correo:</span> <span class="font-semibold">{{ ticket.passengerEmail }}</span></div>
            <div><span class="text-gray-500">Teléfono:</span> <span class="font-semibold">{{ ticket.passengerPhone }}</span></div>
          </div>
        </div>
        <div class="airline-card">
          <h2 class="airline-subtitle mb-3">Pago</h2>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div><span class="text-gray-500">Estado:</span> <span class="font-semibold">{{ ticket.paymentStatus }}</span></div>
            <div><span class="text-gray-500">Método:</span> <span class="font-semibold">{{ ticket.paymentMethod }}</span></div>
            <div><span class="text-gray-500">Total:</span> <span class="font-semibold">{{ ticket.totalAmount }}</span></div>
          </div>
        </div>
      </div>
      <div>
        <div class="airline-card mb-4">
          <h2 class="airline-subtitle mb-2">Calificaciones</h2>
          <div v-if="!reviews.length" class="text-gray-500 text-sm">Sé el primero en opinar.</div>
          <CommentsThread v-else :items="reviews" :flight-id="(ticket.flightId || ticket.flight?.idFlight)" :current-user-id="currentUserId" @refresh="loadReviews" />
        </div>
        <div class="airline-card">
          <h2 class="airline-subtitle mb-2">Escribe tu reseña</h2>
          <div class="flex items-center gap-2 mb-2 text-yellow-500">
            <button v-for="i in 5" :key="i" @click="newRating = i">{{ i <= newRating ? '★' : '☆' }}</button>
          </div>
          <textarea v-model="newComment" rows="3" class="airline-input w-full" placeholder="Tu comentario (opcional)"></textarea>
          <button class="btn-primary mt-2 w-full" @click="submitReview">Publicar</button>
        </div>
      </div>
    </div>
  </div>
</template>


