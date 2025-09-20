import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { integrationsApi } from '../services/api.js';

export default function FlightDetail() {
	const { id } = useParams();
	const navigate = useNavigate();
	const [flights, setFlights] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	useEffect(() => {
		let mounted = true;
		setLoading(true);
		integrationsApi.flights()
			.then(res => {
				if (!mounted) return;
				const list = Array.isArray(res?.flights) ? res.flights : Array.isArray(res) ? res : [];
				setFlights(list);
			})
			.catch(e => mounted && setError(e?.message || 'Error al cargar el detalle del vuelo'))
			.finally(() => mounted && setLoading(false));
		return () => { mounted = false; };
	}, [id]);

	const flight = useMemo(() => {
		const target = decodeURIComponent(id || '');
		return flights.find(f => String(f?.idFlight) === target || String(f?.flightNumber) === target);
	}, [flights, id]);

	return (
		<section className="section" style={{ maxWidth: 840 }}>
			<div className="card airline-gradient-sky airline-shadow-large" style={{ color:'#fff', padding:'24px', border:'none' }}>
				<div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', gap:12 }}>
					<h2 style={{ margin:0 }}>Detalle del vuelo</h2>
					<div style={{ display:'flex', gap:8 }}>
						<button className="btn" onClick={() => navigate(-1)}>Volver</button>
						<Link className="btn" to="/buscar">Nueva búsqueda</Link>
					</div>
				</div>
			</div>

			{loading && (
				<div style={{ display:'grid', placeItems:'center', padding:'48px 0' }}>
					<div className="spinner" style={{ width:36, height:36, border:'3px solid rgba(0,0,0,0.08)', borderTopColor:'#2563eb', borderRadius:'50%', animation:'spin 1s linear infinite' }} />
					<p className="small" style={{ marginTop:8, color:'#6b7280' }}>Cargando detalles del vuelo...</p>
					<style>
						{`@keyframes spin { from { transform: rotate(0deg);} to { transform: rotate(360deg);} }`}
					</style>
				</div>
			)}
			{error && <p className="small" style={{ color:'salmon' }}>{error}</p>}
			{!loading && !flight && !error && (
				<p className="small">No se encontró el vuelo solicitado.</p>
			)}

			{!loading && flight && (
				<div className="card" style={{ padding:'20px' }}>
					<div className="row" style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8 }}>
						<div style={{ fontWeight:700, fontSize:'1.1rem' }}>{flight.flightNumber}</div>
						<span className="price-pill">${flight.basePrice}</span>
					</div>
					<div className="grid">
						<div className="small">Ruta: {flight.originCity} → {flight.destinationCity}</div>
						<div className="small">Salida: {flight.departureDate} {flight.departureTime}</div>
						<div className="small">Llegada: {flight.arrivalDate} {flight.arrivalTime}</div>
						<div className="small">Puerta: {flight.gate || '-'} • Terminal: {flight.terminal || '-'}</div>
						<div className="small">Asientos disponibles: {flight.availableSeats ?? '-'}</div>
						<div className="small">Tarifas: {flight.fares ? Object.entries(flight.fares).map(([k,v]) => `${k}: $${v}`).join(' | ') : 'N/D'}</div>
					</div>
					<div style={{ display:'flex', gap:8, marginTop:12, justifyContent:'flex-end' }}>
						<Link className="btn" to="/resultados">Ver más resultados</Link>
						<Link className="btn btn-primary" to={`/compra?item=${encodeURIComponent(flight.idFlight || flight.flightNumber)}&tipo=vuelo&precio=${encodeURIComponent(flight.basePrice || 0)}`}>Comprar</Link>
					</div>
				</div>
			)}
		</section>
	);
}


