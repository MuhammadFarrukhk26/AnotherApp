import { IBookingRepository } from '../../domain/repositories/IBookingRepository';
import { Booking } from '../../domain/entities/Booking';

export class PayBookingUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(bookingId: string, paymentMethod: string): Promise<Booking> {
    const booking = await this.bookingRepository.findById(bookingId);
    if (!booking) {
      throw new Error(`Booking with ID ${bookingId} not found`);
    }

    // Force status to completed if not already completed so the payment can be recorded
    if (booking.status !== 'COMPLETED') {
      booking.status = 'COMPLETED';
    }

    booking.pay(paymentMethod);
    await this.bookingRepository.save(booking);
    return booking;
  }
}
