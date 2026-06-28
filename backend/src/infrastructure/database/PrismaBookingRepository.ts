import { Booking } from '../../domain/entities/Booking';
import { IBookingRepository } from '../../domain/repositories/IBookingRepository';

export class PrismaBookingRepository implements IBookingRepository {
  // Mock internal in-memory DB representing Prisma or other ORMs
  private db: Map<string, any> = new Map();

  public async findById(id: string): Promise<Booking | null> {
    const record = this.db.get(id);
    if (!record) return null;
    return this.mapToDomain(record);
  }

  public async save(booking: Booking): Promise<Booking> {
    const record = {
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
      createdAt: booking.createdAt,
    };
    
    this.db.set(booking.id, record);
    return booking;
  }

  public async findAllByCustomerId(customerId: string): Promise<Booking[]> {
    const results: Booking[] = [];
    for (const record of this.db.values()) {
      if (record.customerId === customerId) {
        results.push(this.mapToDomain(record));
      }
    }
    return results;
  }

  public async findAllByWorkerId(workerId: string): Promise<Booking[]> {
    const results: Booking[] = [];
    for (const record of this.db.values()) {
      if (record.workerId === workerId) {
        results.push(this.mapToDomain(record));
      }
    }
    return results;
  }

  public async findAvailableBookings(): Promise<Booking[]> {
    const results: Booking[] = [];
    for (const record of this.db.values()) {
      if (record.status === 'PENDING') {
        results.push(this.mapToDomain(record));
      }
    }
    return results;
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
      record.workerId,
      record.beforePhoto,
      record.afterPhoto
    );
  }
}
