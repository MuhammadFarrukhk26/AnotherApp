import { Booking as IBooking } from '@hazir/shared';

export class Booking implements IBooking {
  constructor(
    public id: string,
    public customerId: string,
    public serviceType: string,
    public status: 'PENDING' | 'ACCEPTED' | 'EN_ROUTE' | 'ARRIVED' | 'IN_PROGRESS' | 'STARTED' | 'COMPLETED' | 'CANCELLED',
    public scheduledTime: string,
    public address: string,
    public price: number,
    public latitude: number,
    public longitude: number,
    public createdAt: string,
    public workerId?: string,
    public beforePhoto?: string,
    public afterPhoto?: string,
    public paymentStatus?: 'UNPAID' | 'PAID',
    public paymentMethod?: string,
    public rating?: number,
    public review?: string
  ) {}

  public rate(rating: number, review?: string): void {
    if (this.status !== 'COMPLETED') {
      throw new Error(`Cannot rate a booking that is not completed (current: ${this.status})`);
    }
    if (this.paymentStatus !== 'PAID') {
      throw new Error(`Cannot rate a booking that is not paid yet`);
    }
    if (rating < 1 || rating > 5) {
      throw new Error(`Rating must be between 1 and 5`);
    }
    this.rating = rating;
    this.review = review;
  }

  public accept(workerId: string): void {
    if (this.status !== 'PENDING') {
      throw new Error(`Cannot accept booking in ${this.status} status`);
    }
    this.status = 'ACCEPTED';
    this.workerId = workerId;
  }

  public enRoute(): void {
    this.status = 'EN_ROUTE';
  }

  public arrive(): void {
    this.status = 'ARRIVED';
  }

  public startService(): void {
    this.status = 'IN_PROGRESS';
  }

  public completeService(beforePhoto?: string, afterPhoto?: string): void {
    this.status = 'COMPLETED';
    this.beforePhoto = beforePhoto;
    this.afterPhoto = afterPhoto;
    this.paymentStatus = 'UNPAID'; // initialized on completion
  }

  public pay(paymentMethod: string): void {
    if (this.status !== 'COMPLETED') {
      throw new Error(`Cannot pay for booking that is not completed yet (current: ${this.status})`);
    }
    this.paymentStatus = 'PAID';
    this.paymentMethod = paymentMethod;
  }

  public cancel(): void {
    if (this.status === 'COMPLETED' || this.status === 'CANCELLED') {
      throw new Error(`Cannot cancel booking in ${this.status} status`);
    }
    this.status = 'CANCELLED';
  }
}
