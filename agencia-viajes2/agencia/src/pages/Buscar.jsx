import { useMemo, useState } from 'react';
import FlightSearchCard from '../components/FlightSearchCard.jsx';
import FlightResultsList from '../components/FlightResultsList.jsx';

export default function Buscar() {
	const [results, setResults] = useState({ flights: [], params: null, airlines: [], errors: [] });
	const [error, setError] = useState('');
	const [loading, setLoading] = useState(false);
	const [selectedAirlines, setSelectedAirlines] = useState([]);

	function handleSearch({ params, flights, airlines, errors }) {
		setError(errors && errors.length ? 'Algunas aerolíneas no respondieron' : '');
		setLoading(false);
		setResults({ flights, params, airlines: airlines || [], errors: errors || [] });
		setSelectedAirlines([]); // reset filtros cuando hay búsqueda nueva
	}

	const airlineOptions = useMemo(() => {
		return (results.airlines || []).map(item => ({
			id: item.id,
			name: item.name || 'Aerolínea',
			code: item.code,
			count: item.flightsFound || 0,
			error: item.error || null,
		}));
	}, [results.airlines]);

	const filteredFlights = useMemo(() => {
		if (!selectedAirlines.length) return results.flights;
		return (results.flights || []).filter(f => selectedAirlines.includes(f.airlineId));
	}, [results.flights, selectedAirlines]);

	function toggleAirlineFilter(airlineId) {
		setSelectedAirlines(prev => {
			if (!airlineId) return prev;
			return prev.includes(airlineId)
				? prev.filter(id => id !== airlineId)
				: [...prev, airlineId];
		});
	}

	return (
		<section className="section" style={{ maxWidth: 960 }}>
			<h2>Buscar vuelos</h2>
	<FlightSearchCard onSearch={handleSearch} onLoadingChange={setLoading} />
			{airlineOptions.length > 0 && (
				<div className="card" style={{ marginTop: 12, padding: 16 }}>
					<h4 style={{ marginBottom: 10 }}>Aerolíneas encontradas</h4>
					<div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
						{airlineOptions.map(option => {
							const isSelected = selectedAirlines.includes(option.id);
							return (
								<button
									key={option.id}
									type="button"
									className={`btn btn-small ${isSelected ? 'btn-primary' : ''}`}
									onClick={() => toggleAirlineFilter(option.id)}
									style={{ display: 'flex', alignItems: 'center', gap: 6 }}
								>
									<span>{option.name}{option.code ? ` (${option.code})` : ''}</span>
									<span className="badge" style={{ background: 'rgba(255,255,255,0.2)', color: 'inherit' }}>{option.count}</span>
								</button>
							);
						})}
						{selectedAirlines.length > 0 && (
							<button type="button" className="btn btn-small" onClick={() => setSelectedAirlines([])}>Mostrar todas</button>
						)}
					</div>
				</div>
			)}
			<div className="card" style={{ marginTop: 12 }}>
				<h3 style={{ marginBottom: 6 }}>Resultados</h3>
				<FlightResultsList
					flights={filteredFlights}
					loading={loading}
					error={error}
					airlines={results.airlines}
					warnings={results.errors}
					activeAirlineIds={selectedAirlines}
				/>
			</div>
		</section>
	);
}
