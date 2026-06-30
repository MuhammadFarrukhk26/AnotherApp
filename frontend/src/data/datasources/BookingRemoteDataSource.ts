import axios from 'axios';
import { Booking as IBooking, CreateBookingDTO } from '@hazir/shared';

export class BookingRemoteDataSource {
  private baseUrl = 'https://api.hazir-app.com/api/v1'; // Configuration injected via config

  public async fetchBooking(id: string): Promise<IBooking> {
    try {
      const response = await axios.get<{ success: boolean; data: IBooking }>(
        `${this.baseUrl}/bookings/${id}`
      );
      return response.data.data;
    } catch (error: any) {
      this.handleApiError(error);
      throw error;
    }
  }

  public async submitBooking(dto: CreateBookingDTO): Promise<IBooking> {
    try {
      const userId = await this.getStoredUserId();
      const response = await axios.post<{ success: boolean; data: IBooking }>(
        `${this.baseUrl}/bookings`,
        dto,
        {
          headers: { 'x-user-id': userId }
        }
      );
      return response.data.data;
    } catch (error: any) {
      this.handleApiError(error);
      throw error;
    }
  }

  public async fetchCustomerBookings(customerId: string): Promise<IBooking[]> {
    try {
      const response = await axios.get<{ success: boolean; data: IBooking[] }>(
        `${this.baseUrl}/bookings/customer/${customerId}`
      );
      return response.data.data;
    } catch (error: any) {
      this.handleApiError(error);
      throw error;
    }
  }

  private handleApiError(error: any) {
    if (error.response) {
      // Server responded with non-2xx status code
      console.error('API Error Response:', error.response.status, error.response.data);
      const serverMessage = error.response.data?.message || error.response.data?.error;
      error.message = serverMessage ? `Server Error: ${serverMessage}` : `Request failed with status ${error.response.status}`;
    } else if (error.request) {
      // Request was made but no response was received (network offline or host unreachable)
      console.error('API No Response:', error.request);
      error.message = 'Network Error: Cannot connect to Hazir servers. Please check your internet connection.';
    } else {
      // Something went wrong setting up the request
      console.error('API Config Error:', error.message);
      error.message = `Application Error: ${error.message}`;
    }
  }

  private async getStoredUserId(): Promise<string> {
    // Dynamic cross-platform loading to match iOS & Android storage standard.
    // If AsyncStorage is available in the bundle/system, load the user token/ID.
    try {
      const AsyncStorage = require('@react-native-async-storage/async-storage')?.default;
      if (AsyncStorage) {
        const storedId = await AsyncStorage.getItem('hazir_current_user_id');
        if (storedId) return storedId;
      }
    } catch (e) {
      console.warn('AsyncStorage is not available for React Native session storage, falling back to mock id.');
    }
    return 'cust_mock_react_native';
  }
}
