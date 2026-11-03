import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RecipientComponent } from './recipient.component';
import { RecipientUpdateModalComponent } from './recipient-update-modal.component';
import { SharedModule } from '../../../shared/shared.module';
import { BmFileModule } from '../../../shared/component/bm-file/bm-file.module';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NgJsonEditorModule } from 'ang-jsoneditor';

const ROUTES: Routes = [
  {
    path: '',
    component: RecipientComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), BmFileModule, NzSelectModule, NgJsonEditorModule],
  declarations: [RecipientComponent, RecipientUpdateModalComponent],
})
export class RecipientModule {}
