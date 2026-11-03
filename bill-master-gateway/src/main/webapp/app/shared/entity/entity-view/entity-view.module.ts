import { NgModule } from '@angular/core';
import { EntityViewModalComponent } from './entity-view-modal.component';
import { SharedModule } from '../../shared.module';
import { NzListModule } from 'ng-zorro-antd/list';
import { RouterModule } from '@angular/router';
import { ElementResolverDirective } from '../../directive/element-resolver.directive';
import { BmFileModule } from '../../component/bm-file/bm-file.module';
import { BmJsonViewerModule } from '../../component/bm-json-viewer/bm-json-viewer.module';
import { DatePipe } from '@angular/common';

@NgModule({
  imports: [SharedModule, NzListModule, RouterModule, BmFileModule, BmJsonViewerModule],
  declarations: [EntityViewModalComponent, ElementResolverDirective],
  exports: [EntityViewModalComponent, ElementResolverDirective],
  providers: [DatePipe],
})
export class EntityViewModule {}
