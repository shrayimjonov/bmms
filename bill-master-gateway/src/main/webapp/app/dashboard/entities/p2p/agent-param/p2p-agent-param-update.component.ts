import { Component, OnDestroy, OnInit } from '@angular/core';
import { EntityUpdateBaseComponent } from '../../../../shared/entity/entity-update-base-component';
import { P2pAgentParamModel } from './p2p-agent-param.model';
import { P2pAgentParamService } from './p2p-agent-param.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Event } from '../../../../config/event.constants';
import { Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  templateUrl: 'p2p-agent-param-update.component.html',
})
export class P2PAgentParamUpdateComponent
  extends EntityUpdateBaseComponent<P2pAgentParamModel, P2pAgentParamService>
  implements OnInit, OnDestroy
{
  constructor(private readonly activeModal: NgbActiveModal, private readonly p2pAgentParamService: P2pAgentParamService) {
    super(activeModal, p2pAgentParamService, Event.P2P_AGENT_PARAM_LIST_MODIFICATION);
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [null],
      agentId: [null, Validators.required],
      p2pParamsRegex: [null, Validators.required],
      active: [null],
      description: [null],
    });
  }

  regexTest(): void {
    this.p2pAgentParamService.regexTest(this.editForm.get('p2pParamsRegex').value);
  }

  routeRegexSite(): void {
    window.open('https://regex101.com', '_blank');
  }
}
