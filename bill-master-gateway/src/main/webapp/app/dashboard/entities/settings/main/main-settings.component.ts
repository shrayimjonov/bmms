import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';

import {AccountService} from 'app/core/auth/account.service';
import {Account} from '../../../../core/auth/account.model';
import {Subject} from 'rxjs';
import {BmAlertService} from '../../../../shared/component/bm-alert/bm-alert.service';
import {LanguageService} from '../../../../core/util/language.service';
import {finalize, takeUntil} from 'rxjs/operators';
import {Util} from '../../../../shared/util';
import {BmFile} from '../../../../shared/component/bm-file/bm-file.model';

@Component({
  templateUrl: 'main-settings.template.html',
})
export class MainSettingsComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private account: Account;

  loading: boolean;

  readonly languages = this.langService.getTranslatedLangKeys();
  readonly editForm = this.fb.group({
    firstName: [null, [Validators.required, Validators.minLength(1), Validators.maxLength(50)]],
    lastName: [null, [Validators.required, Validators.minLength(1), Validators.maxLength(50)]],
    phoneNumber: [null],
    email: [null, [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email]],
    langKey: [null],
    imageUrl: [null],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly langService: LanguageService,
    private readonly accountService: AccountService,
    private readonly alertService: BmAlertService
  ) {}

  ngOnInit(): void {
    this.fetchAccount();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private fetchAccount(): void {
    this.loading = true;
    this.accountService
      .identity()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe(account => {
        if (account) {
          this.editForm.patchValue({
            firstName: account.firstName,
            lastName: account.lastName,
            email: account.email,
            langKey: account.langKey,
            imageUrl: account.imageUrl,
          });

          this.account = account;
        }
      });
  }

  save(): void {
    this.account.firstName = this.editForm.get('firstName').value;
    this.account.lastName = this.editForm.get('lastName').value;
    this.account.email = this.editForm.get('email').value;
    this.account.langKey = this.editForm.get('langKey').value;
    this.account.imageUrl = this.editForm.get('imageUrl').value;

    this.loading = true;
    this.accountService
      .save(this.account)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe(() => this.onSaved());
  }

  onSelectImage(event: any): void {
    this.loading = true;
    Util.readAsDataUrl(event.target.files[0])
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe((file: BmFile | null) => this.setImage(file));
  }

  private setImage(file: BmFile | null): void {
    const imageUrl = this.editForm.get('imageUrl');

    if (!file) {
      imageUrl.setValue(null);
    } else {
      const content = `data:${file.contentType};base64,${file.content}`;
      imageUrl.setValue(content, { emitEvent: false });
    }
  }

  private onSaved(): void {
    this.alertService.success('settings.messages.success');
    const updatedLangKey = this.editForm.get('langKey').value;
    if (this.account.langKey !== updatedLangKey) {
      this.langService.change(updatedLangKey).pipe(takeUntil(this.destroy$)).subscribe();
    }
    this.accountService.authenticate(this.account);
  }
}
