import { useEffect, useMemo, useRef, useState } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import { useNavigate } from 'react-router-dom';

export default function Register() {
	const { register } = useAuth();
	const navigate = useNavigate();
	const [form, setForm] = useState({
		firstName: '',
		lastName: '',
		email: '',
		password: '',
		age: '',
		country: '',
		passportNumber: '',
		phone: '',
		address: '',
	});
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState('');
	const [success, setSuccess] = useState('');
	const [captchaToken, setCaptchaToken] = useState('');
	const captchaRef = useRef(null);
	const siteKey = (import.meta && import.meta.env && import.meta.env.VITE_RECAPTCHA_SITE_KEY) || '';

	// Cargar script de reCAPTCHA v2 (checkbox) y renderizar
	useEffect(() => {
		if (!siteKey) return; // si no hay site key, no renderizamos (backend puede aceptar sin captcha)
		function loadScript() {
			if (document.getElementById('recaptcha-api-js')) return Promise.resolve();
			return new Promise((resolve, reject) => {
				const s = document.createElement('script');
				s.id = 'recaptcha-api-js';
				s.src = 'https://www.google.com/recaptcha/api.js?onload=__onRecaptchaLoaded&render=explicit';
				s.async = true; s.defer = true;
				s.onerror = reject; s.onload = () => {};
				document.body.appendChild(s);
				window.__onRecaptchaLoaded = () => resolve();
			});
		}
		loadScript().then(() => {
			if (window.grecaptcha && captchaRef.current) {
				window.grecaptcha.render(captchaRef.current, {
					sitekey: siteKey,
					callback: (token) => setCaptchaToken(token || ''),
					'expired-callback': () => setCaptchaToken(''),
					'error-callback': () => setCaptchaToken(''),
				});
			}
		}).catch(() => {});
	}, [siteKey]);

	function updateField(field, value) {
		setForm(prev => ({ ...prev, [field]: value }));
	}

	const isValid = useMemo(() => {
		const emailOk = /[^@\s]+@[^@\s]+\.[^@\s]+/.test(form.email);
		const passOk = (form.password || '').length >= 8;
		const ageNum = parseInt(form.age, 10);
		const ageOk = !isNaN(ageNum) && ageNum >= 18;
		const passportOk = (form.passportNumber || '').length >= 5;
		return Boolean(form.firstName && form.lastName && emailOk && passOk && ageOk && form.country && passportOk);
	}, [form]);

	async function handleSubmit(e) {
		e.preventDefault();
		setError('');
		setSuccess('');
		setLoading(true);
		try {
			const res = await register({
				firstName: form.firstName,
				lastName: form.lastName,
				email: form.email,
				password: form.password,
				age: parseInt(form.age, 10),
				country: form.country,
				passportNumber: form.passportNumber,
				phone: form.phone || undefined,
				address: form.address || undefined,
				captchaToken: captchaToken || undefined,
			});
			if (res.ok) {
				setSuccess('¡Registro exitoso! Te hemos autenticado en la Aerolínea.');
				setTimeout(() => navigate('/'), 1200);
			}
			else setError(res.message || 'Error de registro');
		} catch (err) {
			setError(err.message || 'Error de registro');
		} finally {
			setLoading(false);
		}
	}

	return (
		<section className="section" style={{ maxWidth: 680 }}>
			<h2>Registro</h2>
			<form onSubmit={handleSubmit} className="grid card">
				<div className="grid-2">
					<label className="field">
						<span className="label">Nombre</span>
						<input className="input" value={form.firstName} onChange={e => updateField('firstName', e.target.value)} required />
					</label>
					<label className="field">
						<span className="label">Apellido</span>
						<input className="input" value={form.lastName} onChange={e => updateField('lastName', e.target.value)} required />
					</label>
				</div>
				<div className="grid-2">
					<label className="field">
						<span className="label">Correo</span>
						<input className="input" type="email" value={form.email} onChange={e => updateField('email', e.target.value)} required />
					</label>
					<label className="field">
						<span className="label">Contraseña</span>
						<input className="input" type="password" value={form.password} onChange={e => updateField('password', e.target.value)} required placeholder="Mínimo 8 caracteres" />
					</label>
				</div>
				<div className="grid-3">
					<label className="field">
						<span className="label">Edad</span>
						<input className="input" type="number" min={18} value={form.age} onChange={e => updateField('age', e.target.value)} required />
					</label>
					<label className="field">
						<span className="label">País</span>
						<input className="input" value={form.country} onChange={e => updateField('country', e.target.value)} required />
					</label>
					<label className="field">
						<span className="label">Pasaporte</span>
						<input className="input" value={form.passportNumber} onChange={e => updateField('passportNumber', e.target.value)} required placeholder="Mínimo 5 caracteres" />
					</label>
				</div>
				<div className="grid-2">
					<label className="field">
						<span className="label">Teléfono (opcional)</span>
						<input className="input" value={form.phone} onChange={e => updateField('phone', e.target.value)} />
					</label>
					<label className="field">
						<span className="label">Dirección (opcional)</span>
						<input className="input" value={form.address} onChange={e => updateField('address', e.target.value)} />
					</label>
				</div>
				{/* reCAPTCHA v2 (opcional) */}
				{siteKey && (
					<div className="field">
						<span className="label">Verificación</span>
						<div ref={captchaRef} />
					</div>
				)}

				<button className="btn btn-primary" disabled={loading || !isValid}>{loading ? 'Creando...' : 'Crear cuenta'}</button>
				{!isValid && <p className="small" style={{ color: '#a94442' }}>Completa los campos requeridos con formato válido.</p>}
				{error && <div className="card" style={{ borderColor:'#fecaca', background:'#fef2f2' }}><strong style={{ color:'#b91c1c' }}>Error:</strong> {error}</div>}
				{success && <div className="card" style={{ borderColor:'#bbf7d0', background:'#f0fdf4' }}><strong style={{ color:'#166534' }}>¡Registro exitoso!</strong><div className="small" style={{ color:'#166534' }}>Serás redirigido en un momento…</div></div>}
			</form>
		</section>
	);
}
