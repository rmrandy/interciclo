// API service for Django backend
// - Usa la misma IP/host que el navegador (window.location.hostname)
// - Autodetecta el puerto del backend probando /health en puertos candidatos
// - Respeta VITE_API_BASE_URL, VITE_DJANGO_BASE_URL y VITE_DJANGO_PORT si se definen

const DEFAULT_PORTS = () => {
	const envPort = import.meta?.env?.VITE_DJANGO_PORT ? Number(import.meta.env.VITE_DJANGO_PORT) : null;
	const ports = [];
	if (envPort) ports.push(envPort);
	
	ports.push( 5001, 5002, 8000, 8080);
	return Array.from(new Set(ports));
};

function readMetaBackend() {
	try {
		const meta = typeof document !== 'undefined' ? (document.querySelector('meta[name="backend-base"]') || document.querySelector('meta[name="backend-url"]')) : null;
		const content = meta?.getAttribute('content');
		if (content && typeof content === 'string' && content.trim()) return content.replace(/\/$/, '');
	} catch {}
	return null;
}

function getEnvBackend() {
	const explicit = import.meta?.env?.VITE_API_BASE_URL;
	if (explicit && typeof explicit === 'string' && explicit.trim()) {
		return explicit.replace(/\/$/, '');
	}
	const base = import.meta?.env?.VITE_DJANGO_BASE_URL;
	if (base && typeof base === 'string' && base.trim().length > 0) {
		return base.replace(/\/$/, '') + '/api';
	}
	return null;
}

let resolvedBaseUrlPromise = null;

async function detectBackendBaseUrl() {
    // - Modo proxy (mismo origen): usar ruta relativa /api
    if (import.meta?.env?.VITE_USE_DEV_PROXY === '1') {
        return '/api';
    }
    // 0) Forzar backend si se proporciona por env
    const env = getEnvBackend();
    if (env) return env;

    // 1) Meta tag
    const meta = readMetaBackend();
    if (meta) return meta.replace(/\/$/, '') + (meta.endsWith('/api') ? '' : '/api');

    // 2) Autodetección basada en salud (sin tocar endpoints de integraciones)
    const host = (typeof window !== 'undefined' && window.location && window.location.hostname) ? window.location.hostname : 'localhost';
    const protocol = (typeof window !== 'undefined' && window.location && window.location.protocol === 'https:') ? 'https' : 'http';
    const envNodePort = (import.meta?.env?.VITE_NODE_PORT && String(import.meta.env?.VITE_NODE_PORT).trim()) || '';
    const candidates = [];
    if (envNodePort) candidates.push(Number(envNodePort));
    DEFAULT_PORTS().forEach(p => candidates.push(p));
    const unique = Array.from(new Set(candidates));

    for (const port of unique) {
        const baseNoApi = `${protocol}://${host}:${port}`;
        const okApiHealth = await probeHealth(`${baseNoApi}/api/health`, 1000);
        if (okApiHealth) return `${baseNoApi}/api`;
        const okHealth = await probeHealth(`${baseNoApi}/health`, 800);
        if (okHealth) return `${baseNoApi}/api`;
    }

    // 3) Fallback seguro (mismo host:5001)
    return `${protocol}://${host}:5001/api`;
}

function withTimeout(ms) {
	const ctrl = new AbortController();
	const id = setTimeout(() => ctrl.abort(), ms);
	return { signal: ctrl.signal, cancel: () => clearTimeout(id) };
}

async function probeHealth(url, timeoutMs) {
	try {
		const t = withTimeout(timeoutMs);
		const res = await fetch(url, { method: 'GET', signal: t.signal, headers: { 'Accept': 'application/json' } });
		t.cancel();
		if (!res.ok) return false;
		// Opcional: validar JSON success
		const ct = res.headers.get('content-type') || '';
		if (ct.includes('application/json')) {
			const data = await res.json().catch(() => null);
			return Boolean(data);
		}
		return true;
	} catch {
		return false;
	}
}

async function probePath(url, timeoutMs) {
    try {
        const t = withTimeout(timeoutMs);
        const res = await fetch(url, { method: 'GET', signal: t.signal, headers: { 'Accept': 'application/json' } });
        t.cancel();
        return res.ok;
    } catch {
        return false;
    }
}

async function getResolvedBaseUrl() {
	if (!resolvedBaseUrlPromise) {
		resolvedBaseUrlPromise = detectBackendBaseUrl();
	}
	return resolvedBaseUrlPromise;
}

// Export público para mostrar la URL detectada en la UI
export async function getBackendBaseUrl() {
	return getResolvedBaseUrl();
}

async function request(path, { method = 'GET', body, token } = {}) {
	const base = await getResolvedBaseUrl();
	const headers = { 'Content-Type': 'application/json' };
	if (token) headers['Authorization'] = `Bearer ${token}`;
	const url = `${base}${path.startsWith('/') ? path : `/${path}`}`;
	console.log(`[API] ${method} ${url}`, body ? { body } : '');
	const res = await fetch(url, {
		method,
		headers,
		body: body ? JSON.stringify(body) : undefined,
	});
	const contentType = res.headers.get('content-type') || '';
	let data;
	if (contentType.includes('application/json')) {
		data = await res.json();
	} else {
		data = await res.text();
	}
	console.log(`[API] Respuesta ${res.status}:`, data);
	if (!res.ok) {
		const message = (data && data.message) || 'Error de red';
		throw new Error(message);
	}
	return data;
}

export function apiPost(path, body, token) {
	return request(path, { method: 'POST', body, token });
}

export function apiGet(path, token) {
	return request(path, { method: 'GET', token });
}

export async function apiGetBlob(path) {
    const base = await getResolvedBaseUrl();
    const url = `${base}${path.startsWith('/') ? path : `/${path}`}`;
    const res = await fetch(url, { method: 'GET' });
    const contentType = res.headers.get('content-type') || '';
    const status = res.status;
    const blob = await res.blob();
    if (!res.ok) return { ok: false, status, contentType, blob };
    return { ok: true, status, contentType, blob };
}

export function apiPatch(path, body, token) {
	return request(path, { method: 'PATCH', body, token });
}

export function apiDelete(path, token) {
	return request(path, { method: 'DELETE', token });
}

export const authApi = {
    login: (email, password) => apiPost('/auth/login', { email, password }),
    register: (payload) => apiPost('/auth/register', payload),
    profile: (token) => apiGet('/auth/profile', token),
};

export const airlinesApi = {
	list: () => apiGet('/airlines'),
	create: (payload) => apiPost('/airlines', payload),
	get: (id) => apiGet(`/airlines/${id}`),
	update: (id, payload) => apiPatch(`/airlines/${id}`, payload),
	remove: (id) => apiDelete(`/airlines/${id}`),
};

export const usersApi = {
	list: () => apiGet('/users'),
	create: (payload) => apiPost('/users', payload),
	get: (id) => apiGet(`/users/${id}`),
	update: (id, payload) => apiPatch(`/users/${id}`, payload),
	remove: (id) => apiDelete(`/users/${id}`),
};

function toQuery(params) {
	if (!params) return '';
	const search = new URLSearchParams();
	Object.entries(params).forEach(([k,v]) => {
		if (v !== undefined && v !== null && String(v).trim() !== '') search.set(k, String(v));
	});
	const qs = search.toString();
	return qs ? `?${qs}` : '';
}

export const integrationsApi = {
	cities: () => apiGet('/integrations/airline/cities'),
	flights: (params) => apiGet(`/integrations/airline/flights${toQuery(params)}`),
	// Endpoints usados por Compra.jsx
	// Si el backend aún no los expone, estas funciones retornarán error manejado en la UI
	seats: (flightId) => apiGet(`/integrations/airline/seats${toQuery({ flightId })}`),
    createTicket: (payload) => apiPost('/integrations/airline/tickets', payload),
    // login/registro de aerolínea solo se usa desde Compra; el login principal de la agencia usa authApi
    loginAirline: (email, password) => apiPost('/integrations/airline/login', { email, password }),
    registerAirline: (payload) => apiPost('/integrations/airline/register', payload),
    ticketsList: (params) => apiGet(`/integrations/airline/tickets-list${toQuery(params)}`),
    ticketPdf: (ticketId) => apiGetBlob(`/integrations/airline/tickets/${encodeURIComponent(ticketId)}/pdf`),
    ticketById: (ticketId) => apiGet(`/integrations/airline/tickets/${encodeURIComponent(ticketId)}`),
    flightReviews: (flightId, params) => apiGet(`/integrations/airline/flights/${encodeURIComponent(flightId)}/reviews${toQuery(params)}`),
};

export const agencyReviewsApi = {
    list: (params) => apiGet(`/agency/reviews${toQuery(params)}`),
    create: (payload) => apiPost('/agency/reviews', payload),
};

export const aggregatedApi = {
	 cities: () => apiGet('/flights/aggregated-cities'),
	 searchFlights: (params) => apiGet(`/flights/aggregated-search${toQuery(params)}`),
	 allFlights: () => apiGet('/flights/aggregated-all'),
	 seats: ({ airlineId, flightId }) => apiGet(`/flights/aggregated-seats${toQuery({ airlineId, flightId })}`),
	 purchase: (payload) => apiPost('/flights/aggregated-purchase', payload),
};

// Informative pages (agencia)
export const infoPagesApi = {
	list: (params) => apiGet(`/info-pages${toQuery(params)}`),
	getBySlug: (slug) => apiGet(`/info-pages/slug/${encodeURIComponent(slug)}`),
	create: (payload) => apiPost('/info-pages', payload),
	updateBySlug: (slug, payload) => apiPatch(`/info-pages/slug/${encodeURIComponent(slug)}`, payload),
	replaceBySlug: (slug, payload) => request(`/info-pages/slug/${encodeURIComponent(slug)}`, { method: 'PUT', body: payload }),
	deleteBySlug: (slug) => apiDelete(`/info-pages/slug/${encodeURIComponent(slug)}`)
};

// Site configuration (configuración del sitio)
export const siteConfigApi = {
	get: () => apiGet('/site-config'),
	update: (payload) => request('/site-config', { method: 'PUT', body: payload }),
};

// Flight cancellations (notificaciones de cancelación)
export const flightCancellationsApi = {
	list: (params) => apiGet(`/flight-cancellations${toQuery(params)}`),
	markRead: (notificationId) => request(`/flight-cancellations/${notificationId}/read`, { method: 'PUT' }),
	markAllRead: () => request('/flight-cancellations/mark-all-read', { method: 'PUT' }),
	delete: (notificationId) => apiDelete(`/flight-cancellations/${notificationId}`),
};
