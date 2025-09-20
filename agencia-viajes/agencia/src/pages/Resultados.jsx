import { useEffect, useMemo, useState } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { integrationsApi } from '../services/api.js';

function normalize(str) {
	return (str || '').toString().trim().toLowerCase();
}

export default function Resultados() {
	const { state } = useLocation();
	const [flights, setFlights] = useState([]);
	const [error, setError] = useState('');
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		let mounted = true;
		setLoading(true);
		integrationsApi.flights().then(res => {
			if (!mounted) return;
			const list = Array.isArray(res.flights) ? res.flights : [];
			setFlights(list);
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
		</section>
	);
}
