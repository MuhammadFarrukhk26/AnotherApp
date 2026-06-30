import mongoose, { Schema, Document } from 'mongoose';

export interface IMessageDocument extends Document {
  id: string;
  bookingId: string;
  text: string;
  sender: 'user' | 'worker';
  timestamp: string;
  createdAt: Date;
}

const MessageSchema: Schema = new Schema(
  {
    id: { type: String, required: true, unique: true, index: true },
    bookingId: { type: String, required: true, index: true },
    text: { type: String, required: true },
    sender: { type: String, required: true, enum: ['user', 'worker'] },
    timestamp: { type: String, required: true },
    createdAt: { type: Date, default: Date.now },
  },
  {
    timestamps: true,
  }
);

export const MessageModel = mongoose.model<IMessageDocument>('Message', MessageSchema);
