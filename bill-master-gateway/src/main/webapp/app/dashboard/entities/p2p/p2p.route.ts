import { Routes } from '@angular/router';
import { P2pComponent } from './p2p.component';
import { P2pCommissionComponent } from './commission/p2p-commission.component';
import { Authority } from '../../../config/authority.constants';
import { P2pConditionComponent } from './condition/p2p-condition.component';
import { P2pParamComponent } from './param/p2p-param.component';
import { P2pAgentParamComponent } from './agent-param/p2p-agent-param.component';

export const P2P_ROUTES: Routes = [
  {
    path: '',
    component: P2pComponent,
    children: [
      {
        path: '',
        redirectTo: 'commission',
        pathMatch: 'full',
      },
      {
        path: 'commission',
        component: P2pCommissionComponent,
        data: {
          authorities: [Authority.P2P_COMMISSION_READ, Authority.P2P_COMMISSION_WRITE],
          pageTitle: 'billMasterGwApp.pkbillmsP2PCommission.home.title',
          defaultSort: 'id,desc',
        },
      },
      {
        path: 'condition',
        component: P2pConditionComponent,
        data: {
          authorities: [Authority.P2P_CONDITION_READ, Authority.P2P_CONDITION_WRITE],
          pageTitle: 'billMasterGwApp.pkbillmsP2PCondition.home.title',
          defaultSort: 'id,desc',
        },
      },
      {
        path: 'param',
        component: P2pParamComponent,
        data: {
          authorities: [Authority.P2P_PARAM_READ, Authority.P2P_PARAM_WRITE],
          pageTitle: 'billMasterGwApp.pkbillmsP2PParam.home.title',
          defaultSort: 'id,desc',
        },
      },
      {
        path: 'agent-param',
        component: P2pAgentParamComponent,
        data: {
          authorities: [Authority.P2P_AGENT_PARAM_READ],
          pageTitle: 'billMasterGwApp.pkbillmsP2PAgentParam.home.title',
          defaultSort: 'id,desc',
        },
      },
    ],
  },
];
