import { CreateBookingDTO } from '@hazir/shared';
import { Booking } from '../models/Booking';

export interface IBookingRepository {
  getBookingDetails(id: string): Promise<Booking>;
  createBooking(dto: CreateBookingDTO): Promise<Booking>;
  getCustomerBookings(customerId: string): Promise<Booking[]>;
}
