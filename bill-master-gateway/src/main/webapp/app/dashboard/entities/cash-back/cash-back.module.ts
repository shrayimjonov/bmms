import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { CashBackComponent } from './list/cash-back.component';
import { CashBackDetailComponent } from './detail/cash-back-detail.component';
import { CashBackUpdateComponent } from './update/cash-back-update.component';
import { CashBackRoutingModule } from './route/cash-back-routing.module';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NgJsonEditorModule } from 'ang-jsoneditor';

@NgModule({
  imports: [SharedModule, NzDropDownModule, NzTagModule, CashBackRoutingModule, NgJsonEditorModule],
  declarations: [CashBackComponent, CashBackDetailComponent, CashBackUpdateComponent],
})
export class CashBackModule {}
