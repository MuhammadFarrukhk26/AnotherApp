import { Booking } from '../models/Booking';
import { IBookingRepository } from '../repositories/IBookingRepository';

export class PayBookingUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(id: string, paymentMethod: string): Promise<Booking> {
    return await this.bookingRepository.payBooking(id, paymentMethod);
  }
}
