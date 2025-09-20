import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { integrationsApi } from '../services/api.js';

export default function BookingDetail() {
	const { id } = useParams();
	const [ticket, setTicket] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	useEffect(() => {
		let mounted = true;
		setLoading(true); setError('');
		integrationsApi.ticketById(id).then(res => {
			if (!mounted) return;
			const t = res?.ticket || null;
			setTicket(t);
		}).catch(e => { if (mounted) setError(e?.message || 'Error cargando detalle'); })
		.finally(() => mounted && setLoading(false));
		return () => { mounted = false; };
	}, [id]);

	return (
		<section className="section" style={{ maxWidth: 820 }}>
			<div className="airline-gradient-sky airline-shadow-large" style={{ color:'#fff', padding:'24px', borderRadius:16, marginBottom:12 }}>
				<h2 style={{ margin:0 }}>Detalle de reserva</h2>
			</div>
			{loading && <p className="small">Cargando...</p>}
			{error && <p className="small" style={{ color:'salmon' }}>{error}</p>}
			{!loading && ticket && (
				<div className="card">
					<div className="row" style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8 }}>
						<strong>Ticket #{ticket.idTicket}</strong>
						<span className="small">{ticket.originCity} → {ticket.destinationCity}</span>
					</div>
					<div className="grid">
						<div className="small">Vuelo: {ticket.flightNumber}</div>
						<div className="small">Salida: {ticket.departureDate} {ticket.departureTime}</div>
						<div className="small">Pasajero: {ticket.passengerFirstName} {ticket.passengerLastName}</div>
						<div className="small">Categoría: {ticket.seatCategory} • Asiento: {ticket.seatNumber || 'AUTO'}</div>
						<div className="small">Estado: {ticket.status} • Pago: {ticket.paymentStatus}</div>
						<div className="small">Total: ${ticket.totalAmount}</div>
					</div>
				</div>
			)}
		</section>
	);
}


