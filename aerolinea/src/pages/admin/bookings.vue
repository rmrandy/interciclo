<template>
  <div class="admin-container">
    <div class="header">
      <h2>Compras / Boletos</h2>
      <div class="actions">
        <button class="btn" @click="load">Refrescar</button>
        <button class="btn-outline" @click="exportCsv">Exportar CSV</button>
      </div>
    </div>

    <div class="filters">
      <div class="field">
        <label>ID Usuario</label>
        <input v-model="filters.userId" type="number" placeholder="Ej. 1" />
      </div>
      <div class="field">
        <label>ID Vuelo</label>
        <input v-model="filters.flightId" type="number" placeholder="Ej. 30" />
      </div>
      <div class="field">
        <label>Estado</label>
        <select v-model="filters.status">
          <option value="">Todos</option>
          <option value="RESERVED">RESERVED</option>
          <option value="CONFIRMED">CONFIRMED</option>
          <option value="CANCELLED">CANCELLED</option>
          <option value="REFUNDED">REFUNDED</option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn" @click="load">Buscar</button>
        <button class="btn-outline" @click="reset">Limpiar</button>
      </div>
    </div>

    <div v-if="loading" class="loading">Cargando...</div>
    <div v-else>
      <div v-if="error" class="error">{{ error }}</div>
      <div v-else class="card">
        <table class="table">
          <thead>
            <tr>
              <th>Ticket</th>
              <th>Usuario</th>
              <th>Vuelo</th>
              <th>Asiento</th>
              <th>Categoría</th>
              <th>Estado</th>
              <th>Pago</th>
              <th>Total</th>
              <th>Fecha</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tickets" :key="t.idTicket">
              <td>#{{ t.idTicket }}</td>
              <td>{{ t.userId || '-' }}</td>
              <td>{{ t.flightNumber }} <small>({{ t.originCity }} → {{ t.destinationCity }})</small></td>
              <td>{{ t.seatNumber }}</td>
              <td><span class="badge">{{ t.seatCategory }}</span></td>
              <td>
                <span :class="['chip', statusClass(t.status)]">{{ t.status }}</span>
              </td>
              <td>
                <span :class="['chip', paymentClass(t.paymentStatus)]">{{ t.paymentStatus }}</span>
              </td>
              <td>{{ t.totalAmount }}</td>
              <td>{{ t.createdAt || t.bookingDate }}</td>
              <td class="row-actions">
                <button class="btn-xs" @click="view(t)">Ver</button>
                <button class="btn-xs" @click="markPaid(t)" :disabled="t.paymentStatus==='PAID'">Marcar pagado</button>
                <button class="btn-xs danger" @click="cancelTicket(t)" :disabled="t.status==='CANCELLED'">Cancelar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal simple -->
    <div v-if="modal.visible" class="modal-backdrop" @click.self="modal.visible=false">
      <div class="modal">
        <h3>Detalle del Ticket #{{ modal.ticket?.idTicket }}</h3>
        <pre class="pre">{{ modal.ticket }}</pre>
        <div class="modal-actions">
          <button class="btn" @click="modal.visible=false">Cerrar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getAirlineApiUrl } from '../../utils/api'

const filters = ref({ userId: '', flightId: '', status: '' })
const tickets = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const modal = ref<{ visible: boolean; ticket: any | null }>({ visible: false, ticket: null })

const buildQuery = () => {
  const params: string[] = []
  if (filters.value.userId) params.push(`userId=${filters.value.userId}`)
  if (filters.value.flightId) params.push(`flightId=${filters.value.flightId}`)
  if (filters.value.status) params.push(`status=${filters.value.status}`)
  return params.length ? `?${params.join('&')}` : ''
}

const load = async () => {
  try {
    loading.value = true
    error.value = ''
    const url = getAirlineApiUrl(`tickets${buildQuery()}`)
    const res = await fetch(url)
    if (!res.ok) throw new Error(String(res.status))
    const data = await res.json()
    if (data && data.success && Array.isArray(data.tickets)) {
      tickets.value = data.tickets
    } else if (Array.isArray(data)) {
      tickets.value = data
    } else {
      error.value = data?.error || 'No se pudieron obtener las compras'
    }
  } catch (e: any) {
    error.value = e?.message || 'Error cargando compras'
  } finally {
    loading.value = false
  }
}

const reset = () => {
  filters.value = { userId: '', flightId: '', status: '' }
  load()
}

const exportCsv = () => {
  const headers = ['ticket','userId','flightNumber','origin','destination','seatNumber','seatCategory','status','paymentStatus','totalAmount','createdAt']
  const rows = tickets.value.map((t: any) => [t.idTicket,t.userId||'',t.flightNumber||'',t.originCity||'',t.destinationCity||'',t.seatNumber||'',t.seatCategory||'',t.status||'',t.paymentStatus||'',t.totalAmount||'',t.createdAt||t.bookingDate||''])
  const csv = [headers.join(','), ...rows.map(r=>r.map(v=>`"${String(v).replaceAll('"','""')}"`).join(','))].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'compras.csv'
  a.click()
  URL.revokeObjectURL(url)
}

const view = (t: any) => {
  modal.value = { visible: true, ticket: t }
}

const markPaid = async (t: any) => {
  try {
    const res = await fetch(getAirlineApiUrl(`tickets/${t.idTicket}/status`), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ paymentStatus: 'PAID' })
    })
    if (!res.ok) throw new Error(String(res.status))
    await load()
  } catch (e) {
    console.warn('No se pudo marcar pagado (revisar endpoint en backend):', e)
  }
}

const cancelTicket = async (t: any) => {
  try {
    const res = await fetch(getAirlineApiUrl(`tickets/${t.idTicket}/status`), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'CANCELLED' })
    })
    if (!res.ok) throw new Error(String(res.status))
    await load()
  } catch (e) {
    console.warn('No se pudo cancelar (revisar endpoint en backend):', e)
  }
}

const statusClass = (s?: string) => {
  const k = String(s||'').toUpperCase()
  if (k==='CONFIRMED') return 'ok'
  if (k==='RESERVED') return 'warn'
  if (k==='CANCELLED' || k==='REFUNDED') return 'danger'
  return ''
}
const paymentClass = (p?: string) => {
  const k = String(p||'').toUpperCase()
  if (k==='PAID') return 'ok'
  if (k==='PENDING') return 'warn'
  return ''
}

load()
</script>

<style scoped>
.admin-container { max-width: 1200px; margin: 2rem auto; padding: 0 1rem; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px; }
.header h2 { margin:0; }
.actions { display:flex; gap:8px; }
.filters { display:flex; gap:12px; flex-wrap:wrap; align-items:flex-end; margin-bottom:12px; }
.field { display:flex; flex-direction:column; gap:6px; }
.field input, .field select { padding:8px 10px; border:1px solid #e5e7eb; border-radius:8px; min-width:160px; }
.filter-actions { display:flex; gap:8px; }
.card { background:#fff; border:1px solid #e5e7eb; border-radius:12px; overflow:auto; }
.table { width:100%; border-collapse:collapse; }
.table th, .table td { padding:10px 12px; border-bottom:1px solid #edf2f7; text-align:left; }
.table thead th { background:#f8fafc; position:sticky; top:0; z-index:1; }
.badge { background:#eef2ff; color:#3730a3; padding:2px 8px; border-radius:999px; font-size:12px; }
.chip { padding:2px 8px; border-radius:999px; font-size:12px; border:1px solid transparent; }
.chip.ok { background:#ecfdf5; color:#065f46; border-color:#a7f3d0; }
.chip.warn { background:#fffbeb; color:#92400e; border-color:#fde68a; }
.chip.danger { background:#fef2f2; color:#991b1b; border-color:#fecaca; }
.row-actions { display:flex; gap:6px; }
.btn { background:#2563eb; color:#fff; border:none; padding:8px 12px; border-radius:8px; cursor:pointer; }
.btn-outline { background:#fff; color:#2563eb; border:1px solid #2563eb; padding:8px 12px; border-radius:8px; cursor:pointer; }
.btn-xs { background:#111827; color:#fff; border:none; padding:4px 8px; border-radius:6px; font-size:12px; cursor:pointer; }
.btn-xs.danger { background:#b91c1c; }
.loading { opacity:.7; }
.error { color:#b91c1c; margin-bottom:12px; }
.pre { background:#0f172a; color:#e2e8f0; padding:12px; border-radius:8px; overflow:auto; }
.modal-backdrop { position:fixed; inset:0; background:rgba(0,0,0,.35); display:flex; align-items:center; justify-content:center; }
.modal { width:640px; max-width:95vw; background:#fff; border-radius:12px; padding:16px; }
.modal-actions { display:flex; justify-content:flex-end; margin-top:12px; }
</style>
