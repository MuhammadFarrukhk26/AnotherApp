import { Router } from 'express';
import { BookingController } from '../controllers/BookingController';
import { MessageController } from '../controllers/MessageController';
import { CreateBookingUseCase } from '../../application/usecases/CreateBookingUseCase';
import { GetBookingUseCase } from '../../application/usecases/GetBookingUseCase';
import { GetCustomerBookingsUseCase } from '../../application/usecases/GetCustomerBookingsUseCase';
import { PayBookingUseCase } from '../../application/usecases/PayBookingUseCase';
import { UpdateBookingStatusUseCase } from '../../application/usecases/UpdateBookingStatusUseCase';
import { RateBookingUseCase } from '../../application/usecases/RateBookingUseCase';
import { GetMessagesUseCase } from '../../application/usecases/GetMessagesUseCase';
import { SendMessageUseCase } from '../../application/usecases/SendMessageUseCase';
import { MongoBookingRepository } from '../../infrastructure/database/MongoBookingRepository';
import { MongoMessageRepository } from '../../infrastructure/database/MongoMessageRepository';

const router = Router();

// Instantiate Clean Architecture dependencies bottom-up with MongoDB integration
const bookingRepository = new MongoBookingRepository();
const createBookingUseCase = new CreateBookingUseCase(bookingRepository);
const getBookingUseCase = new GetBookingUseCase(bookingRepository);
const getCustomerBookingsUseCase = new GetCustomerBookingsUseCase(bookingRepository);
const payBookingUseCase = new PayBookingUseCase(bookingRepository);
const updateBookingStatusUseCase = new UpdateBookingStatusUseCase(bookingRepository);
const rateBookingUseCase = new RateBookingUseCase(bookingRepository);

const bookingController = new BookingController(
  createBookingUseCase,
  getBookingUseCase,
  getCustomerBookingsUseCase,
  payBookingUseCase,
  updateBookingStatusUseCase,
  rateBookingUseCase
);

// Message Clean Architecture dependencies
const messageRepository = new MongoMessageRepository();
const getMessagesUseCase = new GetMessagesUseCase(messageRepository);
const sendMessageUseCase = new SendMessageUseCase(messageRepository);

const messageController = new MessageController(
  getMessagesUseCase,
  sendMessageUseCase
);

// Booking routes
router.post('/bookings', bookingController.create);
router.get('/bookings/:id', bookingController.getById);
router.get('/bookings/customer/:customerId', bookingController.getByCustomerId);
router.post('/bookings/:id/pay', bookingController.pay);
router.patch('/bookings/:id/status', bookingController.updateStatus);
router.post('/bookings/:id/rate', bookingController.rate);

// Message routes
router.get('/bookings/:bookingId/messages', messageController.getMessages);
router.post('/bookings/:bookingId/messages', messageController.sendMessage);

export default router;

