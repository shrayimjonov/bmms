import {Component, OnDestroy} from "@angular/core";
import {FormBuilder, Validators} from "@angular/forms";
import {CardType} from "../../reconcile-summary.model";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {P2pReconcile} from "../p2p-reconcile.model";
import {ReconcileSummaryService} from "../../reconcile-summary.service";
import {Subject} from "rxjs";
import {finalize, takeUntil} from "rxjs/operators";
import {BmAlertService} from "../../../../../../shared/component/bm-alert/bm-alert.service";
import {EventManager} from "../../../../../../core/util/event-manager.service";
import {Event} from "../../../../../../config/event.constants";

@Component({
  templateUrl: 'p2p-card-reconcile-modal.template.html',
})
export class P2pCardReconcileModalComponent implements OnDestroy {
  loading: boolean;

  readonly types = Object.keys(CardType).map(key => CardType[key]);
  readonly editForm = this.fb.group({
    type: [CardType.UZCARD, Validators.required],
    fromDate: [null, Validators.required],
    toDate: [null, Validators.required]
  });

  private readonly destroy$ = new Subject<void>();

  constructor(public readonly activeModal: NgbActiveModal,
              private readonly reconcileService: ReconcileSummaryService,
              private readonly alertService: BmAlertService,
              private readonly eventManager: EventManager,
              private readonly fb: FormBuilder) {
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  save(): void {
    const req: P2pReconcile = this.editForm.getRawValue();

    this.loading = true;
    this.reconcileService.reconcileByCardType(req)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe((success: boolean) => this.onFetched(success));
  }

  private onFetched(success: boolean): void {
    if (!success) {
      this.alertService.error('billMasterGwApp.pkbillmsReconcileSummary.reconcile.error');
    } else {
      this.alertService.success('billMasterGwApp.pkbillmsReconcileSummary.reconcile.success', null, true);
      this.eventManager.broadcast(Event.RECONCILE_SUMMARY_LIST_MODIFICATION);
      this.activeModal.dismiss('success');
    }
  }
}
