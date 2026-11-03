import {Component, OnDestroy} from '@angular/core';
import {Subscription} from 'rxjs';
import {Deposit} from './deposit.model';
import {FormBuilder, Validators} from '@angular/forms';
import {EventManager} from '../../../core/util/event-manager.service';
import {DepositService} from './deposit.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {ReplenishDeposit} from './replenish-deposit.model';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {BmAlertService} from '../../../shared/component/bm-alert/bm-alert.service';
import {finalize} from 'rxjs/operators';
import {Event} from '../../../config/event.constants';

@Component({
  templateUrl: 'replenish-deposit-modal.template.html',
})
export class ReplenishDepositModalComponent implements OnDestroy {
  private subscribers = new Subscription();

  replenishDeposit: ReplenishDeposit;
  error: any;
  deposit: Deposit;
  loading: boolean;

  editForm = this.fb.group({
    doc: [null, [Validators.required]],
    overdraftLimit: [null],
    balanceAmount: [null, [Validators.required]],
    payerTaxnr: [null],
    recipientTaxnr: [null],
  });

  constructor(
    private eventManager: EventManager,
    private depositService: DepositService,
    private activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private alertService: BmAlertService,
  ) {}

  save(): void {
    const replenishDeposit: ReplenishDeposit = {
      agentLogin: this.deposit.agent.shortName,
      doc: this.editForm.get(['doc'])!.value,
      overdraftLimit: this.editForm.get(['overdraftLimit'])!.value,
      balanceAmount: Number(this.editForm.get(['balanceAmount'])!.value),
      payerTaxnr: this.editForm.get(['payerTaxnr'])!.value,
      recipientTaxnr: this.editForm.get(['recipientTaxnr'])!.value,
    };
    this.replenish(replenishDeposit);
  }

  private replenish(replenishDeposit: ReplenishDeposit): void {
    this.loading = true;
    this.subscription = this.depositService
      .depositReplenish(replenishDeposit)
      .pipe(
        finalize(() => this.loading = false)
      )
      .subscribe(
        (res: HttpResponse<any>) => {
          if (res.body['success']) {
            this.eventManager.broadcast(Event.DEPOSIT_LIST_MODIFICATION);
            this.alertService.success('billMasterGwApp.pkbillmsDeposit.onSuccessReplenish');
            this.cancel();
          } else {
            this.alertService.error('billMasterGwApp.pkbillmsDeposit.onFailedReplenish');
          }
        },
        (err: HttpErrorResponse) => {
          this.error = err.error['langs'];
        }
      );
  }

  cancel() {
    this.activeModal.dismiss('cancel');
  }

  private set subscription(subscription: Subscription) {
    this.subscribers.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscribers.unsubscribe();
  }
}
