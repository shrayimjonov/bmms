import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {BillComponent} from './bill.component';
import {RouterModule, Routes} from '@angular/router';
import {BillInfoModalComponent} from './bill-info-modal.component';
import {NzListModule} from 'ng-zorro-antd/list';

const ROUTES: Routes = [
  {
    path: '',
    component: BillComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), NzListModule],
  declarations: [BillComponent, BillInfoModalComponent],
})
export class BillModule {}
