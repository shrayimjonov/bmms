import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {BookkeepingTransactionComponent} from './bookkeeping-transaction.component';
import {RouterModule, Routes} from '@angular/router';
import {NzTabsModule} from "ng-zorro-antd/tabs";

const ROUTES: Routes = [
  {
    path: '',
    component: BookkeepingTransactionComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(ROUTES),
    SharedModule,
    NzTabsModule
  ],
  declarations: [BookkeepingTransactionComponent],
})
export class BookkeepingTransactionModule {
}
