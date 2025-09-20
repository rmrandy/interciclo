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
const AirportSchema = new mongoose_1.Schema({
    code: {
        type: String,
        required: [true, 'El código del aeropuerto es requerido'],
        unique: true,
        uppercase: true,
        trim: true,
        length: [3, 'El código debe tener exactamente 3 caracteres']
    },
    name: {
        type: String,
        required: [true, 'El nombre del aeropuerto es requerido'],
        trim: true,
        maxlength: [100, 'El nombre no puede exceder 100 caracteres']
    },
    city: {
        type: String,
        required: [true, 'La ciudad es requerida'],
        trim: true,
        maxlength: [50, 'La ciudad no puede exceder 50 caracteres']
    },
    country: {
        type: String,
        required: [true, 'El país es requerido'],
        trim: true,
        maxlength: [50, 'El país no puede exceder 50 caracteres']
    },
    coordinates: {
        latitude: {
            type: Number,
            required: [true, 'La latitud es requerida'],
            min: [-90, 'La latitud debe estar entre -90 y 90'],
            max: [90, 'La latitud debe estar entre -90 y 90']
        },
        longitude: {
            type: Number,
            required: [true, 'La longitud es requerida'],
            min: [-180, 'La longitud debe estar entre -180 y 180'],
            max: [180, 'La longitud debe estar entre -180 y 180']
        }
    }
}, {
    timestamps: true
});
AirportSchema.index({ code: 1 });
AirportSchema.index({ city: 1, country: 1 });
AirportSchema.index({ 'coordinates.latitude': 1, 'coordinates.longitude': 1 });
exports.default = mongoose_1.default.model('Airport', AirportSchema);
//# sourceMappingURL=Airport.js.map