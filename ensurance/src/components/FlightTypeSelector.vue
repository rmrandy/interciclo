<template>
  <div class="flight-type-toggle" role="group" aria-label="Tipo de Vuelo">
    <button 
      class="toggle-btn" 
      :class="{ active: selectedType === 'round-trip' }" 
      type="button"
      @click="selectType('round-trip')"
    >
      <span class="dot" v-if="selectedType === 'round-trip'"></span>
      Ida y vuelta
    </button>
    <button 
      class="toggle-btn" 
      :class="{ active: selectedType === 'one-way' }" 
      type="button"
      @click="selectType('one-way')"
    >
      <span class="dot" v-if="selectedType === 'one-way'"></span>
      Solo ida
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  modelValue?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: 'round-trip'
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'type-changed': [type: string]
}>()

// Estado del tipo seleccionado
const selectedType = ref(props.modelValue)

// Métodos
const selectType = (type: string) => {
  selectedType.value = type
  emit('update:modelValue', type)
  emit('type-changed', type)
}

// Mantengo API pública por compatibilidad; no se muestran descripciones en el nuevo diseño
const getTypeDisplayName = () => (selectedType.value === 'one-way' ? 'Solo Ida' : 'Vuelo Redondo')
const getTypeDescription = () => ''
const getTypeTips = () => []

// Watchers
watch(() => props.modelValue, (newValue) => {
  selectedType.value = newValue
}, { immediate: true })
</script>

<style scoped>
.flight-type-toggle {
  display: inline-flex;
  background: #e6f0ff;
  border-radius: 9999px;
  padding: 6px;
  gap: 6px;
}

.toggle-btn {
  border: none;
  background: transparent;
  padding: 8px 14px;
  border-radius: 9999px;
  font-weight: 700;
  color: #1f2937;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.toggle-btn.active {
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 9999px;
  background: #10b981; /* verde */
  box-shadow: inset 0 0 0 2px #fff;
}
</style>
