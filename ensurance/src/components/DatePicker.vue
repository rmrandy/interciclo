<template>
  <div class="date-picker-container">
    <div class="date-input-wrapper" @click="toggleCalendar">
      <div class="date-input">
        <span class="date-icon">📅</span>
        <div class="date-display">
          <div class="date-label">{{ label }}</div>
          <div class="date-value">
            {{ displayValue || placeholder }}
          </div>
        </div>
        <span class="calendar-arrow" :class="{ 'rotated': isOpen }">▼</span>
      </div>
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
          @click="selectDate(day)"
          class="calendar-day"
          :class="{
            'other-month': day.otherMonth,
            'today': day.isToday,
            'selected': day.isSelected,
            'disabled': day.disabled,
            'hover': day.hoverable
          }"
        >
          {{ day.day }}
        </div>
      </div>

      <!-- Acciones rápidas -->
      <div class="quick-actions">
        <button @click="selectToday" class="quick-btn" type="button">
          Hoy
        </button>
        <button @click="clearDate" class="quick-btn clear" type="button">
          Limpiar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'

interface Props {
  modelValue?: string
  label: string
  placeholder?: string
  minDate?: string
  maxDate?: string
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: 'Seleccionar fecha',
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// Estado del calendario
const isOpen = ref(false)
const currentDate = ref(new Date())
const selectedDate = ref<Date | null>(null)

// Días de la semana en español
const weekDays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

// Computed properties
const currentMonthYear = computed(() => {
  return currentDate.value.toLocaleDateString('es-ES', {
    month: 'long',
    year: 'numeric'
  })
})

const displayValue = computed(() => {
  if (!selectedDate.value) return ''
  return selectedDate.value.toLocaleDateString('es-ES', {
    weekday: 'short',
    day: 'numeric',
    month: 'short'
  })
})

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  
  // Primer día del mes
  const firstDay = new Date(year, month, 1)
  // Último día del mes
  const lastDay = new Date(year, month + 1, 0)
  
  // Día de la semana del primer día (0 = domingo, 1 = lunes, etc.)
  const firstDayWeekday = firstDay.getDay()
  // Ajustar para que lunes sea 0
  const adjustedFirstDay = firstDayWeekday === 0 ? 6 : firstDayWeekday - 1
  
  const days = []
  
  // Días del mes anterior para completar la primera semana
  for (let i = adjustedFirstDay - 1; i >= 0; i--) {
    const date = new Date(year, month, -i)
    days.push({
      day: date.getDate(),
      date: date,
      otherMonth: true,
      isToday: isToday(date),
      isSelected: isSelected(date),
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
      isSelected: isSelected(date),
      disabled: isDisabled(date),
      hoverable: !isDisabled(date),
      key: `current-${date.getTime()}`
    })
  }
  
  // Días del mes siguiente para completar la última semana
  const remainingDays = 42 - days.length // 6 semanas * 7 días
  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(year, month + 1, day)
    days.push({
      day: date.getDate(),
      date: date,
      otherMonth: true,
      isToday: isToday(date),
      isSelected: isSelected(date),
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
  
  selectedDate.value = day.date
  const dateString = day.date.toISOString().split('T')[0]
  emit('update:modelValue', dateString)
  closeCalendar()
}

const selectToday = () => {
  const today = new Date()
  selectedDate.value = today
  const dateString = today.toISOString().split('T')[0]
  emit('update:modelValue', dateString)
  closeCalendar()
}

const clearDate = () => {
  selectedDate.value = null
  emit('update:modelValue', '')
  closeCalendar()
}

const isToday = (date: Date) => {
  const today = new Date()
  return date.toDateString() === today.toDateString()
}

const isSelected = (date: Date) => {
  if (!selectedDate.value) return false
  return date.toDateString() === selectedDate.value.toDateString()
}

const isDisabled = (date: Date) => {
  if (props.minDate) {
    const minDate = new Date(props.minDate)
    if (date < minDate) return true
  }
  
  if (props.maxDate) {
    const maxDate = new Date(props.maxDate)
    if (date > maxDate) return true
  }
  
  // Deshabilitar fechas pasadas
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (date < today) return true
  
  return false
}

// Watchers
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    selectedDate.value = new Date(newValue)
    currentDate.value = new Date(newValue)
  } else {
    selectedDate.value = null
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
  if (props.modelValue) {
    selectedDate.value = new Date(props.modelValue)
    currentDate.value = new Date(props.modelValue)
  }
})
</script>

<style scoped>
.date-picker-container {
  position: relative;
  width: 100%;
}

.date-input-wrapper {
  cursor: pointer;
}

.date-input {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: white;
  transition: all 0.2s ease;
  min-height: 56px;
}

.date-input:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.date-input:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.date-icon {
  font-size: 20px;
  color: #64748b;
}

.date-display {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.date-label {
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.date-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.2;
}

.calendar-arrow {
  font-size: 12px;
  color: #64748b;
  transition: transform 0.2s ease;
}

.calendar-arrow.rotated {
  transform: rotate(180deg);
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
  z-index: 1000;
  min-width: 320px;
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

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  cursor: pointer;
  border-radius: 8px;
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

.calendar-day.today {
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
}

.calendar-day.selected {
  background: #3b82f6;
  color: white;
  font-weight: 600;
}

.calendar-day.disabled {
  color: #cbd5e1;
  cursor: not-allowed;
  background: #f8fafc;
}

.calendar-day.disabled:hover {
  background: #f8fafc;
  transform: none;
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
    min-width: 280px;
    left: -20px;
    right: -20px;
  }
  
  .calendar-day {
    font-size: 13px;
  }
  
  .weekday {
    font-size: 11px;
    padding: 10px 6px;
  }
}
</style>
