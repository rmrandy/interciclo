import React, { useState } from 'react';
import { FlightSearchParams } from '../types';

interface FlightSearchProps {
  onSearch: (params: FlightSearchParams) => void;
  loading?: boolean;
}

const FlightSearch: React.FC<FlightSearchProps> = ({ onSearch, loading = false }) => {
  const [searchParams, setSearchParams] = useState<FlightSearchParams>({
    origin: '',
    destination: '',
    departureDate: '',
    returnDate: '',
    passengers: 1,
    seatCategory: 'economy'
  });

  const [isRoundTrip, setIsRoundTrip] = useState(false);

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
    <div className="bg-white rounded-lg shadow-lg p-6 mb-8">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Buscar Vuelos</h2>
      
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Tipo de viaje */}
        <div className="flex space-x-4 mb-6">
          <label className="flex items-center">
            <input
              type="radio"
              name="tripType"
              checked={!isRoundTrip}
              onChange={() => setIsRoundTrip(false)}
              className="mr-2"
            />
            Solo ida
          </label>
          <label className="flex items-center">
            <input
              type="radio"
              name="tripType"
              checked={isRoundTrip}
              onChange={() => setIsRoundTrip(true)}
              className="mr-2"
            />
            Ida y vuelta
          </label>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Origen */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Origen
            </label>
            <input
              type="text"
              value={searchParams.origin}
              onChange={(e) => handleInputChange('origin', e.target.value.toUpperCase())}
              placeholder="Código (ej: BOG)"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              maxLength={3}
            />
          </div>

          {/* Destino */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Destino
            </label>
            <input
              type="text"
              value={searchParams.destination}
              onChange={(e) => handleInputChange('destination', e.target.value.toUpperCase())}
              placeholder="Código (ej: MAD)"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              maxLength={3}
            />
          </div>

          {/* Fecha de salida */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Fecha de salida
            </label>
            <input
              type="date"
              value={searchParams.departureDate}
              onChange={(e) => handleInputChange('departureDate', e.target.value)}
              min={new Date().toISOString().split('T')[0]}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>

          {/* Fecha de regreso */}
          {isRoundTrip && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Fecha de regreso
              </label>
              <input
                type="date"
                value={searchParams.returnDate}
                onChange={(e) => handleInputChange('returnDate', e.target.value)}
                min={searchParams.departureDate || new Date().toISOString().split('T')[0]}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
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

        {/* Botón de búsqueda */}
        <div className="pt-4">
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-primary-600 text-white py-3 px-4 rounded-md hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {loading ? 'Buscando...' : 'Buscar Vuelos'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default FlightSearch;
