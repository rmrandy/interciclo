import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { integrationsApi, agencyReviewsApi } from '../services/api.js';

export default function BookingDetail() {
	const { id } = useParams();
	const [ticket, setTicket] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');
    const [reviews, setReviews] = useState({ items: [], loading: false, error: '' });
    const [agencyReviews, setAgencyReviews] = useState({ items: [], loading: false, error: '' });
    const [replyTo, setReplyTo] = useState(null);
    const [newReview, setNewReview] = useState({ comment: '', rating: 5 });
    const [posting, setPosting] = useState(false);

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

    // Cargar reseñas para el vuelo asociado
    useEffect(() => {
        if (!ticket?.flightId) return;
        setReviews(prev => ({ ...prev, loading: true, error: '' }));
        const flightId = ticket.flightId;
        integrationsApi.flightReviews(flightId, { mode: 'tree' })
            .then(res => {
                const items = Array.isArray(res?.reviews) ? res.reviews : (Array.isArray(res) ? res : []);
                setReviews({ items, loading: false, error: '' });
            })
            .catch(e => setReviews({ items: [], loading: false, error: e?.message || 'No se pudieron cargar reseñas' }));
    }, [ticket]);

    useEffect(() => {
        if (!ticket?.flightId) return;
        setAgencyReviews(prev => ({ ...prev, loading: true, error: '' }));
        agencyReviewsApi.list({ flightId: String(ticket.flightId), mode: 'tree' })
            .then(res => {
                const items = Array.isArray(res?.reviews) ? res.reviews : [];
                setAgencyReviews({ items, loading: false, error: '' });
            })
            .catch(e => setAgencyReviews({ items: [], loading: false, error: e?.message || 'No se pudieron cargar comentarios locales' }));
    }, [ticket]);

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
						<div className="small">Total: ${ticket.totalAmount} {ticket.reservationCode ? `• Código: ${ticket.reservationCode}` : ''}</div>
					</div>

				{/* Reseñas */}
                <div className="card" style={{ marginTop:12 }}>
                    <h3 style={{ marginTop:0 }}>Comentarios del vuelo</h3>
                    {reviews.loading && <p className="small">Cargando comentarios...</p>}
                    {reviews.error && <p className="small" style={{ color:'salmon' }}>{reviews.error}</p>}
                    <CommentsTree items={reviews.items} StarDisplay={StarDisplay} />
                </div>

				{/* Comentarios de la Agencia (anidados) */}
				<div className="card" style={{ marginTop:12 }}>
					<h3 style={{ marginTop:0 }}>Comentarios (Agencia)</h3>
					{agencyReviews.loading && <p className="small">Cargando...</p>}
					{agencyReviews.error && <p className="small" style={{ color:'salmon' }}>{agencyReviews.error}</p>}
                    <CommentsTree
                        items={agencyReviews.items}
                        onReply={(id) => { setReplyTo(id); setNewReview(prev => ({ ...prev, comment: '' })); }}
                        replyTo={replyTo}
                        StarDisplay={StarDisplay}
                        ReplyForm={({ parentId }) => (
                            <InlineReplyForm
                                value={newReview}
                                onChange={(next) => setNewReview(next)}
                                onCancel={() => { setReplyTo(null); setNewReview({ comment:'', rating:5 }); }}
                                onSubmit={async () => {
                                    try {
                                        if (!ticket?.flightId) return;
                                        setPosting(true);
                                        const flightId = String(ticket.flightId);
                                        await agencyReviewsApi.create({ flightId, parentId, authorName: getCurrentUserName(), rating: 0, comment: newReview.comment||'' });
                                        setReplyTo(null);
                                        setNewReview({ comment:'', rating:5 });
                                        const res = await agencyReviewsApi.list({ flightId, mode: 'tree' });
                                        const items = Array.isArray(res?.reviews) ? res.reviews : [];
                                        setAgencyReviews({ items, loading:false, error:'' });
                                    } catch (e) {
                                        alert('No se pudo guardar el comentario');
                                    } finally { setPosting(false); }
                                }}
                                posting={posting}
                                showStars={false}
                            />
                        )}
                    />

                    {replyTo && (
                        <div className="card" style={{ marginTop:8, background:'#f8fafc' }}>
                            <h4 style={{ marginTop:0 }}>Responder</h4>
                            <div className="grid-2">
                                <label className="field">
                                    <span className="label">Nombre</span>
                                    <input className="input" value={newReview.authorName} onChange={e => setNewReview(p => ({ ...p, authorName: e.target.value }))} />
                                </label>
                                <label className="field">
                                    <span className="label">Calificación</span>
                                    <select className="input" value={newReview.rating} onChange={e => setNewReview(p => ({ ...p, rating: Number(e.target.value) }))}>
                                        {[5,4,3,2,1].map(v => <option key={v} value={v}>{v}</option>)}
                                    </select>
                                </label>
                            </div>
                            <label className="field">
                                <span className="label">Comentario</span>
                                <textarea className="input" rows={3} value={newReview.comment} onChange={e => setNewReview(p => ({ ...p, comment: e.target.value }))} />
                            </label>
                            <div style={{ display:'flex', gap:8, justifyContent:'flex-end' }}>
                                <button className="btn" onClick={() => { setReplyTo(null); setNewReview({ authorName:'', comment:'', rating:5 }); }}>Cancelar</button>
                                <button className="btn btn-primary" disabled={posting} onClick={async () => {
                                    try {
                                        if (!ticket?.flightId) return;
                                        setPosting(true);
                                        const flightId = String(ticket.flightId);
                                        await agencyReviewsApi.create({ flightId, parentId: replyTo, authorName: newReview.authorName || 'Usuario', rating: Number(newReview.rating)||0, comment: newReview.comment||'' });
                                        setReplyTo(null);
                                        setNewReview({ authorName:'', comment:'', rating:5 });
                                        const res = await agencyReviewsApi.list({ flightId, mode: 'tree' });
                                        const items = Array.isArray(res?.reviews) ? res.reviews : [];
                                        setAgencyReviews({ items, loading:false, error:'' });
                                    } catch (e) {
                                        alert('No se pudo guardar el comentario');
                                    } finally { setPosting(false); }
                                }}>{posting ? 'Guardando...' : 'Enviar'}</button>
                            </div>
                        </div>
                    )}

                    <div className="card" style={{ marginTop:8 }}>
                        <h4 style={{ marginTop:0 }}>Agregar comentario</h4>
                        <div className="small" style={{ marginBottom:8 }}>Publicando como <strong>{getCurrentUserName()}</strong></div>
                        <div style={{ marginBottom:8 }}><StarInput value={newReview.rating} onChange={v => setNewReview(p => ({ ...p, rating: v }))} /></div>
                        <label className="field">
                            <span className="label">Comentario</span>
                            <textarea className="input" rows={3} value={newReview.comment} onChange={e => setNewReview(p => ({ ...p, comment: e.target.value }))} />
                        </label>
                        <div style={{ display:'flex', gap:8, justifyContent:'flex-end' }}>
                            <button className="btn btn-primary" disabled={posting} onClick={async () => {
                                try {
                                    if (!ticket?.flightId) return;
                                    setPosting(true);
                                    const flightId = String(ticket.flightId);
                                    await agencyReviewsApi.create({ flightId, authorName: getCurrentUserName(), rating: Number(newReview.rating)||0, comment: newReview.comment||'' });
                                    setNewReview({ authorName:'', comment:'', rating:5 });
                                    const res = await agencyReviewsApi.list({ flightId, mode: 'tree' });
                                    const items = Array.isArray(res?.reviews) ? res.reviews : [];
                                    setAgencyReviews({ items, loading:false, error:'' });
                                } catch (e) {
                                    alert('No se pudo guardar el comentario');
                                } finally { setPosting(false); }
                            }}>{posting ? 'Guardando...' : 'Publicar'}</button>
                        </div>
                    </div>
				</div>
				</div>
			)}
		</section>
	);
}

function CommentsTree({ items = [], level = 0, onReply, replyTo, ReplyForm, StarDisplay }) {
    if (!Array.isArray(items) || items.length === 0) return null;
    return (
        <ul className="clean" style={{ display:'grid', gap:8, marginLeft: level ? 12 : 0 }}>
            {items.map((n, i) => (
                <li key={(n.id || i) + '-' + level} className="airline-card">
                    <div style={{ display:'flex', justifyContent:'space-between' }}>
                        <strong>{n.authorName || 'Usuario'}</strong>
                        {'rating' in n && StarDisplay && <StarDisplay value={n.rating} />}
                    </div>
                    <div className="small">{n.comment || n.text || ''}</div>
                    <div className="small" style={{ display:'flex', gap:8, marginTop:4 }}>
                        {onReply && <button className="btn" onClick={() => onReply(n.id)}>Responder</button>}
                    </div>
                    {replyTo === n.id && ReplyForm && (
                        <div style={{ marginTop:8 }}>
                            <ReplyForm parentId={n.id} />
                        </div>
                    )}
                    {Array.isArray(n.children) && n.children.length > 0 && (
                        <CommentsTree items={n.children} level={level + 1} onReply={onReply} replyTo={replyTo} ReplyForm={ReplyForm} StarDisplay={StarDisplay} />
                    )}
                </li>
            ))}
        </ul>
    );
}

function StarDisplay({ value = 0 }) {
    const full = Math.max(0, Math.min(5, Number(value) || 0));
    return <span className="small">{'★'.repeat(full)}{'☆'.repeat(5-full)}</span>;
}

function StarInput({ value = 5, onChange }) {
    return (
        <div style={{ display:'flex', gap:4 }}>
            {[1,2,3,4,5].map(v => (
                <button key={v} type="button" className="btn" style={{ padding:'2px 6px', borderRadius:8 }} onClick={() => onChange && onChange(v)}>{v <= value ? '★' : '☆'}</button>
            ))}
        </div>
    );
}

function getCurrentUserName() {
    try {
        const rawAirline = localStorage.getItem('user');
        const user = rawAirline ? JSON.parse(rawAirline) : null;
        if (!user) return 'Usuario';
        return user.firstName && user.lastName ? `${user.firstName} ${user.lastName}` : (user.firstName || user.name || user.email || 'Usuario');
    } catch { return 'Usuario'; }
}

function InlineReplyForm({ value, onChange, onSubmit, onCancel, posting, showStars = true }) {
    return (
        <div className="card" style={{ marginTop:8, background:'#f8fafc' }}>
            <div className="small" style={{ marginBottom:8 }}>Publicando como <strong>{getCurrentUserName()}</strong></div>
            {showStars && <div style={{ marginBottom:8 }}><StarInput value={value.rating} onChange={v => onChange(p => ({ ...p, rating: v }))} /></div>}
            <label className="field">
                <span className="label">Comentario</span>
                <textarea className="input" rows={3} value={value.comment} onChange={e => onChange(p => ({ ...p, comment: e.target.value }))} />
            </label>
            <div style={{ display:'flex', gap:8, justifyContent:'flex-end' }}>
                <button className="btn" onClick={onCancel}>Cancelar</button>
                <button className="btn btn-primary" disabled={posting} onClick={onSubmit}>{posting ? 'Guardando...' : 'Enviar'}</button>
            </div>
        </div>
    );
}


