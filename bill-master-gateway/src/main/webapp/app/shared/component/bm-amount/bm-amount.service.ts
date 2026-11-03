import {Injectable} from '@angular/core';

@Injectable({ providedIn: 'root' })
export class BmAmountService {
  toModel(amount: number, ratio: number): number {
    if (!amount) {
      return 0;
    }

    amount = Number(amount);
    return amount / ratio;
  }

  fromModel(amount: number, ratio: number): number {
    if (!amount) {
      return 0;
    }

    amount = Number(amount);
    return amount * ratio;
  }
}
