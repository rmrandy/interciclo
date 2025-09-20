import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute({ children, roles }) {
	const { user } = useAuth();
	const location = useLocation();

	if (!user) {
		return <Navigate to="/login" state={{ from: location }} replace />;
	}
	if (roles && Array.isArray(roles) && roles.length > 0) {
		const role = user.role || 'user';
		if (!roles.includes(role)) {
			return <Navigate to="/forbidden" replace />;
		}
	}
	return children;
}
