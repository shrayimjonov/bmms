import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../../../shared/shared.module';
import {FieldUpdateModalComponent} from './field-update-modal.component';
import {FieldComponent} from './field.component';
import {NzSelectModule} from 'ng-zorro-antd/select';

const ROUTES: Routes = [
  {
    path: '',
    component: FieldComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(ROUTES), SharedModule, NzSelectModule],
  declarations: [FieldComponent, FieldUpdateModalComponent],
})
export class FieldModule {}
