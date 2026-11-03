import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {TransactionComponent} from './transaction.component';
import {TRANSACTION_ROUTES, TransactionResolve} from './transaction.route';
import {SharedModule} from '../../../shared/shared.module';
import {EditTransactionComponent} from './edit-transaction/edit-transaction.component';
import {NzListModule} from 'ng-zorro-antd/list';
import {NzCollapseModule} from 'ng-zorro-antd/collapse';
import {TransactionUpdateModalComponent} from './transaction-update-modal.component';
import {TransactionBillsComponent} from './transaction-bills/transaction-bills.component';
import {NzDropDownModule} from 'ng-zorro-antd/dropdown';
import {BmAmountPipe} from '../../../shared/component/bm-amount/bm-amount.pipe';
import {NglStickyScrollXModule} from 'ngl-sticky-scroll-x';
import {NzPopconfirmModule} from 'ng-zorro-antd/popconfirm';
import {CancelPayModalComponent} from './edit-transaction/cancel-pay/cancel-pay-modal.component';
import {PanMaskPipe} from './mask-pipe/pan-mask.pipe';
import {TokenMask} from './mask-pipe/token-mask.pipe';

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(TRANSACTION_ROUTES),
    NzListModule,
    NzCollapseModule,
    NzDropDownModule,
    NglStickyScrollXModule,
    NzPopconfirmModule,
  ],
  declarations: [
    TransactionComponent,
    EditTransactionComponent,
    TransactionUpdateModalComponent,
    TransactionBillsComponent,
    CancelPayModalComponent,
    PanMaskPipe,
    TokenMask,
  ],
  providers: [BmAmountPipe, TransactionResolve],
})
export class TransactionModule {}
