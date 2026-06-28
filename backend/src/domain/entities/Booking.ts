import { Booking as IBooking } from '@hazir/shared';

export class Booking implements IBooking {
  constructor(
    public id: string,
    public customerId: string,
    public serviceType: string,
    public status: 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED',
    public scheduledTime: string,
    public address: string,
    public price: number,
    public latitude: number,
    public longitude: number,
    public createdAt: string,
    public workerId?: string,
    public beforePhoto?: string,
    public afterPhoto?: string
  ) {}

  public accept(workerId: string): void {
    if (this.status !== 'PENDING') {
      throw new Error(`Cannot accept booking in ${this.status} status`);
    }
    this.status = 'ACCEPTED';
    this.workerId = workerId;
  }

  public startService(): void {
    if (this.status !== 'ACCEPTED') {
      throw new Error(`Cannot start service for booking in ${this.status} status`);
    }
    this.status = 'IN_PROGRESS';
  }

  public completeService(beforePhoto?: string, afterPhoto?: string): void {
    if (this.status !== 'IN_PROGRESS') {
      throw new Error(`Cannot complete service for booking in ${this.status} status`);
    }
    this.status = 'COMPLETED';
    this.beforePhoto = beforePhoto;
    this.afterPhoto = afterPhoto;
  }

  public cancel(): void {
    if (this.status === 'COMPLETED' || this.status === 'CANCELLED') {
      throw new Error(`Cannot cancel booking in ${this.status} status`);
    }
    this.status = 'CANCELLED';
  }
}
