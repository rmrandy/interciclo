import { Response, NextFunction } from 'express';
import { IAuthRequest } from '../types';
export declare const authenticate: (req: IAuthRequest, res: Response, next: NextFunction) => Promise<void>;
export declare const authorize: (...roles: string[]) => (req: IAuthRequest, res: Response, next: NextFunction) => void;
export declare const generateToken: (userId: string) => string;
//# sourceMappingURL=auth.d.ts.map