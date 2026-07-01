import { CreateBookingDTO } from '@hazir/shared';
import { Booking, createClientBooking } from '../../domain/models/Booking';
import { IBookingRepository } from '../../domain/repositories/IBookingRepository';
import { BookingRemoteDataSource } from '../datasources/BookingRemoteDataSource';

export class BookingRepositoryImpl implements IBookingRepository {
  constructor(private remoteDataSource: BookingRemoteDataSource) {}

  public async getBookingDetails(id: string): Promise<Booking> {
    const bookingData = await this.remoteDataSource.fetchBooking(id);
    return createClientBooking(bookingData);
  }

  public async createBooking(dto: CreateBookingDTO): Promise<Booking> {
    const bookingData = await this.remoteDataSource.submitBooking(dto);
    return createClientBooking(bookingData);
  }

  public async getCustomerBookings(customerId: string): Promise<Booking[]> {
    const bookingsData = await this.remoteDataSource.fetchCustomerBookings(customerId);
    return bookingsData.map(createClientBooking);
  }

  public async updateBookingStatus(
    id: string,
    status: string,
    workerId?: string,
    beforePhoto?: string,
    afterPhoto?: string
  ): Promise<Booking> {
    const bookingData = await this.remoteDataSource.updateBookingStatus(id, status, workerId, beforePhoto, afterPhoto);
    return createClientBooking(bookingData);
  }

  public async payBooking(id: string, paymentMethod: string): Promise<Booking> {
    const bookingData = await this.remoteDataSource.payBooking(id, paymentMethod);
    return createClientBooking(bookingData);
  }

  public async rateBooking(id: string, rating: number, review?: string): Promise<Booking> {
    const bookingData = await this.remoteDataSource.rateBooking(id, rating, review);
    return createClientBooking(bookingData);
  }
}
