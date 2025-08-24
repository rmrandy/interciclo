// Script de prueba para la API de vuelos
const API_BASE = 'http://192.168.0.8:8080';

// Función para hacer peticiones HTTP
async function makeRequest(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

// Función para probar la API de vuelos
async function testFlightAPI() {
    console.log('🧪 Iniciando pruebas de la API de vuelos...\n');

    try {
        // 1. Probar obtener vuelos existentes
        console.log('1️⃣ Probando GET /api/airline/flights...');
        const flights = await makeRequest(`${API_BASE}/api/airline/flights`);
        console.log('✅ Vuelos obtenidos:', flights);
        console.log('');

        // 2. Probar crear un nuevo vuelo
        console.log('2️⃣ Probando POST /api/airline/flights...');
        const newFlight = {
            flightNumber: "AE001",
            originCityId: 1,
            destinationCityId: 2,
            departureDate: "2024-01-15",
            departureTime: "08:00:00",
            arrivalDate: "2024-01-15",
            arrivalTime: "10:30:00",
            createdBy: 1,
            inventory: {
                ECONOMY: 150,
                BUSINESS: 20,
                FIRST_CLASS: 10
            },
            fares: {
                ECONOMY: 150.00,
                BUSINESS: 450.00,
                FIRST_CLASS: 800.00
            },
            legs: [
                {
                    cityId: 3,
                    legOrder: 1,
                    arrivalTime: "2024-01-15T09:15:00",
                    departureTime: "2024-01-15T09:45:00",
                    aircraftChange: false
                }
            ]
        };

        const createdFlight = await makeRequest(`${API_BASE}/api/airline/flights`, {
            method: 'POST',
            body: JSON.stringify(newFlight),
        });
        console.log('✅ Vuelo creado:', createdFlight);
        console.log('');

        // 3. Probar obtener el vuelo creado
        if (createdFlight.flightId) {
            console.log('3️⃣ Probando GET /api/airline/flights/{id}...');
            const flight = await makeRequest(`${API_BASE}/api/airline/flights/${createdFlight.flightId}`);
            console.log('✅ Vuelo obtenido:', flight);
            console.log('');

            // 4. Probar actualizar estado del vuelo
            console.log('4️⃣ Probando PUT /api/airline/flights/{id}/status...');
            const statusUpdate = await makeRequest(`${API_BASE}/api/airline/flights/${createdFlight.flightId}/status`, {
                method: 'PUT',
                body: JSON.stringify({ status: 'PUBLISHED' }),
            });
            console.log('✅ Estado actualizado:', statusUpdate);
            console.log('');

            // 5. Verificar que el estado se actualizó
            console.log('5️⃣ Verificando cambio de estado...');
            const updatedFlight = await makeRequest(`${API_BASE}/api/airline/flights/${createdFlight.flightId}`);
            console.log('✅ Vuelo con estado actualizado:', updatedFlight);
            console.log('');
        }

        // 6. Probar obtener vuelos nuevamente para ver el nuevo
        console.log('6️⃣ Verificando lista actualizada de vuelos...');
        const updatedFlights = await makeRequest(`${API_BASE}/api/airline/flights`);
        console.log('✅ Lista actualizada de vuelos:', updatedFlights);
        console.log('');

        console.log('🎉 ¡Todas las pruebas pasaron exitosamente!');
        console.log('🚀 La API de vuelos está funcionando correctamente.');

    } catch (error) {
        console.error('❌ Error en las pruebas:', error.message);
        console.error('Detalles:', error);
    }
}

// Función para probar endpoints individuales
async function testIndividualEndpoints() {
    console.log('🔍 Probando endpoints individuales...\n');

    const endpoints = [
        { method: 'GET', path: '/api/airline/cities', description: 'Obtener ciudades' },
        { method: 'GET', path: '/api/airline/flights', description: 'Obtener vuelos' },
        { method: 'GET', path: '/api/health', description: 'Health check' },
    ];

    for (const endpoint of endpoints) {
        try {
            console.log(`📡 Probando ${endpoint.method} ${endpoint.path} (${endpoint.description})...`);
            const response = await makeRequest(`${API_BASE}${endpoint.path}`);
            console.log(`✅ ${endpoint.description}:`, response);
            console.log('');
        } catch (error) {
            console.error(`❌ Error en ${endpoint.description}:`, error.message);
            console.log('');
        }
    }
}

// Función principal
async function main() {
    console.log('🚀 Iniciando pruebas de la API de Aerolínea');
    console.log(`📍 Servidor: ${API_BASE}`);
    console.log('⏰ Fecha:', new Date().toLocaleString());
    console.log('=' .repeat(60));
    console.log('');

    // Probar endpoints básicos primero
    await testIndividualEndpoints();
    
    // Luego probar la funcionalidad completa de vuelos
    await testFlightAPI();
}

// Ejecutar pruebas
if (typeof window === 'undefined') {
    // Si se ejecuta en Node.js
    const fetch = require('node-fetch');
    main();
} else {
    // Si se ejecuta en el navegador
    main();
}
