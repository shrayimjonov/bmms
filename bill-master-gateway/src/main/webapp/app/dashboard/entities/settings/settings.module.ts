import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {PasswordComponent} from './password/password.component';
import {SETTINGS_ROUTE} from "./settings.route";
import {SharedModule} from "../../../shared/shared.module";
import {SettingsComponent} from "./settings.component";
import {NzTabsModule} from "ng-zorro-antd/tabs";
import {PasswordService} from "./password/password.service";
import {BmPasswordInputModule} from "../../../shared/component/password-input/bm-password-input.module";
import {NzAvatarModule} from "ng-zorro-antd/avatar";
import {OtherSettingsComponent} from "./other/other-settings.component";
import {ConfirmModalComponent} from "./other/confirm-modal/confirm-modal.component";
import {NzCollapseModule} from "ng-zorro-antd/collapse";
import {NzListModule} from "ng-zorro-antd/list";
import {MainSettingsComponent} from "./main/main-settings.component";

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(SETTINGS_ROUTE),
    NzTabsModule,
    BmPasswordInputModule,
    NzAvatarModule,
    NzCollapseModule,
    NzListModule
  ],
  declarations: [
    SettingsComponent,
    MainSettingsComponent,
    PasswordComponent,
    OtherSettingsComponent,
    ConfirmModalComponent
  ],
  providers: [PasswordService]
})
export class SettingsModule {
}
