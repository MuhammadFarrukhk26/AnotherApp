import { IBookingRepository } from '../../domain/repositories/IBookingRepository';
import { Booking } from '../../domain/entities/Booking';

export class UpdateBookingStatusUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(
    bookingId: string,
    status: 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED',
    workerId?: string,
    beforePhoto?: string,
    afterPhoto?: string
  ): Promise<Booking> {
    const booking = await this.bookingRepository.findById(bookingId);
    if (!booking) {
      throw new Error(`Booking with ID ${bookingId} not found`);
    }

    if (status === 'ACCEPTED') {
      booking.accept(workerId || 'worker_ayaan_sheikh');
    } else if (status === 'IN_PROGRESS') {
      booking.startService();
    } else if (status === 'COMPLETED') {
      booking.completeService(beforePhoto, afterPhoto);
    } else if (status === 'CANCELLED') {
      booking.cancel();
    } else {
      throw new Error(`Invalid status transition: ${status}`);
    }

    await this.bookingRepository.save(booking);
    return booking;
  }
}
