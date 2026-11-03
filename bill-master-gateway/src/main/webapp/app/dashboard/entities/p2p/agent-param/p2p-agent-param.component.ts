import { Component, OnDestroy, OnInit } from '@angular/core';
import { EntityBaseComponent } from '../../../../shared/entity/entity-base-component';
import { P2pAgentParamModel } from './p2p-agent-param.model';
import { Event } from '../../../../config/event.constants';
import { ActivatedRoute } from '@angular/router';
import { P2pAgentParamService } from './p2p-agent-param.service';

@Component({
  templateUrl: 'p2p-agent-param.component.html',
})
export class P2pAgentParamComponent extends EntityBaseComponent<P2pAgentParamModel> implements OnInit, OnDestroy {
  constructor(public readonly p2pAgentParamService: P2pAgentParamService, private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.p2pAgentParamService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.P2P_AGENT_PARAM_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number) {
    this.loading = true;
    this.p2pAgentParamService
      .query(this.getRequestParams(page))
      .pipe(this.loadPageOperators())
      .subscribe(response => this.onDataReceived(response));
  }
}
