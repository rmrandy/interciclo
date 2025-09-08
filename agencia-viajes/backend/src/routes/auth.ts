import { Router } from 'express';
import { body } from 'express-validator';
import { register, login, getProfile, updateProfile } from '../controllers/authController';
import { authenticate } from '../middleware/auth';

const router = Router();

// Validaciones
const registerValidation = [
  body('firstName')
    .trim()
    .notEmpty()
    .withMessage('El nombre es requerido')
    .isLength({ min: 2, max: 50 })
    .withMessage('El nombre debe tener entre 2 y 50 caracteres'),
  body('lastName')
    .trim()
    .notEmpty()
    .withMessage('El apellido es requerido')
    .isLength({ min: 2, max: 50 })
    .withMessage('El apellido debe tener entre 2 y 50 caracteres'),
  body('email')
    .isEmail()
    .withMessage('Email inválido')
    .normalizeEmail(),
  body('password')
    .isLength({ min: 6 })
    .withMessage('La contraseña debe tener al menos 6 caracteres')
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
    .withMessage('La contraseña debe contener al menos una letra minúscula, una mayúscula y un número'),
  body('phone')
    .optional()
    .isMobilePhone('es-CO')
    .withMessage('Número de teléfono inválido'),
  body('dateOfBirth')
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
  body('email')
    .isEmail()
    .withMessage('Email inválido')
    .normalizeEmail(),
  body('password')
    .notEmpty()
    .withMessage('La contraseña es requerida')
];

const updateProfileValidation = [
  body('firstName')
    .optional()
    .trim()
    .isLength({ min: 2, max: 50 })
    .withMessage('El nombre debe tener entre 2 y 50 caracteres'),
  body('lastName')
    .optional()
    .trim()
    .isLength({ min: 2, max: 50 })
    .withMessage('El apellido debe tener entre 2 y 50 caracteres'),
  body('phone')
    .optional()
    .isMobilePhone('es-CO')
    .withMessage('Número de teléfono inválido'),
  body('dateOfBirth')
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

// Rutas públicas
router.post('/register', registerValidation, register);
router.post('/login', loginValidation, login);

// Rutas protegidas
router.get('/profile', authenticate, getProfile);
router.put('/profile', authenticate, updateProfileValidation, updateProfile);

export default router;
