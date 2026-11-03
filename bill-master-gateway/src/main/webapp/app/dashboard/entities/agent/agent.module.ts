import {NgModule} from '@angular/core';
import {AgentComponent} from './agent.component';
import {AgentUpdateModalComponent} from './agent-update-modal.component';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../../../shared/shared.module';
import {BmPasswordInputModule} from '../../../shared/component/password-input/bm-password-input.module';

const ROUTES: Routes = [
  {
    path: '',
    component: AgentComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(ROUTES),
    BmPasswordInputModule,
  ],
  declarations: [AgentComponent, AgentUpdateModalComponent],
})
export class AgentModule {}
