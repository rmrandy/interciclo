export default function Acerca() {
	return (
		<div className="container" style={{ maxWidth: 980 }}>
			<div className="airline-gradient-sky" style={{ color: 'white', borderRadius: 24, padding: 24, position: 'relative', overflow: 'hidden', marginBottom: 16 }}>
				<div style={{ position: 'absolute', right: 0, top: 0, width: 160, height: 160, opacity: 0.12 }}>
					<svg viewBox="0 0 24 24" fill="currentColor" width="100%" height="100%"><path d="M12 2l3 7 7 1-5 5 1 7-6-3-6 3 1-7-5-5 7-1 3-7z"/></svg>
				</div>
				<div>
					<h1 style={{ fontSize: 26, fontWeight: 900, marginBottom: 8, background: 'linear-gradient(90deg,#fff,#E0F2FE)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>Acerca de la Agencia</h1>
					<p style={{ fontSize: 16, opacity: 0.95 }}>Quiénes somos y cómo te ayudamos a viajar mejor</p>
				</div>
			</div>

			<section className="airline-card" style={{ display: 'grid', gap: 12 }}>
				<p>
					Somos una agencia que integra múltiples Sistemas Proveedores (aerolíneas y cadenas hoteleras) para ofrecerte búsqueda unificada, precios competitivos y una experiencia transparente.
				</p>
				<p>
					Este demo se enfoca en la UI. Las integraciones reales y el registro de operaciones en las BD de los proveedores quedan fuera del alcance de esta entrega.
				</p>
			</section>
		</div>
	);
}
