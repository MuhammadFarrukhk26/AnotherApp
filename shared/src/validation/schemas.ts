import { z } from 'zod';

export const CreateBookingSchema = z.object({
  serviceType: z.string().min(2, 'Service type must be at least 2 characters'),
  scheduledTime: z.string().datetime('Invalid ISO timestamp format'),
  address: z.string().min(5, 'Address must be at least 5 characters'),
  price: z.number().positive('Price must be greater than zero'),
  latitude: z.number().min(-90).max(90),
  longitude: z.number().min(-180).max(180),
});

export const UserRegistrationSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
  role: z.enum(['CUSTOMER', 'WORKER', 'ADMIN']),
});
