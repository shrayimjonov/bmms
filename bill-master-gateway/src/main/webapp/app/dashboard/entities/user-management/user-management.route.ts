import {Routes} from "@angular/router";
import {UserManagementComponent} from "./user-management.component";
import {UserComponent} from "./user/user.component";
import {UserGroupComponent} from "./user-group/user-group.component";
import {AuthorityComponent} from "./authority/authority.component";

export const USER_MANAGEMENT_ROUTES: Routes = [
  {
    path: '',
    component: UserManagementComponent,
    children: [
      {
        path: '',
        redirectTo: 'user',
        pathMatch: 'full'
      },
      {
        path: 'user',
        component: UserComponent,
        data: {
          pageTitle: 'global.menu.admin.userManagement',
          defaultSort: 'id,desc'
        }
      },
      {
        path: 'group',
        component: UserGroupComponent,
        data: {
          pageTitle: 'global.menu.entities.pkuaaGroup',
          defaultSort: 'id,desc'
        }
      },
      {
        path: 'authority',
        component: AuthorityComponent,
        data: {
          pageTitle: 'global.menu.entities.pkuaaAuthority',
          defaultSort: 'id,desc'
        }
      }
    ]
  }
];
