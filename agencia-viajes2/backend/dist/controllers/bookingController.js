"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.confirmBooking = exports.cancelBooking = exports.getBookingById = exports.getUserBookings = exports.createBooking = void 0;
const express_validator_1 = require("express-validator");
const Booking_1 = __importDefault(require("../models/Booking"));
const Flight_1 = __importDefault(require("../models/Flight"));
const createBooking = async (req, res) => {
    try {
        const errors = (0, express_validator_1.validationResult)(req);
        if (!errors.isEmpty()) {
            res.status(400).json({
                success: false,
                message: 'Datos de reserva inválidos',
                errors: errors.array()
            });
            return;
        }
        const userId = req.user._id;
        const { flightId, passengers, paymentMethod } = req.body;
        const flight = await Flight_1.default.findById(flightId);
        if (!flight) {
            res.status(404).json({
                success: false,
                message: 'Vuelo no encontrado'
            });
            return;
        }
        const seatCounts = passengers.reduce((acc, passenger) => {
            acc[passenger.seatCategory] = (acc[passenger.seatCategory] || 0) + 1;
            return acc;
        }, {});
        for (const [category, count] of Object.entries(seatCounts)) {
            const available = flight.inventory[category].available;
            if (available < count) {
                res.status(400).json({
                    success: false,
                    message: `No hay suficientes asientos disponibles en ${category}`
                });
                return;
            }
        }
        let totalAmount = 0;
        passengers.forEach(passenger => {
            const price = flight.inventory[passenger.seatCategory].price;
            totalAmount += price;
        });
        const booking = await Booking_1.default.create({
            userId,
            flightId,
            passengers,
            totalAmount,
            paymentMethod,
            status: 'pending',
            paymentStatus: 'pending'
        });
        for (const [category, count] of Object.entries(seatCounts)) {
            await Flight_1.default.findByIdAndUpdate(flightId, {
                $inc: {
                    [`inventory.${category}.available`]: -count,
                    [`inventory.${category}.soldSeats`]: count
                }
            });
        }
        const populatedBooking = await Booking_1.default.findById(booking._id)
            .populate('flightId', 'flightNumber origin destination departure arrival')
            .populate('flightId.origin', 'code name city country')
            .populate('flightId.destination', 'code name city country');
        res.status(201).json({
            success: true,
            message: 'Reserva creada exitosamente',
            data: populatedBooking
        });
    }
    catch (error) {
        console.error('Error al crear reserva:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.createBooking = createBooking;
const getUserBookings = async (req, res) => {
    try {
        const userId = req.user._id;
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 10;
        const skip = (page - 1) * limit;
        const bookings = await Booking_1.default.find({ userId })
            .populate('flightId', 'flightNumber origin destination departure arrival status')
            .populate('flightId.origin', 'code name city country')
            .populate('flightId.destination', 'code name city country')
            .sort({ createdAt: -1 })
            .skip(skip)
            .limit(limit)
            .lean();
        const total = await Booking_1.default.countDocuments({ userId });
        res.json({
            success: true,
            count: bookings.length,
            total,
            page,
            pages: Math.ceil(total / limit),
            data: bookings
        });
    }
    catch (error) {
        console.error('Error al obtener reservas:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.getUserBookings = getUserBookings;
const getBookingById = async (req, res) => {
    try {
        const { id } = req.params;
        const userId = req.user._id;
        const booking = await Booking_1.default.findOne({ _id: id, userId })
            .populate('flightId', 'flightNumber origin destination departure arrival status aircraft')
            .populate('flightId.origin', 'code name city country coordinates')
            .populate('flightId.destination', 'code name city country coordinates');
        if (!booking) {
            res.status(404).json({
                success: false,
                message: 'Reserva no encontrada'
            });
            return;
        }
        res.json({
            success: true,
            data: booking
        });
    }
    catch (error) {
        console.error('Error al obtener reserva:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.getBookingById = getBookingById;
const cancelBooking = async (req, res) => {
    try {
        const { id } = req.params;
        const userId = req.user._id;
        const booking = await Booking_1.default.findOne({ _id: id, userId });
        if (!booking) {
            res.status(404).json({
                success: false,
                message: 'Reserva no encontrada'
            });
            return;
        }
        if (booking.status === 'cancelled') {
            res.status(400).json({
                success: false,
                message: 'La reserva ya está cancelada'
            });
            return;
        }
        if (booking.status === 'completed') {
            res.status(400).json({
                success: false,
                message: 'No se puede cancelar una reserva completada'
            });
            return;
        }
        booking.status = 'cancelled';
        booking.paymentStatus = 'refunded';
        await booking.save();
        const seatCounts = booking.passengers.reduce((acc, passenger) => {
            acc[passenger.seatCategory] = (acc[passenger.seatCategory] || 0) + 1;
            return acc;
        }, {});
        for (const [category, count] of Object.entries(seatCounts)) {
            await Flight_1.default.findByIdAndUpdate(booking.flightId, {
                $inc: {
                    [`inventory.${category}.available`]: count,
                    [`inventory.${category}.soldSeats`]: -count
                }
            });
        }
        res.json({
            success: true,
            message: 'Reserva cancelada exitosamente',
            data: booking
        });
    }
    catch (error) {
        console.error('Error al cancelar reserva:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.cancelBooking = cancelBooking;
const confirmBooking = async (req, res) => {
    try {
        const { id } = req.params;
        const userId = req.user._id;
        const booking = await Booking_1.default.findOne({ _id: id, userId });
        if (!booking) {
            res.status(404).json({
                success: false,
                message: 'Reserva no encontrada'
            });
            return;
        }
        if (booking.status !== 'pending') {
            res.status(400).json({
                success: false,
                message: 'Solo se pueden confirmar reservas pendientes'
            });
            return;
        }
        booking.status = 'confirmed';
        booking.paymentStatus = 'completed';
        await booking.save();
        res.json({
            success: true,
            message: 'Reserva confirmada exitosamente',
            data: booking
        });
    }
    catch (error) {
        console.error('Error al confirmar reserva:', error);
        res.status(500).json({
            success: false,
            message: 'Error interno del servidor'
        });
    }
};
exports.confirmBooking = confirmBooking;
//# sourceMappingURL=bookingController.js.map