<template>
  <div class="user-orders-container">
    <h1 class="orders-title">Mis Pedidos</h1>
    <div v-if="loading" class="loading-section">
      <div class="loading-spinner"></div>
      <p>Cargando tus pedidos...</p>
    </div>
    <div v-else-if="orders.length === 0" class="no-orders">
      <p>No tienes pedidos registrados.</p>
    </div>
    <div v-else class="orders-table-container">
      <table class="orders-table">
        <thead>
          <tr>
            <th>ID Pedido</th>
            <th>Estado</th>
            <th>Fecha</th>
            <th>Última actualización</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.idOrder">
            <td>#{{ order.idOrder }}</td>
            <td>
              <span class="status-badge" :class="getStatusClass(order.status)">
                {{ order.status }}
              </span>
            </td>
            <td>{{ formatDate(order.createdAt) }}</td>
            <td>{{ formatDate(order.updatedAt) }}</td>
            <td>
              <button @click="viewOrderDetails(order)" class="btn btn-info btn-sm" title="Ver detalles">
                👁️
              </button>
            </td>
          </tr>
        </tbody>
      </table>
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
              <h4>Información del Pedido</h4>
              <p><strong>Estado:</strong> <span class="status-badge" :class="getStatusClass(selectedOrder.status)">{{ selectedOrder.status }}</span></p>
              <p><strong>Fecha:</strong> {{ formatDate(selectedOrder.createdAt) }}</p>
              <p><strong>Última actualización:</strong> {{ formatDate(selectedOrder.updatedAt) }}</p>
            </div>
            <div class="info-section">
              <h4>Dirección de entrega</h4>
              <p>{{ selectedOrder.user?.address || 'N/A' }}</p>
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
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/userStore';
import axios from 'axios';
import ApiService from '../services/ApiService';

const userStore = useUserStore();
const orders = ref([]);
const orderItems = ref([]);
const loading = ref(false);
const showOrderModal = ref(false);
const selectedOrder = ref(null);

const fetchOrders = async () => {
  loading.value = true;
  try {
    const user = userStore.getUser ? userStore.getUser() : userStore.user;
    if (!user || !user.idUser) {
      orders.value = [];
      loading.value = false;
      return;
    }
    const response = await axios.get(ApiService.getPharmacyApiUrl(`/orders?userId=${user.idUser}`));
    orders.value = response.data || [];
    // Ordenar por fecha descendente
    orders.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } catch (error) {
    orders.value = [];
  } finally {
    loading.value = false;
  }
};

const fetchOrderItems = async (orderId) => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl(`/order_medicines?orderId=${orderId}`));
    orderItems.value = response.data || [];
  } catch (error) {
    orderItems.value = [];
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

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.user-orders-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8fafc;
  min-height: 100vh;
}
.orders-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 2rem;
  text-align: center;
}
.orders-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
.orders-table {
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
  .order-info {
    grid-template-columns: 1fr;
  }
  .orders-table th,
  .orders-table td {
    padding: 0.5rem;
  }
}
</style> 