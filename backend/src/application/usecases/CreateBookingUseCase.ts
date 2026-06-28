import { CreateBookingDTO } from '@hazir/shared';
import { Booking } from '../../domain/entities/Booking';
import { IBookingRepository } from '../../domain/repositories/IBookingRepository';

export class CreateBookingUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(customerId: string, dto: CreateBookingDTO): Promise<Booking> {
    const id = `booking_${Math.random().toString(36).substr(2, 9)}`;
    const now = new Date().toISOString();

    const newBooking = new Booking(
      id,
      customerId,
      dto.serviceType,
      'PENDING',
      dto.scheduledTime,
      dto.address,
      dto.price,
      dto.latitude,
      dto.longitude,
      now
    );

    return await this.bookingRepository.save(newBooking);
  }
}
