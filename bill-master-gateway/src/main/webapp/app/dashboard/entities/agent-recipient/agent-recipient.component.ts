import {Component, OnDestroy, OnInit} from '@angular/core';
import {AgentRecipient} from './agent-recipient.model';
import {ActivatedRoute} from '@angular/router';
import {AgentRecipientService} from './agent-recipient.service';
import {HttpResponse} from '@angular/common/http';
import {AgentService} from '../agent/agent.service';
import {RecipientService} from '../recipient/recipient.service';
import {RecipientGroupService} from '../recipient-group/recipient-group.service';
import {Event} from '../../../config/event.constants';
import {P2pParamService} from '../p2p/param/p2p-param.service';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'agent-recipient.template.html'
})
export class AgentRecipientComponent extends EntityBaseComponent<AgentRecipient> implements OnInit, OnDestroy {
  constructor(public readonly agentRecipientService: AgentRecipientService,
              public readonly recipientGroupService: RecipientGroupService,
              public readonly recipientService: RecipientService,
              public readonly agentService: AgentService,
              public readonly p2pParamService: P2pParamService,
              private readonly activatedRoute: ActivatedRoute) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.agentRecipientService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.AGENT_RECIPIENT_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.agentRecipientService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<AgentRecipient[]>) => this.onDataReceived(response));
  }
}
