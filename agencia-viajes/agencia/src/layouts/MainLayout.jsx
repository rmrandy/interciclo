import { Link, NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import BackendBadge from '../components/BackendBadge.jsx';

export default function MainLayout() {
	const { user, logout } = useAuth();
	const isAdmin = (user?.role || 'user') === 'admin';
	return (
		<div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
			<header className="app-header">
				<div className="container" style={{ display: 'flex', gap: 16, alignItems: 'center', justifyContent: 'space-between', padding: '12px 0' }}>
					<Link to="/" className="brand">Agencia de Viajes</Link>
					<nav className="nav">
						<NavLink to="/" end className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Inicio</NavLink>
						<NavLink to="/buscar" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Buscar</NavLink>
						<NavLink to="/resultados" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Resultados</NavLink>
						<NavLink to="/compra" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Compra</NavLink>
						{isAdmin && <NavLink to="/admin" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Admin</NavLink>}
						<NavLink to="/acerca" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Acerca</NavLink>
						<NavLink to="/contacto" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Contacto</NavLink>
						<NavLink to="/terminos" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Términos</NavLink>
						{user && <NavLink to="/mi-perfil" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Mi perfil</NavLink>}
						{user && <NavLink to="/mis-reservas" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>Mis reservas</NavLink>}
					</nav>
					<div className="auth-actions">
						{user ? (
							<>
								<span className="small">Hola, {user.firstName || user.email}</span>
								<button className="btn" onClick={logout}>Salir</button>
							</>
						) : (
							<div className="nav" style={{ gap: 8 }}>
								<Link className="btn" to="/login">Entrar</Link>
								<Link className="btn btn-primary" to="/registro">Registro</Link>
							</div>
						)}
					</div>
				</div>
			</header>
			<main className="app-main">
				<div className="container">
					<Outlet />
				</div>
			</main>
			<footer className="app-footer">
				<div className="container" style={{ display: 'grid', gap: 6, padding: '16px 0', textAlign: 'center' }}>
					<div className="badge"><BackendBadge /></div>
					<span className="small">Pequeño demo de Agencia de Viajes — UI solamente</span>
				</div>
			</footer>
		</div>
	);
}
