import { Injectable, NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { CurrencyComponent } from './currency.component';
import { CurrencyUpdateComponent } from './currency-update.component';
import { ActivatedRouteSnapshot, Resolve, Router, RouterModule, Routes } from '@angular/router';
import { Currency } from './currency.model';
import { CurrencyService } from './currency.service';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { HttpResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class CurrencyRoutingResolveService implements Resolve<Currency> {
  constructor(protected service: CurrencyService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<Currency> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((currency: HttpResponse<Currency>) => {
          if (currency.body) {
            return of(currency.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Currency());
  }
}

const ROUTES: Routes = [
  {
    path: '',
    component: CurrencyComponent,
    data: {
      defaultSort: 'id,asc',
    },
  },
  {
    path: 'new',
    component: CurrencyUpdateComponent,
    data: {
      pageTitle: 'billMasterGwApp.bmmsCurrency.home.createOrEditLabel',
    },
    resolve: {
      currency: CurrencyRoutingResolveService,
    },
  },
  {
    path: ':id/edit',
    component: CurrencyUpdateComponent,
    data: {
      pageTitle: 'billMasterGwApp.bmmsCurrency.home.createOrEditLabel',
    },
    resolve: {
      currency: CurrencyRoutingResolveService,
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [CurrencyComponent, CurrencyUpdateComponent],
  providers: [DatePipe],
})
export class CurrencyModule {}
