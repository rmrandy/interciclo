<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { airlineApi } from '../../utils/airlineApi'

const loading = ref(false)
const error = ref('')
const success = ref('')
const aircrafts = ref<any[]>([])

const editingId = ref<number | null>(null)
const editForm = ref({
  registration: '',
  model: '',
  manufacturer: '',
  seatCapacity: 0,
})

const form = ref({
  registration: '',
  model: '',
  manufacturer: '',
  seatCapacity: 0,
})

const loadAircrafts = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await airlineApi.getAircrafts()
    aircrafts.value = Array.isArray(res) ? res : (res || [])
  } catch (e: any) {
    error.value = e?.message || 'Error cargando aeronaves'
  } finally {
    loading.value = false
  }
}

const createAircraft = async () => {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const res = await airlineApi.createAircraft({
      registration: form.value.registration,
      model: form.value.model,
      manufacturer: form.value.manufacturer,
      seatCapacity: Number(form.value.seatCapacity),
    })
    if (res?.success === false) {
      error.value = res.error || 'Error creando aeronave'
      return
    }
    success.value = 'Aeronave creada'
    form.value = { registration: '', model: '', manufacturer: '', seatCapacity: 0 }
    await loadAircrafts()
  } catch (e: any) {
    error.value = e?.message || 'Error creando aeronave'
  } finally {
    loading.value = false
  }
}

const deleteAircraft = async (id: number) => {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const res = await airlineApi.deleteAircraft(id)
    if (res?.success === false) {
      error.value = res.error || 'No se pudo eliminar'
      return
    }
    success.value = 'Aeronave eliminada'
    await loadAircrafts()
  } catch (e: any) {
    error.value = e?.message || 'Error eliminando aeronave'
  } finally {
    loading.value = false
  }
}

onMounted(loadAircrafts)

const startEdit = (a: any) => {
  editingId.value = a.idAircraft
  editForm.value = {
    registration: a.registration || '',
    model: a.model || '',
    manufacturer: a.manufacturer || '',
    seatCapacity: Number(a.seatCapacity) || 0,
  }
}

const cancelEdit = () => {
  editingId.value = null
  editForm.value = { registration: '', model: '', manufacturer: '', seatCapacity: 0 }
}

const saveEdit = async () => {
  if (!editingId.value) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const payload = {
      registration: editForm.value.registration,
      model: editForm.value.model,
      manufacturer: editForm.value.manufacturer,
      seatCapacity: Number(editForm.value.seatCapacity),
    }
    const res = await airlineApi.updateAircraft(editingId.value, payload)
    if (res?.success === false) {
      error.value = res.error || 'No se pudo actualizar'
      return
    }
    success.value = 'Aeronave actualizada'
    await loadAircrafts()
    cancelEdit()
  } catch (e: any) {
    error.value = e?.message || 'Error actualizando aeronave'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="airline-card">
    <h1 class="airline-subtitle">Gestión de Aeronaves</h1>

    <div v-if="error" class="airline-card error-card">{{ error }}</div>
    <div v-if="success" class="airline-card" style="background:#f0fdf4;border-color:#bbf7d0">{{ success }}</div>

    <div class="grid-container">
      <div class="airline-form-field">
        <label class="airline-form-label">Matrícula</label>
        <input v-model="form.registration" class="airline-form-input" placeholder="XA-A320-01" />
      </div>
      <div class="airline-form-field">
        <label class="airline-form-label">Modelo</label>
        <input v-model="form.model" class="airline-form-input" placeholder="A320" />
      </div>
      <div class="airline-form-field">
        <label class="airline-form-label">Fabricante</label>
        <input v-model="form.manufacturer" class="airline-form-input" placeholder="AIRBUS" />
      </div>
      <div class="airline-form-field">
        <label class="airline-form-label">Capacidad</label>
        <input v-model="form.seatCapacity" type="number" min="0" class="airline-form-input" placeholder="186" />
      </div>
    </div>
    <div class="action-buttons">
      <button @click="createAircraft" :disabled="loading" class="btn-airline-primary">
        {{ loading ? 'Guardando...' : 'Crear Aeronave' }}
      </button>
    </div>

    <div class="section-divider">
      <h3 class="section-title">Aeronaves</h3>
      <div v-if="loading">Cargando...</div>
      <div v-else-if="aircrafts.length === 0">Sin aeronaves</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Matrícula</th>
            <th>Modelo</th>
            <th>Fabricante</th>
            <th>Capacidad</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in aircrafts" :key="a.idAircraft">
            <td>{{ a.idAircraft }}</td>
            <td>
              <template v-if="editingId === a.idAircraft">
                <input v-model="editForm.registration" class="airline-form-input" />
              </template>
              <template v-else>
                {{ a.registration }}
              </template>
            </td>
            <td>
              <template v-if="editingId === a.idAircraft">
                <input v-model="editForm.model" class="airline-form-input" />
              </template>
              <template v-else>
                {{ a.model }}
              </template>
            </td>
            <td>
              <template v-if="editingId === a.idAircraft">
                <input v-model="editForm.manufacturer" class="airline-form-input" />
              </template>
              <template v-else>
                {{ a.manufacturer }}
              </template>
            </td>
            <td>
              <template v-if="editingId === a.idAircraft">
                <input v-model.number="editForm.seatCapacity" type="number" min="0" class="airline-form-input" />
              </template>
              <template v-else>
                {{ a.seatCapacity }}
              </template>
            </td>
            <td>
              <template v-if="editingId === a.idAircraft">
                <button class="status-btn" :disabled="loading" @click="saveEdit">Guardar</button>
                <button class="status-btn delete-btn" :disabled="loading" @click="cancelEdit">Cancelar</button>
              </template>
              <template v-else>
                <button class="status-btn" :disabled="loading" @click="startEdit(a)">Editar</button>
                <button class="status-btn delete-btn" :disabled="loading" @click="deleteAircraft(a.idAircraft)">Eliminar</button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
@import '../../style.css';

/* Header refinado */
.airline-subtitle {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

/* Form responsive */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.airline-form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.airline-form-input {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  outline: none;
  transition: box-shadow .15s ease, border-color .15s ease;
}

.airline-form-input:focus {
  border-color: #93c5fd;
  box-shadow: 0 0 0 4px rgba(59,130,246,.15);
}

.action-buttons {
  margin: 12px 0 18px;
}

.btn-airline-primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  color: #fff;
  padding: 10px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: transform .1s ease, box-shadow .2s ease, opacity .2s ease;
}

.btn-airline-primary:hover { box-shadow: 0 8px 20px rgba(37,99,235,.2) }
.btn-airline-primary:active { transform: translateY(1px) }
.btn-airline-primary:disabled { opacity: .6; cursor: not-allowed }

/* Tabla con cabecera sticky y zebra */
.data-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.data-table thead th {
  position: sticky;
  top: 0;
  background: #f9fafb;
  color: #374151;
  font-weight: 600;
  text-align: left;
  padding: 12px 14px;
  border-bottom: 1px solid #e5e7eb;
}

.data-table tbody td { padding: 12px 14px; border-bottom: 1px solid #f3f4f6 }
.data-table tbody tr:nth-child(odd) { background: #fcfdff }
.data-table tbody tr:hover { background: #f5f9ff }

/* Botones de fila */
.status-btn {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #374151;
  cursor: pointer;
  transition: background .15s ease, box-shadow .15s ease, transform .05s ease;
  margin-right: 8px;
}
.status-btn:hover { background: #f9fafb; box-shadow: 0 2px 8px rgba(0,0,0,.05) }
.status-btn:active { transform: translateY(1px) }
.delete-btn { border-color: #fecaca; color: #b91c1c }
.delete-btn:hover { background: #fff1f2 }

.section-divider { margin-top: 18px }
.section-title { margin-bottom: 10px }

@media (max-width: 640px) {
  .airline-form-input { font-size: 14px }
  .btn-airline-primary { width: 100% }
}
</style>



