import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { airlinesApi, usersApi, getBackendBaseUrl, infoPagesApi } from '../services/api.js';

const emptyAirline = {
	name: '',
	code: '',
	protocol: 'http',
	host: '',
	port: 80,
	basePath: '/',
	apiKey: '', // API Key para autenticación
	endpoints: { search: '/search', book: '/book', cancel: '/cancel', health: '/health' },
	enabled: true,
	timeoutMs: 2000,
	notes: '',
};

const emptyUser = {
	firstName: '', lastName: '', email: '', password: '', age: 18, country: '', passportNumber: '', phone: '', address: '', role: 'user', isActive: true,
};

export default function Admin() {
	// Tabs
	const [tab, setTab] = useState('airlines');

	// Info pages state
	const emptyPage = { title: '', slug: '', description: '', category: 'info', hero: { heading: '', subheading: '', image: '' }, content: { sections: [] }, published: true };
	const [pages, setPages] = useState([]);
	const [pageForm, setPageForm] = useState(emptyPage);
	const [editingSlug, setEditingSlug] = useState(null);

	// Airlines state
	const [items, setItems] = useState([]);
	const [form, setForm] = useState(emptyAirline);
	const [editingId, setEditingId] = useState(null);

	// Users state
	const [users, setUsers] = useState([]);
	const [userForm, setUserForm] = useState(emptyUser);
	const [userEditingId, setUserEditingId] = useState(null);
	const [userQuery, setUserQuery] = useState('');
	const [page, setPage] = useState(1);
	const [total, setTotal] = useState(0);
	const pageSize = 1000; // mostrar todos por defecto (hasta 1000)

	const [error, setError] = useState('');

	async function loadAirlines() {
		try { const res = await airlinesApi.list(); setItems(res.data || []); } catch (e) { setError(e.message || 'Error al cargar aerolíneas'); }
	}
	async function loadUsers() {
		try { const res = await fetchUsers({ q: userQuery, page, pageSize }); setUsers(res.data || []); setTotal(res.total || 0); } catch (e) { setError(e.message || 'Error al cargar usuarios'); }
	}
	useEffect(() => { loadAirlines(); }, []);
	useEffect(() => { loadUsers(); }, [userQuery, page]);
	useEffect(() => { if (tab==='pages') loadPages(); }, [tab]);

	async function fetchUsers({ q, page, pageSize }) {
		const base = await getBackendBaseUrl();
		const qs = new URLSearchParams({ q: q || '', page: String(page), pageSize: String(pageSize) }).toString();
		const res = await fetch(`${base}/users?${qs}`);
		if (!res.ok) throw new Error('Error al cargar usuarios');
		return res.json();
	}

	function update(field, value) { setForm(prev => ({ ...prev, [field]: value })); }
	function updateEndpoint(field, value) { setForm(prev => ({ ...prev, endpoints: { ...prev.endpoints, [field]: value } })); }

	// Info pages CRUD
	async function loadPages() {
		try { const res = await infoPagesApi.list(); setPages(res.data || res?.pages || []); } catch (e) { setError(e.message || 'Error al cargar páginas'); }
	}
	function updatePage(field, value) { setPageForm(prev => ({ ...prev, [field]: value })); }
	function updateHero(field, value) { setPageForm(prev => ({ ...prev, hero: { ...prev.hero, [field]: value } })); }
	async function savePage(e) {
		e?.preventDefault?.(); setError('');
		try {
			if (editingSlug) await infoPagesApi.replaceBySlug(editingSlug, pageForm); else await infoPagesApi.create(pageForm);
			setPageForm(emptyPage); setEditingSlug(null); await loadPages();
		} catch (e) { setError(e.message || 'Error al guardar página'); }
	}
	function editPage(p) { setPageForm({ ...p }); setEditingSlug(p.slug); }
	async function removePage(slug) { if (!confirm('¿Eliminar página?')) return; await infoPagesApi.deleteBySlug(slug); await loadPages(); }

	async function saveAirline(e) {
		e?.preventDefault?.(); setError('');
		try {
			if (editingId) await airlinesApi.update(editingId, form); else await airlinesApi.create(form);
			setForm(emptyAirline); setEditingId(null); await loadAirlines();
		} catch (e) { setError(e.message || 'Error al guardar aerolínea'); }
	}
	function editAirline(it) { setForm({ ...it }); setEditingId(it._id); }
	async function removeAirline(id) { if (!confirm('¿Eliminar aerolínea?')) return; await airlinesApi.remove(id); await loadAirlines(); }

	// Users CRUD
	function updateUser(field, value) { setUserForm(prev => ({ ...prev, [field]: value })); }
	async function saveUser(e) {
		e?.preventDefault?.(); setError('');
		try {
			if (userEditingId) await usersApi.update(userEditingId, userForm); else await usersApi.create(userForm);
			setUserForm(emptyUser); setUserEditingId(null); await loadUsers();
		} catch (e) { setError(e.message || 'Error al guardar usuario'); }
	}
	function editUser(u) { setUserForm({ ...u, password: '' }); setUserEditingId(u.id); }
	async function removeUser(id) { if (!confirm('¿Eliminar usuario?')) return; await usersApi.remove(id); await loadUsers(); }
	async function changeRole(u, role) { await usersApi.update(u.id, { role }); await loadUsers(); }
	async function toggleActive(u) { await usersApi.update(u.id, { isActive: !u.isActive }); await loadUsers(); }

	const totalPages = Math.max(1, Math.ceil(total / pageSize));

	return (
		<section className="section" style={{ maxWidth: 980 }}>
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
				<h2>Administración</h2>
				<div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
					<Link to="/admin/site-config" className="btn btn-primary">
						⚙️ Configuración del Sitio
					</Link>
					<Link to="/admin/corporate-users" className="btn btn-primary">
						👔 Usuarios Empresariales
					</Link>
				</div>
			</div>
			<div className="tabs" style={{ marginBottom: 12 }}>
				<button className={`tab${tab==='airlines' ? ' active' : ''}`} onClick={()=>setTab('airlines')}>Aerolíneas</button>
				<button className={`tab${tab==='users' ? ' active' : ''}`} onClick={()=>setTab('users')}>Usuarios</button>
				<button className={`tab${tab==='pages' ? ' active' : ''}`} onClick={()=>setTab('pages')}>Páginas informativas</button>
			</div>
			{tab==='airlines' && (
				<div className="grid">
					<form onSubmit={saveAirline} className="card grid">
						<div className="grid-3">
							<label className="field"><span className="label">Nombre</span><input className="input" value={form.name} onChange={e=>update('name', e.target.value)} required/></label>
							<label className="field"><span className="label">Código</span><input className="input" value={form.code} onChange={e=>update('code', e.target.value.toUpperCase())} required/></label>
							<label className="field"><span className="label">Protocolo</span>
								<select className="input" value={form.protocol} onChange={e=>update('protocol', e.target.value)}>
									<option value="http">http</option>
									<option value="https">https</option>
								</select>
							</label>
						</div>
						<div className="grid-3">
							<label className="field"><span className="label">Host/IP</span><input className="input" value={form.host} onChange={e=>update('host', e.target.value)} required/></label>
							<label className="field"><span className="label">Puerto</span><input className="input" type="number" value={form.port} onChange={e=>update('port', parseInt(e.target.value)||0)} required/></label>
							<label className="field"><span className="label">Base path</span><input className="input" value={form.basePath} onChange={e=>update('basePath', e.target.value)} required/></label>
						</div>
						<div className="grid-1">
							<label className="field">
								<span className="label">🔑 API Key (Usuario Empresarial)</span>
								<input 
									className="input" 
									value={form.apiKey || ''} 
									onChange={e=>update('apiKey', e.target.value)} 
									placeholder="Ej: abc123xyz456..." 
									style={{ fontFamily: 'monospace', fontSize: '0.9em' }}
								/>
								<span className="small" style={{ color: '#666', marginTop: '4px', display: 'block' }}>
									API Key generado desde la aerolínea para autenticación
								</span>
							</label>
						</div>
						<div className="grid-3">
							<label className="field"><span className="label">Endpoint búsqueda</span><input className="input" value={form.endpoints.search} onChange={e=>updateEndpoint('search', e.target.value)} /></label>
							<label className="field"><span className="label">Endpoint compra</span><input className="input" value={form.endpoints.book} onChange={e=>updateEndpoint('book', e.target.value)} /></label>
							<label className="field"><span className="label">Endpoint cancelación</span><input className="input" value={form.endpoints.cancel} onChange={e=>updateEndpoint('cancel', e.target.value)} /></label>
						</div>
						<div className="grid-3">
							<label className="field"><span className="label">Endpoint health</span><input className="input" value={form.endpoints.health} onChange={e=>updateEndpoint('health', e.target.value)} /></label>
							<label className="field"><span className="label">Timeout (ms)</span><input className="input" type="number" value={form.timeoutMs} onChange={e=>update('timeoutMs', parseInt(e.target.value)||0)} /></label>
							<label className="field"><span className="label">Activo</span>
								<select className="input" value={form.enabled ? '1' : '0'} onChange={e=>update('enabled', e.target.value==='1')}>
									<option value="1">Sí</option>
									<option value="0">No</option>
								</select>
							</label>
						</div>
						<label className="field"><span className="label">Notas</span><input className="input" value={form.notes} onChange={e=>update('notes', e.target.value)} /></label>
						<div style={{ display: 'flex', gap: 8 }}>
							<button className="btn btn-primary" type="submit">{editingId ? 'Actualizar' : 'Crear'} aerolínea</button>
							{editingId && <button className="btn" type="button" onClick={()=>{setForm(emptyAirline); setEditingId(null);}}>Cancelar</button>}
						</div>
						{error && <p className="small" style={{ color: 'salmon' }}>{error}</p>}
					</form>

					<div className="card">
						<h3>Listado</h3>
						<div className="grid" style={{ overflowX: 'auto' }}>
							<table style={{ width: '100%', borderCollapse: 'collapse' }}>
								<thead>
									<tr>
										<th>Nombre</th>
										<th>Código</th>
										<th>Host</th>
										<th>Puerto</th>
										<th>API Key</th>
										<th>Activo</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									{items.map(it => (
										<tr key={it._id} style={{ borderTop: '1px solid #e3e8f3' }}>
											<td>{it.name}</td>
											<td>{it.code}</td>
											<td>{it.host}</td>
											<td>{it.port}</td>
											<td style={{ fontFamily: 'monospace', fontSize: '0.85em' }}>
												{it.apiKey ? `${it.apiKey.substring(0, 8)}...${it.apiKey.substring(it.apiKey.length - 4)}` : '❌ Sin API Key'}
											</td>
											<td>{it.enabled ? '✅ Sí' : '❌ No'}</td>
											<td style={{ textAlign: 'right' }}>
												<button className="btn" onClick={()=>editAirline(it)}>Editar</button>
												<button className="btn" onClick={()=>removeAirline(it._id)}>Eliminar</button>
											</td>
										</tr>
								))}
							</tbody>
						</table>
					</div>
					</div>
				</div>
			)}

			{tab==='users' && (
				<div className="grid">
					<div className="card" style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
						<input className="input" placeholder="Buscar por nombre o email" value={userQuery} onChange={e=>{ setPage(1); setUserQuery(e.target.value); }} style={{ maxWidth: 360 }} />
						{totalPages > 1 && (
							<div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
								<button className="btn" disabled={page<=1} onClick={()=>setPage(p=>Math.max(1,p-1))}>Prev</button>
								<span className="small">Página {page} de {totalPages}</span>
								<button className="btn" disabled={page>=totalPages} onClick={()=>setPage(p=>Math.min(totalPages,p+1))}>Next</button>
							</div>
						)}
					</div>

					<form onSubmit={saveUser} className="card grid">
						<div className="grid-3">
							<label className="field"><span className="label">Nombre</span><input className="input" value={userForm.firstName} onChange={e=>updateUser('firstName', e.target.value)} required/></label>
							<label className="field"><span className="label">Apellido</span><input className="input" value={userForm.lastName} onChange={e=>updateUser('lastName', e.target.value)} required/></label>
							<label className="field"><span className="label">Correo</span><input className="input" type="email" value={userForm.email} onChange={e=>updateUser('email', e.target.value)} required/></label>
						</div>
						<div className="grid-3">
							<label className="field"><span className="label">Contraseña {userEditingId && '(dejar en blanco para no cambiar)'}</span><input className="input" type="password" value={userForm.password} onChange={e=>updateUser('password', e.target.value)} required={!userEditingId}/></label>
							<label className="field"><span className="label">Edad</span><input className="input" type="number" min={18} value={userForm.age} onChange={e=>updateUser('age', parseInt(e.target.value)||18)} /></label>
							<label className="field"><span className="label">País</span><input className="input" value={userForm.country} onChange={e=>updateUser('country', e.target.value)} /></label>
						</div>
						<div className="grid-3">
							<label className="field"><span className="label">Pasaporte</span><input className="input" value={userForm.passportNumber} onChange={e=>updateUser('passportNumber', e.target.value)} /></label>
							<label className="field"><span className="label">Teléfono</span><input className="input" value={userForm.phone} onChange={e=>updateUser('phone', e.target.value)} /></label>
							<label className="field"><span className="label">Dirección</span><input className="input" value={userForm.address} onChange={e=>updateUser('address', e.target.value)} /></label>
						</div>
						<div className="grid-3">
							<label className="field"><span className="label">Rol</span>
								<select className="input" value={userForm.role} onChange={e=>updateUser('role', e.target.value)}>
									<option value="user">user</option>
									<option value="admin">admin</option>
								</select>
							</label>
							<label className="field"><span className="label">Activo</span>
								<select className="input" value={userForm.isActive ? '1' : '0'} onChange={e=>updateUser('isActive', e.target.value==='1')}>
									<option value="1">Sí</option>
									<option value="0">No</option>
								</select>
							</label>
						</div>
						<div style={{ display: 'flex', gap: 8 }}>
							<button className="btn btn-primary" type="submit">{userEditingId ? 'Actualizar' : 'Crear'} usuario</button>
							{userEditingId && <button className="btn" type="button" onClick={()=>{setUserForm(emptyUser); setUserEditingId(null);}}>Cancelar</button>}
						</div>
						{error && <p className="small" style={{ color: 'salmon' }}>{error}</p>}
					</form>

					<div className="card">
						<h3>Usuarios</h3>
						<div className="grid" style={{ overflowX: 'auto' }}>
							<table style={{ width: '100%', borderCollapse: 'collapse' }}>
								<thead>
									<tr>
										<th>Nombre</th>
										<th>Correo</th>
										<th>Rol</th>
										<th>Activo</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									{users.map(u => (
										<tr key={u.id} style={{ borderTop: '1px solid #e3e8f3' }}>
											<td>{u.firstName} {u.lastName}</td>
											<td>{u.email}</td>
											<td>
												<select className="input" value={u.role} onChange={e=>changeRole(u, e.target.value)}>
													<option value="user">user</option>
													<option value="admin">admin</option>
												</select>
											</td>
											<td>
												<button className="btn" onClick={()=>toggleActive(u)}>{u.isActive ? 'Desactivar' : 'Activar'}</button>
											</td>
											<td style={{ textAlign: 'right' }}>
												<button className="btn" onClick={()=>editUser(u)}>Editar</button>
												<button className="btn" onClick={()=>removeUser(u.id)}>Eliminar</button>
											</td>
										</tr>
								))}
							</tbody>
						</table>
					</div>
					</div>
				</div>
			)}

			{tab==='pages' && (
				<div className="grid">
					<form onSubmit={savePage} className="card grid">
						<div className="grid-3">
							<label className="field"><span className="label">Título</span><input className="input" value={pageForm.title} onChange={e=>updatePage('title', e.target.value)} required/></label>
							<label className="field"><span className="label">Slug</span><input className="input" value={pageForm.slug} onChange={e=>updatePage('slug', e.target.value.replace(/\s+/g,'-').toLowerCase())} required disabled={!!editingSlug}/></label>
							<label className="field"><span className="label">Publicado</span>
								<select className="input" value={pageForm.published ? '1' : '0'} onChange={e=>updatePage('published', e.target.value==='1')}>
									<option value="1">Sí</option>
									<option value="0">No</option>
								</select>
							</label>
						</div>
						<label className="field"><span className="label">Descripción</span><input className="input" value={pageForm.description||''} onChange={e=>updatePage('description', e.target.value)} /></label>
						<div className="grid-3">
							<label className="field"><span className="label">Hero Heading</span><input className="input" value={pageForm.hero?.heading||''} onChange={e=>updateHero('heading', e.target.value)} /></label>
							<label className="field"><span className="label">Hero Subheading</span><input className="input" value={pageForm.hero?.subheading||''} onChange={e=>updateHero('subheading', e.target.value)} /></label>
							<label className="field"><span className="label">Hero Imagen</span><input className="input" value={pageForm.hero?.image||''} onChange={e=>updateHero('image', e.target.value)} /></label>
						</div>
						<div style={{ display: 'flex', gap: 8 }}>
							<button className="btn btn-primary" type="submit">{editingSlug ? 'Actualizar' : 'Crear'} página</button>
							{editingSlug && <button className="btn" type="button" onClick={()=>{setPageForm(emptyPage); setEditingSlug(null);}}>Cancelar</button>}
						</div>
						{error && <p className="small" style={{ color: 'salmon' }}>{error}</p>}
					</form>

					<div className="card">
						<h3>Listado</h3>
						<div className="grid" style={{ overflowX: 'auto' }}>
							<table style={{ width: '100%', borderCollapse: 'collapse' }}>
								<thead>
									<tr>
										<th>Título</th>
										<th>Slug</th>
										<th>Publicado</th>
										<th>Actualizado</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									{pages.map(p => (
										<tr key={p._id || p.slug} style={{ borderTop: '1px solid #e3e8f3' }}>
											<td>{p.title}</td>
											<td>{p.slug}</td>
											<td>{p.published ? 'Sí' : 'No'}</td>
											<td>{p.updatedAt ? new Date(p.updatedAt).toLocaleString() : ''}</td>
											<td style={{ textAlign: 'right' }}>
												<button className="btn" onClick={()=>editPage(p)}>Editar</button>
												<button className="btn" onClick={()=>removePage(p.slug)}>Eliminar</button>
											</td>
										</tr>
									))}
								</tbody>
							</table>
						</div>
					</div>
				</div>
			)}
		</section>
	);
}
