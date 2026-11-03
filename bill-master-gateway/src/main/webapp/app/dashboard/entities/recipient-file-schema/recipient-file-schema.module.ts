import {NgModule} from '@angular/core';
import {SharedModule} from 'app/shared/shared.module';
import {RecipientFileSchemaComponent} from './recipient-file-schema.component';
import {RecipientFileSchemaUpdateComponent} from './recipient-file-schema-update.component';
import {RouterModule, Routes} from '@angular/router';

const ROUTES: Routes = [
  {
    path: '',
    component: RecipientFileSchemaComponent,
    data: {
      defaultSort: 'id,desc',
      pageTitle: 'global.menu.entities.bmmsRecipientFileSchema',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(ROUTES)],
  declarations: [RecipientFileSchemaComponent, RecipientFileSchemaUpdateComponent],
  entryComponents: [],
})
export class RecipientFileSchemaModule {}
