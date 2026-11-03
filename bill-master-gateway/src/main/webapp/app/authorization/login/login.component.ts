import { Component, OnDestroy } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { Subscription } from 'rxjs/internal/Subscription';

@Component({
  templateUrl: 'login.component.html',
  styles: [
    `
      .login__container {
        width: 360px;
        margin-top: 4rem;
        padding: 2.2rem 1.85rem;
        background-color: white;
      }
    `,
  ],
})
export class LoginComponent implements OnDestroy {
  private subscription?: Subscription;

  loading?: boolean;

  editForm = this.fb.group({
    username: [null, Validators.required],
    password: [null, Validators.required],
    rememberMe: [false],
  });

  constructor(private router: Router, private fb: FormBuilder) {}

  checkout(): void {
    const params = {
      account: '30',
      orderId: 1,
      amount: 15000,
      recipientId: 1401,
    };
    this.router.navigate(['checkout'], { queryParams: params });
  }

  login(): void {
    // const request: Login = {
    //   username: this.editForm.get('username')!.value,
    //   password: this.editForm.get('password')!.value,
    //   rememberMe: this.editForm.get('rememberMe')!.value,
    // };
    // this.loading = true;
    // this.subscription = this.loginService
    //   .login(request)
    //   .pipe(finalize(() => (this.loading = false)))
    //   .subscribe(() => {
    //     if (!this.router.getCurrentNavigation()) {
    //       // There were no routing during login (eg from navigationToStoredUrl)
    //       this.router.navigate(['dashboard']);
    //     }
    //   });
  }

  requestResetPassword(): void {
    this.router.navigate(['/account/reset', 'request']);
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
