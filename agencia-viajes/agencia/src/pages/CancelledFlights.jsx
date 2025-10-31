import { useState, useEffect } from 'react';
import { flightCancellationsApi } from '../services/api';

export default function CancelledFlights() {
	const [notifications, setNotifications] = useState([]);
	const [loading, setLoading] = useState(true);
	const [filter, setFilter] = useState('all'); // all, unread, read
	const [unreadCount, setUnreadCount] = useState(0);

	const loadNotifications = async () => {
		try {
			setLoading(true);
			const params = filter === 'all' ? {} : { status: filter };
			const response = await flightCancellationsApi.list(params);
			
			if (response?.notifications) {
				setNotifications(response.notifications);
				setUnreadCount(response.unreadCount || 0);
			}
		} catch (error) {
			console.error('Error cargando notificaciones:', error);
		} finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		loadNotifications();
	}, [filter]);

	const markAsRead = async (notificationId) => {
		try {
			await flightCancellationsApi.markRead(notificationId);
			await loadNotifications();
		} catch (error) {
			console.error('Error marcando como leída:', error);
		}
	};

	const markAllAsRead = async () => {
		try {
			await flightCancellationsApi.markAllRead();
			await loadNotifications();
		} catch (error) {
			console.error('Error marcando todas como leídas:', error);
		}
	};

	const deleteNotification = async (notificationId) => {
		if (!confirm('¿Eliminar esta notificación permanentemente?')) return;
		
		try {
			await flightCancellationsApi.delete(notificationId);
			await loadNotifications();
		} catch (error) {
			console.error('Error eliminando notificación:', error);
		}
	};

	const formatDateTime = (dateString) => {
		if (!dateString) return 'N/A';
		const date = new Date(dateString);
		return date.toLocaleString('es', { 
			year: 'numeric', 
			month: 'short', 
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	};

	return (
		<div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 0' }}>
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
				<h1 style={{ margin: 0 }}>🚨 Vuelos Cancelados</h1>
				{unreadCount > 0 && (
					<button
						onClick={markAllAsRead}
						className="btn btn-primary"
					>
						✓ Marcar todas como leídas ({unreadCount})
					</button>
				)}
			</div>

			{/* Filtros */}
			<div style={{ 
				display: 'flex', 
				gap: 12, 
				marginBottom: 24,
				padding: '16px',
				background: '#f9fafb',
				borderRadius: '8px'
			}}>
				<button
					onClick={() => setFilter('all')}
					className={`btn ${filter === 'all' ? 'btn-primary' : ''}`}
				>
					Todas ({notifications.length})
				</button>
				<button
					onClick={() => setFilter('unread')}
					className={`btn ${filter === 'unread' ? 'btn-primary' : ''}`}
				>
					No leídas ({unreadCount})
				</button>
				<button
					onClick={() => setFilter('read')}
					className={`btn ${filter === 'read' ? 'btn-primary' : ''}`}
				>
					Leídas
				</button>
			</div>

			{/* Listado de notificaciones */}
			{loading ? (
				<div style={{ padding: 64, textAlign: 'center', color: '#666' }}>
					<p>Cargando notificaciones...</p>
				</div>
			) : notifications.length === 0 ? (
				<div style={{ 
					padding: 64, 
					textAlign: 'center', 
					background: '#f9fafb',
					borderRadius: '8px'
				}}>
					<p style={{ fontSize: '48px', margin: '0 0 16px 0' }}>✅</p>
					<p style={{ color: '#666', fontSize: '18px', margin: 0 }}>
						{filter === 'unread' 
							? 'No hay notificaciones sin leer' 
							: 'No hay notificaciones'
						}
					</p>
				</div>
			) : (
				<div style={{ display: 'grid', gap: 16 }}>
					{notifications.map((notification) => (
						<div
							key={notification._id}
							style={{
								padding: '24px',
								background: notification.status === 'unread' ? '#fef2f2' : 'white',
								border: `2px solid ${notification.status === 'unread' ? '#dc2626' : '#e5e7eb'}`,
								borderRadius: '8px',
								position: 'relative',
							}}
						>
							{/* Badge de estado */}
							{notification.status === 'unread' && (
								<div style={{
									position: 'absolute',
									top: '12px',
									right: '12px',
									background: '#dc2626',
									color: 'white',
									padding: '4px 12px',
									borderRadius: '12px',
									fontSize: '11px',
									fontWeight: 'bold',
								}}>
									NUEVO
								</div>
							)}

							{/* Encabezado */}
							<div style={{ display: 'flex', gap: 16, alignItems: 'flex-start', marginBottom: 16 }}>
								<div style={{ fontSize: '40px' }}>⚠️</div>
								<div style={{ flex: 1 }}>
									<h2 style={{ margin: '0 0 8px 0', fontSize: '24px', color: '#dc2626' }}>
										Vuelo {notification.flightNumber} Cancelado
									</h2>
									<div style={{ 
										display: 'inline-block',
										background: '#e0f2fe',
										color: '#075985',
										padding: '4px 12px',
										borderRadius: '4px',
										fontSize: '13px',
										fontWeight: '500'
									}}>
										{notification.airlineName || notification.airlineCode}
									</div>
								</div>
							</div>

							{/* Detalles del vuelo */}
							<div style={{ 
								display: 'grid', 
								gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
								gap: 16,
								marginBottom: 16,
								padding: '16px',
								background: 'white',
								borderRadius: '6px',
								border: '1px solid #e5e7eb'
							}}>
								<div>
									<div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>Ruta</div>
									<div style={{ fontWeight: '500', fontSize: '15px' }}>
										{notification.originCity} → {notification.destinationCity}
									</div>
								</div>
								<div>
									<div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>Fecha Programada</div>
									<div style={{ fontWeight: '500', fontSize: '15px' }}>
										{notification.departureDate} {notification.departureTime}
									</div>
								</div>
								<div>
									<div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>Cancelado el</div>
									<div style={{ fontWeight: '500', fontSize: '15px' }}>
										{formatDateTime(notification.cancelledAt || notification.createdAt)}
									</div>
								</div>
							</div>

							{/* Motivo de cancelación */}
							<div style={{ 
								background: '#fffbeb',
								border: '1px solid #fbbf24',
								borderRadius: '6px',
								padding: '16px',
								marginBottom: 16
							}}>
								<div style={{ fontSize: '12px', color: '#92400e', marginBottom: '4px', fontWeight: 'bold' }}>
									📝 MOTIVO DE CANCELACIÓN:
								</div>
								<div style={{ fontSize: '15px', color: '#78350f' }}>
									{notification.cancellationReason}
								</div>
							</div>

							{/* Acciones */}
							<div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
								{notification.status === 'unread' && (
									<button
										onClick={() => markAsRead(notification._id)}
										className="btn btn-primary"
									>
										✓ Marcar como leída
									</button>
								)}
								<button
									onClick={() => deleteNotification(notification._id)}
									className="btn"
									style={{ color: '#dc2626' }}
								>
									🗑️ Eliminar
								</button>
							</div>
						</div>
					))}
				</div>
			)}

			{/* Información adicional */}
			{!loading && notifications.length > 0 && (
				<div style={{ 
					marginTop: 32,
					padding: '16px',
					background: '#f0f9ff',
					border: '1px solid #bae6fd',
					borderRadius: '8px',
					fontSize: '14px',
					color: '#075985'
				}}>
					<p style={{ margin: '0 0 8px 0', fontWeight: 'bold' }}>ℹ️ Información Importante:</p>
					<ul style={{ margin: 0, paddingLeft: 20 }}>
						<li>Los vuelos cancelados NO aparecen en las búsquedas automáticamente</li>
						<li>Las reservas existentes de estos vuelos deben gestionarse manualmente</li>
						<li>Contacta a los pasajeros afectados para procesar reembolsos</li>
					</ul>
				</div>
			)}
		</div>
	);
}

