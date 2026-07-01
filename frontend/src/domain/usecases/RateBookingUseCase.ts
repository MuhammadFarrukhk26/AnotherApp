import { Booking } from '../models/Booking';
import { IBookingRepository } from '../repositories/IBookingRepository';

export class RateBookingUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(id: string, rating: number, review?: string): Promise<Booking> {
    return await this.bookingRepository.rateBooking(id, rating, review);
  }
}
