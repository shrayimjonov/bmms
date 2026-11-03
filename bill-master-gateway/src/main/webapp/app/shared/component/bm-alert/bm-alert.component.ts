import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { BmAlertService } from 'app/shared/component/bm-alert/bm-alert.service';
import { BmAlert } from 'app/shared/component/bm-alert/bm-alert.model';
import { Subscription } from 'rxjs/internal/Subscription';
import { EventManager } from '../../../core/util/event-manager.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'bm-alert',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <nz-alert *ngIf="alert" class="mb-3" [nzType]="alert.type" nzMessage="{{ alert.message }}" nzShowIcon nzCloseable (nzOnClose)="clear()">
    </nz-alert>
  `,
})
export class BmAlertComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private errorSubscription?: Subscription;

  alert: BmAlert;

  constructor(
    private readonly alertService: BmAlertService,
    private readonly eventManager: EventManager,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.alertService
      .get()
      .pipe(takeUntil(this.destroy$))
      .subscribe(alert => {
        this.alert = alert;

        if (alert) {
          window.scrollTo(0, 0);
        }

        this.cdr.detectChanges();
      });

    this.errorSubscription = this.eventManager.subscribe('billMasterGwApp.httpError', (response: any) =>
      this.alertService.error(response.content.error?.detail || response.content.message)
    );
  }

  clear(): void {
    this.alertService.clear();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.errorSubscription?.unsubscribe();
    this.alertService.clear();
  }
}
