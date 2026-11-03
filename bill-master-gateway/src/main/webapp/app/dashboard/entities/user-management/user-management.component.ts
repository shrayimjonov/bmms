import {Component} from '@angular/core';
import {Menu} from '../../dashboard-sidebar-menus';
import {Authority} from '../../../config/authority.constants';

@Component({
  templateUrl: 'user-management.template.html',
})
export class UserManagementComponent {
  readonly tabs: Menu[] = [
    {
      title: 'global.menu.admin.userManagement',
      routerLink: './user',
      icon: 'users',
      authorities: [Authority.USER_MANAGEMENT_READ, Authority.USER_MANAGEMENT_WRITE],
    },
    {
      title: 'global.menu.entities.pkuaaGroup',
      routerLink: './group',
      icon: 'usergroup-add',
      authorities: [Authority.USER_GROUP_READ],
    },
    {
      title: 'global.menu.entities.pkuaaAuthority',
      routerLink: './authority',
      icon: 'user-auth',
      authorities: [Authority.AUTHORITY_READ],
    },
  ];
}
