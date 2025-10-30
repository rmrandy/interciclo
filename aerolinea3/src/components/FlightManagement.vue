<template>
  <div class="flight-management">
    <div class="header">
      <h2>🛫 Gestión de Vuelos</h2>
      <p>Administra todos los aspectos de los vuelos de la aerolínea</p>
    </div>

    <!-- Filtros y búsqueda -->
    <div class="filters">
      <div class="search-box">
        <input 
          v-model="searchTerm" 
          type="text" 
          placeholder="Buscar por número de vuelo, origen o destino..."
          @input="filterFlights"
        />
        <button @click="filterFlights" class="search-btn">🔍</button>
      </div>
      
      <div class="status-filter">
        <select v-model="statusFilter" @change="filterFlights">
          <option value="">Todos los estados</option>
          <option value="DRAFT">Borrador</option>
          <option value="PUBLISHED">Publicado</option>
          <option value="SCHEDULED">Programado</option>
          <option value="CANCELLED">Cancelado</option>
          <option value="COMPLETED">Completado</option>
        </select>
      </div>
    </div>

    <!-- Lista de vuelos -->
    <div class="flights-list" v-if="filteredFlights.length > 0">
      <div 
        v-for="flight in filteredFlights" 
        :key="flight.idFlight" 
        class="flight-card"
        :class="getFlightStatusClass(flight.status)"
      >
        <div class="flight-header">
          <div class="flight-number">
            <h3>{{ flight.flightNumber }}</h3>
            <span class="status-badge" :class="getStatusClass(flight.status)">
              {{ getStatusLabel(flight.status) }}
            </span>
          </div>
          <div class="flight-actions">
            <button 
              @click="editFlight(flight)" 
              class="btn-edit"
              :disabled="!flight.canBeModified"
              title="Editar vuelo"
            >
              ✏️
            </button>
            <button 
              @click="cancelFlight(flight)" 
              class="btn-cancel"
              :disabled="!flight.canBeCancelled"
              title="Cancelar vuelo"
            >
              🚫
            </button>
            <button 
              @click="deleteFlight(flight)" 
              class="btn-delete"
              :disabled="!flight.isDraft"
              title="Eliminar vuelo"
            >
              🗑️
            </button>
          </div>
        </div>

        <div class="flight-details">
          <div class="route">
            <div class="origin">
              <strong>{{ flight.originCity }}</strong>
              <span class="time">{{ flight.departureDate }} {{ flight.departureTime }}</span>
            </div>
            <div class="arrow">→</div>
            <div class="destination">
              <strong>{{ flight.destinationCity }}</strong>
              <span class="time">{{ flight.arrivalDate }} {{ flight.arrivalTime }}</span>
            </div>
          </div>
          
          <div class="flight-info">
            <div class="info-item">
              <span class="label">Precio base:</span>
              <span class="value">${{ flight.basePrice }}</span>
            </div>
            <div class="info-item">
              <span class="label">Asientos disponibles:</span>
              <span class="value">{{ flight.availableSeats }}</span>
            </div>
            <div class="info-item" v-if="flight.gate">
              <span class="label">Puerta:</span>
              <span class="value">{{ flight.gate }}</span>
            </div>
            <div class="info-item" v-if="flight.terminal">
              <span class="label">Terminal:</span>
              <span class="value">{{ flight.terminal }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Mensaje cuando no hay vuelos -->
    <div v-else class="no-flights">
      <p>No se encontraron vuelos con los filtros aplicados.</p>
    </div>

    <!-- Modal de edición -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>✏️ Editar Vuelo {{ editingFlight?.flightNumber }}</h3>
          <button @click="closeEditModal" class="close-btn">×</button>
        </div>
        
        <form @submit.prevent="saveFlightChanges" class="edit-form">
          <div class="form-row">
            <div class="form-group">
              <label>Número de vuelo:</label>
              <input v-model="editForm.flightNumber" type="text" required />
            </div>
            <div class="form-group">
              <label>Estado:</label>
              <select v-model="editForm.status" required disabled title="El estado no se puede cambiar desde aquí">
                <option value="DRAFT">Borrador</option>
                <option value="PUBLISHED">Publicado</option>
                <option value="CANCELLED">Cancelado</option>
                <option value="COMPLETED">Completado</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Ciudad origen:</label>
              <select v-model="editForm.originCityId" required disabled title="El origen no se puede cambiar">
                <option v-for="city in cities" :key="city.idCity" :value="city.idCity">
                  {{ city.name }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Ciudad destino:</label>
              <select v-model="editForm.destinationCityId" required disabled title="El destino no se puede cambiar">
                <option v-for="city in cities" :key="city.idCity" :value="city.idCity">
                  {{ city.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Fecha salida:</label>
              <input v-model="editForm.departureDate" type="date" required />
            </div>
            <div class="form-group">
              <label>Hora salida:</label>
              <input v-model="editForm.departureTime" type="time" required />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Fecha llegada:</label>
              <input v-model="editForm.arrivalDate" type="date" required />
            </div>
            <div class="form-group">
              <label>Hora llegada:</label>
              <input v-model="editForm.arrivalTime" type="time" required />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Precio base:</label>
              <input v-model="editForm.basePrice" type="number" step="0.01" min="0" required />
            </div>
            <div class="form-group">
              <label>Asientos disponibles:</label>
              <input v-model="editForm.availableSeats" type="number" min="0" required />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Puerta:</label>
              <input v-model="editForm.gate" type="text" />
            </div>
            <div class="form-group">
              <label>Terminal:</label>
              <input v-model="editForm.terminal" type="text" />
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeEditModal" class="btn-secondary">Cancelar</button>
            <button type="submit" class="btn-primary">Guardar Cambios</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal de cancelación -->
    <div v-if="showCancelModal" class="modal-overlay" @click="closeCancelModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>🚫 Cancelar Vuelo {{ cancellingFlight?.flightNumber }}</h3>
          <button @click="closeCancelModal" class="close-btn">×</button>
        </div>
        
        <form @submit.prevent="confirmCancelFlight" class="cancel-form">
          <div class="form-group">
            <label>Motivo de cancelación:</label>
            <textarea 
              v-model="cancelForm.reason" 
              placeholder="Describe el motivo de la cancelación..."
              required
              rows="4"
            ></textarea>
          </div>
          
          <div class="form-actions">
            <button type="button" @click="closeCancelModal" class="btn-secondary">Cancelar</button>
            <button type="submit" class="btn-danger">Confirmar Cancelación</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal de confirmación de eliminación -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>🗑️ Eliminar Vuelo {{ deletingFlight?.flightNumber }}</h3>
          <button @click="closeDeleteModal" class="close-btn">×</button>
        </div>
        
        <div class="delete-confirmation">
          <p>¿Estás seguro de que quieres eliminar este vuelo?</p>
          <p><strong>Esta acción no se puede deshacer.</strong></p>
          
          <div class="flight-summary">
            <p><strong>Vuelo:</strong> {{ deletingFlight?.flightNumber }}</p>
            <p><strong>Ruta:</strong> {{ deletingFlight?.originCity }} → {{ deletingFlight?.destinationCity }}</p>
            <p><strong>Fecha:</strong> {{ deletingFlight?.departureDate }}</p>
          </div>
          
          <div class="form-actions">
            <button @click="closeDeleteModal" class="btn-secondary">Cancelar</button>
            <button @click="confirmDeleteFlight" class="btn-danger">Eliminar Definitivamente</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { airlineApi } from '../utils/airlineApi'

// Estado reactivo
const flights = ref<any[]>([])
const cities = ref<any[]>([])
const searchTerm = ref('')
const statusFilter = ref('')
const filteredFlights = ref<any[]>([])
const isLoadingEdit = ref(false)

// Modales
const showEditModal = ref(false)
const showCancelModal = ref(false)
const showDeleteModal = ref(false)

// Formularios
const editingFlight = ref<any>(null)
const cancellingFlight = ref<any>(null)
const deletingFlight = ref<any>(null)

const editForm = ref({
  flightNumber: '',
  status: '',
  originCityId: 0,
  destinationCityId: 0,
  departureDate: '',
  departureTime: '',
  arrivalDate: '',
  arrivalTime: '',
  basePrice: 0,
  availableSeats: 0,
  gate: '',
  terminal: '',
  updatedBy: 1 // Por defecto
})

const cancelForm = ref({
  reason: ''
})

// Computed properties
const getFlightStatusClass = (status: string) => {
  switch (status) {
    case 'DRAFT': return 'status-draft'
    case 'PUBLISHED': return 'status-published'
    case 'SCHEDULED': return 'status-scheduled'
    case 'CANCELLED': return 'status-cancelled'
    case 'COMPLETED': return 'status-completed'
    default: return ''
  }
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'DRAFT': return 'badge-draft'
    case 'PUBLISHED': return 'badge-published'
    case 'SCHEDULED': return 'badge-scheduled'
    case 'CANCELLED': return 'badge-cancelled'
    case 'COMPLETED': return 'badge-completed'
    default: return ''
  }
}

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'DRAFT': return 'Borrador'
    case 'PUBLISHED': return 'Publicado'
    case 'SCHEDULED': return 'Programado'
    case 'CANCELLED': return 'Cancelado'
    case 'COMPLETED': return 'Completado'
    default: return status
  }
}

// Métodos
const loadFlights = async () => {
  try {
    console.log('🔄 Cargando vuelos...')
    console.log('🔧 Configuración API:', airlineApi.config.getBaseUrl())
    const response = await airlineApi.getFlights()
    console.log('📡 Respuesta de getFlights:', response)
    if (response.success) {
      // Enriquecer objetos de vuelo con flags requeridos por la UI cuando el backend no los provee
      const enriched = (response.flights || []).map((f: any) => ({
        ...f,
        isDraft: f?.isDraft ?? (String(f?.status || '').toUpperCase() === 'DRAFT'),
        canBeModified: f?.canBeModified ?? (['DRAFT', 'PUBLISHED', 'SCHEDULED'].includes(String(f?.status || '').toUpperCase())),
        // Por defecto permitimos cancelar salvo que esté COMPLETED (o ya CANCELLED)
        canBeCancelled: f?.canBeCancelled ?? (!['COMPLETED'].includes(String(f?.status || '').toUpperCase())),
      }))
      flights.value = enriched
      console.log('✅ Vuelos cargados:', flights.value.length)
      filterFlights()
    } else {
      console.error('❌ Error en respuesta:', response.error)
    }
  } catch (error) {
    console.error('❌ Error cargando vuelos:', error)
  }
}

const loadCities = async () => {
  try {
    const response = await airlineApi.getCities()
    if (response.success) {
      cities.value = response.cities
    }
  } catch (error) {
    console.error('Error cargando ciudades:', error)
  }
}

const filterFlights = () => {
  let filtered = flights.value

  // Filtro por búsqueda
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase()
    filtered = filtered.filter(flight => 
      flight.flightNumber.toLowerCase().includes(term) ||
      flight.originCity.toLowerCase().includes(term) ||
      flight.destinationCity.toLowerCase().includes(term)
    )
  }

  // Filtro por estado
  if (statusFilter.value) {
    filtered = filtered.filter(flight => flight.status === statusFilter.value)
  }

  filteredFlights.value = filtered
}

// Métodos de edición
const editFlight = async (flight: any) => {
  isLoadingEdit.value = true
  try {
    editingFlight.value = flight

    // Intentar obtener detalle completo desde el backend
    let source: any = flight
    try {
      const res = await airlineApi.getFlight(flight.idFlight)
      if (res?.success && res?.flight) {
        source = res.flight
      }
    } catch (e) {
      console.warn('No se pudo obtener el detalle del vuelo, usando datos de la lista', e)
    }

    const findCityIdByName = (name: string) => {
      if (!name) return 1
      const match = cities.value.find((c: any) => String(c.name).toLowerCase() === String(name).toLowerCase())
      return match ? match.idCity : 1
    }

    editForm.value = {
      flightNumber: source.flightNumber,
      status: source.status,
      originCityId: source.originCityId || source.originCity?.idCity || findCityIdByName(source.originCity),
      destinationCityId: source.destinationCityId || source.destinationCity?.idCity || findCityIdByName(source.destinationCity),
      departureDate: source.departureDate,
      departureTime: source.departureTime,
      arrivalDate: source.arrivalDate,
      arrivalTime: source.arrivalTime,
      basePrice: source.basePrice,
      availableSeats: source.availableSeats,
      gate: source.gate || '',
      terminal: source.terminal || '',
      updatedBy: 1
    }

    showEditModal.value = true
  } finally {
    isLoadingEdit.value = false
  }
}

const saveFlightChanges = async () => {
  try {
    // Enviar solo campos permitidos (sin cambiar origen/destino)
    const payload = {
      flightNumber: editForm.value.flightNumber,
      departureDate: editForm.value.departureDate,
      departureTime: editForm.value.departureTime,
      arrivalDate: editForm.value.arrivalDate,
      arrivalTime: editForm.value.arrivalTime,
      basePrice: editForm.value.basePrice,
      availableSeats: editForm.value.availableSeats,
      gate: editForm.value.gate,
      terminal: editForm.value.terminal,
      updatedBy: editForm.value.updatedBy
    }

    const response = await airlineApi.updateFlight(editingFlight.value.idFlight, payload)
    if (response.success) {
      // Actualizar la lista
      await loadFlights()
      closeEditModal()
      alert('Vuelo actualizado exitosamente')
    } else {
      alert('Error actualizando vuelo: ' + response.error)
    }
  } catch (error) {
    console.error('Error guardando cambios:', error)
    alert('Error al guardar los cambios')
  }
}

const closeEditModal = () => {
  showEditModal.value = false
  editingFlight.value = null
  editForm.value = {
    flightNumber: '',
    status: '',
    originCityId: 0,
    destinationCityId: 0,
    departureDate: '',
    departureTime: '',
    arrivalDate: '',
    arrivalTime: '',
    basePrice: 0,
    availableSeats: 0,
    gate: '',
    terminal: '',
    updatedBy: 1
  }
}

// Métodos de cancelación
const cancelFlight = (flight: any) => {
  cancellingFlight.value = flight
  cancelForm.value.reason = ''
  showCancelModal.value = true
}

const confirmCancelFlight = async () => {
  try {
    const response = await airlineApi.cancelFlight(
      cancellingFlight.value.idFlight,
      cancelForm.value.reason,
      1 // Por defecto
    )
    if (response.success) {
      await loadFlights()
      closeCancelModal()
      alert('Vuelo cancelado exitosamente')
    } else {
      alert('Error cancelando vuelo: ' + response.error)
    }
  } catch (error) {
    console.error('Error cancelando vuelo:', error)
    alert('Error al cancelar el vuelo')
  }
}

const closeCancelModal = () => {
  showCancelModal.value = false
  cancellingFlight.value = null
  cancelForm.value.reason = ''
}

// Métodos de eliminación
const deleteFlight = (flight: any) => {
  deletingFlight.value = flight
  showDeleteModal.value = true
}

const confirmDeleteFlight = async () => {
  try {
    const response = await airlineApi.deleteFlight(deletingFlight.value.idFlight)
    if (response.success) {
      await loadFlights()
      closeDeleteModal()
      alert('Vuelo eliminado exitosamente')
    } else {
      alert('Error eliminando vuelo: ' + response.error)
    }
  } catch (error) {
    console.error('Error eliminando vuelo:', error)
    alert('Error al eliminar el vuelo')
  }
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  deletingFlight.value = null
}

// Lifecycle
onMounted(() => {
  loadFlights()
  loadCities()
})
</script>

<style scoped>
.flight-management {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  align-items: center;
}

.search-box {
  display: flex;
  flex: 1;
}

.search-box input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px 0 0 5px;
  font-size: 14px;
}

.search-btn {
  padding: 10px 15px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 0 5px 5px 0;
  cursor: pointer;
}

.status-filter select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.flights-list {
  display: grid;
  gap: 20px;
}

.flight-card {
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.flight-card:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  transform: translateY(-2px);
}

.flight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.flight-number h3 {
  margin: 0;
  color: #2c3e50;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  margin-left: 10px;
}

.badge-draft { background: #f39c12; color: white; }
.badge-published { background: #27ae60; color: white; }
.badge-cancelled { background: #e74c3c; color: white; }
.badge-completed { background: #3498db; color: white; }

.flight-actions {
  display: flex;
  gap: 10px;
}

.flight-actions button {
  padding: 8px 12px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
}

.btn-edit {
  background: #f39c12;
  color: white;
}

.btn-edit:hover:not(:disabled) {
  background: #e67e22;
}

.btn-cancel {
  background: #e74c3c;
  color: white;
}

.btn-cancel:hover:not(:disabled) {
  background: #c0392b;
}

.btn-delete {
  background: #95a5a6;
  color: white;
}

.btn-delete:hover:not(:disabled) {
  background: #7f8c8d;
}

.flight-actions button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.flight-details {
  display: grid;
  gap: 15px;
}

.route {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.origin, .destination {
  text-align: center;
}

.origin strong, .destination strong {
  display: block;
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 5px;
}

.time {
  font-size: 14px;
  color: #7f8c8d;
}

.arrow {
  font-size: 24px;
  color: #3498db;
  font-weight: bold;
}

.flight-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.label {
  font-weight: bold;
  color: #7f8c8d;
}

.value {
  color: #2c3e50;
}

.no-flights {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

/* Modales */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 10px;
  padding: 0;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #7f8c8d;
}

.close-btn:hover {
  color: #e74c3c;
}

.edit-form, .cancel-form {
  padding: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: bold;
  color: #2c3e50;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn-primary, .btn-secondary, .btn-danger {
  padding: 12px 24px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #27ae60;
  color: white;
}

.btn-primary:hover {
  background: #229954;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.btn-danger {
  background: #e74c3c;
  color: white;
}

.btn-danger:hover {
  background: #c0392b;
}

.delete-confirmation {
  padding: 20px;
  text-align: center;
}

.delete-confirmation p {
  margin-bottom: 15px;
  color: #2c3e50;
}

.flight-summary {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
  text-align: left;
}

.flight-summary p {
  margin: 5px 0;
}

/* Estados de vuelo */
.status-draft { border-left: 4px solid #f39c12; }
.status-published { border-left: 4px solid #27ae60; }
.status-cancelled { border-left: 4px solid #e74c3c; }
.status-completed { border-left: 4px solid #3498db; }

/* Responsive */
@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .route {
    flex-direction: column;
    text-align: center;
  }
  
  .arrow {
    transform: rotate(90deg);
  }
}
</style>

