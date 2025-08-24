<template>
  <div class="flight-type-selector">
    <div class="selector-header">
      <h4 class="selector-title">Tipo de Vuelo</h4>
      <p class="selector-subtitle">Elige el tipo de viaje que necesitas</p>
    </div>
    
    <div class="flight-types">
      <!-- Vuelo Solo Ida -->
      <div 
        class="flight-type-option"
        :class="{ 'selected': selectedType === 'one-way' }"
        @click="selectType('one-way')"
      >
        <div class="type-icon">✈️</div>
        <div class="type-content">
          <h5 class="type-title">Solo Ida</h5>
          <p class="type-description">Un viaje de ida sin regreso</p>
          <div class="type-features">
            <span class="feature">• Solo fecha de salida</span>
            <span class="feature">• Ideal para negocios</span>
            <span class="feature">• Precio más económico</span>
          </div>
        </div>
        <div class="type-radio">
          <div class="radio-circle" :class="{ 'checked': selectedType === 'one-way' }"></div>
        </div>
      </div>

      <!-- Vuelo Solo Regreso -->
      <div 
        class="flight-type-option"
        :class="{ 'selected': selectedType === 'return-only' }"
        @click="selectType('return-only')"
      >
        <div class="type-icon">🏠</div>
        <div class="type-content">
          <h5 class="type-title">Solo Regreso</h5>
          <p class="type-description">Un viaje de regreso sin ida</p>
          <div class="type-features">
            <span class="feature">• Solo fecha de regreso</span>
            <span class="feature">• Para viajeros en destino</span>
            <span class="feature">• Flexibilidad de fechas</span>
          </div>
        </div>
        <div class="type-radio">
          <div class="radio-circle" :class="{ 'checked': selectedType === 'return-only' }"></div>
        </div>
      </div>

      <!-- Vuelo Redondo -->
      <div 
        class="flight-type-option"
        :class="{ 'selected': selectedType === 'round-trip' }"
        @click="selectType('round-trip')"
      >
        <div class="type-icon">🔄</div>
        <div class="type-content">
          <h5 class="type-title">Vuelo Redondo</h5>
          <p class="type-description">Viaje de ida y vuelta completo</p>
          <div class="type-features">
            <span class="feature">• Fechas de ida y regreso</span>
            <span class="feature">• Mejor para turismo</span>
            <span class="feature">• Descuentos por paquete</span>
          </div>
        </div>
        <div class="type-radio">
          <div class="radio-circle" :class="{ 'checked': selectedType === 'round-trip' }"></div>
        </div>
      </div>
    </div>

    <!-- Información adicional según el tipo seleccionado -->
    <div v-if="selectedType" class="type-info">
      <div class="info-header">
        <span class="info-icon">ℹ️</span>
        <span class="info-title">Información del {{ getTypeDisplayName() }}</span>
      </div>
      <div class="info-content">
        <p>{{ getTypeDescription() }}</p>
        <ul class="info-list">
          <li v-for="tip in getTypeTips()" :key="tip">{{ tip }}</li>
        </ul>
      </div>
    </div>
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

const getTypeDisplayName = () => {
  switch (selectedType.value) {
    case 'one-way': return 'Vuelo Solo Ida'
    case 'return-only': return 'Vuelo Solo Regreso'
    case 'round-trip': return 'Vuelo Redondo'
    default: return 'Tipo de Vuelo'
  }
}

const getTypeDescription = () => {
  switch (selectedType.value) {
    case 'one-way':
      return 'Perfecto para viajes de negocios, migración o cuando ya tienes planificado tu regreso por otros medios.'
    case 'return-only':
      return 'Ideal si ya estás en tu destino y solo necesitas regresar, o si tienes un viaje de ida separado.'
    case 'round-trip':
      return 'La opción más popular para turismo, vacaciones familiares y viajes de placer con fechas definidas.'
    default:
      return ''
  }
}

const getTypeTips = () => {
  switch (selectedType.value) {
    case 'one-way':
      return [
        'Considera el costo de tu regreso por separado',
        'Puede ser más económico que un redondo',
        'Flexibilidad total en la fecha de regreso'
      ]
    case 'return-only':
      return [
        'Asegúrate de tener tu vuelo de ida confirmado',
        'Puedes elegir cualquier fecha de regreso disponible',
        'Ideal para viajeros frecuentes'
      ]
    case 'round-trip':
      return [
        'Generalmente más económico que comprar por separado',
        'Mejor para planificar vacaciones completas',
        'Descuentos especiales por paquete'
      ]
    default:
      return []
  }
}

// Watchers
watch(() => props.modelValue, (newValue) => {
  selectedType.value = newValue
}, { immediate: true })
</script>

<style scoped>
.flight-type-selector {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.selector-header {
  text-align: center;
  margin-bottom: 24px;
}

.selector-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.selector-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.flight-types {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.flight-type-option {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f8fafc;
}

.flight-type-option:hover {
  border-color: #cbd5e1;
  background: #f1f5f9;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.flight-type-option.selected {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.type-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.type-content {
  flex: 1;
}

.type-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.type-description {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.type-features {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feature {
  font-size: 12px;
  color: #475569;
  font-weight: 500;
}

.type-radio {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  transition: all 0.2s ease;
  position: relative;
}

.radio-circle.checked {
  border-color: #3b82f6;
  background: #3b82f6;
}

.radio-circle.checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.type-info {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.info-icon {
  font-size: 18px;
}

.info-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.info-content p {
  font-size: 14px;
  color: #475569;
  line-height: 1.5;
  margin: 0 0 16px 0;
}

.info-list {
  margin: 0;
  padding-left: 20px;
}

.info-list li {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
  line-height: 1.4;
}

.info-list li:last-child {
  margin-bottom: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .flight-type-selector {
    padding: 16px;
  }
  
  .flight-type-option {
    padding: 16px;
    gap: 12px;
  }
  
  .type-icon {
    font-size: 24px;
  }
  
  .type-title {
    font-size: 16px;
  }
  
  .type-description {
    font-size: 13px;
  }
  
  .feature {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .flight-types {
    gap: 12px;
  }
  
  .flight-type-option {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .type-radio {
    align-self: center;
  }
}
</style>
