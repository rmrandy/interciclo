import { useMemo, useState } from 'react';
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
			});
			if (res.ok) navigate('/');
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
				<button className="btn btn-primary" disabled={loading || !isValid}>{loading ? 'Creando...' : 'Crear cuenta'}</button>
				{!isValid && <p className="small" style={{ color: '#a94442' }}>Completa los campos requeridos con formato válido.</p>}
				{error && <p className="small" style={{ color: 'salmon' }}>{error}</p>}
			</form>
		</section>
	);
}
