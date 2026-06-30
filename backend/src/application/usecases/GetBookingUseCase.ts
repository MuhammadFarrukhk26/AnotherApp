import { Booking } from '../../domain/entities/Booking';
import { IBookingRepository } from '../../domain/repositories/IBookingRepository';

export class GetBookingUseCase {
  constructor(private bookingRepository: IBookingRepository) {}

  public async execute(id: string): Promise<Booking | null> {
    return await this.bookingRepository.findById(id);
  }
}
