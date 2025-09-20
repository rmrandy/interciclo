"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.notFound = exports.errorHandler = void 0;
const mongodb_1 = require("mongodb");
const errorHandler = (err, req, res, next) => {
    let error = { ...err };
    error.message = err.message;
    console.error('❌ Error:', err);
    if (err instanceof mongodb_1.MongoError && err.code === 11000) {
        const field = Object.keys(err.keyValue || {})[0];
        const message = `${field} ya existe en la base de datos`;
        error = {
            name: 'ValidationError',
            message,
            statusCode: 400
        };
    }
    if (err.name === 'ValidationError') {
        const message = Object.values(err.errors || {}).map((val) => val.message).join(', ');
        error = {
            name: 'ValidationError',
            message,
            statusCode: 400
        };
    }
    if (err.name === 'JsonWebTokenError') {
        const message = 'Token inválido';
        error = {
            name: 'JsonWebTokenError',
            message,
            statusCode: 401
        };
    }
    if (err.name === 'TokenExpiredError') {
        const message = 'Token expirado';
        error = {
            name: 'TokenExpiredError',
            message,
            statusCode: 401
        };
    }
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
exports.errorHandler = errorHandler;
const notFound = (req, res, next) => {
    const error = new Error(`Ruta no encontrada - ${req.originalUrl}`);
    res.status(404).json({
        success: false,
        message: error.message
    });
};
exports.notFound = notFound;
//# sourceMappingURL=errorHandler.js.map