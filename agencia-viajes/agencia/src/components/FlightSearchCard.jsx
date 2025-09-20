import { useEffect, useMemo, useRef, useState } from 'react';
import OriginDestinationSelector from './OriginDestinationSelector.jsx';
import { integrationsApi } from '../services/api.js';

export default function FlightSearchCard({ onSearch }) {
	const [tab, setTab] = useState('roundtrip'); // roundtrip | oneway
	const [od, setOd] = useState({ origin: '', destination: '' });
	const [fechaIda, setFechaIda] = useState('');
	const [fechaVuelta, setFechaVuelta] = useState('');
	const [viajeros, setViajeros] = useState(1);
	const [cabina, setCabina] = useState('economy');
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState('');
	const debounceRef = useRef(0);

	const canSearch = useMemo(() => {
		if (!od.origin || !od.destination || !fechaIda) return false;
		if (tab === 'roundtrip' && !fechaVuelta) return false;
		return true;
	}, [tab, od, fechaIda, fechaVuelta]);

	async function runSearch() {
		setError('');
		setLoading(true);
		try {
			const params = {
				origin: od.origin,
				destination: od.destination,
				date: fechaIda,
				cabin: cabina.toUpperCase(),
				travelers: viajeros,
			};
			const res = await integrationsApi.flights(params);
			const list = Array.isArray(res.flights) ? res.flights : [];
			onSearch?.({ params, flights: list });
		} catch (e) {
			setError(e.message || 'Error al buscar vuelos');
			onSearch?.({ params: { tab, ...od, fechaIda, fechaVuelta, viajeros, cabina }, flights: [] });
		} finally {
			setLoading(false);
		}
	}

	function handleSearchClick() { if (canSearch && !loading) runSearch(); }

	// Búsqueda en vivo con debounce cuando cambian destino/origen/fecha/cabina
	useEffect(() => {
		if (!od.origin || !od.destination || !fechaIda) return;
		window.clearTimeout(debounceRef.current);
		debounceRef.current = window.setTimeout(() => {
			runSearch();
		}, 400);
		return () => window.clearTimeout(debounceRef.current);
	// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [od.origin, od.destination, fechaIda, cabina]);

	return (
		<div className="card" style={{ padding: 20 }}>
			<div className="tabs" style={{ marginBottom: 12 }}>
				<button className={`tab${tab === 'roundtrip' ? ' active' : ''}`} onClick={() => setTab('roundtrip')}>Roundtrip</button>
				<button className={`tab${tab === 'oneway' ? ' active' : ''}`} onClick={() => setTab('oneway')}>One-way</button>
			</div>
			<div className="grid" style={{ gap: 14 }}>
				<OriginDestinationSelector value={od} onChange={setOd} />
				<div className="grid-2">
					<label className="field">
						<span className="label">Dates</span>
						<input className="input" type="date" value={fechaIda} onChange={e => setFechaIda(e.target.value)} />
					</label>
					<label className="field">
						<span className="label">{tab === 'roundtrip' ? 'Return' : 'Return (disabled)'}</span>
						<input className="input" type="date" value={fechaVuelta} onChange={e => setFechaVuelta(e.target.value)} disabled={tab !== 'roundtrip'} />
					</label>
				</div>
				<div className="grid-2">
					<label className="field">
						<span className="label">Travelers</span>
						<input className="input" type="number" min={1} value={viajeros} onChange={e => setViajeros(parseInt(e.target.value) || 1)} />
					</label>
					<label className="field">
						<span className="label">Cabin</span>
						<select className="input" value={cabina} onChange={e => setCabina(e.target.value)}>
							<option value="economy">Economy</option>
							<option value="premium">Premium economy</option>
							<option value="business">Business</option>
							<option value="first">First</option>
						</select>
					</label>
				</div>
				<div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
					<button className="btn btn-primary" disabled={!canSearch || loading} onClick={handleSearchClick}>{loading ? 'Buscando...' : 'Buscar'}</button>
					{error && <span className="small" style={{ color: 'salmon' }}>{error}</span>}
				</div>
			</div>
		</div>
	);
}
