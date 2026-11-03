import {NgModule} from '@angular/core';

import {SharedLibsModule} from './shared-libs.module';
import {FindLanguageFromKeyPipe} from './language/find-language-from-key.pipe';
import {TranslateDirective} from './language/translate.directive';
import {HasAnyAuthorityDirective} from './directive/has-any-authority/has-any-authority.directive';
import {SortByDirective} from './sort/sort-by.directive';
import {SortDirective} from './sort/sort.directive';
import {AutoFocusDirective} from './directive/auto-focus.directive';
import {BmBooleanBadgeDirective} from './directive/bm-boolean-badge.directive';
import {DisableControlDirective} from './directive/disable-control.directive';
import {NgxMaskModule} from 'ngx-mask';
import {BmDatePipe} from './pipe/bm-date.pipe';
import {BmEllipsisPipe} from './component/ellipsis/bm-ellipsis';
import {BmAlertComponent} from './component/bm-alert/bm-alert.component';
import {NzMessageModule} from 'ng-zorro-antd/message';
import {SortIconComponent} from "./sort/sort-icon.component";

@NgModule({
  imports: [SharedLibsModule, NzMessageModule, NgxMaskModule.forRoot()],
  declarations: [
    FindLanguageFromKeyPipe,
    TranslateDirective,
    HasAnyAuthorityDirective,
    SortByDirective,
    SortDirective,
    BmBooleanBadgeDirective,
    AutoFocusDirective,
    DisableControlDirective,
    BmDatePipe,
    BmEllipsisPipe,
    BmAlertComponent,
    SortIconComponent
  ],
  exports: [
    SharedLibsModule,
    FindLanguageFromKeyPipe,
    TranslateDirective,
    HasAnyAuthorityDirective,
    SortByDirective,
    SortDirective,
    BmBooleanBadgeDirective,
    AutoFocusDirective,
    DisableControlDirective,
    NgxMaskModule,
    BmDatePipe,
    BmEllipsisPipe,
    BmAlertComponent,
    SortIconComponent
  ],
})
export class SharedModule {}
