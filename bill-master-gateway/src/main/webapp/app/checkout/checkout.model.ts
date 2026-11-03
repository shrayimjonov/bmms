import {Recipient} from '../dashboard/entities/recipient/recipient.model';
import {Bill} from '../dashboard/entities/bill/bill.model';
import {SimpleBillResponse} from '../shared/model/pkbillms/simple-bill-response.model';
import {FormGroup} from '@angular/forms';

export interface ICheckoutOperation {
  checkout: Checkout;
  editForm: FormGroup;

  pay(): void;
  onPayed(response: SimpleBillResponse): void;
}

export class CheckoutParams {
  orderId: string;
  recipientId: number;
  account: string;
  amount: number;
  redirectUrl?: string;
  billId?: number;
}

export class Checkout {
  recipient: Recipient;
  params: CheckoutParams;
  bill?: Bill;
}
