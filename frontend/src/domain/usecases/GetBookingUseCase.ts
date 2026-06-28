import { Booking } from '../models/Booking';
import { IBookingRepository } from '../repositories/IBookingRepository';

export class GetBookingUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(bookingId: string): Promise<Booking> {
    if (!bookingId) {
      throw new Error('Booking ID is required to fetch details');
    }
    return await this.bookingRepository.getBookingDetails(bookingId);
  }
}
