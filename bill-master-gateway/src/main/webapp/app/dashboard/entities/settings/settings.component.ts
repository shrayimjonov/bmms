import {Component} from "@angular/core";
import {Menu} from "../../dashboard-sidebar-menus";

@Component({
  templateUrl: 'settings.template.html',
})
export class SettingsComponent {
  readonly tabs: Menu[] = [
    {
      title: 'global.menu.account.mainSettings',
      routerLink: './main',
      icon: 'solution'
    },
    {
      title: 'global.menu.account.changePassword',
      routerLink: './password',
      icon: 'lock'
    },
    {
      title: 'global.menu.account.otherSettings',
      routerLink: './other',
      icon: 'control'
    }
  ]
}
