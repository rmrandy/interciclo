<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getAirlineApiUrl } from '../utils/api'
import { airlineApi } from '../utils/airlineApi'

const tickets = ref<any[]>([])
const loading = ref(false)
const error = ref('')

// búsqueda por código
const code = ref('')
const searching = ref(false)
const searchResult = ref<any | null>(null)

const fetchTickets = async () => {
  try {
    loading.value = true
    error.value = ''
    const user = JSON.parse(localStorage.getItem('user') || 'null')
    if (!user || !user.idUser) {
      error.value = 'Sesión no válida'
      return
    }
    const url = getAirlineApiUrl(`tickets?userId=${user.idUser}`)
    const res = await fetch(url)
    if (!res.ok) throw new Error(String(res.status))
    const data = await res.json()
    if (data && data.success && Array.isArray(data.tickets)) {
      tickets.value = data.tickets
    } else if (Array.isArray(data)) {
      tickets.value = data
    } else {
      error.value = data?.error || 'No se pudieron obtener las reservas'
    }
  } catch (e: any) {
    error.value = e?.message || 'Error cargando reservas'
  } finally {
    loading.value = false
  }
}

const searchByCode = async () => {
  if (!code.value.trim()) return
  try {
    searching.value = true
    error.value = ''
    const res = await airlineApi.getTicketByCode(code.value.trim())
    if (res?.success && res.ticket) {
      searchResult.value = res.ticket
    } else {
      searchResult.value = null
      error.value = res?.error || 'Código no encontrado'
    }
  } catch (e: any) {
    searchResult.value = null
    error.value = e?.message || 'Error consultando el código'
  } finally {
    searching.value = false
  }
}

const downloadPdf = async (ticketId: number) => {
  try {
    const blob = await airlineApi.downloadTicketPdf(ticketId)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `Ticket-${ticketId}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    alert('No se pudo descargar el PDF')
  }
}

onMounted(fetchTickets)
</script>

<template>
  <div class="container">
    <h2>Mis Reservas</h2>

    <!-- Buscador por código de reservación -->
    <div class="search-box">
      <input v-model="code" class="code-input" placeholder="Ingresa tu código de reservación" />
      <button :disabled="!code || searching" @click="searchByCode" class="search-btn">Buscar</button>
    </div>

    <div v-if="searchResult" class="search-result">
      <div class="result-header">
        <strong>Resultado</strong>
        <button class="pdf-btn" @click="downloadPdf(searchResult.idTicket)">Descargar PDF</button>
      </div>
      <div>Ticket #{{ searchResult.idTicket }} — Código: {{ searchResult.reservationCode }}</div>
      <div>Vuelo: {{ searchResult.flightNumber }} | {{ searchResult.originCity }} → {{ searchResult.destinationCity }}</div>
      <div>Salida: {{ searchResult.departureDate }} {{ searchResult.departureTime }}</div>
      <div>Estado: {{ searchResult.status }} | Pago: {{ searchResult.paymentStatus }}</div>
    </div>

    <div v-if="loading" class="loading">Cargando...</div>
    <div v-else>
      <div v-if="error" class="error">{{ error }}</div>
      <div v-else-if="tickets.length === 0" class="empty">No tienes reservas.</div>
      <ul v-else class="list">
        <li v-for="t in tickets" :key="t.idTicket" class="item">
          <div class="row">
            <div>
              <strong>Ticket #{{ t.idTicket }}</strong>
              <div>Código: {{ t.reservationCode || '—' }}</div>
              <div>Vuelo: {{ t.flightNumber }} | Asiento: {{ t.seatNumber }} | {{ t.seatCategory }}</div>
              <div>Estado: {{ t.status }} | Pago: {{ t.paymentStatus }}</div>
              <div>Total: {{ t.totalAmount }}</div>
            </div>
            <div>
              <button class="pdf-btn" @click="downloadPdf(t.idTicket)">PDF</button>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.container { max-width: 960px; margin: 2rem auto; padding: 1rem; }
.loading { opacity: .7 }
.error { color: #b91c1c }
.empty { color: #64748b }
.list { display: grid; gap: 12px; }
.item { padding: 12px; border: 1px solid #e2e8f0; border-radius: 10px; background: #fff; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.search-box { display:flex; gap:8px; margin: 12px 0 20px; }
.code-input { flex:1; padding:10px 12px; border:1px solid #cbd5e1; border-radius:8px; }
.search-btn, .pdf-btn { background:#2563eb; color:#fff; border:none; padding:8px 12px; border-radius:8px; cursor:pointer; }
.search-btn:disabled { opacity:.6; cursor:not-allowed; }
.search-result { background:#f1f5f9; border:1px solid #cbd5e1; padding:12px; border-radius:10px; margin-bottom:16px; }
.result-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
</style>
