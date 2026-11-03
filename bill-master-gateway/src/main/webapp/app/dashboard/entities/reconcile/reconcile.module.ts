import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ReconcileComponent } from './reconcile.component';
import { SharedModule } from '../../../shared/shared.module';
import { ReconcileDiscrepancyComponent } from './discrepancy/reconcile-discrepancy.component';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { P2pCardReconcileModalComponent } from './summary/p2p/card/p2p-card-reconcile-modal.component';
import { RECONCILE_ROUTES } from './reconcile.route';
import { TerminalReconcileComponent } from './summary/terminal/terminal-reconcile.component';
import { P2pReconcileComponent } from './summary/p2p/p2p-reconcile.component';
import { PaymentReconcileComponent } from './summary/payment/payment-reconcile.component';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';

@NgModule({
  imports: [RouterModule.forChild(RECONCILE_ROUTES), SharedModule, NzTabsModule, NzPopconfirmModule],
  declarations: [
    ReconcileComponent,
    TerminalReconcileComponent,
    P2pReconcileComponent,
    PaymentReconcileComponent,
    ReconcileDiscrepancyComponent,
    P2pCardReconcileModalComponent,
  ],
})
export class ReconcileModule {}
