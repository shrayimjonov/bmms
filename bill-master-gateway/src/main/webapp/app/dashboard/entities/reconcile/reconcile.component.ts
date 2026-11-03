import {Component} from '@angular/core';
import {Menu} from '../../dashboard-sidebar-menus';
import {Authority} from '../../../config/authority.constants';

@Component({
  templateUrl: 'reconcile.template.html',
})
export class ReconcileComponent {
  readonly tabs: Menu[] = [
    {
      title: 'global.menu.reconcile.PAYMENT',
      routerLink: './payment',
      icon: 'wallet',
      authorities: [Authority.RECONCILE_SUMMARY_READ],
    },
    {
      title: 'global.menu.reconcile.TERMINAL',
      routerLink: './terminal',
      icon: 'inbox',
      authorities: [Authority.RECONCILE_TERMINAL_READ],
    },
    {
      title: 'global.menu.reconcile.P2P',
      routerLink: './p2p',
      icon: 'p2p',
      authorities: [Authority.RECONCILE_P2P_READ, Authority.RECONCILE_P2P_WRITE],
    },
  ];
}
