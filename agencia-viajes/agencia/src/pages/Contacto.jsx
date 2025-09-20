export default function Contacto() {
	return (
		<div className="container" style={{ maxWidth: 980 }}>
			<div className="airline-gradient-sky" style={{ color: 'white', borderRadius: 24, padding: 24, position: 'relative', overflow: 'hidden', marginBottom: 16 }}>
				<div style={{ position: 'absolute', right: 0, top: 0, width: 160, height: 160, opacity: 0.12 }}>
					<svg viewBox="0 0 24 24" fill="currentColor" width="100%" height="100%"><path d="M12 2l3 7 7 1-5 5 1 7-6-3-6 3 1-7-5-5 7-1 3-7z"/></svg>
				</div>
				<div>
					<h1 style={{ fontSize: 26, fontWeight: 900, marginBottom: 8, background: 'linear-gradient(90deg,#fff,#E0F2FE)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>Contacto</h1>
					<p style={{ fontSize: 16, opacity: 0.95 }}>Estamos para ayudarte</p>
				</div>
			</div>

			<section className="airline-card" style={{ display: 'grid', gap: 12, maxWidth: 640 }}>
				<p>Para consultas comerciales o soporte, escríbenos a <strong>soporte@agenciaviajes.demo</strong></p>
				<p>Horario: <strong>Lun-Vie 9:00 - 18:00</strong></p>
				<div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0,1fr))', gap: 12 }}>
					<div className="airline-card" style={{ textAlign: 'center' }}>
						<div style={{ width: 48, height: 48, margin: '0 auto 8px', borderRadius: 12, background: '#E7F0FF', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
							<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2a1 1 0 011.02-.24 12 12 0 003.57.57 1 1 0 011 1V20a1 1 0 01-1 1C10.61 21 3 13.39 3 4a1 1 0 011-1h3.5a1 1 0 011 1c0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/></svg>
						</div>
						<div>Teléfono: +502 1234-5678</div>
					</div>
					<div className="airline-card" style={{ textAlign: 'center' }}>
						<div style={{ width: 48, height: 48, margin: '0 auto 8px', borderRadius: 12, background: '#EAF8EE', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
							<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M4 6h16v12H4z" opacity=".2"/><path d="M4 7h16v10H4zM7 6V4h2v2m6 0V4h2v2M7 14h10M7 11h6"/></svg>
						</div>
						<div>Reservas: reservas@agenciaviajes.demo</div>
					</div>
				</div>
			</section>
		</div>
	);
}
