import { useMemo, useState } from 'react';

function buildPurchaseUrl(flight) {
	const flightId = flight.idFlight ?? flight.flightId ?? flight.id ?? flight.uniqueId ?? flight.flightNumber ?? '';
	const airlineId = flight.airlineId ?? '';
	const params = new URLSearchParams();
	if (flightId) params.set('flightId', flightId);
	if (airlineId) params.set('airlineId', airlineId);
	if (flight.basePrice ?? flight.price ?? flight.totalAmount) {
		params.set('price', String(flight.basePrice ?? flight.price ?? flight.totalAmount));
	}
	params.set('tipo', 'vuelo');
	return `/compra?${params.toString()}`;
}

function FlightDetailModal({ flight, onClose }) {
	if (!flight) return null;
	const flightId = flight.idFlight ?? flight.flightId ?? flight.id ?? flight.uniqueId ?? 'N/D';
	return (
		<div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'grid', placeItems: 'center', zIndex: 50 }} onClick={onClose}>
			<div className="card" style={{ width: 'min(700px, 96vw)' }} onClick={e => e.stopPropagation()}>
				<h3 style={{ marginBottom: 8 }}>Detalle del vuelo {flight.flightNumber || flightId}</h3>
				<div className="small" style={{ marginBottom: 6, color: '#4b5563' }}>
					Aerolínea: <strong>{flight.airlineName || 'Desconocida'}</strong>{flight.airlineCode ? ` (${flight.airlineCode})` : ''} · ID: {flightId}
				</div>
				<div className="grid">
					<div className="small">Ruta: {flight.originCity} → {flight.destinationCity}</div>
					<div className="small">Salida: {flight.departureDate} {flight.departureTime}</div>
					<div className="small">Llegada: {flight.arrivalDate} {flight.arrivalTime}</div>
					<div className="small">Puerta: {flight.gate || '-'} • Terminal: {flight.terminal || '-'}</div>
					<div className="small">Asientos disponibles: {flight.availableSeats ?? '-'}</div>
					<div className="small">Tarifas: {flight.fares ? Object.entries(flight.fares).map(([k,v]) => `${k}: $${v}`).join(' | ') : 'N/D'}</div>
				</div>
				<div style={{ display: 'flex', gap: 8, marginTop: 12, justifyContent: 'flex-end' }}>
					<a className="btn btn-primary" href={buildPurchaseUrl(flight)}>Seleccionar</a>
					<button className="btn" onClick={onClose}>Cerrar</button>
				</div>
			</div>
		</div>
	);
}

export default function FlightResultsList({ flights = [], loading, error, airlines = [], warnings = [], activeAirlineIds = [] }) {
	const [selected, setSelected] = useState(null);
	const warningBlocks = useMemo(() => (warnings || []).filter(Boolean), [warnings]);

	if (loading) return <p className="small">Cargando...</p>;
	if (error) return <p className="small" style={{ color: 'salmon' }}>{error}</p>;

	return (
		<>
			{warningBlocks.length > 0 && (
				<div className="card" style={{ marginBottom: 12, background: '#fff7ed', borderColor: '#fed7aa' }}>
					<strong style={{ color: '#c2410c' }}>Aviso:</strong>
					<ul className="small" style={{ marginTop: 8 }}>
						{warningBlocks.map((warn, idx) => (
							<li key={idx}>{warn.name ? `${warn.name}: ${warn.error || 'Sin respuesta'}` : (warn.error || String(warn))}</li>
						))}
					</ul>
				</div>
			)}
			{airlines.length > 0 && (
				<div className="card" style={{ marginBottom: 12 }}>
					<h4 style={{ marginBottom: 6 }}>Resumen por aerolínea</h4>
					<ul className="small">
						{airlines.map(item => (
							<li key={item.id}>
								<strong>{item.name}</strong>{item.code ? ` (${item.code})` : ''}: {item.flightsFound} vuelos{item.error ? ` · Error: ${item.error}` : ''}
							</li>
						))}
					</ul>
				</div>
			)}
			{!flights.length ? (
				<p className="small">No hay vuelos para los criterios seleccionados.</p>
			) : (
		<>
			<ul className="clean grid">
						{flights.map(item => {
							const flightId = item.idFlight ?? item.flightId ?? item.id ?? item.uniqueId ?? item.flightNumber;
							const price = item.basePrice ?? item.price ?? item.totalAmount ?? 0;
							const isActive = !activeAirlineIds.length || activeAirlineIds.includes(item.airlineId);
							return (
								<li key={`${item.airlineId || 'al'}-${flightId}`} className="result-item" style={isActive ? {} : { opacity: 0.5 }}>
									<div className="result-item-head" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8 }}>
										<div style={{ display: 'flex', flexDirection: 'column' }}>
											<strong>{item.flightNumber || flightId}</strong>
											<span className="badge" style={{ marginTop: 4, alignSelf: 'flex-start' }}>
												{item.airlineName || 'Aerolínea desconocida'}{item.airlineCode ? ` · ${item.airlineCode}` : ''}
											</span>
										</div>
										<span className="price-pill">${price}</span>
						</div>
						<div className="small">{item.originCity} → {item.destinationCity}</div>
						<div className="small">Salida: {item.departureDate} {item.departureTime} — Llegada: {item.arrivalDate} {item.arrivalTime}</div>
						<div style={{ display: 'flex', gap: 8 }}>
							<button className="btn" onClick={() => setSelected(item)}>Ver detalle</button>
										<a className="btn btn-primary" href={buildPurchaseUrl(item)}>Seleccionar</a>
						</div>
					</li>
							);
						})}
			</ul>
			<FlightDetailModal flight={selected} onClose={() => setSelected(null)} />
				</>
			)}
		</>
	);
}
