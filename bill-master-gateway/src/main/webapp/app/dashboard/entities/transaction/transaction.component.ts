import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {filter, pluck, takeUntil} from 'rxjs/operators';
import {TransactionService} from './transaction.service';
import {Transaction} from './transaction.model';
import {BillService} from '../bill/bill.service';
import {AccountService} from '../../../core/auth/account.service';
import * as moment from 'moment';
import {Authority} from '../../../config/authority.constants';
import {RecipientService} from '../recipient/recipient.service';
import {AgentService} from '../agent/agent.service';
import {BmAlertService} from '../../../shared/component/bm-alert/bm-alert.service';
import {Event} from '../../../config/event.constants';
import {BmAmountPipe} from '../../../shared/component/bm-amount/bm-amount.pipe';
import {Agent} from '../agent/agent.model';
import {Bill} from '../bill/bill.model';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {BillInfoModalComponent} from '../bill/bill-info-modal.component';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';
import {NzSizeDSType} from 'ng-zorro-antd/core/types';

@Component({
  templateUrl: 'transaction.template.html',
  styleUrls: ['transaction-status.style.scss'],
})
export class TransactionComponent extends EntityBaseComponent<Transaction> implements OnInit, OnDestroy {
  readonly expandSet = new Set<number>();
  size: NzSizeDSType;
  showExpand: boolean;

  constructor(
    public readonly transactionService: TransactionService,
    public readonly agentService: AgentService,
    private readonly amountPipe: BmAmountPipe,
    private readonly billService: BillService,
    private readonly accountService: AccountService,
    private readonly recipientService: RecipientService,
    private readonly alertService: BmAlertService,
    private readonly modalService: NgbModal,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.transactionService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.TRANSACTION_LIST_MODIFICATION, () => this.loadPage());
    this.showExpand = this.accountService.hasAnyAuthority([Authority.BILL_READ]);
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  shareInfoToTelegram(transaction: Transaction): void {
    const amount = this.amountPipe.transform(transaction.requestAmount, transaction.amountRatio);
    const date = moment(transaction.date).local().format('hh:mm DD.MM.YYYY');
    const body =
      `🧾 Номер ---> ${transaction.id}%0A` +
      `🏛 Поставщик ---> ${transaction.recipientName}%0A` +
      `🛒 Инициатор ---> ${transaction.requestSubAccount}%0A` +
      `📌 Тип оплаты ---> ${transaction.payType}%0A` +
      `⚠ Статус ---> ${transaction.status}%0A` +
      `💰 Сумма ---> ${amount}%0A` +
      `⏱ Дата ---> ${date}`;

    window.open('tg://msg_url?url=🔽 ПЛАТЕЖ 🔽' + '&text=' + body);
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.transactionService
      .query(this.getRequestParams(page))
      .pipe(this.loadPageOperators())
      .subscribe((response: HttpResponse<Transaction[]>) => this.onDataReceived(response));
  }

  onExpandChange(id: number, checked: boolean): void {
    if (checked) {
      this.expandSet.add(id);
    } else {
      this.expandSet.delete(id);
    }
  }

  billView(id: number): void {
    if (this.accountService.hasAnyAuthority([Authority.BILL_READ])) {
      this.billService
        .find(id)
        .pipe(
          takeUntil(this.destroy$),
          pluck('body'),
          filter(b => !!b)
        )
        .subscribe(
          (bill: Bill) => {
            const modal = this.modalService.open(BillInfoModalComponent, { modalDialogClass: 'bill-info-modal' });
            modal.componentInstance.bill = bill;
          },
          error => this.alertService.error(error.detail, null, true)
        );
    } else {
      this.alertService.error('У Вас нет доступа на эту страницу', null, true);
    }
  }

  viewAgent(login: string): void {
    const req = {
      size: 1,
      'shortName.equals': login,
    };

    this.agentService
      .query(req)
      .pipe(takeUntil(this.destroy$), pluck('body'))
      .subscribe((agents: Agent[]) => {
        if (agents.length) {
          this.agentService.openView(agents[0]);
        }
        this.alertService.warning('billMasterGwApp.pkbillmsTransaction.notAgent');
      });
  }
}
