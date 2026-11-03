import {NgModule} from '@angular/core';
import {BookkeepingTemplateComponent} from './bookkeeping-template.component';
import {BookkeepingTemplateUpdateModalComponent} from './bookkeeping-template-update-modal.component';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../../../shared/shared.module';
import {NglStickyScrollXModule} from "ngl-sticky-scroll-x";

const ROUTES: Routes = [
  {
    path: '',
    component: BookkeepingTemplateComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES), NglStickyScrollXModule],
  declarations: [BookkeepingTemplateComponent, BookkeepingTemplateUpdateModalComponent],
})
export class BookkeepingTemplateModule {}
