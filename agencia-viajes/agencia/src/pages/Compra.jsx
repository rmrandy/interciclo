import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { aggregatedApi, integrationsApi } from '../services/api.js';

export default function Compra() {
	const [params] = useSearchParams();
	const tipo = 'vuelo';
	const flightId = params.get('flightId') || params.get('item') || '';
	const airlineId = params.get('airlineId') || '';
	const priceParam = Number(params.get('price') || params.get('precio') || 0) || 0;
    const [form, setForm] = useState({
		firstName: '',
		lastName: '',
		email: '',
        passport: '',
        phone: '',
        seatCategory: 'ECONOMY',
        seatNumber: '',
	});
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(null);
    const [available, setAvailable] = useState({ FIRST_CLASS: [], BUSINESS: [], ECONOMY: [] });
    const [seatInfo, setSeatInfo] = useState({ totalSeats: 0, firstClassCount: 0, businessCount: 0, economyCount: 0 });
    const [loadingSeats, setLoadingSeats] = useState(false);
    const [seatsError, setSeatsError] = useState('');
    const [airlineUser, setAirlineUser] = useState(null);
    const [airlineLogin, setAirlineLogin] = useState({ email: '', password: '' });
    const [airlineLoggingIn, setAirlineLoggingIn] = useState(false);
    const [quantity, setQuantity] = useState(1);
    const [payment, setPayment] = useState({ cardNumber: '', cardName: '', expiry: '', cvv: '' });

	function update(field, value) { setForm(prev => ({ ...prev, [field]: value })); }

    const requiredFields = useMemo(() => ['firstName', 'lastName', 'email', 'passport', 'seatCategory'], []);

    // Prefill con datos del usuario de Aerolínea guardado por la SPA de aerolínea,
    // con respaldo usando el usuario autenticado en la Agencia (agencia_auth)
    useEffect(() => {
        try {
            const rawAirline = localStorage.getItem('airline_user');
            const rawAgency = localStorage.getItem('agencia_auth');
            const airlineUser = rawAirline ? JSON.parse(rawAirline) : null;
            const agencyAuth = rawAgency ? JSON.parse(rawAgency) : null;
            const agencyUser = agencyAuth?.user || null;
            const source = airlineUser || agencyUser || null;
            if (airlineUser) setAirlineUser(airlineUser);
            if (source) {
                setForm(prev => ({
                    ...prev,
                    firstName: source.firstName || source.name || prev.firstName,
                    lastName: source.lastName || source.surname || prev.lastName,
                    email: source.email || prev.email,
                    phone: source.phone || source.telephone || prev.phone,
                }));
                if (!airlineUser && agencyUser?.email) {
                    setAirlineLogin({ email: agencyUser.email, password: '' });
                }
            }
        } catch {}
    }, []);

    // Cargar asientos disponibles del vuelo seleccionado desde Aerolínea (proxy Django)
    useEffect(() => {
        if (!airlineId || !flightId) {
            setError('Faltan datos del vuelo seleccionado. Regresa a la búsqueda e intenta de nuevo.');
            return;
        }
        setSeatsError('');
        setLoadingSeats(true);
        aggregatedApi.seats({ airlineId, flightId }).then(res => {
            const seats = res?.seatsByCategory || {};
            const next = {
                FIRST_CLASS: seats.FIRST_CLASS || [],
                BUSINESS: seats.BUSINESS || [],
                ECONOMY: seats.ECONOMY || [],
            };
            setAvailable(next);
            const info = res?.seatInfo || null;
            if (info) setSeatInfo({
                totalSeats: info.totalSeats || 0,
                firstClassCount: info.firstClassCount || (next.FIRST_CLASS || []).length,
                businessCount: info.businessCount || (next.BUSINESS || []).length,
                economyCount: info.economyCount || (next.ECONOMY || []).length,
            });
            // Si no hay asiento elegido, sugerir el primero disponible de la categoría actual
            const list = next[form.seatCategory] || [];
            setForm(prev => ({ ...prev, seatNumber: prev.seatNumber || list[0] || '' }));
        }).catch((e) => {
            setAvailable({ FIRST_CLASS: [], BUSINESS: [], ECONOMY: [] });
            setSeatsError(e?.message || 'No se pudieron cargar los asientos disponibles');
        }).finally(() => setLoadingSeats(false));
    }, [airlineId, flightId]);

    // Cuando cambia la categoría, limpiar/sugerir asiento acorde
    useEffect(() => {
        const list = available[form.seatCategory] || [];
        setForm(prev => ({ ...prev, seatNumber: list[0] || '' }));
    }, [form.seatCategory]);

    function doAirlineLogin(e) {
        e?.preventDefault?.();
        setError('');
        setAirlineLoggingIn(true);
        integrationsApi.loginAirline(airlineLogin.email, airlineLogin.password)
            .then(res => {
                // Esperamos formato similar al backend Aerolínea
                const user = res?.user || res?.data?.user || null;
                if (user) {
                    try { localStorage.setItem('user', JSON.stringify(user)); } catch {}
                    setAirlineUser(user);
                    setForm(prev => ({
                        ...prev,
                        firstName: user.firstName || user.name || prev.firstName,
                        lastName: user.lastName || user.surname || prev.lastName,
                        email: user.email || prev.email,
                        phone: user.phone || user.telephone || prev.phone,
                    }));
                } else {
                    setError(res?.message || 'No se pudo iniciar sesión en la Aerolínea');
                }
            })
            .catch(err => setError(err?.message || 'Error de red al iniciar sesión'))
            .finally(() => setAirlineLoggingIn(false));
    }

    function formatCardNumber(value) {
        const digits = (value || '').replace(/\D+/g, '').slice(0, 16);
        return digits.replace(/(\d{4})(?=\d)/g, '$1 ').trim();
    }

    function onCardNumberChange(e) {
        const v = formatCardNumber(e.target.value);
        setPayment(prev => ({ ...prev, cardNumber: v }));
    }

    function onCardNameChange(e) {
        const v = (e.target.value || '').toUpperCase().slice(0, 26);
        setPayment(prev => ({ ...prev, cardName: v }));
    }

    function onExpiryChange(e) {
        let v = (e.target.value || '').replace(/\D+/g, '').slice(0, 4);
        if (v.length >= 3) v = v.slice(0, 2) + '/' + v.slice(2);
        setPayment(prev => ({ ...prev, expiry: v }));
    }

    function onCvvChange(e) {
        const v = (e.target.value || '').replace(/\D+/g, '').slice(0, 4);
        setPayment(prev => ({ ...prev, cvv: v }));
    }

	function comprar(e) {
		e.preventDefault();
        setError(''); setSuccess(null);
        
        // 🏢 COMPRA EMPRESARIAL: No requiere login en aerolínea
        // La agencia usa su usuario empresarial automáticamente mediante API_KEY
        // Solo necesitamos validar que el formulario esté completo
        
        if (!form.firstName || !form.lastName || !form.email) {
            setError('Por favor completa todos los campos requeridos del pasajero.');
            return;
        }
        
        // Construir payload para compra empresarial
        // El backend Django agregará el API_KEY automáticamente
        const payload = {
            airlineId,
            flightId,
            // NO enviamos userId - el backend Java usará el usuario empresarial del API_KEY
            seatNumber: form.seatNumber || 'AUTO',
            seatCategory: form.seatCategory,
            fare:  priceParam,
            quantity: Number.isFinite(Number(quantity)) ? Math.max(1, Math.min(9, Number(quantity))) : 1,
            // Datos del pasajero (cliente de la agencia)
            passengerFirstName: form.firstName,
            passengerLastName: form.lastName,
            passengerDocumentType: 'ID_CARD',
            passengerDocumentNumber: form.passport || 'DOC-' + Date.now(),
            passengerEmail: form.email,
            passengerPhone: form.phone || '',
            specialRequests: '',
            paymentMethod: 'CREDIT_CARD',
            totalAmount: priceParam * (Number(quantity) || 1),
        };
        
        console.log('🏢 Compra empresarial - Payload:', payload);
        setLoading(true);
        aggregatedApi.purchase(payload)
            .then(res => {
                if (res && (res.success || res.ticketId)) {
                    setSuccess(res);
                } else {
                    setError(res?.error || 'No se pudo completar la compra');
                }
            })
            .catch(err => setError(err?.message || 'Error de red'))
            .finally(() => setLoading(false));
	}

	return (
        <section className="section" style={{ maxWidth: 720 }}>
            <div className="airline-gradient-sky airline-shadow-large" style={{ color:'#fff', padding:'24px', borderRadius:16 }}>
                <h2 style={{ margin:0 }}>Compra de vuelo</h2>
                <p className="small" style={{ color:'rgba(255,255,255,0.9)' }}>Completa los datos requeridos para emitir el boleto.</p>
            </div>

            {error && <div className="card" style={{ borderColor:'#fecaca', background:'#fef2f2' }}><strong style={{ color:'#b91c1c' }}>Error:</strong> {error}</div>}
            {success && <div className="card" style={{ borderColor:'#bbf7d0', background:'#f0fdf4' }}><strong style={{ color:'#166534' }}>¡Compra exitosa!</strong><div className="small">Ticket ID: {success.ticketId || '-'}</div></div>}

            {/* 🏢 COMPRA EMPRESARIAL: No requiere login en aerolínea 
                La agencia usa su usuario empresarial automáticamente con API_KEY
            */}

            <form onSubmit={comprar} className="grid card">
                {/* Resumen de disponibilidad por categoría */}
                <div className="grid" style={{ gap:8, marginBottom:4 }}>
                    <div className="small" style={{ display:'flex', flexWrap:'wrap', gap:8 }}>
                        <span className="badge" style={{ background:'#eef2ff', color:'#3730a3', padding:'4px 8px', borderRadius:999 }}>Primera: {seatInfo.firstClassCount || (available.FIRST_CLASS || []).length}</span>
                        <span className="badge" style={{ background:'#ecfeff', color:'#155e75', padding:'4px 8px', borderRadius:999 }}>Business: {seatInfo.businessCount || (available.BUSINESS || []).length}</span>
                        <span className="badge" style={{ background:'#ecfccb', color:'#3f6212', padding:'4px 8px', borderRadius:999 }}>Económica: {seatInfo.economyCount || (available.ECONOMY || []).length}</span>
                        {loadingSeats && <span className="small" style={{ color:'#6b7280' }}>Actualizando disponibilidad…</span>}
                    </div>
                </div>
				<label className="field">
					<span className="label">Nombre</span>
                    <input className="input" value={form.firstName} onChange={e => update('firstName', e.target.value)} required={requiredFields.includes('firstName')} readOnly />
				</label>
				<label className="field">
					<span className="label">Apellido</span>
                    <input className="input" value={form.lastName} onChange={e => update('lastName', e.target.value)} required={requiredFields.includes('lastName')} readOnly />
				</label>
				<label className="field">
					<span className="label">Correo</span>
                    <input className="input" type="email" value={form.email} onChange={e => update('email', e.target.value)} required={requiredFields.includes('email')} readOnly />
				</label>
				<label className="field">
                    <span className="label">Documento</span>
                    <input className="input" value={form.passport} onChange={e => update('passport', e.target.value)} required={requiredFields.includes('passport')} />
				</label>
                <div className="grid-2">
                    <label className="field">
                        <span className="label">Categoría</span>
                        <select className="input" value={form.seatCategory} onChange={e => update('seatCategory', e.target.value)} required>
                            <option value="ECONOMY">Económica</option>
                            <option value="BUSINESS">Business</option>
                            <option value="FIRST_CLASS">Primera</option>
                        </select>
                    </label>
                    <label className="field">
                        <span className="label">Asiento (opcional)</span>
                        <input className="input" placeholder="AUTO" list="seatsList" value={form.seatNumber} onChange={e => update('seatNumber', e.target.value)} />
                        <datalist id="seatsList">
                            {(available[form.seatCategory] || []).map(s => <option key={s} value={s} />)}
                        </datalist>
                        {loadingSeats && <div className="small" style={{ color:'#6b7280', marginTop:4 }}>Cargando asientos...</div>}
                        {seatsError && <div className="small" style={{ color:'salmon', marginTop:4 }}>{seatsError}</div>}
                    </label>
                </div>
                <label className="field">
                    <span className="label">Cantidad</span>
                    <input className="input" inputMode="numeric" value={quantity} onChange={e => setQuantity(() => {
                        const v = (e.target.value || '').replace(/\D+/g, '');
                        const n = Math.max(1, Math.min(9, Number(v || 1)));
                        return n;
                    })} />
                </label>
                <div className="card" style={{ border:'1px dashed #d1d5db' }}>
                    <h3>Pago con tarjeta</h3>
                    <div className="grid-2">
                        <label className="field">
                            <span className="label">Número de tarjeta</span>
                            <input className="input" inputMode="numeric" value={payment.cardNumber} onChange={onCardNumberChange} placeholder="4111 1111 1111 1111" required />
                        </label>
                        <label className="field">
                            <span className="label">Nombre en la tarjeta</span>
                            <input className="input" value={payment.cardName} onChange={onCardNameChange} placeholder="Como aparece en la tarjeta" required />
                        </label>
                        <label className="field">
                            <span className="label">Expira (MM/AA)</span>
                            <input className="input" inputMode="numeric" value={payment.expiry} onChange={onExpiryChange} placeholder="MM/AA" required />
                        </label>
                        <label className="field">
                            <span className="label">CVV</span>
                            <input className="input" inputMode="numeric" value={payment.cvv} onChange={onCvvChange} placeholder="123" required />
                        </label>
                    </div>
                </div>
                <button className="btn btn-primary" disabled={loading}>{loading ? 'Procesando compra...' : 'Confirmar compra'}</button>
			</form>
		</section>
	);
}
