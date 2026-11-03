import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
import {ResponseFieldComponent} from './response-field.component';
import {ResponseFieldUpdateModalComponent} from './response-field-update-modal.component';

const ROUTES: Routes = [
  {
    path: '',
    component: ResponseFieldComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [ResponseFieldComponent, ResponseFieldUpdateModalComponent],
})
export class ResponseFieldModule {}
