import {NgModule} from '@angular/core';
import {SharedModule} from '../../../shared/shared.module';
import {ReportComponent} from './report.component';
import {RouterModule, Routes} from '@angular/router';
import {ReportUpdateModalComponent} from './report-update-modal.component';
import {BmFileModule} from "../../../shared/component/bm-file/bm-file.module";

const ROUTES: Routes = [
  {
    path: '',
    component: ReportComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(ROUTES),
    SharedModule,
    BmFileModule
  ],
  declarations: [
    ReportComponent,
    ReportUpdateModalComponent
  ],
})
export class ReportModule {
}
