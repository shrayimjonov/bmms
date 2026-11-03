import {Component, OnDestroy, OnInit} from '@angular/core';
import {Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {RecipientGroup} from './recipient-group.model';
import {RecipientGroupService} from './recipient-group.service';
import {Recipient} from '../recipient/recipient.model';
import {RecipientService} from '../recipient/recipient.service';
import {Event} from '../../../config/event.constants';
import {getRelations} from '../../../shared/functions';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'recipient-group-update-modal.template.html',
})
export class RecipientGroupUpdateModalComponent extends EntityUpdateBaseComponent<RecipientGroup, RecipientGroupService> implements OnInit, OnDestroy {
  recipients: Recipient[];

  constructor(public readonly recipientService: RecipientService,
              private readonly recipientGroupService: RecipientGroupService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, recipientGroupService, Event.RECIPIENT_GROUP_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      name: [null, [Validators.required, Validators.maxLength(64)]],
      active: [true],
      more: [],
      recipients: [null, Validators.required],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  onSubmit(): void {
    const recipientGroup: RecipientGroup = this.getRawValue();
    recipientGroup.recipients = getRelations(this.editForm.get('recipients').value);

    this.save(recipientGroup);
  }
}
