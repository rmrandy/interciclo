import { Routes, Route, BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import { SiteConfigProvider } from './contexts/SiteConfigContext.jsx';
import MainLayout from './layouts/MainLayout.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Buscar from './pages/Buscar.jsx';
import Resultados from './pages/Resultados.jsx';
import FlightDetail from './pages/FlightDetail.jsx';
import Compra from './pages/Compra.jsx';
import Admin from './pages/Admin.jsx';
import Acerca from './pages/Acerca.jsx';
import Contacto from './pages/Contacto.jsx';
import Terminos from './pages/Terminos.jsx';
import Forbidden from './pages/Forbidden.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import MyProfile from './pages/MyProfile.jsx';
import MyBookings from './pages/MyBookings.jsx';
import BookingDetail from './pages/BookingDetail.jsx';
import CorporateUsers from './pages/CorporateUsers.jsx';
import SiteConfig from './pages/SiteConfig.jsx';
import CancelledFlights from './pages/CancelledFlights.jsx';

function App() {
	return (
		<SiteConfigProvider>
			<AuthProvider>
				<BrowserRouter>
					<Routes>
						<Route path="/" element={<MainLayout /> }>
							<Route index element={<Home />} />
							<Route path="login" element={<Login />} />
							<Route path="registro" element={<Register />} />
							<Route path="buscar" element={<Buscar />} />
							<Route path="resultados" element={<Resultados />} />
							<Route path="vuelo/:id" element={<FlightDetail />} />
						<Route path="compra" element={<ProtectedRoute><Compra /></ProtectedRoute>} />
						<Route path="admin/corporate-users" element={<ProtectedRoute roles={["admin"]}><CorporateUsers /></ProtectedRoute>} />
						<Route path="admin/site-config" element={<ProtectedRoute roles={["admin"]}><SiteConfig /></ProtectedRoute>} />
						<Route path="admin/cancelled-flights" element={<ProtectedRoute roles={["admin"]}><CancelledFlights /></ProtectedRoute>} />
						<Route path="admin" element={<ProtectedRoute roles={["admin"]}><Admin /></ProtectedRoute>} />
							<Route path="mi-perfil" element={<ProtectedRoute><MyProfile /></ProtectedRoute>} />
							<Route path="mis-reservas" element={<ProtectedRoute><MyBookings /></ProtectedRoute>} />
							<Route path="reserva/:id" element={<ProtectedRoute><BookingDetail /></ProtectedRoute>} />
							<Route path="acerca" element={<Acerca />} />
							<Route path="contacto" element={<Contacto />} />
							<Route path="terminos" element={<Terminos />} />
							<Route path="forbidden" element={<Forbidden />} />
							<Route path="*" element={<div>Página no encontrada</div>} />
						</Route>
					</Routes>
				</BrowserRouter>
			</AuthProvider>
		</SiteConfigProvider>
	);
}

export default App;
