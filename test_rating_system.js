const axios = require('axios');

// Configuración
const API_BASE_URL = 'http://localhost:8080/api2';

// Configurar axios con timeout
axios.defaults.timeout = 10000;

// Función para probar el sistema de reseñas
async function testRatingSystem() {
  console.log('🧪 Probando el sistema de reseñas...\n');

  try {
    // 1. Obtener todos los comentarios existentes
    console.log('1. Obteniendo comentarios existentes...');
    console.log(`   URL: ${API_BASE_URL}/comments`);
    
    const commentsResponse = await axios.get(`${API_BASE_URL}/comments`);
    console.log(`   ✅ Comentarios obtenidos: ${commentsResponse.data.length}`);
    
    // 2. Obtener medicamentos para probar
    console.log('\n2. Obteniendo medicamentos...');
    console.log(`   URL: ${API_BASE_URL}/medicines`);
    
    const medicinesResponse = await axios.get(`${API_BASE_URL}/medicines`);
    const medicines = medicinesResponse.data;
    console.log(`   ✅ Medicamentos obtenidos: ${medicines.length}`);
    
    if (medicines.length === 0) {
      console.log('   ⚠️  No hay medicamentos disponibles para probar');
      return;
    }
    
    // 3. Obtener usuarios para probar
    console.log('\n3. Obteniendo usuarios...');
    console.log(`   URL: ${API_BASE_URL}/users`);
    
    const usersResponse = await axios.get(`${API_BASE_URL}/users`);
    const users = usersResponse.data;
    console.log(`   ✅ Usuarios obtenidos: ${users.length}`);
    
    if (users.length === 0) {
      console.log('   ⚠️  No hay usuarios disponibles para probar');
      return;
    }
    
    // 4. Crear una reseña de prueba
    console.log('\n4. Creando reseña de prueba...');
    const testMedicine = medicines[0];
    const testUser = users[0];
    
    const ratingData = {
      user: testUser,
      commentText: "¡Excelente producto! Muy efectivo.",
      rating: 5,
      medicine: { idMedicine: testMedicine.idMedicine }
    };
    
    console.log(`   Datos de la reseña:`, JSON.stringify(ratingData, null, 2));
    
    const ratingResponse = await axios.post(`${API_BASE_URL}/comments`, ratingData);
    console.log(`   ✅ Reseña creada exitosamente con ID: ${ratingResponse.data.idComments}`);
    
    // 5. Verificar que la reseña se guardó correctamente
    console.log('\n5. Verificando reseña guardada...');
    const updatedCommentsResponse = await axios.get(`${API_BASE_URL}/comments`);
    const newComment = updatedCommentsResponse.data.find(c => c.idComments === ratingResponse.data.idComments);
    
    if (newComment) {
      console.log(`   ✅ Reseña verificada:`);
      console.log(`      - Usuario: ${newComment.user.name}`);
      console.log(`      - Rating: ${newComment.rating} estrellas`);
      console.log(`      - Comentario: ${newComment.commentText}`);
      console.log(`      - Medicamento: ${newComment.medicine.name}`);
    } else {
      console.log('   ❌ No se pudo encontrar la reseña creada');
    }
    
    // 6. Verificar que el rating del medicamento se actualizó
    console.log('\n6. Verificando actualización del rating del medicamento...');
    const updatedMedicinesResponse = await axios.get(`${API_BASE_URL}/medicines`);
    const updatedMedicine = updatedMedicinesResponse.data.find(m => m.idMedicine === testMedicine.idMedicine);
    
    if (updatedMedicine) {
      console.log(`   ✅ Rating del medicamento actualizado:`);
      console.log(`      - Rating promedio: ${updatedMedicine.averageRating}`);
      console.log(`      - Número de calificaciones: ${updatedMedicine.ratingCount}`);
    } else {
      console.log('   ❌ No se pudo encontrar el medicamento actualizado');
    }
    
    console.log('\n🎉 ¡Prueba del sistema de reseñas completada exitosamente!');
    
  } catch (error) {
    console.error('\n❌ Error durante la prueba:', error.message);
    if (error.response) {
      console.error('   Status:', error.response.status);
      console.error('   Status Text:', error.response.statusText);
      console.error('   Headers:', error.response.headers);
      console.error('   Data:', error.response.data);
    } else if (error.request) {
      console.error('   No se recibió respuesta del servidor');
      console.error('   Request:', error.request);
    } else {
      console.error('   Error de configuración:', error.message);
    }
  }
}

// Ejecutar la prueba
testRatingSystem(); 