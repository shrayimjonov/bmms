import {Routes} from '@angular/router';
import {PasswordComponent} from "./password/password.component";
import {MainSettingsComponent} from "./main/main-settings.component";
import {SettingsComponent} from "./settings.component";
import {OtherSettingsComponent} from "./other/other-settings.component";

export const SETTINGS_ROUTE: Routes = [
  {
    path: '',
    component: SettingsComponent,
    data: {
      pageTitle: 'global.menu.account.settings'
    },
    children: [
      {
        path: '',
        redirectTo: 'main',
        pathMatch: 'full'
      },
      {
        path: 'main',
        component: MainSettingsComponent,
        data: {
          pageTitle: 'global.menu.account.mainSettings'
        },
      },
      {
        path: 'password',
        component: PasswordComponent,
        data: {
          pageTitle: 'global.menu.account.password',
        },
      },
      {
        path: 'other',
        component: OtherSettingsComponent,
        data: {
          pageTitle: 'global.menu.account.otherSettings',
        },
      }
    ]
  }
];
