import {Component, OnDestroy, OnInit} from '@angular/core';
import {Validators} from '@angular/forms';
import {AgentRecipientService} from './agent-recipient.service';
import {AgentService} from '../agent/agent.service';
import {RecipientService} from '../recipient/recipient.service';
import {AgentRecipientSettingService} from '../agent-recipient-setting/agent-recipient-setting.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {RecipientGroupService} from '../recipient-group/recipient-group.service';
import {getJsonEditorOptions, getRelation} from '../../../shared/functions';
import {AgentRecipient} from './agent-recipient.model';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";
import {Event} from "../../../config/event.constants";

@Component({
  templateUrl: 'agent-recipient-update-modal.template.html'
})
export class AgentRecipientUpdateModalComponent extends EntityUpdateBaseComponent<AgentRecipient, AgentRecipientService> implements OnInit, OnDestroy {
  readonly editorOptions = getJsonEditorOptions();

  constructor(public readonly agentService: AgentService,
              public readonly recipientService: RecipientService,
              public readonly recipientGroupService: RecipientGroupService,
              public readonly settingService: AgentRecipientSettingService,
              private readonly agentRecipientService: AgentRecipientService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, agentRecipientService, Event.AGENT_RECIPIENT_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      agentCommission: [null, [Validators.min(0), Validators.max(100)]],
      agentFee: [null, [Validators.min(0), Validators.max(100)]],
      systemCommissionUp: [null, Validators.max(100)],
      systemCommissionDown: [null, [Validators.min(0), Validators.max(100)]],
      systemFee: [null, [Validators.min(0), Validators.max(100)]],
      eposPort: [null],
      eposMerchant: [null],
      eposTerminal: [null],
      active: [true],
      requestGroup: [null, Validators.maxLength(64)],
      jsonParams: [],
      agent: [null, Validators.required],
      recipient: [null, Validators.required],
      setting: [null],
      recipientGroup: [null],
    });

    if (this.data.id) {
      this.editForm.patchValue({
        jsonParams: JSON.parse(this.data.jsonParams)
      });
    }

    this.editForm.get('recipientGroup')?.valueChanges.subscribe(v => {
      if (v) {
        this.editForm.get('recipient')?.clearValidators();
      } else {
        this.editForm.get('recipient')?.setValidators(Validators.required);
      }

      this.editForm.get('recipient')?.updateValueAndValidity();
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const agentRecipient: AgentRecipient = this.getRawValue();
    agentRecipient.jsonParams = JSON.stringify(this.editForm.get('jsonParams')?.value);
    agentRecipient.agent = getRelation(this.editForm.get('agent').value);
    agentRecipient.recipient = getRelation(this.editForm.get('recipient').value);
    agentRecipient.setting = getRelation(this.editForm.get('setting').value);
    agentRecipient.recipientGroup = getRelation(this.editForm.get('recipientGroup').value);

    if (agentRecipient.requestGroup === '') {
      agentRecipient.requestGroup = null;
    }

    this.save(agentRecipient);
  }
}
