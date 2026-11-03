import {NgModule} from "@angular/core";
import {BmTableImageViewerComponent} from "./bm-table-image-viewer.component";
import {NzButtonModule} from "ng-zorro-antd/button";
import {CommonModule} from "@angular/common";
import {NzIconModule} from "ng-zorro-antd/icon";
import {BmFileComponent} from "./bm-file.component";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
  imports: [CommonModule, NzButtonModule, NzIconModule, TranslateModule],
  declarations: [BmFileComponent, BmTableImageViewerComponent],
  exports: [BmFileComponent, BmTableImageViewerComponent]
})
export class BmFileModule {

}
