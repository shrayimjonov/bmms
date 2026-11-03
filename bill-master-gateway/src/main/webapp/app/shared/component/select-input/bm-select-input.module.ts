import {NgModule} from "@angular/core";
import {BmSelectInputComponent} from "./bm-select-input.component";
import {NzSelectModule} from "ng-zorro-antd/select";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {NzIconModule} from "ng-zorro-antd/icon";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
  imports: [CommonModule, FormsModule, NzSelectModule, NzIconModule, TranslateModule],
  declarations: [BmSelectInputComponent],
  exports: [BmSelectInputComponent]
})
export class BmSelectInputModule {
}
