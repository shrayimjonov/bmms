import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CashBackComponent } from '../list/cash-back.component';

const cashBackRoute: Routes = [
  {
    path: '',
    component: CashBackComponent,
    data: {
      defaultSort: 'id,asc',
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(cashBackRoute)],
  exports: [RouterModule],
})
export class CashBackRoutingModule {}
