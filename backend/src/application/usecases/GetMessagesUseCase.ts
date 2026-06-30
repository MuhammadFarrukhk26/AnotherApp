import { IMessageRepository, MessageDomain } from '../../domain/repositories/IMessageRepository';

export class GetMessagesUseCase {
  constructor(private messageRepository: IMessageRepository) {}

  public async execute(bookingId: string): Promise<MessageDomain[]> {
    if (!bookingId) {
      throw new Error('Booking ID is required');
    }
    return await this.messageRepository.findByBookingId(bookingId);
  }
}
