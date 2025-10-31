import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import User, { IUserDocument } from '../models/User';
import { IAuthRequest } from '../types';

export const authenticate = async (req: IAuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const token = req.header('Authorization')?.replace('Bearer ', '');

    if (!token) {
      res.status(401).json({
        success: false,
        message: 'Token de acceso requerido'
      });
      return;
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'fallback_secret') as { userId: string };
    
    const user = await User.findById(decoded.userId).select('-password');
    
    if (!user) {
      res.status(401).json({
        success: false,
        message: 'Token inválido - usuario no encontrado'
      });
      return;
    }

    if (!user.isActive) {
      res.status(401).json({
        success: false,
        message: 'Cuenta desactivada'
      });
      return;
    }

    req.user = user;
    next();
  } catch (error) {
    console.error('Error en autenticación:', error);
    res.status(401).json({
      success: false,
      message: 'Token inválido'
    });
  }
};

export const authorize = (...roles: string[]) => {
  return (req: IAuthRequest, res: Response, next: NextFunction): void => {
    if (!req.user) {
      res.status(401).json({
        success: false,
        message: 'Acceso denegado - usuario no autenticado'
      });
      return;
    }

    if (!roles.includes(req.user.role)) {
      res.status(403).json({
        success: false,
        message: 'Acceso denegado - permisos insuficientes'
      });
      return;
    }

    next();
  };
};

export const generateToken = (userId: string): string => {
  const secret = process.env.JWT_SECRET || 'fallback_secret';
  
  return jwt.sign({ userId }, secret, { expiresIn: '7d' });
};
