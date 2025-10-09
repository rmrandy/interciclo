import { useEffect, useMemo, useState } from 'react';
import { integrationsApi } from '../services/api.js';
import { Link } from 'react-router-dom';

export default function MyBookings() {
	const [tickets, setTickets] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	useEffect(() => {
		let mounted = true;
		loadCorporateTickets();
		
		async function loadCorporateTickets() {
			try {
				setLoading(true);
				setError('');
				
				// 🏢 COMPRA EMPRESARIAL: Obtener tickets de la agencia (con API_KEY automático)
				const response = await fetch('http://localhost:5001/api/integrations/airline/tickets/corporate');
				const data = await response.json();
				
				if (!mounted) return;
				
				if (data.success) {
					// Adaptar formato de tickets empresariales al formato esperado
					const adaptedTickets = (data.tickets || []).map(t => ({
						idTicket: t.ticketId,
						flightNumber: t.flightNumber,
						originCity: t.origin,
						destinationCity: t.destination,
						departureDate: t.departureDate,
						departureTime: t.departureTime,
						passengerName: `${t.passengerFirstName} ${t.passengerLastName}`,
						passengerEmail: t.passengerEmail,
						seatCategory: t.seatCategory,
						seatNumber: t.seatNumber,
						totalAmount: t.totalAmount,
						status: t.status,
						bookingDate: t.bookingDate,
						bookingTime: t.bookingTime,
						purchasedBy: t.purchasedBy
					}));
					setTickets(adaptedTickets);
					console.log('✅ Tickets empresariales cargados:', data.total);
				} else {
					setError(data.error || 'No se pudieron cargar las reservas');
				}
			} catch (err) {
				console.error('❌ Error cargando tickets:', err);
				if (mounted) setError('Error de conexión al cargar reservas');
			} finally {
				if (mounted) setLoading(false);
			}
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
				<h2 style={{ margin:0 }}>🏢 Reservas de la Agencia</h2>
				<p className="small" style={{ color:'rgba(255,255,255,0.9)' }}>Todas las compras realizadas desde la agencia. Total: {sorted.length}</p>
			</div>
			{loading && <p className="small">Cargando reservas empresariales...</p>}
			{error && <div className="card" style={{ borderColor:'#fecaca', background:'#fef2f2' }}><strong style={{ color:'#b91c1c' }}>Error:</strong> {error}</div>}
			{!loading && !error && sorted.length === 0 && (
				<div className="card" style={{ textAlign:'center', padding:'40px' }}>
					<div style={{ fontSize:'3rem', marginBottom:'12px' }}>✈️</div>
					<p className="small" style={{ color:'#6b7280' }}>Aún no hay reservas empresariales. Las compras desde la agencia aparecerán aquí.</p>
				</div>
			)}
			<ul className="clean grid">
				{sorted.map(t => (
					<li key={t.idTicket} className="airline-card">
						<div className="row" style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8 }}>
							<div style={{ fontWeight:700 }}>Ticket #{t.idTicket}</div>
							<div className="small">{t.originCity} → {t.destinationCity}</div>
						</div>
						<div className="small">Vuelo: {t.flightNumber} • Fecha: {t.departureDate} {t.departureTime}</div>
						<div className="small">👤 Pasajero: {t.passengerName} ({t.passengerEmail}) • Categoría: {t.seatCategory} • Asiento: {t.seatNumber || 'AUTO'}</div>
						<div className="small">💰 Total: ${t.totalAmount} {t.purchasedBy && `• 🏢 ${t.purchasedBy}`}</div>
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


