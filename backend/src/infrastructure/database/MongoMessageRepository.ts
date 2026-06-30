import { IMessageRepository, MessageDomain } from '../../domain/repositories/IMessageRepository';
import { MessageModel } from './models/MessageModel';

export class MongoMessageRepository implements IMessageRepository {
  public async save(message: MessageDomain): Promise<MessageDomain> {
    const record = new MessageModel({
      id: message.id,
      bookingId: message.bookingId,
      text: message.text,
      sender: message.sender,
      timestamp: message.timestamp,
    });
    await record.save();
    return message;
  }

  public async findByBookingId(bookingId: string): Promise<MessageDomain[]> {
    const records = await MessageModel.find({ bookingId }).sort({ createdAt: 1 }).exec();
    return records.map((r) => ({
      id: r.id,
      bookingId: r.bookingId,
      text: r.text,
      sender: r.sender,
      timestamp: r.timestamp,
      createdAt: r.createdAt,
    }));
  }
}
