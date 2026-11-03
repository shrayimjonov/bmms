import {NgModule} from '@angular/core';
import {SharedModule} from 'app/shared/shared.module';
import {FaqComponent} from "./faq.component";
import {RouterModule, Routes} from "@angular/router";
import {FaqUpdateModalComponent} from "./faq-update-modal.component";
import {NgxEditorModule} from 'ngx-editor';
import {NzMenuModule} from "ng-zorro-antd/menu";
import {NzLayoutModule} from "ng-zorro-antd/layout";
import {NzSelectModule} from "ng-zorro-antd/select";
import {NzTabsModule} from "ng-zorro-antd/tabs";

const ROUTES: Routes = [
  {
    path: '',
    component: FaqComponent,
    data: {
      defaultSort: 'id, desc'
    }
  }
]

@NgModule({
  imports: [SharedModule,
    RouterModule.forChild(ROUTES),
    NzMenuModule,
    NzLayoutModule,
    NzSelectModule,
    NgxEditorModule,
    NzTabsModule,
  ],
  declarations: [FaqUpdateModalComponent, FaqComponent],
  entryComponents: [],
})
export class FaqModule {}
