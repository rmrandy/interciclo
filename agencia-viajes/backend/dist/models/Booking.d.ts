import mongoose, { Document } from 'mongoose';
import { IBooking } from '../types';
export interface IBookingDocument extends Omit<IBooking, '_id'>, Document {
}
declare const _default: mongoose.Model<IBookingDocument, {}, {}, {}, mongoose.Document<unknown, {}, IBookingDocument, {}, {}> & IBookingDocument & Required<{
    _id: unknown;
}> & {
    __v: number;
}, any>;
export default _default;
//# sourceMappingURL=Booking.d.ts.map