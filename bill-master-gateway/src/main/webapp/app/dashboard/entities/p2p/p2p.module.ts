import { RouterModule } from '@angular/router';
import { SharedModule } from '../../../shared/shared.module';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { P2pComponent } from './p2p.component';
import { P2pCommissionComponent } from './commission/p2p-commission.component';
import { P2pConditionComponent } from './condition/p2p-condition.component';
import { P2pParamComponent } from './param/p2p-param.component';
import { NgModule } from '@angular/core';
import { P2pCommissionUpdateModalComponent } from './commission/p2p-commission-update-modal.component';
import { P2pConditionUpdateModalComponent } from './condition/p2p-condition-update-modal.component';
import { P2pParamUpdateModalComponent } from './param/p2p-param-update-modal.component';
import { NgJsonEditorModule } from 'ang-jsoneditor';
import { NglStickyScrollXModule } from 'ngl-sticky-scroll-x';
import { BmJsonViewerModule } from '../../../shared/component/bm-json-viewer/bm-json-viewer.module';
import { P2pParamTestModalComponent } from './param/test/p2p-param-test-modal.component';
import { P2pParamJsonViewModalComponent } from './param/json-view/p2p-param-json-view-modal.component';
import { P2P_ROUTES } from './p2p.route';
import { P2pAgentParamComponent } from './agent-param/p2p-agent-param.component';
import { P2PAgentParamUpdateComponent } from './agent-param/p2p-agent-param-update.component';
import { RegexTestComponent } from './agent-param/regext-test/regext-test.component';

@NgModule({
  imports: [RouterModule.forChild(P2P_ROUTES), SharedModule, NzTabsModule, NgJsonEditorModule, NglStickyScrollXModule, BmJsonViewerModule],
  declarations: [
    P2pComponent,
    P2pCommissionComponent,
    P2pCommissionUpdateModalComponent,
    P2pConditionComponent,
    P2pConditionUpdateModalComponent,
    P2pParamComponent,
    P2pParamUpdateModalComponent,
    P2pParamTestModalComponent,
    P2pParamJsonViewModalComponent,
    P2pAgentParamComponent,
    P2PAgentParamUpdateComponent,
    RegexTestComponent,
  ],
})
export class P2pModule {}
