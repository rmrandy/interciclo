<template>
  <div id="app">
    <Header @open-port-selector="showPortSelector = true" />
    <router-view /> <!-- Aquí se mostrarán las páginas según la ruta -->
    <Footer />
    
    <!-- Selector de puertos -->
    <PortSelector v-if="showPortSelector" @close="showPortSelector = false" />
  </div>
</template>

<script>
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
import PortSelector from '@/components/PortSelector.vue';
import ApiService from '@/services/ApiService';

export default {
  name: "App",
  components: { 
    Header,
    Footer,
    PortSelector
  },
  data() {
    return {
      showPortSelector: false
    };
  },
  created() {
    // Cargar configuración de puertos
    ApiService.loadPortConfiguration();
    
    // Verificar si se debe mostrar el selector de puertos
    const skipPortSelector = localStorage.getItem('skipPortSelector') === 'true';
    this.showPortSelector = !skipPortSelector;
  }
};
</script>

<style>
/* Reset y estilos base */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  margin: 0;
  padding: 0;
  min-height: 100vh;
  color: #1e3a8a;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Variables CSS globales */
:root {
  --primary-color: #1e3a8a;
  --primary-light: #1e40af;
  --secondary-color: #2563eb;
  --accent-color: #10b981;
  --danger-color: #ef4444;
  --warning-color: #f59e0b;
  --text-primary: #1e3a8a;
  --text-secondary: #64748b;
  --text-light: #94a3b8;
  --bg-primary: #ffffff;
  --bg-secondary: #f8fafc;
  --bg-accent: #f1f5f9;
  --border-color: rgba(30, 58, 138, 0.1);
  --shadow-sm: 0 2px 8px rgba(30, 58, 138, 0.08);
  --shadow-md: 0 4px 20px rgba(30, 58, 138, 0.12);
  --shadow-lg: 0 8px 32px rgba(30, 58, 138, 0.16);
  --border-radius: 12px;
  --border-radius-sm: 8px;
  --border-radius-lg: 16px;
  --transition: all 0.3s ease;
}

/* Estilos de la aplicación */
#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-primary);
  margin-top: 70px; /* Espacio para el header fijo */
  min-height: calc(100vh - 70px);
}

/* Estilos de scrollbar personalizados */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-secondary);
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, var(--primary-light), var(--secondary-color));
}

/* Estilos de enlaces */
a {
  color: var(--primary-color);
  text-decoration: none;
  transition: var(--transition);
}

a:hover {
  color: var(--primary-light);
}

/* Estilos de botones base */
button {
  font-family: inherit;
  cursor: pointer;
  transition: var(--transition);
}

/* Estilos de inputs */
input, textarea, select {
  font-family: inherit;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-sm);
  padding: 12px 16px;
  font-size: 14px;
  transition: var(--transition);
  background: var(--bg-primary);
}

input:focus, textarea:focus, select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
}

/* Estilos de cards */
.card {
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: var(--transition);
}

.card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

/* Estilos de botones */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: var(--border-radius-sm);
  font-weight: 600;
  font-size: 14px;
  border: none;
  cursor: pointer;
  transition: var(--transition);
  text-decoration: none;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  color: white;
}

.btn-primary:hover {
  background: linear-gradient(135deg, var(--primary-light), var(--secondary-color));
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: var(--bg-accent);
  border-color: var(--primary-color);
}

.btn-success {
  background: linear-gradient(135deg, var(--accent-color), #059669);
  color: white;
}

.btn-success:hover {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-1px);
}

.btn-danger {
  background: linear-gradient(135deg, var(--danger-color), #dc2626);
  color: white;
}

.btn-danger:hover {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  transform: translateY(-1px);
}

/* Estilos de texto */
.text-primary {
  color: var(--text-primary);
}

.text-secondary {
  color: var(--text-secondary);
}

.text-light {
  color: var(--text-light);
}

.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

/* Estilos de espaciado */
.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 1rem; }
.mt-4 { margin-top: 1.5rem; }
.mt-5 { margin-top: 3rem; }

.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 1rem; }
.mb-4 { margin-bottom: 1.5rem; }
.mb-5 { margin-bottom: 3rem; }

.p-1 { padding: 0.25rem; }
.p-2 { padding: 0.5rem; }
.p-3 { padding: 1rem; }
.p-4 { padding: 1.5rem; }
.p-5 { padding: 3rem; }

/* Estilos de flexbox */
.flex {
  display: flex;
}

.flex-col {
  flex-direction: column;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.justify-between {
  justify-content: space-between;
}

.gap-1 { gap: 0.25rem; }
.gap-2 { gap: 0.5rem; }
.gap-3 { gap: 1rem; }
.gap-4 { gap: 1.5rem; }
.gap-5 { gap: 3rem; }

/* Estilos de grid */
.grid {
  display: grid;
}

.grid-cols-1 { grid-template-columns: repeat(1, minmax(0, 1fr)); }
.grid-cols-2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.grid-cols-3 { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.grid-cols-4 { grid-template-columns: repeat(4, minmax(0, 1fr)); }

/* Estilos de responsive */
@media (max-width: 768px) {
  .hidden-mobile {
    display: none !important;
  }
  
  .grid-cols-2 {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
  
  .grid-cols-3,
  .grid-cols-4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 480px) {
  .grid-cols-3,
  .grid-cols-4 {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}

/* Animaciones */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

.slide-in {
  animation: slideIn 0.3s ease-out;
}

/* Estilos de loading */
.loading {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Estilos de alertas */
.alert {
  padding: 16px;
  border-radius: var(--border-radius-sm);
  margin-bottom: 16px;
  border: 1px solid;
}

.alert-success {
  background-color: #d1fae5;
  border-color: #10b981;
  color: #065f46;
}

.alert-error {
  background-color: #fee2e2;
  border-color: #ef4444;
  color: #991b1b;
}

.alert-warning {
  background-color: #fef3c7;
  border-color: #f59e0b;
  color: #92400e;
}

.alert-info {
  background-color: #dbeafe;
  border-color: #3b82f6;
  color: #1e40af;
}

/* Estilos de badges */
.badge {
  display: inline-block;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-primary {
  background-color: var(--primary-color);
  color: white;
}

.badge-success {
  background-color: var(--accent-color);
  color: white;
}

.badge-warning {
  background-color: var(--warning-color);
  color: white;
}

.badge-danger {
  background-color: var(--danger-color);
  color: white;
}

/* Estilos de tooltips */
.tooltip {
  position: relative;
  cursor: help;
}

.tooltip::after {
  content: attr(data-tooltip);
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
  z-index: 1000;
}

.tooltip:hover::after {
  opacity: 1;
}

/* Estilos de modales */
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
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  padding: 24px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
  animation: fadeIn 0.3s ease-out;
}

/* Estilos de formularios */
.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-primary);
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-sm);
  font-size: 14px;
  transition: var(--transition);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
}

.form-error {
  color: var(--danger-color);
  font-size: 12px;
  margin-top: 4px;
}

/* Estilos de tablas */
.table {
  width: 100%;
  border-collapse: collapse;
  background: var(--bg-primary);
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.table th,
.table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.table th {
  background: var(--bg-secondary);
  font-weight: 600;
  color: var(--text-primary);
}

.table tr:hover {
  background: var(--bg-accent);
}

/* Estilos de paginación */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
}

.pagination-item {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-sm);
  background: var(--bg-primary);
  color: var(--text-primary);
  text-decoration: none;
  transition: var(--transition);
}

.pagination-item:hover,
.pagination-item.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

/* Estilos de breadcrumbs */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 14px;
}

.breadcrumb-item {
  color: var(--text-secondary);
}

.breadcrumb-item:last-child {
  color: var(--text-primary);
  font-weight: 600;
}

.breadcrumb-separator {
  color: var(--text-light);
}

/* Estilos de skeleton loading */
.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: var(--border-radius-sm);
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.skeleton-text {
  height: 16px;
  margin-bottom: 8px;
}

.skeleton-text:last-child {
  width: 60%;
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.skeleton-image {
  width: 100%;
  height: 200px;
  border-radius: var(--border-radius);
}
</style>
