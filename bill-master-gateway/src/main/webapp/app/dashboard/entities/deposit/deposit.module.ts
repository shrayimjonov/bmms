import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
import {DepositComponent} from './deposit.component';
import {DepositUpdateModalComponent} from './deposit-update-modal.component';
import {ReplenishDepositModalComponent} from './replenish-deposit-modal.component';

const ROUTES: Routes = [
  {
    path: '',
    component: DepositComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [DepositComponent, DepositUpdateModalComponent, ReplenishDepositModalComponent],
})
export class DepositModule {}
