<template>
  <div class="purchase-container">
    <div class="purchase-header">
      <h1 class="purchase-title">Verificación de Compra</h1>
      <p class="purchase-subtitle">Revisando disponibilidad y requisitos</p>
    </div>

    <!-- Datos del medicamento -->
    <div v-if="isLoading" class="loading-section">
      <div class="loading-spinner"></div>
      <p>Verificando información...</p>
    </div>

    <div v-else-if="medicine" class="medicine-details">
      <div class="medicine-header">
        <h2>{{ medicine.name }}</h2>
      </div>

      <div class="medicine-data">
        <div class="data-grid">
          <div class="data-item">
            <span class="label">Principio Activo:</span>
            <span class="value">{{ medicine.activeMedicament }}</span>
          </div>
          <div class="data-item">
            <span class="label">Precio Unitario:</span>
            <span class="value price">Q{{ medicine.price.toFixed(2) }}</span>
          </div>
          <div class="data-item">
            <span class="label">Cantidad:</span>
            <span class="value">{{ quantity }}</span>
          </div>
          <div class="data-item">
            <span class="label">Total:</span>
            <span class="value total">Q{{ (medicine.price * quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- Verificación de stock -->
      <div class="verification-card stock-card" :class="{ 'status-success': hasStock, 'status-error': !hasStock }">
        <div class="card-icon">{{ hasStock ? '✓' : '✗' }}</div>
        <div class="card-content">
          <h3>Verificación de Stock</h3>
          <p v-if="hasStock">
            Hay suficiente stock disponible ({{ medicine.stock }} unidades)
          </p>
          <p v-else>
            No hay suficiente stock disponible. Solo quedan {{ medicine.stock }} unidades.
          </p>
        </div>
      </div>

      <!-- Verificación de seguro -->
      <div v-if="userStore.user" class="verification-card insurance-card" :class="{ 'status-success': hasInsurance, 'status-neutral': !hasInsurance }">
        <div class="card-icon">{{ hasInsurance ? '✓' : 'ⓘ' }}</div>
        <div class="card-content">
          <h3>Seguro Médico</h3>
          <p v-if="hasInsurance">
            Su seguro médico cubrirá {{ coveragePercentage }}% del costo
          </p>
          <p v-else>
            No se detectó seguro médico. Pagará el precio completo.
          </p>
        </div>
      </div>
      
      <!-- Formulario de Tarjeta de Crédito -->
      <div class="payment-section">
        <h3>Información de Pago</h3>
        <div class="card-form">
          <div class="form-group">
            <label for="card-name">Titular de la Tarjeta</label>
            <input 
              type="text" 
              id="card-name" 
              v-model="cardName" 
              placeholder="Nombre como aparece en la tarjeta"
              @input="validateAllCardFields"
            />
          </div>
          
          <div class="form-group">
            <label for="card-number">Número de Tarjeta</label>
            <div class="card-number-wrapper">
              <input 
                type="text" 
                id="card-number" 
                v-model="cardNumber" 
                placeholder="1234 5678 9012 3456"
                @input="formatCardNumber"
                maxlength="19"
              />
              <div class="card-type">
                <i class="fas fa-credit-card"></i>
              </div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group col">
              <label for="card-expiry">Fecha de Expiración</label>
              <input 
                type="text" 
                id="card-expiry" 
                v-model="cardExpiry" 
                placeholder="MM/AA" 
                @input="formatCardExpiry"
                maxlength="5"
              />
            </div>
            
            <div class="form-group col">
              <label for="card-cvc">CVC</label>
              <input 
                type="text" 
                id="card-cvc" 
                v-model="cardCVC" 
                placeholder="123" 
                @input="validateAllCardFields"
                maxlength="4"
              />
            </div>
          </div>
          
          <div class="card-validation" v-if="cardNumber">
            <div class="validation-indicator" :class="{'valid': validateCardNumber()}">
              <i :class="validateCardNumber() ? 'fas fa-check' : 'fas fa-times'"></i>
              <span>Número de tarjeta {{ validateCardNumber() ? 'válido' : 'inválido' }}</span>
            </div>
            <div v-if="cardExpiry" class="validation-indicator" :class="{'valid': validateCardExpiry()}">
              <i :class="validateCardExpiry() ? 'fas fa-check' : 'fas fa-times'"></i>
              <span>Fecha de expiración {{ validateCardExpiry() ? 'válida' : 'inválida' }}</span>
            </div>
            <div v-if="cardCVC" class="validation-indicator" :class="{'valid': validateCardCVC()}">
              <i :class="validateCardCVC() ? 'fas fa-check' : 'fas fa-times'"></i>
              <span>CVC {{ validateCardCVC() ? 'válido' : 'inválido' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Resumen de pago -->
      <div class="payment-summary">
        <h3>Resumen de Pago</h3>
        <div class="summary-item">
          <span>Subtotal:</span>
          <span>Q{{ (medicine.price * quantity).toFixed(2) }}</span>
        </div>
        <div v-if="hasInsurance" class="summary-item discount">
          <span>Cobertura del seguro ({{ coveragePercentage }}%):</span>
          <span>-Q{{ ((medicine.price * quantity) * (coveragePercentage / 100)).toFixed(2) }}</span>
        </div>
        <div class="summary-item total">
          <span>Total a pagar:</span>
          <span>Q{{ calculateTotalToPay().toFixed(2) }}</span>
        </div>
      </div>

      <!-- Botones de acción -->
      <div class="action-buttons">
        <button @click="goBack" class="cancel-button">Cancelar</button>
        <button @click="viewDetail" class="detail-button">Ver Detalle</button>
        <button 
          @click="confirmPurchase" 
          class="confirm-button"
          :disabled="!canConfirmPurchase" 
        >
          Confirmar Compra
        </button>
      </div>
    </div>

    <div v-else class="error-container">
      <h2>Producto no encontrado</h2>
      <p>No se pudo encontrar el medicamento solicitado.</p>
      <button @click="goBack" class="back-button">Volver al catálogo</button>
    </div>

    <!-- Modal de detalle de compra -->
    <div v-if="showDetailModal" class="modal-overlay">
      <div class="modal-content detail-modal">
          <div class="modal-header">
            <h3>Detalle de la Compra</h3>
            <button @click="closeDetailModal" class="close-icon">&times;</button>
          </div>
          
          <div class="detail-content" v-if="medicine"> 
            <div class="detail-section">
              <h4>Medicamento</h4>
              <div class="detail-info">
                <p><strong>Nombre:</strong> {{ medicine.name }}</p>
                <p><strong>Categoria:</strong> {{ medicine.activeMedicament }}</p>
                <p><strong>Presentación:</strong> {{ medicine.presentacion }}</p>
                <p><strong>Precio Unitario:</strong> Q{{ medicine.price.toFixed(2) }}</p>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>Detalles de la Compra</h4>
              <div class="detail-info">
                <p><strong>Cantidad:</strong> {{ quantity }}</p>
                <p><strong>Subtotal:</strong> Q{{ (medicine.price * quantity).toFixed(2) }}</p>
                <p v-if="hasInsurance"><strong>Descuento Seguro:</strong> Q{{ ((medicine.price * quantity) * (coveragePercentage / 100)).toFixed(2) }}</p>
                <p><strong>Total a Pagar:</strong> Q{{ calculateTotalToPay().toFixed(2) }}</p>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>Estado de la Compra</h4>
              <div class="detail-info">
                <p><strong>Stock disponible:</strong> <span :class="hasStock ? 'status-ok' : 'status-error'">{{ hasStock ? 'Sí' : 'No' }}</span></p>
                <p><strong>Seguro médico:</strong> <span :class="hasInsurance ? 'status-ok' : 'status-info'">{{ hasInsurance ? `Cobertura del ${coveragePercentage}%` : 'No disponible' }}</span></p>
              </div>
            </div>
          </div>
           <div v-else class="detail-content">
                <p>Cargando detalles...</p>
           </div>
          
          <div class="modal-actions">
            <button @click="closeDetailModal" class="cancel-button">Cerrar</button>
            <button 
              @click="confirmPurchase" 
              class="confirm-button"
              :disabled="!canConfirmPurchase"
            >
              Confirmar Compra
            </button>
          </div>
        </div>
    </div>

    <!-- Modal de confirmación -->
    <div v-if="showConfirmationModal" class="modal-overlay">
      <div class="modal-content">
        <h3>¡Compra Exitosa!</h3>
        <p>Su compra ha sido procesada correctamente.</p>
        <div class="confirmation-details">
          <p><strong>Medicamento:</strong> {{ medicine?.name }}</p>
          <p><strong>Cantidad:</strong> {{ quantity }}</p>
          <p><strong>Total pagado:</strong> Q{{ calculateTotalToPay().toFixed(2) }}</p>
        </div>
        <button @click="finishPurchase" class="finish-button">Continuar</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/userStore';
import axios from 'axios';
import ApiService from '../services/ApiService';  
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// Estados
const medicine = ref(null);
const quantity = ref(1);
const isLoading = ref(true);
const hasStock = ref(false);
const hasInsurance = ref(false);
const coveragePercentage = ref(0);
const showConfirmationModal = ref(false);
const showDetailModal = ref(false);
const activeOrder = ref(null);

// Nuevos estados para tarjeta y seguro
const cardNumber = ref('');
const cardName = ref('');
const cardExpiry = ref('');
const cardCVC = ref('');
const isCardValid = ref(false);

// Verificar si el usuario puede realizar la compra
const canConfirmPurchase = computed(() => {
  return hasStock.value && isCardValid.value;
});

// Calcular total a pagar
function calculateTotalToPay() {
  if (!medicine.value) return 0;
  
  const subtotal = medicine.value.price * quantity.value;
  if (hasInsurance.value) {
    const discount = subtotal * (coveragePercentage.value / 100);
    return subtotal - discount;
  }
  return subtotal;
}

// Ver detalle de la compra (abre el modal)
function viewDetail() {
  showDetailModal.value = true;
}

// Cerrar modal de detalle
function closeDetailModal() {
  showDetailModal.value = false;
}

// Validar formato de tarjeta
function validateCardNumber() {
  // Implementación básica de validación (Luhn algorithm)
  const value = cardNumber.value.replace(/\s/g, '');
  
  // Verificar longitud
  if (value.length < 13 || value.length > 19) {
    return false;
  }
  
  // Verificar que sean solo dígitos
  if (!/^\d+$/.test(value)) {
    return false;
  }
  
  // Implementación del algoritmo de Luhn
  let sum = 0;
  let double = false;
  
  // Recorrer de derecha a izquierda
  for (let i = value.length - 1; i >= 0; i--) {
    let digit = parseInt(value.charAt(i));
    
    if (double) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }
    
    sum += digit;
    double = !double;
  }
  
  // Valid si es divisible por 10
  return sum % 10 === 0;
}

// Validar fecha de expiración
function validateCardExpiry() {
  const value = cardExpiry.value;
  if (!value || !/^\d{2}\/\d{2}$/.test(value)) {
    return false;
  }
  
  const [month, year] = value.split('/').map(num => parseInt(num, 10));
  
  // Verificar que el mes sea válido (1-12)
  if (month < 1 || month > 12) {
    return false;
  }
  
  // Obtener año actual y convertir el año de 2 dígitos a 4 dígitos
  const currentDate = new Date();
  const currentYear = currentDate.getFullYear() % 100; // últimos 2 dígitos
  const currentMonth = currentDate.getMonth() + 1; // 1-12
  
  // Convertir año de tarjeta a número de 2 dígitos para comparación
  const expYear = year;
  
  // Verificar si la tarjeta ya está vencida
  if (expYear < currentYear || (expYear === currentYear && month < currentMonth)) {
    return false;
  }
  
  return true;
}

// Validar CVC
function validateCardCVC() {
  const value = cardCVC.value;
  return /^\d{3,4}$/.test(value);
}

// Formatear número de tarjeta mientras se escribe
function formatCardNumber(e) {
  let value = e.target.value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
  
  // Limitar a 16 dígitos
  if (value.length > 16) {
    value = value.substr(0, 16);
  }
  
  // Formato 4 dígitos - 4 dígitos - 4 dígitos - 4 dígitos
  const parts = [];
  for (let i = 0; i < value.length; i += 4) {
    parts.push(value.substr(i, 4));
  }
  
  cardNumber.value = parts.join(' ');
}

// Formatear fecha de expiración mientras se escribe
function formatCardExpiry(e) {
  let value = e.target.value.replace(/[^0-9]/g, '');
  
  // Limitar a 4 dígitos
  if (value.length > 4) {
    value = value.substr(0, 4);
  }
  
  // Formato MM/YY
  if (value.length > 2) {
    cardExpiry.value = `${value.substr(0, 2)}/${value.substr(2)}`;
  } else {
    cardExpiry.value = value;
  }
}

// Verificar todos los campos de tarjeta
function validateAllCardFields() {
  isCardValid.value = 
    cardNumber.value.trim() !== '' && validateCardNumber() &&
    cardName.value.trim() !== '' &&
    cardExpiry.value.trim() !== '' && validateCardExpiry() &&
    cardCVC.value.trim() !== '' && validateCardCVC();
  
  return isCardValid.value;
}

// Función para confirmar la compra
async function confirmPurchase() {
  if (!canConfirmPurchase.value) return;

  try {
    // 1. Marcar la orden como "Completado"
    if (activeOrder.value) {
      const updatedOrder = { ...activeOrder.value, status: 'Completado' };
      await axios.put(ApiService.getPharmacyApiUrl(`/orders/${activeOrder.value.idOrder}`), updatedOrder);
    }
    
    // 2. Actualizar el stock del producto
    const updatedStock = medicine.value.stock - quantity.value;
    await axios.put(ApiService.getPharmacyApiUrl(`/medicines/${medicine.value.idMedicine}`), { ...medicine.value, stock: updatedStock });


    // 3. Mostrar modal de confirmación (opcional) o redirigir directamente
      showConfirmationModal.value = true;
    
  } catch (error) {
    console.error("Error al confirmar la compra:", error);
    alert("Hubo un error al procesar tu compra. Por favor, inténtalo de nuevo.");
  }
}

// Finalizar proceso y redirigir
function finishPurchase() {
  showConfirmationModal.value = false;
  router.push({ name: 'Gracias', query: { orderId: activeOrder.value?.idOrder || 'desconocido' } });
}

// Volver atrás
function goBack() {
  router.back();
}

// Cargar datos del medicamento
onMounted(async () => {
    const medicineId = route.params.id;
  quantity.value = Number(route.query.quantity) || 1;
  
  if (!userStore.user?.idUser) {
      alert("Debes iniciar sesión para comprar.");
      router.push('/login');
      return;
  }

  try {
    // Obtener la orden activa del usuario
    const ordersResponse = await axios.get(ApiService.getPharmacyApiUrl(`/orders?userId=${userStore.user.idUser}&status=recibido`));
    activeOrder.value = ordersResponse.data.length > 0 ? ordersResponse.data[0] : null;

    const medicineResponse = await axios.get(ApiService.getPharmacyApiUrl(`/medicines/${medicineId}`));
    medicine.value = medicineResponse.data;
    
    // Verificar stock aquí
    if (medicine.value) {
      hasStock.value = medicine.value.stock >= quantity.value;
      }

  } catch (error) {
    console.error("Error al cargar datos de compra:", error);
  } finally {
    isLoading.value = false;
  }
});

</script>

<style scoped>
.purchase-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8fafc;
}

.purchase-header {
  text-align: center;
  margin-bottom: 2rem;
}

.purchase-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 0.5rem;
}

.purchase-subtitle {
  font-size: 1.1rem;
  color: #64748b;
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

.medicine-details {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  margin-bottom: 2rem;
}

.medicine-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.medicine-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.medicine-data {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.data-item {
  display: flex;
  flex-direction: column;
}

.data-item .label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 0.3rem;
}

.data-item .value {
  font-size: 1.1rem;
  color: #1e293b;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.quantity-btn {
  width: 25px;
  height: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e2e8f0;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.data-item .price,
.data-item .total {
  font-weight: 700;
  color: #16a34a;
}

.verification-card {
  display: flex;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  gap: 1rem;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: bold;
  flex-shrink: 0;
}

.status-success .card-icon {
  background-color: #dcfce7;
  color: #16a34a;
}

.status-error .card-icon {
  background-color: #fee2e2;
  color: #dc2626;
}

.status-pending .card-icon {
  background-color: #fef3c7;
  color: #d97706;
}

.status-neutral .card-icon {
  background-color: #e0f2fe;
  color: #0284c7;
}

.card-content {
  flex: 1;
}

.card-content h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
}

.payment-summary {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.payment-summary h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
}

.summary-item.discount {
  color: #16a34a;
}

.summary-item.total {
  font-weight: 700;
  font-size: 1.2rem;
  padding-top: 1rem;
  margin-top: 0.5rem;
  border-top: 1px solid #e2e8f0;
}

.action-buttons {
  padding: 1.5rem;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.cancel-button,
.confirm-button,
.back-button,
.finish-button,
.detail-button {
  padding: 0.8rem 1.5rem;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.cancel-button,
.back-button {
  background-color: #f1f5f9;
  color: #334155;
}

.confirm-button,
.finish-button {
  background-color: #1e40af;
  color: white;
}

.detail-button {
  background-color: #0284c7;
  color: white;
}

.confirm-button:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.error-container {
  text-align: center;
  padding: 3rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  padding: 0; /* Ajustado para header/content/actions */
  width: 90%;
  max-width: 600px; /* Aumentado para más espacio */
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden; /* Para border-radius */
}

.detail-modal .modal-header {
  padding: 1rem 1.5rem;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-modal h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
}

.close-icon {
  background: none;
  border: none;
  font-size: 1.75rem;
  cursor: pointer;
  color: #64748b;
  line-height: 1;
}

.detail-modal .detail-content {
  padding: 1.5rem;
  text-align: left;
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 1.5rem;
}

.detail-section h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #1e40af;
  margin: 0 0 0.75rem 0;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.5rem;
}

.detail-info {
  background-color: #f8fafc;
  padding: 0.75rem 1rem;
  border-radius: 8px;
}

.detail-info p {
  margin: 0.4rem 0;
  font-size: 0.9rem;
}

.detail-info p strong {
  color: #334155;
}

.status-ok {
  color: #16a34a;
  font-weight: 600;
}

.status-error {
  color: #dc2626;
  font-weight: 600;
}

.status-pending,
.status-warning { /* Unificamos estilos */
  color: #d97706;
  font-weight: 600;
}

.status-info {
  color: #0284c7;
  font-weight: 600;
}

.detail-modal .modal-actions {
  padding: 1rem 1.5rem;
  background-color: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.modal-content h3 {
  /* Estilo para modal de confirmación (asegurar especificidad si es necesario) */
  &.confirmation-title { /* Ejemplo de clase para especificidad */
    font-size: 1.5rem;
    font-weight: 700;
    color: #16a34a;
    margin-bottom: 1rem;
  }
}

.confirmation-details {
  background-color: #f8fafc;
  border-radius: 8px;
  padding: 1rem;
  text-align: left;
  margin: 1.5rem 0;
}

@media (max-width: 768px) {
  .data-grid {
    grid-template-columns: 1fr;
  }

  .verification-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .cancel-button,
  .confirm-button,
  .detail-button {
    width: 100%;
  }
}

.quantity-controls.disabled {
  opacity: 0.7;
}

.quantity-locked {
  font-size: 0.8rem;
  color: #64748b;
  margin-left: 0.5rem;
  font-style: italic;
}

/* Estilos para el formulario de tarjeta de crédito */
.payment-section {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.payment-section h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  color: #1e293b;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.5rem;
}

.card-form {
  background-color: #f8fafc;
  padding: 1.5rem;
  border-radius: 10px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: #334155;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 1rem;
  transition: all 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-row .col {
  flex: 1;
}

.card-number-wrapper {
  position: relative;
}

.card-type {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
}

.card-validation {
  margin-top: 0.5rem;
}

.validation-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: #ef4444;
}

.validation-indicator.valid {
  color: #22c55e;
}

.validation-indicator i {
  font-size: 1rem;
}

/* Pequeño loader para estados de verificación */
.verification-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #e2e8f0;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  display: inline-block;
  margin-right: 0.5rem;
}
</style> 