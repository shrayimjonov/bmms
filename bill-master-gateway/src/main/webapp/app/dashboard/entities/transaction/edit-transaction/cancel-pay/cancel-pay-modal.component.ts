import {Component, OnDestroy} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {OverpayService} from "../overpay.service";
import {Subscription} from "rxjs/internal/Subscription";
import {finalize} from "rxjs/operators";
import {ResultData} from "../../../../../shared/model/pkbillms/result-data.model";
import {OverpayResult} from "../overpay-result.model";
import {BmAlertService} from "../../../../../shared/component/bm-alert/bm-alert.service";

@Component({
  templateUrl: 'cancel-pay-modal.template.html',
})
export class CancelPayModalComponent implements OnDestroy {
  private subscription?: Subscription;

  id: number;
  loading: boolean;

  constructor(private activeModal: NgbActiveModal,
              private overpayService: OverpayService,
              private alertService: BmAlertService) {
  }

  cancelPay(): void {
    this.loading = true;
    this.subscription = this.overpayService.cancelPay(this.id)
      .pipe(finalize(() => this.loading = false))
      .subscribe((res: ResultData<OverpayResult>) => this.onSuccess(res));
  }

  private onSuccess(result: ResultData<OverpayResult>): void {
    if (result.success) {
      this.alertService.success('Транзакция отменена', null, true);
      this.close();
    } else {
      this.alertService.error(result.exception.title);
    }
  }

  close(): void {
    this.activeModal.dismiss('close');
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
