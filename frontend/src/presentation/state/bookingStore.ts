import { create } from 'zustand';
import { Booking } from '../../domain/models/Booking';
import { CreateBookingDTO } from '@hazir/shared';
import { GetBookingUseCase } from '../../domain/usecases/GetBookingUseCase';
import { BookingRepositoryImpl } from '../../data/repositories/BookingRepositoryImpl';
import { BookingRemoteDataSource } from '../../data/datasources/BookingRemoteDataSource';

interface BookingState {
  currentBooking: Booking | null;
  loading: boolean;
  error: string | null;
  fetchBookingDetails: (id: string) => Promise<void>;
  clearState: () => void;
}

// Setup dependencies bottom-up
const remoteDataSource = new BookingRemoteDataSource();
const bookingRepository = new BookingRepositoryImpl(remoteDataSource);
const getBookingUseCase = new GetBookingUseCase(bookingRepository);

export const useBookingStore = create<BookingState>((set) => ({
  currentBooking: null,
  loading: false,
  error: null,

  fetchBookingDetails: async (id: string) => {
    set({ loading: true, error: null });
    try {
      const details = await getBookingUseCase.execute(id);
      set({ currentBooking: details, loading: false });
    } catch (err: any) {
      set({ error: err.message || 'Failed to load booking details', loading: false });
    }
  },

  clearState: () => set({ currentBooking: null, error: null, loading: false }),
}));
