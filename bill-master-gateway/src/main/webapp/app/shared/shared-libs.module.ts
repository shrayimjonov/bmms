import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {NzInputNumberModule} from 'ng-zorro-antd/input-number';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {NzCheckboxModule} from 'ng-zorro-antd/checkbox';
import {NzButtonModule} from 'ng-zorro-antd/button';
import {NzInputModule} from 'ng-zorro-antd/input';
import {NzTableModule} from 'ng-zorro-antd/table';
import {NzDatePickerModule} from 'ng-zorro-antd/date-picker';
import {BmSelectInputModule} from './component/select-input/bm-select-input.module';
import {InputValidatorModule} from './directive/input-validator/input-validator.module';
import {NzResultModule} from 'ng-zorro-antd/result';
import {NglFilterFieldModule} from 'ngl-filter-field';
import {NzPaginationModule} from 'ng-zorro-antd/pagination';
import {BmI18nModule} from './component/bm-i18n-input/bm-i18n.module';
import {BmAmountModule} from './component/bm-amount/bm-amount.module';
import {NzToolTipModule} from 'ng-zorro-antd/tooltip';
import {NglExportAsModule} from 'ngl-export-as';
import {NzSwitchModule} from 'ng-zorro-antd/switch';
import {NzSpinModule} from 'ng-zorro-antd/spin';
import {NzBadgeModule} from 'ng-zorro-antd/badge';
import {NzAlertModule} from 'ng-zorro-antd/alert';
import {BmTableWidgetModule} from "./component/bm-table-widget/bm-table-widget.module";

@NgModule({
  exports: [
    FormsModule,
    CommonModule,
    ReactiveFormsModule,
    TranslateModule,
    NzInputModule,
    NzInputNumberModule,
    NzButtonModule,
    NzCheckboxModule,
    NzIconModule,
    NzTableModule,
    NzDatePickerModule,
    NzResultModule,
    NzToolTipModule,
    NzPaginationModule,
    NzSwitchModule,
    NzSpinModule,
    NzBadgeModule,
    NzAlertModule,
    InputValidatorModule,
    NglExportAsModule,
    NglFilterFieldModule,
    BmAmountModule,
    BmSelectInputModule,
    BmI18nModule,
    BmTableWidgetModule
  ],
})
export class SharedLibsModule {}
