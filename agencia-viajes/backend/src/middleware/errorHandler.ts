import { Request, Response, NextFunction } from 'express';
import { MongoError } from 'mongodb';

interface CustomError extends Error {
  statusCode?: number;
  code?: number;
  keyValue?: Record<string, any>;
  errors?: Record<string, any>;
}

export const errorHandler = (
  err: CustomError,
  req: Request,
  res: Response,
  next: NextFunction
): void => {
  let error = { ...err };
  error.message = err.message;

  // Log del error
  console.error('❌ Error:', err);

  // Error de MongoDB - Duplicado
  if (err instanceof MongoError && err.code === 11000) {
    const field = Object.keys(err.keyValue || {})[0];
    const message = `${field} ya existe en la base de datos`;
    error = {
      name: 'ValidationError',
      message,
      statusCode: 400
    };
  }

  // Error de validación de Mongoose
  if (err.name === 'ValidationError') {
    const message = Object.values(err.errors || {}).map((val: any) => val.message).join(', ');
    error = {
      name: 'ValidationError',
      message,
      statusCode: 400
    };
  }

  // Error de JWT
  if (err.name === 'JsonWebTokenError') {
    const message = 'Token inválido';
    error = {
      name: 'JsonWebTokenError',
      message,
      statusCode: 401
    };
  }

  // Error de JWT expirado
  if (err.name === 'TokenExpiredError') {
    const message = 'Token expirado';
    error = {
      name: 'TokenExpiredError',
      message,
      statusCode: 401
    };
  }

  // Error de cast de ObjectId
  if (err.name === 'CastError') {
    const message = 'ID de recurso inválido';
    error = {
      name: 'CastError',
      message,
      statusCode: 400
    };
  }

  res.status(error.statusCode || 500).json({
    success: false,
    message: error.message || 'Error interno del servidor',
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  });
};

export const notFound = (req: Request, res: Response, next: NextFunction): void => {
  const error = new Error(`Ruta no encontrada - ${req.originalUrl}`);
  res.status(404).json({
    success: false,
    message: error.message
  });
};
