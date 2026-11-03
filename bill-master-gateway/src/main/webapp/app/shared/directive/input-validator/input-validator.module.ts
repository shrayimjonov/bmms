import {NgModule} from "@angular/core";
import {InputValidatorDirective} from "./input-validator.directive";
import {InputValidatorButtonDirective} from "./input-validator-button.directive";

@NgModule({
  declarations: [InputValidatorDirective, InputValidatorButtonDirective],
  exports: [InputValidatorDirective, InputValidatorButtonDirective]
})
export class InputValidatorModule {

}
