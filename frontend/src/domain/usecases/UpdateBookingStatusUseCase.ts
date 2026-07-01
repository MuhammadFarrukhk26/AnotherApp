import { Booking } from '../models/Booking';
import { IBookingRepository } from '../repositories/IBookingRepository';

export class UpdateBookingStatusUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(
    id: string,
    status: string,
    workerId?: string,
    beforePhoto?: string,
    afterPhoto?: string
  ): Promise<Booking> {
    return await this.bookingRepository.updateBookingStatus(id, status, workerId, beforePhoto, afterPhoto);
  }
}
