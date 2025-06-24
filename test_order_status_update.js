const axios = require('axios');

// Configuración
const BASE_URL = 'http://localhost:8080/api2';

async function testOrderStatusUpdate() {
  try {
    console.log('🧪 Probando actualización de estado de pedidos...\n');

    // 1. Primero, obtener todos los pedidos para ver si hay alguno
    console.log('1. Obteniendo lista de pedidos...');
    const ordersResponse = await axios.get(`${BASE_URL}/orders`);
    const orders = ordersResponse.data;
    
    if (!orders || orders.length === 0) {
      console.log('❌ No hay pedidos disponibles para probar');
      return;
    }

    const testOrder = orders[0];
    console.log(`✅ Pedido encontrado: ID=${testOrder.idOrder}, Estado actual=${testOrder.status}`);

    // 2. Probar actualización de estado
    console.log('\n2. Probando actualización de estado...');
    const newStatus = testOrder.status === 'recibido' ? 'enviado' : 'recibido';
    
    const updateResponse = await axios.put(`${BASE_URL}/orders/${testOrder.idOrder}/status`, {
      status: newStatus
    });

    console.log(`✅ Estado actualizado exitosamente:`);
    console.log(`   - Pedido ID: ${updateResponse.data.idOrder}`);
    console.log(`   - Nuevo estado: ${updateResponse.data.status}`);
    console.log(`   - Última actualización: ${updateResponse.data.updatedAt}`);

    // 3. Verificar que el cambio se aplicó
    console.log('\n3. Verificando que el cambio se aplicó...');
    const verifyResponse = await axios.get(`${BASE_URL}/orders?id=${testOrder.idOrder}`);
    const updatedOrder = verifyResponse.data;
    
    if (updatedOrder.status === newStatus) {
      console.log('✅ Verificación exitosa: el estado se actualizó correctamente');
    } else {
      console.log('❌ Error: el estado no se actualizó correctamente');
    }

  } catch (error) {
    console.error('❌ Error durante la prueba:', error.message);
    if (error.response) {
      console.error('   Status:', error.response.status);
      console.error('   Data:', error.response.data);
    }
  }
}

// Ejecutar la prueba
testOrderStatusUpdate(); 