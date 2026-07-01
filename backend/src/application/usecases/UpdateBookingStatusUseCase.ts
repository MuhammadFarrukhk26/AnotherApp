import { IBookingRepository } from '../../domain/repositories/IBookingRepository';
import { Booking } from '../../domain/entities/Booking';

export class UpdateBookingStatusUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(
    bookingId: string,
    status: 'ACCEPTED' | 'EN_ROUTE' | 'ARRIVED' | 'IN_PROGRESS' | 'STARTED' | 'COMPLETED' | 'CANCELLED',
    workerId?: string,
    beforePhoto?: string,
    afterPhoto?: string
  ): Promise<Booking> {
    const booking = await this.bookingRepository.findById(bookingId);
    if (!booking) {
      throw new Error(`Booking with ID ${bookingId} not found`);
    }

    const targetStatus = status.toUpperCase();

    if (targetStatus === 'ACCEPTED') {
      booking.accept(workerId || 'worker_ayaan_sheikh');
    } else if (targetStatus === 'EN_ROUTE' || targetStatus === 'ENROUTE') {
      booking.enRoute();
    } else if (targetStatus === 'ARRIVED') {
      booking.arrive();
    } else if (targetStatus === 'IN_PROGRESS' || targetStatus === 'STARTED') {
      booking.startService();
    } else if (targetStatus === 'COMPLETED') {
      booking.completeService(beforePhoto, afterPhoto);
    } else if (targetStatus === 'CANCELLED') {
      booking.cancel();
    } else {
      throw new Error(`Invalid status transition: ${status}`);
    }

    await this.bookingRepository.save(booking);
    return booking;
  }
}
