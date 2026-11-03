import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {DepositTransaction} from './deposit-transaction.model';
import {DepositTransactionService} from './deposit-transaction.service';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'deposit-transaction.template.html',
})
export class DepositTransactionComponent extends EntityBaseComponent<DepositTransaction> implements OnInit, OnDestroy {
  agentShortName?: string;

  constructor(public readonly depositTransactionService: DepositTransactionService, private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.handleNavigation(this.activatedRoute, (data, params) => this.loadPage(null, params.get('agentShortName')));
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number, agentShortName?: string): void {
    const req = this.getRequestParams(page);
    if (agentShortName) {
      this.agentShortName = agentShortName;
      req['agentLogin.equals'] = agentShortName;
    }

    this.loading = true;
    this.depositTransactionService
      .query(req)
      .pipe(this.loadPageOperators())
      .subscribe((response: HttpResponse<DepositTransaction[]>) => this.onDataReceived(response));
  }
}
