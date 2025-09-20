import { useEffect, useMemo, useState } from 'react';
import { integrationsApi } from '../services/api.js';

export default function OriginDestinationSelector({ value, onChange }) {
	const [origin, setOrigin] = useState(value?.origin || '');
	const [destination, setDestination] = useState(value?.destination || '');
	const [cities, setCities] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	useEffect(() => {
		onChange?.({ origin, destination });
	}, [origin, destination]);

	useEffect(() => {
		let mounted = true;
		setLoading(true);
		integrationsApi.cities().then(res => {
			if (!mounted) return;
			const list = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : []);
			setCities(list);
		}).catch(e => {
			if (!mounted) return;
			setError(e.message || 'Error al cargar ciudades');
		}).finally(() => mounted && setLoading(false));
		return () => { mounted = false; };
	}, []);

	const cityOptions = useMemo(() => cities.map(c => ({
		value: c.name,
		label: c.country ? `${c.name} (${c.country})` : c.name,
	})), [cities]);

	useEffect(() => {
		if (origin && destination && !cityOptions.find(c => c.value === destination)) {
			setDestination('');
		}
	}, [origin, cityOptions]);

	return (
		<div className="grid-2">
			<label className="field">
				<span className="label">Origen</span>
				<select className="input" value={origin} onChange={e => setOrigin(e.target.value)} disabled={loading || !!error}>
					<option value="">Selecciona origen</option>
					{cityOptions.map(o => (
						<option key={o.value} value={o.value}>{o.label}</option>
					))}
				</select>
			</label>
			<label className="field">
				<span className="label">Destino</span>
				<select className="input" value={destination} onChange={e => setDestination(e.target.value)} disabled={!origin || loading || !!error}>
					<option value="">Selecciona destino</option>
					{cityOptions.map(d => (
						<option key={d.value} value={d.value}>{d.label}</option>
					))}
				</select>
			</label>
			{error && <span className="small" style={{ gridColumn: '1 / -1', color: 'salmon' }}>{error}</span>}
		</div>
	);
}
