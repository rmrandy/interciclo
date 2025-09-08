<template>
  <div class="credit-card-input">
    <div class="input-group">
      <label :for="id" class="input-label">{{ label }}</label>
      <div class="input-wrapper">
        <input
          :id="id"
          :type="inputType"
          :value="displayValue"
          @input="handleInput"
          @focus="handleFocus"
          @blur="handleBlur"
          @keydown="handleKeydown"
          :placeholder="placeholder"
          :class="['form-input', { 'error': hasError }]"
          :maxlength="maxLength"
          autocomplete="off"
        />
        <div v-if="showCardIcon" class="card-icon">
          {{ getCardIcon() }}
        </div>
      </div>
      <div v-if="hasError" class="error-message">{{ errorMessage }}</div>
      <div v-if="showHint" class="input-hint">{{ hint }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Props {
  modelValue?: string
  label: string
  type: 'cardNumber' | 'expiry' | 'cvv' | 'cardholderName'
  placeholder?: string
  errorMessage?: string
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '',
  errorMessage: '',
  hint: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'card-type-detected': [cardType: string]
}>()

// Estado local
const isFocused = ref(false)
const cardType = ref('')

// Computed properties
const id = computed(() => `credit-card-${props.type}`)

const inputType = computed(() => {
  if (props.type === 'cvv') return 'password'
  return 'text'
})

const maxLength = computed(() => {
  switch (props.type) {
    case 'cardNumber': return 19 // 16 dígitos + 3 espacios
    case 'expiry': return 5 // MM/YY
    case 'cvv': return 4 // 3-4 dígitos
    case 'cardholderName': return 50
    default: return 50
  }
})

const displayValue = computed(() => {
  if (!props.modelValue) return ''
  
  switch (props.type) {
    case 'cardNumber':
      return formatCardNumber(props.modelValue)
    case 'expiry':
      return formatExpiry(props.modelValue)
    case 'cvv':
      return props.modelValue
    case 'cardholderName':
      return props.modelValue.toUpperCase()
    default:
      return props.modelValue
  }
})

const showCardIcon = computed(() => {
  return props.type === 'cardNumber' && props.modelValue && props.modelValue.length > 0
})

const showHint = computed(() => {
  return props.hint && (isFocused.value || !props.modelValue)
})

const hasError = computed(() => {
  return !!props.errorMessage
})

// Métodos
const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  let value = target.value
  
  // Limpiar caracteres no permitidos según el tipo
  switch (props.type) {
    case 'cardNumber':
      value = value.replace(/\D/g, '').substring(0, 16)
      break
    case 'expiry':
      value = value.replace(/\D/g, '').substring(0, 4)
      break
    case 'cvv':
      value = value.replace(/\D/g, '').substring(0, 4)
      break
    case 'cardholderName':
      value = value.replace(/[^a-zA-Z\s]/g, '')
      break
  }
  
  emit('update:modelValue', value)
  
  // Detectar tipo de tarjeta
  if (props.type === 'cardNumber') {
    const detectedType = detectCardType(value)
    if (detectedType !== cardType.value) {
      cardType.value = detectedType
      emit('card-type-detected', detectedType)
    }
  }
}

const handleFocus = () => {
  isFocused.value = true
}

const handleBlur = () => {
  isFocused.value = false
}

const handleKeydown = (event: KeyboardEvent) => {
  // Permitir teclas de navegación
  if (['Backspace', 'Delete', 'Tab', 'Escape', 'Enter', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].includes(event.key)) {
    return
  }
  
  // Validar entrada según el tipo
  const key = event.key
  let isValid = true
  
  switch (props.type) {
    case 'cardNumber':
      isValid = /\d/.test(key)
      break
    case 'expiry':
      isValid = /\d/.test(key)
      break
    case 'cvv':
      isValid = /\d/.test(key)
      break
    case 'cardholderName':
      isValid = /[a-zA-Z\s]/.test(key)
      break
  }
  
  if (!isValid) {
    event.preventDefault()
  }
}

const formatCardNumber = (value: string): string => {
  if (!value) return ''
  
  // Agregar espacios cada 4 dígitos
  const cleaned = value.replace(/\D/g, '')
  const groups = cleaned.match(/.{1,4}/g)
  
  if (!groups) return cleaned
  
  return groups.join(' ')
}

const formatExpiry = (value: string): string => {
  if (!value) return ''
  
  const cleaned = value.replace(/\D/g, '')
  
  if (cleaned.length >= 2) {
    return `${cleaned.substring(0, 2)}/${cleaned.substring(2)}`
  }
  
  return cleaned
}

const detectCardType = (number: string): string => {
  if (!number) return ''
  
  // Patrones de tarjetas conocidas
  const patterns = {
    visa: /^4/,
    mastercard: /^5[1-5]/,
    amex: /^3[47]/,
    discover: /^6(?:011|5)/,
    diners: /^3(?:0[0-5]|[68])/,
    jcb: /^(?:2131|1800|35\d{3})/
  }
  
  for (const [type, pattern] of Object.entries(patterns)) {
    if (pattern.test(number)) {
      return type
    }
  }
  
  return 'unknown'
}

const getCardIcon = (): string => {
  switch (cardType.value) {
    case 'visa': return '💳'
    case 'mastercard': return '💳'
    case 'amex': return '💳'
    case 'discover': return '💳'
    case 'diners': return '💳'
    case 'jcb': return '💳'
    default: return '💳'
  }
}

// Watchers
watch(() => props.modelValue, (newValue) => {
  if (props.type === 'cardNumber' && newValue) {
    const detectedType = detectCardType(newValue)
    if (detectedType !== cardType.value) {
      cardType.value = detectedType
      emit('card-type-detected', detectedType)
    }
  }
}, { immediate: true })
</script>

<style scoped>
.credit-card-input {
  width: 100%;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 16px;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
  transition: all 0.2s ease;
  background: white;
  color: #1e293b;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input:hover:not(:focus) {
  border-color: #cbd5e1;
}

.form-input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.card-icon {
  position: absolute;
  right: 16px;
  font-size: 20px;
  pointer-events: none;
  color: #64748b;
}

.error-message {
  font-size: 12px;
  color: #ef4444;
  font-weight: 500;
}

.input-hint {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

/* Estilos específicos para cada tipo */
.form-input[data-type="cardNumber"] {
  font-size: 18px;
  letter-spacing: 2px;
}

.form-input[data-type="expiry"] {
  font-size: 16px;
  letter-spacing: 1px;
}

.form-input[data-type="cvv"] {
  font-size: 16px;
  letter-spacing: 1px;
  width: 120px;
}

.form-input[data-type="cardholderName"] {
  font-size: 16px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

/* Responsive */
@media (max-width: 640px) {
  .form-input {
    font-size: 16px;
    padding: 10px 14px;
  }
  
  .form-input[data-type="cardNumber"] {
    font-size: 16px;
    letter-spacing: 1px;
  }
  
  .form-input[data-type="cvv"] {
    width: 100px;
  }
}
</style>
