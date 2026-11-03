import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {HelpComponent} from "./help.component";
import {NzLayoutModule} from "ng-zorro-antd/layout";
import {SharedModule} from "../../../shared/shared.module";
import {NzMenuModule} from "ng-zorro-antd/menu";
import {VideoUrlSanitizerPipe} from "../../../shared/component/sanitizer/video-url-sanitizer.pipe";
import {NgxEditorModule} from "ngx-editor";

const routes: Routes = [
  {
    path: '',
    component: HelpComponent
  }
];

@NgModule({
    imports: [
        RouterModule.forChild(routes),
        SharedModule,
        NzLayoutModule,
        NzMenuModule,
        NgxEditorModule
    ],
  declarations: [
    HelpComponent,
    VideoUrlSanitizerPipe
  ]
})
export class HelpModule {
}
