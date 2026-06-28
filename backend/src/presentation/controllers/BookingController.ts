import { Request, Response, NextFunction } from 'express';
import { CreateBookingSchema } from '@hazir/shared';
import { CreateBookingUseCase } from '../../application/usecases/CreateBookingUseCase';

export class BookingController {
  constructor(private createBookingUseCase: CreateBookingUseCase) {}

  public create = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      // Mock authenticated customer
      const customerId = req.headers['x-user-id'] as string || 'cust_temp_123';
      
      // Strict input validation using shared schema
      const validatedData = CreateBookingSchema.parse(req.body);

      const booking = await this.createBookingUseCase.execute(customerId, validatedData);

      res.status(201).json({
        success: true,
        data: booking,
      });
    } catch (error) {
      next(error);
    }
  };
}
