import { IBookingRepository } from '../../domain/repositories/IBookingRepository';
import { Booking } from '../../domain/entities/Booking';

export class RateBookingUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(bookingId: string, rating: number, review?: string): Promise<Booking> {
    const booking = await this.bookingRepository.findById(bookingId);
    if (!booking) {
      throw new Error(`Booking with ID ${bookingId} not found`);
    }

    booking.rate(rating, review);
    await this.bookingRepository.save(booking);
    return booking;
  }
}
