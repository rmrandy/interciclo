"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const express_validator_1 = require("express-validator");
const flightController_1 = require("../controllers/flightController");
const auth_1 = require("../middleware/auth");
const router = (0, express_1.Router)();
const searchValidation = [
    (0, express_validator_1.query)('origin')
        .optional()
        .isLength({ min: 3, max: 3 })
        .withMessage('El código de origen debe tener 3 caracteres')
        .isUppercase()
        .withMessage('El código de origen debe estar en mayúsculas'),
    (0, express_validator_1.query)('destination')
        .optional()
        .isLength({ min: 3, max: 3 })
        .withMessage('El código de destino debe tener 3 caracteres')
        .isUppercase()
        .withMessage('El código de destino debe estar en mayúsculas'),
    (0, express_validator_1.query)('departureDate')
        .optional()
        .isISO8601()
        .withMessage('Fecha de salida inválida'),
    (0, express_validator_1.query)('returnDate')
        .optional()
        .isISO8601()
        .withMessage('Fecha de regreso inválida'),
    (0, express_validator_1.query)('passengers')
        .optional()
        .isInt({ min: 1, max: 9 })
        .withMessage('El número de pasajeros debe estar entre 1 y 9'),
    (0, express_validator_1.query)('seatCategory')
        .optional()
        .isIn(['economy', 'business', 'firstClass'])
        .withMessage('Categoría de asiento inválida')
];
const flightValidation = [
    (0, express_validator_1.body)('flightNumber')
        .notEmpty()
        .withMessage('El número de vuelo es requerido')
        .matches(/^[A-Z]{2,3}\d{3,4}$/)
        .withMessage('Formato de número de vuelo inválido'),
    (0, express_validator_1.body)('origin')
        .isMongoId()
        .withMessage('ID de aeropuerto de origen inválido'),
    (0, express_validator_1.body)('destination')
        .isMongoId()
        .withMessage('ID de aeropuerto de destino inválido'),
    (0, express_validator_1.body)('departure')
        .isISO8601()
        .withMessage('Fecha de salida inválida')
        .custom((value) => {
        if (new Date(value) <= new Date()) {
            throw new Error('La fecha de salida debe ser futura');
        }
        return true;
    }),
    (0, express_validator_1.body)('arrival')
        .isISO8601()
        .withMessage('Fecha de llegada inválida')
        .custom((value, { req }) => {
        if (req.body.departure && new Date(value) <= new Date(req.body.departure)) {
            throw new Error('La fecha de llegada debe ser posterior a la salida');
        }
        return true;
    }),
    (0, express_validator_1.body)('aircraft.model')
        .notEmpty()
        .withMessage('El modelo de aeronave es requerido')
        .isLength({ max: 50 })
        .withMessage('El modelo no puede exceder 50 caracteres'),
    (0, express_validator_1.body)('aircraft.capacity')
        .isInt({ min: 1 })
        .withMessage('La capacidad debe ser un número positivo'),
    (0, express_validator_1.body)('inventory.economy.total')
        .isInt({ min: 0 })
        .withMessage('El total de asientos económicos debe ser un número no negativo'),
    (0, express_validator_1.body)('inventory.economy.available')
        .isInt({ min: 0 })
        .withMessage('Los asientos económicos disponibles deben ser un número no negativo'),
    (0, express_validator_1.body)('inventory.economy.price')
        .isFloat({ min: 0 })
        .withMessage('El precio de asientos económicos debe ser un número no negativo'),
    (0, express_validator_1.body)('inventory.business.total')
        .isInt({ min: 0 })
        .withMessage('El total de asientos ejecutivos debe ser un número no negativo'),
    (0, express_validator_1.body)('inventory.business.available')
        .isInt({ min: 0 })
        .withMessage('Los asientos ejecutivos disponibles deben ser un número no negativo'),
    (0, express_validator_1.body)('inventory.business.price')
        .isFloat({ min: 0 })
        .withMessage('El precio de asientos ejecutivos debe ser un número no negativo'),
    (0, express_validator_1.body)('inventory.firstClass.total')
        .isInt({ min: 0 })
        .withMessage('El total de asientos de primera clase debe ser un número no negativo'),
    (0, express_validator_1.body)('inventory.firstClass.available')
        .isInt({ min: 0 })
        .withMessage('Los asientos de primera clase disponibles deben ser un número no negativo'),
    (0, express_validator_1.body)('inventory.firstClass.price')
        .isFloat({ min: 0 })
        .withMessage('El precio de asientos de primera clase debe ser un número no negativo'),
    (0, express_validator_1.body)('status')
        .optional()
        .isIn(['scheduled', 'boarding', 'departed', 'arrived', 'cancelled', 'delayed'])
        .withMessage('Estado de vuelo inválido')
];
router.get('/search', searchValidation, flightController_1.searchFlights);
router.get('/:id', flightController_1.getFlightById);
router.get('/', flightController_1.getAllFlights);
router.post('/', auth_1.authenticate, (0, auth_1.authorize)('admin', 'employee'), flightValidation, flightController_1.createFlight);
router.put('/:id', auth_1.authenticate, (0, auth_1.authorize)('admin', 'employee'), flightValidation, flightController_1.updateFlight);
router.delete('/:id', auth_1.authenticate, (0, auth_1.authorize)('admin'), flightController_1.deleteFlight);
exports.default = router;
//# sourceMappingURL=flights.js.map