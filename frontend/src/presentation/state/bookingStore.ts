import { create } from 'zustand';
import { Booking } from '../../domain/models/Booking';
import { CreateBookingDTO } from '@hazir/shared';
import { GetBookingUseCase } from '../../domain/usecases/GetBookingUseCase';
import { GetCustomerBookingsUseCase } from '../../domain/usecases/GetCustomerBookingsUseCase';
import { CreateBookingUseCase } from '../../domain/usecases/CreateBookingUseCase';
import { UpdateBookingStatusUseCase } from '../../domain/usecases/UpdateBookingStatusUseCase';
import { PayBookingUseCase } from '../../domain/usecases/PayBookingUseCase';
import { RateBookingUseCase } from '../../domain/usecases/RateBookingUseCase';
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
  updateBookingStatus: (
    id: string,
    status: string,
    workerId?: string,
    beforePhoto?: string,
    afterPhoto?: string
  ) => Promise<Booking>;
  payBooking: (id: string, paymentMethod: string) => Promise<Booking>;
  rateBooking: (id: string, rating: number, review?: string) => Promise<Booking>;
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
const updateBookingStatusUseCase = new UpdateBookingStatusUseCase(bookingRepository);
const payBookingUseCase = new PayBookingUseCase(bookingRepository);
const rateBookingUseCase = new RateBookingUseCase(bookingRepository);

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

  updateBookingStatus: async (
    id: string,
    status: string,
    workerId?: string,
    beforePhoto?: string,
    afterPhoto?: string
  ) => {
    set({ loading: true, error: null });
    try {
      const updated = await updateBookingStatusUseCase.execute(id, status, workerId, beforePhoto, afterPhoto);
      set((state) => ({
        currentBooking: state.currentBooking?.id === id ? updated : state.currentBooking,
        bookings: state.bookings.map((b) => (b.id === id ? updated : b)),
        loading: false,
      }));
      return updated;
    } catch (err: any) {
      set({ error: err.message || 'Failed to update booking status', loading: false });
      throw err;
    }
  },

  payBooking: async (id: string, paymentMethod: string) => {
    set({ loading: true, error: null });
    try {
      const updated = await payBookingUseCase.execute(id, paymentMethod);
      set((state) => ({
        currentBooking: state.currentBooking?.id === id ? updated : state.currentBooking,
        bookings: state.bookings.map((b) => (b.id === id ? updated : b)),
        loading: false,
      }));
      return updated;
    } catch (err: any) {
      set({ error: err.message || 'Failed to pay booking', loading: false });
      throw err;
    }
  },

  rateBooking: async (id: string, rating: number, review?: string) => {
    set({ loading: true, error: null });
    try {
      const updated = await rateBookingUseCase.execute(id, rating, review);
      set((state) => ({
        currentBooking: state.currentBooking?.id === id ? updated : state.currentBooking,
        bookings: state.bookings.map((b) => (b.id === id ? updated : b)),
        loading: false,
      }));
      return updated;
    } catch (err: any) {
      set({ error: err.message || 'Failed to rate booking', loading: false });
      throw err;
    }
  },

  clearState: () => set({ bookings: [], currentBooking: null, error: null, loading: false }),
}));
