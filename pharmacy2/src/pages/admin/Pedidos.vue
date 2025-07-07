<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="orders-management-container">
    <div class="orders-header">
      <h1 class="orders-title">Gestión de Pedidos</h1>
      <p class="orders-subtitle">Administra y actualiza el estado de los pedidos</p>
    </div>

    <!-- Filtros -->
    <div class="filters-section">
      <div class="filters-grid">
        <div class="filter-group">
          <label for="statusFilter">Filtrar por Estado:</label>
          <select id="statusFilter" v-model="statusFilter" @change="applyFilters">
            <option value="">Todos los estados</option>
            <option value="recibido">Recibido</option>
            <option value="enviado">Enviado</option>
            <option value="entregado">Entregado</option>
            <option value="En progreso">En progreso</option>
            <option value="Completado">Completado</option>
            <option value="Pagado">Pagado</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="userFilter">Filtrar por Usuario:</label>
          <input 
            id="userFilter" 
            v-model="userFilter" 
            type="text" 
            placeholder="Buscar por nombre o email"
            @input="applyFilters"
          />
        </div>

        <div class="filter-group">
          <label for="orderIdFilter">Buscar por ID de Pedido:</label>
          <input 
            id="orderIdFilter" 
            v-model="orderIdFilter" 
            type="number" 
            placeholder="ID del pedido"
            @input="applyFilters"
          />
        </div>

        <div class="filter-actions">
          <button @click="clearFilters" class="btn btn-secondary">
            Limpiar Filtros
          </button>
          <button @click="refreshOrders" class="btn btn-primary">
            Actualizar
          </button>
        </div>
      </div>
    </div>

    <!-- Tabla de pedidos -->
    <div class="orders-table-container">
      <div v-if="loading" class="loading-section">
        <div class="loading-spinner"></div>
        <p>Cargando pedidos...</p>
      </div>

      <div v-else-if="filteredOrders.length === 0" class="no-orders">
        <p>No se encontraron pedidos con los filtros aplicados.</p>
      </div>

      <div v-else class="orders-table">
        <table>
          <thead>
            <tr>
              <th>ID Pedido</th>
              <th>Cliente</th>
              <th>Email</th>
              <th>Estado</th>
              <th>Fecha</th>
              <th>Última actualización</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredOrders" :key="order.idOrder">
              <td>#{{ order.idOrder }}</td>
              <td>{{ order.user?.name || 'N/A' }}</td>
              <td>{{ order.user?.email || 'N/A' }}</td>
              <td>
                <span class="status-badge" :class="getStatusClass(order.status)">
                  {{ order.status }}
                </span>
              </td>
              <td>{{ formatDate(order.createdAt) }}</td>
              <td>{{ formatDate(order.updatedAt) }}</td>
              <td>
                <div class="action-buttons">
                  <button 
                    @click="viewOrderDetails(order)" 
                    class="btn btn-info btn-sm"
                    title="Ver detalles"
                  >
                    👁️
                  </button>
                  <div class="status-dropdown">
                    <button 
                      @click="toggleStatusDropdown(order.idOrder)" 
                      class="btn btn-primary btn-sm"
                      title="Cambiar estado"
                    >
                      📝
                    </button>
                    <div 
                      v-if="activeStatusDropdown === order.idOrder" 
                      class="dropdown-menu"
                    >
                      <button 
                        @click="updateOrderStatus(order.idOrder, 'enviado')"
                        :disabled="order.status !== 'recibido'"
                        class="dropdown-item"
                      >
                        Enviado
                      </button>
                      <button 
                        @click="updateOrderStatus(order.idOrder, 'entregado')"
                        :disabled="order.status !== 'enviado'"
                        class="dropdown-item"
                      >
                        Entregado
                      </button>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal de detalles del pedido -->
    <div v-if="showOrderModal" class="modal-overlay" @click="closeOrderModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Detalles del Pedido #{{ selectedOrder?.idOrder }}</h3>
          <button @click="closeOrderModal" class="close-btn">&times;</button>
        </div>
        
        <div v-if="selectedOrder" class="modal-body">
          <div class="order-info">
            <div class="info-section">
              <h4>Información del Cliente</h4>
              <p><strong>Nombre:</strong> {{ selectedOrder.user?.name }}</p>
              <p><strong>Email:</strong> {{ selectedOrder.user?.email }}</p>
              <p><strong>Teléfono:</strong> {{ selectedOrder.user?.phone }}</p>
              <p><strong>Dirección:</strong> {{ selectedOrder.user?.address }}</p>
            </div>
            
            <div class="info-section">
              <h4>Información del Pedido</h4>
              <p><strong>Estado:</strong> 
                <span class="status-badge" :class="getStatusClass(selectedOrder.status)">
                  {{ selectedOrder.status }}
                </span>
              </p>
              <p><strong>Fecha:</strong> {{ formatDate(selectedOrder.createdAt) }}</p>
              <p><strong>Última actualización:</strong> {{ formatDate(selectedOrder.updatedAt) }}</p>
            </div>
          </div>

          <div class="order-items">
            <h4>Productos del Pedido</h4>
            <div v-if="orderItems.length === 0" class="no-items">
              <p>Cargando productos...</p>
            </div>
            <div v-else class="items-list">
              <div v-for="item in orderItems" :key="`${item.orders.idOrder}-${item.medicine.idMedicine}`" class="item-card">
                <div class="item-image">
                  <img :src="item.medicine.image" :alt="item.medicine.name" />
                </div>
                <div class="item-details">
                  <h5>{{ item.medicine.name }}</h5>
                  <p><strong>Principio Activo:</strong> {{ item.medicine.activeMedicament }}</p>
                  <p><strong>Cantidad:</strong> {{ item.quantity }}</p>
                  <p><strong>Precio Unitario:</strong> Q{{ item.cost?.toFixed(2) }}</p>
                  <p><strong>Total:</strong> Q{{ (item.cost * item.quantity).toFixed(2) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/userStore';
import axios from 'axios';
import ApiService from '../../services/ApiService';

const userStore = useUserStore();

// Estados
const orders = ref([]);
const orderItems = ref([]);
const loading = ref(false);
const statusFilter = ref('');
const userFilter = ref('');
const orderIdFilter = ref('');
const activeStatusDropdown = ref(null);
const showOrderModal = ref(false);
const selectedOrder = ref(null);

// Verificar si el usuario tiene permisos de admin/empleado
const hasAdminAccess = computed(() => {
  const user = userStore.getUser();
  return user && (
    user.role === 'admin' ||
    user.role === 'administrador' ||
    user.role === 'empleado' ||
    user.role === 'employee'
  );
});

// Filtrar pedidos
const filteredOrders = computed(() => {
  let filtered = [...orders.value];

  if (statusFilter.value) {
    filtered = filtered.filter(order => 
      order.status?.toLowerCase().includes(statusFilter.value.toLowerCase())
    );
  }

  if (userFilter.value) {
    const searchTerm = userFilter.value.toLowerCase();
    filtered = filtered.filter(order => 
      order.user?.name?.toLowerCase().includes(searchTerm) ||
      order.user?.email?.toLowerCase().includes(searchTerm)
    );
  }

  if (orderIdFilter.value) {
    filtered = filtered.filter(order => 
      order.idOrder.toString().includes(orderIdFilter.value)
    );
  }

  // Ordenar por fecha de creación descendente (más nuevos arriba)
  filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  return filtered;
});

// Métodos
const fetchOrders = async () => {
  loading.value = true;
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl('/orders'));
    orders.value = response.data || [];
  } catch (error) {
    console.error('Error fetching orders:', error);
    alert('Error al cargar los pedidos');
  } finally {
    loading.value = false;
  }
};

const fetchOrderItems = async (orderId) => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl(`/order_medicines?orderId=${orderId}`));
    orderItems.value = response.data || [];
  } catch (error) {
    console.error('Error fetching order items:', error);
    orderItems.value = [];
  }
};

const updateOrderStatus = async (orderId, newStatus) => {
  try {
    await axios.put(ApiService.getPharmacyApiUrl(`/orders/${orderId}/status`), {
      status: newStatus
    });
    
    // Actualizar el estado local
    const orderIndex = orders.value.findIndex(o => o.idOrder === orderId);
    if (orderIndex !== -1) {
      orders.value[orderIndex].status = newStatus;
    }
    
    activeStatusDropdown.value = null;
    alert(`Estado del pedido #${orderId} actualizado a "${newStatus}"`);
  } catch (error) {
    console.error('Error updating order status:', error);
    alert('Error al actualizar el estado del pedido');
  }
};

const viewOrderDetails = async (order) => {
  selectedOrder.value = order;
  showOrderModal.value = true;
  await fetchOrderItems(order.idOrder);
};

const closeOrderModal = () => {
  showOrderModal.value = false;
  selectedOrder.value = null;
  orderItems.value = [];
};

const toggleStatusDropdown = (orderId) => {
  activeStatusDropdown.value = activeStatusDropdown.value === orderId ? null : orderId;
};

const applyFilters = () => {
  // Los filtros se aplican automáticamente por computed
};

const clearFilters = () => {
  statusFilter.value = '';
  userFilter.value = '';
  orderIdFilter.value = '';
};

const refreshOrders = () => {
  fetchOrders();
};

const getStatusClass = (status) => {
  const statusLower = status?.toLowerCase();
  switch (statusLower) {
    case 'recibido':
      return 'status-received';
    case 'enviado':
      return 'status-sent';
    case 'entregado':
      return 'status-delivered';
    case 'en progreso':
      return 'status-progress';
    case 'completado':
      return 'status-completed';
    case 'pagado':
      return 'status-paid';
    default:
      return 'status-default';
  }
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('es-GT', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// Verificar permisos al montar
onMounted(() => {
  if (!hasAdminAccess.value) {
    alert('No tienes permisos para acceder a esta página');
    window.history.back();
    return;
  }
  
  fetchOrders();
});

// Cerrar dropdown al hacer clic fuera
document.addEventListener('click', (e) => {
  if (!e.target.closest('.status-dropdown')) {
    activeStatusDropdown.value = null;
  }
});
</script>

<style scoped>
.orders-management-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8fafc;
  min-height: 100vh;
}

.orders-header {
  text-align: center;
  margin-bottom: 2rem;
}

.orders-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 0.5rem;
}

.orders-subtitle {
  font-size: 1.1rem;
  color: #64748b;
}

.filters-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 600;
  color: #374151;
  font-size: 0.9rem;
}

.filter-group select,
.filter-group input {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
}

.filter-group select:focus,
.filter-group input:focus {
  outline: none;
  border-color: #1e40af;
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
}

.filter-actions {
  display: flex;
  gap: 0.5rem;
}

.orders-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #1e40af;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-orders {
  text-align: center;
  padding: 3rem;
  color: #64748b;
}

.orders-table {
  overflow-x: auto;
}

.orders-table table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th,
.orders-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.orders-table th {
  background-color: #f9fafb;
  font-weight: 600;
  color: #374151;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.orders-table tr:hover {
  background-color: #f9fafb;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.status-received {
  background-color: #fef3c7;
  color: #92400e;
}

.status-sent {
  background-color: #dbeafe;
  color: #1e40af;
}

.status-delivered {
  background-color: #d1fae5;
  color: #065f46;
}

.status-progress {
  background-color: #fef3c7;
  color: #92400e;
}

.status-completed {
  background-color: #d1fae5;
  color: #065f46;
}

.status-paid {
  background-color: #dbeafe;
  color: #1e40af;
}

.status-default {
  background-color: #f3f4f6;
  color: #374151;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
}

.btn-primary {
  background-color: #1e40af;
  color: white;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.btn-secondary {
  background-color: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background-color: #4b5563;
}

.btn-info {
  background-color: #0891b2;
  color: white;
}

.btn-info:hover {
  background-color: #0e7490;
}

.status-dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  z-index: 10;
  min-width: 120px;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.5rem 1rem;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  font-size: 0.875rem;
  color: #374151;
}

.dropdown-item:hover {
  background-color: #f3f4f6;
}

.dropdown-item:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dropdown-item:first-child {
  border-radius: 8px 8px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 8px 8px;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  color: #1e40af;
  font-size: 1.5rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  background-color: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
}

.order-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.info-section h4 {
  color: #1e40af;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.info-section p {
  margin-bottom: 0.5rem;
  color: #374151;
}

.order-items h4 {
  color: #1e40af;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.no-items {
  text-align: center;
  padding: 2rem;
  color: #64748b;
}

.items-list {
  display: grid;
  gap: 1rem;
}

.item-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: #f9fafb;
}

.item-image img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  border-radius: 6px;
  background: white;
}

.item-details h5 {
  margin: 0 0 0.5rem 0;
  color: #1e40af;
  font-size: 1rem;
}

.item-details p {
  margin: 0.25rem 0;
  color: #374151;
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .filters-grid {
    grid-template-columns: 1fr;
  }
  
  .order-info {
    grid-template-columns: 1fr;
  }
  
  .orders-table {
    font-size: 0.875rem;
  }
  
  .orders-table th,
  .orders-table td {
    padding: 0.5rem;
  }
}
</style> 