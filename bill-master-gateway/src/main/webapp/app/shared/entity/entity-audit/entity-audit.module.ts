import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DiffMatchPatchModule } from 'ng-diff-match-patch';

import { SharedModule } from 'app/shared/shared.module';
import { EntityAuditHistoryModalComponent } from './entity-audit-history-modal.component';

@NgModule({
  imports: [CommonModule, SharedModule, DiffMatchPatchModule],
  declarations: [EntityAuditHistoryModalComponent],
})
export class EntityAuditModule {}
