<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { airlineApi } from '../../utils/airlineApi'

const loading = ref(false)
const error = ref('')
const success = ref('')
const aircrafts = ref<any[]>([])
const selectedAircraftId = ref<number | ''>('')

const cfg = ref<Record<string, { seats: number; priceMultiplier: number }>>({
  ECONOMY: { seats: 0, priceMultiplier: 1.0 },
  BUSINESS: { seats: 0, priceMultiplier: 2.0 },
  FIRST_CLASS: { seats: 0, priceMultiplier: 3.0 },
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

const loadSeatConfig = async () => {
  if (!selectedAircraftId.value) return
  loading.value = true
  error.value = ''
  try {
    const res = await airlineApi.getSeatConfig(Number(selectedAircraftId.value))
    if (res?.success && res.config) {
      cfg.value = {
        ECONOMY: { seats: res.config.ECONOMY?.seats || 0, priceMultiplier: res.config.ECONOMY?.priceMultiplier || 1.0 },
        BUSINESS: { seats: res.config.BUSINESS?.seats || 0, priceMultiplier: res.config.BUSINESS?.priceMultiplier || 2.0 },
        FIRST_CLASS: { seats: res.config.FIRST_CLASS?.seats || 0, priceMultiplier: res.config.FIRST_CLASS?.priceMultiplier || 3.0 },
      }
    } else {
      cfg.value = { ECONOMY: { seats: 0, priceMultiplier: 1.0 }, BUSINESS: { seats: 0, priceMultiplier: 2.0 }, FIRST_CLASS: { seats: 0, priceMultiplier: 3.0 } }
    }
  } catch (e: any) {
    error.value = e?.message || 'Error cargando configuración'
  } finally {
    loading.value = false
  }
}

watch(selectedAircraftId, loadSeatConfig)

const save = async () => {
  if (!selectedAircraftId.value) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const res = await airlineApi.updateSeatConfig(Number(selectedAircraftId.value), cfg.value)
    if (res?.success === false) {
      error.value = res.error || 'No se pudo guardar'
      return
    }
    success.value = 'Configuración guardada'
  } catch (e: any) {
    error.value = e?.message || 'Error guardando cambios'
  } finally {
    loading.value = false
  }
}

onMounted(loadAircrafts)
</script>

<template>
  <div class="airline-card">
    <h1 class="airline-subtitle">Configuración de Asientos por Aeronave</h1>
    <div v-if="error" class="airline-card error-card">{{ error }}</div>
    <div v-if="success" class="airline-card" style="background:#f0fdf4;border-color:#bbf7d0">{{ success }}</div>

    <div class="airline-form-field">
      <label class="airline-form-label">Aeronave</label>
      <select v-model="selectedAircraftId" class="airline-form-input">
        <option value="">Seleccionar aeronave</option>
        <option v-for="a in aircrafts" :key="a.idAircraft" :value="a.idAircraft">
          {{ a.registration }} — {{ a.model }} ({{ a.seatCapacity }} asientos)
        </option>
      </select>
    </div>

    <div v-if="selectedAircraftId" class="section-divider">
      <h3 class="section-title">Categorías</h3>
      <div class="grid-container">
        <div class="airline-form-field" v-for="cat in ['ECONOMY','BUSINESS','FIRST_CLASS']" :key="cat">
          <label class="airline-form-label">{{ cat }}</label>
          <div class="grid-container">
            <div class="airline-form-field">
              <label class="airline-form-label">Asientos</label>
              <input v-model.number="cfg[cat].seats" type="number" min="0" class="airline-form-input" />
            </div>
            <div class="airline-form-field">
              <label class="airline-form-label">Multiplicador Precio</label>
              <input v-model.number="cfg[cat].priceMultiplier" type="number" step="0.01" min="0.01" class="airline-form-input" />
            </div>
          </div>
        </div>
      </div>
      <div class="action-buttons">
        <button class="btn-airline-primary" :disabled="loading" @click="save">{{ loading ? 'Guardando...' : 'Guardar Configuración' }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import '../../style.css';

.airline-subtitle {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.airline-form-field { display: flex; flex-direction: column; gap: 6px }
.airline-form-input {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  outline: none;
  transition: box-shadow .15s ease, border-color .15s ease;
}
.airline-form-input:focus { border-color: #93c5fd; box-shadow: 0 0 0 4px rgba(59,130,246,.15) }

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 14px;
}

.section-title { margin-top: 10px; margin-bottom: 8px }

.btn-airline-primary {
  background: linear-gradient(135deg, #059669, #047857);
  border: none;
  color: #fff;
  padding: 10px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: transform .1s ease, box-shadow .2s ease, opacity .2s ease;
}
.btn-airline-primary:hover { box-shadow: 0 8px 20px rgba(5,150,105,.18) }
.btn-airline-primary:active { transform: translateY(1px) }
.btn-airline-primary:disabled { opacity: .6; cursor: not-allowed }

@media (max-width: 640px) {
  .btn-airline-primary { width: 100% }
}
</style>



