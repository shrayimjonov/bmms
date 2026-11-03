import {Component, OnDestroy, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Bill} from './bill.model';
import {Authority} from '../../../config/authority.constants';
import {Router} from '@angular/router';
import {AccountService} from '../../../core/auth/account.service';
import {Recipient} from '../recipient/recipient.model';
import {RecipientService} from '../recipient/recipient.service';
import {AgentService} from '../agent/agent.service';
import {pluck, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {Agent} from '../agent/agent.model';
import {TranslateService} from '@ngx-translate/core';

@Component({
  templateUrl: 'bill-info-modal.template.html',
  styleUrls: ['bill-type.style.scss'],
})
export class BillInfoModalComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  bill: Bill;

  /* for translation */
  id: string;
  login: string;
  shortName: string;
  recipientId: string;
  account: string;
  amount: string;
  extId: string;
  responseCode: string;
  responseText: string;
  transactionId: string;
  type: string;
  success: string;

  constructor(
    private activeModal: NgbActiveModal,
    private router: Router,
    private recipientService: RecipientService,
    private agentService: AgentService,
    private accountService: AccountService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.translator();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  translator(): void {
    this.id = this.translateService.instant('global.field.id');
    this.login = this.translateService.instant('billMasterGwApp.pkbillmsBill.login');
    this.shortName = this.translateService.instant('billMasterGwApp.pkbillmsBill.recipientShortName');
    this.recipientId = this.translateService.instant('billMasterGwApp.pkbillmsBill.recipientId');
    this.account = this.translateService.instant('billMasterGwApp.pkbillmsBill.account');
    this.amount = this.translateService.instant('billMasterGwApp.pkbillmsBill.amount');
    this.extId = this.translateService.instant('billMasterGwApp.pkbillmsBill.extId');
    this.responseCode = this.translateService.instant('billMasterGwApp.pkbillmsBill.responseCode');
    this.responseText = this.translateService.instant('billMasterGwApp.pkbillmsBill.responseText');
    this.transactionId = this.translateService.instant('billMasterGwApp.pkbillmsBill.transactionId');
    this.type = this.translateService.instant('billMasterGwApp.pkbillmsBill.type');
    this.success = this.translateService.instant('billMasterGwApp.pkbillmsBill.success');
  }

  close(): void {
    this.activeModal.dismiss('close');
  }

  editTransaction(id: number): void {
    if (this.accountService.hasAnyAuthority([Authority.TRANSACTION_EDIT])) {
      this.router.navigate([`dashboard/transaction/${id}/edit`]).then(() => this.close());
    }
  }

  viewRecipient(id: number): void {
    this.recipientService
      .find(id)
      .pipe(takeUntil(this.destroy$), pluck('body'))
      .subscribe((recipient: Recipient) => this.recipientService.openView(recipient));
  }

  viewAgent(login: string): void {
    const req = {
      size: 1,
      'shortName.equals': login,
    };

    this.agentService
      .query(req)
      .pipe(takeUntil(this.destroy$), pluck('body'))
      .subscribe((agents: Agent[]) => this.agentService.openView(agents[0]));
  }
}
