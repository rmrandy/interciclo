import React, { useState } from 'react';
import { FlightSearchParams } from '../types';

interface AdvancedFlightSearchProps {
  onSearch: (params: FlightSearchParams) => void;
  loading?: boolean;
}

const AdvancedFlightSearch: React.FC<AdvancedFlightSearchProps> = ({ onSearch, loading = false }) => {
  const [searchParams, setSearchParams] = useState<FlightSearchParams>({
    origin: '',
    destination: '',
    departureDate: '',
    returnDate: '',
    passengers: 1,
    seatCategory: 'economy'
  });

  const [isRoundTrip, setIsRoundTrip] = useState(false);
  const [directFlightsOnly, setDirectFlightsOnly] = useState(true);
  const [flexibleDates, setFlexibleDates] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch(searchParams);
  };

  const handleInputChange = (field: keyof FlightSearchParams, value: string | number) => {
    setSearchParams(prev => ({
      ...prev,
      [field]: value
    }));
  };


  return (
    <div className="search-container">
      <div className="search-tabs">
        <button className="search-tab active">Vuelos</button>
        <button className="search-tab">Hoteles</button>
        <button className="search-tab">Coches</button>
        <button className="search-tab">Paquetes</button>
      </div>
      
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-semibold text-gray-800">Buscar vuelos</h2>
        <div className="flex items-center space-x-4">
          <label className="flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={directFlightsOnly}
              onChange={(e) => setDirectFlightsOnly(e.target.checked)}
              className="w-4 h-4 text-kayak-blue border-gray-300 rounded focus:ring-kayak-blue"
            />
            <span className="ml-2 text-sm text-gray-600">Solo vuelos directos</span>
          </label>
          <label className="flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={flexibleDates}
              onChange={(e) => setFlexibleDates(e.target.checked)}
              className="w-4 h-4 text-kayak-blue border-gray-300 rounded focus:ring-kayak-blue"
            />
            <span className="ml-2 text-sm text-gray-600">Fechas flexibles</span>
          </label>
        </div>
      </div>
      
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Tipo de viaje */}
        <div className="flex space-x-6 mb-6">
          <label className="flex items-center cursor-pointer">
            <input
              type="radio"
              name="tripType"
              checked={!isRoundTrip}
              onChange={() => setIsRoundTrip(false)}
              className="w-4 h-4 text-kayak-blue border-gray-300 focus:ring-kayak-blue"
            />
            <span className="ml-2 text-sm font-medium text-gray-700">Solo ida</span>
          </label>
          <label className="flex items-center cursor-pointer">
            <input
              type="radio"
              name="tripType"
              checked={isRoundTrip}
              onChange={() => setIsRoundTrip(true)}
              className="w-4 h-4 text-kayak-blue border-gray-300 focus:ring-kayak-blue"
            />
            <span className="ml-2 text-sm font-medium text-gray-700">Ida y vuelta</span>
          </label>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Origen */}
          <div className="form-group">
            <label className="form-label">
              Origen
            </label>
            <input
              type="text"
              value={searchParams.origin}
              onChange={(e) => handleInputChange('origin', e.target.value.toUpperCase())}
              placeholder="Código (ej: BOG)"
              className="form-input"
              maxLength={3}
            />
          </div>

          {/* Destino */}
          <div className="form-group">
            <label className="form-label">
              Destino
            </label>
            <input
              type="text"
              value={searchParams.destination}
              onChange={(e) => handleInputChange('destination', e.target.value.toUpperCase())}
              placeholder="Código (ej: MAD)"
              className="form-input"
              maxLength={3}
            />
          </div>

          {/* Fecha de salida */}
          <div className="form-group">
            <label className="form-label">
              Fecha de salida
            </label>
            <input
              type="date"
              value={searchParams.departureDate}
              onChange={(e) => handleInputChange('departureDate', e.target.value)}
              min={new Date().toISOString().split('T')[0]}
              className="form-input"
            />
          </div>

          {/* Fecha de regreso */}
          {isRoundTrip && (
            <div className="form-group">
              <label className="form-label">
                Fecha de regreso
              </label>
              <input
                type="date"
                value={searchParams.returnDate}
                onChange={(e) => handleInputChange('returnDate', e.target.value)}
                min={searchParams.departureDate || new Date().toISOString().split('T')[0]}
                className="form-input"
              />
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Pasajeros */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Pasajeros
            </label>
            <select
              value={searchParams.passengers}
              onChange={(e) => handleInputChange('passengers', parseInt(e.target.value))}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(num => (
                <option key={num} value={num}>
                  {num} {num === 1 ? 'pasajero' : 'pasajeros'}
                </option>
              ))}
            </select>
          </div>

          {/* Categoría de asiento */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Clase
            </label>
            <select
              value={searchParams.seatCategory}
              onChange={(e) => handleInputChange('seatCategory', e.target.value as 'economy' | 'business' | 'firstClass')}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="economy">Económica</option>
              <option value="business">Ejecutiva</option>
              <option value="firstClass">Primera Clase</option>
            </select>
          </div>
        </div>

        {/* Información adicional */}
        <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-blue-800">
                Búsqueda optimizada para vuelos directos
              </h3>
              <div className="mt-2 text-sm text-blue-700">
                <ul className="list-disc list-inside space-y-1">
                  <li>Sin escalas ni conexiones</li>
                  <li>Tiempo de viaje más corto</li>
                  <li>Menor riesgo de retrasos</li>
                  <li>Experiencia de viaje más cómoda</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        {/* Botón de búsqueda */}
        <div className="pt-4">
          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary w-full text-lg py-4"
          >
            {loading ? 'Buscando vuelos...' : 'Buscar vuelos'}
          </button>
        </div>
      </form>

      {/* Características de búsqueda optimizada */}
      <div className="mt-6 p-4 bg-blue-50 rounded-lg">
        <h3 className="text-lg font-semibold text-gray-800 mb-3">
          ¿Por qué elegir vuelos directos?
        </h3>
        <ul className="feature-list">
          <li>Sin escalas ni conexiones</li>
          <li>Tiempo de viaje más corto</li>
          <li>Menor riesgo de retrasos</li>
          <li>Experiencia de viaje más cómoda</li>
        </ul>
      </div>
    </div>
  );
};

export default AdvancedFlightSearch;
