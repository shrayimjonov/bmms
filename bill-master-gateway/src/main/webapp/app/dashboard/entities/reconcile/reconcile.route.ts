import {Routes} from "@angular/router";
import {ReconcileComponent} from "./reconcile.component";
import {ReconcileDiscrepancyComponent} from "./discrepancy/reconcile-discrepancy.component";
import {TerminalReconcileComponent} from "./summary/terminal/terminal-reconcile.component";
import {P2pReconcileComponent} from "./summary/p2p/p2p-reconcile.component";
import {PaymentReconcileComponent} from "./summary/payment/payment-reconcile.component";

export const RECONCILE_ROUTES: Routes = [
  {
    path: '',
    component: ReconcileComponent,
    children: [
      {
        path: '',
        redirectTo: 'payment',
        pathMatch: 'full'
      },
      {
        path: 'payment',
        component: PaymentReconcileComponent,
        data: {
          pageTitle: 'global.menu.entities.pkbillmsReconcileSummary',
          defaultSort: 'id,desc',
          noBreadcrumb: true
        }
      },
      {
        path: 'terminal',
        component: TerminalReconcileComponent,
        data: {
          pageTitle: 'global.menu.entities.reconcileTerminal',
          defaultSort: 'id,desc',
          noBreadcrumb: true
        }
      },
      {
        path: 'p2p',
        component: P2pReconcileComponent,
        data: {
          pageTitle: 'global.menu.entities.reconcileP2P',
          defaultSort: 'id,desc',
          noBreadcrumb: true
        }
      }
    ]
  },
  {
    path: 'discrepancy',
    component: ReconcileDiscrepancyComponent,
    data: {
      pageTitle: 'global.menu.entities.pkbillmsReconcileDiscrepancy',
      defaultSort: 'id,desc',
    },
  },
];
