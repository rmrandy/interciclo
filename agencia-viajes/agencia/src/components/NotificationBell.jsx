import { useState, useEffect, useRef } from 'react';
import { flightCancellationsApi } from '../services/api';
import { useAuth } from '../context/AuthContext.jsx';

export default function NotificationBell() {
	const { user } = useAuth();
	const [notifications, setNotifications] = useState([]);
	const [unreadCount, setUnreadCount] = useState(0);
	const [isOpen, setIsOpen] = useState(false);
	const [loading, setLoading] = useState(false);
	const dropdownRef = useRef(null);

	// Solo mostrar para administradores
	const isAdmin = user?.role === 'admin';

	// Cargar notificaciones
	const loadNotifications = async () => {
		if (!isAdmin) return;
		
		try {
			setLoading(true);
			const response = await flightCancellationsApi.list({ limit: 10 });
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

	// Marcar como leída
	const markAsRead = async (notificationId) => {
		try {
			await flightCancellationsApi.markRead(notificationId);
			await loadNotifications();
		} catch (error) {
			console.error('Error marcando como leída:', error);
		}
	};

	// Marcar todas como leídas
	const markAllAsRead = async () => {
		try {
			await flightCancellationsApi.markAllRead();
			await loadNotifications();
		} catch (error) {
			console.error('Error marcando todas como leídas:', error);
		}
	};

	// Eliminar notificación
	const deleteNotification = async (notificationId) => {
		if (!confirm('¿Eliminar esta notificación?')) return;
		
		try {
			await flightCancellationsApi.delete(notificationId);
			await loadNotifications();
		} catch (error) {
			console.error('Error eliminando notificación:', error);
		}
	};

	// Cargar al montar y cada 30 segundos
	useEffect(() => {
		if (isAdmin) {
			loadNotifications();
			const interval = setInterval(loadNotifications, 30000); // 30 segundos
			return () => clearInterval(interval);
		}
	}, [isAdmin]);

	// Cerrar dropdown al hacer clic fuera
	useEffect(() => {
		const handleClickOutside = (event) => {
			if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
				setIsOpen(false);
			}
		};

		document.addEventListener('mousedown', handleClickOutside);
		return () => document.removeEventListener('mousedown', handleClickOutside);
	}, []);

	// No mostrar si no es admin
	if (!isAdmin) return null;

	// Formatear fecha relativa
	const formatRelativeTime = (dateString) => {
		const date = new Date(dateString);
		const now = new Date();
		const diffInMs = now - date;
		const diffInMins = Math.floor(diffInMs / 60000);
		const diffInHours = Math.floor(diffInMs / 3600000);
		const diffInDays = Math.floor(diffInMs / 86400000);

		if (diffInMins < 1) return 'Ahora';
		if (diffInMins < 60) return `Hace ${diffInMins} min`;
		if (diffInHours < 24) return `Hace ${diffInHours} h`;
		if (diffInDays === 1) return 'Ayer';
		if (diffInDays < 7) return `Hace ${diffInDays} días`;
		return date.toLocaleDateString();
	};

	return (
		<div ref={dropdownRef} style={{ position: 'relative' }}>
			{/* Botón de campana */}
			<button
				onClick={() => setIsOpen(!isOpen)}
				style={{
					position: 'relative',
					background: 'transparent',
					border: 'none',
					cursor: 'pointer',
					fontSize: '24px',
					padding: '8px',
					borderRadius: '50%',
					transition: 'background 0.2s',
				}}
				onMouseEnter={(e) => e.currentTarget.style.background = '#f3f4f6'}
				onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
				title="Notificaciones de cancelación"
			>
				🔔
				{unreadCount > 0 && (
					<span
						style={{
							position: 'absolute',
							top: '4px',
							right: '4px',
							background: '#dc2626',
							color: 'white',
							borderRadius: '50%',
							width: '18px',
							height: '18px',
							fontSize: '11px',
							fontWeight: 'bold',
							display: 'flex',
							alignItems: 'center',
							justifyContent: 'center',
							border: '2px solid white',
						}}
					>
						{unreadCount > 9 ? '9+' : unreadCount}
					</span>
				)}
			</button>

			{/* Panel desplegable */}
			{isOpen && (
				<div
					style={{
						position: 'absolute',
						top: '100%',
						right: 0,
						marginTop: '8px',
						width: '400px',
						maxHeight: '500px',
						background: 'white',
						border: '1px solid #e5e7eb',
						borderRadius: '8px',
						boxShadow: '0 10px 25px rgba(0,0,0,0.15)',
						zIndex: 1000,
						overflow: 'hidden',
					}}
				>
					{/* Header del panel */}
					<div style={{ 
						padding: '16px', 
						borderBottom: '1px solid #e5e7eb',
						background: '#f9fafb',
						display: 'flex',
						justifyContent: 'space-between',
						alignItems: 'center'
					}}>
						<h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold' }}>
							🚨 Vuelos Cancelados
						</h3>
						{unreadCount > 0 && (
							<button
								onClick={markAllAsRead}
								style={{
									fontSize: '12px',
									color: '#2563eb',
									background: 'none',
									border: 'none',
									cursor: 'pointer',
									textDecoration: 'underline',
								}}
							>
								Marcar todas leídas
							</button>
						)}
					</div>

					{/* Lista de notificaciones */}
					<div style={{ maxHeight: '400px', overflowY: 'auto' }}>
						{loading ? (
							<div style={{ padding: '32px', textAlign: 'center', color: '#666' }}>
								Cargando...
							</div>
						) : notifications.length === 0 ? (
							<div style={{ padding: '32px', textAlign: 'center', color: '#666' }}>
								✅ No hay notificaciones
							</div>
						) : (
							notifications.map((notification) => (
								<div
									key={notification._id}
									style={{
										padding: '16px',
										borderBottom: '1px solid #f3f4f6',
										background: notification.status === 'unread' ? '#fef2f2' : 'white',
										cursor: 'pointer',
										transition: 'background 0.2s',
									}}
									onMouseEnter={(e) => {
										if (notification.status === 'read') {
											e.currentTarget.style.background = '#f9fafb';
										}
									}}
									onMouseLeave={(e) => {
										if (notification.status === 'read') {
											e.currentTarget.style.background = 'white';
										}
									}}
									onClick={() => {
										if (notification.status === 'unread') {
											markAsRead(notification._id);
										}
									}}
								>
									<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
										<div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
											<span style={{ fontSize: '20px' }}>⚠️</span>
											<div>
												<div style={{ fontWeight: 'bold', fontSize: '14px', color: '#dc2626' }}>
													{notification.flightNumber}
												</div>
												<div style={{ fontSize: '12px', color: '#666' }}>
													{notification.airlineName || notification.airlineCode}
												</div>
											</div>
										</div>
										{notification.status === 'unread' && (
											<span style={{
												width: '8px',
												height: '8px',
												borderRadius: '50%',
												background: '#dc2626',
												display: 'block',
												marginTop: '4px',
											}} />
										)}
									</div>

									<div style={{ fontSize: '13px', color: '#111', marginBottom: '4px' }}>
										<strong>Ruta:</strong> {notification.originCity} → {notification.destinationCity}
									</div>

									<div style={{ fontSize: '13px', color: '#666', marginBottom: '8px' }}>
										<strong>Motivo:</strong> {notification.cancellationReason}
									</div>

									<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
										<span style={{ fontSize: '11px', color: '#999' }}>
											{formatRelativeTime(notification.cancelledAt || notification.createdAt)}
										</span>
										<button
											onClick={(e) => {
												e.stopPropagation();
												deleteNotification(notification._id);
											}}
											style={{
												fontSize: '11px',
												color: '#dc2626',
												background: 'none',
												border: 'none',
												cursor: 'pointer',
												padding: '4px 8px',
											}}
										>
											Eliminar
										</button>
									</div>
								</div>
							))
						)}
					</div>

					{/* Footer del panel */}
					{notifications.length > 0 && (
						<div style={{ 
							padding: '12px 16px', 
							borderTop: '1px solid #e5e7eb',
							background: '#f9fafb',
							textAlign: 'center'
						}}>
							<a
								href="/admin/cancelled-flights"
								style={{
									fontSize: '13px',
									color: '#2563eb',
									textDecoration: 'none',
									fontWeight: '500',
								}}
								onClick={() => setIsOpen(false)}
							>
								Ver todas las notificaciones →
							</a>
						</div>
					)}
				</div>
			)}
		</div>
	);
}

