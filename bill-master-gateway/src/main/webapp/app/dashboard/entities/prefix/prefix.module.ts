import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
import {PrefixComponent} from './prefix.component';
import {PrefixUpdateModalComponent} from './prefix-update-modal.component';
import {NzSelectModule} from 'ng-zorro-antd/select';

const ROUTES: Routes = [
  {
    path: '',
    component: PrefixComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), NzSelectModule],
  declarations: [PrefixComponent, PrefixUpdateModalComponent],
})
export class PrefixModule {}
