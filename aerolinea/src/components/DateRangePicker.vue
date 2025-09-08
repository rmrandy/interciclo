<template>
  <div class="date-range-picker-container">
    <div class="date-range-input" @click="toggleCalendar">
      <div class="date-range-display">
        <div class="date-range-label">{{ label }}</div>
        <div class="date-range-values">
          <div class="date-item">
            <span class="date-icon">✈️</span>
            <span class="date-text">{{ departureDisplay || 'Ida' }}</span>
          </div>
          <div v-if="props.flightType === 'round-trip'" class="date-separator">→</div>
          <div v-if="props.flightType === 'round-trip'" class="date-item">
            <span class="date-icon">🏠</span>
            <span class="date-text">{{ returnDisplay || 'Vuelta' }}</span>
          </div>
        </div>
      </div>
      <span class="calendar-arrow" :class="{ 'rotated': isOpen }">▼</span>
    </div>

    <!-- Calendario desplegable -->
    <div v-if="isOpen" class="calendar-dropdown" v-click-outside="closeCalendar">
      <div class="calendar-header">
        <button @click="previousMonth" class="nav-btn" type="button">
          ‹
        </button>
        <div class="current-month">
          {{ currentMonthYear }}
        </div>
        <button @click="nextMonth" class="nav-btn" type="button">
          ›
        </button>
      </div>

      <!-- Días de la semana -->
      <div class="weekdays">
        <div v-for="day in weekDays" :key="day" class="weekday">
          {{ day }}
        </div>
      </div>

      <!-- Días del mes -->
      <div class="calendar-grid">
        <div
          v-for="day in calendarDays"
          :key="day.key"
          @mousedown.prevent.stop="selectDate(day)"
          class="calendar-day"
          :class="{
            'other-month': day.otherMonth,
            'today': day.isToday,
            'departure': day.isDeparture,
            'return': day.isReturn,
            'in-range': day.inRange,
            'disabled': day.disabled,
            'hover': day.hoverable
          }"
        >
          {{ day.day }}
        </div>
      </div>

      <!-- Información del rango seleccionado -->
      <div v-if="departureDate || returnDate" class="range-info">
        <div class="range-item">
          <span class="range-label">Ida:</span>
          <span class="range-date">{{ departureDisplay || 'No seleccionada' }}</span>
        </div>
        <div class="range-item">
          <span class="range-label">Vuelta:</span>
          <span class="range-date">{{ returnDisplay || 'No seleccionada' }}</span>
        </div>
        <div v-if="departureDate && returnDate" class="range-duration">
          {{ getDurationText() }}
        </div>
      </div>

      <!-- Acciones rápidas -->
      <div class="quick-actions">
        <button @click="selectToday" class="quick-btn" type="button">Hoy</button>
        <button @click="selectNextWeek" class="quick-btn" type="button">Próxima semana</button>
        <button @click="clearDates" class="quick-btn clear" type="button">Limpiar</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'

interface Props {
  departureDate?: string
  returnDate?: string
  label?: string
  disabled?: boolean
  flightType?: 'one-way' | 'round-trip'
}

const props = withDefaults(defineProps<Props>(), {
  label: 'Seleccionar fechas',
  disabled: false,
  flightType: 'round-trip'
})

const emit = defineEmits<{
  // Compatibilidad con padres que usan kebab-case en el template
  'update:departure-date': [value: string]
  'update:return-date': [value: string]
  // Compatibilidad con camelCase si fuese usado en algún lugar
  'update:departureDate': [value: string]
  'update:returnDate': [value: string]
}>()

// Estado del calendario
const isOpen = ref(false)
const currentDate = ref(new Date())
const departureDate = ref<Date | null>(null)
const returnDate = ref<Date | null>(null)
const selectionMode = ref<'departure' | 'return'>('departure')

// Días de la semana en español
const weekDays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

// Computed properties
const currentMonthYear = computed(() => {
  return currentDate.value.toLocaleDateString('es-ES', {
    month: 'long',
    year: 'numeric'
  })
})

const departureDisplay = computed(() => {
  if (!departureDate.value) return ''
  return departureDate.value.toLocaleDateString('es-ES', {
    weekday: 'short',
    day: 'numeric',
    month: 'short'
  })
})

const departureDisplayDDMM = computed(() => {
  if (!departureDate.value) return ''
  const d = departureDate.value
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const yyyy = d.getFullYear()
  return `${dd}/${mm}/${yyyy}`
})

const returnDisplay = computed(() => {
  if (!returnDate.value) return ''
  return returnDate.value.toLocaleDateString('es-ES', {
    weekday: 'short',
    day: 'numeric',
    month: 'short'
  })
})

const returnDisplayDDMM = computed(() => {
  if (!returnDate.value) return ''
  const d = returnDate.value
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const yyyy = d.getFullYear()
  return `${dd}/${mm}/${yyyy}`
})

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  
  const firstDayWeekday = firstDay.getDay()
  const adjustedFirstDay = firstDayWeekday === 0 ? 6 : firstDayWeekday - 1
  
  const days = []
  
  // Días del mes anterior
  for (let i = adjustedFirstDay - 1; i >= 0; i--) {
    const date = new Date(year, month, -i)
    days.push({
      day: date.getDate(),
      date: date,
      otherMonth: true,
      isToday: isToday(date),
      isDeparture: isDeparture(date),
      isReturn: isReturn(date),
      inRange: isInRange(date),
      disabled: isDisabled(date),
      hoverable: !isDisabled(date),
      key: `prev-${date.getTime()}`
    })
  }
  
  // Días del mes actual
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(year, month, day)
    days.push({
      day: day,
      date: date,
      otherMonth: false,
      isToday: isToday(date),
      isDeparture: isDeparture(date),
      isReturn: isReturn(date),
      inRange: isInRange(date),
      disabled: isDisabled(date),
      hoverable: !isDisabled(date),
      key: `current-${date.getTime()}`
    })
  }
  
  // Días del mes siguiente
  const remainingDays = 42 - days.length
  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(year, month + 1, day)
    days.push({
      day: date.getDate(),
      date: date,
      otherMonth: true,
      isToday: isToday(date),
      isDeparture: isDeparture(date),
      isReturn: isReturn(date),
      inRange: isInRange(date),
      disabled: isDisabled(date),
      hoverable: !isDisabled(date),
      key: `next-${date.getTime()}`
    })
  }
  
  return days
})

// Métodos
const toggleCalendar = () => {
  if (props.disabled) return
  isOpen.value = !isOpen.value
}

const closeCalendar = () => {
  isOpen.value = false
}

const previousMonth = () => {
  currentDate.value = new Date(
    currentDate.value.getFullYear(),
    currentDate.value.getMonth() - 1,
    1
  )
}

const nextMonth = () => {
  currentDate.value = new Date(
    currentDate.value.getFullYear(),
    currentDate.value.getMonth() + 1,
    1
  )
}

const selectDate = (day: any) => {
  if (day.disabled || day.otherMonth) return
  
  if (props.flightType === 'one-way') {
    // Solo ida: solo seleccionar fecha de salida
    departureDate.value = day.date
    const iso = day.date.toISOString().split('T')[0]
    emit('update:departure-date', iso)
    emit('update:departureDate', iso)
    closeCalendar()
  } else {
    // Vuelo redondo: selección secuencial
    if (selectionMode.value === 'departure') {
      departureDate.value = day.date
      selectionMode.value = 'return'
      const iso = day.date.toISOString().split('T')[0]
      emit('update:departure-date', iso)
      emit('update:departureDate', iso)
    } else {
      // Validar que la fecha de vuelta sea posterior a la de ida
      if (departureDate.value && day.date <= departureDate.value) {
        return
      }
      returnDate.value = day.date
      selectionMode.value = 'departure'
      const isoR = day.date.toISOString().split('T')[0]
      emit('update:return-date', isoR)
      emit('update:returnDate', isoR)
      closeCalendar()
    }
  }
}

const focusDeparture = () => { selectionMode.value = 'departure'; isOpen.value = true }
const focusReturn = () => { if (props.flightType === 'round-trip') { selectionMode.value = 'return'; isOpen.value = true } }

const selectToday = () => {
  const today = new Date()
  departureDate.value = today
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  returnDate.value = tomorrow
  
  emit('update:departure-date', today.toISOString().split('T')[0])
  emit('update:departureDate', today.toISOString().split('T')[0])
  emit('update:return-date', tomorrow.toISOString().split('T')[0])
  emit('update:returnDate', tomorrow.toISOString().split('T')[0])
  closeCalendar()
}

const selectNextWeek = () => {
  const today = new Date()
  const nextWeek = new Date(today)
  nextWeek.setDate(today.getDate() + 7)
  const returnDate = new Date(nextWeek)
  returnDate.setDate(nextWeek.getDate() + 3) // 3 días después
  
  departureDate.value = nextWeek
  returnDate.value = returnDate
  
  emit('update:departure-date', nextWeek.toISOString().split('T')[0])
  emit('update:departureDate', nextWeek.toISOString().split('T')[0])
  emit('update:return-date', returnDate.toISOString().split('T')[0])
  emit('update:returnDate', returnDate.toISOString().split('T')[0])
  closeCalendar()
}

const clearDates = () => {
  departureDate.value = null
  returnDate.value = null
  selectionMode.value = 'departure'
  emit('update:departure-date', '')
  emit('update:departureDate', '')
  emit('update:return-date', '')
  emit('update:returnDate', '')
  closeCalendar()
}

const isToday = (date: Date) => {
  const today = new Date()
  return date.toDateString() === today.toDateString()
}

const isDeparture = (date: Date) => {
  if (!departureDate.value) return false
  return date.toDateString() === departureDate.value.toDateString()
}

const isReturn = (date: Date) => {
  if (!returnDate.value) return false
  return date.toDateString() === returnDate.value.toDateString()
}

const isInRange = (date: Date) => {
  if (!departureDate.value || !returnDate.value) return false
  return date > departureDate.value && date < returnDate.value
}

const isDisabled = (date: Date) => {
  // Deshabilitar fechas pasadas
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (date < today) return true
  
  return false
}

const getDurationText = () => {
  if (!departureDate.value || !returnDate.value) return ''
  
  const diffTime = returnDate.value.getTime() - departureDate.value.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays === 1) return '1 día'
  return `${diffDays} días`
}

// Watchers
watch(() => props.departureDate, (newValue) => {
  if (newValue) {
    departureDate.value = new Date(newValue)
    currentDate.value = new Date(newValue)
  } else {
    departureDate.value = null
  }
}, { immediate: true })

watch(() => props.returnDate, (newValue) => {
  if (newValue) {
    returnDate.value = new Date(newValue)
  } else {
    returnDate.value = null
  }
}, { immediate: true })

// Directiva para cerrar al hacer clic fuera
const vClickOutside = {
  mounted(el: HTMLElement, binding: any) {
    el._clickOutside = (event: Event) => {
      if (!(el === event.target || el.contains(event.target as Node))) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el: HTMLElement) {
    document.removeEventListener('click', el._clickOutside)
  }
}

onMounted(() => {
  if (props.departureDate) {
    departureDate.value = new Date(props.departureDate)
    currentDate.value = new Date(props.departureDate)
  }
  if (props.returnDate) {
    returnDate.value = new Date(props.returnDate)
  }
})
</script>

<style scoped>
.date-range-picker-container {
  position: relative;
  width: 100%;
}

.date-range-input {
  cursor: pointer;
  border: 2px solid #dfe6ef;
  border-radius: 16px;
  background: white;
  transition: all 0.2s ease;
  min-height: 64px;
  padding: 8px 12px 0 12px;
  box-shadow: 0 6px 16px rgba(17,24,39,0.06);
}

.date-range-input:hover {
  border-color: #d4dbe6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.date-range-input:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.date-range-display {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.date-range-label {
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.date-range-values {
  display: flex;
  align-items: center;
  gap: 12px;
}

.date-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
  min-width: 100px;
}

.date-icon {
  font-size: 16px;
}

.date-text {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.date-separator {
  font-size: 18px;
  color: #64748b;
  font-weight: 600;
}

/* Nuevo layout compacto con dos segmentos (Ida / Vuelta) */
.dual-display {
  display: flex;
  align-items: stretch;
  gap: 12px;
}

.segment {
  flex: 1;
  padding: 8px 10px 12px;
  border-radius: 10px;
  position: relative;
}

.segment.active::after {
  content: '';
  position: absolute;
  left: 8px;
  right: 8px;
  bottom: 0;
  height: 3px;
  background: #16a34a; /* verde más sobrio */
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
}

.seg-label { font-size: 12px; color: #6b7280; margin-bottom: 6px; text-transform: uppercase; letter-spacing: .02em; }

.seg-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.seg-icon { font-size: 16px; }
.seg-date {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #111827;
}

.segment.active { background: #f8fafc; }

.divider { width: 1px; background: #e5e7eb; margin: 0 4px; }

.calendar-arrow {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #64748b;
  transition: transform 0.2s ease;
}

.calendar-arrow.rotated {
  transform: translateY(-50%) rotate(180deg);
}

.calendar-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
  z-index: 9999;
  min-width: 360px;
  animation: slideDown 0.2s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.nav-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 18px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover {
  background: #e2e8f0;
  color: #334155;
}

.current-month {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  text-transform: capitalize;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  padding: 0 20px;
  background: #f8fafc;
}

.weekday {
  padding: 12px 8px;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  padding: 8px 20px 16px;
}

.calendar-grid.compact {
  gap: 6px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.2s ease;
  position: relative;
}

.calendar-day:hover:not(.disabled) {
  background: #f1f5f9;
  transform: scale(1.05);
}

.calendar-day.other-month {
  color: #cbd5e1;
}

.calendar-day.today { color: #1d4ed8; font-weight: 700; }

.calendar-day.departure { background: #111827; color: #fff; font-weight: 700; }

.calendar-day.return { background: #111827; color: #fff; font-weight: 700; }

.calendar-day.in-range { background: #f1f5f9; color: #111827; font-weight: 600; }

.calendar-day.disabled {
  color: #cbd5e1;
  cursor: not-allowed;
  background: #f8fafc;
}

.calendar-day.disabled:hover {
  background: #f8fafc;
  transform: none;
}

.range-info {
  padding: 16px 20px;
  background: #f8fafc;
  border-top: 1px solid #f1f5f9;
}

.range-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.range-item:last-child {
  margin-bottom: 0;
}

.range-label {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
}

.range-date {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.range-duration {
  text-align: center;
  padding: 8px;
  background: #dbeafe;
  color: #1d4ed8;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  margin-top: 8px;
}

.quick-actions {
  display: flex;
  gap: 8px;
  padding: 16px 20px 20px;
  border-top: 1px solid #f1f5f9;
}

.quick-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-btn:not(.clear) {
  background: #3b82f6;
  color: white;
}

.quick-btn:not(.clear):hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.quick-btn.clear {
  background: #f1f5f9;
  color: #64748b;
}

.quick-btn.clear:hover {
  background: #e2e8f0;
  color: #334155;
}

/* Responsive */
@media (max-width: 640px) {
  .calendar-dropdown {
    min-width: 320px;
    left: -20px;
    right: -20px;
  }
  
  .date-range-values {
    flex-direction: column;
    gap: 8px;
  }
  
  .date-separator {
    transform: rotate(90deg);
  }
}
</style>
