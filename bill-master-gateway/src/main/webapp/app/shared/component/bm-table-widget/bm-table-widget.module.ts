import {NgModule} from "@angular/core";
import {BmTableWidgetComponent} from "./bm-table-widget.component";
import {NzRadioModule} from "ng-zorro-antd/radio";
import {FormsModule} from "@angular/forms";
import {NzIconModule} from "ng-zorro-antd/icon";
import {TranslateModule} from "@ngx-translate/core";
import {NzSelectModule} from "ng-zorro-antd/select";
import {NzInputModule} from "ng-zorro-antd/input";
import {NzButtonModule} from "ng-zorro-antd/button";
import {CommonModule} from "@angular/common";

@NgModule({
  imports: [
    FormsModule,
    NzRadioModule,
    NzIconModule,
    TranslateModule,
    NzSelectModule,
    NzInputModule,
    NzButtonModule,
    CommonModule
  ],
  exports: [
    BmTableWidgetComponent
  ],
  declarations: [
    BmTableWidgetComponent
  ]
})
export class BmTableWidgetModule {

}
