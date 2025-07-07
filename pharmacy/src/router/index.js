import { createRouter, createWebHistory } from 'vue-router';
import Home from '../pages/Home.vue'; // Ajustar rutas relativas
import Login from '../pages/Login.vue';
import Dashboard from '../pages/Dashboard.vue';
import Register from '../pages/Register.vue';
import Catalogo from '../pages/Catalogo.vue';
import Aseguradoras from '@/pages/Aseguradoras.vue';
import Ofertas from '@/pages/Ofertas.vue';
import ProductoDetalle from '@/pages/ProductoDetalle.vue'; // new import
import VerificarCompra from '@/pages/VerificarCompra.vue'; // nueva página de verificación
import Receta from '@/components/Receta.vue';
import CreateProduct from '@/pages/CreateProduct.vue';
import Prescriptions from '@/pages/Prescriptions.vue';
import { authService } from '@/services/authService';
import Cart from "@/components/Cart.vue";
import AdminDash from '@/pages/AdminDash.vue'; // Importar el dashboard administrativo
import DetalleReceta from '@/pages/DetalleReceta.vue'
import Checkout from '../pages/Checkout.vue';
import Categoria from '../pages/Categoria.vue'; // Nueva página de categoría
import Perfil from '../pages/Perfil.vue'; // Nueva página de perfil
import Gracias from '../pages/Gracias.vue'; // Nueva página de gracias

// Rutas para usuarios públicos y autenticados
const userRoutes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/catalogo', component: Catalogo },
  { path: '/categoria/:category', name: 'Categoria', component: Categoria }, // Nueva ruta de categoría
  { path: '/aseguradoras', component: Aseguradoras },
  { path: '/ofertas', component: Ofertas },
  { path: '/producto/:id', name: 'ProductoDetalle', component: ProductoDetalle },
  { path: '/verificar-compra/:id', name: 'VerificarCompra', component: VerificarCompra, meta: { requiresAuth: true } },
  { path: '/receta', component: Receta },
  { path: '/create-product', name: 'CreateProduct', component: CreateProduct },
  { path: '/prescriptions', name: 'Prescriptions', component: Prescriptions },
  { path: '/dashboard', component: Dashboard, meta: { requiresAuth: true } },
  { path: '/perfil', name: 'Perfil', component: Perfil, meta: { requiresAuth: true } }, // Nueva ruta de perfil
  { path: '/gracias', name: 'Gracias', component: Gracias, meta: { requiresAuth: true } },
  {path: '/cart', name: 'Cart', component: Cart},
  {
    path: '/detalle-receta/:id',
    name: 'DetalleReceta',
    component: DetalleReceta,
    meta: { requiresAuth: true }
  },
  {
    path: '/gestion-productos',
    name: 'GestionProductos',
    component: () => import('../pages/GestionProductos.vue'),
    meta: { requiresAuth: true, allowedRoles: ['admin', 'employee'] }
  },
  { path: '/checkout', name: 'Checkout', component: Checkout, meta: { requiresAuth: true } },
  {
    path: '/catalogo-internacional',
    name: 'CatalogoInternacional',
    component: () => import('../pages/CatalogoInternacional.vue')
  },
  {
    path: '/producto-internacional/:id',
    name: 'ProductoInternacional',
    component: () => import('../pages/ProductoInternacional.vue')
  },
  {
    path: '/mis-pedidos',
    name: 'MisPedidos',
    component: () => import('@/pages/MisPedidos.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checkout-internacional',
    name: 'CheckoutInternacional',
    component: () => import('@/pages/CheckoutInternacional.vue'),
    meta: { requiresAuth: false }
  },
  { path: '/reporteria', name: 'Reporteria', component: () => import('../pages/ReporteriaPage.vue') },
];

// Rutas exclusivas para administradores
const adminRoutes = [
  {
    path: '/admin/create-product',
    name: 'AdminCreateProduct',
    component: CreateProduct,
    meta: { admin: true }
  },
  {
    path: '/admindash',
    name: 'AdminDash',
    component: AdminDash,
    meta: { admin: true }
  },
  {
    path: '/admin/pedidos',
    name: 'AdminPedidos',
    component: () => import('../pages/admin/Pedidos.vue'),
    meta: { admin: true }
  },
  {
    path: '/admin/site-content',
    name: 'SiteContent',
    component: () => import('../pages/admin/site-content.vue'),
    meta: { admin: true }
  },
  {
    path: '/admin/puertos',
    name: 'AdminPuertos',
    component: () => import('../pages/admin/puertos.vue'),
    meta: { admin: true }
  },
];

const routes = [
  ...userRoutes,
  ...adminRoutes
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const adminOnly = to.matched.some(record => record.meta.admin);
  
  // Intentar obtener el usuario del servicio de autenticación
  const currentUser = authService.getCurrentUser();
  console.log("Router: Usuario actual:", currentUser);
  
  // Para rutas administrativas, verificar también en localStorage
  if (adminOnly) {
    console.log("Ruta protegida para admin:", to.path);
    if (currentUser && ['admin','administrador','employee','empleado'].includes(currentUser.role)) {
      console.log("Acceso permitido por authService");
      return next();
    }
    try {
      const storedRole = localStorage.getItem('role');
      if (['admin','administrador','employee','empleado'].includes(storedRole)) {
        console.log("Acceso permitido por localStorage.role");
        return next();
      }
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        const user = JSON.parse(storedUser);
        if (['admin','administrador','employee','empleado'].includes(user.role)) {
          console.log("Acceso permitido por localStorage.user");
          return next();
        }
      }
      const storedSession = localStorage.getItem('session');
      if (storedSession) {
        const session = JSON.parse(storedSession);
        if (['admin','administrador','employee','empleado'].includes(session.role)) {
          console.log("Acceso permitido por localStorage.session");
          return next();
        }
      }
    } catch (e) {
      console.error("Error verificando el rol en localStorage:", e);
    }
    console.log("Acceso denegado a ruta admin");
    return next('/');
  }
  
  // Para rutas que requieren autenticación (no admin)
  if (requiresAuth && !currentUser) {
    console.log("Ruta requiere autenticación - redirigiendo a login");
    return next('/login');
  }
  
  // Acceso permitido para otras rutas
  return next();
});

export default router;