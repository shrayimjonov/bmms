import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {RouterModule} from '@angular/router';
import {HomeWidgetService} from './home-widget.service';
import {HomeComponent} from './home.component';
import {NzRadioModule} from "ng-zorro-antd/radio";

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: '',
        component: HomeComponent
      }
    ]),
    SharedModule,
    NzRadioModule
  ],
  declarations: [
    HomeComponent,
  ],
  providers: [HomeWidgetService],
})
export class HomeModule {
}
