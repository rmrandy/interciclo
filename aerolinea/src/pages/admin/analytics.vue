<template>
  <section class="section" style="max-width: 1100px">
    <h2>Analítica de Clics</h2>
    <div class="card grid" style="gap:12px">
      <div class="grid-3">
        <label class="field"><span class="label">Desde (ISO)</span><input class="input" v-model="from" placeholder="2025-09-14T00:00:00Z"/></label>
        <label class="field"><span class="label">Hasta (ISO)</span><input class="input" v-model="to" placeholder="2025-09-15T00:00:00Z"/></label>
        <label class="field"><span class="label">Usuario (ID)</span><input class="input" v-model.number="userId" type="number" min="1"/></label>
      </div>
      <div class="grid-3">
        <label class="field"><span class="label">Path contiene</span><input class="input" v-model="pathLike" placeholder="/admin"/></label>
        <label class="field"><span class="label">Límite</span><input class="input" v-model.number="limit" type="number" min="1"/></label>
      </div>
      <div style="display:flex; gap:8px">
        <button class="btn btn-primary" @click="load">Buscar</button>
        <button class="btn" @click="downloadCsv">Descargar CSV</button>
      </div>
    </div>

    <div class="card" style="overflow:auto">
      <table style="width:100%; border-collapse:collapse">
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
          <tr v-for="e in events" :key="e.idClick" style="border-top:1px solid #e3e8f3">
            <td>{{ e.eventTime }}</td>
            <td>{{ e.userId ?? '' }}</td>
            <td>{{ e.urlPath }}</td>
            <td>{{ e.elementTag }}</td>
            <td>{{ e.elementIdAttr }}</td>
            <td>{{ e.elementClasses }}</td>
            <td>{{ e.textSnippet }}</td>
          </tr>
        </tbody>
      </table>
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

const base = `${window.location.protocol}//${window.location.hostname}:8080/api/analytics`

async function load() {
  const qs = new URLSearchParams()
  if (from.value) qs.set('from', from.value)
  if (to.value) qs.set('to', to.value)
  if (userId.value) qs.set('userId', String(userId.value))
  if (pathLike.value) qs.set('pathLike', `%${pathLike.value}%`)
  if (limit.value) qs.set('limit', String(limit.value))
  const r = await fetch(`${base}/events?${qs.toString()}`)
  const j = await r.json()
  events.value = j.events || []
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
.btn { padding:8px 12px; border:1px solid #d0d7de; border-radius:6px; background:#fff }
.btn-primary { background:#2da44e; color:#fff; border-color:#2da44e }
.field .label { display:block; font-size:12px; color:#57606a; margin-bottom:4px }
.input { padding:8px 10px; border:1px solid #d0d7de; border-radius:6px; }
</style>





