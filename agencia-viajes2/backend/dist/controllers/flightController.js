"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.deleteFlight = exports.updateFlight = exports.createFlight = exports.getAllFlights = exports.getFlightById = exports.searchFlights = void 0;
const express_validator_1 = require("express-validator");
const Flight_1 = __importDefault(require("../models/Flight"));
const Airport_1 = __importDefault(require("../models/Airport"));
const searchFlights = async (req, res) => {
    try {
        const errors = (0, express_validator_1.validationResult)(req);
        if (!errors.isEmpty()) {
            res.status(400).json({
                success: false,
                message: 'Parámetros de búsqueda inválidos',
                errors: errors.array()
            });
            return;
        }
        const { origin, destination, departureDate, returnDate, passengers, seatCategory } = req.query;
        const filters = {
            status: { $in: ['scheduled', 'boarding'] }
        };
        if (origin) {
            const originAirport = await Airport_1.default.findOne({ code: origin.toUpperCase() });
            if (originAirport) {
                filters.origin = originAirport._id;
            }
        }
        if (destination) {
            const destinationAirport = await Airport_1.default.findOne({ code: destination.toUpperCase() });
            if (destinationAirport) {
                filters.destination = destinationAirport._id;
            }
        }
        if (departureDate) {
            const startDate = new Date(departureDate);
            const endDate = new Date(departureDate);
            endDate.setDate(endDate.getDate() + 1);
            filters.departure = {
                $gte: startDate,
                $lt: endDate
            };
        }
        if (passengers) {
            const passengerCount = parseInt(passengers.toString());
            if (seatCategory) {
                const category = seatCategory.toString();
                filters[`inventory.${category}.available`] = { $gte: passengerCount };
            }
            else {
                filters.$or = [
                    { 'inventory.economy.available': { $gte: passengerCount } },
                    { 'inventory.business.available': { $gte: passengerCount } },
                    { 'inventory.firstClass.available': { $gte: passengerCount } }
                ];
            }
        }
        const flights = await Flight_1.default.find(filters)
            .populate('origin', 'code name city country')
            .populate('destination', 'code name city country')
            .sort({ departure: 1 })
            .lean();
        res.json({
            success: true,
            count: flights.length,
            data: flights
        });
    }
    catch (error) {
        console.error('Error en búsqueda de vuelos:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.searchFlights = searchFlights;
const getFlightById = async (req, res) => {
    try {
        const { id } = req.params;
        const flight = await Flight_1.default.findById(id)
            .populate('origin', 'code name city country coordinates')
            .populate('destination', 'code name city country coordinates');
        if (!flight) {
            res.status(404).json({
                success: false,
                message: 'Vuelo no encontrado'
            });
            return;
        }
        res.json({
            success: true,
            data: flight
        });
    }
    catch (error) {
        console.error('Error al obtener vuelo:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.getFlightById = getFlightById;
const getAllFlights = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 10;
        const skip = (page - 1) * limit;
        const flights = await Flight_1.default.find()
            .populate('origin', 'code name city country')
            .populate('destination', 'code name city country')
            .sort({ departure: 1 })
            .skip(skip)
            .limit(limit)
            .lean();
        const total = await Flight_1.default.countDocuments();
        res.json({
            success: true,
            count: flights.length,
            total,
            page,
            pages: Math.ceil(total / limit),
            data: flights
        });
    }
    catch (error) {
        console.error('Error al obtener vuelos:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.getAllFlights = getAllFlights;
const createFlight = async (req, res) => {
    try {
        const errors = (0, express_validator_1.validationResult)(req);
        if (!errors.isEmpty()) {
            res.status(400).json({
                success: false,
                message: 'Datos de vuelo inválidos',
                errors: errors.array()
            });
            return;
        }
        const flight = await Flight_1.default.create(req.body);
        const populatedFlight = await Flight_1.default.findById(flight._id)
            .populate('origin', 'code name city country')
            .populate('destination', 'code name city country');
        res.status(201).json({
            success: true,
            message: 'Vuelo creado exitosamente',
            data: populatedFlight
        });
    }
    catch (error) {
        console.error('Error al crear vuelo:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.createFlight = createFlight;
const updateFlight = async (req, res) => {
    try {
        const { id } = req.params;
        const errors = (0, express_validator_1.validationResult)(req);
        if (!errors.isEmpty()) {
            res.status(400).json({
                success: false,
                message: 'Datos de vuelo inválidos',
                errors: errors.array()
            });
            return;
        }
        const flight = await Flight_1.default.findByIdAndUpdate(id, req.body, { new: true, runValidators: true }).populate('origin', 'code name city country')
            .populate('destination', 'code name city country');
        if (!flight) {
            res.status(404).json({
                success: false,
                message: 'Vuelo no encontrado'
            });
            return;
        }
        res.json({
            success: true,
            message: 'Vuelo actualizado exitosamente',
            data: flight
        });
    }
    catch (error) {
        console.error('Error al actualizar vuelo:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.updateFlight = updateFlight;
const deleteFlight = async (req, res) => {
    try {
        const { id } = req.params;
        const flight = await Flight_1.default.findByIdAndDelete(id);
        if (!flight) {
            res.status(404).json({
                success: false,
                message: 'Vuelo no encontrado'
            });
            return;
        }
        res.json({
            success: true,
            message: 'Vuelo eliminado exitosamente'
        });
    }
    catch (error) {
        console.error('Error al eliminar vuelo:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.deleteFlight = deleteFlight;
//# sourceMappingURL=flightController.js.map