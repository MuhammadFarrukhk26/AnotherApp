export interface User {
  id: string;
  name: string;
  email: string;
  role: 'CUSTOMER' | 'WORKER' | 'ADMIN';
  createdAt: string;
}

export interface Booking {
  id: string;
  customerId: string;
  workerId?: string;
  serviceType: string;
  status: 'PENDING' | 'ACCEPTED' | 'EN_ROUTE' | 'ARRIVED' | 'IN_PROGRESS' | 'STARTED' | 'COMPLETED' | 'CANCELLED';
  scheduledTime: string;
  address: string;
  price: number;
  latitude: number;
  longitude: number;
  beforePhoto?: string;
  afterPhoto?: string;
  paymentStatus?: 'UNPAID' | 'PAID';
  paymentMethod?: string;
  rating?: number;
  review?: string;
  createdAt: string;
}

export interface CreateBookingDTO {
  serviceType: string;
  scheduledTime: string;
  address: string;
  price: number;
  latitude: number;
  longitude: number;
}
