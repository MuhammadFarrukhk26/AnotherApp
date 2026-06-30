import { Request, Response, NextFunction } from 'express';
import { GetMessagesUseCase } from '../../application/usecases/GetMessagesUseCase';
import { SendMessageUseCase } from '../../application/usecases/SendMessageUseCase';

export class MessageController {
  constructor(
    private getMessagesUseCase: GetMessagesUseCase,
    private sendMessageUseCase: SendMessageUseCase
  ) {}

  public getMessages = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const { bookingId } = req.params;
      const messages = await this.getMessagesUseCase.execute(bookingId);
      
      res.status(200).json({
        success: true,
        data: messages,
      });
    } catch (error) {
      next(error);
    }
  };

  public sendMessage = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      const { bookingId } = req.params;
      const { text, sender } = req.body;

      if (!text || !sender) {
        res.status(400).json({
          success: false,
          error: 'Text and sender parameters are required',
        });
        return;
      }

      const message = await this.sendMessageUseCase.execute(bookingId, text, sender);

      res.status(201).json({
        success: true,
        data: message,
      });
    } catch (error) {
      next(error);
    }
  };
}
