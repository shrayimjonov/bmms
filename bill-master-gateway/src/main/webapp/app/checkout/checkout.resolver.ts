import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router} from '@angular/router';
import {EMPTY, Observable, of} from 'rxjs';
import {Checkout} from './checkout.model';
import {catchError, mergeMap, pluck} from 'rxjs/operators';
import {Recipient} from '../dashboard/entities/recipient/recipient.model';
import {BillService} from '../dashboard/entities/bill/bill.service';
import {Bill} from '../dashboard/entities/bill/bill.model';
import {PaymentService} from '../dashboard/entities/_temp_payment/payment.service';

@Injectable()
export class CheckoutResolver implements Resolve<Observable<Checkout | never>> {
  constructor(
    private readonly paymentService: PaymentService,
    private readonly billService: BillService,
    private readonly router: Router
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<Checkout | never> {
    const empty = (): Observable<never> => {
      this.router.navigate(['404']);
      return EMPTY;
    };

    const isNotValid = ['orderId', 'recipientId', 'account', 'amount'].some(field => !route.queryParamMap.has(field));

    if (isNotValid) {
      return empty();
    }

    return this.paymentService.getRecipientWithoutAuthorization(route.queryParamMap.get('recipientId')).pipe(
      mergeMap((recipient: Recipient) => {
        if (!recipient) {
          return empty();
        }

        const checkout: Checkout = {
          recipient: recipient,
          params: {
            orderId: route.queryParamMap.get('orderId'),
            account: route.queryParamMap.get('account'),
            redirectUrl: route.queryParamMap.get('redirectUrl'),
            amount: Number(route.queryParamMap.get('amount')),
            recipientId: Number(route.queryParamMap.get('recipientId')),
          },
        };

        const billId = route.queryParamMap.get('billId');

        if (billId) {
          return this.billService.find(Number(billId)).pipe(
            pluck('body'),
            mergeMap((bill: Bill) => {
              checkout.bill = bill;
              return of<Checkout>(checkout);
            }),
            catchError(() => empty())
          );
        }

        return of<Checkout>(checkout);
      })
    );
  }
}
