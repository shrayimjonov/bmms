import {Component} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {Checkout, ICheckoutOperation} from '../checkout.model';
import {SimpleBillResponse} from '../../shared/model/pkbillms/simple-bill-response.model';
import {FormBuilder, Validators} from '@angular/forms';
import {finalize, takeUntil} from 'rxjs/operators';
import {CheckRequest} from '../../shared/model/pkbillms/check-request.model';
import {PayRequest} from '../../shared/model/pkbillms/pay-request.model';
import {PayType} from '../../shared/model/enumerations/pay-type.model';
import {Router} from '@angular/router';
import {PaymentService} from '../../dashboard/entities/_temp_payment/payment.service';

@Component({
  templateUrl: 'checkout-unauthorized.component.html',
  styles: [
    `
      #exp {
        width: 3.2rem;
        padding-right: 0;
      }
    `,
  ],
  styleUrls: ['../brand-styles/anorbank.scss'],
})
export class CheckoutUnauthorizedComponent implements ICheckoutOperation {
  private readonly destroy$ = new Subject<void>();

  checkout: Checkout;
  checkResponse: SimpleBillResponse = {};
  payResponse: SimpleBillResponse = {};
  loading: boolean;
  codeTimeLeft: Observable<number>;

  editForm = this.fb.group({
    pan: [
      null,
      [Validators.required, Validators.minLength(16), Validators.maxLength(16), Validators.pattern(/^((?!98606003|98606068).)*$/)],
    ],
    exp: [null, [Validators.required, Validators.minLength(4), Validators.maxLength(4)]],
    code: [null],
  });

  constructor(public readonly paymentService: PaymentService, private readonly fb: FormBuilder, private readonly router: Router) {}

  submit(): void {
    if (!this.checkResponse.success) {
      this.check();
    } else {
      this.pay();
    }
  }

  check(): void {
    const request: CheckRequest = {
      account: this.checkout.params.account,
      amount: this.checkout.params.amount * 100,
      recipientId: this.checkout.recipient.id,
      agentTranId: this.checkout.params.orderId,
      currency: this.checkout.recipient.ccy,
      params: {
        pan: this.editForm.get('pan').value,
        exp: this.editForm.get('exp').value,
      },
    };

    this.loading = true;
    this.paymentService
      .check(request, true)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe((response: SimpleBillResponse) => this.onChecked(response));
  }

  pay(): void {
    const request: PayRequest = {
      billId: this.checkResponse.id,
      type: PayType.PC,
      params: {
        otp: this.editForm.get('code').value,
        pan: this.editForm.get('pan').value,
        exp: this.editForm.get('exp').value,
      },
    };

    this.loading = true;
    this.paymentService
      .pay(request, true)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe((response: SimpleBillResponse) => this.onPayed(response));
  }

  onChecked(response: SimpleBillResponse): void {
    this.checkResponse = response;
    if (!response.success) {
      return this.paymentService.showResponseExceptionMessage(response);
    }

    this.editForm.get('code')?.setValidators([Validators.required, Validators.minLength(6), Validators.maxLength(6)]);
    this.codeTimeLeft = this.startTimer().pipe(takeUntil(this.destroy$));
  }

  onPayed(response: SimpleBillResponse): void {
    this.payResponse = response;
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

  private startTimer(duration?: number): Observable<number | null> {
    return new Observable<any>(observer => {
      let timeLeft = duration || 59;
      observer.next(timeLeft);
      const intervalId = setInterval(() => {
        if (timeLeft === 1) {
          timeLeft = null;
          observer.next(null);
          observer.complete();
          clearInterval(intervalId);
        }

        timeLeft--;
        observer.next(timeLeft);
      }, 1000);
    });
  }

  onChangePan(): void {
    if (this.editForm.get('pan').valid) {
      document.getElementById('exp')?.focus();
    }
  }
}
