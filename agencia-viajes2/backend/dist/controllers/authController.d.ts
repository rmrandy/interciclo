import { Request, Response } from 'express';
import { IRegisterRequest, ILoginRequest } from '../types';
export declare const register: (req: Request<{}, {}, IRegisterRequest>, res: Response) => Promise<void>;
export declare const login: (req: Request<{}, {}, ILoginRequest>, res: Response) => Promise<void>;
export declare const getProfile: (req: Request, res: Response) => Promise<void>;
export declare const updateProfile: (req: Request, res: Response) => Promise<void>;
//# sourceMappingURL=authController.d.ts.map