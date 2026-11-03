import { Component } from '@angular/core';
import { Menu } from '../../dashboard-sidebar-menus';
import { Authority } from '../../../config/authority.constants';

@Component({
  templateUrl: 'p2p.template.html',
})
export class P2pComponent {
  readonly tabs: Menu[] = [
    {
      title: 'global.menu.entities.pkbillmsP2PCommission',
      routerLink: './commission',
      authorities: [Authority.P2P_COMMISSION_READ, Authority.P2P_COMMISSION_WRITE],
    },
    {
      title: 'global.menu.entities.pkbillmsP2PCondition',
      routerLink: './condition',
      authorities: [Authority.P2P_CONDITION_READ, Authority.P2P_CONDITION_WRITE],
    },
    {
      title: 'global.menu.entities.pkbillmsP2PParam',
      routerLink: './param',
      authorities: [Authority.P2P_PARAM_READ, Authority.P2P_PARAM_WRITE],
    },
    {
      title: 'global.menu.entities.pkbillmsP2PAgentParam',
      routerLink: './agent-param',
      authorities: [Authority.P2P_PARAM_READ],
    },
  ];
}
