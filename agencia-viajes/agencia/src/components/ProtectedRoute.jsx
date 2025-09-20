import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute({ children, roles }) {
	const { user } = useAuth();
	const location = useLocation();

	if (!user) {
		return <Navigate to="/login" state={{ from: location }} replace />;
	}
	if (roles && Array.isArray(roles) && roles.length > 0) {
        const role = String(user.role || 'user').toLowerCase();
        const allowed = roles.map(r => String(r).toLowerCase());
        if (!allowed.includes(role)) {
			return <Navigate to="/forbidden" replace />;
		}
	}
	return children;
}
