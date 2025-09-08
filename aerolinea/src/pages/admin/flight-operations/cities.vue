<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { airlineApi } from '../../../utils/airlineApi'

const cities = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const name = ref('')
const country = ref('')

const loadCities = async () => {
  loading.value = true
  error.value = ''
  try {
    cities.value = await airlineApi.getCities()
  } catch (e: any) {
    error.value = 'Error cargando ciudades'
  } finally {
    loading.value = false
  }
}

const createCity = async () => {
  if (!name.value || !country.value) {
    error.value = 'Nombre y país son obligatorios'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await airlineApi.createCity({ name: name.value, country: country.value })
    name.value = ''
    country.value = ''
    await loadCities()
  } catch (e: any) {
    error.value = e?.message || 'Error creando ciudad'
  } finally {
    loading.value = false
  }
}

onMounted(loadCities)
</script>

<template>
  <div class="space-y-6">
    <h1 class="text-2xl font-semibold">Ciudades</h1>

    <div class="airline-card p-4 space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <input v-model="name" class="airline-form-input" placeholder="Nombre de la ciudad" />
        <input v-model="country" class="airline-form-input" placeholder="País" />
      </div>
      <button class="btn-airline-primary" @click="createCity" :disabled="loading">Agregar</button>
      <p v-if="error" class="text-red-600 text-sm">{{ error }}</p>
    </div>

    <div class="airline-card p-4">
      <table class="w-full">
        <thead>
          <tr class="text-left">
            <th class="p-2">ID</th>
            <th class="p-2">Nombre</th>
            <th class="p-2">País</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in cities" :key="c.idCity" class="border-t">
            <td class="p-2">{{ c.idCity }}</td>
            <td class="p-2">{{ c.name }}</td>
            <td class="p-2">{{ c.country }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped></style>



