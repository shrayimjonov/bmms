import {NgModule} from "@angular/core";
import {SharedModule} from "../../shared.module";
import {BmPasswordInputComponent} from "./bm-password-input.component";

@NgModule({
  imports: [SharedModule],
  declarations: [BmPasswordInputComponent],
  exports: [BmPasswordInputComponent]
})
export class BmPasswordInputModule {
}
