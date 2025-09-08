import React from 'react';
import { Flight } from '../types';

interface FlightCardProps {
  flight: Flight;
  onSelect?: (flight: Flight) => void;
  showSelectButton?: boolean;
}

const FlightCard: React.FC<FlightCardProps> = ({ 
  flight, 
  onSelect, 
  showSelectButton = true 
}) => {
  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  const formatDuration = (departure: string, arrival: string) => {
    const dep = new Date(departure);
    const arr = new Date(arrival);
    const diffMs = arr.getTime() - dep.getTime();
    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    return `${hours}h ${minutes}m`;
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'scheduled':
        return 'bg-green-100 text-green-800';
      case 'boarding':
        return 'bg-blue-100 text-blue-800';
      case 'departed':
        return 'bg-yellow-100 text-yellow-800';
      case 'arrived':
        return 'bg-gray-100 text-gray-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      case 'delayed':
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'scheduled':
        return 'Programado';
      case 'boarding':
        return 'Abordando';
      case 'departed':
        return 'Despegó';
      case 'arrived':
        return 'Llegó';
      case 'cancelled':
        return 'Cancelado';
      case 'delayed':
        return 'Retrasado';
      default:
        return status;
    }
  };

  return (
    <div className="flight-card">
      {/* Header con número de vuelo y estado */}
      <div className="flight-header">
        <div>
          <h3 className="text-lg font-semibold text-gray-800">
            {flight.flightNumber}
          </h3>
          <p className="text-sm text-gray-600">
            {flight.aircraft.model}
          </p>
        </div>
        <span className={`px-3 py-1 rounded text-xs font-medium ${getStatusColor(flight.status)}`}>
          {getStatusText(flight.status)}
        </span>
      </div>

      {/* Ruta y horarios */}
      <div className="flight-route">
        <div className="text-center">
          <div className="flight-time">
            {formatTime(flight.departure)}
          </div>
          <div className="flight-airport">
            {flight.origin.code}
          </div>
          <div className="text-xs text-gray-500 mt-1">
            {flight.origin.city}
          </div>
        </div>

        <div className="flex-1 mx-4">
          <div className="flex items-center">
            <div className="flex-1 border-t border-gray-300"></div>
            <div className="flight-duration">
              {formatDuration(flight.departure, flight.arrival)}
            </div>
            <div className="flex-1 border-t border-gray-300"></div>
          </div>
          <div className="flight-stops">
            Vuelo directo
          </div>
        </div>

        <div className="text-center">
          <div className="flight-time">
            {formatTime(flight.arrival)}
          </div>
          <div className="flight-airport">
            {flight.destination.code}
          </div>
          <div className="text-xs text-gray-500 mt-1">
            {flight.destination.city}
          </div>
        </div>
      </div>

      {/* Precios por categoría */}
      <div className="grid grid-cols-3 gap-4 mb-4">
        <div className="text-center p-3 bg-gray-50 rounded border">
          <div className="text-xs font-medium text-gray-600">Económica</div>
          <div className="text-lg font-bold text-kayak-blue mt-1">
            ${flight.inventory.economy.price.toLocaleString()}
          </div>
          <div className="text-xs text-gray-500">
            {flight.inventory.economy.available} disponibles
          </div>
        </div>
        
        <div className="text-center p-3 bg-blue-50 rounded border border-kayak-blue">
          <div className="text-xs font-medium text-kayak-blue">Ejecutiva</div>
          <div className="text-lg font-bold text-kayak-blue mt-1">
            ${flight.inventory.business.price.toLocaleString()}
          </div>
          <div className="text-xs text-gray-500">
            {flight.inventory.business.available} disponibles
          </div>
        </div>
        
        <div className="text-center p-3 bg-orange-50 rounded border border-kayak-orange">
          <div className="text-xs font-medium text-kayak-orange">Primera</div>
          <div className="text-lg font-bold text-kayak-orange mt-1">
            ${flight.inventory.firstClass.price.toLocaleString()}
          </div>
          <div className="text-xs text-gray-500">
            {flight.inventory.firstClass.available} disponibles
          </div>
        </div>
      </div>

      {/* Botón de selección */}
      {showSelectButton && onSelect && (
        <div className="flex justify-end">
          <button
            onClick={() => onSelect(flight)}
            className="btn btn-primary"
          >
            Seleccionar
          </button>
        </div>
      )}
    </div>
  );
};

export default FlightCard;
