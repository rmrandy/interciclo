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
const PassengerSchema = new mongoose_1.Schema({
    firstName: {
        type: String,
        required: [true, 'El nombre del pasajero es requerido'],
        trim: true,
        maxlength: [50, 'El nombre no puede exceder 50 caracteres']
    },
    lastName: {
        type: String,
        required: [true, 'El apellido del pasajero es requerido'],
        trim: true,
        maxlength: [50, 'El apellido no puede exceder 50 caracteres']
    },
    dateOfBirth: {
        type: Date,
        required: [true, 'La fecha de nacimiento del pasajero es requerida'],
        validate: {
            validator: function (value) {
                return value < new Date();
            },
            message: 'La fecha de nacimiento debe ser anterior a hoy'
        }
    },
    passportNumber: {
        type: String,
        trim: true,
        uppercase: true,
        match: [/^[A-Z0-9]{6,12}$/, 'Formato de pasaporte inválido']
    },
    seatCategory: {
        type: String,
        enum: ['economy', 'business', 'firstClass'],
        required: [true, 'La categoría de asiento es requerida']
    },
    seatNumber: {
        type: String,
        required: [true, 'El número de asiento es requerido'],
        trim: true
    }
}, { _id: false });
const BookingSchema = new mongoose_1.Schema({
    userId: {
        type: String,
        required: [true, 'El ID del usuario es requerido']
    },
    flightId: {
        type: String,
        required: [true, 'El ID del vuelo es requerido']
    },
    passengers: [PassengerSchema],
    totalAmount: {
        type: Number,
        required: [true, 'El monto total es requerido'],
        min: [0, 'El monto total no puede ser negativo']
    },
    status: {
        type: String,
        enum: ['pending', 'confirmed', 'cancelled', 'completed'],
        default: 'pending'
    },
    paymentMethod: {
        type: String,
        enum: ['credit_card', 'debit_card', 'paypal'],
        required: [true, 'El método de pago es requerido']
    },
    paymentStatus: {
        type: String,
        enum: ['pending', 'completed', 'failed', 'refunded'],
        default: 'pending'
    },
    bookingReference: {
        type: String,
        required: [true, 'La referencia de reserva es requerida'],
        unique: true,
        uppercase: true,
        trim: true,
        match: [/^[A-Z0-9]{6,12}$/, 'Formato de referencia de reserva inválido']
    }
}, {
    timestamps: true
});
BookingSchema.index({ userId: 1 });
BookingSchema.index({ flightId: 1 });
BookingSchema.index({ bookingReference: 1 });
BookingSchema.index({ status: 1 });
BookingSchema.index({ paymentStatus: 1 });
BookingSchema.index({ createdAt: -1 });
BookingSchema.pre('save', function (next) {
    const doc = this;
    if (!doc.bookingReference) {
        const timestamp = Date.now().toString(36).toUpperCase();
        const random = Math.random().toString(36).substr(2, 4).toUpperCase();
        doc.bookingReference = `BK${timestamp}${random}`;
    }
    next();
});
BookingSchema.pre('save', function (next) {
    const doc = this;
    doc.passengers.forEach((passenger, index) => {
        if (!passenger.seatNumber) {
            const timestamp = Date.now();
            passenger.seatNumber = `AUTO-${doc.flightId}-${passenger.seatCategory}-${timestamp}-${index}`;
        }
    });
    next();
});
BookingSchema.virtual('passengerCount').get(function () {
    const doc = this;
    return doc.passengers.length;
});
BookingSchema.virtual('isActive').get(function () {
    const doc = this;
    return doc.status === 'confirmed' || doc.status === 'pending';
});
exports.default = mongoose_1.default.model('Booking', BookingSchema);
//# sourceMappingURL=Booking.js.map