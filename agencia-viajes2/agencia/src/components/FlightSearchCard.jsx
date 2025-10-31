import { useEffect, useMemo, useRef, useState } from 'react';
import OriginDestinationSelector from './OriginDestinationSelector.jsx';
import { aggregatedApi } from '../services/api.js';

export default function FlightSearchCard({ onSearch, onLoadingChange }) {
	const [tab, setTab] = useState('roundtrip'); // roundtrip | oneway
	const [selection, setSelection] = useState({ origin: null, destination: null });
	const [fechaIda, setFechaIda] = useState('');
	const [fechaVuelta, setFechaVuelta] = useState('');
	const [viajeros, setViajeros] = useState(1);
	const [cabina, setCabina] = useState('economy');
	const [searching, setSearching] = useState(false);
	const [error, setError] = useState('');
	const debounceRef = useRef(0);

	const canSearch = useMemo(() => {
		if (!selection.origin || !selection.destination) return false;
		// Ya no requiere fechas obligatorias
		if (tab === 'roundtrip' && fechaIda && !fechaVuelta) return false;
		return true;
	}, [tab, selection, fechaIda, fechaVuelta]);

	function normalize(str) {
		return (str || '').trim().toLowerCase();
	}

	function applyFilters(flights, params) {
		const originName = normalize(selection.origin?.name);
		const destinationName = normalize(selection.destination?.name);
		const departureDate = params.departureDate;
		const seatCategory = (params.seatCategory || '').toUpperCase();
		const passengers = Number(params.passengers || 1);

		console.log('🔍 Filtrando vuelos:', {
			total: flights.length,
			origen: originName,
			destino: destinationName,
			fecha: departureDate,
			pasajeros: passengers
		});

		const filtered = flights.filter(flight => {
			const flightOrigin = normalize(flight.originCity);
			const flightDestination = normalize(flight.destinationCity);
			const flightDate = flight.departureDate || '';
			
			// Coincidencia exacta de origen y destino (sin importar país)
			const matchesOrigin = !originName || flightOrigin === originName;
			const matchesDestination = !destinationName || flightDestination === destinationName;
			// Si no hay fecha de búsqueda, mostrar todos los vuelos
			const matchesDate = !departureDate || !flightDate || flightDate === departureDate;
			
			// Verificar disponibilidad de asientos
			let matchesPassengers = true;
			if (Number.isFinite(passengers) && passengers > 0) {
				const available = Number(flight.availableSeats);
				if (Number.isFinite(available)) {
					matchesPassengers = available >= passengers;
				}
			}
			
			// Verificar categoría de asiento si se especificó
			let matchesSeatCategory = true;
			if (seatCategory && seatCategory !== 'ECONOMY') {
				const fares = flight.fares || {};
				matchesSeatCategory = Object.keys(fares).some(key => 
					normalize(key) === normalize(seatCategory)
				);
			}
			
			const matches = matchesOrigin && matchesDestination && matchesDate && matchesPassengers && matchesSeatCategory;
			return matches;
		});

		console.log('✅ Vuelos filtrados:', filtered.length);
		return filtered;
	}

	function buildSummary(baseSummary = [], filteredFlights = [], errors = []) {
		const summaryMap = new Map();
		(baseSummary || []).forEach(item => {
			summaryMap.set(item.id, {
				id: item.id,
				name: item.name,
				code: item.code,
				error: item.error || null,
				flightsFound: 0,
			});
		});

		(filteredFlights || []).forEach(flight => {
			const id = flight.airlineId || 'unknown';
			if (!summaryMap.has(id)) {
				summaryMap.set(id, {
					id,
					name: flight.airlineName || 'Aerolínea',
					code: flight.airlineCode,
					error: null,
					flightsFound: 0,
				});
			}
			summaryMap.get(id).flightsFound += 1;
		});

		(errors || []).forEach(err => {
			const id = err.id || err.airlineId;
			if (!id) return;
			if (!summaryMap.has(id)) {
				summaryMap.set(id, {
					id,
					name: err.name || 'Aerolínea',
					code: err.code,
					error: err.error || String(err),
					flightsFound: 0,
				});
			} else {
				summaryMap.get(id).error = err.error || summaryMap.get(id).error;
			}
		});

		return Array.from(summaryMap.values());
	}

	async function runSearch() {
		setError('');
		onLoadingChange?.(true);
		setSearching(true);
		try {
			const params = {
				originName: selection.origin?.name,
				originCountry: selection.origin?.country,
				destinationName: selection.destination?.name,
				destinationCountry: selection.destination?.country,
				departureDate: fechaIda,
				returnDate: tab === 'roundtrip' ? fechaVuelta : undefined,
				passengers: viajeros,
				seatCategory: cabina?.toUpperCase?.() || undefined,
			};
			const res = await aggregatedApi.allFlights();
			const list = Array.isArray(res?.flights) ? res.flights : [];
			const filtered = applyFilters(list, params);
			const airlinesSummary = buildSummary(res?.airlinesSummary, filtered, res?.airlinesWithErrors);
			onSearch?.({
				params,
				flights: filtered,
				airlines: airlinesSummary,
				errors: Array.isArray(res?.airlinesWithErrors) ? res.airlinesWithErrors : [],
			});
		} catch (e) {
			setError(e.message || 'Error al buscar vuelos');
			onSearch?.({
				params: { tab, fechaIda, fechaVuelta, viajeros, cabina, origin: selection.origin, destination: selection.destination },
				flights: [],
				airlines: [],
				errors: [{ error: e.message }],
			});
		} finally {
			onLoadingChange?.(false);
			setSearching(false);
		}
	}

	function handleSearchClick() { if (canSearch && !searching) runSearch(); }

	// Búsqueda en vivo con debounce cuando cambian destino/origen/fecha/cabina
	useEffect(() => {
		if (!selection.origin || !selection.destination) return;
		window.clearTimeout(debounceRef.current);
		debounceRef.current = window.setTimeout(() => {
			runSearch();
		}, 400);
		return () => window.clearTimeout(debounceRef.current);
	// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [selection.origin, selection.destination, fechaIda, cabina]);

	return (
		<div className="card" style={{ padding: 20 }}>
			<div className="tabs" style={{ marginBottom: 12 }}>
				<button className={`tab${tab === 'roundtrip' ? ' active' : ''}`} onClick={() => setTab('roundtrip')}>Roundtrip</button>
				<button className={`tab${tab === 'oneway' ? ' active' : ''}`} onClick={() => setTab('oneway')}>One-way</button>
			</div>
			<div className="grid" style={{ gap: 14 }}>
				<OriginDestinationSelector value={selection} onChange={setSelection} />
				<div className="grid-2">
					<label className="field">
						<span className="label">Fecha de salida (opcional)</span>
						<input className="input" type="date" value={fechaIda} onChange={e => setFechaIda(e.target.value)} />
					</label>
					<label className="field">
						<span className="label">{tab === 'roundtrip' ? 'Fecha de regreso (opcional)' : 'Return (disabled)'}</span>
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
					<button className="btn btn-primary" disabled={!canSearch || searching} onClick={handleSearchClick}>{searching ? 'Buscando...' : 'Buscar'}</button>
					{error && <span className="small" style={{ color: 'salmon' }}>{error}</span>}
				</div>
			</div>
		</div>
	);
}
