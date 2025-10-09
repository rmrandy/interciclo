import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { integrationsApi, agencyReviewsApi } from '../services/api.js';

export default function FlightDetail() {
	const { id } = useParams();
	const navigate = useNavigate();
	const [flights, setFlights] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');
    const [reviews, setReviews] = useState({ items: [], loading: false, error: '' });
    const [agencyReviews, setAgencyReviews] = useState({ items: [], loading: false, error: '' });
    const [replyTo, setReplyTo] = useState(null);
    const [newReview, setNewReview] = useState({ comment: '', rating: 5 });
    const [posting, setPosting] = useState(false);

	// Importante: calcular `flight` ANTES de usarlo en dependencias de efectos
	const flight = useMemo(() => {
		const target = decodeURIComponent(id || '');
		return flights.find(f => String(f?.idFlight) === target || String(f?.flightNumber) === target);
	}, [flights, id]);

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

	// Cargar reseñas del vuelo
	useEffect(() => {
		if (!flight) return;
		setReviews(prev => ({ ...prev, loading: true, error: '' }));
		const flightId = flight.idFlight || flight.flightId || flight.flightNumber;
        integrationsApi.flightReviews(flightId, { mode: 'tree' })
			.then(res => {
                const items = Array.isArray(res?.reviews) ? res.reviews : (Array.isArray(res) ? res : []);
				setReviews({ items, loading: false, error: '' });
			})
			.catch(e => setReviews({ items: [], loading: false, error: e?.message || 'No se pudieron cargar reseñas' }));
	}, [flight]);

    // Cargar reseñas locales (Agencia) en árbol
    useEffect(() => {
        if (!flight) return;
        setAgencyReviews(prev => ({ ...prev, loading: true, error: '' }));
        const flightId = String(flight.idFlight || flight.flightId || flight.flightNumber);
        agencyReviewsApi.list({ flightId, mode: 'tree' })
            .then(res => {
                const items = Array.isArray(res?.reviews) ? res.reviews : [];
                setAgencyReviews({ items, loading: false, error: '' });
            })
            .catch(e => setAgencyReviews({ items: [], loading: false, error: e?.message || 'No se pudieron cargar comentarios locales' }));
    }, [flight]);

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
                <>
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
                        StarDisplay={StarDisplay}
                        replyTo={replyTo}
                        ReplyForm={({ parentId }) => (
                            <InlineReplyForm
                                value={newReview}
                                onChange={(next) => setNewReview(next)}
                                onCancel={() => { setReplyTo(null); setNewReview({ comment:'', rating:5 }); }}
                                onSubmit={async () => {
                                    try {
                                        if (!flight) return;
                                        setPosting(true);
                                        const flightId = String(flight.idFlight || flight.flightId || flight.flightNumber);
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

                    {/* Formulario raíz */}
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
                                    if (!flight) return;
                                    setPosting(true);
                                    const flightId = String(flight.idFlight || flight.flightId || flight.flightNumber);
                                    await agencyReviewsApi.create({ flightId, authorName: getCurrentUserName(), rating: Number(newReview.rating)||0, comment: newReview.comment||'' });
                                    setNewReview({ comment:'', rating:5 });
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
                </>
            )}
		</section>
	);
}

function CommentsTree({ items = [], level = 0 }) {
    if (!Array.isArray(items) || items.length === 0) return null;
    return (
        <ul className="clean" style={{ display:'grid', gap:8, marginLeft: level ? 12 : 0 }}>
            {items.map((n, i) => (
                <li key={(n.id || i) + '-' + level} className="airline-card">
                    <div style={{ display:'flex', justifyContent:'space-between' }}>
                        <strong>{n.authorName || 'Usuario'}</strong>
                        {'rating' in n && <StarDisplay value={n.rating} />}
                    </div>
                    <div className="small">{n.comment || n.text || ''}</div>
                    {Array.isArray(n.children) && n.children.length > 0 && (
                        <CommentsTree items={n.children} level={level + 1} />
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
                <button key={v} type="button" className="btn" style={{ padding:'2px 6px', borderRadius:8 }} onClick={() => onChange && onChange(v)}>
                    {v <= value ? '★' : '☆'}
                </button>
            ))}
        </div>
    );
}

function getCurrentUserName() {
    try {
        const rawAirline = localStorage.getItem('airline_user');
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


