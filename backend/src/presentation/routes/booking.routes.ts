import { Router } from 'express';
import { BookingController } from '../controllers/BookingController';
import { CreateBookingUseCase } from '../../application/usecases/CreateBookingUseCase';
import { PrismaBookingRepository } from '../../infrastructure/database/PrismaBookingRepository';

const router = Router();

// Instantiate Clean Architecture dependencies bottom-up
const bookingRepository = new PrismaBookingRepository();
const createBookingUseCase = new CreateBookingUseCase(bookingRepository);
const bookingController = new BookingController(createBookingUseCase);

router.post('/bookings', bookingController.create);

export default router;
