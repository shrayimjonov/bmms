import {NgModule} from '@angular/core';
import {CategoryComponent} from './category.component';
import {CategoryUpdateModalComponent} from './category-update-modal.component';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../../../shared/shared.module';

const ROUTES: Routes = [
  {
    path: '',
    component: CategoryComponent,
    data: {
      defaultSort: 'id,desc',
    },
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(ROUTES),
    SharedModule
  ],
  declarations: [
    CategoryComponent,
    CategoryUpdateModalComponent
  ],
})
export class CategoryModule {
}
