import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {RouterModule, Routes} from '@angular/router';
import {GatewayParamComponent} from './gateway-param.component';
import {GatewayParamUpdateModalComponent} from './gateway-param-update-modal.component';
import {KeyValueModule} from '../../../shared/component/key-value/key-value.module';

const ROUTES: Routes = [
  {
    path: '',
    component: GatewayParamComponent,
    data: {
      defaultSort: 'id, asc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), KeyValueModule],
  declarations: [GatewayParamComponent, GatewayParamUpdateModalComponent],
})
export class GatewayParamModule {}
