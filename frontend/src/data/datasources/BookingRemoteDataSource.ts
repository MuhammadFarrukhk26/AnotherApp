import axios from 'axios';
import { Booking as IBooking, CreateBookingDTO } from '@hazir/shared';

export class BookingRemoteDataSource {
  private baseUrl = 'https://api.hazir-app.com/api/v1'; // Configuration injected via config

  public async fetchBooking(id: string): Promise<IBooking> {
    const response = await axios.get<{ success: boolean; data: IBooking }>(
      `${this.baseUrl}/bookings/${id}`
    );
    return response.data.data;
  }

  public async submitBooking(dto: CreateBookingDTO): Promise<IBooking> {
    const response = await axios.post<{ success: boolean; data: IBooking }>(
      `${this.baseUrl}/bookings`,
      dto,
      {
        headers: { 'x-user-id': 'cust_mock_react_native' }
      }
    );
    return response.data.data;
  }

  public async fetchCustomerBookings(customerId: string): Promise<IBooking[]> {
    const response = await axios.get<{ success: boolean; data: IBooking[] }>(
      `${this.baseUrl}/bookings/customer/${customerId}`
    );
    return response.data.data;
  }
}
