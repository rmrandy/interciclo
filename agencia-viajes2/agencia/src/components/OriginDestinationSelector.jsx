import { useEffect, useMemo, useState } from 'react';
import { aggregatedApi } from '../services/api.js';

export default function OriginDestinationSelector({ value, onChange }) {
	const [originOption, setOriginOption] = useState(null);
	const [destinationOption, setDestinationOption] = useState(null);
	const [cityGroups, setCityGroups] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	useEffect(() => {
		onChange?.({ origin: originOption?.group || null, destination: destinationOption?.group || null });
	}, [originOption, destinationOption]);

	useEffect(() => {
		let mounted = true;
		setLoading(true);
		aggregatedApi.cities().then(res => {
			if (!mounted) return;
			const list = Array.isArray(res?.cities) ? res.cities : [];
			setCityGroups(list);
		}).catch(e => {
			if (!mounted) return;
			setError(e.message || 'Error al cargar ciudades');
		}).finally(() => mounted && setLoading(false));
		return () => { mounted = false; };
	}, []);

	const cityOptions = useMemo(() => {
		return cityGroups.map(group => ({
			key: `${(group.name || '').toLowerCase()}|${(group.country || '').toLowerCase()}`,
			label: group.label || group.name || 'Ciudad',
			group,
		}));
	}, [cityGroups]);

	useEffect(() => {
		if (destinationOption && !cityOptions.find(c => c.key === destinationOption.key)) {
			setDestinationOption(null);
		}
	}, [cityOptions]);

	useEffect(() => {
		if (value?.origin) {
			const found = cityOptions.find(o => o.group.name === value.origin.name && o.group.country === value.origin.country);
			if (found) setOriginOption(found);
		}
		if (value?.destination) {
			const found = cityOptions.find(o => o.group.name === value.destination.name && o.group.country === value.destination.country);
			if (found) setDestinationOption(found);
		}
	// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [cityOptions.length]);

	const originValue = originOption?.key || '';
	const destinationValue = destinationOption?.key || '';

	function handleOriginChange(e) {
		const selected = cityOptions.find(o => o.key === e.target.value) || null;
		setOriginOption(selected);
		// Reset destination if it no longer matches
		if (destinationOption && selected && destinationOption.key === selected.key) {
			setDestinationOption(null);
		}
	}

	function handleDestinationChange(e) {
		const selected = cityOptions.find(o => o.key === e.target.value) || null;
		setDestinationOption(selected);
		}

	if (loading) {
		return (
			<div className="grid-2">
				<div className="field">
					<span className="label">Origen</span>
					<div className="input" style={{ background: '#f3f4f6', color: '#6b7280' }}>Cargando ciudades...</div>
				</div>
				<div className="field">
					<span className="label">Destino</span>
					<div className="input" style={{ background: '#f3f4f6', color: '#6b7280' }}>Cargando ciudades...</div>
				</div>
			</div>
		);
	}

	return (
		<div className="grid-2">
			<label className="field">
				<span className="label">Origen ({cityOptions.length} ciudades)</span>
				<select className="input" value={originValue} onChange={handleOriginChange} disabled={!!error}>
					<option value="">Selecciona origen</option>
					{cityOptions.map(o => (
						<option key={o.key} value={o.key}>{o.label}</option>
					))}
				</select>
			</label>
			<label className="field">
				<span className="label">Destino ({cityOptions.length} ciudades)</span>
				<select className="input" value={destinationValue} onChange={handleDestinationChange} disabled={!originOption || !!error}>
					<option value="">Selecciona destino</option>
					{cityOptions.map(d => (
						<option key={d.key} value={d.key}>{d.label}</option>
					))}
				</select>
			</label>
			{error && <span className="small" style={{ gridColumn: '1 / -1', color: 'salmon' }}>{error}</span>}
		</div>
	);
}
