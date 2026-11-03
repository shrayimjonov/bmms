import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../../../shared/shared.module';
import { UserManagementComponent } from './user-management.component';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { UserComponent } from './user/user.component';
import { BmPasswordInputModule } from '../../../shared/component/password-input/bm-password-input.module';
import { UserUpdateModalComponent } from './user/user-update-modal.component';
import { UserGroupComponent } from './user-group/user-group.component';
import { AuthorityComponent } from './authority/authority.component';
import { UserGroupUpdateModalComponent } from './user-group/user-group-update-modal.component';
import { AuthorityUpdateModalComponent } from './authority/authority-update-modal.component';
import { USER_MANAGEMENT_ROUTES } from './user-management.route';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';

@NgModule({
  imports: [RouterModule.forChild(USER_MANAGEMENT_ROUTES), SharedModule, NzTabsModule, BmPasswordInputModule, NzPopconfirmModule],
  declarations: [
    UserManagementComponent,
    UserComponent,
    UserUpdateModalComponent,
    UserGroupComponent,
    UserGroupUpdateModalComponent,
    AuthorityComponent,
    AuthorityUpdateModalComponent,
  ],
})
export class UserManagementModule {}
