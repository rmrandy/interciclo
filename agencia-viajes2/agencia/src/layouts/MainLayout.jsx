import { Link, NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useSiteConfig } from '../contexts/SiteConfigContext.jsx';
import BackendBadge from '../components/BackendBadge.jsx';

export default function MainLayout() {
	const { user, logout } = useAuth();
	const { config } = useSiteConfig();
	const isAdmin = (user?.role || 'user') === 'admin';
	return (
		<div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
			<header className="app-header">
				<div className="container" style={{ display: 'flex', gap: 16, alignItems: 'center', justifyContent: 'space-between', padding: '12px 0' }}>
					<Link to="/" className="brand">{config?.agencyName || 'Agencia de Viajes'}</Link>
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
			<div className="container" style={{ padding: '24px 0' }}>
				<div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: 32, marginBottom: 24 }}>
					{/* Información de la empresa */}
					<div>
						<h3 style={{ marginBottom: 12, fontSize: '1.1rem' }}>{config?.footer?.companyName || 'Agencia de Viajes S.A.'}</h3>
						<div style={{ display: 'grid', gap: 8, fontSize: '0.9rem', color: '#666' }}>
							{config?.footer?.address && <p>{config.footer.address}</p>}
							{config?.footer?.phone && <p>📞 {config.footer.phone}</p>}
							{config?.footer?.email && <p>✉️ {config.footer.email}</p>}
						</div>
					</div>
					
					{/* Enlaces rápidos */}
					<div>
						<h3 style={{ marginBottom: 12, fontSize: '1.1rem' }}>Enlaces</h3>
						<nav style={{ display: 'grid', gap: 8 }}>
							<Link to="/acerca" style={{ color: '#666', textDecoration: 'none' }}>Acerca de</Link>
							<Link to="/contacto" style={{ color: '#666', textDecoration: 'none' }}>Contacto</Link>
							<Link to="/terminos" style={{ color: '#666', textDecoration: 'none' }}>Términos y condiciones</Link>
						</nav>
					</div>
					
					{/* Redes sociales */}
					{(config?.footer?.socialMedia?.facebook || config?.footer?.socialMedia?.twitter || config?.footer?.socialMedia?.instagram) && (
						<div>
							<h3 style={{ marginBottom: 12, fontSize: '1.1rem' }}>Síguenos</h3>
							<div style={{ display: 'flex', gap: 12 }}>
								{config?.footer?.socialMedia?.facebook && (
									<a href={config.footer.socialMedia.facebook} target="_blank" rel="noopener noreferrer" style={{ color: '#1877f2', fontSize: '1.5rem' }}>📘</a>
								)}
								{config?.footer?.socialMedia?.twitter && (
									<a href={config.footer.socialMedia.twitter} target="_blank" rel="noopener noreferrer" style={{ color: '#1da1f2', fontSize: '1.5rem' }}>🐦</a>
								)}
								{config?.footer?.socialMedia?.instagram && (
									<a href={config.footer.socialMedia.instagram} target="_blank" rel="noopener noreferrer" style={{ color: '#c13584', fontSize: '1.5rem' }}>📷</a>
								)}
							</div>
						</div>
					)}
				</div>
				
				{/* Barra inferior */}
				<div style={{ borderTop: '1px solid #e5e7eb', paddingTop: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
					<span className="small" style={{ color: '#666' }}>© {new Date().getFullYear()} {config?.footer?.companyName || 'Agencia de Viajes S.A.'}</span>
					<div className="badge"><BackendBadge /></div>
				</div>
			</div>
		</footer>
		</div>
	);
}
