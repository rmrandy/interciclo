import { Request, Response } from 'express';
import { validationResult } from 'express-validator';
import Flight from '../models/Flight';
import Airport from '../models/Airport';
import { IFlightSearchRequest } from '../types';

export const searchFlights = async (req: Request<{}, {}, {}, IFlightSearchRequest>, res: Response): Promise<void> => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      res.status(400).json({
        success: false,
        message: 'Parámetros de búsqueda inválidos',
        errors: errors.array()
      });
      return;
    }

    const { origin, destination, departureDate, returnDate, passengers, seatCategory } = req.query;

    // Construir filtros de búsqueda
    const filters: any = {
      status: { $in: ['scheduled', 'boarding'] }
    };

    // Filtro por aeropuertos
    if (origin) {
      const originAirport = await Airport.findOne({ code: origin.toUpperCase() });
      if (originAirport) {
        filters.origin = originAirport._id;
      }
    }

    if (destination) {
      const destinationAirport = await Airport.findOne({ code: destination.toUpperCase() });
      if (destinationAirport) {
        filters.destination = destinationAirport._id;
      }
    }

    // Filtro por fecha de salida
    if (departureDate) {
      const startDate = new Date(departureDate);
      const endDate = new Date(departureDate);
      endDate.setDate(endDate.getDate() + 1);
      
      filters.departure = {
        $gte: startDate,
        $lt: endDate
      };
    }

    // Filtro por disponibilidad de asientos
    if (passengers) {
      const passengerCount = parseInt(passengers.toString());
      if (seatCategory) {
        const category = seatCategory.toString();
        filters[`inventory.${category}.available`] = { $gte: passengerCount };
      } else {
        // Buscar en cualquier categoría
        filters.$or = [
          { 'inventory.economy.available': { $gte: passengerCount } },
          { 'inventory.business.available': { $gte: passengerCount } },
          { 'inventory.firstClass.available': { $gte: passengerCount } }
        ];
      }
    }

    // Ejecutar búsqueda
    const flights = await Flight.find(filters)
      .populate('origin', 'code name city country')
      .populate('destination', 'code name city country')
      .sort({ departure: 1 })
      .lean();

    res.json({
      success: true,
      count: flights.length,
      data: flights
    });
  } catch (error) {
    console.error('Error en búsqueda de vuelos:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const getFlightById = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;

    const flight = await Flight.findById(id)
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
  } catch (error) {
    console.error('Error al obtener vuelo:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const getAllFlights = async (req: Request, res: Response): Promise<void> => {
  try {
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 10;
    const skip = (page - 1) * limit;

    const flights = await Flight.find()
      .populate('origin', 'code name city country')
      .populate('destination', 'code name city country')
      .sort({ departure: 1 })
      .skip(skip)
      .limit(limit)
      .lean();

    const total = await Flight.countDocuments();

    res.json({
      success: true,
      count: flights.length,
      total,
      page,
      pages: Math.ceil(total / limit),
      data: flights
    });
  } catch (error) {
    console.error('Error al obtener vuelos:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const createFlight = async (req: Request, res: Response): Promise<void> => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      res.status(400).json({
        success: false,
        message: 'Datos de vuelo inválidos',
        errors: errors.array()
      });
      return;
    }

    const flight = await Flight.create(req.body);

    const populatedFlight = await Flight.findById(flight._id)
      .populate('origin', 'code name city country')
      .populate('destination', 'code name city country');

    res.status(201).json({
      success: true,
      message: 'Vuelo creado exitosamente',
      data: populatedFlight
    });
  } catch (error) {
    console.error('Error al crear vuelo:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const updateFlight = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;
    const errors = validationResult(req);
    
    if (!errors.isEmpty()) {
      res.status(400).json({
        success: false,
        message: 'Datos de vuelo inválidos',
        errors: errors.array()
      });
      return;
    }

    const flight = await Flight.findByIdAndUpdate(
      id,
      req.body,
      { new: true, runValidators: true }
    ).populate('origin', 'code name city country')
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
  } catch (error) {
    console.error('Error al actualizar vuelo:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

export const deleteFlight = async (req: Request, res: Response): Promise<void> => {
  try {
    const { id } = req.params;

    const flight = await Flight.findByIdAndDelete(id);

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
  } catch (error) {
    console.error('Error al eliminar vuelo:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};
