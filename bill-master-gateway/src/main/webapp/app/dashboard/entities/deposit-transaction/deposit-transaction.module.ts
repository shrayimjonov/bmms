import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
import {DepositTransactionComponent} from './deposit-transaction.component';

const ROUTES: Routes = [
  {
    path: '',
    component: DepositTransactionComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [DepositTransactionComponent],
})
export class DepositTransactionModule {}
