import {Component, OnDestroy} from '@angular/core';
import {PaymentService} from '../../dashboard/entities/_temp_payment/payment.service';
import {Checkout, ICheckoutOperation} from '../checkout.model';
import {SimpleBillResponse} from '../../shared/model/pkbillms/simple-bill-response.model';
import {FormBuilder} from '@angular/forms';
import {Subject} from 'rxjs';
import {Router} from '@angular/router';
import {PayRequest} from '../../shared/model/pkbillms/pay-request.model';
import {finalize, takeUntil} from 'rxjs/operators';

@Component({
  templateUrl: 'checkout-agent.component.html',
})
export class CheckoutAgentComponent implements OnDestroy, ICheckoutOperation {
  private readonly destroy$ = new Subject<void>();

  checkout: Checkout;
  loading: boolean;

  editForm = this.fb.group({
    deposit: ['ВАШ ДЕПОЗИТ'],
  });

  constructor(private readonly fb: FormBuilder, private readonly router: Router, private readonly paymentService: PaymentService) {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  pay(): void {
    const request: PayRequest = {
      billId: this.checkout.bill.id,
    };

    this.loading = true;
    this.paymentService
      .pay(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe((response: SimpleBillResponse) => this.onPayed(response));
  }

  onPayed(response: SimpleBillResponse): void {
    if (!response.success) {
      return this.paymentService.showResponseExceptionMessage(response);
    }

    this.router.navigate(['/checkout/success'], {
      queryParams: {
        orderId: this.checkout.params.orderId,
        redirectUrl: this.checkout.params.redirectUrl,
      },
    });
  }
}
