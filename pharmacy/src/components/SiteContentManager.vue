<template>
  <div class="site-content-manager">
    <div class="manager-header">
      <h2>📝 Administrador de Contenido del Sitio</h2>
      <p>Gestiona los textos del header, footer y otros elementos del sitio web</p>
    </div>

    <!-- Botones de acción -->
    <div class="action-buttons">
      <button @click="loadAllContent" class="btn btn-secondary">
        🔄 Recargar Contenido
      </button>
      <button @click="initializeContent" class="btn btn-primary">
        🚀 Inicializar Contenido
      </button>
      <button @click="clearCache" class="btn btn-warning">
        🗑️ Limpiar Cache
      </button>
    </div>

    <!-- Sección de Header -->
    <div class="content-section">
      <h3>🏠 Contenido del Header</h3>
      <div class="content-grid">
        <div class="content-item">
          <label>Título del Header:</label>
          <div class="edit-container">
            <input 
              v-model="headerTitle" 
              @blur="updateHeaderTitle"
              @keyup.enter="updateHeaderTitle"
              class="content-input"
              placeholder="Título del header"
            />
            <button @click="updateHeaderTitle" class="save-btn">💾</button>
          </div>
        </div>
        
        <div class="content-item">
          <label>Subtítulo del Header:</label>
          <div class="edit-container">
            <input 
              v-model="headerSubtitle" 
              @blur="updateHeaderSubtitle"
              @keyup.enter="updateHeaderSubtitle"
              class="content-input"
              placeholder="Subtítulo del header"
            />
            <button @click="updateHeaderSubtitle" class="save-btn">💾</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Sección de Footer -->
    <div class="content-section">
      <h3>🦶 Contenido del Footer</h3>
      <div class="content-grid">
        <div class="content-item">
          <label>Texto del Footer:</label>
          <div class="edit-container">
            <textarea 
              v-model="footerText" 
              @blur="updateFooterText"
              class="content-textarea"
              placeholder="Texto del footer"
              rows="2"
            ></textarea>
            <button @click="updateFooterText" class="save-btn">💾</button>
          </div>
        </div>
        
        <div class="content-item">
          <label>Información de Contacto:</label>
          <div class="edit-container">
            <textarea 
              v-model="footerContact" 
              @blur="updateFooterContact"
              class="content-textarea"
              placeholder="Información de contacto"
              rows="2"
            ></textarea>
            <button @click="updateFooterContact" class="save-btn">💾</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Sección de Contenido Personalizado -->
    <div class="content-section">
      <h3>⚙️ Contenido Personalizado</h3>
      <div class="custom-content">
        <div class="add-content-form">
          <input 
            v-model="newContentKey" 
            class="content-input"
            placeholder="Clave del contenido (ej: welcome_message)"
          />
          <textarea 
            v-model="newContentValue" 
            class="content-textarea"
            placeholder="Valor del contenido"
            rows="3"
          ></textarea>
          <input 
            v-model="newContentDescription" 
            class="content-input"
            placeholder="Descripción (opcional)"
          />
          <button @click="createCustomContent" class="btn btn-primary">
            ➕ Crear Contenido
          </button>
        </div>
      </div>
    </div>

    <!-- Lista de Contenido Existente -->
    <div class="content-section">
      <h3>📋 Contenido Existente</h3>
      <div class="content-list">
        <div 
          v-for="item in allContent" 
          :key="item.contentKey" 
          class="content-list-item"
        >
          <div class="content-info">
            <strong>{{ item.contentKey }}</strong>
            <p class="content-description">{{ item.description || 'Sin descripción' }}</p>
            <p class="content-value">{{ item.contentValue }}</p>
          </div>
          <div class="content-actions">
            <button @click="editContent(item)" class="btn btn-small btn-secondary">
              ✏️ Editar
            </button>
            <button @click="deleteContent(item.contentKey)" class="btn btn-small btn-danger">
              🗑️ Eliminar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Edición -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <h3>✏️ Editar Contenido</h3>
        <div class="modal-form">
          <label>Clave:</label>
          <input v-model="editingContent.contentKey" class="content-input" readonly />
          
          <label>Valor:</label>
          <textarea 
            v-model="editingContent.contentValue" 
            class="content-textarea"
            rows="4"
          ></textarea>
          
          <label>Descripción:</label>
          <input v-model="editingContent.description" class="content-input" />
          
          <div class="modal-actions">
            <button @click="saveEditContent" class="btn btn-primary">💾 Guardar</button>
            <button @click="closeEditModal" class="btn btn-secondary">❌ Cancelar</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Notificaciones -->
    <div v-if="notification.show" class="notification" :class="notification.type">
      {{ notification.message }}
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/userStore';
import SiteContentService from '@/services/SiteContentService';

export default {
  name: 'SiteContentManager',
  
  setup() {
    const userStore = useUserStore();
    
    // Contenido del sitio
    const headerTitle = ref('');
    const headerSubtitle = ref('');
    const footerText = ref('');
    const footerContact = ref('');
    const allContent = ref([]);
    
    // Contenido personalizado
    const newContentKey = ref('');
    const newContentValue = ref('');
    const newContentDescription = ref('');
    
    // Modal de edición
    const showEditModal = ref(false);
    const editingContent = ref({});
    
    // Notificaciones
    const notification = ref({
      show: false,
      message: '',
      type: 'success'
    });
    
    // Verificar si es administrador
    const isAdmin = () => {
      const user = userStore.getUser();
      return user && (user.role === 'admin' || user.role === 'administrador');
    };
    
    // Cargar todo el contenido
    const loadAllContent = async () => {
      try {
        const content = await SiteContentService.loadAllContentV2();
        allContent.value = content;
        
        // Cargar contenido específico
        headerTitle.value = await SiteContentService.getHeaderTitle();
        headerSubtitle.value = await SiteContentService.getHeaderSubtitle();
        footerText.value = await SiteContentService.getFooterText();
        footerContact.value = await SiteContentService.getFooterContact();
        
        showNotification('Contenido cargado exitosamente', 'success');
      } catch (error) {
        showNotification('Error al cargar el contenido', 'error');
      }
    };
    
    // Inicializar contenido
    const initializeContent = async () => {
      try {
        await SiteContentService.initializeContentV2();
        await loadAllContent();
        showNotification('Contenido inicializado exitosamente', 'success');
      } catch (error) {
        showNotification('Error al inicializar el contenido', 'error');
      }
    };
    
    // Limpiar cache
    const clearCache = () => {
      SiteContentService.clearCache();
      showNotification('Cache limpiado exitosamente', 'success');
    };
    
    // Actualizar contenido del header
    const updateHeaderTitle = async () => {
      try {
        await SiteContentService.updateHeaderTitle(headerTitle.value);
        showNotification('Título del header actualizado', 'success');
      } catch (error) {
        showNotification('Error al actualizar el título', 'error');
      }
    };
    
    const updateHeaderSubtitle = async () => {
      try {
        await SiteContentService.updateHeaderSubtitle(headerSubtitle.value);
        showNotification('Subtítulo del header actualizado', 'success');
      } catch (error) {
        showNotification('Error al actualizar el subtítulo', 'error');
      }
    };
    
    // Actualizar contenido del footer
    const updateFooterText = async () => {
      try {
        await SiteContentService.updateFooterText(footerText.value);
        showNotification('Texto del footer actualizado', 'success');
      } catch (error) {
        showNotification('Error al actualizar el texto del footer', 'error');
      }
    };
    
    const updateFooterContact = async () => {
      try {
        await SiteContentService.updateFooterContact(footerContact.value);
        showNotification('Contacto del footer actualizado', 'success');
      } catch (error) {
        showNotification('Error al actualizar el contacto', 'error');
      }
    };
    
    // Crear contenido personalizado
    const createCustomContent = async () => {
      if (!newContentKey.value || !newContentValue.value) {
        showNotification('Por favor completa todos los campos requeridos', 'error');
        return;
      }
      
      try {
        await SiteContentService.createContent(
          newContentKey.value,
          newContentValue.value,
          newContentDescription.value
        );
        
        // Limpiar formulario
        newContentKey.value = '';
        newContentValue.value = '';
        newContentDescription.value = '';
        
        // Recargar contenido
        await loadAllContent();
        
        showNotification('Contenido creado exitosamente', 'success');
      } catch (error) {
        showNotification('Error al crear el contenido', 'error');
      }
    };
    
    // Editar contenido
    const editContent = (content) => {
      editingContent.value = { ...content };
      showEditModal.value = true;
    };
    
    const saveEditContent = async () => {
      try {
        await SiteContentService.updateContentV2(
          editingContent.value.contentKey,
          editingContent.value.contentValue
        );
        
        closeEditModal();
        await loadAllContent();
        showNotification('Contenido actualizado exitosamente', 'success');
      } catch (error) {
        showNotification('Error al actualizar el contenido', 'error');
      }
    };
    
    const closeEditModal = () => {
      showEditModal.value = false;
      editingContent.value = {};
    };
    
    // Eliminar contenido
    const deleteContent = async (key) => {
      if (!confirm('¿Estás seguro de que quieres eliminar este contenido?')) {
        return;
      }
      
      try {
        await SiteContentService.deleteContent(key);
        await loadAllContent();
        showNotification('Contenido eliminado exitosamente', 'success');
      } catch (error) {
        showNotification('Error al eliminar el contenido', 'error');
      }
    };
    
    // Mostrar notificación
    const showNotification = (message, type = 'success') => {
      notification.value = {
        show: true,
        message,
        type
      };
      
      setTimeout(() => {
        notification.value.show = false;
      }, 3000);
    };
    
    onMounted(() => {
      if (isAdmin()) {
        loadAllContent();
      }
    });
    
    return {
      headerTitle,
      headerSubtitle,
      footerText,
      footerContact,
      allContent,
      newContentKey,
      newContentValue,
      newContentDescription,
      showEditModal,
      editingContent,
      notification,
      loadAllContent,
      initializeContent,
      clearCache,
      updateHeaderTitle,
      updateHeaderSubtitle,
      updateFooterText,
      updateFooterContact,
      createCustomContent,
      editContent,
      saveEditContent,
      closeEditModal,
      deleteContent
    };
  }
};
</script>

<style scoped>
.site-content-manager {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.manager-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e5e7eb;
}

.manager-header h2 {
  color: #1e3a8a;
  margin-bottom: 10px;
}

.manager-header p {
  color: #64748b;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.content-section {
  margin-bottom: 40px;
  padding: 25px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.content-section h3 {
  color: #1e3a8a;
  margin-bottom: 20px;
  font-size: 20px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.content-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.content-item label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.edit-container {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.content-input,
.content-textarea {
  flex: 1;
  padding: 12px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: white;
}

.content-input:focus,
.content-textarea:focus {
  outline: none;
  border-color: #1e3a8a;
  box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
}

.content-textarea {
  resize: vertical;
  min-height: 60px;
}

.save-btn {
  padding: 12px 16px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
}

.save-btn:hover {
  background: #059669;
  transform: translateY(-1px);
}

.custom-content {
  margin-top: 20px;
}

.add-content-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-width: 600px;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.content-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.content-list-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.content-info {
  flex: 1;
}

.content-info strong {
  color: #1e3a8a;
  font-size: 16px;
  display: block;
  margin-bottom: 5px;
}

.content-description {
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 8px;
}

.content-value {
  color: #374151;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
}

.content-actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.btn-primary {
  background: #1e3a8a;
  color: white;
}

.btn-primary:hover {
  background: #1e40af;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background: #4b5563;
  transform: translateY(-1px);
}

.btn-warning {
  background: #f59e0b;
  color: white;
}

.btn-warning:hover {
  background: #d97706;
  transform: translateY(-1px);
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.btn-small {
  padding: 6px 12px;
  font-size: 12px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  color: #1e3a8a;
  margin-bottom: 20px;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

/* Notificaciones */
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  z-index: 1001;
  animation: slideIn 0.3s ease;
}

.notification.success {
  background: #10b981;
}

.notification.error {
  background: #ef4444;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .site-content-manager {
    padding: 15px;
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .content-list-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .content-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style> 