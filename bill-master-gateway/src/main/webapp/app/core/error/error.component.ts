import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Subject} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';
import {NzResultStatusType} from 'ng-zorro-antd/result/result.component';
import {finalize, takeUntil} from 'rxjs/operators';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'error',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <nz-result [style.padding-top]="'7rem'" [nzStatus]="status" [nzTitle]="title[status]"
               nzSubTitle="{{ description }}">
      <div nz-result-extra>
        <button nz-button nzType="primary" nzSize="large" jhiTranslate="entity.action.back" (click)="back()">Back
        </button>
      </div>
    </nz-result>
  `,
})
export class ErrorComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  description: string;
  status: NzResultStatusType;
  readonly title = {
    warning: 'Error',
    403: '403',
    404: '404',
    500: '500',
  };

  constructor(
    private readonly translateService: TranslateService,
    private readonly modalService: NgbModal,
    private readonly activatedRoute: ActivatedRoute,
    private readonly cdr: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.status = this.activatedRoute.snapshot.data.status || 'warning';
    this.getErrorMessageTranslation();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private getErrorMessageTranslation(): void {
    this.translateService
      .get('error.http.' + this.status)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.cdr.markForCheck())
      )
      .subscribe(message => (this.description = message));
  }

  back(): void {
    window.history.back();
  }
}
