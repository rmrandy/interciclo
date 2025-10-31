"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const express_validator_1 = require("express-validator");
const authController_1 = require("../controllers/authController");
const auth_1 = require("../middleware/auth");
const router = (0, express_1.Router)();
const registerValidation = [
    (0, express_validator_1.body)('firstName')
        .trim()
        .notEmpty()
        .withMessage('El nombre es requerido')
        .isLength({ min: 2, max: 50 })
        .withMessage('El nombre debe tener entre 2 y 50 caracteres'),
    (0, express_validator_1.body)('lastName')
        .trim()
        .notEmpty()
        .withMessage('El apellido es requerido')
        .isLength({ min: 2, max: 50 })
        .withMessage('El apellido debe tener entre 2 y 50 caracteres'),
    (0, express_validator_1.body)('email')
        .isEmail()
        .withMessage('Email inválido')
        .normalizeEmail(),
    (0, express_validator_1.body)('password')
        .isLength({ min: 6 })
        .withMessage('La contraseña debe tener al menos 6 caracteres')
        .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
        .withMessage('La contraseña debe contener al menos una letra minúscula, una mayúscula y un número'),
    (0, express_validator_1.body)('phone')
        .optional()
        .isMobilePhone('es-CO')
        .withMessage('Número de teléfono inválido'),
    (0, express_validator_1.body)('dateOfBirth')
        .optional()
        .isISO8601()
        .withMessage('Fecha de nacimiento inválida')
        .custom((value) => {
        if (new Date(value) >= new Date()) {
            throw new Error('La fecha de nacimiento debe ser anterior a hoy');
        }
        return true;
    })
];
const loginValidation = [
    (0, express_validator_1.body)('email')
        .isEmail()
        .withMessage('Email inválido')
        .normalizeEmail(),
    (0, express_validator_1.body)('password')
        .notEmpty()
        .withMessage('La contraseña es requerida')
];
const updateProfileValidation = [
    (0, express_validator_1.body)('firstName')
        .optional()
        .trim()
        .isLength({ min: 2, max: 50 })
        .withMessage('El nombre debe tener entre 2 y 50 caracteres'),
    (0, express_validator_1.body)('lastName')
        .optional()
        .trim()
        .isLength({ min: 2, max: 50 })
        .withMessage('El apellido debe tener entre 2 y 50 caracteres'),
    (0, express_validator_1.body)('phone')
        .optional()
        .isMobilePhone('es-CO')
        .withMessage('Número de teléfono inválido'),
    (0, express_validator_1.body)('dateOfBirth')
        .optional()
        .isISO8601()
        .withMessage('Fecha de nacimiento inválida')
        .custom((value) => {
        if (value && new Date(value) >= new Date()) {
            throw new Error('La fecha de nacimiento debe ser anterior a hoy');
        }
        return true;
    })
];
router.post('/register', registerValidation, authController_1.register);
router.post('/login', loginValidation, authController_1.login);
router.get('/profile', auth_1.authenticate, authController_1.getProfile);
router.put('/profile', auth_1.authenticate, updateProfileValidation, authController_1.updateProfile);
exports.default = router;
//# sourceMappingURL=auth.js.map