import { useEffect, useMemo, useState } from 'react';
import { integrationsApi } from '../services/api.js';
import { Link } from 'react-router-dom';

export default function MyBookings() {
	const [tickets, setTickets] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	useEffect(() => {
		let mounted = true;
		setLoading(true); setError('');
		try {
			const raw = localStorage.getItem('user');
			const user = raw ? JSON.parse(raw) : null;
			const userId = user?.idUser || user?.id || user?.userId || null;
			if (!userId) {
				setError('Debes iniciar sesión para ver tus reservaciones.');
				setLoading(false);
				return;
			}
			integrationsApi.ticketsList({ userId }).then(res => {
				if (!mounted) return;
				const list = Array.isArray(res?.tickets) ? res.tickets : [];
				setTickets(list);
			}).catch(e => { if (mounted) setError(e?.message || 'Error al cargar tus reservaciones'); })
			.finally(() => mounted && setLoading(false));
		} catch (e) {
			setError('Error leyendo usuario');
			setLoading(false);
		}
		return () => { mounted = false; };
	}, []);

	async function downloadPdf(id) {
		try {
			const res = await integrationsApi.ticketPdf(id);
			if (!res.ok || !(res.contentType || '').includes('pdf')) {
				alert('El PDF no está disponible por el momento.');
				return;
			}
			const url = URL.createObjectURL(res.blob);
			const a = document.createElement('a');
			a.href = url; a.download = `Ticket-${id}.pdf`; a.click();
			URL.revokeObjectURL(url);
		} catch (e) {
			alert('No se pudo descargar el PDF');
		}
	}

	const sorted = useMemo(() => {
		return [...tickets].sort((a, b) => (b.idTicket || 0) - (a.idTicket || 0));
	}, [tickets]);

	return (
		<section className="section">
			<div className="airline-gradient-sky airline-shadow-large" style={{ color:'#fff', padding:'24px', borderRadius:16, marginBottom:12 }}>
				<h2 style={{ margin:0 }}>Mis reservaciones</h2>
				<p className="small" style={{ color:'rgba(255,255,255,0.9)' }}>Consulta tus boletos y descarga el PDF.</p>
			</div>
			{loading && <p className="small">Cargando...</p>}
			{error && <div className="card" style={{ borderColor:'#fecaca', background:'#fef2f2' }}><strong style={{ color:'#b91c1c' }}>Error:</strong> {error}</div>}
			{!loading && !error && sorted.length === 0 && <p className="small">Aún no tienes reservaciones.</p>}
			<ul className="clean grid">
				{sorted.map(t => (
					<li key={t.idTicket} className="airline-card">
						<div className="row" style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8 }}>
							<div style={{ fontWeight:700 }}>Ticket #{t.idTicket}</div>
							<div className="small">{t.originCity} → {t.destinationCity}</div>
						</div>
						<div className="small">Vuelo: {t.flightNumber} • Fecha: {t.departureDate} {t.departureTime}</div>
						<div className="small">Pasajero: {t.passengerName} • Categoría: {t.seatCategory} • Asiento: {t.seatNumber || 'AUTO'}</div>
						<div className="small">Total: ${t.totalAmount}</div>
						<div style={{ display:'flex', gap:8, marginTop:8, justifyContent:'flex-end' }}>
							<Link className="btn" to={`/reserva/${t.idTicket}`}>Ver detalle</Link>
							<button className="btn" onClick={() => downloadPdf(t.idTicket)}>Descargar PDF</button>
						</div>
					</li>
				))}
			</ul>
		</section>
	);
}


