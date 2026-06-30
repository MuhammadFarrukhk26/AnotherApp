import { create } from 'zustand';
import { Booking } from '../../domain/models/Booking';
import { CreateBookingDTO } from '@hazir/shared';
import { GetBookingUseCase } from '../../domain/usecases/GetBookingUseCase';
import { GetCustomerBookingsUseCase } from '../../domain/usecases/GetCustomerBookingsUseCase';
import { CreateBookingUseCase } from '../../domain/usecases/CreateBookingUseCase';
import { BookingRepositoryImpl } from '../../data/repositories/BookingRepositoryImpl';
import { BookingRemoteDataSource } from '../../data/datasources/BookingRemoteDataSource';

interface BookingState {
  bookings: Booking[];
  currentBooking: Booking | null;
  loading: boolean;
  error: string | null;
  fetchBookingDetails: (id: string) => Promise<void>;
  fetchCustomerBookings: (customerId: string) => Promise<void>;
  scheduleBooking: (dto: CreateBookingDTO) => Promise<Booking>;
  clearState: () => void;
  favoriteWorkerIds: string[];
  toggleFavoriteWorker: (workerId: string) => void;
}

// Setup dependencies bottom-up
const remoteDataSource = new BookingRemoteDataSource();
const bookingRepository = new BookingRepositoryImpl(remoteDataSource);
const getBookingUseCase = new GetBookingUseCase(bookingRepository);
const getCustomerBookingsUseCase = new GetCustomerBookingsUseCase(bookingRepository);
const createBookingUseCase = new CreateBookingUseCase(bookingRepository);

export const useBookingStore = create<BookingState>((set) => ({
  bookings: [],
  currentBooking: null,
  loading: false,
  error: null,
  favoriteWorkerIds: [],

  toggleFavoriteWorker: (workerId: string) => {
    set((state) => {
      const exists = state.favoriteWorkerIds.includes(workerId);
      const updated = exists
        ? state.favoriteWorkerIds.filter((id) => id !== workerId)
        : [...state.favoriteWorkerIds, workerId];
      return { favoriteWorkerIds: updated };
    });
  },

  fetchBookingDetails: async (id: string) => {
    set({ loading: true, error: null });
    try {
      const details = await getBookingUseCase.execute(id);
      set({ currentBooking: details, loading: false });
    } catch (err: any) {
      set({ error: err.message || 'Failed to load booking details', loading: false });
    }
  },

  fetchCustomerBookings: async (customerId: string) => {
    set({ loading: true, error: null });
    try {
      const list = await getCustomerBookingsUseCase.execute(customerId);
      set({ bookings: list, loading: false });
    } catch (err: any) {
      set({ error: err.message || 'Failed to load customer bookings', loading: false });
    }
  },

  scheduleBooking: async (dto: CreateBookingDTO) => {
    set({ loading: true, error: null });
    try {
      const newBooking = await createBookingUseCase.execute(dto);
      set((state) => ({
        bookings: [newBooking, ...state.bookings],
        loading: false,
      }));
      return newBooking;
    } catch (err: any) {
      set({ error: err.message || 'Failed to schedule recurring booking', loading: false });
      throw err;
    }
  },

  clearState: () => set({ bookings: [], currentBooking: null, error: null, loading: false }),
}));
