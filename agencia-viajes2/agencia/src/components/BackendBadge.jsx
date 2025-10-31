import { useEffect, useState } from 'react';
import { getBackendBaseUrl } from '../services/api.js';

export default function BackendBadge() {
	const [url, setUrl] = useState('detectando...');

	useEffect(() => {
		let mounted = true;
		getBackendBaseUrl().then(u => {
			if (mounted) setUrl(u);
		}).catch(() => {
			if (mounted) setUrl('no disponible');
		});
		return () => { mounted = false; };
	}, []);

	return (
		<div style={{ fontSize: 12, color: '#666' }}>
			Backend: {url}
		</div>
	);
}
