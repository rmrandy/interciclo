import { Request, Response } from 'express';
import { IBookingRequest } from '../types';
export declare const createBooking: (req: Request<{}, {}, IBookingRequest>, res: Response) => Promise<void>;
export declare const getUserBookings: (req: Request, res: Response) => Promise<void>;
export declare const getBookingById: (req: Request, res: Response) => Promise<void>;
export declare const cancelBooking: (req: Request, res: Response) => Promise<void>;
export declare const confirmBooking: (req: Request, res: Response) => Promise<void>;
//# sourceMappingURL=bookingController.d.ts.map