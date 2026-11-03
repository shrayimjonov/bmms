import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {Transaction, TransactionStatus} from '../transaction.model';
import {ActivatedRoute} from '@angular/router';
import {TransactionService} from '../transaction.service';
import {HttpResponse} from '@angular/common/http';
import {Subscription} from 'rxjs/internal/Subscription';
import {Bill} from '../../bill/bill.model';
import {Gateway} from '../../gateway/gateway.model';
import {Recipient} from '../../recipient/recipient.model';
import {GatewayService} from '../../gateway/gateway.service';
import {RecipientService} from '../../recipient/recipient.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {AgentService} from '../../agent/agent.service';
import {filter, finalize, map, pluck} from 'rxjs/operators';
import {Agent} from '../../agent/agent.model';
import {BillService} from '../../bill/bill.service';
import {BillInfoModalComponent} from '../../bill/bill-info-modal.component';
import {Action} from '../../../../shared/model/action.model';
import {BmAlertService} from '../../../../shared/component/bm-alert/bm-alert.service';
import {ResultData} from '../../../../shared/model/pkbillms/result-data.model';
import {CancelPayModalComponent} from './cancel-pay/cancel-pay-modal.component';
import {TransactionBillsComponent} from "../transaction-bills/transaction-bills.component";

@Component({
  templateUrl: 'edit-transaction.template.html',
  styleUrls: ['edit-transaction.style.scss', '../transaction-status.style.scss'],
})
export class EditTransactionComponent implements OnInit {
  @ViewChild('transactionBills', {read: TransactionBillsComponent})
  transactionBillsRef: TransactionBillsComponent;

  private readonly subscribers = new Subscription();

  transaction: Transaction;
  relatedTransactions: Transaction[];
  recipients: Recipient[];
  actions: Action[];
  loading: boolean;
  status = TransactionStatus;

  constructor(
    private readonly modalService: NgbModal,
    private readonly agentService: AgentService,
    private readonly billService: BillService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly transactionService: TransactionService,
    private readonly gatewayService: GatewayService,
    private readonly alertService: BmAlertService,
    private readonly recipientService: RecipientService,
  ) {
  }

  ngOnInit(): void {
    this.subscription = this.activatedRoute.data.subscribe(({transaction}) => {
      this.transaction = transaction;
      this.loadRelatedTransactions(transaction.requestTrId);
    });

    this.loadActions();
  }

  private loadActions(): void {
    this.subscription = this.transactionService
      .getActions(this.transaction.status)
      .pipe(
        map(actions => actions.filter(a => a.active)),
      )
      .subscribe((activeActions: Action[]) => (this.actions = activeActions));
  }

  executeAction(action: Action): void {
    if (!action.uri) {
      return this.alertService.error(`Field 'url' cannot be empty in action`, null, true);
    }

    const body = this.getRequestBody(action.uriParams);
    const url = this.getURL(action);

    this.loading = true;
    this.subscription = this.transactionService.executeAction(url, action.method, body)
      .pipe(
        finalize(() => this.loading = false)
      )
      .subscribe((result: ResultData<any>) => {
        if (result.success) {
          this.alertService.success(result.details, null, true);
        } else {
          this.alertService.error(result.details, null, true);
        }

        this.transactionBillsRef.loadBills();
        this.loadTransaction();
        this.loadRelatedTransactions(this.transaction.requestTrId);
      });
  }

  private getURL(action: Action): string {
    if (!action.pathVariables?.length) {
      return action.uri;
    }

    return `/${action.uri}/${this.transaction[action.pathVariables[0]]}`;
  }

  private loadTransaction(): void {
    this.loading = true;
    this.subscription = this.transactionService
      .find(this.transaction.id)
      .pipe(
        pluck('body'),
        finalize(() => this.loading = false)
      )
      .subscribe(transaction => {
        this.transaction = transaction;
        this.loadActions();
      });
  }

  private getRequestBody(keys: string[]): {} {
    const req = {};

    if (keys?.length) {
      keys.forEach(key => (req[key] = this.transaction[key]));
    }

    return req;
  }

  cancelPay(): void {
    this.modalService.open(CancelPayModalComponent).componentInstance.id = this.transaction.id;
  }

  private loadRelatedTransactions(requestTrId: string): void {
    if (requestTrId) {
      const req = {
        size: 2000,
        sort: ['recipientDate,desc'],
        'requestTrId.equals': requestTrId,
        'id.notEquals': this.transaction.id,
      };

      this.subscription = this.transactionService
        .query(req)
        .subscribe((res: HttpResponse<Transaction[]>) => (this.relatedTransactions = res.body));
    }
  }

  viewRecipient(id: number): void {
    this.subscription = this.recipientService
      .find(id)
      .pipe(
        pluck('body'),
        filter(r => !!r)
      )
      .subscribe((recipient: Recipient) => this.recipientService.openView(recipient));
  }

  viewAgent(shortName: string): void {
    const req = {
      size: 1,
      'shortName.equals': shortName,
    };

    this.subscription = this.agentService
      .query(req)
      .pipe(
        pluck('body'),
        filter((agents: Agent[]) => !!agents?.length)
      )
      .subscribe((result: Agent[]) => this.agentService.openView(result[0]));
  }

  viewBill(id: number): void {
    this.subscription = this.billService
      .find(id)
      .pipe(
        pluck('body'),
        filter((b: Bill) => !!b)
      )
      .subscribe((bill: Bill) => {
        this.modalService.open(BillInfoModalComponent, {modalDialogClass: 'bill-info-modal'}).componentInstance.bill = bill;
      });
  }

  viewGateway(name: string): void {
    const query = {
      size: 1,
      'name.equals': name,
    };

    this.subscription = this.gatewayService
      .query(query)
      .pipe(
        pluck('body'),
        filter((gateways: Gateway[]) => !!gateways?.length)
      )
      .subscribe((result: Gateway[]) => this.gatewayService.openView(result[0]));
  }

  trackId(index: number, item: Transaction | Bill): number | undefined {
    return item.id;
  }

  private set subscription(subscription: Subscription) {
    this.subscribers.add(subscription);
  }
}
