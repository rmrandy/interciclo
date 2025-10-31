"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const express_validator_1 = require("express-validator");
const Airport_1 = __importDefault(require("../models/Airport"));
const auth_1 = require("../middleware/auth");
const router = (0, express_1.Router)();
const airportValidation = [
    (0, express_validator_1.body)('code')
        .isLength({ min: 3, max: 3 })
        .withMessage('El código debe tener exactamente 3 caracteres')
        .isUppercase()
        .withMessage('El código debe estar en mayúsculas'),
    (0, express_validator_1.body)('name')
        .notEmpty()
        .withMessage('El nombre es requerido')
        .isLength({ max: 100 })
        .withMessage('El nombre no puede exceder 100 caracteres'),
    (0, express_validator_1.body)('city')
        .notEmpty()
        .withMessage('La ciudad es requerida')
        .isLength({ max: 50 })
        .withMessage('La ciudad no puede exceder 50 caracteres'),
    (0, express_validator_1.body)('country')
        .notEmpty()
        .withMessage('El país es requerido')
        .isLength({ max: 50 })
        .withMessage('El país no puede exceder 50 caracteres'),
    (0, express_validator_1.body)('coordinates.latitude')
        .isFloat({ min: -90, max: 90 })
        .withMessage('La latitud debe estar entre -90 y 90'),
    (0, express_validator_1.body)('coordinates.longitude')
        .isFloat({ min: -180, max: 180 })
        .withMessage('La longitud debe estar entre -180 y 180')
];
const mongoIdValidation = [
    (0, express_validator_1.param)('id')
        .isMongoId()
        .withMessage('ID inválido')
];
router.get('/', async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 50;
        const skip = (page - 1) * limit;
        const airports = await Airport_1.default.find()
            .sort({ city: 1, name: 1 })
            .skip(skip)
            .limit(limit)
            .lean();
        const total = await Airport_1.default.countDocuments();
        res.json({
            success: true,
            count: airports.length,
            total,
            page,
            pages: Math.ceil(total / limit),
            data: airports
        });
    }
    catch (error) {
        console.error('Error al obtener aeropuertos:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});
router.get('/:id', mongoIdValidation, async (req, res) => {
    try {
        const airport = await Airport_1.default.findById(req.params.id);
        if (!airport) {
            return res.status(404).json({
                success: false,
                message: 'Aeropuerto no encontrado'
            });
        }
        res.json({
            success: true,
            data: airport
        });
    }
    catch (error) {
        console.error('Error al obtener aeropuerto:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});
router.get('/search/:query', async (req, res) => {
    try {
        const query = req.params.query;
        const searchRegex = new RegExp(query, 'i');
        const airports = await Airport_1.default.find({
            $or: [
                { code: searchRegex },
                { name: searchRegex },
                { city: searchRegex },
                { country: searchRegex }
            ]
        })
            .sort({ city: 1, name: 1 })
            .limit(20)
            .lean();
        res.json({
            success: true,
            count: airports.length,
            data: airports
        });
    }
    catch (error) {
        console.error('Error en búsqueda de aeropuertos:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});
router.post('/', auth_1.authenticate, (0, auth_1.authorize)('admin'), airportValidation, async (req, res) => {
    try {
        const airport = await Airport_1.default.create(req.body);
        res.status(201).json({
            success: true,
            message: 'Aeropuerto creado exitosamente',
            data: airport
        });
    }
    catch (error) {
        console.error('Error al crear aeropuerto:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});
router.put('/:id', auth_1.authenticate, (0, auth_1.authorize)('admin'), mongoIdValidation, airportValidation, async (req, res) => {
    try {
        const airport = await Airport_1.default.findByIdAndUpdate(req.params.id, req.body, { new: true, runValidators: true });
        if (!airport) {
            return res.status(404).json({
                success: false,
                message: 'Aeropuerto no encontrado'
            });
        }
        res.json({
            success: true,
            message: 'Aeropuerto actualizado exitosamente',
            data: airport
        });
    }
    catch (error) {
        console.error('Error al actualizar aeropuerto:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});
router.delete('/:id', auth_1.authenticate, (0, auth_1.authorize)('admin'), mongoIdValidation, async (req, res) => {
    try {
        const airport = await Airport_1.default.findByIdAndDelete(req.params.id);
        if (!airport) {
            return res.status(404).json({
                success: false,
                message: 'Aeropuerto no encontrado'
            });
        }
        res.json({
            success: true,
            message: 'Aeropuerto eliminado exitosamente'
        });
    }
    catch (error) {
        console.error('Error al eliminar aeropuerto:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
});
exports.default = router;
//# sourceMappingURL=airports.js.map