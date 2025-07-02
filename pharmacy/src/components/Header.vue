<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <header class="navbar">
    <div class="navbar-content">
      <!-- Logo -->
      <div class="logo-container">
        <img src="@/assets/logo.png" alt="Logo" class="logo" />
        <span class="logo-text" @click="editHeaderTitle" v-if="!isEditingTitle">
          {{ headerTitle }}
        </span>
        <input 
          v-if="isEditingTitle" 
          v-model="editingTitle" 
          @blur="saveHeaderTitle" 
          @keyup.enter="saveHeaderTitle"
          @keyup.esc="cancelEditTitle"
          class="edit-title-input"
          ref="titleInput"
        />
        <span class="edit-icon" v-if="isAdmin && !isEditingTitle" @click="editHeaderTitle">✏️</span>
      </div>

      <!-- Dropdown de sucursales/puertos -->
      <div class="sucursal-dropdown" style="margin-left: 24px;">
        <label for="sucursal-select" style="font-weight: 500; margin-right: 8px;">Sucursal:</label>
        <select id="sucursal-select" v-model="selectedSucursal" @change="onSucursalChange" class="sucursal-select">
          <option v-for="sucursal in sucursales" :key="sucursal.puerto" :value="sucursal.puerto">
            {{ sucursal.nombre }} ({{ sucursal.puerto }})
          </option>
        </select>
        <button @click="openPortSelector" class="btn btn-secondary btn-sm" style="margin-left: 8px;">⚙️</button>
      </div>

      <!-- Menú de Navegación (versión desktop) -->
      <nav class="nav-links hidden md:flex">
        <router-link to="/" class="nav-item">
          <span class="nav-icon">🏠</span>
          Inicio
        </router-link>
        <router-link to="/catalogo" class="nav-item">
          <span class="nav-icon">📋</span>
          Catálogo
        </router-link>
        <router-link to="/catalogo-internacional" class="nav-item">
          <span class="nav-icon">🌎</span>
          Catálogo Internacional
        </router-link>
        <router-link to="/contact" class="nav-item">
          <span class="nav-icon">📞</span>
          Contacto
        </router-link>
        <router-link to="/admin/puertos" class="nav-item">
          <span class="nav-icon">🛠️</span>
          Puertos
        </router-link>
        <router-link to="/cart" class="nav-item cart-link">
          <span class="nav-icon">🛒</span>
          Carrito
        </router-link>

        <!-- Enlace SOLO para administradores -->
        <router-link
          v-if="isLoggedIn && userStore.getUser().role === 'admin'"
          to="/admindash"
          class="nav-item admin-link"
        >
          <span class="nav-icon">📊</span>
          Dashboard
        </router-link>

        <router-link
          v-if="isLoggedIn && userStore.getUser().role === 'admin'"
          to="/create-product"
          class="nav-item"
        >
          <span class="nav-icon">➕</span>
          Crear Producto
        </router-link>

        <!-- Configuración de puertos (solo para admin y empleados) -->
        <button
          v-if="isLoggedIn && (userStore.getUser().role === 'admin' || userStore.getUser().role === 'employee')"
          @click="openPortSelector"
          class="nav-item admin-config-button"
        >
          <span class="nav-icon">⚙️</span>
          Puertos
        </button>

        <!-- Enlace para administración de contenido del sitio -->
        <router-link
          v-if="isLoggedIn && (['admin','administrador'].includes(userStore.getUser().role))"
          to="/admin/site-content"
          class="nav-item"
        >
          <span class="nav-icon">📝</span>
          Contenido del Sitio
        </router-link>

        <!-- Si el usuario está loggeado -->
        <template v-if="isLoggedIn">
          <!-- Enlace al perfil -->
          <router-link to="/perfil" class="nav-item">
            <span class="nav-icon">👤</span>
            Mi Perfil
          </router-link>
          
          <!-- Muestra el rol -->
          <div class="user-info">
            <span class="user-role">{{ userStore.getUser().role }}</span>
            <span class="user-avatar">👤</span>
          </div>
          <!-- Botón para cerrar sesión -->
          <button @click="logout" class="logout-button">
            <span class="logout-icon">🚪</span>
            Salir
          </button>
        </template>

        <!-- Si NO está loggeado, muestra el botón de Iniciar Sesión -->
        <template v-else>
          <router-link to="/login" class="login-button">
            <span class="login-icon">🔑</span>
            Iniciar Sesión
          </router-link>
        </template>

        <!-- Enlace para gestión de productos -->
        <router-link
          v-if="isLoggedIn && (['admin','administrador','employee','empleado'].includes(userStore.getUser().role))"
          to="/gestion-productos"
          class="nav-item"
        >
          <span class="nav-icon">✏️</span>
          Gestión de Productos
        </router-link>

        <!-- Enlace para gestión de pedidos -->
        <router-link
          v-if="isLoggedIn && (['admin','administrador','employee','empleado'].includes(userStore.getUser().role))"
          to="/admin/pedidos"
          class="nav-item"
        >
          <span class="nav-icon">📦</span>
          Gestión de Pedidos
        </router-link>
      </nav>

      <!-- Botón de menú hamburguesa (versión móvil) -->
      <button @click="toggleMenu" class="menu-button md:hidden">
        <span class="menu-icon">☰</span>
      </button>
    </div>

    <!-- Menú desplegable en móviles -->
    <div v-if="mobileMenuOpen" class="mobile-menu">
      <div class="mobile-menu-header">
        <span class="mobile-menu-title">Menú</span>
        <button @click="toggleMenu" class="close-menu-btn">✕</button>
      </div>
      
      <router-link to="/" class="mobile-item" @click="toggleMenu">
        <span class="mobile-icon">🏠</span>
        Inicio
      </router-link>
      
      <router-link to="/catalogo" class="mobile-item" @click="toggleMenu">
        <span class="mobile-icon">📋</span>
        Catálogo de Productos
      </router-link>
      
      <router-link to="/catalogo-internacional" class="mobile-item" @click="toggleMenu">
        <span class="mobile-icon">🌎</span>
        Catálogo Internacional
      </router-link>
      
      <router-link to="/contact" class="mobile-item" @click="toggleMenu">
        <span class="mobile-icon">📞</span>
        Contacto
      </router-link>
      
      <router-link to="/cart" class="mobile-item" @click="toggleMenu">
        <span class="mobile-icon">🛒</span>
        Carrito
      </router-link>
     
      <!-- Configuración de puertos (móvil - solo para admin y empleados) -->
      <button
        v-if="isLoggedIn && (userStore.getUser().role === 'admin' || userStore.getUser().role === 'employee')"
        @click="openPortSelector(); toggleMenu();"
        class="mobile-item admin-config-button"
      >
        <span class="mobile-icon">⚙️</span>
        Configurar Puertos
      </button>

      <!-- Enlace SOLO para administradores (móvil) -->
      <router-link
        v-if="isLoggedIn && userStore.getUser().role === 'admin'"
        to="/admindash"
        class="mobile-item admin-link"
        @click="toggleMenu"
      >
        <span class="mobile-icon">📊</span>
        Dashboard Admin
      </router-link>

      <!-- Enlace para gestión de productos -->
      <router-link
        v-if="isLoggedIn && (['admin','administrador','employee','empleado'].includes(userStore.getUser().role))"
        to="/gestion-productos"
        class="mobile-item"
        @click="toggleMenu"
      >
        <span class="mobile-icon">✏️</span>
        Gestión de Productos
      </router-link>

      <!-- Enlace para gestión de pedidos -->
      <router-link
        v-if="isLoggedIn && (['admin','administrador','employee','empleado'].includes(userStore.getUser().role))"
        to="/admin/pedidos"
        class="mobile-item"
        @click="toggleMenu"
      >
        <span class="mobile-icon">📦</span>
        Gestión de Pedidos
      </router-link>

      <!-- Enlace para administración de contenido del sitio -->
      <router-link
        v-if="isLoggedIn && (['admin','administrador'].includes(userStore.getUser().role))"
        to="/admin/site-content"
        class="mobile-item"
        @click="toggleMenu"
      >
        <span class="mobile-icon">📝</span>
        Contenido del Sitio
      </router-link>

      <!-- Si está loggeado, muestra rol y logout (móvil) -->
      <template v-if="isLoggedIn">
        <router-link to="/perfil" class="mobile-item" @click="toggleMenu">
          <span class="mobile-icon">👤</span>
          Mi Perfil
        </router-link>
        
        <div class="mobile-user-info">
          <span class="mobile-user-role">Rol: {{ userStore.user.role }}</span>
        </div>
        <button
          @click="
            logout();
            toggleMenu();
          "
          class="mobile-logout"
        >
          <span class="mobile-icon">🚪</span>
          Cerrar Sesión
        </button>
      </template>
      <!-- Si NO está loggeado, login (móvil) -->
      <template v-else>
        <router-link to="/login" class="mobile-login" @click="toggleMenu">
          <span class="mobile-icon">🔑</span>
          Iniciar Sesión
        </router-link>
      </template>
    </div>
  </header>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import SiteContentService from "@/services/SiteContentService";
import { getPharmacyApiUrl } from '@/services/ApiService';
import ApiService from '@/services/ApiService';
import eventBus from '@/eventBus';

export default {
  name: 'AppHeader',
  
  emits: ['open-port-selector'],
  
  setup(props, { emit }) {
    // Menú móvil
    const mobileMenuOpen = ref(false);
    const toggleMenu = () => {
      mobileMenuOpen.value = !mobileMenuOpen.value;
    };
    
    // Store de usuario
    const userStore = useUserStore();
    const router = useRouter();
    
    // Contenido dinámico
    const headerTitle = ref("");
    const headerSubtitle = ref("");
    const isEditingTitle = ref(false);
    const editingTitle = ref('');
    const titleInput = ref(null);
    
    const isLoggedIn = computed(() => {
      const user = userStore.getUser();
      return Object.keys(user).length !== 0;
    });
    
    const isAdmin = computed(() => {
      const user = userStore.getUser();
      return user && (user.role === 'admin' || user.role === 'administrador');
    });
    
    // Función para abrir el selector de puertos
    const openPortSelector = () => {
      // Emite un evento que será capturado por App.vue
      emit('open-port-selector');
    };
    
    // Cerrar sesión
    const logout = () => {
      userStore.logout();
      // Limpiar todas las claves de sesión del localStorage
      localStorage.removeItem("session");
      localStorage.removeItem("user");
      localStorage.removeItem("role");
      console.log("Sesión cerrada: localStorage limpiado");
      
      router.push("/");
      setTimeout(() => {
        window.location.reload();
      }, 100);
    };
    
    // Funciones para editar título
    const editHeaderTitle = () => {
      if (!isAdmin.value) return;
      
      editingTitle.value = headerTitle.value;
      isEditingTitle.value = true;
      
      // Enfocar el input en el siguiente tick
      setTimeout(() => {
        if (titleInput.value) {
          titleInput.value.focus();
          titleInput.value.select();
        }
      }, 0);
    };
    
    const saveHeaderTitle = async () => {
      try {
        await SiteContentService.updateContent('header_title', editingTitle.value);
        headerTitle.value = editingTitle.value;
        isEditingTitle.value = false;
      } catch (error) {
        console.error('Error al guardar el título:', error);
        alert('Error al guardar el título');
      }
    };
    
    const cancelEditTitle = () => {
      isEditingTitle.value = false;
      editingTitle.value = headerTitle.value;
    };
    
    onMounted(async () => {
      try {
        const res = await fetch(getPharmacyApiUrl('site-content-v2'));
        const data = await res.json();
        const foundTitle = data.find(item => item.key === "header_title");
        const foundSubtitle = data.find(item => item.key === "header_subtitle");
        headerTitle.value = foundTitle && foundTitle.value ? foundTitle.value : '';
        headerSubtitle.value = foundSubtitle && foundSubtitle.value ? foundSubtitle.value : '';
      } catch (e) {
        headerTitle.value = '';
        headerSubtitle.value = '';
      }
    });
    
    // Nuevo código para sucursales
    const sucursales = ref([]);
    const selectedSucursal = ref('');
    
    const loadSucursales = () => {
      // Cargar sucursales desde localStorage o usar valores por defecto
      const defaultSucursales = [
        { nombre: 'Sucursal 1', puerto: '8080' },
        { nombre: 'Sucursal 2', puerto: '8081' },
      ];
      const saved = localStorage.getItem('sucursalesPharmacy');
      sucursales.value = saved ? JSON.parse(saved) : defaultSucursales;
      // Selección previa o la primera
      const last = localStorage.getItem('selectedSucursalPharmacy');
      selectedSucursal.value = last || sucursales.value[0].puerto;
      // Aplicar el puerto seleccionado
      ApiService.configureApiPorts({ pharmacy: selectedSucursal.value });
    };
    
    const onSucursalChange = () => {
      ApiService.configureApiPorts({ pharmacy: selectedSucursal.value });
      localStorage.setItem('selectedSucursalPharmacy', selectedSucursal.value);
    };
    
    onMounted(() => {
      eventBus.on('sucursales-actualizadas', loadSucursales);
    });
    
    onBeforeUnmount(() => {
      eventBus.off('sucursales-actualizadas', loadSucursales);
    });
    
    return {
      mobileMenuOpen,
      toggleMenu,
      userStore,
      isLoggedIn,
      isAdmin,
      openPortSelector,
      logout,
      headerTitle,
      headerSubtitle,
      isEditingTitle,
      editingTitle,
      titleInput,
      editHeaderTitle,
      saveHeaderTitle,
      cancelEditTitle,
      sucursales,
      selectedSucursal,
      loadSucursales,
      onSucursalChange
    };
  }
}
</script>

<style scoped>
/* Navbar principal */
.navbar {
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1001;
  box-shadow: 0 4px 20px rgba(30, 58, 138, 0.2);
  backdrop-filter: blur(10px);
}

.navbar-content {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  height: 70px;
}

/* Logo */
.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.logo {
  height: 45px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.logo-text {
  color: white;
  font-size: 20px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 4px 8px;
  border-radius: 6px;
}

.logo-text:hover {
  background: rgba(255, 255, 255, 0.1);
}

.edit-title-input {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #1e3a8a;
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 20px;
  font-weight: 700;
  color: #1e3a8a;
  outline: none;
  min-width: 200px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.edit-title-input:focus {
  border-color: #10b981;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.edit-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.logo-container:hover .edit-icon {
  opacity: 1;
}

.edit-icon:hover {
  background: white;
  transform: scale(1.1);
}

/* Navegación desktop */
.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  color: white;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  padding: 10px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s;
}

.nav-item:hover::before {
  left: 100%;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.15);
  font-weight: 600;
}

.nav-icon {
  font-size: 16px;
}

/* Enlaces especiales */
.cart-link {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.cart-link:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.admin-link {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  font-weight: 600;
}

.admin-link:hover {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
}

.admin-config-button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 10px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  color: white;
  font-weight: 500;
}

.admin-config-button:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

/* Información del usuario */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  margin-left: 12px;
}

.user-role {
  color: white;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.user-avatar {
  font-size: 16px;
}

/* Botones de autenticación */
.login-button {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  margin-left: 12px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.login-button:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

.login-icon {
  font-size: 14px;
}

.logout-button {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  margin-left: 12px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.logout-button:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(239, 68, 68, 0.4);
}

.logout-icon {
  font-size: 14px;
}

/* Botón de menú móvil */
.menu-button {
  font-size: 24px;
  color: white;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.menu-icon {
  font-size: 20px;
}

/* Menú móvil */
.mobile-menu {
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  position: absolute;
  top: 70px;
  left: 0;
  width: 100%;
  flex-direction: column;
  text-align: left;
  padding: 0;
  box-shadow: 0 8px 32px rgba(30, 58, 138, 0.3);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  animation: slideDown 0.3s ease-out;
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

.mobile-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-menu-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.close-menu-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.3s ease;
}

.close-menu-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.mobile-item {
  color: white;
  font-size: 16px;
  padding: 16px 30px;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.mobile-item:hover {
  background: rgba(255, 255, 255, 0.1);
  padding-left: 35px;
}

.mobile-icon {
  font-size: 18px;
  width: 20px;
  text-align: center;
}

.mobile-user-info {
  padding: 16px 30px;
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-user-role {
  color: white;
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.mobile-login {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 16px 30px;
  text-decoration: none;
  font-weight: 600;
  margin-top: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.mobile-login:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
}

.mobile-logout {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  padding: 16px 30px;
  text-decoration: none;
  font-weight: 600;
  margin-top: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  border: none;
  cursor: pointer;
  width: 100%;
  transition: all 0.3s ease;
}

.mobile-logout:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
}

/* Responsive */
@media (max-width: 768px) {
  .navbar-content {
    padding: 0 20px;
  }
  
  .logo {
    height: 40px;
  }
  
  .logo-text {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .navbar-content {
    padding: 0 16px;
  }
  
  .logo {
    height: 35px;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .mobile-menu-header {
    padding: 16px 20px;
  }
  
  .mobile-item {
    padding: 14px 20px;
  }
  
  .mobile-user-info {
    padding: 14px 20px;
  }
  
  .mobile-login,
  .mobile-logout {
    padding: 14px 20px;
  }
}

.sucursal-dropdown {
  display: flex;
  align-items: center;
}
.sucursal-select {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 14px;
}
.btn-sm {
  padding: 4px 10px;
  font-size: 13px;
}
</style>
