import { Router } from 'express';
import { body, query } from 'express-validator';
import {
  searchFlights,
  getFlightById,
  getAllFlights,
  createFlight,
  updateFlight,
  deleteFlight
} from '../controllers/flightController';
import { authenticate, authorize } from '../middleware/auth';

const router = Router();

// Validaciones para búsqueda de vuelos
const searchValidation = [
  query('origin')
    .optional()
    .isLength({ min: 3, max: 3 })
    .withMessage('El código de origen debe tener 3 caracteres')
    .isUppercase()
    .withMessage('El código de origen debe estar en mayúsculas'),
  query('destination')
    .optional()
    .isLength({ min: 3, max: 3 })
    .withMessage('El código de destino debe tener 3 caracteres')
    .isUppercase()
    .withMessage('El código de destino debe estar en mayúsculas'),
  query('departureDate')
    .optional()
    .isISO8601()
    .withMessage('Fecha de salida inválida'),
  query('returnDate')
    .optional()
    .isISO8601()
    .withMessage('Fecha de regreso inválida'),
  query('passengers')
    .optional()
    .isInt({ min: 1, max: 9 })
    .withMessage('El número de pasajeros debe estar entre 1 y 9'),
  query('seatCategory')
    .optional()
    .isIn(['economy', 'business', 'firstClass'])
    .withMessage('Categoría de asiento inválida')
];

// Validaciones para crear/actualizar vuelos
const flightValidation = [
  body('flightNumber')
    .notEmpty()
    .withMessage('El número de vuelo es requerido')
    .matches(/^[A-Z]{2,3}\d{3,4}$/)
    .withMessage('Formato de número de vuelo inválido'),
  body('origin')
    .isMongoId()
    .withMessage('ID de aeropuerto de origen inválido'),
  body('destination')
    .isMongoId()
    .withMessage('ID de aeropuerto de destino inválido'),
  body('departure')
    .isISO8601()
    .withMessage('Fecha de salida inválida')
    .custom((value) => {
      if (new Date(value) <= new Date()) {
        throw new Error('La fecha de salida debe ser futura');
      }
      return true;
    }),
  body('arrival')
    .isISO8601()
    .withMessage('Fecha de llegada inválida')
    .custom((value, { req }) => {
      if (req.body.departure && new Date(value) <= new Date(req.body.departure)) {
        throw new Error('La fecha de llegada debe ser posterior a la salida');
      }
      return true;
    }),
  body('aircraft.model')
    .notEmpty()
    .withMessage('El modelo de aeronave es requerido')
    .isLength({ max: 50 })
    .withMessage('El modelo no puede exceder 50 caracteres'),
  body('aircraft.capacity')
    .isInt({ min: 1 })
    .withMessage('La capacidad debe ser un número positivo'),
  body('inventory.economy.total')
    .isInt({ min: 0 })
    .withMessage('El total de asientos económicos debe ser un número no negativo'),
  body('inventory.economy.available')
    .isInt({ min: 0 })
    .withMessage('Los asientos económicos disponibles deben ser un número no negativo'),
  body('inventory.economy.price')
    .isFloat({ min: 0 })
    .withMessage('El precio de asientos económicos debe ser un número no negativo'),
  body('inventory.business.total')
    .isInt({ min: 0 })
    .withMessage('El total de asientos ejecutivos debe ser un número no negativo'),
  body('inventory.business.available')
    .isInt({ min: 0 })
    .withMessage('Los asientos ejecutivos disponibles deben ser un número no negativo'),
  body('inventory.business.price')
    .isFloat({ min: 0 })
    .withMessage('El precio de asientos ejecutivos debe ser un número no negativo'),
  body('inventory.firstClass.total')
    .isInt({ min: 0 })
    .withMessage('El total de asientos de primera clase debe ser un número no negativo'),
  body('inventory.firstClass.available')
    .isInt({ min: 0 })
    .withMessage('Los asientos de primera clase disponibles deben ser un número no negativo'),
  body('inventory.firstClass.price')
    .isFloat({ min: 0 })
    .withMessage('El precio de asientos de primera clase debe ser un número no negativo'),
  body('status')
    .optional()
    .isIn(['scheduled', 'boarding', 'departed', 'arrived', 'cancelled', 'delayed'])
    .withMessage('Estado de vuelo inválido')
];

// Rutas públicas
router.get('/search', searchValidation, searchFlights);
router.get('/:id', getFlightById);
router.get('/', getAllFlights);

// Rutas protegidas (solo administradores y empleados)
router.post('/', authenticate, authorize('admin', 'employee'), flightValidation, createFlight);
router.put('/:id', authenticate, authorize('admin', 'employee'), flightValidation, updateFlight);
router.delete('/:id', authenticate, authorize('admin'), deleteFlight);

export default router;
