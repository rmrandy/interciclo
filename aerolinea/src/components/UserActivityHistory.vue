<template>
  <div class="user-activity-history">
    <div class="history-header">
      <h3>📋 Historial de Actividad</h3>
      <p>Revisa tu actividad reciente en el sistema</p>
    </div>

    <!-- Filtros y Controles -->
    <div class="history-controls">
      <div class="filter-group">
        <label for="activityType">Filtrar por tipo:</label>
        <select 
          id="activityType"
          v-model="selectedType" 
          class="filter-select"
        >
          <option value="">Todos los tipos</option>
          <option value="flight_search">Búsquedas de vuelo</option>
          <option value="profile_update">Actualizaciones de perfil</option>
          <option value="settings_update">Cambios de configuración</option>
          <option value="security_update">Cambios de seguridad</option>
          <option value="booking">Reservas</option>
        </select>
      </div>

      <div class="filter-group">
        <label for="dateRange">Rango de fechas:</label>
        <select 
          id="dateRange"
          v-model="selectedDateRange" 
          class="filter-select"
        >
          <option value="7">Últimos 7 días</option>
          <option value="30">Últimos 30 días</option>
          <option value="90">Últimos 3 meses</option>
          <option value="365">Último año</option>
          <option value="all">Todo el historial</option>
        </select>
      </div>

      <button 
        @click="clearHistory"
        class="btn-clear"
        :disabled="activities.length === 0"
      >
        🗑️ Limpiar Historial
      </button>
    </div>

    <!-- Lista de Actividades -->
    <div class="activities-list">
      <div v-if="filteredActivities.length === 0" class="no-activities">
        <div class="no-activities-icon">📭</div>
        <h4>No hay actividades para mostrar</h4>
        <p>Tu historial de actividad aparecerá aquí</p>
      </div>

      <div 
        v-else
        v-for="activity in filteredActivities" 
        :key="activity.id"
        class="activity-item"
        :class="getActivityTypeClass(activity.type)"
      >
        <div class="activity-icon">
          {{ getActivityIcon(activity.type) }}
        </div>
        
        <div class="activity-content">
          <div class="activity-header">
            <h4 class="activity-title">{{ activity.description }}</h4>
            <span class="activity-time">{{ formatTime(activity.timestamp) }}</span>
          </div>
          
          <div class="activity-details">
            <div v-if="activity.details" class="details-grid">
              <div 
                v-for="(value, key) in activity.details" 
                :key="key"
                class="detail-item"
              >
                <span class="detail-label">{{ formatDetailLabel(key) }}:</span>
                <span class="detail-value">{{ formatDetailValue(value) }}</span>
              </div>
            </div>
          </div>
          
          <div class="activity-type-badge">
            {{ getActivityTypeLabel(activity.type) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Paginación -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        @click="currentPage--"
        :disabled="currentPage === 1"
        class="page-btn"
      >
        ← Anterior
      </button>
      
      <span class="page-info">
        Página {{ currentPage }} de {{ totalPages }}
      </span>
      
      <button 
        @click="currentPage++"
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        Siguiente →
      </button>
    </div>

    <!-- Estadísticas -->
    <div class="activity-stats">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ totalActivities }}</div>
          <div class="stat-label">Total de Actividades</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🔍</div>
        <div class="stat-content">
          <div class="stat-value">{{ searchCount }}</div>
          <div class="stat-label">Búsquedas de Vuelo</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">✏️</div>
        <div class="stat-content">
          <div class="stat-value">{{ updateCount }}</div>
          <div class="stat-label">Actualizaciones</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

// Props
interface Props {
  activities: any[]
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  clearHistory: []
}>()

// State
const selectedType = ref('')
const selectedDateRange = ref('7')
const currentPage = ref(1)
const itemsPerPage = ref(10)

// Computed
const filteredActivities = computed(() => {
  let filtered = [...props.activities]
  
  // Filtrar por tipo
  if (selectedType.value) {
    filtered = filtered.filter(activity => activity.type === selectedType.value)
  }
  
  // Filtrar por fecha
  if (selectedDateRange.value !== 'all') {
    const days = parseInt(selectedDateRange.value)
    const cutoffDate = new Date()
    cutoffDate.setDate(cutoffDate.getDate() - days)
    
    filtered = filtered.filter(activity => {
      const activityDate = new Date(activity.timestamp)
      return activityDate >= cutoffDate
    })
  }
  
  return filtered
})

const paginatedActivities = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredActivities.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredActivities.value.length / itemsPerPage.value)
})

const totalActivities = computed(() => props.activities.length)

const searchCount = computed(() => {
  return props.activities.filter(activity => activity.type === 'flight_search').length
})

const updateCount = computed(() => {
  return props.activities.filter(activity => 
    activity.type === 'profile_update' || 
    activity.type === 'settings_update'
  ).length
})

// Methods
const getActivityIcon = (type: string): string => {
  const icons: Record<string, string> = {
    'flight_search': '🔍',
    'profile_update': '👤',
    'settings_update': '⚙️',
    'security_update': '🔐',
    'booking': '🎫',
    'default': '📝'
  }
  return icons[type] || icons.default
}

const getActivityTypeClass = (type: string): string => {
  const classes: Record<string, string> = {
    'flight_search': 'activity-search',
    'profile_update': 'activity-profile',
    'settings_update': 'activity-settings',
    'security_update': 'activity-security',
    'booking': 'activity-booking',
    'default': 'activity-default'
  }
  return classes[type] || classes.default
}

const getActivityTypeLabel = (type: string): string => {
  const labels: Record<string, string> = {
    'flight_search': 'Búsqueda',
    'profile_update': 'Perfil',
    'settings_update': 'Configuración',
    'security_update': 'Seguridad',
    'booking': 'Reserva',
    'default': 'Actividad'
  }
  return labels[type] || labels.default
}

const formatTime = (timestamp: string): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
  
  if (diffInHours < 1) {
    return 'Hace unos minutos'
  } else if (diffInHours < 24) {
    return `Hace ${diffInHours} hora${diffInHours > 1 ? 's' : ''}`
  } else {
    const diffInDays = Math.floor(diffInHours / 24)
    if (diffInDays < 7) {
      return `Hace ${diffInDays} día${diffInDays > 1 ? 's' : ''}`
    } else {
      return date.toLocaleDateString('es-ES', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      })
    }
  }
}

const formatDetailLabel = (key: string): string => {
  const labels: Record<string, string> = {
    'origin': 'Origen',
    'destination': 'Destino',
    'date': 'Fecha',
    'field': 'Campo',
    'oldValue': 'Valor Anterior',
    'newValue': 'Nuevo Valor',
    'updatedFields': 'Campos Actualizados',
    'updatedSettings': 'Configuraciones'
  }
  return labels[key] || key.charAt(0).toUpperCase() + key.slice(1)
}

const formatDetailValue = (value: any): string => {
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

const clearHistory = () => {
  if (confirm('¿Estás seguro de que quieres limpiar todo el historial de actividad?')) {
    emit('clearHistory')
  }
}

// Watchers
watch([selectedType, selectedDateRange], () => {
  currentPage.value = 1
})
</script>

<style scoped>
.user-activity-history {
  max-width: 1000px;
  margin: 0 auto;
}

.history-header {
  text-align: center;
  margin-bottom: 2rem;
}

.history-header h3 {
  font-size: 1.5rem;
  color: white;
  margin-bottom: 0.5rem;
}

.history-header p {
  color: rgba(255, 255, 255, 0.7);
}

.history-controls {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  flex-wrap: wrap;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 200px;
}

.filter-group label {
  color: white;
  font-size: 0.9rem;
  font-weight: 500;
}

.filter-select {
  padding: 0.5rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 0.9rem;
}

.btn-clear {
  padding: 0.5rem 1rem;
  background: rgba(245, 101, 101, 0.2);
  color: #f56565;
  border: 1px solid rgba(245, 101, 101, 0.3);
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn-clear:hover:not(:disabled) {
  background: rgba(245, 101, 101, 0.3);
  border-color: rgba(245, 101, 101, 0.5);
}

.btn-clear:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.activities-list {
  margin-bottom: 2rem;
}

.no-activities {
  text-align: center;
  padding: 3rem 2rem;
  color: rgba(255, 255, 255, 0.6);
}

.no-activities-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-activities h4 {
  color: white;
  margin-bottom: 0.5rem;
}

.activity-item {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  margin-bottom: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.activity-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.activity-item.activity-search {
  border-left: 4px solid #4299e1;
}

.activity-item.activity-profile {
  border-left: 4px solid #48bb78;
}

.activity-item.activity-settings {
  border-left: 4px solid #ed8936;
}

.activity-item.activity-security {
  border-left: 4px solid #f56565;
}

.activity-item.activity-booking {
  border-left: 4px solid #9f7aea;
}

.activity-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.activity-title {
  color: white;
  font-size: 1.1rem;
  margin: 0;
}

.activity-time {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
  white-space: nowrap;
}

.activity-details {
  margin-bottom: 0.75rem;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.5rem;
}

.detail-item {
  display: flex;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.detail-label {
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.detail-value {
  color: white;
}

.activity-type-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 1rem;
  font-size: 0.8rem;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.activity-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-icon {
  font-size: 2rem;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: white;
  margin-bottom: 0.25rem;
}

.stat-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

/* Responsive */
@media (max-width: 768px) {
  .history-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    min-width: auto;
  }
  
  .activity-header {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .details-grid {
    grid-template-columns: 1fr;
  }
  
  .activity-stats {
    grid-template-columns: 1fr;
  }
}
</style>
