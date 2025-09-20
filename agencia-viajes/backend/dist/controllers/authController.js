"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.updateProfile = exports.getProfile = exports.login = exports.register = void 0;
const express_validator_1 = require("express-validator");
const User_1 = __importDefault(require("../models/User"));
const auth_1 = require("../middleware/auth");
const register = async (req, res) => {
    try {
        const errors = (0, express_validator_1.validationResult)(req);
        if (!errors.isEmpty()) {
            res.status(400).json({
                success: false,
                message: 'Datos de entrada inválidos',
                errors: errors.array()
            });
            return;
        }
        const { firstName, lastName, email, password, phone, dateOfBirth } = req.body;
        const existingUser = await User_1.default.findOne({ email });
        if (existingUser) {
            res.status(400).json({
                success: false,
                message: 'El usuario ya existe con este email'
            });
            return;
        }
        const user = await User_1.default.create({
            firstName,
            lastName,
            email,
            password,
            phone,
            dateOfBirth: dateOfBirth ? new Date(dateOfBirth) : undefined
        });
        const token = (0, auth_1.generateToken)(user._id.toString());
        res.status(201).json({
            success: true,
            message: 'Usuario registrado exitosamente',
            data: {
                user: {
                    id: user._id,
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                    phone: user.phone,
                    role: user.role,
                    isActive: user.isActive
                },
                token
            }
        });
    }
    catch (error) {
        console.error('Error en registro:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.register = register;
const login = async (req, res) => {
    try {
        const errors = (0, express_validator_1.validationResult)(req);
        if (!errors.isEmpty()) {
            res.status(400).json({
                success: false,
                message: 'Datos de entrada inválidos',
                errors: errors.array()
            });
            return;
        }
        const { email, password } = req.body;
        const user = await User_1.default.findOne({ email }).select('+password');
        if (!user) {
            res.status(401).json({
                success: false,
                message: 'Credenciales inválidas'
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
        const isPasswordValid = await user.comparePassword(password);
        if (!isPasswordValid) {
            res.status(401).json({
                success: false,
                message: 'Credenciales inválidas'
            });
            return;
        }
        const token = (0, auth_1.generateToken)(user._id.toString());
        res.json({
            success: true,
            message: 'Inicio de sesión exitoso',
            data: {
                user: {
                    id: user._id,
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                    phone: user.phone,
                    role: user.role,
                    isActive: user.isActive
                },
                token
            }
        });
    }
    catch (error) {
        console.error('Error en login:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.login = login;
const getProfile = async (req, res) => {
    try {
        const user = req.user;
        res.json({
            success: true,
            data: {
                user: {
                    id: user._id,
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                    phone: user.phone,
                    dateOfBirth: user.dateOfBirth,
                    role: user.role,
                    isActive: user.isActive,
                    createdAt: user.createdAt
                }
            }
        });
    }
    catch (error) {
        console.error('Error al obtener perfil:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.getProfile = getProfile;
const updateProfile = async (req, res) => {
    try {
        const errors = (0, express_validator_1.validationResult)(req);
        if (!errors.isEmpty()) {
            res.status(400).json({
                success: false,
                message: 'Datos de entrada inválidos',
                errors: errors.array()
            });
            return;
        }
        const userId = req.user._id;
        const { firstName, lastName, phone, dateOfBirth } = req.body;
        const user = await User_1.default.findByIdAndUpdate(userId, {
            firstName,
            lastName,
            phone,
            dateOfBirth: dateOfBirth ? new Date(dateOfBirth) : undefined
        }, { new: true, runValidators: true });
        if (!user) {
            res.status(404).json({
                success: false,
                message: 'Usuario no encontrado'
            });
            return;
        }
        res.json({
            success: true,
            message: 'Perfil actualizado exitosamente',
            data: {
                user: {
                    id: user._id,
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                    phone: user.phone,
                    dateOfBirth: user.dateOfBirth,
                    role: user.role,
                    isActive: user.isActive
                }
            }
        });
    }
    catch (error) {
        console.error('Error al actualizar perfil:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.updateProfile = updateProfile;
//# sourceMappingURL=authController.js.map