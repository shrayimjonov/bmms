import {NgModule} from '@angular/core';
import {RecipientGroupComponent} from './recipient-group.component';
import {SharedModule} from '../../../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
import {RecipientGroupUpdateModalComponent} from './recipient-group-update-modal.component';

const ROUTES: Routes = [
  {
    path: '',
    component: RecipientGroupComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [RecipientGroupComponent, RecipientGroupUpdateModalComponent],
})
export class RecipientGroupModule {}
