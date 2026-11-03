import {Component, OnDestroy, OnInit} from '@angular/core';
import {Agent} from './agent.model';
import {AgentService} from './agent.service';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {StateEnum} from '../user-management/user/user.model';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from "../../../shared/entity/entity-base-component";

@Component({
  templateUrl: 'agent.template.html'
})
export class AgentComponent extends EntityBaseComponent<Agent> implements OnInit, OnDestroy {
  constructor(public readonly agentService: AgentService,
              private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.agentService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.AGENT_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    const req = {...this.getRequestParams(page), 'state.notEquals': StateEnum.DELETED}

    this.loading = true;
    this.agentService.query(req)
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Agent[]>) => this.onDataReceived(response));
  }
}
