import {Component, OnDestroy, OnInit} from '@angular/core';
import {AgentService} from './agent.service';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {LOGIN_REGEX_VALIDATOR} from '../../../config/input-validator.constants';
import {AMOUNT_RATIOS, CURRENCIES} from '../../../config/pagination.constants';
import {Agent, DetailedAgent} from './agent.model';
import {EntityUpdateBaseComponent} from '../../../shared/entity/entity-update-base-component';
import {Event} from '../../../config/event.constants';

@Component({
  templateUrl: 'agent-update-modal.template.html'
})
export class AgentUpdateModalComponent extends EntityUpdateBaseComponent<Agent, AgentService> implements OnInit, OnDestroy {
  readonly amountRatios = AMOUNT_RATIOS;
  readonly currencies = CURRENCIES;

  constructor(
    private readonly activeModal: NgbActiveModal,
    private readonly agentService: AgentService,
  ) {
    super(activeModal, agentService, Event.AGENT_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      shortName: [null, [Validators.required, Validators.maxLength(64), Validators.pattern(LOGIN_REGEX_VALIDATOR)]],
      amountRatio: [this.amountRatios[0].value, Validators.required],
      apiKey: [null, [Validators.required, Validators.maxLength(64)]],
      currencies: [[860], [Validators.required, Validators.maxLength(64)]],
      phoneNumber: [null, Validators.maxLength(12)],
      inn: [null, Validators.maxLength(9)],
      orgName: [null, Validators.maxLength(64)],
      email: [null, Validators.required],
      titles: [null, Validators.required],
      password: ['12345', [Validators.required, Validators.minLength(5)]],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const rawValue = this.getRawValue() as any;
    let detailedAgent: DetailedAgent | Agent = {
      email: rawValue.email,
      password: rawValue.password,
      agent: {...rawValue, active: true},
      agentDTO: {...rawValue, active: true},
    };
    detailedAgent.agent.id = this.data.id;
    detailedAgent.agentDTO.id = this.data.id;
    detailedAgent.id = detailedAgent.agent.id;

    if (detailedAgent.id) {
      detailedAgent = detailedAgent.agent;
    }

    this.save(detailedAgent, this.getMethod());
  }

  private getMethod(): keyof AgentService {
    if (this.data.id) {
      return 'update';
    }

    return 'createWithPassword';
  }
}
