import { Booking as IBooking } from '@hazir/shared';

export interface Booking extends IBooking {
  // Client-specific computed properties can go here (e.g., formatted times, localization)
  formattedPrice: string;
}

export function createClientBooking(dto: IBooking): Booking {
  return {
    ...dto,
    formattedPrice: `PKR ${dto.price.toLocaleString(undefined, { minimumFractionDigits: 2 })}`
  };
}
