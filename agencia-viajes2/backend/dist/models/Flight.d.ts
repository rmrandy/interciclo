import mongoose, { Document } from 'mongoose';
import { IFlight } from '../types';
export interface IFlightDocument extends Omit<IFlight, '_id'>, Document {
}
declare const _default: mongoose.Model<IFlightDocument, {}, {}, {}, mongoose.Document<unknown, {}, IFlightDocument, {}, {}> & IFlightDocument & Required<{
    _id: unknown;
}> & {
    __v: number;
}, any>;
export default _default;
//# sourceMappingURL=Flight.d.ts.map