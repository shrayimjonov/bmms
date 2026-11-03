import {NgModule} from "@angular/core";
import {BmI18nInputComponent} from "./bm-i18n-input.component";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {NzInputModule} from "ng-zorro-antd/input";
import {BmI18nDirective} from "./bm-i18n.directive";
import {InputValidatorModule} from "../../directive/input-validator/input-validator.module";
import {BmI18nTabComponent} from "./bm-18n-tabs/bm-i18n-tab.component";
import {NzTabsModule} from "ng-zorro-antd/tabs";

@NgModule({
  imports: [CommonModule, FormsModule, NzInputModule, InputValidatorModule, NzTabsModule],
  declarations: [BmI18nInputComponent, BmI18nDirective, BmI18nTabComponent],
  exports: [BmI18nInputComponent, BmI18nDirective, BmI18nTabComponent]
})
export class BmI18nModule {
}
