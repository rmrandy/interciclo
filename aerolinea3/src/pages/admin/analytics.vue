<template>
  <section class="section" style="max-width: 1100px">
    <h2 style="font-size:20px; font-weight:700; margin-bottom:12px">Analítica de Clics</h2>

    <!-- Filtros -->
    <div class="card grid" style="gap:12px">
      <div class="grid-3">
        <label class="field">
          <span class="label">Desde (ISO)</span>
          <input class="input" v-model="from" placeholder="2025-09-14T00:00:00Z"/>
        </label>
        <label class="field">
          <span class="label">Hasta (ISO)</span>
          <input class="input" v-model="to" placeholder="2025-09-15T00:00:00Z"/>
        </label>
        <label class="field">
          <span class="label">Usuario (ID)</span>
          <input class="input" v-model.number="userId" type="number" min="1"/>
        </label>
      </div>

      <div class="grid-3">
        <label class="field">
          <span class="label">Path contiene</span>
          <input class="input" v-model="pathLike" placeholder="/admin"/>
        </label>
        <label class="field">
          <span class="label">Límite</span>
          <input class="input" v-model.number="limit" type="number" min="1"/>
        </label>
        <div class="quick-range">
          <span class="label">Rangos rápidos</span>
          <div class="chips">
            <button class="chip" @click="setRange('2h')">Últimas 2 h</button>
            <button class="chip" @click="setRange('today')">Hoy</button>
            <button class="chip" @click="setRange('24h')">24 h</button>
          </div>
        </div>
      </div>

      <div style="display:flex; gap:8px; align-items:center">
        <button class="btn btn-primary" @click="load" :disabled="loading">
          <span v-if="!loading">Buscar</span>
          <span v-else class="spinner" aria-label="Cargando"></span>
        </button>
        <button class="btn" @click="downloadCsv" :disabled="loading || events.length===0">Descargar CSV</button>
        <div class="hint" v-if="events.length">{{ events.length }} resultados</div>
      </div>

      <div v-if="errorMsg" class="alert">
        {{ errorMsg }}
      </div>
    </div>

    <!-- Resultados -->
    <div class="card table-wrap">
      <template v-if="loading">
        <div class="loading-row">
          <span class="spinner"></span>
          <span>Cargando eventos…</span>
        </div>
      </template>
      <template v-else-if="!events.length">
        <div class="empty">
          <div>Sin resultados. Ajusta los filtros o el rango de fechas.</div>
        </div>
      </template>
      <template v-else>
        <table class="table">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>User</th>
              <th>Path</th>
              <th>Tag</th>
              <th>ID</th>
              <th>Clases</th>
              <th>Texto</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in events" :key="e.idClick">
              <td class="mono">{{ e.eventTime }}</td>
              <td class="mono">{{ e.userId ?? '' }}</td>
              <td class="ellipsis" :title="e.urlPath">{{ e.urlPath }}</td>
              <td class="mono">{{ e.elementTag }}</td>
              <td class="ellipsis" :title="e.elementIdAttr">{{ e.elementIdAttr }}</td>
              <td class="ellipsis" :title="e.elementClasses">{{ e.elementClasses }}</td>
              <td class="ellipsis" :title="e.textSnippet">{{ e.textSnippet }}</td>
            </tr>
          </tbody>
        </table>
      </template>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const from = ref('')
const to = ref('')
const userId = ref<number | null>(null)
const pathLike = ref('')
const limit = ref(500)
const events = ref<any[]>([])
const loading = ref(false)
const errorMsg = ref('')

const base = `${window.location.protocol}//${window.location.hostname}:6060/api/analytics`

function setRange(preset: '2h' | 'today' | '24h') {
  const now = new Date()
  if (preset === '2h') {
    to.value = now.toISOString()
    from.value = new Date(now.getTime() - 2 * 60 * 60 * 1000).toISOString()
  } else if (preset === 'today') {
    const start = new Date(now)
    start.setUTCHours(0, 0, 0, 0)
    from.value = start.toISOString()
    to.value = now.toISOString()
  } else if (preset === '24h') {
    to.value = now.toISOString()
    from.value = new Date(now.getTime() - 24 * 60 * 60 * 1000).toISOString()
  }
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const qs = new URLSearchParams()
    if (from.value) qs.set('from', from.value)
    if (to.value) qs.set('to', to.value)
    if (userId.value) qs.set('userId', String(userId.value))
    if (pathLike.value) qs.set('pathLike', `%${pathLike.value}%`)
    if (limit.value) qs.set('limit', String(limit.value))
    const r = await fetch(`${base}/events?${qs.toString()}`)
    const j = await r.json()
    events.value = j.events || []
  } catch (err: any) {
    errorMsg.value = 'No se pudo cargar la información.'
  } finally {
    loading.value = false
  }
}

function downloadCsv() {
  const qs = new URLSearchParams()
  if (from.value) qs.set('from', from.value)
  if (to.value) qs.set('to', to.value)
  if (userId.value) qs.set('userId', String(userId.value))
  if (pathLike.value) qs.set('pathLike', `%${pathLike.value}%`)
  if (limit.value) qs.set('limit', String(limit.value))
  window.open(`${base}/events.csv?${qs.toString()}`, '_blank')
}
</script>

<style scoped>
.grid { display:grid; gap: 12px; }
.grid-3 { display:grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px; }
.card { padding:16px; background:white; border-radius:8px; box-shadow:0 1px 3px rgba(0,0,0,.08); margin-bottom:16px }
.btn { padding:8px 12px; border:1px solid #d0d7de; border-radius:6px; background:#fff; cursor:pointer }
.btn:disabled { opacity:.6; cursor:not-allowed }
.btn-primary { background:#2da44e; color:#fff; border-color:#2da44e }
.field .label { display:block; font-size:12px; color:#57606a; margin-bottom:4px }
.input { padding:8px 10px; border:1px solid #d0d7de; border-radius:6px; width:100% }
.hint { font-size:12px; color:#57606a }
.alert { padding:10px 12px; background:#fff2f2; border:1px solid #ffd1d1; color:#b42318; border-radius:6px }

.quick-range .label { display:block; font-size:12px; color:#57606a; margin-bottom:6px }
.chips { display:flex; gap:8px; flex-wrap:wrap }
.chip { padding:6px 10px; background:#f3f4f6; border:1px solid #e5e7eb; border-radius:999px; font-size:12px; cursor:pointer }
.chip:hover { background:#e5e7eb }

.table-wrap { overflow:auto; }
.table { width:100%; border-collapse:separate; border-spacing:0 }
.table thead th { position:sticky; top:0; background:#f8fafc; z-index:1; text-align:left; font-weight:600; font-size:12px; color:#475569; border-bottom:1px solid #e2e8f0; padding:10px }
.table td { padding:10px; font-size:13px; border-bottom:1px solid #f1f5f9; vertical-align:top }
.table tbody tr:nth-child(odd) { background:#fcfdff }

.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; font-size:12px }
.ellipsis { max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }

.loading-row { display:flex; align-items:center; gap:10px; padding:16px; color:#475569 }
.empty { padding:24px; text-align:center; color:#64748b }
.spinner { width:16px; height:16px; border:2px solid #cbd5e1; border-top-color:#2da44e; border-radius:50%; display:inline-block; animation:spin 1s linear infinite }
@keyframes spin { to { transform: rotate(360deg) } }

@media (max-width: 920px) {
  .grid-3 { grid-template-columns: 1fr; }
  .ellipsis { max-width: 160px; }
}
</style>





