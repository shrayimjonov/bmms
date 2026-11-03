import { ChangeDetectionStrategy, Component, Input, OnDestroy } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { finalize, takeUntil } from 'rxjs/operators';
import { EventManager } from '../../../core/util/event-manager.service';
import { Subject } from 'rxjs';
import { BmAlertService } from '../../component/bm-alert/bm-alert.service';
import { Observable } from 'rxjs/internal/Observable';
import { ActivatedRoute } from '@angular/router';
import { Event } from '../../../config/event.constants';

@Component({
  templateUrl: 'entity-delete-modal.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntityDeleteModalComponent implements OnDestroy {
  @Input() useFunction: Observable<void>;
  @Input() event: Event;

  private readonly destroy$ = new Subject();

  loading: boolean;
  translateKey: string;
  translateValues: any;

  constructor(
    private activeModal: NgbActiveModal,
    private eventManager: EventManager,
    private activatedRoute: ActivatedRoute,
    private alertService: BmAlertService
  ) {}

  close(): void {
    this.activeModal.dismiss();
  }

  delete(): void {
    this.loading = true;
    this.useFunction
      ?.pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe(() => this.onSuccess());
  }

  private onSuccess(): void {
    this.close();

    this.eventManager.broadcast(this.event);
    this.alertService.success(this.translateKey, this.translateValues, true);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
