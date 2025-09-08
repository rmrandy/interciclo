import React, { useState } from 'react';
import AdvancedFlightSearch from '../components/AdvancedFlightSearch';
import FlightCard from '../components/FlightCard';
import { Flight, FlightSearchParams } from '../types';
import apiService from '../services/api';

const Home: React.FC = () => {
  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async (params: FlightSearchParams) => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await apiService.searchFlights(params);
      
      if (response.success && response.data) {
        setFlights(response.data);
      } else {
        setError('No se encontraron vuelos directos para los criterios de búsqueda');
      }
    } catch (err) {
      console.error('Error en búsqueda:', err);
      setError('Error al buscar vuelos. Por favor, intenta de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectFlight = (flight: Flight) => {
    // Aquí podrías navegar a una página de detalles o agregar al carrito
    console.log('Vuelo seleccionado:', flight);
    // Por ahora solo mostramos un alert
    alert(`Vuelo ${flight.flightNumber} seleccionado. Funcionalidad de reserva próximamente.`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <div className="hero">
        <div className="container">
          <div className="text-center">
            <h1 className="text-4xl md:text-5xl font-bold mb-4">
              Busca vuelos baratos
            </h1>
            <p className="text-xl md:text-2xl mb-8 opacity-90">
              Compara precios de cientos de sitios de viajes
            </p>
          </div>
        </div>
      </div>

      {/* Search Section */}
      <div className="container -mt-12 relative z-10">
        <AdvancedFlightSearch onSearch={handleSearch} loading={loading} />
      </div>

      {/* Results Section */}
      <div className="container py-8">
        {loading && (
          <div className="text-center py-12">
            <div className="inline-block animate-pulse rounded-full h-8 w-8 border-2 border-kayak-blue border-t-transparent"></div>
            <p className="mt-4 text-gray-600">Buscando vuelos...</p>
          </div>
        )}

        {error && (
          <div className="card error mb-8">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-6 w-6 text-error-red" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-4">
                <h3 className="text-lg font-bold text-error-red">Error en la búsqueda</h3>
                <div className="mt-2 text-sm text-error-red">
                  {error}
                </div>
              </div>
            </div>
          </div>
        )}

        {!loading && !error && flights.length > 0 && (
          <div className="animate-fade-in-up">
            <h2 className="section-title text-left mb-8">
              Vuelos Directos Encontrados ({flights.length})
            </h2>
            <div className="space-y-6">
              {flights.map((flight) => (
                <FlightCard
                  key={flight._id}
                  flight={flight}
                  onSelect={handleSelectFlight}
                />
              ))}
            </div>
          </div>
        )}

        {!loading && !error && flights.length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-400 mb-4">
              <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-gray-800 mb-2">No se encontraron vuelos</h3>
            <p className="text-gray-600 mb-6">Intenta ajustar tus criterios de búsqueda</p>
            
            <div className="max-w-md mx-auto">
              <h4 className="text-lg font-semibold text-gray-800 mb-4">
                ¿Por qué elegir vuelos directos?
              </h4>
              <div className="grid grid-cols-3 gap-4 text-center">
                <div className="p-3">
                  <div className="text-2xl mb-2">⏰</div>
                  <div className="text-sm font-medium text-gray-700">Tiempo Ahorrado</div>
                </div>
                <div className="p-3">
                  <div className="text-2xl mb-2">✅</div>
                  <div className="text-sm font-medium text-gray-700">Menos Estrés</div>
                </div>
                <div className="p-3">
                  <div className="text-2xl mb-2">❤️</div>
                  <div className="text-sm font-medium text-gray-700">Mayor Comodidad</div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Features Section */}
      <div className="bg-gray-50 py-12">
        <div className="container">
          <div className="text-center mb-8">
            <h2 className="text-xl font-semibold text-gray-800 mb-2">
              ¿Por qué elegir vuelos directos?
            </h2>
            <p className="text-gray-600 text-sm">
              Beneficios de viajar sin escalas
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center p-4">
              <div className="text-2xl mb-2">⏰</div>
              <h3 className="text-base font-semibold text-gray-800 mb-1">Tiempo Ahorrado</h3>
              <p className="text-gray-600 text-sm">
                Llega más rápido a tu destino
              </p>
            </div>
            
            <div className="text-center p-4">
              <div className="text-2xl mb-2">✅</div>
              <h3 className="text-base font-semibold text-gray-800 mb-1">Menos Estrés</h3>
              <p className="text-gray-600 text-sm">
                Sin preocuparte por conexiones
              </p>
            </div>
            
            <div className="text-center p-4">
              <div className="text-2xl mb-2">❤️</div>
              <h3 className="text-base font-semibold text-gray-800 mb-1">Mayor Comodidad</h3>
              <p className="text-gray-600 text-sm">
                Viaja más cómodo
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;