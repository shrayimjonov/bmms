import {NgModule} from "@angular/core";
import {KeyValueComponent} from "./key-value.component";
import {SharedModule} from "../../shared.module";

@NgModule({
  imports: [SharedModule],
  exports: [
    KeyValueComponent
  ],
  declarations: [KeyValueComponent]
})
export class KeyValueModule {
}
