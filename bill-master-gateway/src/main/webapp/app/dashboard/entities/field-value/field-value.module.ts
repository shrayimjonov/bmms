import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FieldValueComponent} from './field-value.component';
import {FieldValueUpdateModalComponent} from './field-value-update-modal.component';
import {SharedModule} from '../../../shared/shared.module';
import {NzSelectModule} from 'ng-zorro-antd/select';

const ROUTES: Routes = [
  {
    path: '',
    component: FieldValueComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), NzSelectModule],
  declarations: [FieldValueComponent, FieldValueUpdateModalComponent],
})
export class FieldValueModule {}
