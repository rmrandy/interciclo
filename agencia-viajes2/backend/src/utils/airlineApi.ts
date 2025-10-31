import axios, { AxiosInstance } from 'axios';

const AIRLINE_PROTOCOL = (process.env.AIRLINE_PROTOCOL || 'http').trim();
const AIRLINE_HOST = (process.env.AIRLINE_HOST || 'localhost').trim();
const AIRLINE_PORT = parseInt(process.env.AIRLINE_PORT || '8080', 10);
const AIRLINE_BASE_PATH = (process.env.AIRLINE_BASE_PATH || '/api').trim();
const AIRLINE_TIMEOUT_MS = parseInt(process.env.AIRLINE_TIMEOUT_MS || '5000', 10);

export const AIRLINE_BASE_URL = `${AIRLINE_PROTOCOL}://${AIRLINE_HOST}:${AIRLINE_PORT}${AIRLINE_BASE_PATH}`;

export const airlineApi: AxiosInstance = axios.create({
  baseURL: AIRLINE_BASE_URL,
  timeout: AIRLINE_TIMEOUT_MS,
  headers: {
    'Content-Type': 'application/json'
  }
});

export interface AirlineFlightsResponse {
  success: boolean;
  flights?: any[];
  error?: string;
}

export async function fetchAllAirlineFlights(): Promise<AirlineFlightsResponse> {
  const response = await airlineApi.get<AirlineFlightsResponse>('/airline/flights');
  return response.data;
}

export interface AirlineCity {
  idCity: number;
  name: string;
  country: string;
}

export async function fetchAirlineCities(): Promise<AirlineCity[]> {
  const response = await airlineApi.get<AirlineCity[]>('/airline/cities');
  return response.data;
}


