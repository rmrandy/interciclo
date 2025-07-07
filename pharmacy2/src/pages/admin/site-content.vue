<template>
  <div class="site-content-admin">
    <h2>📝 Administración de Contenido del Sitio</h2>
    <div v-for="item in items" :key="item.key" class="content-item">
      <label>{{ item.label }}</label>
      <div v-if="!item.editing" class="content-view">
        <span>{{ item.value }}</span>
        <button @click="editItem(item)" class="edit-btn">✏️</button>
      </div>
      <div v-else class="content-edit">
        <input v-model="item.editValue" />
        <button @click="saveItem(item)" class="save-btn">💾</button>
        <button @click="cancelEdit(item)" class="cancel-btn">❌</button>
      </div>
    </div>
    <div v-if="notification.show" :class="['notification', notification.type]">
      {{ notification.message }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getPharmacyApiUrl } from '@/services/ApiService';

const items = ref([
  { key: 'header_title', label: 'Título del Header', value: '', editValue: '', editing: false },
  { key: 'header_subtitle', label: 'Subtítulo del Header', value: '', editValue: '', editing: false },
  { key: 'footer_text', label: 'Texto del Footer', value: '', editValue: '', editing: false },
  { key: 'footer_contact', label: 'Contacto del Footer', value: '', editValue: '', editing: false }
]);

const notification = ref({ show: false, message: '', type: 'success' });

async function loadContent() {
  try {
    const res = await fetch(getPharmacyApiUrl('site-content-v2'));
    const data = await res.json();
    for (const item of items.value) {
      const found = data.find(d => d.key === item.key);
      item.value = found && found.value ? found.value : '';
      item.editValue = item.value;
      item.editing = false;
    }
  } catch (e) {
    showNotification('Error al cargar contenido', 'error');
  }
}

function editItem(item) {
  item.editing = true;
  item.editValue = item.value;
}

function cancelEdit(item) {
  item.editing = false;
  item.editValue = item.value;
}

async function saveItem(item) {
  try {
    const res = await fetch(getPharmacyApiUrl(`site-content-v2/${item.key}`), {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ value: item.editValue })
    });
    if (!res.ok) {
      const err = await res.json();
      showNotification(err.error || 'Error al actualizar', 'error');
      return;
    }
    item.value = item.editValue;
    item.editing = false;
    showNotification('¡Actualizado correctamente!', 'success');
  } catch (e) {
    showNotification('Error al actualizar', 'error');
  }
}

function showNotification(message, type = 'success') {
  notification.value = { show: true, message, type };
  setTimeout(() => { notification.value.show = false; }, 2500);
}

onMounted(loadContent);
</script>

<style scoped>
.site-content-admin {
  max-width: 600px;
  margin: 40px auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  padding: 32px;
}
.content-item {
  margin-bottom: 32px;
}
label {
  font-weight: bold;
  color: #1e3a8a;
  display: block;
  margin-bottom: 8px;
}
.content-view {
  display: flex;
  align-items: center;
  gap: 12px;
}
.content-edit {
  display: flex;
  align-items: center;
  gap: 12px;
}
input {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  font-size: 16px;
  min-width: 250px;
}
.edit-btn, .save-btn, .cancel-btn {
  background: #1e3a8a;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 18px;
  transition: background 0.2s;
}
.edit-btn:hover, .save-btn:hover {
  background: #2563eb;
}
.cancel-btn {
  background: #ef4444;
}
.cancel-btn:hover {
  background: #b91c1c;
}
.notification {
  margin-top: 24px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: bold;
  color: #fff;
  background: #10b981;
  text-align: center;
  animation: fadeIn 0.3s;
}
.notification.error {
  background: #ef4444;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style> 