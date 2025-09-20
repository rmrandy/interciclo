"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.generateToken = exports.authorize = exports.authenticate = void 0;
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const User_1 = __importDefault(require("../models/User"));
const authenticate = async (req, res, next) => {
    try {
        const token = req.header('Authorization')?.replace('Bearer ', '');
        if (!token) {
            res.status(401).json({
                success: false,
                message: 'Token de acceso requerido'
            });
            return;
        }
        const decoded = jsonwebtoken_1.default.verify(token, process.env.JWT_SECRET || 'fallback_secret');
        const user = await User_1.default.findById(decoded.userId).select('-password');
        if (!user) {
            res.status(401).json({
                success: false,
                message: 'Token inválido - usuario no encontrado'
            });
            return;
        }
        if (!user.isActive) {
            res.status(401).json({
                success: false,
                message: 'Cuenta desactivada'
            });
            return;
        }
        req.user = user;
        next();
    }
    catch (error) {
        console.error('Error en autenticación:', error);
        res.status(401).json({
            success: false,
            message: 'Token inválido'
        });
    }
};
exports.authenticate = authenticate;
const authorize = (...roles) => {
    return (req, res, next) => {
        if (!req.user) {
            res.status(401).json({
                success: false,
                message: 'Acceso denegado - usuario no autenticado'
            });
            return;
        }
        if (!roles.includes(req.user.role)) {
            res.status(403).json({
                success: false,
                message: 'Acceso denegado - permisos insuficientes'
            });
            return;
        }
        next();
    };
};
exports.authorize = authorize;
const generateToken = (userId) => {
    const secret = process.env.JWT_SECRET || 'fallback_secret';
    return jsonwebtoken_1.default.sign({ userId }, secret, { expiresIn: '7d' });
};
exports.generateToken = generateToken;
//# sourceMappingURL=auth.js.map