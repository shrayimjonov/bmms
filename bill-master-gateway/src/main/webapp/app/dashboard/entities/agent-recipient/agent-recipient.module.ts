import {NgModule} from '@angular/core';
import {AgentRecipientComponent} from './agent-recipient.component';
import {SharedModule} from '../../../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
import {AgentRecipientUpdateModalComponent} from './agent-recipient-update-modal.component';
import {NgJsonEditorModule} from 'ang-jsoneditor';

const ROUTES: Routes = [
  {
    path: '',
    component: AgentRecipientComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), NgJsonEditorModule],
  declarations: [AgentRecipientComponent, AgentRecipientUpdateModalComponent],
})
export class AgentRecipientModule {}
