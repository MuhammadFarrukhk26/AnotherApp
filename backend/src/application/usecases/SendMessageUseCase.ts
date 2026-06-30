import { IMessageRepository, MessageDomain } from '../../domain/repositories/IMessageRepository';

const WORKER_RESPONSES: Record<string, string> = {
  '👋 Hi, are you on your way?': 'Yes, I am en route! Navigating through the main road now. See you in about 10-12 minutes.',
  '🏠 I am at the location.': 'Perfect! Thanks for confirming. I am about 3 km away and moving steadily.',
  '🚗 Parking is available inside.': 'Excellent, thank you! That makes it much easier to park the ride safely.',
  '🔑 Gate/intercom code is 1234.': 'Got it! Gate code 1234. I will enter directly once I arrive.',
  '📞 Please call me when you arrive.': 'Will do! I will ring your phone as soon as I pull up outside.',
  'default': 'Thank you! I am on my way and focused on safe driving. I will update you as soon as I arrive.',
};

export class SendMessageUseCase {
  constructor(private messageRepository: IMessageRepository) {}

  public async execute(
    bookingId: string,
    text: string,
    sender: 'user' | 'worker'
  ): Promise<MessageDomain> {
    if (!bookingId || !text) {
      throw new Error('Booking ID and message text are required');
    }

    const currentTimeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    // Save user's message
    const userMessage: MessageDomain = {
      id: Math.random().toString(36).substring(2, 9),
      bookingId,
      text: text.trim(),
      sender,
      timestamp: currentTimeStr,
    };

    const savedUserMessage = await this.messageRepository.save(userMessage);

    // If the message is from the user, simulate worker response asynchronously or with a tiny delay
    if (sender === 'user') {
      setTimeout(async () => {
        try {
          const matchedResponse = WORKER_RESPONSES[text.trim()] || WORKER_RESPONSES['default'];
          const workerMessage: MessageDomain = {
            id: Math.random().toString(36).substring(2, 9),
            bookingId,
            text: matchedResponse,
            sender: 'worker',
            timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          };
          await this.messageRepository.save(workerMessage);
          console.log(`[Messaging] Automated response sent for booking ${bookingId}`);
        } catch (e) {
          console.error('[Messaging] Error saving automated worker response:', e);
        }
      }, 1000);
    }

    return savedUserMessage;
  }
}
