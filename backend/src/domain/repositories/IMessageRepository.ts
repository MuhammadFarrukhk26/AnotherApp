export interface MessageDomain {
  id: string;
  bookingId: string;
  text: string;
  sender: 'user' | 'worker';
  timestamp: string;
  createdAt?: Date;
}

export interface IMessageRepository {
  save(message: MessageDomain): Promise<MessageDomain>;
  findByBookingId(bookingId: string): Promise<MessageDomain[]>;
}
