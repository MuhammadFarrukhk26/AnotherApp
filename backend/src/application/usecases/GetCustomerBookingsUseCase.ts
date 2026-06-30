import { Booking } from '../../domain/entities/Booking';
import { IBookingRepository } from '../../domain/repositories/IBookingRepository';

export class GetCustomerBookingsUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(customerId: string): Promise<Booking[]> {
    return await this.bookingRepository.findAllByCustomerId(customerId);
  }
}
