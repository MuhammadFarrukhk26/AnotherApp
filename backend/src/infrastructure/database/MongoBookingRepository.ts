import { Booking } from '../../domain/entities/Booking';
import { IBookingRepository } from '../../domain/repositories/IBookingRepository';
import { BookingModel } from './models/BookingModel';

export class MongoBookingRepository implements IBookingRepository {
  public async findById(id: string): Promise<Booking | null> {
    const record = await BookingModel.findOne({ id }).exec();
    if (!record) return null;
    return this.mapToDomain(record);
  }

  public async save(booking: Booking): Promise<Booking> {
    const updateData = {
      id: booking.id,
      customerId: booking.customerId,
      workerId: booking.workerId,
      serviceType: booking.serviceType,
      status: booking.status,
      scheduledTime: booking.scheduledTime,
      address: booking.address,
      price: booking.price,
      latitude: booking.latitude,
      longitude: booking.longitude,
      beforePhoto: booking.beforePhoto,
      afterPhoto: booking.afterPhoto,
      paymentStatus: booking.paymentStatus,
      paymentMethod: booking.paymentMethod,
      rating: booking.rating,
      review: booking.review,
      createdAt: booking.createdAt,
    };

    await BookingModel.findOneAndUpdate(
      { id: booking.id },
      updateData,
      { upsert: true, new: true }
    ).exec();

    return booking;
  }

  public async findAllByCustomerId(customerId: string): Promise<Booking[]> {
    const records = await BookingModel.find({ customerId }).exec();
    return records.map(record => this.mapToDomain(record));
  }

  public async findAllByWorkerId(workerId: string): Promise<Booking[]> {
    const records = await BookingModel.find({ workerId }).exec();
    return records.map(record => this.mapToDomain(record));
  }

  public async findAvailableBookings(): Promise<Booking[]> {
    const records = await BookingModel.find({ status: 'PENDING' }).exec();
    return records.map(record => this.mapToDomain(record));
  }

  private mapToDomain(record: any): Booking {
    return new Booking(
      record.id,
      record.customerId,
      record.serviceType,
      record.status,
      record.scheduledTime,
      record.address,
      record.price,
      record.latitude,
      record.longitude,
      record.createdAt,
      record.workerId || undefined,
      record.beforePhoto || undefined,
      record.afterPhoto || undefined,
      record.paymentStatus || undefined,
      record.paymentMethod || undefined,
      record.rating !== undefined ? record.rating : undefined,
      record.review || undefined
    );
  }
}
