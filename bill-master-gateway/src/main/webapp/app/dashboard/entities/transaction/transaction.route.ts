import {ActivatedRouteSnapshot, Resolve, Router, Routes} from '@angular/router';
import {TransactionComponent} from './transaction.component';
import {EditTransactionComponent} from './edit-transaction/edit-transaction.component';
import {Injectable} from '@angular/core';
import {EMPTY, Observable, of} from 'rxjs';
import {flatMap} from 'rxjs/operators';
import {HttpResponse} from '@angular/common/http';
import {Transaction} from './transaction.model';
import {TransactionService} from './transaction.service';
import {Authority} from '../../../config/authority.constants';
import {CanActivateUserRoute} from '../../../core/auth/can-activate-user-route';

@Injectable()
export class TransactionResolve implements Resolve<Transaction> {
  constructor(private service: TransactionService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<Transaction> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((transaction: HttpResponse<Transaction>) => {
          if (transaction.body) {
            return of(transaction.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Transaction());
  }
}

export const TRANSACTION_ROUTES: Routes = [
  {
    path: '',
    component: TransactionComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
  {
    path: ':id/edit',
    component: EditTransactionComponent,
    resolve: {
      transaction: TransactionResolve,
    },
    data: {
      defaultSort: 'id,desc',
      pageTitle: 'billMasterGwApp.pkbillmsTransaction.editTransaction',
      authorities: [Authority.TRANSACTION_EDIT],
    },
    canActivate: [CanActivateUserRoute],
  },
];
