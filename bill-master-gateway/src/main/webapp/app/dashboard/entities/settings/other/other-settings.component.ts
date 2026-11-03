import {Component, OnDestroy, OnInit} from '@angular/core';
import {OtherSettingsService} from './other-settings.service';
import {Subject} from 'rxjs';
import {finalize, takeUntil} from 'rxjs/operators';
import {BmAlertService} from '../../../../shared/component/bm-alert/bm-alert.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ConfirmModalComponent} from './confirm-modal/confirm-modal.component';
import {CommonResult} from '../../../../shared/model/common-result.model';
import {I18n} from '../../../../shared/model/pkbillms/i18n.model';
import {LanguageService} from '../../../../core/util/language.service';
import {Job} from "../../../../shared/model/job.model";

@Component({
  templateUrl: 'other-settings.template.html',
})
export class OtherSettingsComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  jobs: Job[];
  response: any;
  param: Map<string, any>;
  loading: boolean;

  constructor(
    private readonly otherSettingsService: OtherSettingsService,
    private readonly modalService: NgbModal,
    private readonly langService: LanguageService,
    private readonly alertService: BmAlertService,
  ) {}

  ngOnInit(): void {
    this.fetchActions();
  }

  private fetchActions(): void {
    this.loading = true;
    this.otherSettingsService
      .getActions()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe((jobs: Job[]) => this.jobs = jobs);
  }

  executeAction(job: Job): void {
    if (!job?.uri) {
      return this.alertService.error(`Field 'url' cannot be empty in action`, null, true);
    }
    if (job.confirmable) {
      this.openConfirmModal(job.uriParams, job);
    } else {
      this.loading = true;
      this.otherSettingsService
        .executeAction(job.uriParams, job)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => this.loading = false)
        )
        .subscribe((res: CommonResult<I18n[]>) => {
          this.alertService.success(res.details[this.langService.getCurrent()], null, true);
        });
    }
  }

  private openConfirmModal(body: any, job: Job): void {
    const modalRef = this.modalService.open(ConfirmModalComponent);
    modalRef.componentInstance.job = job;
    modalRef.componentInstance.body = body;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
