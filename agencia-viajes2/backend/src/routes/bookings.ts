import { Router } from 'express';
import { body, param } from 'express-validator';
import {
  createBooking,
  getUserBookings,
  getBookingById,
  cancelBooking,
  confirmBooking
} from '../controllers/bookingController';
import { authenticate } from '../middleware/auth';

const router = Router();

// Validaciones para crear reserva
const createBookingValidation = [
  body('flightId')
    .isMongoId()
    .withMessage('ID de vuelo inválido'),
  body('passengers')
    .isArray({ min: 1, max: 9 })
    .withMessage('Debe haber entre 1 y 9 pasajeros'),
  body('passengers.*.firstName')
    .notEmpty()
    .withMessage('El nombre del pasajero es requerido')
    .isLength({ min: 2, max: 50 })
    .withMessage('El nombre debe tener entre 2 y 50 caracteres'),
  body('passengers.*.lastName')
    .notEmpty()
    .withMessage('El apellido del pasajero es requerido')
    .isLength({ min: 2, max: 50 })
    .withMessage('El apellido debe tener entre 2 y 50 caracteres'),
  body('passengers.*.dateOfBirth')
    .isISO8601()
    .withMessage('Fecha de nacimiento inválida')
    .custom((value) => {
      if (new Date(value) >= new Date()) {
        throw new Error('La fecha de nacimiento debe ser anterior a hoy');
      }
      return true;
    }),
  body('passengers.*.passportNumber')
    .optional()
    .isLength({ min: 6, max: 12 })
    .withMessage('El número de pasaporte debe tener entre 6 y 12 caracteres')
    .matches(/^[A-Z0-9]+$/)
    .withMessage('El pasaporte solo puede contener letras mayúsculas y números'),
  body('passengers.*.seatCategory')
    .isIn(['economy', 'business', 'firstClass'])
    .withMessage('Categoría de asiento inválida'),
  body('paymentMethod')
    .isIn(['credit_card', 'debit_card', 'paypal'])
    .withMessage('Método de pago inválido')
];

// Validaciones para parámetros
const mongoIdValidation = [
  param('id')
    .isMongoId()
    .withMessage('ID inválido')
];

// Todas las rutas requieren autenticación
router.use(authenticate);

// Rutas de reservas
router.post('/', createBookingValidation, createBooking);
router.get('/', getUserBookings);
router.get('/:id', mongoIdValidation, getBookingById);
router.put('/:id/cancel', mongoIdValidation, cancelBooking);
router.put('/:id/confirm', mongoIdValidation, confirmBooking);

export default router;
