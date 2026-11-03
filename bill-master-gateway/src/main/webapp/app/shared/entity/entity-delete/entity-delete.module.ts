import { NgModule } from '@angular/core';
import { EntityDeleteModalComponent } from './entity-delete-modal.component';
import { SharedModule } from '../../shared.module';

@NgModule({
  imports: [SharedModule],
  declarations: [EntityDeleteModalComponent],
  exports: [EntityDeleteModalComponent],
})
export class EntityDeleteModule {}
