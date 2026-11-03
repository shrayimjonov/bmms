import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {EventManager} from '../../../core/util/event-manager.service';
import {BmAlertService} from '../../../shared/component/bm-alert/bm-alert.service';
import {HttpResponse} from '@angular/common/http';
import {Transaction, TransactionStatus} from './transaction.model';
import {TransactionService} from './transaction.service';
import {finalize, takeUntil} from 'rxjs/operators';
import * as moment from 'moment';
import {Event} from '../../../config/event.constants';
import {PayType} from '../../../shared/model/enumerations/pay-type.model';
import {Subject} from "rxjs";

@Component({
  templateUrl: 'transaction-update-modal.template.html',
})
export class TransactionUpdateModalComponent implements OnInit, OnDestroy {
  transaction: Transaction;
  loading: boolean;
  private readonly destroy$ = new Subject<void>();

  readonly transactionStatus = Object.keys(TransactionStatus);
  readonly payTypes = Object.keys(PayType);
  readonly editForm = this.fb.group({
    id: [],
    requestAmount: [],
    requestCcy: [],
    requestTrId: [null, [Validators.required, Validators.maxLength(72)]],
    requestLogin: [null, [Validators.required, Validators.maxLength(64)]],
    requestGroup: [null, [Validators.maxLength(64)]],
    senderName: [null, [Validators.maxLength(32)]],
    senderAccount: [null, [Validators.maxLength(64)]],
    senderServiceId: [],
    senderAmount: [],
    senderCcy: [],
    senderTrId: [],
    category: [],
    senderSuccess: [],
    senderDate: [],
    senderStatus: [null, [Validators.maxLength(16)]],
    senderStatusText: [null, [Validators.maxLength(128)]],
    senderBillId: [],
    senderGateway: [],
    gatewayAmount: [],
    recipientName: [null, [Validators.maxLength(32)]],
    recipientAccount: [null, [Validators.maxLength(64)]],
    recipientServiceId: [],
    recipientAmount: [],
    recipientCcy: [],
    recipientTrId: [],
    recipientSuccess: [],
    recipientDate: [],
    recipientStatus: [null, [Validators.maxLength(16)]],
    recipientStatusText: [null, [Validators.maxLength(128)]],
    recipientBillId: [],
    recipientGateway: [],
    systemCommissionUpAmount: [],
    systemCommissionDownAmount: [],
    systemFeeAmount: [],
    agentFeeAmount: [],
    roundAmount: [null, [Validators.min(0)]],
    systemFee: [null, [Validators.min(0), Validators.max(100)]],
    systemCurrency: [],
    ccyRate: [],
    status: [],
    comment: [null, [Validators.required, Validators.maxLength(128)]],
    date: [],
    payType: [null, [Validators.required]],
    requestSubAccount: [null, [Validators.maxLength(32)]],
    eposMerchant: [null, [Validators.maxLength(32)]],
    eposTerminal: [null, [Validators.maxLength(32)]],
    main: [],
    amountRatio: [null, [Validators.required]],
    senderBean: [null, [Validators.maxLength(32)]],
    promo: [null, Validators.maxLength(128)],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly activeModal: NgbActiveModal,
    private readonly eventManager: EventManager,
    private readonly modalService: NgbModal,
    private readonly alertService: BmAlertService,
    private readonly transactionService: TransactionService,
  ) {
  }

  ngOnInit(): void {
    if (this.transaction?.id) {
      this.updateForm();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private updateForm(): void {
    this.editForm.patchValue({
      ...this.transaction,
      senderDate: this.transaction.senderDate ? moment(this.transaction.senderDate).toDate() : null,
      recipientDate: this.transaction.recipientDate ? moment(this.transaction.recipientDate).toDate() : null,
    });
  }

  save(): void {
    const date = {
      sender: this.editForm.get(['senderDate'])!.value,
      recipient: this.editForm.get(['recipientDate'])!.value,
    };

    const transaction: Transaction = {
      id: this.transaction?.id,
      requestAmount: this.editForm.get(['requestAmount'])!.value,
      requestCcy: this.editForm.get(['requestCcy'])!.value,
      requestTrId: this.editForm.get(['requestTrId'])!.value,
      requestLogin: this.editForm.get(['requestLogin'])!.value,
      requestGroup: this.editForm.get(['requestGroup'])!.value,
      senderName: this.editForm.get(['senderName'])!.value,
      senderAccount: this.editForm.get(['senderAccount'])!.value,
      senderServiceId: this.editForm.get(['senderServiceId'])!.value,
      senderAmount: this.editForm.get(['senderAmount'])!.value,
      senderCcy: this.editForm.get(['senderCcy'])!.value,
      senderTrId: this.editForm.get(['senderTrId'])!.value,
      senderSuccess: this.editForm.get(['senderSuccess'])!.value,
      senderStatus: this.editForm.get(['senderStatus'])!.value,
      senderStatusText: this.editForm.get(['senderStatusText'])!.value,
      senderBillId: this.editForm.get(['senderBillId'])!.value,
      category: this.editForm.get(['category'])!.value,
      senderGateway: this.editForm.get(['senderGateway'])!.value,
      senderDate: date.sender ? moment(date.sender) : null,
      gatewayAmount: this.editForm.get(['gatewayAmount'])!.value,
      recipientName: this.editForm.get(['recipientName'])!.value,
      recipientAccount: this.editForm.get(['recipientAccount'])!.value,
      recipientServiceId: this.editForm.get(['recipientServiceId'])!.value,
      recipientAmount: this.editForm.get(['recipientAmount'])!.value,
      recipientCcy: this.editForm.get(['recipientCcy'])!.value,
      recipientTrId: this.editForm.get(['recipientTrId'])!.value,
      recipientSuccess: this.editForm.get(['recipientSuccess'])!.value,
      recipientStatus: this.editForm.get(['recipientStatus'])!.value,
      recipientStatusText: this.editForm.get(['recipientStatusText'])!.value,
      recipientBillId: this.editForm.get(['recipientBillId'])!.value,
      recipientGateway: this.editForm.get(['recipientGateway'])!.value,
      recipientDate: date.recipient ? moment(date.recipient) : null,
      date: moment(this.transaction.date),
      systemCommissionUpAmount: this.editForm.get(['systemCommissionUpAmount'])!.value,
      systemCommissionDownAmount: this.editForm.get(['systemCommissionDownAmount'])!.value,
      systemFeeAmount: this.editForm.get(['systemFeeAmount'])!.value,
      agentFeeAmount: this.editForm.get(['agentFeeAmount'])!.value,
      roundAmount: this.editForm.get(['roundAmount'])!.value,
      systemFee: this.editForm.get(['systemFee'])!.value,
      systemCurrency: this.editForm.get(['systemCurrency'])!.value,
      ccyRate: this.editForm.get(['ccyRate'])!.value,
      status: this.editForm.get(['status'])!.value,
      comment: this.editForm.get(['comment'])!.value,
      payType: this.editForm.get(['payType'])!.value,
      requestSubAccount: this.editForm.get(['requestSubAccount'])!.value,
      eposMerchant: this.editForm.get(['eposMerchant'])!.value,
      eposTerminal: this.editForm.get(['eposTerminal'])!.value,
      main: this.editForm.get(['main'])!.value,
      amountRatio: this.editForm.get(['amountRatio'])!.value,
      senderBean: this.editForm.get(['senderBean'])!.value,
      promo: this.editForm.get(['promo'])!.value,
    };

    this.loading = true;
    if (transaction?.id) {
      this.update(transaction);
    } else {
      this.create(transaction);
    }
  }

  private create(transaction: Transaction): void {
    this.transactionService
      .create(transaction)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe((res: HttpResponse<Transaction>) => this.onSuccess('created', res.body.id));
  }

  private update(transaction: Transaction): void {
    this.transactionService
      .update(transaction)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe(() => this.onSuccess('updated', transaction.id));
  }

  private onSuccess(action: 'created' | 'updated', id: number): void {
    this.close();
    this.eventManager.broadcast(Event.TRANSACTION_LIST_MODIFICATION);
    this.alertService.success('pkbillmsApp.pkbillmsTransaction.' + action, id, true);
  }

  close(): void {
    this.activeModal.dismiss();
  }
}
