import { CreateBookingDTO } from '@hazir/shared';
import { Booking } from '../models/Booking';
import { IBookingRepository } from '../repositories/IBookingRepository';

export class CreateBookingUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(dto: CreateBookingDTO): Promise<Booking> {
    if (!dto.serviceType) {
      throw new Error('Service type is required to schedule a service');
    }
    if (!dto.scheduledTime) {
      throw new Error('Scheduled time is required to schedule a service');
    }
    return await this.bookingRepository.createBooking(dto);
  }
}
