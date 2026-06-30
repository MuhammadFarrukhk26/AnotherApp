import { Request, Response, NextFunction } from 'express';
import { CreateBookingSchema } from '@hazir/shared';
import { CreateBookingUseCase } from '../../application/usecases/CreateBookingUseCase';
import { GetBookingUseCase } from '../../application/usecases/GetBookingUseCase';
import { GetCustomerBookingsUseCase } from '../../application/usecases/GetCustomerBookingsUseCase';
import { PayBookingUseCase } from '../../application/usecases/PayBookingUseCase';
import { UpdateBookingStatusUseCase } from '../../application/usecases/UpdateBookingStatusUseCase';
import { RateBookingUseCase } from '../../application/usecases/RateBookingUseCase';

export class BookingController {
  constructor(
    private createBookingUseCase: CreateBookingUseCase,
    private getBookingUseCase: GetBookingUseCase,
    private getCustomerBookingsUseCase: GetCustomerBookingsUseCase,
    private payBookingUseCase: PayBookingUseCase,
    private updateBookingStatusUseCase: UpdateBookingStatusUseCase,
    private rateBookingUseCase: RateBookingUseCase
  ) {}

  public create = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const customerId = req.headers['x-user-id'] as string || 'cust_temp_123';
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

  public getById = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const { id } = req.params;
      const booking = await this.getBookingUseCase.execute(id);
      
      if (!booking) {
        res.status(404).json({
          success: false,
          error: `Booking with ID ${id} not found`,
        });
        return;
      }

      res.status(200).json({
        success: true,
        data: booking,
      });
    } catch (error) {
      next(error);
    }
  };

  public getByCustomerId = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const { customerId } = req.params;
      const bookings = await this.getCustomerBookingsUseCase.execute(customerId);

      res.status(200).json({
        success: true,
        data: bookings,
      });
    } catch (error) {
      next(error);
    }
  };

  public pay = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const { id } = req.params;
      const { paymentMethod } = req.body;

      if (!paymentMethod) {
        res.status(400).json({
          success: false,
          error: 'paymentMethod is required',
        });
        return;
      }

      const booking = await this.payBookingUseCase.execute(id, paymentMethod);

      res.status(200).json({
        success: true,
        data: booking,
      });
    } catch (error) {
      next(error);
    }
  };

  public updateStatus = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const { id } = req.params;
      const { status, workerId, beforePhoto, afterPhoto } = req.body;

      if (!status) {
        res.status(400).json({
          success: false,
          error: 'status parameter is required',
        });
        return;
      }

      const booking = await this.updateBookingStatusUseCase.execute(
        id,
        status,
        workerId,
        beforePhoto,
        afterPhoto
      );

      res.status(200).json({
        success: true,
        data: booking,
      });
    } catch (error) {
      next(error);
    }
  };

  public rate = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const { id } = req.params;
      const { rating, review } = req.body;

      if (rating === undefined || rating === null) {
        res.status(400).json({
          success: false,
          error: 'rating is required',
        });
        return;
      }

      const booking = await this.rateBookingUseCase.execute(id, Number(rating), review);

      res.status(200).json({
        success: true,
        data: booking,
      });
    } catch (error) {
      next(error);
    }
  };
}
