import { Request, Response } from 'express';
import { IFlightSearchRequest } from '../types';
export declare const searchFlights: (req: Request<{}, {}, {}, IFlightSearchRequest>, res: Response) => Promise<void>;
export declare const getFlightById: (req: Request, res: Response) => Promise<void>;
export declare const getAllFlights: (req: Request, res: Response) => Promise<void>;
export declare const createFlight: (req: Request, res: Response) => Promise<void>;
export declare const updateFlight: (req: Request, res: Response) => Promise<void>;
export declare const deleteFlight: (req: Request, res: Response) => Promise<void>;
//# sourceMappingURL=flightController.d.ts.map