import { Booking } from '../entities/Booking';

export interface IBookingRepository {
  findById(id: string): Promise<Booking | null>;
  save(booking: Booking): Promise<Booking>;
  findAllByCustomerId(customerId: string): Promise<Booking[]>;
  findAllByWorkerId(workerId: string): Promise<Booking[]>;
  findAvailableBookings(): Promise<Booking[]>;
}
