"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
const mongoose_1 = __importStar(require("mongoose"));
const FlightSchema = new mongoose_1.Schema({
    flightNumber: {
        type: String,
        required: [true, 'El número de vuelo es requerido'],
        unique: true,
        uppercase: true,
        trim: true,
        match: [/^[A-Z]{2,3}\d{3,4}$/, 'Formato de número de vuelo inválido']
    },
    origin: {
        type: mongoose_1.Schema.Types.ObjectId,
        ref: 'Airport',
        required: [true, 'El aeropuerto de origen es requerido']
    },
    destination: {
        type: mongoose_1.Schema.Types.ObjectId,
        ref: 'Airport',
        required: [true, 'El aeropuerto de destino es requerido']
    },
    departure: {
        type: Date,
        required: [true, 'La fecha de salida es requerida']
    },
    arrival: {
        type: Date,
        required: [true, 'La fecha de llegada es requerida']
    },
    aircraft: {
        model: {
            type: String,
            required: [true, 'El modelo de aeronave es requerido'],
            trim: true,
            maxlength: [50, 'El modelo no puede exceder 50 caracteres']
        },
        capacity: {
            type: Number,
            required: [true, 'La capacidad es requerida'],
            min: [1, 'La capacidad debe ser al menos 1']
        }
    },
    inventory: {
        economy: {
            total: {
                type: Number,
                required: true,
                min: [0, 'El total de asientos económicos no puede ser negativo']
            },
            available: {
                type: Number,
                required: true,
                min: [0, 'Los asientos económicos disponibles no pueden ser negativos']
            },
            price: {
                type: Number,
                required: true,
                min: [0, 'El precio no puede ser negativo']
            }
        },
        business: {
            total: {
                type: Number,
                required: true,
                min: [0, 'El total de asientos ejecutivos no puede ser negativo']
            },
            available: {
                type: Number,
                required: true,
                min: [0, 'Los asientos ejecutivos disponibles no pueden ser negativos']
            },
            price: {
                type: Number,
                required: true,
                min: [0, 'El precio no puede ser negativo']
            }
        },
        firstClass: {
            total: {
                type: Number,
                required: true,
                min: [0, 'El total de asientos de primera clase no puede ser negativo']
            },
            available: {
                type: Number,
                required: true,
                min: [0, 'Los asientos de primera clase disponibles no pueden ser negativos']
            },
            price: {
                type: Number,
                required: true,
                min: [0, 'El precio no puede ser negativo']
            }
        }
    },
    status: {
        type: String,
        enum: ['scheduled', 'boarding', 'departed', 'arrived', 'cancelled', 'delayed'],
        default: 'scheduled'
    }
}, {
    timestamps: true
});
FlightSchema.index({ flightNumber: 1 });
FlightSchema.index({ origin: 1, destination: 1 });
FlightSchema.index({ departure: 1 });
FlightSchema.index({ status: 1 });
FlightSchema.index({ 'inventory.economy.available': 1 });
FlightSchema.index({ 'inventory.business.available': 1 });
FlightSchema.index({ 'inventory.firstClass.available': 1 });
FlightSchema.pre('save', function (next) {
    const doc = this;
    const inventory = doc.inventory;
    if (inventory.economy.available > inventory.economy.total) {
        return next(new Error('Los asientos económicos disponibles no pueden exceder el total'));
    }
    if (inventory.business.available > inventory.business.total) {
        return next(new Error('Los asientos ejecutivos disponibles no pueden exceder el total'));
    }
    if (inventory.firstClass.available > inventory.firstClass.total) {
        return next(new Error('Los asientos de primera clase disponibles no pueden exceder el total'));
    }
    next();
});
FlightSchema.virtual('duration').get(function () {
    const doc = this;
    return doc.arrival.getTime() - doc.departure.getTime();
});
FlightSchema.virtual('hasAvailableSeats').get(function () {
    const doc = this;
    const inventory = doc.inventory;
    return inventory.economy.available > 0 ||
        inventory.business.available > 0 ||
        inventory.firstClass.available > 0;
});
exports.default = mongoose_1.default.model('Flight', FlightSchema);
//# sourceMappingURL=Flight.js.map