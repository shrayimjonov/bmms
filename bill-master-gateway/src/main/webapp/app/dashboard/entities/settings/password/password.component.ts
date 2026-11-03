import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {Subject} from 'rxjs';

import {PasswordService} from './password.service';
import {BmAlertService} from '../../../../shared/component/bm-alert/bm-alert.service';
import {finalize, pluck, takeUntil} from 'rxjs/operators';
import {AccountService} from "../../../../core/auth/account.service";

@Component({
  templateUrl: 'password.template.html',
})
export class PasswordComponent implements OnInit, OnDestroy {

  private readonly destroy$ = new Subject<void>();

  loading: boolean;
  login: string;

  readonly editForm = this.fb.group({
    currentPassword: [null, Validators.required],
    newPassword: [null, [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    confirmPassword: [null, [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly passwordService: PasswordService,
    private readonly accountService: AccountService,
    private readonly alertService: BmAlertService
  ) {}

  ngOnInit(): void {
    this.fetchAccountLogin();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  save(): void {
    const newPassword = this.editForm.get('newPassword').value;

    if (newPassword !== this.editForm.get('confirmPassword').value) {
      this.alertService.error('global.messages.error.dontmatch');
    } else {
      this.change(newPassword);
    }
  }

  private fetchAccountLogin(): void {
    this.accountService.identity()
      .pipe(
        pluck('login'),
        takeUntil(this.destroy$),
      )
      .subscribe(login => this.login = login);
  }

  private change(newPassword: string): void {
    this.loading = true;
    this.passwordService
      .save(newPassword, this.login)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe(() => this.onChanged());
  }

  private onChanged(): void {
    this.alertService.success('password.messages.success');
  }
}
