import { Request, Response } from 'express';
import { fetchAllAirlineFlights, fetchAirlineCities, AIRLINE_BASE_URL } from '../utils/airlineApi';

export const proxyAllFlights = async (_req: Request, res: Response): Promise<void> => {
  try {
    const data = await fetchAllAirlineFlights();

    if (data.success !== true || !Array.isArray(data.flights)) {
      res.status(502).json({
        success: false,
        message: 'Respuesta inválida del servicio de aerolínea',
        provider: AIRLINE_BASE_URL,
        providerResponse: data
      });
      return;
    }

    res.json({
      success: true,
      source: AIRLINE_BASE_URL,
      count: data.flights.length,
      data: data.flights
    });
  } catch (error: any) {
    console.error('Error consultando vuelos de aerolínea:', error?.message || error);
    res.status(502).json({
      success: false,
      message: 'No se pudo consultar el servicio de aerolínea',
      provider: AIRLINE_BASE_URL
    });
  }
};

export const proxyCities = async (_req: Request, res: Response): Promise<void> => {
  try {
    const cities = await fetchAirlineCities();
    res.json({
      success: true,
      source: AIRLINE_BASE_URL,
      count: Array.isArray(cities) ? cities.length : 0,
      data: cities
    });
  } catch (error: any) {
    console.error('Error consultando ciudades de aerolínea:', error?.message || error);
    res.status(502).json({
      success: false,
      message: 'No se pudo consultar el servicio de aerolínea (ciudades)',
      provider: AIRLINE_BASE_URL
    });
  }
};


