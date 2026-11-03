import {Component, OnDestroy, OnInit} from '@angular/core';
import {RecipientFileSchema} from './recipient-file-schema.model';
import {Validators} from '@angular/forms';
import {RecipientFileSchemaService} from './recipient-file-schema.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Event} from '../../../config/event.constants';
import {EntityUpdateBaseComponent} from "../../../shared/entity/entity-update-base-component";

@Component({
  templateUrl: 'recipient-file-schema-update.template.html',
})
export class RecipientFileSchemaUpdateComponent extends EntityUpdateBaseComponent<RecipientFileSchema, RecipientFileSchemaService> implements OnInit, OnDestroy {
  constructor(private readonly recipientFileSchemaService: RecipientFileSchemaService,
              private readonly activeModal: NgbActiveModal) {
    super(activeModal, recipientFileSchemaService, Event.RECIPIENT_FILE_SCHEMA_LIST_MODIFICATION);
  }

  ngOnInit(): void {
    this.createEditForm({
      id: [],
      name: [null, [Validators.required, Validators.maxLength(32)]],
      idList: [null, [Validators.required, Validators.maxLength(2048)]],
      root: [null, [Validators.maxLength(512)]],
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }
}
