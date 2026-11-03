import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpResponse } from '@angular/common/http';
import { Deposit } from './deposit.model';
import { DepositService } from './deposit.service';
import { GatewayService } from '../gateway/gateway.service';
import { AgentService } from '../agent/agent.service';
import { Event } from '../../../config/event.constants';
import { EntityBaseComponent } from '../../../shared/entity/entity-base-component';
import { DepositTransactionService } from '../deposit-transaction/deposit-transaction.service';
import { finalize } from 'rxjs/operators';

@Component({
  templateUrl: 'deposit.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DepositComponent extends EntityBaseComponent<Deposit> implements OnInit, OnDestroy {
  constructor(
    public readonly depositService: DepositService,
    public readonly gatewayService: GatewayService,
    public readonly agentService: AgentService,
    private readonly activatedRoute: ActivatedRoute,
    public readonly depositTransactionService: DepositTransactionService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.depositService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.DEPOSIT_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  viewDepositTransactions(agentShortName: string): void {
    this.router.navigate(['dashboard/deposit-transaction'], { queryParams: { agentShortName } });
  }

  loadPage(page?: number): void {
    const req = { ...this.getRequestParams(page) };

    this.loading = true;
    this.depositService
      .query(req)
      .pipe(
        this.loadPageOperators(),
        finalize(() => this.cdr.markForCheck())
      )
      .subscribe((response: HttpResponse<Deposit[]>) => this.onDataReceived(response));
  }
}
