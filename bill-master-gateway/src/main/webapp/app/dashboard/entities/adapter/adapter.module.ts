import { NgModule } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { AdapterComponent } from './adapter.component';
import { RouterModule, Routes } from '@angular/router';
import { AdapterService } from './adapter.service';
import { AdapterHistoryModalComponent } from './history/adapter-history-modal.component';
import { AdapterFileUploadModalComponent } from './file-upload/adapter-file-upload-modal.component';
import { BmFileModule } from '../../../shared/component/bm-file/bm-file.module';
import { NzListModule } from 'ng-zorro-antd/list';
import { FilterByPipe } from './filter-by.pipe';

const ROUTES: Routes = [
  {
    path: '',
    component: AdapterComponent,
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), NzListModule, BmFileModule],
  declarations: [AdapterComponent, AdapterHistoryModalComponent, AdapterFileUploadModalComponent, FilterByPipe],
  providers: [AdapterService],
})
export class AdapterModule {}
