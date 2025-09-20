"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const express_validator_1 = require("express-validator");
const bookingController_1 = require("../controllers/bookingController");
const auth_1 = require("../middleware/auth");
const router = (0, express_1.Router)();
const createBookingValidation = [
    (0, express_validator_1.body)('flightId')
        .isMongoId()
        .withMessage('ID de vuelo inválido'),
    (0, express_validator_1.body)('passengers')
        .isArray({ min: 1, max: 9 })
        .withMessage('Debe haber entre 1 y 9 pasajeros'),
    (0, express_validator_1.body)('passengers.*.firstName')
        .notEmpty()
        .withMessage('El nombre del pasajero es requerido')
        .isLength({ min: 2, max: 50 })
        .withMessage('El nombre debe tener entre 2 y 50 caracteres'),
    (0, express_validator_1.body)('passengers.*.lastName')
        .notEmpty()
        .withMessage('El apellido del pasajero es requerido')
        .isLength({ min: 2, max: 50 })
        .withMessage('El apellido debe tener entre 2 y 50 caracteres'),
    (0, express_validator_1.body)('passengers.*.dateOfBirth')
        .isISO8601()
        .withMessage('Fecha de nacimiento inválida')
        .custom((value) => {
        if (new Date(value) >= new Date()) {
            throw new Error('La fecha de nacimiento debe ser anterior a hoy');
        }
        return true;
    }),
    (0, express_validator_1.body)('passengers.*.passportNumber')
        .optional()
        .isLength({ min: 6, max: 12 })
        .withMessage('El número de pasaporte debe tener entre 6 y 12 caracteres')
        .matches(/^[A-Z0-9]+$/)
        .withMessage('El pasaporte solo puede contener letras mayúsculas y números'),
    (0, express_validator_1.body)('passengers.*.seatCategory')
        .isIn(['economy', 'business', 'firstClass'])
        .withMessage('Categoría de asiento inválida'),
    (0, express_validator_1.body)('paymentMethod')
        .isIn(['credit_card', 'debit_card', 'paypal'])
        .withMessage('Método de pago inválido')
];
const mongoIdValidation = [
    (0, express_validator_1.param)('id')
        .isMongoId()
        .withMessage('ID inválido')
];
router.use(auth_1.authenticate);
router.post('/', createBookingValidation, bookingController_1.createBooking);
router.get('/', bookingController_1.getUserBookings);
router.get('/:id', mongoIdValidation, bookingController_1.getBookingById);
router.put('/:id/cancel', mongoIdValidation, bookingController_1.cancelBooking);
router.put('/:id/confirm', mongoIdValidation, bookingController_1.confirmBooking);
exports.default = router;
//# sourceMappingURL=bookings.js.map