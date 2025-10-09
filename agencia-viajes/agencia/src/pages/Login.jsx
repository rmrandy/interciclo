import { useState } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import { useNavigate } from 'react-router-dom';

export default function Login() {
	const { login } = useAuth();
	const navigate = useNavigate();
	const [email, setEmail] = useState('');
	const [password, setPassword] = useState('');
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState('');

	async function handleSubmit(e) {
		e.preventDefault();
		setError('');
		setLoading(true);
		console.log('[Login] Intentando login con:', email);
		try {
			const res = await login(email, password);
			console.log('[Login] Respuesta de login:', res);
			if (res.ok) navigate('/');
			else setError(res.message || 'Error de inicio de sesión');
		} catch (err) {
			console.error('[Login] Error capturado:', err);
			setError(err.message || 'Error de inicio de sesión');
		} finally {
			setLoading(false);
		}
	}

	return (
		<section className="section" style={{ maxWidth: 480 }}>
			<h2>Iniciar sesión</h2>
			<form onSubmit={handleSubmit} className="grid card">
				<label className="field">
					<span className="label">Correo</span>
					<input className="input" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
				</label>
				<label className="field">
					<span className="label">Contraseña</span>
					<input className="input" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
				</label>
				<button className="btn btn-primary" disabled={loading}>{loading ? 'Ingresando...' : 'Entrar'}</button>
				{error && <p className="small" style={{ color: 'salmon' }}>{error}</p>}
			</form>
		</section>
	);
}
