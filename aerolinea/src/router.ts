import { createMemoryHistory, createRouter, createWebHistory } from "vue-router";
import type { RouteLocationNormalized, NavigationGuardNext } from "vue-router";
import Login from "./pages/login.vue";
import Home from "./pages/home.vue";
import Register from "./pages/register.vue";
import AdminUsers from "./pages/admin/users.vue";
import InsuranceServices from "./pages/admin/insurance-services.vue";
import HospitalServices from "./pages/admin/hospital-services.vue";
import HospitalServicesImport from "./pages/admin/hospital-services-import.vue";
import HospitalConfiguration from "./pages/admin/hospital-configuration.vue";
import FlightManagement from "./components/FlightManagement.vue";
import ProfileCompletion from "./pages/profile-completion.vue";
import CatalogInsuranceServices from "./pages/catalog/insurance-services.vue";
import CatalogHospitals from "./pages/catalog/hospitals.vue";
import CatalogHospitalServices from "./pages/catalog/hospital-services.vue";
import Policies from "./pages/admin/policies.vue";
import RegisterClient from "./pages/employee/register-client.vue";
import ClientManagement from "./pages/admin/client-management.vue";
import SystemConfiguration from "./pages/admin/system-configuration.vue";
import SiteSettings from "./pages/admin/site-settings.vue";
import Analytics from "./pages/admin/analytics.vue";
import AircraftSeatConfig from "./pages/admin/aircraft-seat-config.vue";
import UserServices from "./pages/user-services.vue";
import { checkMissingRequiredFields } from "./utils/profile-utils";


const requireAuth = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  
  if (!profile || profile === "null") {
    // No autenticado, redirigir a login con parámetro de retorno
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`);
  } else {
    // Usuario autenticado, continuar (sin verificar activación)
    next();
  }
};

const requireAdmin = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  console.log("Profile en requireAdmin:", profile);
  
  if (!profile || profile === "null") {
    // No autenticado, redirigir a login
    console.log("No autenticado, redirigiendo a login");
    next('/login');
    return;
  } 
  
  console.log("Role:", profile.role);
  
  // Verificar si es admin (aceptar mayúsculas/minúsculas)
  const role = String(profile.role || '').toUpperCase();
  if (role !== "ADMIN") {
    // No es administrador, redirigir a home
    console.log("No es admin, redirigiendo a home");
    next('/home');
    return;
  }
  
  // Es administrador, continuar
  console.log("Es admin, continuando");
  next();
};


const requireCompleteProfile = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  
  if (!profile || profile === "null") {
    next('/login');
    return;
  }
  
  // Ya no verificamos campos faltantes en el perfil
  // Permitir acceso a todas las rutas independientemente del estado del perfil
  next();
  
  /* CÓDIGO ORIGINAL COMENTADO
  const missingFields = checkMissingRequiredFields(profile);
  
  if (missingFields) {
    // Solo permitir acceso a la ruta de completar perfil
    if (to.path !== '/profile-completion') {
      next('/profile-completion');
    } else {
      next();
    }
  } else {
    next(); // Perfil completo, continuar
  }
  */
};

// Middleware para usuarios inactivos
const inactiveUserOnly = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  
  if (!profile || profile === "null") {
    next('/login');
  } else if (profile.enabled === 1) {
    next('/home');
  } else {
    next(); // Usuario inactivo, puede ver la página
  }
};

// Middleware para requerir rol de empleado o admin
const requireEmployeeOrAdmin = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const user = JSON.parse(localStorage.getItem("user") || "null");
  if (!user) {
    next('/login');
  } else {
    const role = String(user.role || '').toUpperCase();
    if (role === 'EMPLOYEE' || role === 'ADMIN') {
    next();
    } else {
      next('/home');
    }
  }
};

const routes = [
  { path: "/", redirect: "/home" },
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { 
    path: "/home", 
    component: Home 
    // Sin requireAuth - página pública
  },
  {
    path: "/admin/users",
    component: AdminUsers,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/insurance-services",
    component: InsuranceServices,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/hospital-services",
    component: HospitalServices,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/hospital-services-import",
    component: HospitalServicesImport,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/hospital-configuration",
    component: HospitalConfiguration,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/policies",
    component: Policies,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/analytics",
    component: Analytics,
    beforeEnter: requireAdmin
  },
  // Gestión completa de vuelos (admin)
  {
    path: "/admin/flight-management",
    component: FlightManagement,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/aircraft-seat-config",
    component: AircraftSeatConfig,
    beforeEnter: requireAdmin
  },
  // Gestión de ciudades (admin)
  {
    path: "/admin/flight-operations",
    component: () => import('./pages/admin/flight-operations/cities.vue'),
    beforeEnter: requireAdmin
  },
  // Gestión de vuelos (admin)
  {
    path: "/admin/flight-schedule",
    component: () => import('./pages/admin/flight-operations/flight-schedule.vue'),
    beforeEnter: requireAdmin
  },
  {
    path: '/admin/client-management',
    component: ClientManagement,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/purchases",
    component: () => import('./pages/admin/bookings.vue'),
    beforeEnter: requireAdmin
  },

  {
    path: '/admin/configuration',
    component: SystemConfiguration,
    beforeEnter: requireEmployeeOrAdmin // Permitir a empleados y admins
  },
  {
    path: '/admin/site-settings',
    component: SiteSettings,
    beforeEnter: requireAdmin
  },
  {
    path: '/admin/informative-pages',
    component: () => import('./pages/admin/informative-pages.vue'),
    beforeEnter: requireAdmin
  },
  {
    path: "/profile-completion",
    component: ProfileCompletion,
    beforeEnter: requireCompleteProfile
  },
  {
    path: "/inactive-account",
    component: () => import('./pages/inactive-account.vue'),
    beforeEnter: inactiveUserOnly
  },
  // Nuevas rutas de catálogo
  {
    path: "/catalog/insurance-services",
    component: CatalogInsuranceServices,
    beforeEnter: requireAuth
  },
  {
    path: "/catalog/hospitals",
    component: CatalogHospitals,
    beforeEnter: requireAuth
  },
  {
    path: "/catalog/hospital-services",
    component: CatalogHospitalServices,
    beforeEnter: requireAuth
  },
  {
    path: '/employee/register-client',
    component: RegisterClient,
    beforeEnter: requireEmployeeOrAdmin
  },

  {
    path: '/user-services',
    component: UserServices,
    beforeEnter: requireAuth
  },
  // Rutas públicas para consulta de vuelos
  {
    path: '/flights',
    component: () => import('./pages/flights.vue')
    // Pública - no requiere autenticación
  },
  {
    path: '/flight-details/:id',
    component: () => import('./pages/flight-details.vue')
    // Pública - no requiere autenticación
  },
  // Rutas protegidas para compra
  {
    path: '/flight/:id/book',
    component: () => import('./pages/book-flight.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/cart',
    component: () => import('./pages/cart.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/checkout',
    component: () => import('./pages/checkout.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/booking/confirmation/:bookingId',
    component: () => import('./pages/booking-confirmation.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/my-bookings',
    component: () => import('./pages/my-bookings.vue'),
    beforeEnter: requireAuth
  }
  ,  {
    path: '/reservation/:id',
    component: () => import('./pages/reservation-detail.vue'),
    beforeEnter: requireAuth
  },
  // Páginas informativas (públicas)
  {
    path: '/informative/seat-types',
    component: () => import('./pages/informative/seat-types.vue')
    // Pública - no requiere autenticación
  },
  {
    path: '/informative/boarding-instructions',
    component: () => import('./pages/informative/boarding-instructions.vue')
    // Pública - no requiere autenticación
  },
  {
    path: '/informative/checkin-process',
    component: () => import('./pages/informative/checkin-process.vue')
    // Pública - no requiere autenticación
  },
  {
    path: '/informative/baggage-info',
    component: () => import('./pages/informative/baggage-info.vue')
    // Pública - no requiere autenticación
  },
  {
    path: '/informative/travel-tips',
    component: () => import('./pages/informative/travel-tips.vue')
    // Pública - no requiere autenticación
  },
  {
    path: '/informative/contact',
    component: () => import('./pages/informative/contact.vue')
    // Pública - no requiere autenticación
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
