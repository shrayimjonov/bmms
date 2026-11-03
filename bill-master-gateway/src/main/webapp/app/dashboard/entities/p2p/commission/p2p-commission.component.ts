import {Component, OnDestroy, OnInit} from '@angular/core';
import {P2pCommission} from './p2p-commission.model';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {P2pCommissionService} from './p2p-commission.service';
import {Event} from '../../../../config/event.constants';
import {EntityBaseComponent} from '../../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'p2p-commission.template.html',
})
export class P2pCommissionComponent extends EntityBaseComponent<P2pCommission> implements OnInit, OnDestroy {
  constructor(
    public readonly p2pCommissionService: P2pCommissionService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.p2pCommissionService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.P2P_COMMISSION_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.p2pCommissionService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<P2pCommission[]>) => this.onDataReceived(response));
  }
}
