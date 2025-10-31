import { useEffect, useMemo, useState } from 'react';
import { integrationsApi } from '../services/api.js';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { jsPDF } from 'jspdf';

export default function MyBookings() {
	const { user } = useAuth();
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
					
					// Filtrar solo las reservas del usuario loggeado
					const userTickets = adaptedTickets.filter(t => 
						t.passengerEmail && user?.email && 
						t.passengerEmail.toLowerCase() === user.email.toLowerCase()
					);
					
					setTickets(userTickets);
					console.log(`✅ Mis reservas cargadas: ${userTickets.length} de ${data.total} totales`);
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
	}, [user?.email]);

	function downloadPdf(ticketId) {
		try {
			console.log('📄 Generando PDF del ticket:', ticketId);
			
			// Encontrar el ticket en la lista
			const ticket = sorted.find(t => t.idTicket === ticketId);
			if (!ticket) {
				alert('No se encontró la información del ticket');
				return;
			}
			
			// Crear un nuevo documento PDF
			const doc = new jsPDF();
			
			// Título
			doc.setFontSize(24);
			doc.setFont('helvetica', 'bold');
			doc.text('TICKET DE VUELO', 20, 30);
			
			// Línea separadora
			doc.setLineWidth(0.5);
			doc.line(20, 35, 190, 35);
			
			// Información del ticket
			let y = 50;
			doc.setFontSize(14);
			doc.setFont('helvetica', 'bold');
			doc.text(`Ticket #${ticket.idTicket}`, 20, y);
			
			y += 15;
			doc.setFontSize(12);
			doc.setFont('helvetica', 'normal');
			
			// Detalles del vuelo
			doc.text(`Vuelo: ${ticket.flightNumber || 'N/A'}`, 20, y);
			y += 7;
			doc.text(`Ruta: ${ticket.originCity || 'N/A'} → ${ticket.destinationCity || 'N/A'}`, 20, y);
			y += 7;
			doc.text(`Fecha: ${ticket.departureDate || 'N/A'}`, 20, y);
			y += 7;
			doc.text(`Hora: ${ticket.departureTime || 'N/A'}`, 20, y);
			
			y += 15;
			
			// Información del pasajero
			doc.setFont('helvetica', 'bold');
			doc.text('Informacion del Pasajero', 20, y);
			y += 10;
			doc.setFont('helvetica', 'normal');
			doc.text(`Nombre: ${ticket.passengerName || 'N/A'}`, 20, y);
			y += 7;
			doc.text(`Email: ${ticket.passengerEmail || 'N/A'}`, 20, y);
			
			y += 15;
			
			// Información del asiento
			doc.setFont('helvetica', 'bold');
			doc.text('Informacion del Asiento', 20, y);
			y += 10;
			doc.setFont('helvetica', 'normal');
			doc.text(`Categoria: ${ticket.seatCategory || 'N/A'}`, 20, y);
			y += 7;
			doc.text(`Asiento: ${ticket.seatNumber || 'Por asignar'}`, 20, y);
			
			y += 15;
			
			// Información de pago
			doc.setFont('helvetica', 'bold');
			doc.text('Informacion de Pago', 20, y);
			y += 10;
			doc.setFont('helvetica', 'normal');
			doc.text(`Total: $${ticket.totalAmount || 0}`, 20, y);
			y += 7;
			doc.text(`Estado: ${ticket.status || 'N/A'}`, 20, y);
			
			// Nota al pie
			doc.setFontSize(10);
			doc.setFont('helvetica', 'italic');
			doc.text('Gracias por volar con nosotros - AeroLinea', 20, 280);
			
			// Guardar el PDF
			doc.save(`Ticket-${ticketId}.pdf`);
			console.log('✅ PDF generado y descargado exitosamente');
			
		} catch (e) {
			console.error('❌ Error generando PDF:', e);
			alert('No se pudo generar el PDF: ' + e.message);
		}
	}

	const sorted = useMemo(() => {
		return [...tickets].sort((a, b) => (b.idTicket || 0) - (a.idTicket || 0));
	}, [tickets]);

	return (
		<section className="section">
			<div className="airline-gradient-sky airline-shadow-large" style={{ color:'#fff', padding:'24px', borderRadius:16, marginBottom:12 }}>
				<h2 style={{ margin:0 }}>✈️ Mis reservas</h2>
				<p className="small" style={{ color:'rgba(255,255,255,0.9)' }}>Todas tus reservaciones de vuelos. Total: {sorted.length}</p>
			</div>
			{loading && <p className="small">Cargando tus reservas...</p>}
			{error && <div className="card" style={{ borderColor:'#fecaca', background:'#fef2f2' }}><strong style={{ color:'#b91c1c' }}>Error:</strong> {error}</div>}
			{!loading && !error && sorted.length === 0 && (
				<div className="card" style={{ textAlign:'center', padding:'40px' }}>
					<div style={{ fontSize:'3rem', marginBottom:'12px' }}>✈️</div>
					<p className="small" style={{ color:'#6b7280' }}>Aún no tienes reservas. Tus compras de vuelos aparecerán aquí.</p>
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


