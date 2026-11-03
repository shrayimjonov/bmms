import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { BmPasswordInputModule } from '../shared/component/password-input/bm-password-input.module';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'register',
    component: RegisterComponent,
    data: {
      pageTitle: 'register.title',
    },
  },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(routes), BmPasswordInputModule],
  declarations: [LoginComponent, RegisterComponent],
})
export class AuthorizationModule {}
