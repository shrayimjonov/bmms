import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HumoBinComponent} from './humo-bin.component';
import {SharedModule} from '../../../shared/shared.module';
import {HumoBinUpdateModalComponent} from './humo-bin-update-modal.component';

const ROUTES: Routes = [
  {
    path: '',
    component: HumoBinComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
    imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [HumoBinComponent, HumoBinUpdateModalComponent],
})
export class HumoBinModule {}
