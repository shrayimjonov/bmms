import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {DashboardComponent} from './dashboard.component';
import {NzLayoutModule} from 'ng-zorro-antd/layout';
import {NzBreadCrumbModule} from 'ng-zorro-antd/breadcrumb';
import {NzMenuModule} from 'ng-zorro-antd/menu';
import {SharedModule} from '../shared/shared.module';
import {DASHBOARD_ROUTES} from './dashboard.route';
import {NzDropDownModule} from 'ng-zorro-antd/dropdown';
import {NzAvatarModule} from 'ng-zorro-antd/avatar';
import {EntityAuditModule} from '../shared/entity/entity-audit/entity-audit.module';
import {DashboardHeaderComponent} from "./header/dashboard-header.component";
import {DashboardBreadcrumbComponent} from "./breadcrumb/dashboard-breadcrumb.component";

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(DASHBOARD_ROUTES),
    NzLayoutModule,
    NzBreadCrumbModule,
    NzMenuModule,
    NzDropDownModule,
    NzAvatarModule,
    EntityAuditModule,
  ],
  declarations: [
    DashboardComponent,
    DashboardHeaderComponent,
    DashboardBreadcrumbComponent
  ]
})
export class DashboardModule {
}
