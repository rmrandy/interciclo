import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../services/api.js';

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
        // Login contra el backend propio de la Agencia
        console.log('[AuthContext] Llamando authApi.login con:', email);
        const res = await authApi.login(email, password);
        console.log('[AuthContext] Respuesta cruda:', res);
        const userObj = res?.user || res?.data?.user || res?.data; // tolerante a distintos formatos
        const tokenVal = res?.token || res?.data?.token || null;
        if (userObj) {
            console.log('[AuthContext] Usuario obtenido:', userObj);
            const normalizedUser = { ...userObj, role: String(userObj.role || 'user').toLowerCase() };
            setAuth({ token: tokenVal, user: normalizedUser }); // se persiste en agencia_auth via useEffect
            return { ok: true };
        }
        console.warn('[AuthContext] No se obtuvo usuario válido en la respuesta');
        return { ok: false, message: res?.message || 'Credenciales inválidas' };
    }

    async function register(payload) {
        // Registro contra el backend propio de la Agencia
        const res = await authApi.register(payload);
        const userObj = res?.user || res?.data?.user || res?.data;
        const tokenVal = res?.token || res?.data?.token || null;
        if (userObj) {
            const normalizedUser = { ...userObj, role: String(userObj.role || 'user').toLowerCase() };
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
