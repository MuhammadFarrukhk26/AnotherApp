import mongoose, { Schema, Document } from 'mongoose';

export interface IBookingDocument extends Document {
  id: string;
  customerId: string;
  workerId?: string;
  serviceType: string;
  status: 'PENDING' | 'ACCEPTED' | 'EN_ROUTE' | 'ARRIVED' | 'IN_PROGRESS' | 'STARTED' | 'COMPLETED' | 'CANCELLED';
  scheduledTime: string;
  address: string;
  price: number;
  latitude: number;
  longitude: number;
  beforePhoto?: string;
  afterPhoto?: string;
  paymentStatus?: 'UNPAID' | 'PAID';
  paymentMethod?: string;
  rating?: number;
  review?: string;
  createdAt: string;
}

const BookingSchema: Schema = new Schema(
  {
    id: { type: String, required: true, unique: true, index: true },
    customerId: { type: String, required: true, index: true },
    workerId: { type: String, index: true },
    serviceType: { type: String, required: true },
    status: {
      type: String,
      required: true,
      enum: ['PENDING', 'ACCEPTED', 'EN_ROUTE', 'ARRIVED', 'IN_PROGRESS', 'STARTED', 'COMPLETED', 'CANCELLED'],
      default: 'PENDING',
    },
    scheduledTime: { type: String, required: true },
    address: { type: String, required: true },
    price: { type: Number, required: true },
    latitude: { type: Number, required: true },
    longitude: { type: Number, required: true },
    beforePhoto: { type: String },
    afterPhoto: { type: String },
    paymentStatus: { type: String, enum: ['UNPAID', 'PAID'], default: 'UNPAID' },
    paymentMethod: { type: String },
    rating: { type: Number },
    review: { type: String },
    createdAt: { type: String, required: true },
  },
  {
    timestamps: true,
  }
);

export const BookingModel = mongoose.model<IBookingDocument>('Booking', BookingSchema);
