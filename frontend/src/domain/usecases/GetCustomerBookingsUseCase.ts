import { Booking } from '../models/Booking';
import { IBookingRepository } from '../repositories/IBookingRepository';

export class GetCustomerBookingsUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(customerId: string): Promise<Booking[]> {
    if (!customerId) {
      throw new Error('Customer ID is required to fetch bookings');
    }
    return await this.bookingRepository.getCustomerBookings(customerId);
  }
}
