<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { getInsuranceApiUrl } from '../../utils/api'

const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')

const form = ref({
  title: 'AeroLinea',
  subtitle: 'Tu compañía de seguros aeronáuticos',
  footer: '© 2025 AeroLinea. Todos los derechos reservados.',
  logoUrl: ''
})

const loadSettings = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await axios.get(getInsuranceApiUrl('/site-settings'))
    form.value.title = data['brand.title'] || form.value.title
    form.value.subtitle = data['brand.subtitle'] || form.value.subtitle
    form.value.footer = data['footer.text'] || form.value.footer
    form.value.logoUrl = data['brand.logoUrl'] || ''
  } catch (e: any) {
    error.value = 'No se pudieron cargar los ajustes del sitio'
  } finally {
    loading.value = false
  }
}

const save = async () => {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    const payload = {
      'brand.title': form.value.title,
      'brand.subtitle': form.value.subtitle,
      'footer.text': form.value.footer,
      'brand.logoUrl': form.value.logoUrl
    }
    await axios.put(getInsuranceApiUrl('/site-settings'), payload)
    success.value = 'Ajustes guardados'
    setTimeout(() => success.value = '', 2500)
  } catch (e: any) {
    error.value = 'No se pudieron guardar los cambios'
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>

<template>
  <div class="airline-card">
    <h1 class="airline-subtitle">Ajustes del Sitio (Header/Footer)</h1>

    <div v-if="error" class="airline-card error-card">{{ error }}</div>
    <div v-if="success" class="airline-card" style="background:#f0fdf4;border-color:#bbf7d0">{{ success }}</div>

    <div v-if="loading">Cargando...</div>
    <div v-else class="grid-container">
      <div class="airline-form-field">
        <label class="airline-form-label">Título</label>
        <input v-model="form.title" class="airline-form-input" placeholder="AeroLinea" />
      </div>
      <div class="airline-form-field">
        <label class="airline-form-label">Subtítulo</label>
        <input v-model="form.subtitle" class="airline-form-input" placeholder="Tu compañía de seguros aeronáuticos" />
      </div>
      <div class="airline-form-field" style="grid-column: 1 / -1;">
        <label class="airline-form-label">Texto de Footer</label>
        <input v-model="form.footer" class="airline-form-input" />
      </div>
      <div class="airline-form-field" style="grid-column: 1 / -1;">
        <label class="airline-form-label">URL del Logo (opcional)</label>
        <input v-model="form.logoUrl" class="airline-form-input" placeholder="http://IP:PUERTO/logo.png" />
        <div v-if="form.logoUrl" class="info-text">Vista previa:</div>
        <img v-if="form.logoUrl" :src="form.logoUrl" alt="logo" style="width:56px;height:56px;border-radius:50%;margin-top:6px;object-fit:cover;"/>
      </div>
    </div>

    <div class="action-buttons">
      <button class="btn-airline-secondary" @click="loadSettings" :disabled="loading">Recargar</button>
      <button class="btn-airline-primary" @click="save" :disabled="saving">{{ saving ? 'Guardando...' : 'Guardar Cambios' }}</button>
    </div>
  </div>
</template>

<style scoped>
@import '../../style.css';
</style>



