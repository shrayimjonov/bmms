import { Component, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { OtherSettingsService } from '../other-settings.service';
import { finalize, takeUntil } from 'rxjs/operators';
import { CommonResult } from '../../../../../shared/model/common-result.model';
import { I18n } from '../../../../../shared/model/pkbillms/i18n.model';
import { BmAlertService } from '../../../../../shared/component/bm-alert/bm-alert.service';
import { LanguageService } from '../../../../../core/util/language.service';
import { Job } from '../../../../../shared/model/job.model';
import { BmI18nService } from '../../../../../shared/component/bm-i18n-input/bm-i18n.service';

@Component({
  templateUrl: 'confirm-modal.template.html',
})
export class ConfirmModalComponent implements OnDestroy {
  private readonly destroy$ = new Subject<void>();

  job: Job;
  body: any;
  loading: boolean;

  constructor(
    private activeModal: NgbActiveModal,
    private service: OtherSettingsService,
    private readonly alertService: BmAlertService,
    private readonly langService: LanguageService,
    private readonly i18nService: BmI18nService
  ) {}

  executeAction(): void {
    this.loading = true;
    this.service
      .executeAction(this.body, this.job)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe((result: CommonResult<I18n[]>) => {
        if (result.success) {
          const message = this.i18nService.getText(result.data);
          this.alertService.success(message, null, true);
          this.close();
        } else {
          this.alertService.error('Error on process execution', null);
        }
      });
  }

  close(): void {
    this.activeModal.dismiss();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
