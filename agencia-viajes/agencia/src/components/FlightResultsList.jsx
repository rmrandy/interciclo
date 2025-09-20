import { useState } from 'react';

function FlightDetailModal({ flight, onClose }) {
	if (!flight) return null;
	return (
		<div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'grid', placeItems: 'center', zIndex: 50 }} onClick={onClose}>
			<div className="card" style={{ width: 'min(700px, 96vw)' }} onClick={e => e.stopPropagation()}>
				<h3 style={{ marginBottom: 8 }}>Detalle del vuelo {flight.flightNumber}</h3>
				<div className="grid">
					<div className="small">Ruta: {flight.originCity} → {flight.destinationCity}</div>
					<div className="small">Salida: {flight.departureDate} {flight.departureTime}</div>
					<div className="small">Llegada: {flight.arrivalDate} {flight.arrivalTime}</div>
					<div className="small">Puerta: {flight.gate || '-'} • Terminal: {flight.terminal || '-'}</div>
					<div className="small">Asientos disponibles: {flight.availableSeats ?? '-'}</div>
					<div className="small">Tarifas: {flight.fares ? Object.entries(flight.fares).map(([k,v]) => `${k}: $${v}`).join(' | ') : 'N/D'}</div>
				</div>
				<div style={{ display: 'flex', gap: 8, marginTop: 12, justifyContent: 'flex-end' }}>
					<a className="btn btn-primary" href={`/compra?item=${encodeURIComponent(flight.idFlight || flight.flightNumber)}&tipo=vuelo`}>Seleccionar</a>
					<button className="btn" onClick={onClose}>Cerrar</button>
				</div>
			</div>
		</div>
	);
}

export default function FlightResultsList({ flights = [], loading, error }) {
	const [selected, setSelected] = useState(null);
	if (loading) return <p className="small">Cargando...</p>;
	if (error) return <p className="small" style={{ color: 'salmon' }}>{error}</p>;
	if (!flights.length) return <p className="small">No hay vuelos para los criterios seleccionados.</p>;
	return (
		<>
			<ul className="clean grid">
				{flights.map(item => (
					<li key={item.idFlight || item.flightNumber} className="result-item">
						<div className="result-item-head">
							<strong>{item.flightNumber}</strong>
							<span className="price-pill">${item.basePrice}</span>
						</div>
						<div className="small">{item.originCity} → {item.destinationCity}</div>
						<div className="small">Salida: {item.departureDate} {item.departureTime} — Llegada: {item.arrivalDate} {item.arrivalTime}</div>
						<div style={{ display: 'flex', gap: 8 }}>
							<button className="btn" onClick={() => setSelected(item)}>Ver detalle</button>
							<a className="btn btn-primary" href={`/compra?item=${encodeURIComponent(item.idFlight || item.flightNumber)}&tipo=vuelo`}>Seleccionar</a>
						</div>
					</li>
				))}
			</ul>
			<FlightDetailModal flight={selected} onClose={() => setSelected(null)} />
		</>
	);
}
