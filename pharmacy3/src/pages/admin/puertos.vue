<template>
  <div class="admin-puertos">
    <h1>Gestión de Sucursales y Puertos</h1>
    <table class="sucursales-table">
      <thead>
        <tr>
          <th>Nombre de Sucursal</th>
          <th>Puerto</th>
          <th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(sucursal, idx) in sucursales" :key="idx">
          <td>
            <input v-model="sucursal.nombre" class="input-nombre" />
          </td>
          <td>
            <input v-model="sucursal.puerto" class="input-puerto" />
          </td>
          <td>
            <button @click="eliminarSucursal(idx)" class="btn btn-danger">Eliminar</button>
          </td>
        </tr>
      </tbody>
    </table>
    <button @click="agregarSucursal" class="btn btn-primary">Agregar Sucursal</button>
    <button @click="guardarCambios" class="btn btn-success">Guardar Cambios</button>
    <div v-if="mensaje" class="mensaje">{{ mensaje }}</div>
  </div>
</template>

<script>
import eventBus from '@/eventBus';
export default {
  name: 'AdminPuertos',
  data() {
    return {
      sucursales: [],
      mensaje: ''
    };
  },
  created() {
    this.cargarSucursales();
  },
  methods: {
    cargarSucursales() {
      const saved = localStorage.getItem('sucursalesPharmacy');
      this.sucursales = saved ? JSON.parse(saved) : [
        { nombre: 'Sucursal 1', puerto: '8080' },
        { nombre: 'Sucursal 2', puerto: '8081' },
      ];
    },
    agregarSucursal() {
      this.sucursales.push({ nombre: '', puerto: '' });
    },
    eliminarSucursal(idx) {
      this.sucursales.splice(idx, 1);
    },
    guardarCambios() {
      // Validar que no haya campos vacíos
      for (const suc of this.sucursales) {
        if (!suc.nombre || !suc.puerto) {
          this.mensaje = 'Todos los campos son obligatorios.';
          return;
        }
      }
      localStorage.setItem('sucursalesPharmacy', JSON.stringify(this.sucursales));
      this.mensaje = '¡Cambios guardados!';
      eventBus.emit('sucursales-actualizadas');
    }
  }
};
</script>

<style scoped>
.admin-puertos {
  max-width: 600px;
  margin: 40px auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(30,58,138,0.08);
  padding: 32px;
}
.sucursales-table {
  width: 100%;
  margin-bottom: 20px;
  border-collapse: collapse;
}
.sucursales-table th, .sucursales-table td {
  border: 1px solid #eee;
  padding: 8px 12px;
  text-align: left;
}
.input-nombre, .input-puerto {
  width: 100%;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
.btn {
  margin-right: 8px;
  margin-top: 8px;
}
.btn-danger {
  background: #ef4444;
  color: #fff;
}
.btn-primary {
  background: #2563eb;
  color: #fff;
}
.btn-success {
  background: #10b981;
  color: #fff;
}
.mensaje {
  margin-top: 16px;
  color: #10b981;
  font-weight: 500;
}
</style> 