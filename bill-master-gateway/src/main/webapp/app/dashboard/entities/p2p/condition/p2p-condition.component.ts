import {Component, OnDestroy, OnInit} from '@angular/core';
import {P2pCondition} from './p2p-condition.model';
import {ActivatedRoute} from '@angular/router';
import {P2pConditionService} from './p2p-condition.service';
import {HttpResponse} from '@angular/common/http';
import {Event} from '../../../../config/event.constants';
import {EntityBaseComponent} from '../../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'p2p-condition.template.html',
})
export class P2pConditionComponent extends EntityBaseComponent<P2pCondition> implements OnInit, OnDestroy {
  constructor(
    public readonly p2pConditionService: P2pConditionService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.p2pConditionService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.P2P_CONDITION_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.p2pConditionService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<P2pCondition[]>) => this.onDataReceived(response));
  }
}
