import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {PaymentComponent} from './payment.component';
import {RouterModule, Routes} from '@angular/router';
import {RecipientsFilterPipe} from './pipe/recipients-filter.pipe';
import {NzModalModule} from 'ng-zorro-antd/modal';
import {PaymentModalComponent} from './payment-modal.component';
import {ImageLoaderModule} from '../../../shared/directive/image-loader/image-loader.module';
import {CategoriesFilterPipe} from './pipe/categories-filter.pipe';
import {NzSelectModule} from 'ng-zorro-antd/select';
import {SortByPipe} from './pipe/sort-by.pipe';

const routes: Routes = [
  {
    path: '',
    component: PaymentComponent,
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(routes), NzModalModule, ImageLoaderModule, NzSelectModule],
  declarations: [PaymentComponent, CategoriesFilterPipe, RecipientsFilterPipe, PaymentModalComponent, SortByPipe],
})
export class PaymentModule {}
