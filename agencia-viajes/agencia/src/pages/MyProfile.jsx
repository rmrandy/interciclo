import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import { usersApi, authApi } from '../services/api.js';

export default function MyProfile() {
	const { user, token, logout } = useAuth();
	const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phone: '', address: '', country: '', passportNumber: '', age: 18 });
	const [loading, setLoading] = useState(false);
	const [msg, setMsg] = useState('');
	const [error, setError] = useState('');

	useEffect(() => {
		if (!user) return;
		setForm({
			firstName: user.firstName || '',
			lastName: user.lastName || '',
			email: user.email || '',
			phone: user.phone || '',
			address: user.address || '',
			country: user.country || '',
			passportNumber: user.passportNumber || '',
			age: user.age || 18,
		});
	}, [user]);

	function update(field, value) { setForm(prev => ({ ...prev, [field]: value })); }

	async function save(e) {
		e?.preventDefault?.();
		setError(''); setMsg(''); setLoading(true);
		try {
			await usersApi.update(user.id, form);
			setMsg('Perfil actualizado');
		} catch (e) { setError(e.message || 'Error al guardar'); }
		finally { setLoading(false); }
	}

	return (
		<section className="section" style={{ maxWidth: 720 }}>
			<h2>Mi perfil</h2>
			<form onSubmit={save} className="grid card">
				<div className="grid-2">
					<label className="field"><span className="label">Nombre</span><input className="input" value={form.firstName} onChange={e=>update('firstName', e.target.value)} /></label>
					<label className="field"><span className="label">Apellido</span><input className="input" value={form.lastName} onChange={e=>update('lastName', e.target.value)} /></label>
				</div>
				<label className="field"><span className="label">Correo</span><input className="input" type="email" value={form.email} onChange={e=>update('email', e.target.value)} /></label>
				<div className="grid-2">
					<label className="field"><span className="label">Teléfono</span><input className="input" value={form.phone} onChange={e=>update('phone', e.target.value)} /></label>
					<label className="field"><span className="label">Dirección</span><input className="input" value={form.address} onChange={e=>update('address', e.target.value)} /></label>
				</div>
				<div className="grid-3">
					<label className="field"><span className="label">País</span><input className="input" value={form.country} onChange={e=>update('country', e.target.value)} /></label>
					<label className="field"><span className="label">Pasaporte</span><input className="input" value={form.passportNumber} onChange={e=>update('passportNumber', e.target.value)} /></label>
					<label className="field"><span className="label">Edad</span><input className="input" type="number" min={18} value={form.age} onChange={e=>update('age', parseInt(e.target.value)||18)} /></label>
				</div>
				<div style={{ display: 'flex', gap: 8 }}>
					<button className="btn btn-primary" disabled={loading}>{loading ? 'Guardando...' : 'Guardar'}</button>
					<button type="button" className="btn" onClick={logout}>Cerrar sesión</button>
				</div>
				{msg && <p className="small" style={{ color: 'green' }}>{msg}</p>}
				{error && <p className="small" style={{ color: 'salmon' }}>{error}</p>}
			</form>
		</section>
	);
}
