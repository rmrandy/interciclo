import { Request, Response } from 'express';
import { validationResult } from 'express-validator';
import Booking from '../models/Booking';
import Flight from '../models/Flight';
import { IBookingRequest } from '../types';

export const createBooking = async (req: Request<{}, {}, IBookingRequest>, res: Response): Promise<void> => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      res.status(400).json({
        success: false,
        message: 'Datos de reserva inválidos',
        errors: errors.array()
      });
      return;
    }

    const userId = (req as any).user._id;
    const { flightId, passengers, paymentMethod } = req.body;

    // Verificar que el vuelo existe
    const flight = await Flight.findById(flightId);
    if (!flight) {
      res.status(404).json({
        success: false,
        message: 'Vuelo no encontrado'
      });
      return;
    }

    // Verificar disponibilidad de asientos
    const seatCounts = passengers.reduce((acc, passenger) => {
      acc[passenger.seatCategory] = (acc[passenger.seatCategory] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);

    // Verificar disponibilidad por categoría
    for (const [category, count] of Object.entries(seatCounts)) {
      const available = flight.inventory[category as keyof typeof flight.inventory].available;
      if (available < count) {
        res.status(400).json({
          success: false,
          message: `No hay suficientes asientos disponibles en ${category}`
        });
        return;
      }
    }

    // Calcular monto total
    let totalAmount = 0;
    passengers.forEach(passenger => {
      const price = flight.inventory[passenger.seatCategory].price;
      totalAmount += price;
    });

    // Crear la reserva
    const booking = await Booking.create({
      userId,
      flightId,
      passengers,
      totalAmount,
      paymentMethod,
      status: 'pending',
      paymentStatus: 'pending'
    });

    // Actualizar inventario de asientos
    for (const [category, count] of Object.entries(seatCounts)) {
      await Flight.findByIdAndUpdate(flightId, {
        $inc: {
          [`inventory.${category}.available`]: -count,
          [`inventory.${category}.soldSeats`]: count
        }
      });
    }

    // Obtener la reserva completa con datos del vuelo
    const populatedBooking = await Booking.findById(booking._id)
      .populate('flightId', 'flightNumber origin destination departure arrival')
      .populate('flightId.origin', 'code name city country')
      .populate('flightId.destination', 'code name city country');

    res.status(201).json({
      success: true,
      message: 'Reserva creada exitosamente',
      data: populatedBooking
    });
  } catch (error) {
    console.error('Error al crear reserva:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const getUserBookings = async (req: Request, res: Response): Promise<void> => {
  try {
    const userId = (req as any).user._id;
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 10;
    const skip = (page - 1) * limit;

    const bookings = await Booking.find({ userId })
      .populate('flightId', 'flightNumber origin destination departure arrival status')
      .populate('flightId.origin', 'code name city country')
      .populate('flightId.destination', 'code name city country')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit)
      .lean();

    const total = await Booking.countDocuments({ userId });

    res.json({
      success: true,
      count: bookings.length,
      total,
      page,
      pages: Math.ceil(total / limit),
      data: bookings
    });
  } catch (error) {
    console.error('Error al obtener reservas:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const getBookingById = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const userId = (req as any).user._id;

    const booking = await Booking.findOne({ _id: id, userId })
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
  } catch (error) {
    console.error('Error al obtener reserva:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const cancelBooking = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const userId = (req as any).user._id;

    const booking = await Booking.findOne({ _id: id, userId });
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

    // Actualizar estado de la reserva
    booking.status = 'cancelled';
    booking.paymentStatus = 'refunded';
    await booking.save();

    // Devolver asientos al inventario
    const seatCounts = booking.passengers.reduce((acc, passenger) => {
      acc[passenger.seatCategory] = (acc[passenger.seatCategory] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);

    for (const [category, count] of Object.entries(seatCounts)) {
      await Flight.findByIdAndUpdate(booking.flightId, {
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
  } catch (error) {
    console.error('Error al cancelar reserva:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const confirmBooking = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const userId = (req as any).user._id;

    const booking = await Booking.findOne({ _id: id, userId });
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

    // Simular procesamiento de pago
    booking.status = 'confirmed';
    booking.paymentStatus = 'completed';
    await booking.save();

    res.json({
      success: true,
      message: 'Reserva confirmada exitosamente',
      data: booking
    });
  } catch (error) {
    console.error('Error al confirmar reserva:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};
