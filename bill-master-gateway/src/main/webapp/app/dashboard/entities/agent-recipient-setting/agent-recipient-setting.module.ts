import { NgModule } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { RouterModule, Routes } from '@angular/router';
import { AgentRecipientSettingComponent } from './agent-recipient-setting.component';
import { AgentRecipientSettingUpdateModalComponent } from './agent-recipient-setting-update-modal.component';
import { PropFormComponent } from './prop-form/prop-form.component';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { EposExpressionCheckerModalComponent } from './prop-form/epos-expression-checker-modal/epos-expression-checker-modal.component';
import { KeyValueModule } from '../../../shared/component/key-value/key-value.module';

const ROUTES: Routes = [
  {
    path: '',
    component: AgentRecipientSettingComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), NzSelectModule, KeyValueModule],
  declarations: [
    AgentRecipientSettingComponent,
    AgentRecipientSettingUpdateModalComponent,
    PropFormComponent,
    EposExpressionCheckerModalComponent,
  ],
})
export class AgentRecipientSettingModule {}
