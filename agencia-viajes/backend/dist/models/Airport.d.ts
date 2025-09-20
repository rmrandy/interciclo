import mongoose, { Document } from 'mongoose';
import { IAirport } from '../types';
export interface IAirportDocument extends Omit<IAirport, '_id'>, Document {
}
declare const _default: mongoose.Model<IAirportDocument, {}, {}, {}, mongoose.Document<unknown, {}, IAirportDocument, {}, {}> & IAirportDocument & Required<{
    _id: unknown;
}> & {
    __v: number;
}, any>;
export default _default;
//# sourceMappingURL=Airport.d.ts.map