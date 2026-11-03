import { Component, OnDestroy, OnInit } from '@angular/core';
import { Validators } from '@angular/forms';
import { AgentRecipientSettingService } from './agent-recipient-setting.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Event } from '../../../config/event.constants';
import { AgentRecipientSetting } from './agent-recipient-setting.model';
import { EntityUpdateBaseComponent } from '../../../shared/entity/entity-update-base-component';

@Component({
  templateUrl: 'agent-recipient-setting-update-modal.template.html',
})
export class AgentRecipientSettingUpdateModalComponent
  extends EntityUpdateBaseComponent<AgentRecipientSetting, AgentRecipientSettingService>
  implements OnInit, OnDestroy
{
  constructor(readonly settingService: AgentRecipientSettingService, private activeModal: NgbActiveModal) {
    super(activeModal, settingService, Event.SETTING_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      shortName: [null, [Validators.required, Validators.maxLength(64)]],
      ord: [],
      active: [true],
      props: [null, Validators.required],
      parentSettingId: [],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }
}
