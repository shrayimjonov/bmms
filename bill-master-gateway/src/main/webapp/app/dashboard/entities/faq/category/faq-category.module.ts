import {NgModule} from '@angular/core';
import {SharedModule} from 'app/shared/shared.module';
import {FaqCategoryComponent} from './faq-category.component';
import {FaqCategoryUpdateComponent} from "./faq-category-update.component";
import {RouterModule, Routes} from "@angular/router";

const ROUTES: Routes = [
  {
    path: '',
    component: FaqCategoryComponent,
    data: {
      defaultSort: 'id, desc'
    }
  }
]

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [FaqCategoryUpdateComponent, FaqCategoryComponent],
  entryComponents: [],
})
export class FaqCategoryModule {}
