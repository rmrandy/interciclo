import { useState, useEffect } from 'react';
import { useSiteConfig } from '../contexts/SiteConfigContext.jsx';

export default function SiteConfig() {
	const { config, updateConfig, loading: contextLoading } = useSiteConfig();
	const [formData, setFormData] = useState({
		agencyName: '',
		footer: {
			companyName: '',
			address: '',
			phone: '',
			email: '',
			socialMedia: {
				facebook: '',
				twitter: '',
				instagram: ''
			}
		}
	});
	const [loading, setLoading] = useState(false);
	const [message, setMessage] = useState(null);

	useEffect(() => {
		if (config) {
			setFormData({
				agencyName: config.agencyName || '',
				footer: {
					companyName: config.footer?.companyName || '',
					address: config.footer?.address || '',
					phone: config.footer?.phone || '',
					email: config.footer?.email || '',
					socialMedia: {
						facebook: config.footer?.socialMedia?.facebook || '',
						twitter: config.footer?.socialMedia?.twitter || '',
						instagram: config.footer?.socialMedia?.instagram || ''
					}
				}
			});
		}
	}, [config]);

	const handleSubmit = async (e) => {
		e.preventDefault();
		setLoading(true);
		setMessage(null);

		try {
			await updateConfig(formData);
			setMessage({ type: 'success', text: '✅ Configuración actualizada exitosamente' });
		} catch (error) {
			console.error('Error al actualizar configuración:', error);
			setMessage({ type: 'error', text: '❌ Error al actualizar la configuración' });
		} finally {
			setLoading(false);
		}
	};

	const handleChange = (field, value) => {
		if (field.startsWith('footer.')) {
			const subField = field.replace('footer.', '');
			if (subField.startsWith('socialMedia.')) {
				const socialField = subField.replace('socialMedia.', '');
				setFormData(prev => ({
					...prev,
					footer: {
						...prev.footer,
						socialMedia: {
							...prev.footer.socialMedia,
							[socialField]: value
						}
					}
				}));
			} else {
				setFormData(prev => ({
					...prev,
					footer: {
						...prev.footer,
						[subField]: value
					}
				}));
			}
		} else {
			setFormData(prev => ({
				...prev,
				[field]: value
			}));
		}
	};

	if (contextLoading) {
		return <div style={{ padding: 32, textAlign: 'center' }}>Cargando configuración...</div>;
	}

	return (
		<div style={{ maxWidth: 800, margin: '0 auto', padding: '32px 0' }}>
			<h1 style={{ marginBottom: 24 }}>⚙️ Configuración del Sitio</h1>

			{message && (
				<div
					style={{
						padding: 16,
						marginBottom: 24,
						backgroundColor: message.type === 'success' ? '#d1fae5' : '#fee2e2',
						color: message.type === 'success' ? '#065f46' : '#991b1b',
						borderRadius: 8,
						border: `1px solid ${message.type === 'success' ? '#6ee7b7' : '#fca5a5'}`
					}}
				>
					{message.text}
				</div>
			)}

			<form onSubmit={handleSubmit}>
				{/* Nombre de la agencia */}
				<section style={{ marginBottom: 32, padding: 24, backgroundColor: '#f9fafb', borderRadius: 8 }}>
					<h2 style={{ marginBottom: 16, fontSize: '1.25rem' }}>Nombre de la Agencia</h2>
					<div>
						<label htmlFor="agencyName" style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
							Nombre de la agencia (aparece en el header)
						</label>
						<input
							id="agencyName"
							type="text"
							value={formData.agencyName}
							onChange={(e) => handleChange('agencyName', e.target.value)}
							className="input"
							placeholder="Ej: Agencia de Viajes"
							required
						/>
					</div>
				</section>

				{/* Información del footer */}
				<section style={{ marginBottom: 32, padding: 24, backgroundColor: '#f9fafb', borderRadius: 8 }}>
					<h2 style={{ marginBottom: 16, fontSize: '1.25rem' }}>Información del Footer</h2>
					
					<div style={{ display: 'grid', gap: 16 }}>
						<div>
							<label htmlFor="companyName" style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
								Nombre de la empresa
							</label>
							<input
								id="companyName"
								type="text"
								value={formData.footer.companyName}
								onChange={(e) => handleChange('footer.companyName', e.target.value)}
								className="input"
								placeholder="Ej: Agencia de Viajes S.A."
							/>
						</div>

						<div>
							<label htmlFor="address" style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
								Dirección
							</label>
							<input
								id="address"
								type="text"
								value={formData.footer.address}
								onChange={(e) => handleChange('footer.address', e.target.value)}
								className="input"
								placeholder="Ej: Av. Principal 123, Ciudad"
							/>
						</div>

						<div>
							<label htmlFor="phone" style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
								Teléfono
							</label>
							<input
								id="phone"
								type="text"
								value={formData.footer.phone}
								onChange={(e) => handleChange('footer.phone', e.target.value)}
								className="input"
								placeholder="Ej: +1 234 567 890"
							/>
						</div>

						<div>
							<label htmlFor="email" style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
								Email
							</label>
							<input
								id="email"
								type="email"
								value={formData.footer.email}
								onChange={(e) => handleChange('footer.email', e.target.value)}
								className="input"
								placeholder="Ej: info@agencia.com"
							/>
						</div>
					</div>
				</section>

				{/* Redes sociales */}
				<section style={{ marginBottom: 32, padding: 24, backgroundColor: '#f9fafb', borderRadius: 8 }}>
					<h2 style={{ marginBottom: 16, fontSize: '1.25rem' }}>Redes Sociales</h2>
					
					<div style={{ display: 'grid', gap: 16 }}>
						<div>
							<label htmlFor="facebook" style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
								📘 Facebook (URL completa)
							</label>
							<input
								id="facebook"
								type="url"
								value={formData.footer.socialMedia.facebook}
								onChange={(e) => handleChange('footer.socialMedia.facebook', e.target.value)}
								className="input"
								placeholder="https://facebook.com/tu-pagina"
							/>
						</div>

						<div>
							<label htmlFor="twitter" style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
								🐦 Twitter/X (URL completa)
							</label>
							<input
								id="twitter"
								type="url"
								value={formData.footer.socialMedia.twitter}
								onChange={(e) => handleChange('footer.socialMedia.twitter', e.target.value)}
								className="input"
								placeholder="https://twitter.com/tu-usuario"
							/>
						</div>

						<div>
							<label htmlFor="instagram" style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
								📷 Instagram (URL completa)
							</label>
							<input
								id="instagram"
								type="url"
								value={formData.footer.socialMedia.instagram}
								onChange={(e) => handleChange('footer.socialMedia.instagram', e.target.value)}
								className="input"
								placeholder="https://instagram.com/tu-usuario"
							/>
						</div>
					</div>
				</section>

				<div style={{ display: 'flex', gap: 12 }}>
					<button
						type="submit"
						className="btn btn-primary"
						disabled={loading}
						style={{ flex: 1 }}
					>
						{loading ? 'Guardando...' : '💾 Guardar cambios'}
					</button>
				</div>
			</form>
		</div>
	);
}

