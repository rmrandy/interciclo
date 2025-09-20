import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { integrationsApi } from '../services/api.js';

const AuthContext = createContext(null);

function getStoredAuth() {
	try {
		const raw = localStorage.getItem('agencia_auth');
		if (!raw) return null;
		return JSON.parse(raw);
	} catch {
		return null;
	}
}

function setStoredAuth(value) {
	try {
		if (!value) localStorage.removeItem('agencia_auth');
		else localStorage.setItem('agencia_auth', JSON.stringify(value));
	} catch {
		// ignore
	}
}

export function AuthProvider({ children }) {
	const [auth, setAuth] = useState(() => getStoredAuth());
	const token = auth?.token || null;
	const user = auth?.user || null;

	useEffect(() => {
		setStoredAuth(auth);
	}, [auth]);

    async function login(email, password) {
        const res = await integrationsApi.loginAirline(email, password);
        // Derivar user/token según la Aerolínea
        const userObj = res?.user || res?.data?.user;
        const tokenVal = res?.token || res?.data?.token || null;
        if (userObj) {
            const normalizedUser = { ...userObj, role: String(userObj.role || 'user').toLowerCase() };
            try { localStorage.setItem('user', JSON.stringify(normalizedUser)); } catch {}
            setAuth({ token: tokenVal, user: normalizedUser });
            return { ok: true };
        }
        return { ok: false, message: res?.message || 'Credenciales inválidas' };
    }

    async function register(payload) {
        const res = await integrationsApi.registerAirline(payload);
        const userObj = res?.user || res?.data?.user;
        const tokenVal = res?.token || res?.data?.token || null;
        if (userObj) {
            const normalizedUser = { ...userObj, role: String(userObj.role || 'user').toLowerCase() };
            try { localStorage.setItem('user', JSON.stringify(normalizedUser)); } catch {}
            setAuth({ token: tokenVal, user: normalizedUser });
            return { ok: true };
        }
        return { ok: false, message: res?.message || 'Registro fallido' };
    }

	function logout() {
		setAuth(null);
	}

	const value = useMemo(() => ({ token, user, login, register, logout }), [token, user]);

	return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
	const ctx = useContext(AuthContext);
	if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
	return ctx;
}
