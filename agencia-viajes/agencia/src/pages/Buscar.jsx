import { useState } from 'react';
import FlightSearchCard from '../components/FlightSearchCard.jsx';
import FlightResultsList from '../components/FlightResultsList.jsx';

export default function Buscar() {
	const [results, setResults] = useState({ flights: [], params: null });
	const [error, setError] = useState('');
	const [loading, setLoading] = useState(false);

	function handleSearch({ params, flights }) {
		setError('');
		setLoading(false);
		setResults({ flights, params });
	}

	return (
		<section className="section" style={{ maxWidth: 960 }}>
			<h2>Buscar vuelos</h2>
			<FlightSearchCard onSearch={handleSearch} />
			<div className="card" style={{ marginTop: 12 }}>
				<h3 style={{ marginBottom: 6 }}>Resultados</h3>
				<FlightResultsList flights={results.flights} loading={loading} error={error} />
			</div>
		</section>
	);
}
