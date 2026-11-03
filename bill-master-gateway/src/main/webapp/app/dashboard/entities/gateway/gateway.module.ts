import {NgModule} from '@angular/core';
import {GatewayComponent} from './gateway.component';
import {GatewayUpdateModalComponent} from './gateway-update-modal.component';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../../../shared/shared.module';

const ROUTES: Routes = [
  {
    path: '',
    component: GatewayComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [GatewayComponent, GatewayUpdateModalComponent],
})
export class GatewayModule {}
