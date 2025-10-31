import { useEffect, useMemo, useState } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { integrationsApi } from '../services/api.js';

function normalize(str) {
	return (str || '').toString().trim().toLowerCase();
}

export default function Resultados() {
	const { state } = useLocation();
	const [flights, setFlights] = useState([]);
	const [oneStopFlights, setOneStopFlights] = useState([]);  // Vuelos con 1 escala
	const [returnFlights, setReturnFlights] = useState([]);     // Vuelos de vuelta
	const [error, setError] = useState('');
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		let mounted = true;
		setLoading(true);
		integrationsApi.flights().then(res => {
			if (!mounted) return;
			const list = Array.isArray(res.flights) ? res.flights : [];
			setFlights(list);
			
			// ✈️ Capturar vuelos con escala y round-trip del backend
			setOneStopFlights(res.oneStopFlights || []);
			setReturnFlights(res.returnFlights || []);
			
			console.log('📊 Resultados:', {
				directos: list.length,
				conEscala: (res.oneStopFlights || []).length,
				vuelta: (res.returnFlights || []).length
			});
		}).catch(e => {
			if (!mounted) return;
			setError(e.message || 'Error al cargar vuelos');
		}).finally(() => mounted && setLoading(false));
		return () => { mounted = false; };
	}, []);

	const filtered = useMemo(() => {
		const origin = normalize(state?.origin);
		const destination = normalize(state?.destination);
		const fechaIda = (state?.fechaIda || '').trim();
		const cabina = (state?.cabina || '').toUpperCase();
		return flights.filter(f => {
			if (origin && normalize(f.originCity) !== origin) return false;
			if (destination && normalize(f.destinationCity) !== destination) return false;
			if (fechaIda && (f.departureDate || '').trim() !== fechaIda) return false;
			// Si se indicó cabina, asegurarse que exista tarifa en fares
			if (cabina && f.fares && typeof f.fares === 'object' && !(cabina in f.fares)) return false;
			return true;
		});
	}, [flights, state]);

	const ordered = useMemo(() => {
		return [...filtered].sort((a, b) => (a.basePrice || 0) - (b.basePrice || 0));
	}, [filtered]);

    return (
        <section className="section">
            <div className="card airline-gradient-sky airline-shadow-large" style={{ color:'#fff', padding:'28px', border:'none' }}>
                <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', gap:12 }}>
                    <div>
                        <h2 style={{ margin:0 }}>Resultados de vuelos</h2>
                        <p className="small" style={{ color:'rgba(255,255,255,0.9)' }}>Resultados del proveedor, filtrados en el cliente.</p>
                    </div>
                </div>
            </div>
			{loading && <p className="small">Cargando...</p>}
			{error && <p className="small" style={{ color: 'salmon' }}>{error}</p>}
			{!loading && ordered.length === 0 && <p className="small">No hay vuelos que coincidan con tu búsqueda.</p>}
            <ul className="clean grid" style={{ marginTop: 10 }}>
				{ordered.map(item => (
                    <li key={item.idFlight || item.flightNumber} className="airline-card animate-fade-in-up">
                        <div className="row" style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8 }}>
                            <div style={{ fontWeight:700, fontSize:'1.05rem' }}>{item.flightNumber}</div>
                            <span className="price-pill">${item.basePrice}</span>
                        </div>
                        <div className="small" style={{ marginBottom:4 }}>{item.originCity} → {item.destinationCity}</div>
                        <div className="small" style={{ marginBottom:12 }}>Salida: {item.departureDate} {item.departureTime} — Llegada: {item.arrivalDate} {item.arrivalTime}</div>
                        <div style={{ display:'flex', gap:8, justifyContent:'flex-end' }}>
                            <Link className="btn" to={`/vuelo/${encodeURIComponent(item.idFlight || item.flightNumber)}`}>Ver detalle</Link>
                            <Link className="btn btn-primary" to={`/compra?item=${encodeURIComponent(item.idFlight || item.flightNumber)}&tipo=vuelo&precio=${encodeURIComponent(item.basePrice || 0)}`}>Comprar</Link>
                        </div>
                    </li>
				))}
			</ul>

			{/* ✈️ VUELOS CON ESCALA (1 parada) */}
			{oneStopFlights.length > 0 && (
				<div style={{ marginTop: 32 }}>
					<h3 style={{ color: '#1f2937', fontSize: '1.5rem', marginBottom: 16, paddingLeft: 8, borderLeft: '4px solid #f59e0b' }}>
						✈️ Vuelos con 1 Escala
					</h3>
					<ul className="clean grid">
						{oneStopFlights.map((route, index) => (
							<li key={'onestop-' + index} className="airline-card" style={{ border: '2px solid #fbbf24', background: 'linear-gradient(to right, #fffbeb, #ffffff)' }}>
								<div style={{ marginBottom: 16, padding: 8, background: '#fef3c7', borderRadius: 8 }}>
									<strong style={{ color: '#92400e' }}>🔄 Ruta con escala en {route.viaCityName}</strong>
								</div>
								
								{/* Segmento 1 */}
								<div style={{ marginBottom: 12, padding: 12, background: '#f9fafb', borderRadius: 8 }}>
									<div style={{ fontWeight: 600, color: '#374151', marginBottom: 8 }}>
										Segmento 1: {route.firstSegment.originCity} → {route.firstSegment.destinationCity}
									</div>
									<div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', color: '#6b7280' }}>
										<span>Vuelo {route.firstSegment.flightNumber}</span>
										<span>{route.firstSegment.departureDate} {route.firstSegment.departureTime}</span>
										<span>${route.firstSegment.basePrice}</span>
									</div>
								</div>
								
								{/* Segmento 2 */}
								<div style={{ padding: 12, background: '#f9fafb', borderRadius: 8, marginBottom: 16 }}>
									<div style={{ fontWeight: 600, color: '#374151', marginBottom: 8 }}>
										Segmento 2: {route.secondSegment.originCity} → {route.secondSegment.destinationCity}
									</div>
									<div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', color: '#6b7280' }}>
										<span>Vuelo {route.secondSegment.flightNumber}</span>
										<span>{route.secondSegment.departureDate} {route.secondSegment.departureTime}</span>
										<span>${route.secondSegment.basePrice}</span>
									</div>
								</div>
								
								<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: 12, borderTop: '2px solid #fef3c7' }}>
									<div>
										<strong style={{ fontSize: '1.25rem', color: '#059669' }}>Total: ${route.totalPrice}</strong>
										<p style={{ fontSize: '0.875rem', color: '#6b7280', margin: '4px 0 0 0' }}>2 vuelos incluidos</p>
									</div>
									<Link 
										className="btn btn-primary" 
										to={`/compra?stopover=true&segment1=${route.firstSegment.idFlight}&segment2=${route.secondSegment.idFlight}&precio=${route.totalPrice}`}
										style={{ background: '#f59e0b' }}
									>
										Comprar con Escala
									</Link>
								</div>
							</li>
						))}
					</ul>
				</div>
			)}

			{/* 🔄 VUELOS DE VUELTA (Round-Trip) */}
			{returnFlights.length > 0 && (
				<div style={{ marginTop: 32 }}>
					<h3 style={{ color: '#1f2937', fontSize: '1.5rem', marginBottom: 16, paddingLeft: 8, borderLeft: '4px solid #10b981' }}>
						🔄 Vuelos de Vuelta (Round-Trip)
					</h3>
					<ul className="clean grid">
						{returnFlights.map((returnFlight) => (
							<li key={'return-' + returnFlight.idFlight} className="airline-card" style={{ border: '2px solid #10b981', background: 'linear-gradient(to right, #d1fae5, #ffffff)' }}>
								<div style={{ marginBottom: 8, padding: '6px 12px', background: '#d1fae5', borderRadius: 999, display: 'inline-block' }}>
									<strong style={{ color: '#065f46' }}>🔙 Vuelo de Vuelta</strong>
								</div>
								<div style={{ marginTop: 12 }}>
									<div style={{ fontWeight: 700, fontSize: '1.05rem', marginBottom: 8 }}>{returnFlight.flightNumber}</div>
									<div className="small" style={{ marginBottom: 4 }}>{returnFlight.originCity} → {returnFlight.destinationCity}</div>
									<div className="small" style={{ marginBottom: 12 }}>Salida: {returnFlight.departureDate} {returnFlight.departureTime}</div>
									<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
										<span className="price-pill" style={{ background: '#10b981', color: '#fff' }}>${returnFlight.basePrice}</span>
										<Link className="btn" to={`/vuelo/${returnFlight.idFlight}`}>Ver detalle</Link>
									</div>
								</div>
							</li>
						))}
					</ul>
				</div>
			)}
		</section>
	);
}
