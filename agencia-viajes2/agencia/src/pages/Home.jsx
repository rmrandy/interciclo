import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import FlightSearchCard from '../components/FlightSearchCard.jsx';
import { integrationsApi } from '../services/api.js';

export default function Home() {
	const navigate = useNavigate();
	const [pageTitle, setPageTitle] = useState('¡Bienvenido a tu Agencia de Viajes!');
	const [pageDescription, setPageDescription] = useState('Encuentra, compara y reserva vuelos fácilmente');
	const [cities, setCities] = useState([]);
	const [loadingCities, setLoadingCities] = useState(false);
	const [citiesError, setCitiesError] = useState('');

	useEffect(() => {
		document.title = `${pageTitle} - Agencia`;
		const meta = document.querySelector('meta[name="description"]');
		if (meta) meta.setAttribute('content', pageDescription);
	}, [pageTitle, pageDescription]);

	// Cargar ciudades disponibles desde Aerolínea (proxy)
	useEffect(() => {
		let mounted = true;
		setLoadingCities(true);
		setCitiesError('');
		integrationsApi.cities().then(res => {
			if (!mounted) return;
			const list = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : []);
			setCities(list);
		}).catch(e => {
			if (!mounted) return;
			setCitiesError(e?.message || 'No se pudieron cargar las ciudades');
		}).finally(() => mounted && setLoadingCities(false));
		return () => { mounted = false; };
	}, []);

	function handleSearch(payload) {
		navigate('/resultados', { state: payload });
	}

	function goOrigin(cityName) {
		if (!cityName) return;
		navigate('/resultados', { state: { origin: cityName } });
	}

	const infoCards = useMemo(() => ([
		{ key: 'asientos', title: 'Asientos', desc: 'Tipos y precios', bg: '#E7F0FF', to: '/acerca', icon: (
			<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M7 3h6a2 2 0 012 2v6h3a2 2 0 012 2v6h-2v-4H8a2 2 0 01-2-2V5a2 2 0 012-2z"/></svg>
		)},
		{ key: 'checkin', title: 'Check-in', desc: 'Proceso paso a paso', bg: '#EAF8EE', to: '/acerca', icon: (
			<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M4 12l4 4 8-8 2 2-10 10-6-6z"/></svg>
		)},
		{ key: 'abordaje', title: 'Abordaje', desc: 'Instrucciones', bg: '#FFF2E5', to: '/acerca', icon: (
			<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M2 12h20M5 9l2 3-2 3"/></svg>
		)},
		{ key: 'equipaje', title: 'Equipaje', desc: 'Políticas y restricciones', bg: '#F1E9FF', to: '/acerca', icon: (
			<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M9 7V5a3 3 0 013-3h0a3 3 0 013 3v2h3a2 2 0 012 2v9a2 2 0 01-2 2H6a2 2 0 01-2-2V9a2 2 0 012-2h3zm2 0h2V5a1 1 0 10-2 0v2z"/></svg>
		)},
		{ key: 'tips', title: 'Consejos', desc: 'Para tu viaje', bg: '#FFEAF3', to: '/acerca', icon: (
			<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a7 7 0 017 7c0 2.2-1.2 4.1-3 5.2V18h-8v-3.8A6.98 6.98 0 015 9a7 7 0 017-7zm-3 18h6v2H9z"/></svg>
		)},
		{ key: 'contacto', title: 'Contacto', desc: 'Ayuda y soporte', bg: '#E6FBF6', to: '/contacto', icon: (
			<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2a1 1 0 011.02-.24 12 12 0 003.57.57 1 1 0 011 1V20a1 1 0 01-1 1C10.61 21 3 13.39 3 4a1 1 0 011-1h3.5a1 1 0 011 1c0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/></svg>
		)},
	]), []);

	const promos = useMemo(() => ([
		{ id: 1, title: '2x1 fin de semana', desc: 'Compra hoy y recibe 2x1 en rutas seleccionadas', color: 'linear-gradient(135deg, #EC4899, #F43F5E)' },
		{ id: 2, title: 'Tarifa Flash', desc: 'Descuentos hasta 35% por 24 horas', color: 'linear-gradient(135deg, #3B82F6, #06B6D4)' },
		{ id: 3, title: 'Business Upgrade', desc: 'Upgrade a Business desde Q399', color: 'linear-gradient(135deg, #F59E0B, #FB923C)' }
	]), []);

	return (
		<div className="container" style={{ maxWidth: 1140 }}>
			{/* Hero */}
			<div className="airline-gradient-sky" style={{ color: 'white', borderRadius: 24, padding: 28, position: 'relative', overflow: 'hidden', marginBottom: 20 }}>
				<div style={{ position: 'absolute', right: 0, top: 0, width: 180, height: 180, opacity: 0.12 }}>
					<svg viewBox="0 0 24 24" fill="currentColor" width="100%" height="100%"><path d="M12 2l3 7 7 1-5 5 1 7-6-3-6 3 1-7-5-5 7-1 3-7z"/></svg>
				</div>
				<div className="animate-fade-in-up">
					<h1 style={{ fontSize: 30, fontWeight: 900, marginBottom: 10, background: 'linear-gradient(90deg,#fff,#E0F2FE)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>{pageTitle}</h1>
					<p style={{ fontSize: 18, opacity: 0.95 }}>{pageDescription}</p>
					<div style={{ display: 'flex', gap: 8, marginTop: 12, flexWrap: 'wrap' }}>
						<span style={{ background: 'rgba(255,255,255,0.18)', border: '1px solid rgba(255,255,255,0.25)', padding: '6px 10px', borderRadius: 999, fontWeight: 600 }}>🔥 Ofertas</span>
						<span style={{ background: 'rgba(255,255,255,0.18)', border: '1px solid rgba(255,255,255,0.25)', padding: '6px 10px', borderRadius: 999, fontWeight: 600 }}>🔎 Comparador</span>
						<span style={{ background: 'rgba(255,255,255,0.18)', border: '1px solid rgba(255,255,255,0.25)', padding: '6px 10px', borderRadius: 999, fontWeight: 600 }}>⚡ 24/7</span>
					</div>
				</div>
			</div>

			{/* Acciones rápidas */}
			<div className="grid-3" style={{ marginBottom: 16 }}>
				<div className="airline-card" onClick={() => navigate('/buscar')} style={{ cursor: 'pointer' }}>
					<div style={{ textAlign: 'center' }}>
						<div style={{ width: 48, height: 48, margin: '0 auto 8px', borderRadius: 12, background: 'var(--gradient-primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
							<svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M10 2a8 8 0 100 16 8 8 0 000-16zm11 18l-4.35-4.35"/></svg>
						</div>
						<h3 style={{ fontWeight: 700, marginBottom: 4 }}>Consultar Vuelos</h3>
						<div className="small">Explora rutas y precios</div>
					</div>
				</div>
				<div className="airline-card" onClick={() => navigate('/login')} style={{ cursor: 'pointer' }}>
					<div style={{ textAlign: 'center' }}>
						<div style={{ width: 48, height: 48, margin: '0 auto 8px', borderRadius: 12, background: 'var(--gradient-sunset)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
							<svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M4 6h16v12H4z" opacity=".2"/><path d="M4 7h16v10H4zM7 6V4h2v2m6 0V4h2v2M7 14h10M7 11h6"/></svg>
						</div>
						<h3 style={{ fontWeight: 700, marginBottom: 4 }}>Reservar Vuelo</h3>
						<div className="small">Requiere registro</div>
					</div>
				</div>
				<div className="airline-card" onClick={() => navigate('/registro')} style={{ cursor: 'pointer' }}>
					<div style={{ textAlign: 'center' }}>
						<div style={{ width: 48, height: 48, margin: '0 auto 8px', borderRadius: 12, background: 'var(--gradient-primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
							<svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M12 12a4 4 0 100-8 4 4 0 000 8zm-7 8a7 7 0 0114 0H5z"/></svg>
						</div>
						<h3 style={{ fontWeight: 700, marginBottom: 4 }}>Crear Cuenta</h3>
						<div className="small">Accede a reservas y beneficios</div>
					</div>
				</div>
			</div>

			{/* Links informativos */}
			<div className="airline-card" style={{ marginBottom: 16 }}>
				<div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
					<div style={{ width: 40, height: 40 }} className="airline-gradient-sunset"></div>
					<h2 className="airline-subtitle" style={{ margin: 0 }}>Información Útil</h2>
				</div>
				<div className="grid" style={{ gridTemplateColumns: 'repeat(6, minmax(0,1fr))', gap: 12 }}>
					{infoCards.map(c => (
						<div key={c.key} className="airline-card" onClick={() => navigate(c.to)} style={{ cursor: 'pointer', textAlign: 'center' }}>
							<div style={{ width: 48, height: 48, margin: '0 auto 8px', borderRadius: 12, background: c.bg, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{c.icon}</div>
							<div style={{ fontWeight: 700, fontSize: 14 }}>{c.title}</div>
							<div className="small">{c.desc}</div>
						</div>
					))}
				</div>
			</div>

			{/* Ciudades disponibles */}
			<div className="airline-card" style={{ marginBottom: 16 }}>
				<div style={{ display:'flex', alignItems:'center', gap:8, marginBottom:12 }}>
					<div style={{ width: 40, height: 40 }} className="airline-gradient-sky"></div>
					<h2 className="airline-subtitle" style={{ margin: 0 }}>Explora por ciudad de origen</h2>
				</div>
				{loadingCities && <div className="small">Cargando ciudades...</div>}
				{citiesError && <div className="small" style={{ color:'salmon' }}>{citiesError}</div>}
				<div style={{ display:'flex', flexWrap:'wrap', gap:8 }}>
					{(cities || []).slice(0, 24).map((c) => {
						const name = c?.name || c?.city || c;
						return (
							<button key={name} className="btn" onClick={() => goOrigin(name)} style={{ background:'#f1f5f9', border:'1px solid #e2e8f0' }}>
								{name}
							</button>
						);
					})}
					{!loadingCities && (cities || []).length === 0 && !citiesError && (
						<span className="small">Sin ciudades disponibles por ahora.</span>
					)}
				</div>
			</div>

			{/* Caja de búsqueda */}
			<div className="airline-card" style={{ marginBottom: 16 }}>
				<div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
					<div style={{ width: 40, height: 40 }} className="airline-gradient-secondary"></div>
					<h2 className="airline-subtitle" style={{ margin: 0 }}>Busca tu vuelo</h2>
				</div>
				<FlightSearchCard onSearch={handleSearch} />
			</div>

			{/* Promos */}
			<div className="airline-card" style={{ marginBottom: 16 }}>
				<div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
					<div style={{ width: 40, height: 40 }} className="airline-gradient-accent"></div>
					<h2 className="airline-subtitle" style={{ margin: 0 }}>Promociones activas</h2>
				</div>
				<div className="grid-3">
					{promos.map(p => (
						<div key={p.id} style={{ borderRadius: 16, padding: 16, color: 'white', background: p.color }}>
							<div style={{ fontWeight: 800, marginBottom: 4 }}>{p.title}</div>
							<div style={{ opacity: 0.95 }}>{p.desc}</div>
						</div>
					))}
				</div>
			</div>

			{/* Atajo a Mis reservas si hay sesión */}
			<div className="airline-card" style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
				<div>
					<h3 style={{ margin:'0 0 6px' }}>¿Ya compraste?</h3>
					<div className="small">Consulta y descarga tus reservaciones.</div>
				</div>
				<button className="btn btn-primary" onClick={() => navigate('/mis-reservas')}>Ver mis reservas</button>
			</div>
		</div>
	);
}
