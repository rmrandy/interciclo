<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="reporteria-container">
    <h1 class="main-title">Reportería</h1>
    <div class="tabs">
      <button v-for="tab in tabs" :key="tab.key" :class="['tab', {active: activeTab === tab.key}]" @click="activeTab = tab.key">
        {{ tab.label }}
      </button>
    </div>
    <div class="tab-content">
      <div v-if="activeTab === 'ventas'">
        <h2>Reporte de Ventas</h2>
        <div class="filters">
          <label>Desde:
            <input type="date" v-model="fechaInicio" />
          </label>
          <label>Hasta:
            <input type="date" v-model="fechaFin" />
          </label>
          <label>Cliente:
            <input type="text" v-model="filtroCliente" placeholder="Nombre o email" />
          </label>
          <label>Estado:
            <select v-model="filtroEstado">
              <option value="">Todos</option>
              <option v-for="estado in estadosUnicos" :key="estado" :value="estado">{{ estado }}</option>
            </select>
          </label>
          <button class="btn-filtrar" @click="filtrarVentas">Filtrar</button>
          <button class="btn-limpiar" @click="limpiarFiltros">Limpiar</button>
        </div>
        <div class="tabla-wrapper">
          <table class="ventas-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Fecha</th>
                <th>Cliente</th>
                <th>Total</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="venta in ventasFiltradas" :key="venta.idOrder">
                <td>{{ venta.idOrder }}</td>
                <td>{{ formatearFecha(venta.createdAt) }}</td>
                <td>{{ venta.user?.name || venta.user?.email || 'N/A' }}</td>
                <td>Q{{ formatearTotal(venta.total) }}</td>
                <td>
                  <span :class="['estado', 'estado-' + (venta.status || '').toLowerCase().replace(/ /g, '-') ]">
                    {{ venta.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="ventasFiltradas.length === 0" class="no-data">No hay ventas en el rango/filtros seleccionados.</div>
      </div>
      <div v-if="activeTab === 'inventario'">
        <h2>Reporte de Inventario</h2>
        <div class="filters">
          <label>Producto:
            <input type="text" v-model="filtroProducto" placeholder="Buscar por nombre..." />
          </label>
          <button class="btn-limpiar" @click="limpiarFiltroInventario">Limpiar</button>
        </div>
        <div class="tabla-wrapper">
          <table class="inv-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Stock</th>
                <th>Precio</th>
                <th>Vendidos</th>
                <th>Marca</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="prod in productosFiltrados" :key="prod.idMedicine">
                <td>{{ prod.name }}</td>
                <td :class="{ 'stock-bajo': prod.stock < 10 }">{{ prod.stock }}</td>
                <td>Q{{ formatearTotal(prod.price) }}</td>
                <td>{{ prod.soldUnits }}</td>
                <td>{{ prod.brand }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="productosFiltrados.length === 0" class="no-data">No hay productos para mostrar.</div>
      </div>
      <div v-if="activeTab === 'pedidos'">
        <h2>Reporte de Pedidos</h2>
        <div class="filters">
          <label>Desde:
            <input type="date" v-model="fechaInicioPedido" />
          </label>
          <label>Hasta:
            <input type="date" v-model="fechaFinPedido" />
          </label>
          <label>Cliente:
            <input type="text" v-model="filtroClientePedido" placeholder="Nombre o email" />
          </label>
          <label>Estado:
            <select v-model="filtroEstadoPedido">
              <option value="">Todos</option>
              <option v-for="estado in estadosUnicosPedidos" :key="estado" :value="estado">{{ estado }}</option>
            </select>
          </label>
          <button class="btn-filtrar" @click="filtrarPedidos">Filtrar</button>
          <button class="btn-limpiar" @click="limpiarFiltrosPedidos">Limpiar</button>
        </div>
        <div class="tabla-wrapper">
          <table class="pedidos-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Fecha</th>
                <th>Cliente</th>
                <th>Total</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pedido in pedidosFiltrados" :key="pedido.idOrder">
                <td>{{ pedido.idOrder }}</td>
                <td>{{ formatearFecha(pedido.createdAt) }}</td>
                <td>{{ pedido.user?.name || pedido.user?.email || 'N/A' }}</td>
                <td>Q{{ formatearTotal(pedido.total) }}</td>
                <td>
                  <span :class="['estado', 'estado-' + (pedido.status || '').toLowerCase().replace(/ /g, '-') ]">
                    {{ pedido.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="pedidosFiltrados.length === 0" class="no-data">No hay pedidos para mostrar.</div>
      </div>
      <div v-if="activeTab === 'clientes'">
        <h2>Reporte de Clientes</h2>
        <div class="filters">
          <label>Buscar:
            <input type="text" v-model="filtroClienteGeneral" placeholder="Nombre o email" />
          </label>
          <button class="btn-limpiar" @click="limpiarFiltroClientes">Limpiar</button>
        </div>
        <div class="tabla-wrapper">
          <table class="clientes-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Cant. Pedidos</th>
                <th>Total Gastado</th>
                <th>Último Pedido</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(cliente, idx) in clientesFiltrados" :key="cliente.email">
                <td>{{ idx + 1 }}</td>
                <td>{{ cliente.name }}</td>
                <td>{{ cliente.email }}</td>
                <td>{{ cliente.cantidadPedidos }}</td>
                <td>Q{{ formatearTotal(cliente.totalGastado) }}</td>
                <td>{{ formatearFecha(cliente.ultimoPedido) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="clientesFiltrados.length === 0" class="no-data">No hay clientes para mostrar.</div>
      </div>
      <div v-if="activeTab === 'bestsellers'">
        <h2>Reporte de Productos Más Vendidos</h2>
        <div class="filters">
          <label>Producto:
            <input type="text" v-model="filtroBestSeller" placeholder="Buscar por nombre..." />
          </label>
          <button class="btn-limpiar" @click="limpiarFiltroBestSeller">Limpiar</button>
        </div>
        <div class="tabla-wrapper">
          <table class="bestsellers-table">
            <thead>
              <tr>
                <th @click="ordenarPor('name')">Nombre</th>
                <th @click="ordenarPor('soldUnits')">Vendidos</th>
                <th @click="ordenarPor('stock')">Stock</th>
                <th @click="ordenarPor('price')">Precio</th>
                <th>Marca</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="prod in productosPaginados" :key="prod.idMedicine">
                <td>{{ prod.name }}</td>
                <td>{{ prod.soldUnits }}</td>
                <td :class="{ 'stock-bajo': prod.stock < 10 }">{{ prod.stock }}</td>
                <td>Q{{ formatearTotal(prod.price) }}</td>
                <td>{{ prod.brand }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="paginacion">
          <button :disabled="paginaActual === 1" @click="paginaActual--">Anterior</button>
          <span>Página {{ paginaActual }} de {{ totalPaginas }}</span>
          <button :disabled="paginaActual === totalPaginas" @click="paginaActual++">Siguiente</button>
        </div>
        <div v-if="productosBestSellersFiltrados.length === 0" class="no-data">No hay productos para mostrar.</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import ApiService, { getPharmacyApiUrl } from '../services/ApiService';

const tabs = [
  { key: 'ventas', label: 'Ventas' },
  { key: 'inventario', label: 'Inventario' },
  { key: 'pedidos', label: 'Pedidos' },
  { key: 'clientes', label: 'Clientes' },
  { key: 'bestsellers', label: 'Más Vendidos' },
];
const activeTab = ref('ventas');
// Ventas
const ventas = ref([]);
const fechaInicio = ref('');
const fechaFin = ref('');
const filtroCliente = ref('');
const filtroEstado = ref('');
const fetchVentas = async () => {
  try {
    const url = getPharmacyApiUrl('orders');
    const data = await ApiService.get(url);
    ventas.value = Array.isArray(data) ? data : [];
  } catch (e) {
    ventas.value = [];
  }
};
onMounted(fetchVentas);
const estadosUnicos = computed(() => {
  const set = new Set(ventas.value.map(v => v.status).filter(Boolean));
  return Array.from(set);
});
const ventasFiltradas = computed(() => {
  let lista = [...ventas.value];
  // Ordenar por fecha descendente
  lista.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  // Filtros
  if (fechaInicio.value) {
    const desde = new Date(fechaInicio.value);
    lista = lista.filter(v => new Date(v.createdAt) >= desde);
  }
  if (fechaFin.value) {
    const hasta = new Date(fechaFin.value);
    lista = lista.filter(v => new Date(v.createdAt) <= hasta);
  }
  if (filtroCliente.value) {
    const f = filtroCliente.value.toLowerCase();
    lista = lista.filter(v => (v.user?.name || '').toLowerCase().includes(f) || (v.user?.email || '').toLowerCase().includes(f));
  }
  if (filtroEstado.value) {
    lista = lista.filter(v => v.status === filtroEstado.value);
  }
  return lista;
});
function filtrarVentas() {
  // Reactivo, no hace falta lógica extra
}
function limpiarFiltros() {
  fechaInicio.value = '';
  fechaFin.value = '';
  filtroCliente.value = '';
  filtroEstado.value = '';
}
function formatearFecha(fecha) {
  if (!fecha) return '';
  const d = new Date(fecha);
  return d.toLocaleDateString('es-ES', { year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}
function formatearTotal(total) {
  if (typeof total === 'number') return total.toFixed(2);
  if (!total) return '0.00';
  const num = Number(total);
  return isNaN(num) ? '0.00' : num.toFixed(2);
}
// Inventario
const productos = ref([]);
const filtroProducto = ref('');
const fetchProductos = async () => {
  try {
    const url = getPharmacyApiUrl('medicines');
    const data = await ApiService.get(url);
    productos.value = Array.isArray(data) ? data : [];
  } catch (e) {
    productos.value = [];
  }
};
onMounted(fetchProductos);
const productosFiltrados = computed(() => {
  if (!filtroProducto.value) return productos.value;
  return productos.value.filter(p => p.name?.toLowerCase().includes(filtroProducto.value.toLowerCase()));
});
function limpiarFiltroInventario() {
  filtroProducto.value = '';
}
// Pedidos
const pedidos = ref([]);
const fechaInicioPedido = ref('');
const fechaFinPedido = ref('');
const filtroClientePedido = ref('');
const filtroEstadoPedido = ref('');
const fetchPedidos = async () => {
  try {
    const url = getPharmacyApiUrl('orders');
    const data = await ApiService.get(url);
    pedidos.value = Array.isArray(data) ? data : [];
  } catch (e) {
    pedidos.value = [];
  }
};
onMounted(fetchPedidos);
const estadosUnicosPedidos = computed(() => {
  const set = new Set(pedidos.value.map(v => v.status).filter(Boolean));
  return Array.from(set);
});
const pedidosFiltrados = computed(() => {
  let lista = [...pedidos.value];
  lista.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  if (fechaInicioPedido.value) {
    const desde = new Date(fechaInicioPedido.value);
    lista = lista.filter(v => new Date(v.createdAt) >= desde);
  }
  if (fechaFinPedido.value) {
    const hasta = new Date(fechaFinPedido.value);
    lista = lista.filter(v => new Date(v.createdAt) <= hasta);
  }
  if (filtroClientePedido.value) {
    const f = filtroClientePedido.value.toLowerCase();
    lista = lista.filter(v => (v.user?.name || '').toLowerCase().includes(f) || (v.user?.email || '').toLowerCase().includes(f));
  }
  if (filtroEstadoPedido.value) {
    lista = lista.filter(v => v.status === filtroEstadoPedido.value);
  }
  return lista;
});
function filtrarPedidos() {}
function limpiarFiltrosPedidos() {
  filtroClientePedido.value = '';
  filtroEstadoPedido.value = '';
  fechaInicioPedido.value = '';
  fechaFinPedido.value = '';
}
// Clientes
const filtroClienteGeneral = ref('');
const clientesAgrupados = computed(() => {
  // Agrupar pedidos por cliente (usando pedidos.value)
  const mapa = new Map();
  pedidos.value.forEach(ped => {
    const email = ped.user?.email || 'N/A';
    const name = ped.user?.name || 'N/A';
    if (!mapa.has(email)) {
      mapa.set(email, {
        name,
        email,
        cantidadPedidos: 0,
        totalGastado: 0,
        ultimoPedido: null
      });
    }
    const cli = mapa.get(email);
    cli.cantidadPedidos++;
    cli.totalGastado += Number(ped.total) || 0;
    if (!cli.ultimoPedido || new Date(ped.createdAt) > new Date(cli.ultimoPedido)) {
      cli.ultimoPedido = ped.createdAt;
    }
  });
  // Convertir a array y ordenar por total gastado descendente
  return Array.from(mapa.values()).sort((a, b) => b.totalGastado - a.totalGastado);
});
const clientesFiltrados = computed(() => {
  if (!filtroClienteGeneral.value) return clientesAgrupados.value;
  const f = filtroClienteGeneral.value.toLowerCase();
  return clientesAgrupados.value.filter(cli =>
    cli.name.toLowerCase().includes(f) || cli.email.toLowerCase().includes(f)
  );
});
function limpiarFiltroClientes() {
  filtroClienteGeneral.value = '';
}
// Más Vendidos
const productosBestSellers = ref([]);
const filtroBestSeller = ref('');
const sortKey = ref('soldUnits');
const sortDesc = ref(true);
const paginaActual = ref(1);
const itemsPorPagina = 5;
const fetchBestSellers = async () => {
  try {
    const url = getPharmacyApiUrl('medicines/bestsellers?count=20');
    const data = await ApiService.get(url);
    productosBestSellers.value = Array.isArray(data) ? data : [];
  } catch (e) {
    productosBestSellers.value = [];
  }
};
onMounted(() => {
  fetchVentas();
  fetchProductos();
  fetchPedidos();
  fetchBestSellers();
});
const productosBestSellersFiltrados = computed(() => {
  let lista = [...productosBestSellers.value];
  if (filtroBestSeller.value) {
    lista = lista.filter(p => p.name?.toLowerCase().includes(filtroBestSeller.value.toLowerCase()));
  }
  lista.sort((a, b) => {
    if (sortKey.value === 'name') {
      return sortDesc.value ? b.name.localeCompare(a.name) : a.name.localeCompare(b.name);
    } else {
      return sortDesc.value ? (b[sortKey.value] - a[sortKey.value]) : (a[sortKey.value] - b[sortKey.value]);
    }
  });
  return lista;
});
const totalPaginas = computed(() => Math.ceil(productosBestSellersFiltrados.value.length / itemsPorPagina));
const productosPaginados = computed(() => {
  const start = (paginaActual.value - 1) * itemsPorPagina;
  return productosBestSellersFiltrados.value.slice(start, start + itemsPorPagina);
});
function limpiarFiltroBestSeller() {
  filtroBestSeller.value = '';
  paginaActual.value = 1;
}
function ordenarPor(key) {
  if (sortKey.value === key) {
    sortDesc.value = !sortDesc.value;
  } else {
    sortKey.value = key;
    sortDesc.value = true;
  }
}
</script>

<style scoped>
.reporteria-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(30,58,138,0.08);
  min-height: 80vh;
}
.main-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 2rem;
  text-align: center;
}
.tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  justify-content: center;
}
.tab {
  padding: 0.7rem 2rem;
  border: none;
  border-radius: 8px 8px 0 0;
  background: #f1f5f9;
  color: #1e293b;
  font-weight: 600;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background 0.2s;
}
.tab.active {
  background: #2563eb;
  color: #fff;
}
.tab-content {
  background: #f8fafc;
  border-radius: 0 0 12px 12px;
  padding: 2rem;
  min-height: 400px;
}
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
  align-items: center;
  background: #f1f5f9;
  padding: 1rem 1.5rem;
  border-radius: 10px;
}
.filters label {
  font-weight: 500;
  color: #1e293b;
  display: flex;
  flex-direction: column;
  font-size: 0.98rem;
}
.filters input, .filters select {
  margin-top: 4px;
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  font-size: 1rem;
}
.btn-filtrar, .btn-limpiar {
  padding: 8px 18px;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  margin-left: 8px;
  transition: background 0.2s;
}
.btn-filtrar {
  background: #2563eb;
  color: #fff;
}
.btn-filtrar:hover {
  background: #1e40af;
}
.btn-limpiar {
  background: #e5e7eb;
  color: #1e293b;
}
.btn-limpiar:hover {
  background: #cbd5e1;
}
.tabla-wrapper {
  overflow-x: auto;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(30,58,138,0.04);
}
.ventas-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
  font-size: 1rem;
}
.ventas-table th, .ventas-table td {
  border: 1px solid #e5e7eb;
  padding: 10px 14px;
  text-align: left;
}
.ventas-table th {
  background: #f1f5f9;
  position: sticky;
  top: 0;
  z-index: 1;
}
.ventas-table tbody tr:nth-child(even) {
  background: #f8fafc;
}
.ventas-table tbody tr:hover {
  background: #e0e7ff;
}
.estado {
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.98rem;
  text-transform: capitalize;
  background: #e0e7ff;
  color: #1e40af;
  display: inline-block;
}
.estado-completado, .estado-pagado {
  background: #bbf7d0;
  color: #166534;
}
.estado-pending, .estado-pendiente {
  background: #fef08a;
  color: #92400e;
}
.estado-enviado, .estado-recibido {
  background: #bae6fd;
  color: #0369a1;
}
.estado-en-curso {
  background: #fca5a5;
  color: #991b1b;
}
.no-data {
  color: #64748b;
  text-align: center;
  margin-top: 1rem;
}
.inv-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
  font-size: 1rem;
}
.inv-table th, .inv-table td {
  border: 1px solid #e5e7eb;
  padding: 10px 14px;
  text-align: left;
}
.inv-table th {
  background: #f1f5f9;
  position: sticky;
  top: 0;
  z-index: 1;
}
.inv-table tbody tr:nth-child(even) {
  background: #f8fafc;
}
.inv-table tbody tr:hover {
  background: #e0e7ff;
}
.stock-bajo {
  color: #dc2626;
  font-weight: bold;
  background: #fee2e2;
}
.pedidos-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
  font-size: 1rem;
}
.pedidos-table th, .pedidos-table td {
  border: 1px solid #e5e7eb;
  padding: 10px 14px;
  text-align: left;
}
.pedidos-table th {
  background: #f1f5f9;
  position: sticky;
  top: 0;
  z-index: 1;
}
.pedidos-table tbody tr:nth-child(even) {
  background: #f8fafc;
}
.pedidos-table tbody tr:hover {
  background: #e0e7ff;
}
.clientes-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
  font-size: 1rem;
}
.clientes-table th, .clientes-table td {
  border: 1px solid #e5e7eb;
  padding: 10px 14px;
  text-align: left;
}
.clientes-table th {
  background: #f1f5f9;
  position: sticky;
  top: 0;
  z-index: 1;
}
.clientes-table tbody tr:nth-child(even) {
  background: #f8fafc;
}
.clientes-table tbody tr:hover {
  background: #e0e7ff;
}
.bestsellers-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
  font-size: 1rem;
}
.bestsellers-table th, .bestsellers-table td {
  border: 1px solid #e5e7eb;
  padding: 10px 14px;
  text-align: left;
  cursor: pointer;
}
.bestsellers-table th {
  background: #f1f5f9;
  position: sticky;
  top: 0;
  z-index: 1;
  user-select: none;
}
.bestsellers-table tbody tr:nth-child(even) {
  background: #f8fafc;
}
.bestsellers-table tbody tr:hover {
  background: #e0e7ff;
}
.grafico-wrapper {
  max-width: 700px;
  margin: 0 auto 2rem auto;
  background: #f8fafc;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(30,58,138,0.08);
  padding: 2.5rem 2rem 2rem 2rem;
  border: 1.5px solid #e0e7ff;
}
.paginacion {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}
.paginacion button {
  padding: 6px 16px;
  border-radius: 6px;
  border: none;
  background: #2563eb;
  color: #fff;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}
.paginacion button:disabled {
  background: #cbd5e1;
  color: #64748b;
  cursor: not-allowed;
}
</style> 