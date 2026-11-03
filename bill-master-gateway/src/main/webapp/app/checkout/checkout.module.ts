import { NgModule } from '@angular/core';
import { SharedModule } from '../shared/shared.module';
import { CheckoutComponent } from './checkout.component';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { ImageLoaderModule } from '../shared/directive/image-loader/image-loader.module';
import { RouterModule, Routes } from '@angular/router';
import { CheckoutResolver } from './checkout.resolver';
import { CheckoutUnauthorizedComponent } from './unauthorized/checkout-unauthorized.component';
import { CheckoutAgentComponent } from './agent/checkout-agent.component';
import { CheckoutSuccessComponent } from './success/checkout-success.component';

const routes: Routes = [
  {
    path: '',
    component: CheckoutComponent,
    data: {
      pageTitle: 'global.paymentCheckout',
    },
    resolve: {
      checkout: CheckoutResolver,
    },
  },
  {
    path: 'success',
    component: CheckoutSuccessComponent,
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(routes), NzDrawerModule, ImageLoaderModule],
  declarations: [CheckoutComponent, CheckoutUnauthorizedComponent, CheckoutAgentComponent, CheckoutSuccessComponent],
  providers: [CheckoutResolver],
})
export class CheckoutModule {}
