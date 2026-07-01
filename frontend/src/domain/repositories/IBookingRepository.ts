import { CreateBookingDTO } from '@hazir/shared';
import { Booking } from '../models/Booking';

export interface IBookingRepository {
  getBookingDetails(id: string): Promise<Booking>;
  createBooking(dto: CreateBookingDTO): Promise<Booking>;
  getCustomerBookings(customerId: string): Promise<Booking[]>;
  updateBookingStatus(
    id: string,
    status: string,
    workerId?: string,
    beforePhoto?: string,
    afterPhoto?: string
  ): Promise<Booking>;
  payBooking(id: string, paymentMethod: string): Promise<Booking>;
  rateBooking(id: string, rating: number, review?: string): Promise<Booking>;
}
