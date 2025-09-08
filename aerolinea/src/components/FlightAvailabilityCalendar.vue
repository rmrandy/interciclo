<template>
  <div class="flight-availability-calendar">
    <div class="calendar-header">
      <h3 class="calendar-title">📅 Calendario de disponibilidad</h3>
      <p class="calendar-subtitle">Selecciona una fecha para ver los vuelos disponibles</p>
    </div>
    
    <div class="calendar-controls">
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
          'has-flights': day.hasFlights,
          'no-flights': day.noFlights,
          'disabled': day.disabled,
          'hover': day.hoverable
        }"
      >
        <div class="day-number">{{ day.day }}</div>
        <div v-if="day.flightCount > 0" class="flight-indicator">
          <span class="flight-count">{{ day.flightCount }}</span>
          <span class="flight-icon">✈️</span>
        </div>
        <div v-if="day.lowestPrice" class="price-indicator">
          <span class="price">${{ day.lowestPrice }}</span>
        </div>
      </div>
    </div>

    <!-- Información de la fecha seleccionada -->
    <div v-if="selectedDateInfo" class="selected-date-info">
      <div class="date-header">
        <h4>{{ selectedDateInfo.formattedDate }}</h4>
        <span class="flight-count-badge">
          {{ selectedDateInfo.flightCount }} vuelos disponibles
        </span>
      </div>
      
      <div v-if="selectedDateInfo.lowestPrice" class="price-info">
        <span class="price-label">Precio más bajo:</span>
        <span class="price-value">${{ selectedDateInfo.lowestPrice }}</span>
      </div>
      
      <div class="quick-actions">
        <button @click="selectThisDate" class="select-btn" type="button">
          Seleccionar esta fecha
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'

interface Flight {
  idFlight: number
  departureDate: string
  basePrice: number
  originCity: string
  destinationCity: string
}

interface Props {
  flights: Flight[]
  selectedDate?: string
  onDateSelect?: (date: string) => void
}

const props = withDefaults(defineProps<Props>(), {
  flights: () => [],
  selectedDate: '',
  onDateSelect: undefined
})

// Estado del calendario
const currentDate = ref(new Date())
const selectedDateInfo = ref<any>(null)

// Días de la semana en español
const weekDays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

// Computed properties
const currentMonthYear = computed(() => {
  return currentDate.value.toLocaleDateString('es-ES', {
    month: 'long',
    year: 'numeric'
  })
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
    const dayInfo = getDayInfo(date)
    days.push({
      day: date.getDate(),
      date: date,
      otherMonth: true,
      isToday: isToday(date),
      isSelected: isSelected(date),
      hasFlights: dayInfo.hasFlights,
      noFlights: dayInfo.noFlights,
      flightCount: dayInfo.flightCount,
      lowestPrice: dayInfo.lowestPrice,
      disabled: isDisabled(date),
      hoverable: !isDisabled(date),
      key: `prev-${date.getTime()}`
    })
  }
  
  // Días del mes actual
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(year, month, day)
    const dayInfo = getDayInfo(date)
    days.push({
      day: day,
      date: date,
      otherMonth: false,
      isToday: isToday(date),
      isSelected: isSelected(date),
      hasFlights: dayInfo.hasFlights,
      noFlights: dayInfo.noFlights,
      flightCount: dayInfo.flightCount,
      lowestPrice: dayInfo.lowestPrice,
      disabled: isDisabled(date),
      hoverable: !isDisabled(date),
      key: `current-${date.getTime()}`
    })
  }
  
  // Días del mes siguiente
  const remainingDays = 42 - days.length
  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(year, month + 1, day)
    const dayInfo = getDayInfo(date)
    days.push({
      day: day,
      date: date,
      otherMonth: true,
      isToday: isToday(date),
      isSelected: isSelected(date),
      hasFlights: dayInfo.hasFlights,
      noFlights: dayInfo.noFlights,
      flightCount: dayInfo.flightCount,
      lowestPrice: dayInfo.lowestPrice,
      disabled: isDisabled(date),
      hoverable: !isDisabled(date),
      key: `next-${date.getTime()}`
    })
  }
  
  return days
})

// Métodos
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
  
  const dateString = day.date.toISOString().split('T')[0]
  selectedDateInfo.value = {
    date: dateString,
    formattedDate: day.date.toLocaleDateString('es-ES', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }),
    flightCount: day.flightCount,
    lowestPrice: day.lowestPrice
  }
  
  if (props.onDateSelect) {
    props.onDateSelect(dateString)
  }
}

const selectThisDate = () => {
  if (selectedDateInfo.value && props.onDateSelect) {
    props.onDateSelect(selectedDateInfo.value.date)
  }
}

const getDayInfo = (date: Date) => {
  const dateString = date.toISOString().split('T')[0]
  const dayFlights = props.flights.filter(flight => 
    flight.departureDate === dateString
  )
  
  if (dayFlights.length === 0) {
    return {
      hasFlights: false,
      noFlights: true,
      flightCount: 0,
      lowestPrice: null
    }
  }
  
  const lowestPrice = Math.min(...dayFlights.map(f => f.basePrice))
  
  return {
    hasFlights: true,
    noFlights: false,
    flightCount: dayFlights.length,
    lowestPrice: lowestPrice
  }
}

const isToday = (date: Date) => {
  const today = new Date()
  return date.toDateString() === today.toDateString()
}

const isSelected = (date: Date) => {
  if (!props.selectedDate) return false
  const selectedDate = new Date(props.selectedDate)
  return date.toDateString() === selectedDate.toDateString()
}

const isDisabled = (date: Date) => {
  // Deshabilitar fechas pasadas
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (date < today) return true
  
  return false
}

// Watchers
watch(() => props.selectedDate, (newValue) => {
  if (newValue) {
    const date = new Date(newValue)
    currentDate.value = new Date(date.getFullYear(), date.getMonth(), 1)
  }
}, { immediate: true })

onMounted(() => {
  if (props.selectedDate) {
    const date = new Date(props.selectedDate)
    currentDate.value = new Date(date.getFullYear(), date.getMonth(), 1)
  }
})
</script>

<style scoped>
.flight-availability-calendar {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.calendar-header {
  text-align: center;
  margin-bottom: 24px;
}

.calendar-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.calendar-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.calendar-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #f8fafc;
  border-radius: 10px;
  font-size: 20px;
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
  transform: scale(1.05);
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
  gap: 2px;
  margin-bottom: 16px;
}

.weekday {
  padding: 12px 8px;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: #f8fafc;
  border-radius: 8px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: 24px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s ease;
  position: relative;
  min-height: 80px;
  background: #f8fafc;
}

.calendar-day:hover:not(.disabled) {
  background: #e2e8f0;
  transform: scale(1.02);
}

.calendar-day.other-month {
  color: #cbd5e1;
  background: #f1f5f9;
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

.calendar-day.has-flights {
  background: #dcfce7;
  border: 2px solid #22c55e;
}

.calendar-day.no-flights {
  background: #fef2f2;
  border: 2px solid #ef4444;
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

.day-number {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.flight-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.flight-count {
  font-size: 12px;
  font-weight: 600;
  color: #16a34a;
}

.flight-icon {
  font-size: 12px;
}

.price-indicator {
  text-align: center;
}

.price {
  font-size: 10px;
  font-weight: 600;
  color: #059669;
  background: #d1fae5;
  padding: 2px 6px;
  border-radius: 4px;
}

.selected-date-info {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.date-header h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.flight-count-badge {
  background: #3b82f6;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.price-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 12px;
  background: #dbeafe;
  border-radius: 8px;
}

.price-label {
  font-size: 14px;
  font-weight: 600;
  color: #1e40af;
}

.price-value {
  font-size: 18px;
  font-weight: 700;
  color: #1e40af;
}

.quick-actions {
  text-align: center;
}

.select-btn {
  background: #10b981;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.select-btn:hover {
  background: #059669;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

/* Responsive */
@media (max-width: 640px) {
  .flight-availability-calendar {
    padding: 16px;
  }
  
  .calendar-day {
    min-height: 60px;
    padding: 4px;
  }
  
  .day-number {
    font-size: 14px;
  }
  
  .flight-count {
    font-size: 10px;
  }
  
  .price {
    font-size: 8px;
  }
}
</style>
